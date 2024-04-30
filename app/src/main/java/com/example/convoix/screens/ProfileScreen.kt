package com.example.convoix.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.convoix.ChatViewModel
import com.example.convoix.Dialogs.ProfileEditDialog
import com.example.convoix.Dialogs.StoryPreview
import com.example.convoix.R
import com.example.convoix.Firebase.UserData
import com.example.convoix.Dialogs.View
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel, state: AppState,
    onSignOut: () -> Unit, navController: NavController
) {
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val context = LocalContext.current
    val user = state.userData
    var editProfile by remember {
        mutableStateOf(false)
    }
    var viewImage by remember {
        mutableStateOf(false)
    }
    var isLoading by remember {
        mutableStateOf(false)
    }
    var name by remember {
        mutableStateOf(user?.username)
    }
    var bio by remember {
        mutableStateOf(user?.bio)
    }
    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }
    var bitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    Image(modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.blurry_gradient_haikei),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
    fun compressImage(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return outputStream.toByteArray()
    }
    fun uploadImage(){
        viewModel.UploadImage(compressImage()) { imageUrl ->
            viewModel.updateProfile(
                userData = UserData(
                    email = user?.email.toString(),
                    userId = user?.userId.toString(),
                    username = name,
                    bio = bio.toString(),
                    ppurl = imageUrl
                )
            )
            isLoading = false
        }

    }
    BackHandler {
        if (editProfile || viewImage || imgUri!=null) {
            editProfile = false
            viewImage = false
            imgUri = null
        } else {
            navController.popBackStack()
        }
    }
    Box(contentAlignment = Alignment.TopCenter) {
        Image(modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.screen1 ),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Row(modifier = Modifier.padding(horizontal = 15.dp)) {
            IconButton(modifier = Modifier.padding(top = 40.dp), onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
        }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(modifier = Modifier.padding(top = 40.dp), onClick = { editProfile = true}) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
            }
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
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.ppurl)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, CircleShape)
                            .shadow(1.dp, shape = CircleShape)
                            .clickable { viewImage = true }
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    )
                    if (isLoading){
                        Column(modifier = Modifier
                            .background(Color.DarkGray.copy(alpha = 0.7f), CircleShape)
                            .size(150.dp)) {
                        }
                        LottieAnimation(composition = comp, iterations = LottieConstants.IterateForever)
                    }
                }
                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier
                    .size(40.dp)
                    .offset(x = 110.dp, y = (-50).dp)
                    .background(MaterialTheme.colorScheme.inversePrimary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clip(CircleShape)) {
                   Icon(imageVector = Icons.Filled.Edit, contentDescription = null,modifier = Modifier.size(20.dp))
                }
            }
        }
        Text(text = user?.username.toString(),
            modifier = Modifier
                .offset(y = (-25).dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = user?.email.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
            Column( modifier = Modifier
                .fillMaxWidth(0.6f)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))) {
                Text(text = if (user?.bio.toString() != "") "Bio:\n"+user?.bio.toString() else "No Bio",
                    modifier = Modifier
                        .width(250.dp)
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (user?.bio.toString() != "") Color.White else Color.LightGray
                )
            }
        Spacer(modifier = Modifier.height(16.dp))
    }
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 50.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.updateStatus(false)
                viewModel.removeL()
                viewModel.removeFCMToken(user?.userId.toString())
                onSignOut()
                      },
            modifier = Modifier
                .background(brush, CircleShape)
                .fillMaxWidth(0.7f)
                .height(50.dp), colors = ButtonDefaults.buttonColors(Color.Transparent), shape = CircleShape
        ) {
            Text(
                text = "Sign Out",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
    imgUri?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val src = ImageDecoder.createSource(context.contentResolver, it)
            bitmap = ImageDecoder.decodeBitmap(src)
        }
        StoryPreview(uri = imgUri, hideDialog = { imgUri = null }, upload = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && it.toString()!="") {
                val src = ImageDecoder.createSource(context.contentResolver, it!!)
                bitmap = ImageDecoder.decodeBitmap(src)
            }
            uploadImage()
            imgUri = null
            isLoading=true })
    }
    AnimatedVisibility(viewImage) {
        View(imageUrl = user?.ppurl.toString(), hideDialog = {viewImage=false})
    }
    AnimatedVisibility(visible = editProfile) {
        ProfileEditDialog(hideDialog = { editProfile = false }, saveProfile = { name, bio ->
            viewModel.updateProfile(
                userData = UserData(
                    email = user?.email.toString(),
                    userId = user?.userId.toString(),
                    username = name,
                    bio = bio,
                    ppurl = user?.ppurl.toString()
                )
            )
            editProfile = false
        }, user = user)
    }

}
