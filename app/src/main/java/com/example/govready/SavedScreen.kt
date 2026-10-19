package com.example.govready

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SavedScreen(
    saved: List<SavedChecklist>,
    onBack: () -> Unit,
    onOpen: (SavedChecklist) -> Unit
) {
    Column(Modifier.fillMaxSize().background(GovColors.Background)) {
        GovTopBar("Saved checklists", onBack)

        if (saved.isEmpty()) {
            Text(
                "Nothing saved yet.",
                color = GovColors.TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(saved) { SavedCard(it) { onOpen(it) } }
            }
        }
    }
}

/** [compact] is the Home variant (title and chip on one row); the default is the Saved screen card. */
@Composable
fun SavedCard(item: SavedChecklist, compact: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = GovColors.White,
        border = BorderStroke(1.dp, GovColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (compact) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.details.transaction, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GovColors.TextDark)
                    Text(item.details.summary, fontSize = 11.sp, color = GovColors.TextMuted)
                }
                Spacer(Modifier.width(8.dp))
                VerifiedChip()
            }
        } else {
            Column(Modifier.padding(16.dp)) {
                Text(item.details.transaction, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GovColors.TextDark)
                Text(item.details.summary, fontSize = 11.sp, color = GovColors.TextMuted)
                Spacer(Modifier.height(8.dp))
                VerifiedChip()
            }
        }
    }
}
