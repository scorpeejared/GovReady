package com.example.govready

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    saved: List<SavedChecklist>,
    inProgress: Details?,
    checkedCount: Int,
    onCheckRequirements: () -> Unit,
    onPopularClick: (String) -> Unit,
    onInProgressClick: () -> Unit,
    onSeeAll: () -> Unit,
    onSavedClick: (SavedChecklist) -> Unit
) {
    Column(Modifier.fillMaxSize().background(GovColors.Background)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(GovColors.Navy)
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.13f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text("GovReady", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Know exactly what to bring before you go.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Button(
                onClick = onCheckRequirements,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GovColors.Navy)
            ) {
                Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Check requirements", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
            }

            if (inProgress != null) {
                Spacer(Modifier.height(20.dp))
                GovSectionLabel("In progress")
                Spacer(Modifier.height(8.dp))
                Surface(
                    onClick = onInProgressClick,
                    shape = RoundedCornerShape(12.dp),
                    color = GovColors.White,
                    border = BorderStroke(1.dp, GovColors.IconTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                inProgress.transaction,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GovColors.TextDark
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                                tint = GovColors.TextMuted, modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "$checkedCount of ${buildChecklist(inProgress).requirements.size} ready",
                            modifier = Modifier.padding(top = 2.dp),
                            fontSize = 13.sp,
                            color = GovColors.TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { GovSectionLabel("Saved") }
                if (saved.isNotEmpty()) {
                    Text(
                        "See all",
                        modifier = Modifier.clickable(onClick = onSeeAll).padding(vertical = 12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GovColors.Navy
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (saved.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dashedBorder(GovColors.Border)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.BookmarkBorder, null, tint = GovColors.TextMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Save a checklist to keep it for later.", color = GovColors.TextMuted, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    saved.take(3).forEach { SavedCard(it, compact = true) { onSavedClick(it) } }
                }
            }

            Spacer(Modifier.height(24.dp))
            GovSectionLabel("Popular")
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Transactions.forEach { GovChip(it) { onPopularClick(it) } }
            }
        }
    }
}
