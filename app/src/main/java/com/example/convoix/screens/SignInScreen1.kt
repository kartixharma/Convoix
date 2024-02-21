package com.example.convoix.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.R

@Composable
fun SignInScreen1(
    state: AppState,
    onSignInCLick:() -> Unit,
){
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.done))
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF255DCC)
    ))
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError){
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_SHORT).show()
        }
    }
    Image(modifier = Modifier
        .fillMaxSize(),
        contentScale = ContentScale.Crop,
        painter = painterResource(R.drawable.login),
        contentDescription = null)
    Column(modifier = Modifier.padding(bottom = 200.dp),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if(!state.showAnim){
            Image(modifier = Modifier
                .size(200.dp),
                painter = painterResource(R.drawable.oig3),
                contentDescription = null)
        }
    }
    Column(modifier = Modifier.padding(bottom = 200.dp),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(state.showAnim){
            LottieAnimation(modifier=Modifier.size(250.dp),composition = comp)
        }
    }
    Column(modifier = Modifier.padding(top = 200.dp)
        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Button(
            onClick = onSignInCLick,
            modifier = Modifier
                .background(brush, CircleShape)
                .fillMaxWidth(0.7f)
                .height(60.dp), colors = ButtonDefaults.buttonColors(Color.Transparent), shape = CircleShape
        ) {
            Text(modifier = Modifier.padding(end = 20.dp),
                text = "Continue with Google",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Image(painter = painterResource(id = R.drawable.goog_0ed88f7c), contentDescription = null, contentScale = ContentScale.Fit)
        }
    }
}