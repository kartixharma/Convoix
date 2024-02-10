package com.example.convoix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.R
import kotlin.io.path.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel, state: AppState,
    onSignOut: () -> Unit
) {
    val userData = state.userData
    Image(painter = painterResource(id = R.drawable.untitled_1), contentDescription = null, contentScale = ContentScale.Crop, colorFilter = if(!isSystemInDarkTheme()) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            userData?.ppurl?.let {
                AsyncImage(
                    model = userData.ppurl,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .border(5.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clip(CircleShape)

                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = userData?.username.toString(),
                modifier = Modifier
                    .background(Color.DarkGray, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = userData?.email.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.fillMaxHeight(0.8f))
            Button(
                onClick = { onSignOut() },
                modifier = Modifier.fillMaxWidth(0.8f), shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Sign Out",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
               )
            }
        }
}
