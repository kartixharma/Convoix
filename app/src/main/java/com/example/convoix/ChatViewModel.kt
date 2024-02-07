package com.example.convoix

import android.content.ContentValues
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar


class ChatViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val usersCollection = firestore.collection("users")
    var chats by mutableStateOf<List<ChatData>>(emptyList())
    var messages by mutableStateOf<List<Message>>(listOf())
    var msgListener: ListenerRegistration? = null

    fun setUserData(userData: UserData){
        _state.update { it.copy(userData=userData) }
    }
    fun onSignInResult(result: SignInResult){
        _state.update {
            it.copy(
                isSignedIn = result.data != null,
                signInError = result.errmsg
            )
        }
    }
    fun popMessage(chatId: String) {
        msgListener?.remove()
        msgListener = firestore.collection("chats").document(chatId).collection("message")
            .addSnapshotListener { value, error ->
                if (value!=null){
                    messages = value.documents.mapNotNull {
                        it.toObject(Message::class.java)
                    }.sortedBy { it.time }.reversed()
            }
        }
    }
    fun dePopMsg(){
        messages = listOf()
        msgListener?.remove()
    }
    fun showChats(){
        firestore.collection("chats").where(
            Filter.or(
                Filter.equalTo("user1.userId", state.value.userData?.userId),
                Filter.equalTo("user2.userId", state.value.userData?.userId)
            )
        ).addSnapshotListener{ value, err ->
            if(value!=null){
                    chats = value.documents.mapNotNull {
                        it.toObject<ChatData>()
                    }
                }
        }
    }
    fun sendReply(chatId: String, msg: String){
        val time = Calendar.getInstance().time
        val message = Message(
            senderId = state.value.userData?.userId.toString(),
            content = msg,
            time = Timestamp(time)
        )
        firestore.collection("chats").document(chatId).collection("message").document().set(message)
        firestore.collection("chats").document(chatId).update("last", message)
            .addOnSuccessListener {
                Log.d("Firestore Update", "Last message updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore Update", "Error updating last message", e)
            }
    }

    fun addChat(email: String){
        firestore.collection("chats").where(Filter.or(
            Filter.and(
                Filter.equalTo("user1.email", email),
                Filter.equalTo("user2.email", state.value.userData?.email)
            ),
            Filter.and(
                Filter.equalTo("user1.email", state.value.userData?.email),
                Filter.equalTo("user2.email", email)
            )
        )).get().addOnSuccessListener{
            if(it.isEmpty){
                usersCollection.whereEqualTo("email", email).get().addOnSuccessListener{
                    if(it.isEmpty){
                        println("failed")
                    }
                    else{
                        val chatPartner = it.toObjects(UserData::class.java).firstOrNull()
                        val id = firestore.collection("chats").document().id
                        val chat = ChatData(
                            chatId = id,
                            Message(
                                senderId = "",
                                content = "",
                                time = null
                            ),
                            UserData(
                                userId = state.value.userData?.userId.toString(),
                                username = state.value.userData?.username,
                                ppurl = state.value.userData?.ppurl.toString(),
                                email = state.value.userData?.email.toString()
                            ),
                            UserData(
                                userId = chatPartner?.userId.toString(),
                                username = chatPartner?.username,
                                ppurl = chatPartner?.ppurl.toString(),
                                email = chatPartner?.email.toString()
                            )
                        )
                        firestore.collection("chats").document(id).set(chat)
                    }
                }.addOnFailureListener {
                    println("failed..")
                }
            }
            else {

            }
        }
    }
    fun addUserDataToFirestore(userData: UserData) {
        val userDocument = usersCollection.document(userData.userId)
        val userDataMap = mapOf(
            "userId" to userData.userId,
            "username" to userData.username,
            "ppurl" to userData.ppurl,
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

    fun deleteChat(chatId: String) {
        firestore.collection("chats").document(chatId).delete()
        firestore.collection("chats").document(chatId).collection("message").get()
            .addOnSuccessListener { querySnapshot ->
                // Iterate through all documents and delete each one
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            println("Document deleted successfully.")
                        }
                        .addOnFailureListener { e ->

                            println("Error deleting document: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Error getting documents: $e")
            }
    }

    fun showSingleChat(){
        _state.update { it.copy(showSingleChat = true) }
    }

    fun resetState() {
        _state.update { AppState() }
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

    fun showDialog(){
        _state.update { it.copy( showDialog=true) }
    }
    fun hideDialog(){
        _state.update { it.copy( showDialog=false) }
    }
    fun setSrEmail(email: String){
        _state.update { it.copy( srEmail = email) }
    }

    fun setchatUser(usr: UserData, id: String) {
        _state.update { it.copy( User2 = usr, chatId = id ) }
    }

}