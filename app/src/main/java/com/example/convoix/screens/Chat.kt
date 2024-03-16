package com.example.convoix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.ClearChatDialog
import com.example.convoix.Dialogs.ImagePreview
import com.example.convoix.Dialogs.ImageViewer
import com.example.convoix.Firebase.Message
import com.example.convoix.Dialogs.MsgDeleteDialog
import com.example.convoix.R
import com.makeappssimple.abhimanyu.composeemojipicker.ComposeEmojiPickerBottomSheetUI
import com.makeappssimple.abhimanyu.composeemojipicker.utils.capitalizeWords
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController,
         viewModel: ChatViewModel,
         messages: List<Message>,
         userData: ChatUserData,
         chatId:String, state: AppState,
         onBack:()->Unit
) {
    var editMsgId by remember {
        mutableStateOf("")
    }
    var editMsgContent by remember {
        mutableStateOf("")
    }
    var isLoading by remember {
        mutableStateOf(false)
    }
    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current
    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }
    var bitmap by remember {mutableStateOf<Bitmap?>(null)}
    val tp = viewModel.tp
    var selectionMode by remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    var showDialog by remember {
        mutableStateOf(false)
    }
    var clearChatDialog by remember {
        mutableStateOf(false)
    }
    var msg by remember {
        mutableStateOf(Message())
    }
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    var isModalBottomSheetVisible by remember {
        mutableStateOf(false)
    }
    var selectedEmoji by remember {
        mutableStateOf("")
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = reply){
        if(reply.length>0) {
            viewModel.typing(true, chatId = chatId, userId = state.userData?.userId.toString())
        }
        if(reply.length==0){
            viewModel.typing(false, chatId = chatId, userId = state.userData?.userId.toString())
        }
    }
    LaunchedEffect(key1 = Unit){
        viewModel.popMessage(state.chatId)
    }
    BackHandler {
        if(selectionMode || showDialog || clearChatDialog || editMsgId.isNotBlank() || isModalBottomSheetVisible){
            showDialog = false
            clearChatDialog = false
            selectionMode = false
            selectedItem.clear()
            editMsgId=""
            isModalBottomSheetVisible = false
        }
        else{
            navController.popBackStack()
            viewModel.dePopMsg()
            viewModel.depopTp()
            expanded=false
            reply=""
        }
    }
    Scaffold(modifier = Modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.onPrimary), //FF4A275F
                        title = {
                            if(!selectionMode){
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
                                    Column(verticalArrangement = Arrangement.Center){
                                        Text(
                                            modifier = Modifier.padding(start = 16.dp),
                                            text = userData.username.toString()
                                        )
                                        if(userData.userId==tp.user1?.userId){
                                            AnimatedVisibility(tp.user1.typing) {
                                                Text(modifier = Modifier.padding(start = 16.dp),
                                                    text = "Typing...",
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                        }
                                        if(userData.userId==tp.user2?.userId){
                                            AnimatedVisibility(tp.user2.typing) {
                                                Text(modifier = Modifier.padding(start = 16.dp),
                                                    text = "Typing...",
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))
                                    Column {
                                        IconButton(onClick = { expanded = true }) {
                                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null)
                                        }
                                        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(0xFF294F86))) {
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(MaterialTheme.colorScheme.background)
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Profile",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    },
                                                    onClick = { navController.navigate("otherprofile")
                                                        expanded=false }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = if(viewModel.isBLockedByMe(userData.userId)) "Unblock user" else "Block user",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    },
                                                    onClick = { if(viewModel.isBLockedByMe(userData.userId)) viewModel.unblockUser(userData.userId) else viewModel.blockUser(userData.userId)
                                                        expanded=false}
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Clear chat",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    },
                                                    onClick = { clearChatDialog=true; expanded=false
                                                    }
                                                )

                                            }
                                        }
                                    }
                                }
                        }
                            AnimatedVisibility(
                                selectionMode,
                                enter = slideInVertically(),
                                exit = slideOutVertically()
                            ){
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                ) {
                                    Text(text = selectedItem.size.toString(), modifier = Modifier.padding(start = 20.dp))
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        showDialog = true
                                    }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                    }
                                }
                            }
                },
                navigationIcon = {
                    IconButton(onClick = {if(selectionMode) {selectionMode = false ; selectedItem.clear()} else onBack() }) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
            )
        }
    ) {it->
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
                            viewModel.Reaction(emoji.character, chatId, selectedEmoji)
                        },
                        onEmojiLongClick = { emoji ->
                            Toast.makeText(
                                context,
                                emoji.unicodeName.capitalizeWords(),
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
        val focusRequester = FocusRequester()
        val keyboardController = LocalSoftwareKeyboardController.current
        Image(modifier = Modifier
            .fillMaxSize()
            .alpha(
                alpha = state.userData?.pref?.back
                    .toString()
                    .toFloat()
            ),
            painter = painterResource(R.drawable.blurry_gradient_haikei),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            )
        Image(modifier = Modifier
            .fillMaxSize()
            .alpha(
                alpha = state.userData?.pref?.doodles
                    .toString()
                    .toFloat()
            ),
            painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        AnimatedVisibility(showDialog) {
            MsgDeleteDialog(selectedItem.size, hideDialog = { showDialog = false }, deleteMsg = {viewModel.deleteMsg(selectedItem, chatId)
                selectedItem.clear()
                showDialog=false
                selectionMode = false
            })
        }
        AnimatedVisibility(clearChatDialog) {
            ClearChatDialog(hideDialog = { clearChatDialog = false}, clearChat = { viewModel.clearChat(chatId); clearChatDialog=false })
        }
        AnimatedVisibility(msg.imgUrl!="") {
           ImageViewer(userData = userData, hideDialog = { msg= Message() }, message = msg)
        }
        fun compressImage(): ByteArray {
            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
            return outputStream.toByteArray()
        }
        imgUri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver,it)
                bitmap = ImageDecoder.decodeBitmap(src)
            }
           ImagePreview(bitmap = bitmap, hideDialog = { imgUri=null }, send = {
               isLoading = true
               val reply1 = it
               viewModel.UploadImage(compressImage()) { imageUrl ->
                   viewModel.sendReply(chatId = chatId, msg = reply1, imgUrl = imageUrl)
                   isLoading = false
               }
               imgUri = null
           })
        }
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messages.size) { index ->
                    val message = messages[index]
                    val prevMessage = if (index > 0) messages[index - 1] else null
                    val nextMessage = if (index < messages.size - 1) messages[index + 1] else null
                    MessageItem(
                        message = message,
                        state, viewImage = { msg = it },
                        reaction = { viewModel.Reaction(it, chatId, message.msgId) },
                        selectionMode = { selectionMode = true
                                        selectedItem.add(it) },
                        mode = selectionMode,
                        Selected = {if(selectedItem.contains(message.msgId))  {selectedItem.remove(it); if (selectedItem.size==0) selectionMode = false} else selectedItem.add(it) },
                        isSelected = selectedItem.contains(message.msgId),
                        prevId = prevMessage?.senderId.toString(),
                        nextId = nextMessage?.senderId.toString(),
                        editMessage = { id, content ->
                            editMsgId=id
                            editMsgContent=content
                            reply=content
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        reactionPicker = {
                            selectedEmoji=message.msgId
                            isModalBottomSheetVisible=true },
                        removeReaction = { viewModel.removeReaction(chatId, message.msgId) }
                        )
                }
            }
            if(userData.userId==tp.user1?.userId){
                AnimatedVisibility(tp.user1.typing) {
                    Loading()
                }
            }
            if(userData.userId==tp.user2?.userId){
                AnimatedVisibility(tp.user2.typing) {
                    Loading()
                }
            }
            AnimatedVisibility(editMsgId.length>0) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),) {
                    Row( modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp)) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Edit message",)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(modifier = Modifier.clickable {
                            editMsgId=""
                            reply=""
                        },
                            imageVector = Icons.Filled.Close, contentDescription = null)

                    }

                    Text(text = editMsgContent, color = Color.LightGray,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp))
                }
            }
            if(isLoading){
                Upload(content = reply, state = state, bitmap)
            }
            var isBlocked by remember { mutableStateOf(false) }

            viewModel.isBlocked(userData.userId) {
                isBlocked = it
            }

            if (isBlocked) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)) {
                    Text(modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                        text = "You are Blocked by the user!!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                        )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, CircleShape)) {
                    TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = "Message")},
                        value = reply,
                        onValueChange = { reply=it },
                        trailingIcon = {
                            if(editMsgId.length==0){
                                IconButton(onClick = { launcher.launch("image/*") }) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null
                                    )
                                }
                            }

                          },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        )
                    AnimatedVisibility(reply.isNotEmpty()) {
                        IconButton(modifier = Modifier
                            .padding(end = 4.dp)
                            .background(brush, CircleShape) ,
                            onClick = {
                            if(editMsgId.length==0){
                                viewModel.sendReply(msg = reply, chatId = chatId, imgUrl = "")
                                reply = ""

                            }
                            else{
                                viewModel.editMessage(editMsgId, chatId, reply)
                                reply=""
                                editMsgId=""
                                keyboardController?.hide()
                            }

                        }) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(message: Message,
                state: AppState,
                reaction:(String)->Unit,
                viewImage:(Message)->Unit,
                selectionMode:(String)->Unit,
                mode: Boolean,
                Selected:(String)->Unit,
                isSelected: Boolean,
                prevId: String,
                nextId: String,
                editMessage: (String, String) -> Unit,
                reactionPicker: () -> Unit,
                removeReaction: () -> Unit
) {
    val context = LocalContext.current
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF2A4783),
        Color(0xFF2F6086)
    ))
    val isCurrentUser = state.userData?.userId == message.senderId
    val shape = if(isCurrentUser){
        if(prevId==message.senderId && nextId==message.senderId){
            RoundedCornerShape(16.dp, 3.dp, 3.dp, 16.dp)
        }
        else if(prevId==message.senderId){
            RoundedCornerShape(16.dp, 16.dp, 3.dp, 16.dp)
        }
        else if(nextId==message.senderId){
            RoundedCornerShape(16.dp, 3.dp, 16.dp, 16.dp)
        }
        else{
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
    }
    else{
        if(prevId==message.senderId && nextId==message.senderId){
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 3.dp)
        }
        else if(prevId==message.senderId){
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp)
        }
        else if(nextId==message.senderId){
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 16.dp)
        }
        else{
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
    }
    val color = if (isCurrentUser) brush else  brush2
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    var onClick by remember {
        mutableStateOf(false)
    }
    var showReactions by remember {
        mutableStateOf(false)
    }
    val clkcolor = if(isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else Color.Transparent
    Box(
        modifier = Modifier
            .background(clkcolor)
            .combinedClickable(
                onLongClick = {
                    if (!mode) selectionMode(message.msgId) else {
                        null
                    }
                }, onClick = { if (mode) Selected(message.msgId) else onClick = !onClick })
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = 0.dp,
                start = 10.dp,
                end = 10.dp
            ),
        contentAlignment = alignment
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .shadow(2.dp, shape)
                    .widthIn(max = 270.dp)
                    .fillMaxHeight()
                    .background(color, shape), horizontalAlignment = Alignment.End
            ) {
                if(message.imgUrl!=""){
                    AsyncImage(
                        model = message.imgUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clickable { viewImage(message) }
                            .aspectRatio(16f / 9f)
                            .padding(6.dp, 6.dp, 6.dp)
                            .clip(
                                RoundedCornerShape(10.dp)
                            )
                            .size(60.dp)
                    )
                }
                if(message.content!=""){
                    Text(
                        text = message.content.toString(),
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                        color = Color.White
                    )
                }
                Text(
                    text = formatter.format(message.time?.toDate()!!),
                    modifier = Modifier.padding(end = 8.dp, bottom = if(message.reaction.isNotEmpty()) 10.dp else 5.dp, start = 8.dp, top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
            if(message.reaction.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .clickable {
                            showReactions = true
                        }
                        .graphicsLayer(
                            translationY = (-20).dp.value,
                            clip = false
                        )
                        .background(color, CircleShape)
                        .border(
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.background),
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    items(message.reaction){
                        Text(text = it.reaction,  modifier = Modifier.padding(horizontal = 2.dp))
                    }
                }
            }
        }
        AnimatedVisibility(showReactions) {
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                0xFF274F6F))) {
                DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                    expanded = showReactions,
                    onDismissRequest = { showReactions = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .width(200.dp)) {
                        message.reaction.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = it.ppurl,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(CircleShape)
                                        .size(30.dp)

                                )
                                Text(
                                    text = it.username,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(modifier = Modifier.clickable {
                                  if ( it.userId==state.userData?.userId ) removeReaction(); showReactions=false
                                },
                                    text = it.reaction,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(onClick) {
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
                                modifier = Modifier.clickable { reaction(it)
                                    onClick = false
                                }
                            )
                        }
                        Icon(
                            modifier = Modifier
                                .size(35.dp)
                                .clickable {
                                    reactionPicker()
                                    onClick = false
                                },
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = null)
                    }
                    DropdownMenuItem(
                        text = { Text(text = "Copy", style = MaterialTheme.typography.bodyLarge) },
                        onClick = { val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData: ClipData = ClipData.newPlainText("text", message.content)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                            onClick = false
                        }
                    )
                    if(isCurrentUser){
                        DropdownMenuItem(
                            text = { Text(text = "Edit message", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                editMessage(message.msgId, message.content.toString())
                                onClick = false
                            }
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun Upload(
    content: String, state: AppState, bitmap: Bitmap?
) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    Box(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = 3.dp,
                start = 10.dp,
                end = 10.dp
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .widthIn(max = 270.dp)
                    .background(brush, RoundedCornerShape(16.dp)), horizontalAlignment = Alignment.End
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bitmap?.asImageBitmap()!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(16f / 9f)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .blur(5.dp)
                            .size(60.dp)
                    )
                    Column(modifier = Modifier
                        .aspectRatio(16f / 9f)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .blur(5.dp)
                        .background(Color.DarkGray.copy(alpha = 0.3f))
                        .size(60.dp)) {

                    }
                    LottieAnimation(composition = comp, iterations = LottieConstants.IterateForever)
                }
                if(content!=""){
                    Text(
                        text =content,
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                        color = Color.White
                    )
                }
                Text(
                    text = formatter.format(Calendar.getInstance().time),
                    modifier = Modifier.padding(end = 8.dp, bottom = 5.dp, start = 8.dp, top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun Loading(){
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Column(Modifier.padding(start = 30.dp),horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = rememberAsyncImagePainter(R.drawable.loading, imageLoader),
            contentDescription = null, modifier = Modifier
                .scale(1.3f)
                .size(70.dp)
        )
    }
}