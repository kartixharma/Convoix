package com.example.convoix.screens

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.ClearChatDialog
import com.example.convoix.Dialogs.ImagePreview
import com.example.convoix.Dialogs.ImageViewer
import com.example.convoix.Dialogs.MsgDeleteDialog
import com.example.convoix.Dialogs.VideoPlayer
import com.example.convoix.Dialogs.VideoPreview
import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.Firebase.Message
import com.example.convoix.Firebase.ScheduledMsg
import com.example.convoix.MessageItem
import com.example.convoix.R
import com.example.convoix.schedule.TimePickerDialog
import com.google.firebase.Timestamp
import com.makeappssimple.abhimanyu.composeemojipicker.ComposeEmojiPickerBottomSheetUI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Chat(navController: NavController,
         viewModel: ChatViewModel,
         userData: ChatUserData,
         chatId: String,
         messages: List<Message>,
         state: AppState,
         onBack:() -> Unit,
         schedule:(LocalDateTime, String, String, String) -> Unit,
         context: Context = LocalContext.current
) {
    val file = File(context.filesDir, "temp.png")
    val uri = FileProvider.getUriForFile(
        context,
        "com.example.convoix.fileProvider",
        file
    )
    val tState = rememberTimePickerState(is24Hour = false)
    val dState = rememberDatePickerState(yearRange = (2024 .. 2025))
    val cal = Calendar.getInstance()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.imgUri = it
    }
    val vLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.vidUri = it
    }
    val fLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        viewModel.fileUri = it
    }
    val cLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
        viewModel.imgUri=null
        if(it)
            viewModel.imgUri = uri
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        if(!it) {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        else{
            cLauncher.launch(uri)
        }
    }
    val tp = viewModel.tp
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    var indx by remember { mutableStateOf(-1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true,)
    val msgs = if (viewModel.searchText1.isNotEmpty()) {
            messages.filter { it.content.toString().contains(viewModel.searchText1) }
        } else {
            emptyList()
        }
    var srchIndx by remember { mutableStateOf(-1) }
    val fileName = viewModel.fileUri?.let { DocumentFile.fromSingleUri(context, it)?.name } ?: ""
    val size = viewModel.fileUri?.let { String.format("%.2f", DocumentFile.fromSingleUri(context, it)?.length()!!.div(1024f*1024f)) } ?: ""
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = viewModel.reply){
        if(viewModel.reply.length>0) {
            viewModel.typing(true, chatId = chatId, userId = state.userData?.userId.toString())
        }
        if(viewModel.reply.length==0){
            viewModel.typing(false, chatId = chatId, userId = state.userData?.userId.toString())
        }
    }
    LaunchedEffect(key1 = messages) {
        if(chatId!=""){
            viewModel.readAllMessagesInChat(chatId)
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.popMessage(state.chatId)
    }
    BackHandler {
        if(viewModel.searchText1.isNotBlank()
            || srchIndx!=-1
            || viewModel.showSearch
            || viewModel.selectionMode
            || viewModel.showDialog
            || viewModel.clearChatDialog
            || viewModel.editMsgId.isNotBlank()
            || viewModel.isModalBottomSheetVisible
            || viewModel.replyMessage!=Message()
            || viewModel.forwardMsgs.size>0
            || viewModel.reply.isNotBlank()){
            srchIndx=-1
            viewModel.showDialog = false
            viewModel.clearChatDialog = false
            viewModel.selectionMode = false
            selectedItem.clear()
            viewModel.editMsgId=""
            viewModel.isModalBottomSheetVisible = false
            viewModel.replyMessage = Message()
            viewModel.forwardMsgs.clear()
            viewModel.reply=""
            viewModel.showSearch = false
            viewModel.searchText1 = ""
        }
        else{
            navController.popBackStack()
            viewModel.dePopMsg()
            viewModel.depopTp()
            expanded=false
            viewModel.dltchatUser()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.onPrimary), //FF4A275F
                        title = {
                            if(!viewModel.selectionMode && !viewModel.showSearch){
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
                                                            text = "Search",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    },
                                                    onClick = { viewModel.showSearch=true
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
                                                    onClick = { viewModel.clearChatDialog=true; expanded=false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                        }
                            AnimatedVisibility(
                                viewModel.selectionMode,
                                enter = slideInVertically(),
                                exit = slideOutVertically()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                ) {
                                    Text(text = selectedItem.size.toString(), modifier = Modifier.padding(start = 20.dp))
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        navController.navigate("forward")
                                        viewModel.selectionMode=false
                                    }) {
                                        Icon(modifier = Modifier.size(25.dp), painter = painterResource(id = R.drawable.pngwing_com), contentDescription = null)
                                    }
                                    IconButton(onClick = {
                                        viewModel.showDialog = true
                                    }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                    }
                                }
                            }
                            AnimatedVisibility(
                                viewModel.showSearch,
                                enter = slideInVertically(),
                                exit = slideOutVertically().plus(fadeOut())
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    var indexx by remember {
                                        mutableStateOf(-1)
                                    }
                                    OutlinedTextField(
                                        textStyle = TextStyle.Default.copy(fontSize = 15.sp),
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f),
                                        value = viewModel.searchText1,
                                        onValueChange = { viewModel.searchText1 = it; indexx=-1 },
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
                                        leadingIcon = {
                                            IconButton(onClick = { viewModel.showSearch=false
                                                viewModel.searchText1=""
                                                srchIndx=-1
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowBackIosNew,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (viewModel.searchText1.isNotBlank() && msgs.isNotEmpty()) {
                                            Row {
                                                IconButton(onClick = {
                                                    if(indexx!=msgs.size-1) {
                                                        indexx++
                                                        coroutineScope.launch {
                                                            srchIndx = messages.indexOf(messages.find { it == msgs[indexx] })
                                                            listState.animateScrollToItem(srchIndx)
                                                        }
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.KeyboardArrowUp,
                                                        contentDescription = null
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    if(indexx!=0 && indexx!=-1) {
                                                        indexx--
                                                        coroutineScope.launch {
                                                            srchIndx = messages.indexOf(messages.find { it == msgs[indexx] })
                                                            listState.animateScrollToItem(srchIndx)
                                                        }
                                                    }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        }
                                        }
                                    )
                                    AnimatedVisibility(viewModel.searchText1.isNotEmpty(), enter = slideInHorizontally(), exit = slideOutHorizontally()) {
                                        if(msgs.isEmpty()){
                                            Text(text = "No Results",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                        else{
                                            Text(text = (indexx+1).toString() + " / " + (msgs.size).toString(),
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                },
                navigationIcon = {
                    if(!viewModel.showSearch){
                        IconButton(onClick = {if(viewModel.selectionMode) {viewModel.selectionMode = false ; selectedItem.clear() ; viewModel.forwardMsgs.clear()
                        } else onBack() }) {
                            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                        }
                    }
                },
            )
        }
    ) {
        if (viewModel.isModalBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = sheetState,
                shape = RectangleShape,
                tonalElevation = 0.dp,
                onDismissRequest = {
                    viewModel.isModalBottomSheetVisible = false
                    viewModel.searchText = ""
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
                            viewModel.isModalBottomSheetVisible = false
                            viewModel.Reaction(emoji.character, chatId, viewModel.selectedEmoji)
                        },
                        onEmojiLongClick = { emoji ->
                            Toast.makeText(
                                context,
                                emoji.unicodeName,
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        searchText = viewModel.searchText,
                        updateSearchText = { updatedSearchText ->
                            viewModel.searchText = updatedSearchText
                        },
                    )
                }
            }
        }
        if(state.userData?.pref?.customImg!!.isBlank()){
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(
                        alpha = state.userData.pref.back
                            .toString()
                            .toFloat()
                    ),
                painter = painterResource(R.drawable.blurry_gradient_haikei),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(
                        alpha = state.userData.pref.doodles
                            .toString()
                            .toFloat()
                    ),
                painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
        else{
            AsyncImage(model = state.userData.pref.customImg, contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(
                        alpha = state.userData.pref.back
                            .toString()
                            .toFloat()
                    ),
                contentScale = ContentScale.Crop
            )
        }
        AnimatedVisibility(viewModel.showDialog) {
            MsgDeleteDialog(selectedItem.size, hideDialog = { viewModel.showDialog = false }, deleteMsg = {viewModel.deleteMsg(selectedItem, chatId)
                selectedItem.clear()
                viewModel.forwardMsgs.clear()
                viewModel.showDialog=false
                viewModel.selectionMode = false
            })
        }
        AnimatedVisibility(visible = viewModel.datePicker) {
            DatePickerDialog(onDismissRequest = { viewModel.datePicker = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.datePicker = false; viewModel.timePicker = true}) {
                    Text(text = "Confirm")
                }},
                dismissButton = {
                    TextButton(onClick = { viewModel.datePicker=false }) {
                        Text(text = "Cancel")
                    }
                }) {
                DatePicker(state = dState)
            }
        }
        AnimatedVisibility(visible = viewModel.timePicker) {
            TimePickerDialog(onCancel = { viewModel.timePicker =false },
                onConfirm = {
                    val dateinmillis = dState.selectedDateMillis
                    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateinmillis!!), ZoneId.systemDefault())
                    cal.set(Calendar.YEAR, date.year)
                    cal.set(Calendar.MONTH, date.month.value-1)
                    cal.set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                    cal.set(Calendar.HOUR_OF_DAY, tState.hour)
                    cal.set(Calendar.MINUTE, tState.minute)
                    cal.set(Calendar.SECOND, 0)
                    cal.isLenient=false
                    schedule(cal.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), viewModel.reply, chatId, state.userData.userId)
                    viewModel.timePicker = false
                    Toast.makeText(context, "Message successfully scheduled", Toast.LENGTH_SHORT).show()
                    viewModel.addScheduledMsg(ScheduledMsg(
                        username = userData.username.toString(),
                        ppurl = userData.ppurl,
                        chatId = chatId,
                        content = viewModel.reply,
                        senderId = state.userData.userId,
                        time = Timestamp(cal.time)
                    ))
                    viewModel.reply=""
                }) {
                TimePicker(state = tState)
            }
        }
        AnimatedVisibility(viewModel.clearChatDialog) {
            ClearChatDialog(hideDialog = { viewModel.clearChatDialog = false}, clearChat = { viewModel.clearChat(chatId); viewModel.clearChatDialog=false })
        }
        AnimatedVisibility(viewModel.msg.imgUrl!="") {
            ImageViewer(userData = userData, hideDialog = { viewModel.msg = Message() }, message = viewModel.msg)
        }
        AnimatedVisibility(viewModel.msg.vidUrl!="") {
            VideoPlayer(userData = userData, hideDialog = { viewModel.msg= Message() }, message = viewModel.msg)
        }
        fun compressImage(quality: Int): ByteArray {
            val outputStream = ByteArrayOutputStream()
            viewModel.bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            return outputStream.toByteArray()
        }
        viewModel.imgUri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver, it)
                viewModel.bitmap = ImageDecoder.decodeBitmap(src)
            }
           ImagePreview(uri = viewModel.imgUri, hideDialog = { viewModel.imgUri=null }, send = { cUri, reply, cm ->
               if(cUri!=null){
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                       val src = ImageDecoder.createSource(context.contentResolver, cUri)
                       viewModel.bitmap = ImageDecoder.decodeBitmap(src)
                   }
               }
               val reply1 = reply
               val id = viewModel.sendReply(chatId = chatId, msg = reply1, imgUrl = "uploadingImage", imgUri = if(cUri!=null) cUri.toString() else viewModel.imgUri.toString())
               viewModel.UploadImage(img = if(cm) compressImage(70) else compressImage(100) , chatId = chatId, msgId = id) { imageUrl ->
                   viewModel.addUrl(chatId = chatId, imgUrl = imageUrl, msgId = id)
               }
               viewModel.imgUri = null
           })
        }
        viewModel.vidUri?.let {
            VideoPreview(uri = it, hideDialog = { viewModel.vidUri=null }, send = { reply ->
                val id = viewModel.sendReply(chatId = chatId, msg = reply, vidUrl = "uploadingVideo", imgUri = viewModel.imgUri.toString())
                viewModel.uploadVideo(vidUri = viewModel.vidUri!!, chatId = chatId, msgId = id) { vidUrl ->
                    viewModel.addUrl(chatId = chatId, vidUrl = vidUrl, msgId = id)
                }
                viewModel.vidUri = null
            })
        }
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
                LazyColumn(state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    reverseLayout = true
                ) {
                    items(messages.size) { index ->
                        val message = messages[index]
                        val prevMessage = if (index > 0) messages[index - 1] else null
                        val nextMessage = if (index < messages.size - 1) messages[index + 1] else null
                        MessageItem(
                            viewModel.searchText1,
                            searchIndx = srchIndx,
                            message = message,
                            state, viewImage = { viewModel.msg = it },
                            reaction = { viewModel.Reaction(it, chatId, message.msgId) },
                            selectionMode = {
                                viewModel.selectionMode = true
                                selectedItem.add(it)
                                viewModel.forwardMsgs.add(message) },
                            mode = viewModel.selectionMode,
                            Selected = { if(selectedItem.contains(message.msgId)) {
                                selectedItem.remove(it)
                                viewModel.forwardMsgs.remove(message)
                                if (selectedItem.size==0) viewModel.selectionMode = false
                            } else { selectedItem.add(it)
                                viewModel.forwardMsgs.add(message)
                            }},
                            isSelected = selectedItem.contains(message.msgId),
                            prevId = prevMessage?.senderId.toString(),
                            nextId = nextMessage?.senderId.toString(),
                            editMessage = { id, content ->
                                viewModel.editMsgId=id
                                viewModel.editMsgContent=content
                                viewModel.reply=content
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                            reactionPicker = {
                                viewModel.selectedEmoji=message.msgId
                                viewModel.isModalBottomSheetVisible=true },
                            removeReaction = { viewModel.removeReaction(chatId, message.msgId) },
                            replyMessage = { msg ->
                                viewModel.replyMessage = msg
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                            onReplyClick = { id ->
                                coroutineScope.launch {
                                    indx = messages.indexOfFirst { it.msgId == id }
                                    if (indx != -1) {
                                        listState.animateScrollToItem(indx)
                                    }
                                    indx=-1
                                }},
                            cindex = indx,
                            index = index,
                            playVideo = { viewModel.msg=it }
                        )
                    }
                }
            if(userData.userId==tp.user1?.userId) {
                AnimatedVisibility(tp.user1.typing) {
                    Loading(state.userData.pref.anim)
                }
            }
            if(userData.userId==tp.user2?.userId) {
                AnimatedVisibility(tp.user2.typing) {
                    Loading(state.userData.pref.anim)
                }
            }
            AnimatedVisibility(viewModel.editMsgId.isNotEmpty()) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(16.dp))) {
                    Row( modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp)) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Edit message", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(modifier = Modifier.clickable {
                            viewModel.editMsgId=""
                            viewModel.reply=""
                        },
                            imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                    Text(text = viewModel.editMsgContent, color = Color.LightGray,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                RoundedCornerShape(12.dp)
                            )
                            .fillMaxWidth()
                            .padding(8.dp))
                }
            }
            AnimatedVisibility(visible = viewModel.fileUri!=null) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(16.dp))) {
                    Row( modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp)) {
                        Text(text = "Document", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(modifier = Modifier.clickable {
                            viewModel.fileUri=null
                        }, imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                    Row(modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(12.dp)
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(modifier = Modifier.padding(10.dp),
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null)
                        Text(text = fileName, color = Color.LightGray,
                            modifier = Modifier
                                .width(200.dp)
                                .padding(8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = size+" MB", color = Color.LightGray,
                            modifier = Modifier
                                .padding(5.dp)
                                .background(Color.Gray.copy(alpha = 0.6f), RoundedCornerShape(7.dp))
                                .padding(8.dp))
                    }
                }
            }
            AnimatedVisibility(viewModel.replyMessage.content!="" || viewModel.replyMessage.vidUrl!="" || viewModel.replyMessage.imgUrl!="" || viewModel.replyMessage.fileUrl!="") {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(16.dp))) {
                    Row( modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp)) {
                        Text(text = "Reply", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(modifier = Modifier.clickable {
                            viewModel.replyMessage = Message()
                            viewModel.reply=""
                        },
                            imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                    Row(modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onPrimary,
                            RoundedCornerShape(12.dp)
                        ),
                        verticalAlignment = Alignment.CenterVertically
                        ) {
                        if(viewModel.replyMessage.imgUrl.isNotEmpty()){
                            AsyncImage(
                                model = viewModel.replyMessage.imgUrl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .aspectRatio(1f / 1f)
                                    .padding(5.dp)
                                    .clip(
                                        RoundedCornerShape(7.dp)
                                    )
                            )
                        }
                        if(viewModel.replyMessage.vidUrl.isNotEmpty()){
                            Image(painter = painterResource(id = R.drawable.play),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(40.dp),
                                contentDescription = null
                            )
                        }
                        if(viewModel.replyMessage.fileUrl.isNotEmpty()){
                            Icon(imageVector = Icons.Rounded.InsertDriveFile,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(40.dp),
                                contentDescription = null
                            )
                        }
                        Column {
                            if(viewModel.replyMessage.content!!.isNotEmpty()){
                                Text(text = viewModel.replyMessage.content!!.toString(), color = Color.LightGray,
                                    modifier = Modifier
                                        .padding(8.dp))
                            }
                            else if(viewModel.replyMessage.fileUrl.isNotEmpty()){
                                Text(text = viewModel.replyMessage.fileName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.LightGray,
                                    modifier = Modifier
                                        .padding(8.dp))
                            }
                        }
                    }
                }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    Icon(tint = Color.White,
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(viewModel.brush, CircleShape)
                            .padding(13.dp)
                            .clickable {
                                val permissionCheckResult =
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    )
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    cLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .background(
                                MaterialTheme.colorScheme.onPrimary,
                                RoundedCornerShape(32.dp)
                            )) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(text = "Message") },
                            value = viewModel.reply,
                            onValueChange = { viewModel.reply = it },
                            trailingIcon = {
                                if (viewModel.editMsgId.isEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.showOptions = !viewModel.showOptions
                                    }) {
                                        Icon(
                                            modifier = Modifier.size(25.dp),
                                            painter = painterResource(id = R.drawable.attach_file),
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
                        AnimatedVisibility(viewModel.reply.isNotEmpty() || viewModel.fileUri!=null) {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .background(viewModel.brush, CircleShape)
                                    .padding(12.dp)
                                    .combinedClickable(
                                        onLongClick = {
                                            viewModel.datePicker = true
                                        },
                                        onClick = {
                                            if (viewModel.fileUri != null) {
                                                val id = viewModel.sendReply(
                                                    chatId = chatId,
                                                    msg = viewModel.reply,
                                                    fileUrl = "uploadingFile",
                                                    fileName = fileName,
                                                    fileSize = "$size MB"
                                                )
                                                viewModel.uploadFile(
                                                    fileUri = viewModel.fileUri!!,
                                                    chatId = chatId,
                                                    msgId = id
                                                ) { fileUrl ->
                                                    viewModel.addUrl(
                                                        chatId = chatId,
                                                        fileUrl = fileUrl,
                                                        msgId = id
                                                    )
                                                }
                                                viewModel.fileUri = null
                                            } else if (viewModel.editMsgId.isEmpty()) {
                                                viewModel.sendReply(
                                                    msg = viewModel.reply,
                                                    chatId = chatId,
                                                    imgUrl = "",
                                                    replyMsg = viewModel.replyMessage
                                                )
                                                viewModel.reply = ""
                                                viewModel.replyMessage = Message()

                                            } else {
                                                viewModel.editMessage(
                                                    viewModel.editMsgId,
                                                    chatId,
                                                    viewModel.reply
                                                )
                                                viewModel.reply = ""
                                                viewModel.editMsgId = ""
                                                keyboardController?.hide()
                                            }

                                        }

                                    ))
                        }
                        Column {
                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = CircleShape)) {
                                DropdownMenu(
                                    expanded = viewModel.showOptions,
                                    onDismissRequest = { viewModel.showOptions = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
                                            .shadow(1.dp, CircleShape)
                                            .background(viewModel.brush, CircleShape)
                                            .clickable {
                                                launcher.launch("image/*")
                                                viewModel.showOptions = false
                                            }
                                            .padding(12.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(vertical = 5.dp, horizontal = 10.dp)
                                            .shadow(1.dp, CircleShape)
                                            .background(viewModel.brush, CircleShape)
                                            .clickable {
                                                vLauncher.launch("video/*")
                                                viewModel.showOptions = false
                                            }
                                            .padding(12.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
                                            .shadow(1.dp, CircleShape)
                                            .background(viewModel.brush, CircleShape)
                                            .clickable {
                                                fLauncher.launch("*/*")
                                                viewModel.showOptions = false
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Loading(anim: Int){
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(
        when(anim){
            1 -> R.raw.animation1
            2 -> R.raw.animation2
            3 -> R.raw.animation3
            else -> R.raw.animation4
        }
    ))
    LottieAnimation(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .size(80.dp)
            .alpha(0.8f),
        composition = comp,
        iterations = LottieConstants.IterateForever
    )
}