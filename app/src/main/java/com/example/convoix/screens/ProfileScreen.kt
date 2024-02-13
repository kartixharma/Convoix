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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.DeleteDialog
import com.example.convoix.R
import com.example.convoix.UserData
import com.example.convoix.View
import kotlin.io.path.Path

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel, state: AppState,
    onSignOut: () -> Unit, navController: NavController
) {
    val user = state.userData
    var editName by remember {
        mutableStateOf(false)
    }
    var viewImage by remember {
        mutableStateOf(false)
    }
    var editBio by remember {
        mutableStateOf(false)
    }
    var name by remember {
        mutableStateOf(user?.username)
    }
    var bio by remember {
        mutableStateOf(user?.bio)
    }
    BackHandler {
        if (editName || editBio || viewImage) {
            editName = false
            editBio = false
            viewImage = false
        } else {
            navController.popBackStack()
        }
    }
    AnimatedVisibility(viewImage) {
        View(imageUrl = user?.ppurl.toString(), hideDialog = {viewImage=false})
    }
    Image(
        painter = painterResource(id = R.drawable.untitled_1),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = if (!isSystemInDarkTheme()) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null
    )
    Column(
        modifier = Modifier.padding(top = 110.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        user?.ppurl?.let {
            AsyncImage(
                model = user.ppurl,
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .shadow(1.dp, shape = CircleShape)
                    .clickable { viewImage = true }
                    .size(150.dp)
                    .border(5.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
            if (!editName) {
                Text(text = user?.username.toString(),
                    modifier = Modifier
                        .clickable { editName = true },
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }

            if (editName) {
                OutlinedTextField(value = name.toString(), onValueChange = { name = it })
            }


        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.email.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!editBio) {
            Text(text = if (user?.bio.toString() != "") "Bio:\n"+user?.bio.toString() else "No Bio",
                modifier = Modifier.width(250.dp)
                    .clickable { editBio = true }
                    .background(Color.DarkGray, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                color = if (user?.bio.toString() != "") Color.White else Color.LightGray
            )
        }
        if (editBio) {
            OutlinedTextField(value = bio.toString(), onValueChange = { bio = it })
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (editName || editBio) {
            Button(
                onClick = {
                    viewModel.updateProfile(
                        userData = UserData(
                            email = user?.email.toString(),
                            userId = user?.userId.toString(),
                            username = name,
                            bio = bio.toString(),
                            ppurl = user?.ppurl.toString()
                        )
                    )
                    editBio = false
                    editName = false
                },
                modifier = Modifier.fillMaxWidth(0.8f), shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 50.dp)
    ) {
        Button(
            onClick = { onSignOut() },
            modifier = Modifier.fillMaxWidth(0.8f), shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
