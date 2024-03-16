package com.example.convoix

import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.Firebase.UserData

data class AppState(
    val isSignedIn: Boolean = false,
    val signInError: String? = null,
    val showAnim: Boolean = false,
    val showDialog: Boolean = false,
    val srEmail:String = "",
    val userData: UserData? = null,
    val showSingleChat: Boolean = false,
    val chatId: String = "",
    val User2: ChatUserData? = null,
)
