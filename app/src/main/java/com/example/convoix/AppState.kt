package com.example.convoix

data class AppState(
    val isSignedIn: Boolean = false,
    val signInError: String? = null,
    val showAnim: Boolean = false,
    val email: String = "",
    val pass: String = "",
    val showDialog: Boolean = false,
    val srEmail:String = "",
    val userData: UserData? = null,
    val showSingleChat: Boolean = false
)
