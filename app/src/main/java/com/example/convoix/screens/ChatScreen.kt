@file:OptIn(ExperimentalToolkitApi::class)

package com.example.convoix.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.Firebase.ChatData
import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.ChatViewModel
import com.example.convoix.Dialogs.CustomDialogBox
import com.example.convoix.Dialogs.DeleteDialog
import com.example.convoix.Dialogs.StoryDialog
import com.example.convoix.Dialogs.StoryPreview
import com.example.convoix.R
import com.example.convoix.Firebase.Story
import com.primex.core.ExperimentalToolkitApi
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, viewModel: ChatViewModel, state: AppState, showSingleChat: (ChatUserData, String) -> Unit){
    val notLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()){

    }
    LaunchedEffect(key1 = true){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val myStoryVm = viewModel.stories.filter {
        it.userId==state.userData?.userId
    }
    val myStory = if (myStoryVm.isEmpty()) {
        listOf(Story())
    } else {
        myStoryVm
    }
    val stories = viewModel.stories.filter {
        it.userId!=state.userData?.userId
    }
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
    var isUploading by remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateListOf<String>()
    }
    var curStory by remember {
        mutableStateOf(Story())
    }
    var showStory by remember {
        mutableStateOf(false)
    }
    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }
    var bitmap by remember {mutableStateOf<Bitmap?>(null)}
    val border =
        Brush.sweepGradient(
            listOf(
                Color(0xFFA7E6FF),
                Color(0xFFA7E6FF),
            )
        )

    val border1 = Brush.sweepGradient(
            listOf(
                Color(0xFF777777),
                Color(0xFF777777),
        )
    )


    val story = Brush.sweepGradient(
            listOf(
                Color(0xFF78BEDA),
                Color(0xFF2458DB),
                Color(0xFFFF71FF),
                Color(0xFF8B2ECF),
                Color(0xFF3F3FD3),
                Color(0xFF78BEDA),
            ),
        )
    var showSearch by remember {
        mutableStateOf(false)
    }
    val padding by animateDpAsState(targetValue = if (showSearch) 15.dp else 10.dp)
    var showDialog by remember {
        mutableStateOf(false)
    }
    var expanded by remember { mutableStateOf(false) }
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    BackHandler {
        showDialog=false
        showStory=false
        searchText=""
        showSearch=false
        selectedItem.clear()
        selectionMode=false
        imgUri=null
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
            painter = painterResource(id = R.drawable.blck_blurry),
            contentDescription = "",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        AnimatedVisibility(showDialog) {
            DeleteDialog(
                hideDialog = { showDialog = !showDialog
                    selectedItem.clear() },
                deleteChat = { viewModel.deleteChat(selectedItem)
                    showDialog = !showDialog
                    selectedItem.clear() }
            )
        }
        AnimatedVisibility(showStory) {
            StoryDialog(
                state,
                story = curStory,
                hideDialog = { showStory = !showStory
                    selectedItem.clear() },
                deleteStory = { viewModel.deleteStory(curStory, it) }
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
        imgUri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(LocalContext.current.contentResolver,it)
                bitmap = ImageDecoder.decodeBitmap(src)
            }
            StoryPreview(bitmap = bitmap, hideDialog = { imgUri=null }, upload = {
                isUploading = true
                viewModel.UploadImage1(imgUri!!){
                    viewModel.uploadStory(it, myStory[0].id)
                    isUploading = false
                }
                imgUri = null
            })
            }
        Column(modifier = Modifier
            .padding(top = 36.dp)) {//.background(colorScheme.primaryContainer)
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
                                if(!searchText.isBlank())
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
                                text = "Hello",
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                            )
                            Text(
                                text = state.userData?.username.toString(),
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
                        Column {
                            IconButton(modifier = Modifier
                                .background(colorScheme.background.copy(alpha = 0.2f), CircleShape)
                                .border(0.05.dp, Color(0xFF35567A), CircleShape),
                                onClick = { expanded=true }) {
                                Icon(
                                    modifier = Modifier.scale(1.3f),
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = null
                                )
                            }
                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp)), colorScheme = MaterialTheme.colorScheme.copy(background = Color(
                                0xFF294F86
                            )
                            ) ) {
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
                                        onClick = { navController.navigate("settings")
                                            expanded=false}
                                    )
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(!showSearch){
                LazyRow {
                    item {
                        if(isUploading){
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        curStory = myStory[0]
                                        showStory = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                                    .size(90.dp)
                                    .padding(7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(state.userData?.ppurl)
                                        .crossfade(true)
                                        .allowHardware(false)
                                        .build(),
                                    placeholder = painterResource(id = R.drawable.person_placeholder_4),
                                    error = painterResource(id = R.drawable.person_placeholder_4),
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clip(CircleShape)
                                )
                                LottieAnimation(composition = comp, iterations = LottieConstants.IterateForever)
                            }
                        }
                        if(myStory[0].userId.length>0 && !isUploading){
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        curStory = myStory[0]
                                        showStory = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                                    .size(90.dp)
                                    .padding(7.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(myStory[0].ppurl)
                                            .crossfade(true)
                                            .allowHardware(false)
                                            .build(),
                                        placeholder = painterResource(id = R.drawable.person_placeholder_4),
                                        error = painterResource(id = R.drawable.person_placeholder_4),
                                        contentDescription = "Profile picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.clip(CircleShape).fillMaxSize()
                                    )
                                    Icon( modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            launcher.launch("image/*")
                                        },
                                        imageVector = Icons.Filled.AddCircle, contentDescription = null)
                            }
                        }
                        if(myStory[0].userId.length==0 && !isUploading){
                            Box(modifier = Modifier
                                .padding(vertical = 15.dp, horizontal = 10.dp)
                                .size(70.dp)
                                .drawWithCache {
                                    onDrawBehind {
                                        drawCircle(
                                            brush = border,
                                            style = Stroke(
                                                width = 6f,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(
                                                        85f,
                                                        15f
                                                    ), 0f
                                                )
                                            )
                                        )
                                    }
                                }
                                .padding(5.dp)
                                .background(colorScheme.background.copy(alpha = 0.4f), CircleShape)
                                .clickable {
                                    launcher.launch("image/*")
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(modifier = Modifier.size(40.dp),
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    items(stories){
                        Box(
                            modifier = Modifier
                                .clickable {
                                    curStory = it
                                    showStory = true
                                    viewModel.viewStory(it.id)
                                }
                                .padding(horizontal = 6.dp, vertical = 10.dp)
                                .size(80.dp)
                                .shadow(1.dp, CircleShape)
                                .border(
                                    if (it.viewedBy.contains(state.userData?.userId)) 2.dp else 3.dp,
                                    if (it.viewedBy.contains(state.userData?.userId)) border1 else story,
                                    CircleShape
                                )
                                .padding(7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it.ppurl)
                                    .crossfade(true)
                                    .allowHardware(false)
                                    .build(),
                                placeholder = painterResource(id = R.drawable.person_placeholder_4),
                                error = painterResource(id = R.drawable.person_placeholder_4),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape)
                            )
                        }
                    }
                }
            }
            LazyColumn(modifier= Modifier
                .padding(top = padding)
                .fillMaxSize()
                .background(
                    colorScheme.background.copy(alpha = 0.2f),
                    RoundedCornerShape(30.dp, 30.dp)
                )
                .border(0.05.dp, Color(0xFF35567A), RoundedCornerShape(30.dp, 30.dp))){
                item {
                    Text(
                        text = "Chats",
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal
                    )
                }
                items(filteredChats){
                        val chatUser = if(it.user1?.userId!=state.userData?.userId) { it.user1 } else it.user2
                        ChatItem(state,
                            chatUser!!, showSingleChat = { user, id-> showSingleChat(user, id)}, it,
                            selectionMode = { selectionMode = true
                                selectedItem.add(it) },
                            mode = selectionMode,
                            Selected = {if(selectedItem.contains(it))  {selectedItem.remove(it); if (selectedItem.size==0) selectionMode = false} else selectedItem.add(it) },
                            isSelected = selectedItem.contains(it.chatId),
                        )
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(state: AppState,
             userData: ChatUserData,
             showSingleChat: (ChatUserData, String) -> Unit,
             chat: ChatData,
             selectionMode:(String)->Unit,
             mode: Boolean,
             Selected:(String)->Unit,
             isSelected: Boolean,
) {
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    val color = if(!isSelected) Color.Transparent else colorScheme.onPrimary
    Row(
        modifier = Modifier
            .background(color)
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    if (!mode) selectionMode(chat.chatId) else {
                        null
                    }
                },
                onClick = {
                    if (mode) Selected(chat.chatId) else showSingleChat(
                        userData,
                        chat.chatId
                    )
                })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(!userData.status){
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
        }
        else{
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
                text = if(userData.userId==state.userData?.userId) userData.username.orEmpty() + " (You)" else userData.username.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AnimatedVisibility(chat.last?.time!=null && !userData.typing) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    if(chat.last?.imgUrl!=""){
                        Icon(modifier = Modifier.padding(end =3.dp),
                            imageVector = Icons.Rounded.Image, contentDescription = null)
                    }

                    Text(modifier = Modifier.widthIn(max = 150.dp),
                        text = chat.last?.content.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Light)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if(chat.last?.time.toString()!="null") formatter.format(chat.last?.time?.toDate()!!) else "",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                    )
                }
            }
            if(userData.typing){
                Text(
                    text = "Typing...",
                    color = Color(0xFF30BDFF),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}