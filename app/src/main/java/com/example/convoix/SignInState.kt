package com.example.convoix

data class SignInState(
    val isSignedIn: Boolean = false,
    val signInError: String? = null,
    val showAnim: Boolean = false,
    val email: String = "",
    val pass: String = ""
)
