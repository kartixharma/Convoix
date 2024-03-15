package com.example.convoix.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.Pref
import com.example.convoix.R

@Composable
fun Customization(viewModel: ChatViewModel, state: AppState) {
    val pref = state.userData?.pref
    var sliderPosition by remember { mutableFloatStateOf(pref?.fontSize.toString().toFloat()) }
    var bsliderPosition by remember { mutableFloatStateOf(pref?.back.toString().toFloat()) }
    var dsliderPosition by remember { mutableFloatStateOf(pref?.doodles.toString().toFloat()) }
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
        Box(modifier = Modifier.shadow(5.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.inversePrimary,
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp))
            .height(230.dp)) {
            Image(modifier = Modifier
                .fillMaxSize()
                .alpha(bsliderPosition)
                .clip(RoundedCornerShape(16.dp)),
                painter = painterResource(R.drawable.blurry_gradient_haikei),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Image(modifier = Modifier
                .fillMaxSize()
                .alpha(
                    dsliderPosition
                )
                .clip(RoundedCornerShape(16.dp)),
                painter = painterResource(R.drawable.social_media_doodle_seamless_pattern_vector_27700734),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Text(text = "Preview", textAlign = TextAlign.Center, modifier = Modifier.fillMaxSize().padding(10.dp))
            Column (modifier = Modifier.padding(bottom = 10.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom){
                MessageItem("This is a Sample Text", alignment = Alignment.CenterStart, brush2, size)
                MessageItem("This is a sample Text 2", alignment = Alignment.CenterEnd, brush,size)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier
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
        Row(modifier = Modifier
            .padding(top = 20.dp)
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
                    text = "background",
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
        Row(modifier = Modifier
            .padding(top = 20.dp)
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
                    text = "doodles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Slider(
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
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { viewModel.updatePref(state.userData!!, Pref(fontSize = sliderPosition, back = bsliderPosition, doodles = dsliderPosition))
                Toast.makeText(context, "Preferences saved", Toast.LENGTH_SHORT).show()},
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
fun MessageItem(
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
                Text(
                    text = text,
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
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