package com.example.convoix.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.Message
import com.example.convoix.UserData
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController, viewModel: ChatViewModel, messages: List<Message>, userData: UserData, sendReply:(String, String)->Unit, chatId:String, state: AppState, onBack:()->Unit) {
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = Unit){
        viewModel.popMessage(state.chatId)
    }
    BackHandler {
        navController.popBackStack()
        viewModel.dePopMsg()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .clickable { navController.navigate("otherprofile") }
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                }},
            )

        }
    ) {it->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(10.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)) {
                TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = "Message")},
                    value = reply,
                    onValueChange = { reply=it },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ))
                AnimatedVisibility(reply.isNotEmpty()) {
                    IconButton(onClick = { sendReply(reply,chatId)
                        reply=""}) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null )
                    }
                }

            }

        }
    }

}

@Composable
fun MessageItem(message: Message, state: AppState) {
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF124770),
        Color(0xFF124770)
    ))
    val isCurrentUser = state.userData?.userId == message.senderId
    val color = if (isCurrentUser) brush else  brush2
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var onClick by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 10.dp),
        contentAlignment = alignment
    ) {

        Column(
            modifier = Modifier
                .clickable { onClick = !onClick }
                .widthIn(max = 270.dp)
                .fillMaxHeight()
                .background(color, RoundedCornerShape(16.dp)), horizontalAlignment = Alignment.End
        ) {

            Text(
                text = message.content.toString(),
                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = Color.White
            )
            Text(
                text = formatter.format(message.time?.toDate()!!),
                modifier = Modifier.padding(end = 8.dp, bottom = 5.dp, start = 8.dp, top = 2.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
        AnimatedVisibility(onClick && !isCurrentUser) {
            OptionsBox()
        }
    }

}
@Composable
fun ReactionBox(){
   // Row(modifier = Modifier.graphicsLayer { translationX = 200f } , horizontalArrangement = Arrangement.spacedBy(5.dp)) {
   //     Text(text = "😂",style = MaterialTheme.typography.headlineSmall)
    //    Text(text = "👍",style = MaterialTheme.typography.headlineSmall)
    //    Text(text = "❤️",style = MaterialTheme.typography.headlineSmall)
    //    Text(text = "😭",style = MaterialTheme.typography.headlineSmall)
    //    Text(text = "😮",style = MaterialTheme.typography.headlineSmall)
   // }
}

@Composable
fun OptionsBox() {
    Box(
        modifier = Modifier
            .graphicsLayer { translationX = 200f }
            .width(100.dp)
            .height(50.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Copy")
            Text("Delete")
        }
    }
}