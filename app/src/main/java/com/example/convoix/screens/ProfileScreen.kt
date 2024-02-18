package com.example.convoix.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.R
import com.example.convoix.UserData
import com.example.convoix.View
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ChatViewModel, state: AppState,
    onSignOut: () -> Unit, navController: NavController
) {
    val context = LocalContext.current
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
    fun compressImage(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
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
        if (editName || editBio || viewImage || imgUri!=null) {
            editName = false
            editBio = false
            viewImage = false
            imgUri = null
        } else {
            navController.popBackStack()
        }
    }
    Box() {
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
                    if (isLoading){
                        LoadingItem()
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
            if (!editName) {
                Text(text = user?.username.toString(),
                    modifier = Modifier
                        .offset(y = (-25).dp)
                        .clickable { editName = true },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        AnimatedVisibility (editName,enter = slideInHorizontally(), exit = ExitTransition.None) {
                OutlinedTextField(modifier = Modifier
                    .offset(y = (-25).dp)
                    .width(250.dp),
                    value = name.toString(),
                    onValueChange = { name = it },
                    placeholder = { Text(text = "Name")},
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors()
                )
            }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = user?.email.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!editBio) {
            Column( modifier = Modifier
                .fillMaxWidth(0.6f)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))) {
                Text(text = if (user?.bio.toString() != "") "Bio:\n"+user?.bio.toString() else "No Bio",
                    modifier = Modifier
                        .width(250.dp)
                        .clickable { editBio = true }
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (user?.bio.toString() != "") Color.White else Color.LightGray
                )
            }
            }

        AnimatedVisibility (editBio, enter = slideInHorizontally(), exit = ExitTransition.None) {
            OutlinedTextField(modifier = Modifier.width(250.dp),
                value = bio.toString(),
                onValueChange = { bio = it },
                placeholder = { Text(text = "Add bio")},
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (editName || editBio) {
            Button(modifier = Modifier.height(50.dp), shape = CircleShape,
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
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFFA02424),
        Color(0xFFC43B56)
    ))
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 50.dp)
    ) {
       // Button(
       //     onClick = { onSignOut() },
      //      modifier = Modifier
        //        .background(brush2, CircleShape)
       //         .fillMaxWidth(0.7f)
        //        .height(50.dp), colors = ButtonDefaults.buttonColors(Color.Transparent), shape = CircleShape
      //  ) {
      //      Text(
       //         text = "Delete account",
       //         color = Color.White,
       //         fontSize = 18.sp,
       //         fontWeight = FontWeight.SemiBold
       //     )
      //  }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSignOut() },
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
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Image(bitmap = bitmap?.asImageBitmap()!!, contentDescription = null, modifier = Modifier
                .fillMaxWidth()
                .size(400.dp)
                .padding(10.dp))
            Button(onClick = { uploadImage(); imgUri = null;isLoading=true  }) {
                Text(text = "Upload")
            }
        }

    }
    AnimatedVisibility(viewImage) {
        View(imageUrl = user?.ppurl.toString(), hideDialog = {viewImage=false})
    }

}

@Composable
fun LoadingItem(){
    Box(modifier = Modifier
        .wrapContentHeight(),
        contentAlignment = Alignment.Center){
        CircularProgressIndicator(
            modifier = Modifier
                .size(42.dp)
                .padding(8.dp),
            strokeWidth = 5.dp
        )
    }
}
