package com.example.convoix.Dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.convoix.ChatViewModel

@Composable
fun ForwardMsgDialog(hideDialog: () ->Unit, ForwardMsg:()->Unit, number: Int, viewModel: ChatViewModel){
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimary)
        ) {
            Column( modifier = Modifier
                .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Forward "+ if(viewModel.forwardMsgs.size==1) "message" else "${viewModel.forwardMsgs.size} messages",
                    style = MaterialTheme.typography.titleLarge,)
                Row {
                    TextButton(onClick = hideDialog) {
                        Text(text = "Cancel", style = MaterialTheme.typography.titleMedium, color= MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = ForwardMsg ) {
                        Text(text = "Forward", style = MaterialTheme.typography.titleMedium)
                    }

                }
            }
        }
    }
}
