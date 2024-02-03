package com.example.convoix

import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.graphics.alpha
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInCLick:() -> Unit,
    onValueChange:(String)->Unit,
    onPassChange:(String)->Unit
){
    val context= LocalContext.current
    LaunchedEffect(key1 = state.signInError){
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_SHORT).show()
        }
    }
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    var passwordVisbility by remember { mutableStateOf(false) }
    Image(modifier = Modifier
        .blur(15.dp)
        .fillMaxWidth()
        .scale(2.5f),
        painter = painterResource(id = R.drawable.abstract_blur_color_background_vector_21457509),
        contentDescription = null)
    if(state.showAnim){
        Column(modifier = Modifier.padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.animation___1706898281923, imageLoader),
                contentDescription = null, Modifier.size(170.dp)
            )
        }
    }
    else{
        Column(modifier = Modifier.padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id =R.drawable.login_user_name_1),
                contentDescription = null, modifier = Modifier.size(130.dp), alpha = 0.7f)
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .padding(top = 300.dp, start = 30.dp, end = 30.dp),
            border = BorderStroke(0.4.dp, Color.Gray),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier
                .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(label = { Text(text = "Email" )},
                    value = state.email,
                    onValueChange = {onValueChange(it)},
                    shape = RoundedCornerShape(20.dp))
                OutlinedTextField(label = { Text(text = "Password" )},
                    value = state.pass,
                    onValueChange = {onPassChange(it)},
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordVisbility = !passwordVisbility
                        }) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Visibility Icon"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = if (passwordVisbility) VisualTransformation.None
                    else PasswordVisualTransformation()
                )
                Button(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp)
                    .height(50.dp),
                    shape =  RoundedCornerShape(20.dp),
                    onClick = onSignInCLick ) {
                        Text(text = "Sign Up", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Text(text = "Or", modifier = Modifier.padding(10.dp),
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Button(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp)
            .height(50.dp),
            shape =  RoundedCornerShape(20.dp),
            onClick = onSignInCLick ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Continue with Google", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Image(painter = painterResource(id = R.drawable.goog_0ed88f7c), contentDescription = null, contentScale = ContentScale.Fit)
            }

        }
    }

}