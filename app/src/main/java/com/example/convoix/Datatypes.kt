package com.example.convoix

import androidx.compose.ui.unit.TextUnit
import com.google.firebase.Timestamp

data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)

data class UserData (
    var userId: String = "",
    var username: String? = "",
    val bio: String? = "",
    val token: String? = "",
    val ppurl: String? = "",
    var email: String = "",
    val pref: Pref = Pref(),
    val blockedUsers: List<String> = emptyList()
)

data class Pref(
    val fontSize: Float = 16f,
    val back: Float = 1f,
    val themes: Int = 1
)


data class ChatData (
    val chatId: String = "",
    val last: Message? = null,
    val user1: ChatUserData? = null,
    val user2: ChatUserData? = null
)



data class Message(
    val msgId: String = "",
    val senderId: String? = "",
    val reaction: String? = "",
    val imgUrl: String? = "",
    val content: String?="",
    val time: Timestamp? = Timestamp.now()
)




data class ChatUserData (
    val userId: String = "",
    val typing:Boolean= false,
    val bio: String = "",
    val username: String? = "",
    val ppurl: String = "",
    val email: String = "",
    val status: Boolean = false
)

data class Story(
    val id: String = "",
    val userId: String = "",
    val username: String? = "",
    val imageUrl: String = "",
    val time: Timestamp? = Timestamp.now(),
    val ppurl: String = "",
    val viewedBy: List<String> = emptyList()
)
