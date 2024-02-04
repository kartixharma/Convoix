package com.example.convoix

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ChatScreen(viewModel: ChatViewModel, state: AppState){
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
            items(viewModel.chats){
                val chatUser = if(it.user1?.userId!=state.userData?.userId) { it.user1 } else it.user2
                ChatItem(ppurl = chatUser?.ppurl.toString(), username = chatUser?.username.toString())
            }
        }
    }
}

@Composable
fun ChatItem(ppurl: String, username: String){
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = ppurl,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(60.dp)
        )
        Text(modifier = Modifier.padding(16.dp),text = username)
    }
}