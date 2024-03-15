package com.example.convoix.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.Dialogs.UnblockDialog
import com.example.convoix.R
import com.example.convoix.UserData

@Composable
fun BlockedUsers(
    viewModel: ChatViewModel, state: AppState
){
    var showDialog by remember {
        mutableStateOf(false)
    }
    var userId by remember {
        mutableStateOf("")
    }
    AnimatedVisibility(visible = showDialog) {
        UnblockDialog(hideDialog = { showDialog=false }, unblock = { viewModel.unblockUser(userId); showDialog=false })
    }
    Image(modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.blurry_gradient_haikei),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Blocked Users",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp,)
        )
            LazyColumn {
                items(viewModel.list){
                    println(it)
                    Users(it, showDialog={
                        showDialog=true
                        userId=it
                    })
                }
            }

    }
    if(viewModel.list.isEmpty()){
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = "No blocked users",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

    }
}
@Composable
fun Users(userData: UserData, showDialog:(String) -> Unit){
    Row(
        modifier = Modifier
            .background(Color.Transparent).clickable { showDialog(userData.userId) }
            .fillMaxWidth()
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
            Text(
                text = userData.username.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

    }
}