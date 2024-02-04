package com.example.convoix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.Message
import com.example.convoix.UserData

@Composable
fun Chat(messages: List<Message>, userData: UserData, sendReply:(String, String)->Unit, chatId:String, state: AppState) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = userData.ppurl,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(40.dp)
            )
            Text(modifier = Modifier.padding(16.dp),text = userData.username.toString())
        }

        // Message list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages) { message ->
                MessageItem(message = message, state)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(modifier = Modifier.weight(1f), label = { Text(text = "Enter message here" )},
                value = reply,
                onValueChange = { reply=it } ,
                shape = RoundedCornerShape(20.dp))
            IconButton(onClick = { sendReply(reply,chatId)
            reply=""}) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = null )
            }
        }

    }
}

@Composable
fun MessageItem(message: Message, state: AppState) {
    val isCurrentUser = state.userData?.userId == message.senderId
    val color = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else Color.Gray
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier.widthIn(max = 250.dp)
                .background(color, RoundedCornerShape(12.dp))
                .padding(10.dp),
        ) {
            Text(
                text = message.content.toString(),
                modifier = Modifier.padding(0.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            //Text(
            //    text = message.content.toString(),
            //    modifier = Modifier.padding(0.dp),
            //    style = MaterialTheme.typography.bodyLarge
            //)
        }
    }
}
