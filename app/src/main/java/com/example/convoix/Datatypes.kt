package com.example.convoix

import com.google.firebase.Timestamp

data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)

data class UserData (
    val userId: String = "",
    val username: String? = "",
    val bio: String = "",
    val token: String = "",
    val ppurl: String = "",
    val email: String = ""
)

data class ChatUserData (
    val userId: String = "",
    val typing:Boolean= false,
    val bio: String = "",
    val username: String? = "",
    val ppurl: String = "",
    val email: String = ""
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
