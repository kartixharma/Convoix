package com.example.convoix.schedule

import com.example.convoix.Firebase.Reaction
import com.google.firebase.Timestamp
import java.time.LocalDateTime

data class AlarmItem(
    val chatId: String,
    val content: String,
    val senderId: String,
    val time: LocalDateTime
)