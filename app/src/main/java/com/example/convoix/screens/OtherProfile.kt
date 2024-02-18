package com.example.convoix.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.convoix.ChatUserData
import com.example.convoix.R
import com.example.convoix.View

@Composable
fun OtherProfile(
    user: ChatUserData , navController: NavController
) {
    var viewImage by remember {
        mutableStateOf(false)
    }
    BackHandler {
        if (viewImage) {
            viewImage = false
        } else {
            navController.popBackStack()
        }
    }

    Box {
        Image(modifier = Modifier.fillMaxWidth(),painter = painterResource(id = if(isSystemInDarkTheme()) R.drawable.screen1 else R.drawable.screen),
            contentDescription = null)
        IconButton(modifier = Modifier.padding(top = 40.dp, start = 10.dp), onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 70.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.ppurl?.let {
            Column {
                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = user.ppurl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .shadow(1.dp, shape = CircleShape)
                            .clickable { viewImage = true }
                            .size(150.dp)
                            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clip(CircleShape)
                    )
                }
            }

        }
            Text(text = user.username.toString(),
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
            Column( modifier = Modifier
                .width(250.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))) {
                Text(text = if (user.bio != "") "Bio:\n"+user.bio else "No Bio",
                    modifier = Modifier
                        .width(250.dp)
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (user.bio != "") Color.White else Color.LightGray
                )
            }


    }
    AnimatedVisibility(viewImage) {
        View(imageUrl = user.ppurl, hideDialog = {viewImage=false})
    }
}