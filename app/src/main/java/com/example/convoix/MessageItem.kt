package com.example.convoix

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.Firebase.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.delimiterOffset
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(search: String,
                searchIndx: Int,
                message: Message,
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
                removeReaction: () -> Unit,
                replyMessage: (Message) -> Unit,
                onReplyClick:(String)->Unit,
                cindex: Int,
                index: Int,
                playVideo:(Message)->Unit
) {
    val context = LocalContext.current
    val centerX = LocalConfiguration.current.screenWidthDp.dp / 2
    val centerY = 64.dp / 2
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(
        bounded = true,
        color = Color(0xFFFFFFFF)
    )
    val density = LocalDensity.current
    LaunchedEffect(key1 = cindex) {
        scope.launch(Dispatchers.IO) {
            if (cindex == index) {
                with(density) {
                    delay(100)
                    interactionSource.emit(PressInteraction.Press(Offset(centerX.toPx(), centerY.toPx())))
                    delay(400)
                    interactionSource.emit(PressInteraction.Release(PressInteraction.Press(Offset(centerX.toPx(), centerY.toPx()))))
                }
            }
        }
    }
    var released by remember {
        mutableStateOf(false)
    }
    var offset by remember { mutableStateOf(0.dp) }
    val aoffset by animateDpAsState(targetValue = if (released) 0.dp else offset)
    val size by animateDpAsState(targetValue = if (offset > 70.dp) 30.dp else 20.dp)
    val brush = Brush.linearGradient(
        listOf(
            Color(0xFF238CDD),
            Color(0xFF1952C4)
        )
    )
    val brush2 = Brush.linearGradient(
        listOf(
            Color(0xFF2A4783),
            Color(0xFF2F6086)
        )
    )
    val isCurrentUser = state.userData?.userId == message.senderId
    val shape =  if (isCurrentUser) {
        if (prevId == message.senderId && nextId == message.senderId) {
            RoundedCornerShape(16.dp, 3.dp, 3.dp, 16.dp)
        } else if (prevId == message.senderId) {
            RoundedCornerShape(16.dp, 16.dp, 3.dp, 16.dp)
        } else if (nextId == message.senderId) {
            RoundedCornerShape(16.dp, 3.dp, 16.dp, 16.dp)
        } else {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 16.dp)
        }
    } else {
        if (prevId == message.senderId && nextId == message.senderId) {
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 3.dp)
        } else if (prevId == message.senderId) {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 3.dp)
        } else if (nextId == message.senderId) {
            RoundedCornerShape(3.dp, 16.dp, 16.dp, 16.dp)
        } else {
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
    val clkcolor = if(isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else if(searchIndx == index) Color.White.copy(alpha = 0.4f) else Color.Transparent
    Box(modifier = Modifier
        .indication(interactionSource, indication)
        .pointerInput(message) {
            scope.launch(Dispatchers.IO) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        released = false
                    },
                    onDragEnd = {
                        if (offset > 70.dp) {
                            replyMessage(message)
                        }
                        released = true
                        offset = 0.dp
                    }
                ) { _, dragAmount ->
                    if (offset >= 0.dp)
                        offset += (dragAmount * 0.35f).toDp()
                }
            }
        }
        .background(clkcolor)
        .combinedClickable(enabled = true,
            onLongClick = {
                if (!mode) selectionMode(message.msgId) else {
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
        Icon(modifier = Modifier
            .size(size)
            .align(Alignment.CenterStart)
            .offset(aoffset - 50.dp),
            tint = if(offset>70.dp) Color.White else Color.Gray,
            imageVector = Icons.AutoMirrored.Rounded.Reply, contentDescription = null)
        Column(
            verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .offset(aoffset)
                    .shadow(2.dp, shape)
                    .widthIn(max = 270.dp)
                    .fillMaxHeight()
                    .background(color, shape), horizontalAlignment = Alignment.End
            ) {
                if(message.forwarded) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .padding(horizontal = 10.dp)) {
                        Icon(modifier = Modifier.size(15.dp),
                            painter = painterResource(id = R.drawable.pngwing_com),
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                        Text(
                            text = "Forwarded",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = state.userData?.pref?.fontSize.toString()
                                    .toFloat().sp.times(0.8)
                            ),
                            color = Color.LightGray,
                        )
                    }
                }
                if(message.repliedMsg?.imgUrl!!.isNotEmpty() || message.repliedMsg.content!!.isNotEmpty() || message.repliedMsg.vidUrl.isNotEmpty() || message.repliedMsg.fileUrl.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onReplyClick(message.repliedMsg.msgId) }
                            .background(Color.Black.copy(alpha = 0.4f)),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (message.repliedMsg.imgUrl.isNotEmpty()) {
                            AsyncImage(
                                model = message.repliedMsg.imgUrl,
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .aspectRatio(1f / 1f)
                                    .padding(5.dp)
                                    .clip(
                                        RoundedCornerShape(7.dp)
                                    )
                            )
                        }
                        if(message.repliedMsg.vidUrl.isNotEmpty()) {
                            Image(painter = painterResource(id = R.drawable.play),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(40.dp),
                                contentDescription = null
                            )
                        }
                        if(message.repliedMsg.fileUrl.isNotEmpty()) {
                            Icon(imageVector = Icons.Rounded.InsertDriveFile,
                                modifier = Modifier
                                    .padding(top = 5.dp, start = 5.dp, bottom = 5.dp)
                                    .size(45.dp),
                                contentDescription = null
                            )
                        }
                        Column {
                            Text(
                                text = if(message.repliedMsg.senderId==state.userData?.userId) state.userData.username.toString() else state.User2?.username.toString(),
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .padding(top = 4.dp),
                                fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp.times(0.85),
                                color = Color(0xFF3CAAFF),
                                fontWeight = FontWeight.Black
                            )
                            if (message.repliedMsg.content!!.isNotEmpty()) {
                                Text(
                                    text = message.repliedMsg.content,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .padding(bottom = 4.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = state.userData?.pref?.fontSize.toString()
                                            .toFloat().sp.times(0.8)
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            else if(message.repliedMsg.fileUrl.isNotEmpty()) {
                                Text(
                                    text = message.repliedMsg.fileName,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .padding(bottom = 4.dp),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = state.userData?.pref?.fontSize.toString()
                                            .toFloat().sp.times(0.8)
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if(message.fileUrl!="" && message.fileUrl!="uploadingFile"){
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val uri = Uri.parse(message.fileUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                        .background(Color.Black.copy(alpha = 0.4f))
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(modifier = Modifier
                            .padding(5.dp)
                            .size(45.dp),
                            imageVector = Icons.Rounded.InsertDriveFile, contentDescription = null)
                        Column {
                            Text(modifier = Modifier.width(190.dp),
                                text = message.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White)
                            Text(text = message.fileSize)
                        }
                    }
                }
                if(message.fileUrl=="uploadingFile" && isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column (modifier = Modifier
                            .padding(5.dp)
                            .size(45.dp)){
                            LottieAnimation(
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                        }
                        Column {
                            Text(modifier = Modifier.width(190.dp),
                                text = message.fileName,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White)
                            Text(modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                text = message.progress,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                if(message.fileUrl=="uploadingFile" && !isCurrentUser){
                    Row(modifier = Modifier
                        .padding(4.dp, 4.dp, 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                            Text(text = "Sending a File",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = state.userData?.pref?.fontSize.toString().toFloat().sp),
                                color = Color.White)
                    }
                }
                if(message.vidUrl!= "" && message.vidUrl!="uploadingVideo") {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage (
                            model = ImageRequest.Builder(context)
                                .data(message.vidUrl)
                                .videoFrameMillis(0)
                                .build(),
                            contentDescription = "First frame of video",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .size(60.dp)
                        )
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    playVideo(message)
                                }
                                .background(Color.Black.copy(alpha = 0.5f))
                                .size(60.dp)
                        ) {
                        }
                        Image(painter = painterResource(id = R.drawable.play), contentDescription = null)
                    }
                }
                if(message.vidUrl=="uploadingVideo" &&  isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Column(verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            LottieAnimation(
                                modifier = Modifier.size(80.dp),
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                            Text(modifier = Modifier
                                .padding(top = 4.dp)
                                .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                text = message.progress,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                if(message.vidUrl=="uploadingVideo" && !isCurrentUser){
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Text(text = "Sending a Video...",
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="") 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                            state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                if(message.imgUrl!="" && message.imgUrl!="uploadingImage"){
                    AsyncImage(
                        model = message.imgUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clickable { viewImage(message) }
                            .aspectRatio(16f / 9f)
                            .padding(4.dp, 4.dp, 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .size(60.dp)
                    )
                }
                if(message.imgUrl=="uploadingImage" && isCurrentUser){
                    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = message.imgUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .size(60.dp)
                        )
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Column(verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            LottieAnimation(
                                modifier = Modifier.size(80.dp),
                                composition = comp,
                                iterations = LottieConstants.IterateForever
                            )
                            Text(modifier = Modifier
                                .padding(top = 4.dp)
                                .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                                text = message.progress,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                if(message.imgUrl=="uploadingImage" && !isCurrentUser){
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(4.dp, 4.dp, 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .blur(5.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f))
                                .size(60.dp)
                        ) {

                        }
                        Text(text = "Sending an Image...",
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="") 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                                state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                if(message.content!="") {
                    if(search.isNotEmpty()) {
                    Text(text = buildAnnotatedString {
                                var start = 0
                                while (message.content.toString().indexOf(
                                        search,
                                        start,
                                        ignoreCase = true
                                    ) != -1 && search.isNotBlank()
                                ) {
                                    val firstIndex = message.content.toString().indexOf(search, start, true)
                                    val end = firstIndex + search.length
                                    append(message.content.toString().substring(start, firstIndex))
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.SemiBold,
                                            background = Color.White
                                        )
                                    ) {
                                        append(
                                            message.content.toString().substring(firstIndex, end)
                                        )
                                    }
                                    start = end
                                }
                                append(
                                    message.content.toString()
                                        .substring(start, message.content.toString().length)
                                )
                                toAnnotatedString()
                            },
                        modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="" ) 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize =
                        if(Regex("[\\p{So}\\p{Sk}]").matches(message.content.toString())){
                            state.userData?.pref?.fontSize.toString().toFloat().sp.times(3)
                        }
                        else
                            state.userData?.pref?.fontSize.toString().toFloat().sp
                        ),
                        color = Color.White
                    )}
                    else{
                        Text(text = message.content.toString(),
                            modifier = Modifier.padding(top = if(message.forwarded && message.imgUrl=="" ) 0.dp else 5.dp, start = 10.dp, end = 10.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize =
                            if(Regex("[\\p{So}\\p{Sk}]").matches(message.content.toString())){
                                state.userData?.pref?.fontSize.toString().toFloat().sp.times(3)
                            }
                            else
                                state.userData?.pref?.fontSize.toString().toFloat().sp
                            ),
                            color = Color.White
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(end = 8.dp, bottom = if(message.reaction.isNotEmpty()) 10.dp else 5.dp, start = 8.dp, top = 2.dp)) {
                    Text(
                        text = formatter.format(message.time?.toDate()!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                    if(isCurrentUser) {
                        Icon(modifier = Modifier.size(10.dp),
                            painter = painterResource(id = R.drawable.check_mark),
                            contentDescription = null,
                            tint = if(message.read) Color(0xFF13C70D) else Color.White
                        )
                    }
                }
            }
            if(message.reaction.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .offset(aoffset)
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
        Column{
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                0xFF274F6F)
            )) {
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
                        onClick = { val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
