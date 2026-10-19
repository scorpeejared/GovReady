package com.example.govready

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val MIN_AGE = 15
private const val MAX_AGE = 90

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    transaction: String,
    initial: Details?,
    onBack: () -> Unit,
    onGenerate: (Details) -> Unit
) {
    var age by rememberSaveable { mutableIntStateOf(initial?.age ?: 19) }
    var status by rememberSaveable { mutableStateOf(initial?.status ?: "Student") }
    var firstTime by rememberSaveable { mutableStateOf(initial?.firstTime ?: true) }

    Column(Modifier.fillMaxSize().background(GovColors.Background)) {
        GovTopBar(transaction, onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("A few details so the checklist fits your situation.", color = GovColors.TextMuted, fontSize = 12.sp)
            Spacer(Modifier.height(16.dp))

            SectionLabel("Age", fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StepperButton(Icons.Filled.Remove, "Decrease age") { if (age > MIN_AGE) age-- }
                Text(
                    "$age",
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GovColors.TextDark
                )
                StepperButton(Icons.Filled.Add, "Increase age") { if (age < MAX_AGE) age++ }
            }
            Spacer(Modifier.height(20.dp))

            SectionLabel("Status", fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Student", "Employed", "Self-employed", "Unemployed").forEach {
                    GovChip(it, selected = status == it) { status = it }
                }
            }
            Spacer(Modifier.height(20.dp))

            SectionLabel("First-time applicant?", fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                YesNoButton("Yes", firstTime, Modifier.weight(1f)) { firstTime = true }
                YesNoButton("No", !firstTime, Modifier.weight(1f)) { firstTime = false }
            }
        }

        BottomBar {
            PrimaryButton("Generate my checklist") { onGenerate(Details(transaction, age, status, firstTime)) }
        }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = GovColors.White,
        border = BorderStroke(1.dp, GovColors.Border),
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, description, tint = GovColors.TextDark, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun YesNoButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) GovColors.Navy else GovColors.White,
        border = BorderStroke(1.dp, if (selected) GovColors.Navy else GovColors.Border),
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else GovColors.TextDark
            )
        }
    }
}
