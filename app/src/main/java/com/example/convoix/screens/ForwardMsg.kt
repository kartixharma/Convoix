package com.example.convoix.screens



import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.convoix.AppState
import com.example.convoix.Firebase.ChatData
import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.Dialogs.ForwardMsgDialog
import com.example.convoix.R

@Composable
fun ForwardMsg(navController: NavController, viewModel: ChatViewModel, state: AppState) {
    val chats = viewModel.chats
    var searchText by rememberSaveable { mutableStateOf("") }
    val filteredChats = if (searchText.isBlank()) {
        chats
    } else {
        chats.filter {
            if(it.user1?.username==state.userData?.username){
                it.user2?.username.toString().startsWith(searchText, ignoreCase = true)
            }
            else{
                it.user1?.username.toString().startsWith(searchText, ignoreCase = true)
            }

        }
    }
    var selectionMode by remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    var showSearch by remember {
        mutableStateOf(false)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    BackHandler {
        if(showDialog || searchText.isNotBlank() || showSearch || selectedItem.isNotEmpty() || selectionMode) {
            showDialog = false
            searchText = ""
            showSearch = false
            selectedItem.clear()
            selectionMode = false
        }
        else {
            viewModel.forwardMsgs.clear()
            navController.popBackStack()
        }
    }
        Image(
            painter = painterResource(id = R.drawable.blck_blurry),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    AnimatedVisibility(visible = showDialog) {
        ForwardMsgDialog(hideDialog = { showDialog = false ; selectedItem.clear() },
            ForwardMsg = {
                viewModel.forwardMsg(selectedItem)
                navController.popBackStack()
            },
        selectedItem.size,
            viewModel
        )
    }
        Column(modifier = Modifier
            .padding(top = 36.dp)) {
            Box {
                this@Column.AnimatedVisibility(
                    selectionMode,
                    enter = slideInVertically(),
                    exit = slideOutVertically()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        IconButton(modifier = Modifier.padding(5.dp),
                            onClick = {
                                selectionMode = false
                                selectedItem.clear()
                            }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = null
                            )
                        }
                        Text(text = selectedItem.size.toString(),
                            modifier = Modifier.padding(start = 20.dp),
                            style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            showDialog = true
                            selectionMode = false
                        }) {
                            Icon(modifier = Modifier.size(25.dp), painter = painterResource(id = R.drawable.pngwing_com), contentDescription = null)
                        }
                    }
                }
                this@Column.AnimatedVisibility(
                    showSearch,
                    enter = slideInVertically(),
                    exit = slideOutVertically()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text(text = "Search") },
                            shape = CircleShape,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent.copy(alpha = 0.2f),
                                focusedContainerColor = Color.Transparent.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color(0xFF35567A),
                                unfocusedIndicatorColor = Color(0xFF233E5C),
                                unfocusedLeadingIconColor = Color.White,
                                focusedLeadingIconColor = Color.White,
                                unfocusedTrailingIconColor = Color.White,
                                focusedTrailingIconColor = Color.White,
                                focusedPlaceholderColor = Color.White,
                                unfocusedPlaceholderColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            leadingIcon = { Icon(modifier = Modifier.size(25.dp),
                                painter = painterResource(id = R.drawable._666693_search_icon), contentDescription = null)},
                            trailingIcon = {
                                if(searchText.isNotBlank())
                                    IconButton(onClick = { searchText = ""}) {
                                        Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                                    }

                            }
                        )
                    }
                }
                if (!selectionMode && !showSearch) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(0.98f)
                    ) {
                        Column {
                            Text(
                                text = "Forward "+ viewModel.forwardMsgs.size+if(viewModel.forwardMsgs.size==1) " message" else " messages",
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(modifier = Modifier
                            .background(colorScheme.background.copy(alpha = 0.2f), CircleShape)
                            .border(0.05.dp, Color(0xFF35567A), CircleShape),
                            onClick = { showSearch = true }) {
                            Icon(
                                modifier = Modifier.scale(0.7f),
                                painter = painterResource(id = R.drawable._666693_search_icon),
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
            }
            LazyColumn(modifier= Modifier
                .fillMaxSize()) {
                items(filteredChats) {
                    val chatUser = if(it.user1?.userId!=state.userData?.userId) { it.user1 } else it.user2
                    Item(state,
                        chatUser!!,it,
                        selected = { chatId ->
                            selectionMode = true
                            if(selectedItem.contains(chatId))  { selectedItem.remove(chatId)
                                if (selectedItem.size==0) selectionMode = false
                            }
                            else selectedItem.add(chatId)
                                   },
                        isSelected = selectedItem.contains(it.chatId)
                    )
                }
            }
        }
}
@Composable
fun Item(state: AppState,
             userData: ChatUserData,
             chat: ChatData,
             selected:(String)->Unit,
             isSelected: Boolean,

) {
    val color = if(!isSelected) Color.Transparent else colorScheme.onPrimary
    Row(
        modifier = Modifier
            .background(color)
            .fillMaxWidth()
            .clickable {
                selected(chat.chatId)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userData.ppurl)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                placeholder = painterResource(id = R.drawable.person_placeholder_4),
                error = painterResource(id = R.drawable.person_placeholder_4),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
            )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if(userData.userId==state.userData?.userId) userData.username.orEmpty() + " (You)" else userData.username.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}