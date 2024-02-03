package com.example.convoix

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SignInViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
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
    fun addUserDataToFirestore(userData: UserData) {
        val userCollection = firestore.collection("users")
        val userDocument = userCollection.document(userData.userId)

        val userDataMap = mapOf(
            "userId" to userData.userId,
            "username" to userData.username,
            "pPUrl" to userData.pPUrl,
            "email" to userData.email
        )

        userDocument.set(userDataMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "User data added to Firestore successfully")
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error adding user data to Firestore", e)
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