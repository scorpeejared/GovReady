package com.example.govready

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle

/** Single source of truth for the transactions offered on Home and Purpose. */
val Transactions = listOf("Driver's License", "Passport", "NBI Clearance", "Postal ID")

/** The answers offered for the status question on Details. */
val Statuses = listOf("Student", "Employed", "Self-employed", "Unemployed")

/** Applicants younger than this need a parent or guardian's consent (sample rule). */
const val ADULT_AGE = 18

data class Details(
    val transaction: String,
    val age: Int,
    val status: String,
    val firstTime: Boolean
) {
    val summary: String
        get() = "${if (firstTime) "First-time" else "Renewal"} · $status · age $age"
}

data class Requirement(val id: String, val title: String, val note: String, val dot: Color)

data class Appointment(val required: Boolean, val text: String)

data class Checklist(
    val requirements: List<Requirement>,
    val appointment: Appointment,
    val fees: String,
    val where: String
)

data class SavedChecklist(val details: Details, val checkedIds: List<String>)

private object Category {
    val Id = Color(0xFF2563EB)
    val Medical = Color(0xFF0E9488)
    val Course = Color(0xFF7C3AED)
    val Permit = Color(0xFFC2410C)
    val Form = Color(0xFF5A6577)
    val Proof = Color(0xFF0E9488)
}

/** Sample data only - prototype. Replace with real data / API later. */
fun buildChecklist(d: Details): Checklist {
    val base = baseChecklist(d)
    return base.copy(requirements = base.requirements + personalRequirements(d))
}

/** Extra items that depend on the applicant's age and work status, so every answer shapes the result. */
private fun personalRequirements(d: Details): List<Requirement> = buildList {
    if (d.age < ADULT_AGE) {
        add(Requirement("guardian", "Parent or guardian consent", "Signed consent and their valid ID", Category.Permit))
    }
    when (d.status) {
        "Employed" -> add(Requirement("work", "Company ID or employment certificate", "Supporting proof of identity", Category.Proof))
        "Self-employed" -> add(Requirement("work", "Business permit or registration", "Supporting proof of identity", Category.Proof))
    }
}

private fun baseChecklist(d: Details): Checklist = when (d.transaction) {
    "Driver's License" -> Checklist(
        requirements = buildList {
            add(Requirement("id", "Valid government-issued ID", "Or PSA birth certificate if none", Category.Id))
            add(Requirement("med", "Medical certificate", "LTO-accredited clinic, includes drug test", Category.Medical))
            add(Requirement("form", "Accomplished application form", "Filled at LTO or the online portal", Category.Form))
            if (d.firstTime) {
                add(Requirement("tdc", "Theoretical Driving Course certificate", "15-hour course, first-time applicants only", Category.Course))
                add(Requirement("permit", "Student permit", "Held at least 1 month before the license", Category.Permit))
            }
        },
        appointment = Appointment(true, "LTO online appointment needed"),
        fees = "Approx. ₱600 to ₱1,300",
        where = "LTO Licensing Center"
    )
    "Passport" -> Checklist(
        requirements = listOf(
            Requirement("psa", "PSA birth certificate", "Original plus a photocopy", Category.Id),
            Requirement("gov", "One valid government ID", "With your name, photo, and signature", Category.Id)
        ),
        appointment = Appointment(true, "DFA online appointment required"),
        fees = "₱950 regular · ₱1,200 express",
        where = "DFA Consular Office"
    )
    "NBI Clearance" -> Checklist(
        requirements = listOf(
            Requirement("id1", "Two valid government IDs", "At least one with photo and signature", Category.Id),
            Requirement("reg", "Online registration reference", "Created on the NBI Clearance website", Category.Form)
        ),
        appointment = Appointment(true, "Online registration and appointment"),
        fees = "Approx. ₱130 plus service fee",
        where = "NBI Clearance Center"
    )
    else -> Checklist( // Postal ID
        requirements = listOf(
            Requirement("id", "Valid ID or PSA birth certificate", "Proof of identity", Category.Id),
            Requirement("addr", "Proof of address", "Barangay certificate or a recent utility bill", Category.Proof)
        ),
        appointment = Appointment(false, "Walk-in accepted at most post offices"),
        fees = "Approx. ₱504",
        where = "Post Office"
    )
}

/**
 * Backed by [SavedStateHandle] so the in-progress checklist comes back if Android kills the
 * app process while it is in the background. The saved list is also written to private app
 * storage, so it survives the app being closed completely.
 */
class AppViewModel(app: Application, private val state: SavedStateHandle) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences(PREFS, Application.MODE_PRIVATE)

    var details by mutableStateOf(state.get<String>(KEY_DETAILS)?.toDetails())
        private set
    val checked = mutableStateListOf<String>().apply {
        addAll(state.get<ArrayList<String>>(KEY_CHECKED).orEmpty())
    }
    val saved = mutableStateListOf<SavedChecklist>().apply {
        val onDevice = prefs.getString(KEY_SAVED, null)?.split(RECORD_SEP).orEmpty().filter { it.isNotEmpty() }
        addAll((onDevice.ifEmpty { state.get<ArrayList<String>>(KEY_SAVED).orEmpty() }).mapNotNull { it.toSavedChecklist() })
    }

    fun start(d: Details) {
        details = d
        checked.clear()
        persist()
    }

    fun toggle(id: String) {
        if (id in checked) checked.remove(id) else checked.add(id)
        persist()
    }

    fun clearChecked() {
        checked.clear()
        persist()
    }

    fun openSaved(s: SavedChecklist) {
        details = s.details
        checked.clear()
        checked.addAll(s.checkedIds)
        persist()
    }

    fun isSaved(): Boolean = saved.any { it.details == details }

    fun save() {
        val d = details ?: return
        saved.removeAll { it.details == d }
        saved.add(0, SavedChecklist(d, checked.toList()))
        persist()
    }

    private fun persist() {
        // A saved checklist follows the ticks, so the "Saved" button never promises more than is stored.
        val i = saved.indexOfFirst { it.details == details }
        if (i >= 0 && saved[i].checkedIds != checked.toList()) saved[i] = saved[i].copy(checkedIds = checked.toList())
        state[KEY_DETAILS] = details?.encode()
        state[KEY_CHECKED] = ArrayList(checked)
        val encoded = saved.map { it.encode() }
        state[KEY_SAVED] = ArrayList(encoded)
        prefs.edit().putString(KEY_SAVED, encoded.joinToString(RECORD_SEP)).apply()
    }

    private companion object {
        const val PREFS = "govready"
        const val RECORD_SEP = "\u001E"
        const val KEY_DETAILS = "details"
        const val KEY_CHECKED = "checked"
        const val KEY_SAVED = "saved"
    }
}

private const val SEP = "\u001F"

private fun Details.encode() = listOf(transaction, age, status, firstTime).joinToString(SEP)

private fun String.toDetails(): Details? {
    val p = split(SEP)
    val age = p.getOrNull(1)?.toIntOrNull() ?: return null
    return if (p.size == 4) Details(p[0], age, p[2], p[3].toBoolean()) else null
}

private fun SavedChecklist.encode() = details.encode() + SEP + checkedIds.joinToString(",")

private fun String.toSavedChecklist(): SavedChecklist? {
    val sep = lastIndexOf(SEP)
    if (sep < 0) return null
    val details = substring(0, sep).toDetails() ?: return null
    val ids = substring(sep + 1).split(",").filter { it.isNotEmpty() }
    return SavedChecklist(details, ids)
}
