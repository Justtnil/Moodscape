package com.example.moodscape.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NotificationPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't dismiss automatically */ },
        title = {
            Text(
                text = "Daily Mood Reminders",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Would you like to receive daily reminders to log your mood?")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "You can change this setting anytime in the app settings.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Yes, I'd like reminders")
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Not now")
                }
            }
        }
    )
}