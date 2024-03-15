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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.convoix.UserData

@Composable
fun ProfileEditDialog(hideDialog: () ->Unit, saveProfile:(String, String)->Unit, user: UserData?){
    var name by remember {
        mutableStateOf(user?.username)
    }
    var bio by remember {
        mutableStateOf(user?.bio)
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimary)
        ) {
            Column( modifier = Modifier
                .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Edit profile",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(modifier = Modifier.width(250.dp),
                    value = name.toString(),
                    label = { Text(text = "Name") },
                    onValueChange = { name = it },
                    placeholder = { Text(text = "Name")},
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    )
                )
                OutlinedTextField(modifier = Modifier.width(250.dp),
                    value = bio.toString(),
                    label = { Text(text = "Bio") },
                    onValueChange = { bio = it },
                    placeholder = { Text(text = "Bio")},
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                Row {
                    TextButton(onClick = hideDialog) {
                        Text(text = "Cancel", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { saveProfile(name!!, bio!!) } ) {
                        Text(text = "Save", style = MaterialTheme.typography.titleMedium, color= Color(
                            0xFF22B122
                        )
                        )
                    }

                }
            }
        }
    }
}
