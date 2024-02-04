package com.example.convoix

data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)
data class UserData (
    val userId: String = "",
    val username: String? = "",
    val ppurl: String = "",
    val email: String = ""
)
data class ChatData (
    val chatId: String = "",
    val user1: UserData? = null,
    val user2: UserData? = null
)

