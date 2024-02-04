package com.example.convoix


import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomDialogBox(state: AppState, hideDialog: () ->Unit, addChat:()->Unit, setEmail:(String)->Unit){
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.90f),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column( modifier = Modifier
                .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(25.dp)) {
                Text(
                    text = "Enter Email ID",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                OutlinedTextField(label = { Text(text = "Enter Email" )}, modifier = Modifier.align(Alignment.CenterHorizontally),
                    value = state.srEmail,
                    onValueChange = { setEmail(it) } ,
                    shape = RoundedCornerShape(20.dp))
                Row {
                    TextButton(onClick = addChat) {
                        Text(text = "Add", style = MaterialTheme.typography.titleLarge, color = Color(0xFF09a129))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = hideDialog) {
                        Text(text = "Cancel", style = MaterialTheme.typography.titleLarge, color= MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}