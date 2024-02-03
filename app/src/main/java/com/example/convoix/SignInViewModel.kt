package com.example.convoix

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SignInViewModel: ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult){
        _state.update {
            it.copy(
                isSignedIn = result.data != null,
                signInError = result.errmsg
            )
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun showAnim(){
        _state.update { it.copy( showAnim = true) }
    }

    fun setEmail(email: String){
        _state.update { it.copy( email = email) }
    }

    fun setPass(pass: String){
        _state.update { it.copy( pass = pass) }
    }
}