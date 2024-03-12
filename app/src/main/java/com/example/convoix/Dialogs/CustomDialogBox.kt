@file:OptIn(ExperimentalToolkitApi::class)

package com.example.convoix.Dialogs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.convoix.AppState
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.blur.legacyBackgroundBlur

@Composable
fun CustomDialogBox(
    state: AppState,
    hideDialog: () -> Unit,
    addChat: () -> Unit,
    setEmail: (String) -> Unit
) {
    Dialog(
        onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.90f),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onSecondary)
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Text(
                    text = "Enter Email ID",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                OutlinedTextField(
                    label = { Text(text = "Enter Email") },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = state.srEmail,
                    onValueChange = { setEmail(it) },
                    shape = RoundedCornerShape(20.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = addChat) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    TextButton(onClick = hideDialog) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}