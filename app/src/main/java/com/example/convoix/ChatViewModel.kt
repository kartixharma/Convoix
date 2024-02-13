package com.example.convoix

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID


class ChatViewModel: ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()
    private val usersCollection = firestore.collection("users")
    var chats by mutableStateOf<List<ChatData>>(emptyList())
    var messages by mutableStateOf<List<Message>>(listOf())
    var msgListener: ListenerRegistration? = null
    var tp by mutableStateOf(ChatData())
    var tpListener: ListenerRegistration?=null
    val storage = FirebaseStorage.getInstance()

    fun onSignInResult(result: SignInResult){
        _state.update {
            it.copy(
                isSignedIn = result.data != null,
                signInError = result.errmsg
            )
        }
    }

    fun getTp(chatId: String) {
        tpListener?.remove()
        tpListener = firestore.collection("chats").document(chatId).addSnapshotListener{ snp, err->
            if (snp != null) {
                tp = snp.toObject(ChatData::class.java)!!
            }
        }
    }
    fun depopTp(){
        tpListener?.remove()
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

    fun showChats(userId: String){
        firestore.collection("chats").where(
            Filter.or(
                Filter.equalTo("user1.userId", userId),
                Filter.equalTo("user2.userId", userId)
            )
        ).addSnapshotListener{ value, err ->
            if(value!=null){
                    chats = value.documents.mapNotNull {
                        it.toObject<ChatData>()
                    }
                }
        }
    }

    fun UploadImage(imgUri: Uri?, callback: (String) -> Unit) {
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        viewModelScope.launch(Dispatchers.IO) {
            imageRef.putFile(imgUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            val url = downloadUri.toString()
                            callback(url)
                        }
                        .addOnFailureListener {
                            callback("")
                        }
                }
                .addOnFailureListener {
                    callback("")
                }
        }

    }


    fun sendReply(chatId: String, msg: String, imgUrl: String =""){
        val id = firestore.collection("chats").document().collection("message").document().id
        val time = Calendar.getInstance().time
        val message = Message(
            reaction = "",
            msgId = id,
            imgUrl = imgUrl,
            senderId = state.value.userData?.userId.toString(),
            content = msg,
            time = Timestamp(time)
        )
        firestore.collection("chats").document(chatId).collection("message").document(id).set(message)
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
        )).get().addOnSuccessListener {
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
                            ChatUserData(
                                bio = state.value.userData?.bio.toString(),
                                typing = false,
                                userId = state.value.userData?.userId.toString(),
                                username = state.value.userData?.username,
                                ppurl = state.value.userData?.ppurl.toString(),
                                email = state.value.userData?.email.toString()
                            ),
                            ChatUserData(
                                bio = chatPartner?.bio.toString(),
                                typing = false,
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
        userDocument.get().addOnSuccessListener {
            if(it.exists()){

            }
            else{
                userDocument.set(userDataMap)
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "User data added to Firestore successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error adding user data to Firestore", e)
                    }
            }
        }
    }

    fun updateProfile(userData: UserData) {
        val userDocument = usersCollection.document(userData.userId)
        userDocument.update("username", userData.username, "bio", userData.bio)
        firestore.collection("chats").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val user1Id = document.getString("user1.userId")
                    val user2Id = document.getString("user2.userId")

                    if (user1Id == userData.userId) {
                        document.reference.update("user1.username", userData.username, "user1.bio", userData.bio)
                    }
                    if (user2Id == userData.userId) {
                        document.reference.update("user2.username", userData.username, "user2.bio", userData.bio)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error getting chats collection", e)
            }
    }

   fun getUserData(userId: String){
        usersCollection.document(userId).addSnapshotListener { value, error ->
            if(value!=null){
                _state.update { it.copy(userData = value.toObject(UserData::class.java)) }
            }
        }
    }

    fun deleteChat(chatId: String) {
        firestore.collection("chats").document(chatId).delete()
        firestore.collection("chats").document(chatId).collection("message").get()
            .addOnSuccessListener { querySnapshot ->
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
    fun Reaction(str: String, chatId: String, msgId: String){
        firestore.collection("chats").document(chatId).collection("message").document(msgId).update("reaction", str)
    }
    suspend fun typing(t: Boolean, chatId: String, userId: String) {
        val chatRef = firestore.collection("chats").document(chatId)
        val documentSnapshot = chatRef.get().await()
        val fieldToUpdate = when {
            userId == documentSnapshot.getString("user1.userId") -> "user1.typing"
            userId == documentSnapshot.getString("user2.userId") -> "user2.typing"
            else -> return
        }

        chatRef.update(fieldToUpdate, t)
    }
    fun deleteMsg(msgIds: List<String>, chatId: String){
        for (id in msgIds){
            firestore.collection("chats").document(chatId).collection("message").document(id).delete()
        }
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

    fun setchatUser(usr: ChatUserData, id: String) {
        _state.update { it.copy( User2 = usr, chatId = id ) }
    }
}