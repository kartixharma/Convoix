package com.example.convoix.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.convoix.ChatViewModel
import com.example.convoix.AppState
import com.example.convoix.Firebase.ScheduledMsg
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.convoix.schedule.AlarmItem
import com.example.convoix.schedule.AndroidAlarmSch
import com.example.convoix.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

@Composable
fun ScheduledMsgs(navController: NavController, viewModel: ChatViewModel, state: AppState, scheduler: AndroidAlarmSch) {
    val scheduledMsgs = state.userData!!.scheduledMsgs
    Image(
        painter = painterResource(id = R.drawable.blurry_gradient_haikei),
        contentDescription = "",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    if(scheduledMsgs.isEmpty()){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 30.dp)) {
            Text(
                text = "No scheduled messages",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Long-press the send button to schedule your messages.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
    if(scheduledMsgs.isNotEmpty()){
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 30.dp)
            ) {
            item {
                Text(
                    text = "Scheduled messages",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                )
            }
            items(scheduledMsgs) { scheduledMsg ->
                ScheduledMsgItem(scheduledMsg = scheduledMsg, cancel = {
                    val instant = Instant.ofEpochSecond(it.time!!.seconds, it.time.nanoseconds.toLong())
                    val alarmItem = AlarmItem(
                        chatId = it.chatId,
                        content = it.content,
                        senderId = it.senderId,
                        time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                    )
                    alarmItem.let(scheduler::cancel)
                    viewModel.cancelScheduledMsg(it.chatId, it.content)
                })
            }
        }
    }

}

@Composable
fun ScheduledMsgItem(scheduledMsg: ScheduledMsg, cancel:(ScheduledMsg)->Unit) {
    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    }
    var expanded by remember {
        mutableStateOf(false)
    }
        Row(
            modifier = Modifier
                .padding(vertical = 6.dp, horizontal = 16.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = scheduledMsg.ppurl,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 10.dp, top = 10.dp)
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "To: " + scheduledMsg.username,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scheduled for: " + formatter.format(scheduledMsg.time?.toDate()!!),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(modifier = Modifier.animateContentSize(),
                    text = "Message: " + scheduledMsg.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if(expanded) 5 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AnimatedVisibility (expanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = { cancel(scheduledMsg) } , modifier = Modifier.fillMaxWidth(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text(text = "Cancel schedule",
                            style = MaterialTheme.typography.titleMedium,)
                    }
                }
            }
        }
}
