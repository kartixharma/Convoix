package com.example.convoix


data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val pPUrl: String?,
    val email: String?
)