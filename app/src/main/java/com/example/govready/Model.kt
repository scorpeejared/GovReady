package com.example.govready

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

/** Single source of truth for the transactions offered on Home and Purpose. */
val Transactions = listOf("Driver's License", "Passport", "NBI Clearance", "Postal ID")

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
fun buildChecklist(d: Details): Checklist = when (d.transaction) {
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

class AppViewModel : ViewModel() {
    var details by mutableStateOf<Details?>(null)
        private set
    val checked = mutableStateListOf<String>()
    val saved = mutableStateListOf<SavedChecklist>()

    fun start(d: Details) {
        details = d
        checked.clear()
    }

    fun toggle(id: String) {
        if (id in checked) checked.remove(id) else checked.add(id)
    }

    fun clearChecked() = checked.clear()

    fun openSaved(s: SavedChecklist) {
        details = s.details
        checked.clear()
        checked.addAll(s.checkedIds)
    }

    fun isSaved(): Boolean = saved.any { it.details == details }

    fun save() {
        val d = details ?: return
        saved.removeAll { it.details == d }
        saved.add(0, SavedChecklist(d, checked.toList()))
    }
}
