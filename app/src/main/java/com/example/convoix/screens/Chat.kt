package com.example.convoix.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.convoix.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.ClearChatDialog
import com.example.convoix.Message
import com.example.convoix.Dialogs.MsgDeleteDialog
import com.example.convoix.R
import com.example.convoix.View
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
    var imageUrl by remember {
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
        if(selectionMode || showDialog || clearChatDialog){
            showDialog = false
            clearChatDialog = false
            selectionMode = false
            selectedItem.clear()
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
                                                    onClick = { navController.navigate("otherprofile")
                                                        expanded=false }
                                                )
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "Block user",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    },
                                                    onClick = {  }
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                ) {
                                    Text(text = selectedItem.size.toString(), modifier = Modifier.padding(start = 20.dp))
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
        Image(modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.blurry_gradient_haikei),
            contentDescription = null,
            contentScale = ContentScale.Crop)
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
        AnimatedVisibility(imageUrl!="") {
            View(imageUrl = imageUrl, hideDialog = {imageUrl=""})
        }
        fun compressImage(): ByteArray {
            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
            return outputStream.toByteArray()
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
                item {
                    if(userData.userId==tp.user1?.userId){
                        AnimatedVisibility(tp.user1.typing) {
                            loading()
                        }
                    }
                    if(userData.userId==tp.user2?.userId){
                        AnimatedVisibility(tp.user2.typing) {
                            loading()
                        }
                    }
                }
                items(messages.size) { index ->
                    val message = messages[index]
                    val prevMessage = if (index > 0) messages[index - 1] else null
                    val nextMessage = if (index < messages.size - 1) messages[index + 1] else null
                    MessageItem(
                        message = message,
                        state, viewImage = {imageUrl = it},
                        reaction = {viewModel.Reaction(it, chatId, message.msgId)},
                        selectionMode = { selectionMode = true
                                        selectedItem.add(it)},
                        mode = selectionMode,
                        Selected = {if(selectedItem.contains(message.msgId))  {selectedItem.remove(it); if (selectedItem.size==0) selectionMode = false} else selectedItem.add(it) },
                        isSelected = selectedItem.contains(message.msgId),
                        prevId = prevMessage?.senderId.toString(),
                        nextId = nextMessage?.senderId.toString()
                        )
                }
            }
            imgUri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver,it)
                    bitmap = ImageDecoder.decodeBitmap(src)
                }
                Image(bitmap = bitmap?.asImageBitmap()!!, contentDescription = null, modifier = Modifier
                    .fillMaxWidth()
                    .size(300.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(10.dp))

            }
            if(isLoading){
                Upload(content = reply, state = state, bitmap)
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.onPrimary, CircleShape)) {
                TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = "Message")},
                    value = reply,
                    onValueChange = { reply=it },
                    //trailingIcon = {
                    //   IconButton(onClick = { launcher.launch("image/*") }) {
                      //     Icon(
                    //            imageVector = Icons.Filled.InsertPhoto,
                     //           contentDescription = null
                     //       )
                    //    }
                  //  },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ))
                AnimatedVisibility(reply.isNotEmpty() || imgUri!=null) {
                    val scope = rememberCoroutineScope()
                    IconButton(onClick = {
                        if (imgUri == null) {
                            viewModel.sendReply(msg = reply, chatId = chatId, imgUrl = "")
                            reply = ""
                        } else {
                            isLoading = true
                            viewModel.UploadImage(compressImage()) { imageUrl ->
                                viewModel.sendReply(chatId = chatId, msg = reply, imgUrl = imageUrl)
                                isLoading = false
                                reply = ""
                            }
                            imgUri = null
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null)
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
                viewImage:(String)->Unit,
                selectionMode:(String)->Unit,
                mode: Boolean,
                Selected:(String)->Unit,
                isSelected: Boolean,
                prevId: String,
                nextId: String
) {
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF2A4783),
        Color(0xFF2F6086)
    ))
    val brush3 = Brush.linearGradient(listOf(
        Color(0xFF9465FF),
        Color(0xFF6723D1)
    ))
    val brush4 = Brush.linearGradient(listOf(
        Color(0xFF54308D),
        Color(0xFF5E449B)
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
    val clkcolor = if(isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent
    Box(
        modifier = Modifier
            .background(clkcolor)
            //.combinedClickable(
            //    onLongClick = {
            //  if (!mode) selectionMode(message.msgId) else {
            //      null
            //  }
            //  }, onClick = { if (mode) Selected(message.msgId)  else onClick=!onClick})
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = if (message.reaction.toString() != "") 0.dp else 3.dp,
                start = 10.dp,
                end = 10.dp
            ),
        contentAlignment = alignment
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .widthIn(max = 270.dp)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(16.dp)), horizontalAlignment = Alignment.End
            ) {
                if(message.imgUrl!=""){
                    AsyncImage(
                        model = message.imgUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clickable { viewImage(message.imgUrl.toString()) }
                            .aspectRatio(16f / 9f)
                            .padding(6.dp)
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
                    modifier = Modifier.padding(end = 8.dp, bottom = if(message.reaction.toString()!="") 10.dp else 5.dp, start = 8.dp, top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
            AnimatedVisibility(message.reaction.toString()!="") {
                Text(text = message.reaction.toString(), modifier = Modifier
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
                    )
            }
        }
        AnimatedVisibility(onClick && !isCurrentUser) {
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = CircleShape)) {
                DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                    expanded = onClick,
                    onDismissRequest = { onClick = false },
                    modifier = Modifier
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 10.dp)) {
                        listOf("😂", "👍", "❤️", "😭", "😮").forEach {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.clickable { reaction(it)
                                    onClick = false}
                            )
                        }
                    }
                    //DropdownMenuItem(
                   //     text = { Text(text = "Copy", style = MaterialTheme.typography.bodyLarge) },
                    //    onClick = { val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    //        val clipData: ClipData = ClipData.newPlainText("text", message.content)
                     //       clipboardManager.setPrimaryClip(clipData)
                      //      Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                      //      onClick = false
                      //  }
                    //)
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
fun loading(){
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