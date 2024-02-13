package com.example.convoix.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatData
import com.example.convoix.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.CustomDialogBox
import com.example.convoix.DeleteDialog
import com.example.convoix.UserData
import com.skydoves.cloudy.Cloudy
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel, state: AppState, showSingleChat: (ChatUserData, String) -> Unit){
    val chats = viewModel.chats
    var isSelected by remember {
        mutableStateOf(false)
    }
    var dltChatId by remember {
        mutableStateOf("")
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedItems = mutableMapOf<Int, Boolean>()
    BackHandler {
        isSelected=false
        selectedItems.clear()
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {viewModel.showDialog()},
                shape = RoundedCornerShape(50.dp),
                containerColor = colorScheme.inversePrimary
            ){
                Icon(Icons.Filled.Add, contentDescription = "ADD", tint = Color.White)
            }
        }
    ){it->
        AnimatedVisibility(showDialog) {
            DeleteDialog(
                hideDialog = { showDialog = !showDialog
                    selectedItems.clear()},
                deleteChat = { viewModel.deleteChat(dltChatId)
                    showDialog = !showDialog
                    selectedItems.clear()}
            )
        }
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
        Column(modifier = Modifier.padding(top = 36.dp)) { //.background(colorScheme.primaryContainer)
            Box {
                this@Column.AnimatedVisibility(
                    isSelected,
                    enter = slideInVertically(),
                    exit = slideOutVertically()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        IconButton(modifier = Modifier.padding(9.dp),
                            onClick = {
                                isSelected = false
                                selectedItems.clear()
                            }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = {
                            showDialog=true
                            isSelected = false
                        }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
                if (!isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.98f)
                    ) {
                        Text(
                            text = "Chats",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { }) {
                            Icon(
                                modifier = Modifier.scale(1.3f),
                                imageVector = Icons.Filled.Search,
                                contentDescription = null
                            )
                        }
                        Column {
                            IconButton(onClick = { expanded=true }) {
                                Icon(
                                    modifier = Modifier.scale(1.3f),
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = null
                                )
                            }
                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))) {
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Profile",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = { navController.navigate("profile")
                                        expanded=false }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Settings",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        onClick = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(modifier= Modifier
                .padding()
                .background(colorScheme.background, RoundedCornerShape(30.dp, 30.dp))){
                items(chats){
                        val chatUser = if(it.user1?.userId!=state.userData?.userId) { it.user1 } else it.user2
                        ChatItem(selectedItems[chats.indexOf(it)], chatUser!!, showSingleChat = { user, id-> showSingleChat(user, id)}, it, showRow = { id->
                            selectedItems[chats.indexOf(it)] = true
                            dltChatId = id
                            isSelected = true
                        })
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(isSelected: Boolean?, userData: ChatUserData, showSingleChat: (ChatUserData, String) -> Unit, chat: ChatData, showRow:(String)->Unit) {
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    val color = if(isSelected==null || isSelected==false) Color.Transparent else colorScheme.secondaryContainer
    Row(
        modifier = Modifier
            .background(color)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showSingleChat(userData, chat.chatId) },
                onLongClick = { showRow(chat.chatId) })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userData.ppurl,
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .size(60.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = userData.username.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AnimatedVisibility(chat.last?.time!=null && !userData.typing) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text( modifier = Modifier.width(200.dp),
                        text = chat.last?.content.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if(chat.last?.time.toString()!="null") formatter.format(chat.last?.time?.toDate()!!) else "",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            if(userData.typing){
                Text(
                    text = "Typing...",
                    color = Color(0xFF3075FF),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}