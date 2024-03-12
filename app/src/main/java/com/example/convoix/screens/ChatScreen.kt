@file:OptIn(ExperimentalToolkitApi::class)

package com.example.convoix.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.convoix.AppState
import com.example.convoix.ChatData
import com.example.convoix.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.Dialogs.CustomDialogBox
import com.example.convoix.Dialogs.DeleteDialog
import com.example.convoix.R
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.blur.legacyBackgroundBlur
import com.primex.core.noise
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel, state: AppState, showSingleChat: (ChatUserData, String) -> Unit){
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
    var showSearch by remember {
        mutableStateOf(false)
    }
    val padding by animateDpAsState(targetValue = if (showSearch) 10.dp else 0.dp)
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
        searchText=""
        showSearch=false
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
                Icon(Icons.Filled.AddComment, contentDescription = "ADD", tint = Color.White)
            }
        }
    ){it->
        Image(
            painter = painterResource(id = R.drawable.blurry_gradient_haikei),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
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
        Column(modifier = Modifier
            .padding(top = 36.dp)) {//.background(colorScheme.primaryContainer)
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
                            showDialog = true
                            isSelected = false
                        }) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
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
                                focusedIndicatorColor = Color.Gray,
                                unfocusedIndicatorColor = Color.DarkGray,
                                unfocusedLeadingIconColor = Color.White,
                                focusedLeadingIconColor = Color.White,
                                unfocusedTrailingIconColor = Color.White,
                                focusedTrailingIconColor = Color.White,
                                focusedPlaceholderColor = Color.White,
                                unfocusedPlaceholderColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null)},
                            trailingIcon = {
                                if(!searchText.isBlank())
                                    IconButton(onClick = { searchText = ""}) {
                                        Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                                }

                            }
                        )
                    }
                }
                if (!isSelected && !showSearch) {
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
                        IconButton(modifier = Modifier
                            .background(colorScheme.background.copy(alpha = 0.2f), CircleShape)
                            .border(0.05.dp, Color.DarkGray, CircleShape),
                                onClick = { showSearch = true }) {
                            Icon(
                                modifier = Modifier.scale(0.7f),
                                painter = painterResource(id = R.drawable._666693_search_icon),
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            IconButton(modifier = Modifier
                                .background(colorScheme.background.copy(alpha = 0.2f), CircleShape)
                                .border(0.05.dp, Color.DarkGray, CircleShape),
                                onClick = { expanded=true }) {
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
                                        onClick = { navController.navigate("settings") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyColumn(modifier= Modifier
                .padding(top = padding)
                .fillMaxHeight()
                .background(
                    colorScheme.background.copy(alpha = 0.2f),
                    RoundedCornerShape(30.dp, 30.dp)
                )
                .border(0.05.dp, Color.DarkGray, RoundedCornerShape(30.dp, 30.dp))){
                items(filteredChats){
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(!userData.status){
            AsyncImage(
                model = userData.ppurl,
                placeholder = painterResource(id = R.drawable.person_placeholder_4),
                error = painterResource(id = R.drawable.person_placeholder_4),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
            )
        }
        else{
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(userData.ppurl).build(),
                placeholder = painterResource(id = R.drawable.person_placeholder_4),
                error = painterResource(id = R.drawable.person_placeholder_4),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val path = Path()
                        path.addOval(
                            Rect(
                                topLeft = Offset.Zero,
                                bottomRight = Offset(size.width, size.height)
                            )
                        )
                        onDrawWithContent {
                            clipPath(path) {
                                this@onDrawWithContent.drawContent()
                            }
                            val dotSize = size.width / 8f
                            drawCircle(
                                Color.Black,
                                radius = dotSize,
                                center = Offset(
                                    x = size.width - dotSize,
                                    y = size.height - dotSize
                                ),
                                blendMode = BlendMode.Clear
                            )
                            drawCircle(
                                Color(0xFF60BB47), radius = dotSize * 0.8f,
                                center = Offset(
                                    x = size.width - dotSize,
                                    y = size.height - dotSize
                                )
                            )
                        }
                    }
            )
        }
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light)
                    )
                    Text(
                        text = if(chat.last?.time.toString()!="null") formatter.format(chat.last?.time?.toDate()!!) else "",
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
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