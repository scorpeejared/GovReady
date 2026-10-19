package com.example.govready

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PurposeScreen(onBack: () -> Unit, onSelect: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(GovColors.Background)) {
        GovTopBar("What do you need?", onBack)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(
                "Choose the transaction you are preparing for.",
                color = GovColors.TextMuted,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Transactions.forEach { name ->
                    Surface(
                        onClick = { onSelect(name) },
                        shape = RoundedCornerShape(12.dp),
                        color = GovColors.White,
                        border = BorderStroke(1.dp, GovColors.Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .background(GovColors.IconTint, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Description, null, tint = GovColors.Navy, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                name,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = GovColors.TextDark
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward, null,
                                tint = GovColors.TextMuted, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
