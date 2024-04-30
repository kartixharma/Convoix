package com.example.convoix.Dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddReaction
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.Firebase.Message
import com.example.convoix.Firebase.Story
import com.makeappssimple.abhimanyu.composeemojipicker.ComposeEmojiPickerBottomSheetUI
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StoryDialog(appState: AppState,
                viewModel: ChatViewModel,
                story: Story,
                hideDialog:()->Unit,
                deleteStory:(Int)->Unit
){
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var dialog by remember {
        mutableStateOf(false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pagerState = rememberPagerState(
        pageCount = { story.images.size }, initialPage = 0
    )
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    var onClick by remember {
        mutableStateOf(false)
    }
    var isModalBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var searchText by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))) {
        }
        if (isModalBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = sheetState,
                shape = RectangleShape,
                tonalElevation = 0.dp,
                onDismissRequest = {
                    isModalBottomSheetVisible = false
                    searchText = ""
                },
                dragHandle = null,
                windowInsets = WindowInsets(0),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    ComposeEmojiPickerBottomSheetUI(
                        onEmojiClick = { emoji ->
                            isModalBottomSheetVisible = false
                            viewModel.storyReaction(story.id, pagerState.currentPage, reaction = emoji.character, story.userId)
                        },
                        onEmojiLongClick = { emoji ->
                            Toast.makeText(
                                context,
                                emoji.unicodeName,
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        searchText = searchText,
                        updateSearchText = { updatedSearchText ->
                            searchText = updatedSearchText
                        },
                    )
                }
            }
        }
        if(showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                LazyColumn(modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)) {
                    item{
                        Text(modifier = Modifier
                            .padding(bottom = 16.dp),
                            text = "Viewers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(story.images[pagerState.currentPage].viewedBy) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = it.ppurl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp)
                            )
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                Text(
                                    text = it.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatter.format(it.time?.toDate()!!),
                                    color = Color.LightGray,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = it.reaction,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
            HorizontalPager(state = pagerState) {
                AsyncImage(
                    model = story.images[it].imgUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .clickable(interactionSource = interactionSource, indication = null) {}
                        .fillMaxSize()
                )
            }
            AnimatedVisibility(visible = !isPressed, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { hideDialog() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBackIosNew,
                                contentDescription = null
                            )
                        }
                        AsyncImage(
                            model = story.ppurl,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(40.dp)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = if (story.userId == appState.userData?.userId) story.username.toString() + " (You)" else story.username.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatter.format(story.images[pagerState.currentPage].time?.toDate()!!),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (story.userId == appState.userData?.userId) {
                            IconButton(onClick = { dialog = true }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            }
                        }else{
                            IconButton(onClick = { onClick = true }) {
                                Icon(imageVector = Icons.Filled.AddReaction, contentDescription = null)
                            }
                            Column{
                                MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                                    0xFF294F86
                                )
                                ) ) {
                                    DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                                        expanded = onClick,
                                        onDismissRequest = { onClick = false },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 10.dp)) {
                                            listOf("😂", "👍", "❤️", "😭", "😮").forEach {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    modifier = Modifier.clickable { viewModel.storyReaction(id = story.id, index = pagerState.currentPage, reaction = it, story.userId)
                                                        onClick = false
                                                    }
                                                )
                                            }
                                            Icon(
                                                modifier = Modifier
                                                    .size(35.dp)
                                                    .clickable {
                                                        isModalBottomSheetVisible=true
                                                        onClick = false
                                                    },
                                                imageVector = Icons.Filled.AddCircle,
                                                contentDescription = null)
                                        }

                                    }
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                           repeat(story.images.size) {
                               val color = if(it == pagerState.currentPage) Color(0xFF0F70AF) else Color.LightGray
                               Box(modifier = Modifier
                                   .padding(3.dp)
                                   .height(5.dp)
                                   .weight(1f)
                                   .background(color, CircleShape)) {
                               }
                           }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (story.userId != appState.userData?.userId){
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .background(MaterialTheme.colorScheme.onPrimary, CircleShape)) {
                            TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = "Reply...")},
                                value = reply,
                                onValueChange = { reply=it },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                            )
                            AnimatedVisibility(visible = reply.isNotBlank()) {
                                IconButton(modifier = Modifier
                                    .padding(end = 4.dp)
                                    .background(brush, CircleShape) ,
                                    onClick = { viewModel.findChatId(story.userId){
                                        viewModel.sendReply(chatId = it, replyMsg = Message().copy(imgUrl = story.images[pagerState.currentPage].imgUrl, content = "Replied to story"), msg = reply)
                                        reply=""
                                        Toast.makeText(context, "Message sent to " + story.username, Toast.LENGTH_SHORT).show()
                                    } } ) {
                                    Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                                }
                            }

                        }
                    }
                    if (story.userId == appState.userData?.userId) {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = story.images[pagerState.currentPage].viewedBy.size.toString())
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(imageVector = Icons.Filled.RemoveRedEye, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
    }
    AnimatedVisibility(visible = dialog) {
        DeleteStoryDialog(hideDialog = { dialog = false }, deleteStory = { deleteStory(pagerState.currentPage)
            dialog = false
        hideDialog()})
    }
    LaunchedEffect(pagerState.currentPage) {
        if(story.images[pagerState.currentPage].viewedBy.all { it.userId != appState.userData?.userId } && story.userId != appState.userData?.userId){
            viewModel.viewStory(story.id, pagerState.currentPage)
        }
    }
}
