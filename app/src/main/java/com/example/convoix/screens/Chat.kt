package com.example.convoix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.InsertPhoto
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.example.convoix.AppState
import com.example.convoix.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.DeleteDialog
import com.example.convoix.Message
import com.example.convoix.MsgDeleteDialog
import com.example.convoix.R
import com.example.convoix.UserData
import com.example.convoix.View
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(navController: NavController,
         viewModel: ChatViewModel,
         messages: List<Message>,
         userData: ChatUserData,
         chatId:String, state: AppState,
         onBack:()->Unit
) {
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
        if(selectionMode){
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.onPrimary),
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
                                                    color = Color(0xFF1952C4),
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
                                                            text = " ",
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
        Image(modifier = Modifier
            .fillMaxSize()
            .scale(1.3f)
            .alpha(if (!isSystemInDarkTheme()) 0.8f else 1f),
            painter = painterResource(R.drawable.dark),
            contentDescription = null)
        AnimatedVisibility(showDialog) {
            MsgDeleteDialog(selectedItem.size, hideDialog = { showDialog = false }, deleteMsg = {viewModel.deleteMsg(selectedItem, chatId)
                selectedItem.clear()
                showDialog=false
                selectionMode = false
            })
        }
        AnimatedVisibility(imageUrl!="") {
            View(imageUrl = imageUrl, hideDialog = {imageUrl=""})
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
                items(messages) { message ->
                    MessageItem(message = message,
                        state, viewImage = {imageUrl = it},
                        reaction = { viewModel.Reaction(it, chatId, message.msgId) },
                        selectionMode = { selectionMode = true
                                        selectedItem.add(it)}, mode = selectionMode, Selected = {if(selectedItem.contains(message.msgId))  {selectedItem.remove(it); if (selectedItem.size==0) selectionMode = false} else selectedItem.add(it) }, isSelected = selectedItem.contains(message.msgId)
                        )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)) {
                TextField(modifier = Modifier.weight(1f), placeholder = { Text(text = "Message")},
                    value = reply,
                    onValueChange = { reply=it },
                   // trailingIcon = {
                    //    IconButton(onClick = { launcher.launch("image/*") }) {
                    //        Icon(
                    //            imageVector = Icons.Filled.InsertPhoto,
                    //            contentDescription = null
                    //        )
                   //     }
                   // },
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
                            scope.launch {
                                viewModel.UploadImage(imgUri!!) { imageUrl ->
                                    viewModel.sendReply(chatId = chatId, msg = reply, imgUrl = imageUrl)
                                }
                                reply = ""
                                imgUri = null
                            }

                             }
                    }) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(message: Message, state: AppState, reaction:(String)->Unit, viewImage:(String)->Unit, selectionMode:(String)->Unit, mode: Boolean, Selected:(String)->Unit, isSelected: Boolean) {
    fun Path.rightBubbleShape(
        size: Size,
        cornerRadius: Float,
        tailWidth: Float,
    ) {

        val arcBoxSize = cornerRadius * 2
        moveTo(size.width, size.height)
        arcTo(
            rect = Rect(
                left = 0f,
                top = size.height - arcBoxSize,
                right = arcBoxSize,
                bottom = size.height,
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        arcTo(
            rect = Rect(
                left = 0f,
                top = 0f,
                right = arcBoxSize,
                bottom = arcBoxSize,
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        arcTo(
            rect = Rect(
                left = size.width - arcBoxSize - tailWidth,
                top = 0f,
                right = size.width - tailWidth,
                bottom = arcBoxSize,
            ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        cubicTo(
            x1 = size.width - tailWidth,
            y1 = size.height - cornerRadius,
            x2 = size.width - tailWidth,
            y2 = size.height,
            x3 = size.width,
            y3 = size.height,
        )
        close()
    }

    val cornerRadius = 20.dp
    val tailWidth = 15.dp
    val rightBubblePadding = PaddingValues(
        start = 20.dp,
        end = 20.dp + tailWidth,
        top = 12.dp,
        bottom = 12.dp,
    )
    val (radius, tail) = with(LocalDensity.current) {
        listOf(cornerRadius.toPx(), tailWidth.toPx())
    }
    val rightBubble = remember {
        GenericShape { size, _ ->
            this.rightBubbleShape(
                size = size,
                cornerRadius = radius,
                tailWidth = tail,
            )
        }
    }
    val context = LocalContext.current
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF42238A),
        Color(0xFF5E1D81)
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
    val clkcolor = if(isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent
    Box(
        modifier = Modifier
            .background(clkcolor)
            //.combinedClickable(
            //   onLongClick = {
            //  if (!mode) selectionMode(message.msgId) else {
            //      null
            //  }
            //  }, onClick = { if (mode) Selected(message.msgId)  else onClick=!onClick})
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 10.dp),
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
                            .padding(7.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .size(60.dp)
                    )
                }
                if(message.content!=""){
                    Text(
                        text = message.content.toString(),
                        modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        color = Color.White
                    )
                }
                Text(
                    text = formatter.format(message.time?.toDate()!!),
                    modifier = Modifier.padding(end = 8.dp, bottom = 5.dp, start = 8.dp, top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
            AnimatedVisibility(message.reaction.toString()!="") {
                Text(text = message.reaction.toString(), modifier = Modifier
                    .offset(y = (-10).dp)
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
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
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
                    DropdownMenuItem(
                        text = { Text(text = "Copy", style = MaterialTheme.typography.bodyLarge) },
                        onClick = { val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData: ClipData = ClipData.newPlainText("text", message.content)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                            onClick = false
                        }
                    )
                }
            }

        }
        AnimatedVisibility(onClick && isCurrentUser) {
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
                DropdownMenu( offset = DpOffset(50.dp, 30.dp),
                    expanded = onClick,
                    onDismissRequest = { onClick = false },
                    modifier = Modifier
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Copy", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            val clipboardManager =
                                context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData: ClipData = ClipData.newPlainText("text", message.content)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                            onClick = false
                        }
                    )
                }
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
