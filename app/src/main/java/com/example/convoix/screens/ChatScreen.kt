package com.example.convoix.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatData
import com.example.convoix.ChatViewModel
import com.example.convoix.CustomDialogBox
import com.example.convoix.UserData

@Composable
fun ChatScreen(viewModel: ChatViewModel, state: AppState, showSingleChat: (UserData, String) -> Unit){
    val chats = viewModel.chats
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {viewModel.showDialog()},
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.size(70.dp),
                containerColor = colorScheme.inversePrimary
            ){
                Icon(Icons.Filled.Add, contentDescription = "ADD", Modifier.size(35.dp), tint = Color.White)
            }
        }

    ){it->
        AnimatedVisibility(state.showDialog){
            CustomDialogBox(
                state = state,
                hideDialog = {viewModel.hideDialog()},
                addChat = { viewModel.addChat(state.srEmail)
                          viewModel.hideDialog()
                          viewModel.setSrEmail("")} ,
                setEmail = {viewModel.setSrEmail(it)}
            )
        }
        LazyColumn(modifier= Modifier.padding(it)){
            items(chats){
                val chatUser = if(it.user1?.userId!=state.userData?.userId) { it.user1 } else it.user2
                ChatItem(chatUser!!, showSingleChat = {user, id-> showSingleChat(user, id)}, it)
            }
        }
    }
}

@Composable
fun ChatItem(userData: UserData, showSingleChat:(UserData, String)->Unit, chat: ChatData){
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { showSingleChat(userData, chat.chatId) },
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = userData.ppurl,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .size(60.dp)
        )
        Column {
            Text(modifier = Modifier,text = userData.username.toString())
            Text(modifier = Modifier.padding(end = 100.dp), text = chat.last?.content.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)
        }
    }
    Divider(modifier = Modifier.padding(horizontal = 10.dp))
}