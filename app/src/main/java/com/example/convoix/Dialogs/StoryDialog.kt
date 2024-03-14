package com.example.convoix.Dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.convoix.Story
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StoryDialog(story: Story, hideDialog:()->Unit){
    val formatter = remember {
        SimpleDateFormat(("hh:mm a"), Locale.getDefault())
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .blur(15.dp)) {
        Text(text = "")
    }
    Dialog(onDismissRequest = hideDialog,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    )       {
        var scale by remember {
            mutableStateOf(1f)
        }
        var offset by remember {
            mutableStateOf(Offset.Zero)
        }
        val interactionSource = remember { MutableInteractionSource() }
        var show by remember {
            mutableStateOf(true)
        }
        BoxWithConstraints {
            val state = rememberTransformableState{ zoomChange, panChange, rotationChange ->
                scale = (scale*zoomChange).coerceIn(1f,5f)
                val extWidth = (scale - 1)*constraints.maxWidth
                val extHeight = (scale -1) * constraints.maxHeight
                val maxX = extWidth/2
                val maxY = extHeight/2
                offset = Offset(
                    x=(offset.x + scale * panChange.x).coerceIn(-maxX,maxX),
                    y=(offset.y + scale * panChange.y).coerceIn(-maxY,maxY)
                )
            }
            AsyncImage(
                model = story.imageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        show = !show
                    }
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .transformable(state)
            )
            AnimatedVisibility(visible = show, enter = fadeIn(), exit = fadeOut()) {
                Row(modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = story.ppurl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)){
                        Text(
                            text = story.username.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatter.format(story.time?.toDate()!!),
                            color = Color.LightGray,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Light)
                        )
                    }

                }
            }
            
        }

    }
}
