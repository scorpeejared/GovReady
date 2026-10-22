package com.example.govready

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ChecklistScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onEditDetails: () -> Unit
) {
    val details = vm.details ?: return
    val checklist = remember(details) { buildChecklist(details) }
    val total = checklist.requirements.size
    val done = checklist.requirements.count { it.id in vm.checked }
    var showConfirm by rememberSaveable { mutableStateOf(false) }
    val saved = vm.isSaved()

    Column(Modifier.fillMaxSize().background(GovColors.Background)) {
        GovTopBar("Your checklist", onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.fillMaxWidth().background(GovColors.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text(details.transaction, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GovColors.TextDark)
                    Text(
                        details.summary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                        fontSize = 12.sp,
                        color = GovColors.TextMuted
                    )
                    Row(Modifier.fillMaxWidth()) { GovVerifiedChip() }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ready to bring", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GovColors.TextMuted)
                        Text("$done / $total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GovColors.Navy)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (total == 0) 0f else done / total.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = GovColors.Navy,
                        trackColor = GovColors.IconTint,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
                HorizontalDivider(color = GovColors.Border)
            }

            Column(Modifier.padding(16.dp)) {
                GovSectionLabel("Requirements")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    checklist.requirements.forEach { r ->
                        RequirementCard(r, checked = r.id in vm.checked) { vm.toggle(r.id) }
                    }
                }

                Spacer(Modifier.height(20.dp))
                GovSectionLabel("Good to know")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val appt = checklist.appointment
                    MetaRow(
                        Icons.Filled.CalendarToday,
                        if (appt.required) GovColors.Red else GovColors.Green,
                        if (appt.required) GovColors.RedBg else GovColors.GreenBg,
                        if (appt.required) "Appointment required" else "No appointment needed",
                        appt.text
                    )
                    MetaRow(Icons.Filled.AccountBalanceWallet, GovColors.Amber, GovColors.AmberBg, "Estimated fees", checklist.fees)
                    MetaRow(Icons.Filled.LocationOn, GovColors.Navy, GovColors.IconTint, "Where to apply", checklist.where)
                }

                Text(
                    "Prototype uses sample requirements for demonstration.",
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = GovColors.TextMuted
                )
            }
        }

        GovBottomBar {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { if (vm.checked.isEmpty()) onEditDetails() else showConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    color = GovColors.Background,
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.RotateLeft, null, tint = GovColors.TextDark, modifier = Modifier.size(15.dp))
                        Text("Edit details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GovColors.TextDark)
                    }
                }
                Button(
                    onClick = vm::save,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (saved) GovColors.GreenBg else GovColors.Navy,
                        contentColor = if (saved) GovColors.Green else Color.White
                    )
                ) {
                    Icon(if (saved) Icons.Filled.Check else Icons.Filled.Bookmark, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (saved) "Saved" else "Save checklist", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }

    if (showConfirm) {
        ConfirmEditDialog(
            checkedCount = vm.checked.size,
            onKeep = { showConfirm = false },
            onChange = {
                showConfirm = false
                vm.clearChecked()
                onEditDetails()
            }
        )
    }
}

@Composable
private fun RequirementCard(r: Requirement, checked: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = GovColors.White,
        border = BorderStroke(1.dp, if (checked) GovColors.Green else GovColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .size(24.dp)
                    .background(if (checked) GovColors.Green else GovColors.White, RoundedCornerShape(6.dp))
                    .border(1.5.dp, if (checked) GovColors.Green else GovColors.Border, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (checked) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(r.dot, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        r.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GovColors.TextDark,
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Text(
                    r.note,
                    fontSize = 11.sp,
                    color = GovColors.TextMuted,
                    modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, fg: Color, bg: Color, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GovColors.White,
        border = BorderStroke(1.dp, GovColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).background(bg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = fg, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GovColors.TextDark)
                Text(value, fontSize = 11.sp, color = GovColors.TextMuted)
            }
        }
    }
}

@Composable
private fun ConfirmEditDialog(checkedCount: Int, onKeep: () -> Unit, onChange: () -> Unit) {
    Dialog(onDismissRequest = onKeep, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GovColors.White,
            modifier = Modifier.padding(32.dp).widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Change your details?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GovColors.TextDark)
                Text(
                    "Editing rebuilds the checklist and clears the $checkedCount item${if (checkedCount == 1) "" else "s"} you already checked off.",
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 12.sp,
                    color = GovColors.TextMuted
                )
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DialogButton("Keep checklist", GovColors.Background, GovColors.TextDark, Modifier.weight(1f), onKeep)
                    DialogButton("Change details", GovColors.Red, Color.White, Modifier.weight(1f), onChange)
                }
            }
        }
    }
}

@Composable
private fun DialogButton(text: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(8.dp), color = bg, modifier = modifier) {
        Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = fg)
        }
    }
}
