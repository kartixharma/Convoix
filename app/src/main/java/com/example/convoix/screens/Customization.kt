package com.example.convoix.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.Firebase.Pref
import com.example.convoix.R

@Composable
fun Customization(viewModel: ChatViewModel, state: AppState) {
    val pref = state.userData?.pref
    var anim by remember { mutableStateOf(pref?.anim) }
    var sliderPosition by remember { mutableFloatStateOf(pref?.fontSize.toString().toFloat()) }
    var bsliderPosition by remember { mutableFloatStateOf(pref?.back.toString().toFloat()) }
    var dsliderPosition by remember { mutableFloatStateOf(pref?.doodles.toString().toFloat()) }
    var customImg by remember { mutableStateOf(pref?.customImg.toString()) }
    var imgUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var isUploading by remember {
        mutableStateOf(false)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()){
        imgUri = it
    }
    var bitmap by remember {mutableStateOf<Bitmap?>(null)}
    val size = sliderPosition.sp
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    val brush2 = Brush.linearGradient(listOf(
        Color(0xFF2A4783),
        Color(0xFF2F6086)
    ))
    val context = LocalContext.current
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.upload))
    val comp1 by rememberLottieComposition(LottieCompositionSpec.RawRes(
        when(anim){
            1 -> R.raw.animation1
            2 -> R.raw.animation2
            3 -> R.raw.animation3
            else -> {
                R.raw.animation4
            }
        }))
    Image(modifier = Modifier.fillMaxSize(),
        painter = painterResource(R.drawable.blurry_gradient_haikei),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 50.dp)
        .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Customization",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp,)
        )
        Box(
            modifier = Modifier
                .shadow(5.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.inversePrimary,
                    RoundedCornerShape(16.dp)
                )
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
                .height(350.dp)
        ) {
            if (customImg.length == 0) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(R.drawable.blurry_gradient_haikei),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(
                            dsliderPosition
                        )
                        .clip(RoundedCornerShape(16.dp)),
                    painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            if (customImg.isNotBlank()) {
                AsyncImage(
                    model = customImg, contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            imgUri?.let {
                customImg = "1"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(LocalContext.current.contentResolver, it)
                    bitmap = ImageDecoder.decodeBitmap(src)
                }
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(bsliderPosition)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = "Preview", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
            Column(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxSize(), verticalArrangement = Arrangement.Bottom
            ) {
                MessageItem1("Hey there! How's your day going?", alignment = Alignment.CenterStart, brush2, size)
                MessageItem1("Doing well, You?", alignment = Alignment.CenterEnd, brush, size)
                LottieAnimation(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(80.dp)
                        .alpha(0.8f),
                    composition = comp1,
                    iterations = LottieConstants.IterateForever
                )
            }
            Text(
                text = "Preview", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            )
            if (isUploading) {
                LottieAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .scale(0.5f),
                    composition = comp,
                    iterations = LottieConstants.IterateForever
                )
            }
        }
    Spacer(modifier = Modifier.height(20.dp))
    LazyColumn(modifier = Modifier.weight(1f)) {
        item {
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Font size",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        steps = 4,
                        valueRange = 14f..24f
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Typing indicator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround) {
                        RadioButton(selected = anim==1, onClick = { anim=1 })
                        RadioButton(selected = anim==2, onClick = { anim=2 })
                        RadioButton(selected = anim==3, onClick = { anim=3 })
                        RadioButton(selected = anim==4, onClick = { anim=4 })
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Background",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        value = bsliderPosition,
                        onValueChange = { bsliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),

                        )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "Doodles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Slider(
                        enabled = customImg.isEmpty(),
                        value = dsliderPosition,
                        onValueChange = { dsliderPosition = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.secondary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        valueRange = 0f..0.5f
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Browse images",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(25.dp)
                    )
                }
                if (customImg.isNotBlank()) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Column(
                        modifier = Modifier
                            .clickable {
                                customImg = ""
                                imgUri = null
                            }
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Remove image",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(25.dp)
                        )
                    }
                }

            }
        }
    }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if(imgUri!=null){
                isUploading=true
                viewModel.UploadImage1(imgUri!!){
                    viewModel.updatePref(state.userData!!,
                        state.userData.pref.copy(
                            anim = anim!!,
                            fontSize = sliderPosition,
                            back = bsliderPosition,
                            doodles = dsliderPosition,
                            customImg=it
                        )
                    )
                    isUploading=false
                    imgUri=null
                }
            }
              else {
                viewModel.updatePref(state.userData!!,
                    state.userData.pref.copy(
                        anim = anim!!,
                        fontSize = sliderPosition,
                        back = bsliderPosition,
                        doodles = dsliderPosition,
                        customImg=customImg
                    )
                )
            }
                Toast.makeText(context, "Preferences saved", Toast.LENGTH_SHORT).show()
                        },
            modifier = Modifier
                .padding(bottom = 50.dp)
                .background(brush, CircleShape)
                .fillMaxWidth(0.7f)
                .height(50.dp), colors = ButtonDefaults.buttonColors(Color.Transparent), shape = CircleShape
        ) {
            Text(
                text = "Save preferences",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
@Composable
fun MessageItem1(
    text: String,
    alignment: Alignment,
    brush: Brush,
    size: TextUnit,
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = 3.dp,
                start = 10.dp,
                end = 10.dp
            ),
        contentAlignment = alignment
    ) {
        Column(verticalArrangement = Arrangement.Bottom) {
            Column(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .widthIn(max = 270.dp)
                    .background(brush, RoundedCornerShape(16.dp)),
                horizontalAlignment = Alignment.End
            ) {
                if(alignment == Alignment.CenterEnd){
                    Row(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Text(
                                text = "Username",
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .padding(top = 4.dp),
                                fontSize = size.times(0.85),
                                color = Color(0xFF3CAAFF),
                                fontWeight = FontWeight.Black
                            )
                        Text(
                            text = "Hey there! How's your day going?",
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .padding(bottom = 4.dp),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = size.times(0.8)
                            ),
                            color = Color.White,
                        )
                    }
                    }
                }

                Text(
                    text = text,
                    modifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = size),
                    color = Color.White
                )
                Text(
                    text = "12:30 pm",
                    modifier = Modifier.padding(
                        end = 8.dp,
                        bottom = 5.dp,
                        start = 8.dp,
                        top = 2.dp
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}