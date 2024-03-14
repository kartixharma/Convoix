@file:OptIn(ExperimentalToolkitApi::class)

package com.example.convoix.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.GoogleAuthUiClient
import com.example.convoix.R
import com.primex.core.ExperimentalToolkitApi
import com.primex.core.blur.legacyBackgroundBlur
import com.primex.core.noise

@Composable
fun SignInScreen1(
    state: AppState,
    onSignInCLick:() -> Unit,
    custom:(String, String) -> Unit,
    googleAuthUiClient: GoogleAuthUiClient
){
    var empty by remember {
        mutableStateOf(false)
    }
    var acc by remember {
        mutableStateOf(true)
    }
    var showPass by remember {
        mutableStateOf(false)
    }
    val brush = Brush.linearGradient(listOf(
        Color(0xFF1D5ED5),
        Color(0xFF4D7CD1)
    ))
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    Image(
        painter = painterResource(id = R.drawable.login_back
        ),
        contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Column(modifier = Modifier.padding(horizontal = 30.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Welcome to Convoix", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold))
        Text(text = "Dive in and connect with friends, share stories, and discover new perspectives.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(40.dp))
        Column(modifier = Modifier
            .shadow(5.dp, RoundedCornerShape(18.dp))
            .background(
                MaterialTheme.colorScheme.onBackground.copy(0.1f),
                RoundedCornerShape(18.dp)
            )
            .border(0.3.dp, Color.Gray, RoundedCornerShape(18.dp))
            .legacyBackgroundBlur(25f, 0.1f)
            .noise(0.03f),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Sign in to your account",
                style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(30.dp))
            if(!acc){
                Text(
                    text = "Account does not exist",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error)
            }
            if(empty){
                Text(
                    text = "Enter required details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error)
            }
            OutlinedTextField(value = email,
                onValueChange = { email=it ; empty=false; acc=true},
                shape = CircleShape,
                placeholder = { Text(text = "Enter your Email") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = password,
                onValueChange = { password=it; empty=false; acc=true },
                shape = CircleShape,
                placeholder = { Text(text = "Enter password") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color.White.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                visualTransformation =  if(showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { showPass = !showPass }) {
                    if(showPass) Icon(imageVector = Icons.Filled.Visibility, contentDescription = null)
                    else Icon(imageVector = Icons.Filled.VisibilityOff, contentDescription = null)
                } }
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(modifier = Modifier
                .height(50.dp)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .background(brush, CircleShape),
                onClick = {
                    if(email.isEmpty() || password.isEmpty()){
                        empty=true
                    }
                    else{
                        custom(email, password)
                    }
                        }
                    ,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Text(text = "SIGN IN", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Don't have an account? Sign up", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(20.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "or", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(10.dp))
        Button(modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(0.85f)
            .shadow(5.dp, CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(0.1f), CircleShape)
            .border(0.3.dp, Color.Gray, CircleShape)
            .legacyBackgroundBlur(25f, 0.1f)
            .noise(0.03f),
            onClick = onSignInCLick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
            Text(text = "Continue with Google", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Image(painter = painterResource(id = R.drawable.goog_0ed88f7c),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(30.dp))
        }
    }
}