package com.example.convoix

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.convoix.Firebase.ChatData
import com.example.convoix.Firebase.ChatUserData
import com.example.convoix.Firebase.Image
import com.example.convoix.Firebase.Message
import com.example.convoix.Firebase.Pref
import com.example.convoix.Firebase.ScheduledMsg
import com.example.convoix.Firebase.SignInResult
import com.example.convoix.Firebase.Story
import com.example.convoix.Firebase.StoryViewer
import com.example.convoix.Firebase.UserData
import com.google.common.primitives.Bytes
import com.google.firebase.Timestamp
import com.google.firebase.appcheck.internal.util.Logger.TAG
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.Calendar


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
    var stories by mutableStateOf<List<Story>>(emptyList())
    var list by mutableStateOf<List<UserData>>(emptyList())
    var chatListener: ListenerRegistration? = null
    var userDataListener: ListenerRegistration? = null
    var storyListener: ListenerRegistration? = null
    var forwardMsgs by mutableStateOf(mutableStateListOf<Message>())
    var timePicker by mutableStateOf(false)
    var datePicker by mutableStateOf(false)
    var showSearch by mutableStateOf(false)
    var reply by mutableStateOf("")
    var editMsgId by mutableStateOf("")
    var replyMessage by mutableStateOf(Message())
    var editMsgContent by mutableStateOf("")
    var imgUri by mutableStateOf<Uri?>(null)
    var vidUri by mutableStateOf<Uri?>(null)
    var fileUri by mutableStateOf<Uri?>(null)
    var showOptions by mutableStateOf(false)
    var bitmap by mutableStateOf<Bitmap?>(null)
    var selectionMode by mutableStateOf(false)
    var showDialog by mutableStateOf(false)
    var clearChatDialog by mutableStateOf(false)
    var msg by mutableStateOf(Message())
    val brush = Brush.linearGradient(listOf(
        Color(0xFF238CDD),
        Color(0xFF1952C4)
    ))
    var isModalBottomSheetVisible by mutableStateOf(false)
    var selectedEmoji by mutableStateOf("")
    var searchText by mutableStateOf("")
    var searchText1 by mutableStateOf("")
    companion object {
        private var instance: ChatViewModel? = null
        fun getInstance(): ChatViewModel {
            if (instance == null) {
                instance = ChatViewModel()
            }
            return instance!!
        }
    }
    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                isSignedIn = result.data != null,
                signInError = result.errmsg
            )
        }
    }
    fun uploadStory(url: String, storyId: String) {
        val image = Image(
            imgUrl = url,
            time = Timestamp(Calendar.getInstance().time)
        )
        if(storyId.isNotBlank()){
            firestore.collection("stories").document(storyId).update("images", FieldValue.arrayUnion(image))

        }
        else {
            val id = firestore.collection("stories").document().id
            val story = Story(
                id = id,
                userId = state.value.userData?.userId.toString(),
                username = state.value.userData?.username,
                images = listOf(image),
                ppurl = state.value.userData?.ppurl.toString(),
            )
            firestore.collection("stories").document(id).set(story)
        }
    }
    fun viewStory(id: String, index: Int) {
        firestore.collection("stories").document(id).get()
            .addOnSuccessListener {
                val existingImages = it.toObject<Story>()?.images ?: emptyList()
                val imageAtIndex = existingImages[index]
                val viewedImage = imageAtIndex.copy(viewedBy =
                if (imageAtIndex.viewedBy.any { it.userId== state.value.userData?.userId.toString() }){
                    imageAtIndex.viewedBy
                }
                    else {
                    imageAtIndex.viewedBy.plusElement(
                        StoryViewer(
                            ppurl = state.value.userData?.ppurl.toString(),
                            username = state.value.userData?.username.toString(),
                            userId = state.value.userData?.userId.toString(),
                            time = Timestamp(Calendar.getInstance().time)
                        )
                    )
                }
                    )
                val updatedImages = existingImages.map {
                    if (it == imageAtIndex) viewedImage else it
                }
                firestore.collection("stories").document(id).update("images", updatedImages)
            }
    }
    fun storyReaction(id: String, index: Int, reaction: String, userId: String) {
        firestore.collection("stories").document(id).get()
            .addOnSuccessListener {
                val existingImages = it.toObject<Story>()?.images ?: emptyList()
                val imageAtIndex = existingImages[index]
                val viewedByUpdated = imageAtIndex.viewedBy.find { it.userId == state.value.userData?.userId.toString() }!!.copy(reaction = reaction)
                val reactedImage = imageAtIndex.copy(viewedBy = imageAtIndex.viewedBy.filter { it.userId!=state.value.userData?.userId.toString() }.plusElement(viewedByUpdated) )
                val updatedImages = existingImages.map {
                    if (it == imageAtIndex) reactedImage else it
                }
                firestore.collection("stories").document(id).update("images", updatedImages)
            }
        sendNotification("Reacted "+reaction+" to your story", userId)
    }
    fun popStory(currentUserId: String) {
        viewModelScope.launch {
            val storyCol = firestore.collection("stories")
            val users = arrayListOf(state.value.userData?.userId)
            firestore.collection("chats").where(
                Filter.or(
                    Filter.equalTo("user1.userId", currentUserId),
                    Filter.equalTo("user2.userId", currentUserId)
                )
            ).addSnapshotListener { chatSnapshot, chatError ->
                if (chatSnapshot != null) {
                    chatSnapshot.toObjects<ChatData>().forEach { chat ->
                        val otherUserId = if (chat.user1?.userId == currentUserId) {
                            chat.user2?.userId.toString()
                        } else {
                            chat.user1?.userId.toString()
                        }
                        users.add(otherUserId)
                    }
                    users.add(currentUserId)
                    storyListener = storyCol
                        .whereIn("userId", users)
                        .addSnapshotListener { storySnapshot, storyError ->
                            if (storySnapshot != null) {
                                stories = storySnapshot.documents.mapNotNull { document ->
                                    document.toObject<Story>()
                                }
                            }
                        }
                }
            }
        }

    }
    fun getTp(chatId: String) {
        tpListener?.remove()
        viewModelScope.launch(Dispatchers.IO) {
            tpListener = firestore.collection("chats").document(chatId).addSnapshotListener { snp, err->
                if (snp != null) {
                    tp = snp.toObject(ChatData::class.java)!!
                }
            }
        }
    }
    fun depopTp(){
        tpListener?.remove()
    }
    fun popMessage(chatId: String) {
        msgListener?.remove()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (chatId != "") {
                    msgListener = firestore.collection("chats").document(chatId).collection("message")
                        .addSnapshotListener { value, error ->
                            if (value != null) {
                                messages = value.documents.mapNotNull {
                                    it.toObject(Message::class.java)
                                }.sortedBy { it.time }.reversed()
                            }
                        }
                }
            }
        }
    }
    fun dePopMsg(){
        messages = listOf()
        msgListener?.remove()
    }

    fun showChats(userId: String) {
            chatListener = firestore.collection("chats").where(
                Filter.or(
                    Filter.equalTo("user1.userId", userId),
                    Filter.equalTo("user2.userId", userId)
                )
            ).addSnapshotListener { value, err ->
                if(value!=null){
                    chats = value.documents.mapNotNull {
                        it.toObject<ChatData>()
                    }.sortedBy { it.last?.time }.reversed()
                }
            }
    }
    fun findChatId(user2Id: String, callback: (String) -> Unit) {
        firestore.collection("chats").where(
            Filter.and(
            Filter.or(
                Filter.equalTo("user1.userId", state.value.userData?.userId),
                Filter.equalTo("user2.userId", state.value.userData?.userId)
            ),
                Filter.or(
                    Filter.equalTo("user1.userId", user2Id),
                    Filter.equalTo("user2.userId", user2Id)
                )
        )).get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents.first()
                val chatId = document.getString("chatId")
                callback(chatId.toString())
            }
        }
    }
    fun UploadImage(img: ByteArray = ByteArray(0), msgId: String = "", chatId: String = "", imgUri: Uri = Uri.EMPTY, callback: (String) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}")
        if (imgUri.path != "") {
            imageRef.putFile(imgUri)
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
                .addOnProgressListener {
                    if (msgId != "" && it.totalByteCount / (1024 * 1024) > 0) {
                        updateProgress(
                            chatId,
                            msgId,
                            (it.bytesTransferred / (1024 * 1024)).toString() + " MB / " + (it.totalByteCount / (1024 * 1024)).toString() + " MB"
                        )
                    } else if (msgId != "") {
                        updateProgress(
                            chatId,
                            msgId,
                            (it.bytesTransferred / (1024)).toString() + " KB / " + (it.totalByteCount / (1024)).toString() + " KB"
                        )
                    }
                }
        } else {
            imageRef.putBytes(img)
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
                .addOnProgressListener {
                    if (msgId != "" && it.totalByteCount / (1024 * 1024) > 0) {
                        updateProgress(
                            chatId,
                            msgId,
                            (it.bytesTransferred / (1024 * 1024)).toString() + " MB / " + (it.totalByteCount / (1024 * 1024)).toString() + " MB"
                        )
                    } else if (msgId != "") {
                        updateProgress(
                            chatId,
                            msgId,
                            (it.bytesTransferred / (1024)).toString() + " KB / " + (it.totalByteCount / (1024)).toString() + " KB"
                        )
                    }
                }
        }
    }
    fun uploadVideo(msgId: String = "", chatId: String = "", vidUri: Uri, callback: (String) -> Unit) {
        val storageRef = storage.reference
        val videoRef = storageRef.child("videos/${System.currentTimeMillis()}")
        videoRef.putFile(vidUri)
            .addOnSuccessListener {
                videoRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()
                        callback(url)
                    }
                    .addOnFailureListener {
                        callback("")
                    }
            }
            .addOnProgressListener {
                if (msgId != "" && it.totalByteCount / (1024 * 1024) > 0) {
                    updateProgress(
                        chatId,
                        msgId,
                        (it.bytesTransferred / (1024 * 1024)).toString() + " MB / " + (it.totalByteCount / (1024 * 1024)).toString() + " MB"
                    )
                } else if (msgId != "") {
                    updateProgress(
                        chatId,
                        msgId,
                        (it.bytesTransferred / (1024)).toString() + " KB / " + (it.totalByteCount / (1024)).toString() + " KB"
                    )
                }
            }
    }
    fun uploadFile(msgId: String = "", chatId: String = "", fileUri: Uri, callback: (String) -> Unit) {
        val storageRef = storage.reference
        val fileRef = storageRef.child("files/${System.currentTimeMillis()}")
        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                fileRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()
                        callback(url)
                    }
                    .addOnFailureListener {
                        callback("")
                    }
            }
            .addOnProgressListener {
                if (msgId != "" && it.totalByteCount / (1024 * 1024) > 0) {
                    updateProgress(
                        chatId,
                        msgId,
                        (it.bytesTransferred / (1024 * 1024)).toString() + " MB / " + (it.totalByteCount / (1024 * 1024)).toString() + " MB"
                    )
                } else if (msgId != "") {
                    updateProgress(
                        chatId,
                        msgId,
                        (it.bytesTransferred / (1024)).toString() + " KB / " + (it.totalByteCount / (1024)).toString() + " KB"
                    )
                }
            }
    }
    fun updateProgress(
        chatId: String,
        msgId: String,
        process: String
    ){
        firestore.collection("chats").document(chatId).collection("message").document(msgId).update("progress", process)
    }
    fun UploadImage1(img: Uri, callback: (String) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}")
        imageRef.putFile(img)
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
            .addOnCompleteListener {
            }
    }
    fun sendReply(chatId: String,
                  msg: String,
                  imgUrl: String = "",
                  vidUrl: String = "",
                  replyMsg: Message = Message(),
                  fileUrl: String = "",
                  fileName: String = "",
                  fileSize: String = "",
                  senderId: String = state.value.userData?.userId.toString(),
                  imgUri: String = ""
    ): String {
        val id = firestore.collection("chats").document().collection("message").document().id
        val time = Calendar.getInstance().time
        val message = Message(
            msgId = id,
            imgUrl = imgUrl,
            imgUri = imgUri,
            vidUrl = vidUrl,
            repliedMsg = replyMsg,
            fileUrl = fileUrl,
            fileName = fileName,
            fileSize = fileSize,
            senderId = senderId,
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
        firestore.collection("chats").document(chatId).get()
            .addOnSuccessListener { querySnapshot ->
                    val user1Id = querySnapshot.getString("user1.userId")
                    val user2Id = querySnapshot.getString("user2.userId")

                    if (user1Id == senderId) {
                        querySnapshot.reference.update("user1.unread", FieldValue.increment(1))
                    }
                    if (user2Id == senderId) {
                        querySnapshot.reference.update("user2.unread", FieldValue.increment(1))                    }

            }
        if(imgUrl!="uploadingImage" && vidUrl!="uploadingVideo")
            sendNotification(msg)
        return id
    }
    fun addUrl(
        vidUrl: String = "",
        imgUrl: String = "",
        fileUrl: String = "",
        chatId: String,
        msgId: String
    ){
        if(fileUrl!=""){
            firestore.collection("chats").document(chatId)
                .collection("message").document(msgId).update("fileUrl", fileUrl)
            sendNotification("sent a File")
        }
        if(vidUrl!=""){
            firestore.collection("chats").document(chatId)
                .collection("message").document(msgId).update("vidUrl", vidUrl)
            sendNotification("sent a Video")
        }else{
            firestore.collection("chats").document(chatId)
                .collection("message").document(msgId).update("imgUrl",imgUrl)
            sendNotification("sent an Image")
        }
    }
    fun forwardMsg(chatIds: List<String>, senderId: String = state.value.userData?.userId.toString()){
            for(chatId in chatIds){
                for(msg in forwardMsgs){
                    val id = firestore.collection("chats").document().collection("message").document().id
                    val time = Calendar.getInstance().time
                    val message = msg.copy(time = Timestamp(time), forwarded = true, msgId = id, senderId = senderId)
                    firestore.collection("chats").document(chatId).collection("message").document(id).set(message)
                    firestore.collection("chats").document(chatId).update("last", message)
                        .addOnSuccessListener {
                            Log.d("Firestore Update", "Last message updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore Update", "Error updating last message", e)
                        }
                    firestore.collection("chats").document(chatId).get()
                        .addOnSuccessListener { querySnapshot ->
                            val user1Id = querySnapshot.getString("user1.userId")
                            val user2Id = querySnapshot.getString("user2.userId")

                            if (user1Id == senderId) {
                                querySnapshot.reference.update("user1.unread", FieldValue.increment(1))
                            }
                            if (user2Id == senderId) {
                                querySnapshot.reference.update("user2.unread", FieldValue.increment(1))                    }

                        }
                    sendNotification("*Forwarded*\n"+ message.content)
                }
            }
        forwardMsgs.clear()
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
                usersCollection.whereEqualTo("email", email).get().addOnSuccessListener {
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
            "email" to userData.email,
            "pref" to userData.pref
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
        userDocument.update("username", userData.username, "bio", userData.bio, "ppurl", userData.ppurl)
        firestore.collection("chats").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val user1Id = document.getString("user1.userId")
                    val user2Id = document.getString("user2.userId")

                    if (user1Id == userData.userId) {
                        document.reference.update("user1.username", userData.username, "user1.bio", userData.bio, "user1.ppurl", userData.ppurl)
                    }
                    if (user2Id == userData.userId) {
                        document.reference.update("user2.username", userData.username, "user2.bio", userData.bio, "user2.ppurl", userData.ppurl)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error getting chats collection", e)
            }
        firestore.collection("stories").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val userId = document.getString("userId")
                    if (userId == userData.userId) {
                        document.reference.update("ppurl", userData.ppurl, "username", userData.username)
                    }
                }
            }
    }
    fun updatePref(userData: UserData, pref: Pref) {
        val userDocument = usersCollection.document(userData.userId)
        userDocument.update("pref", pref)
    }
    fun addScheduledMsg(scheduledMsg: ScheduledMsg) {
        usersCollection.document(state.value.userData?.userId.toString())
            .update("scheduledMsgs", FieldValue.arrayUnion(scheduledMsg))
    }
    fun cancelScheduledMsg(chatId: String, content: String, userId: String = state.value.userData?.userId.toString()) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val scheduledMsgs = documentSnapshot.get("scheduledMsgs") as? List<HashMap<String, Any>>?
                if (scheduledMsgs != null && scheduledMsgs.isNotEmpty()) {
                    val indexToRemove = scheduledMsgs.indexOfFirst { msg ->
                        msg["chatId"] == chatId && msg["content"] == content
                    }
                    if (indexToRemove != -1) {
                        val updatedScheduledMsgs = scheduledMsgs.toMutableList()
                        updatedScheduledMsgs.removeAt(indexToRemove)
                        usersCollection.document(userId)
                            .update("scheduledMsgs", updatedScheduledMsgs)
                    } else {
                        Log.d(TAG, "No matching scheduled message found")
                    }
                } else {
                    Log.d(TAG, "No scheduled messages found for user $userId")
                }
            }
    }

    fun getUserData(userId: String){
        userDataListener = usersCollection.document(userId).addSnapshotListener { value, error ->
            if(value!=null){
                _state.update { it.copy(userData = value.toObject(UserData::class.java)) }
            }
        }
    }
    fun getBlockedUsers(userIds: List<String>) {
        if(userIds.isNotEmpty()){
            firestore.collection("users").whereIn("userId", userIds)
                .addSnapshotListener{ value, err ->
                    if(value!=null){
                        list = value.documents.mapNotNull {
                            it.toObject<UserData>()
                        }
                    }
                }
        }
        else
            list= emptyList()

    }

    fun deleteChat(chatIds: List<String>) {
        for (id in chatIds){
            firestore.collection("chats").document(id).delete()
            firestore.collection("chats").document(id).collection("message").get()
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

    }

    fun deleteStory(story: Story, number: Int){
        if (story.images.size==1){
            firestore.collection("stories").document(story.id).delete()
        }
        else{
           val newStory =  story.images.filter { story.images[number]!=it }
            firestore.collection("stories").document(story.id).update("images", newStory)
        }
    }

    fun Reaction(str: String, chatId: String, msgId: String) {
        val reaction = com.example.convoix.Firebase.Reaction(
            ppurl = state.value.userData?.ppurl.toString(),
            username = state.value.userData?.username.toString(),
            userId = state.value.userData?.userId.toString(),
            reaction = str
        )
        firestore.collection("chats")
            .document(chatId).collection("message").document(msgId).get()
            .addOnSuccessListener { messageSnapshot ->
                val existingReactions = messageSnapshot.toObject<Message>()?.reaction ?: emptyList()
                val existingUserReaction = existingReactions.find { it.userId == state.value.userData?.userId }
                if (existingUserReaction == null) {
                    firestore.collection("chats").document(chatId).collection("message").document(msgId)
                        .update("reaction", FieldValue.arrayUnion(reaction))
                    sendNotification("Reacted "+str+" to your message")
                } else {
                    val updatedReactions = existingReactions.map {
                        if (it.userId == state.value.userData?.userId) reaction else it
                    }
                    firestore.collection("chats").document(chatId).collection("message").document(msgId)
                        .update("reaction", updatedReactions)
                }
            }
    }

    fun removeReaction(chatId: String, msgId: String) {
        firestore.collection("chats")
            .document(chatId)
            .collection("message")
            .document(msgId)
            .get()
            .addOnSuccessListener { messageSnapshot ->
                val existingReactions = messageSnapshot.toObject<Message>()?.reaction ?: emptyList()
                val updatedReactions = existingReactions.filter { it.userId != state.value.userData?.userId }
                firestore.collection("chats")
                    .document(chatId)
                    .collection("message")
                    .document(msgId)
                    .update("reaction", updatedReactions)
            }
    }
    suspend fun typing(t: Boolean, chatId: String, userId: String) {
        if(chatId!=""){
            val chatRef = firestore.collection("chats").document(chatId)
            val documentSnapshot = chatRef.get().await()
            val field = when {
                userId == documentSnapshot.getString("user1.userId") -> "user1.typing"
                userId == documentSnapshot.getString("user2.userId") -> "user2.typing"
                else -> return
            }
            chatRef.update(field, t)
        }

    }
    fun readAllMessagesInChat(chatId: String) {
        val messagesCollection = firestore.collection("chats").document(chatId).collection("message")
        viewModelScope.launch(Dispatchers.IO) {
            val userDataRef = firestore.collection("users").document(state.value.User2?.userId.toString())
            val snapshot = userDataRef.get().await()
            val userData = snapshot.toObject(UserData::class.java)
            val rr = userData?.pref?.rr ?: true
            if(state.value.userData?.pref?.rr!! || rr){
                messagesCollection
                    .whereNotEqualTo("senderId", state.value.userData?.userId)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            val messageId = document.id
                            messagesCollection.document(messageId).update("read", true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting messages for chat $chatId: ", exception)
                    }
                firestore.collection("chats").document(chatId).get()
                    .addOnSuccessListener { querySnapshot ->
                        val userId = querySnapshot.getString("last.senderId")
                        if (userId != state.value.userData?.userId) {
                            querySnapshot.reference.update("last.read", true)
                        }
                    }
            }
            firestore.collection("chats").document(chatId).get()
                .addOnSuccessListener { querySnapshot ->
                    val user1Id = querySnapshot.getString("user1.userId")
                    val user2Id = querySnapshot.getString("user2.userId")

                    if (user1Id == state.value.userData?.userId) {
                        querySnapshot.reference.update("user2.unread", 0)
                    }
                    if (user2Id == state.value.userData?.userId) {
                        querySnapshot.reference.update("user1.unread", 0)
                    }
                }

        }

    }
    fun deleteMsg(msgIds: List<String>, chatId: String) {
        for (id in msgIds) {
            firestore.collection("chats").document(chatId).collection("message")
                .document(id).get()
                .addOnSuccessListener { documentSnapshot ->
                    val isRead = documentSnapshot.getBoolean("read") ?: false
                    if (isRead) {
                        firestore.collection("chats").document(chatId).collection("message").document(id).delete()
                    } else {
                        firestore.collection("chats").document(chatId).collection("message").document(id).delete()
                        firestore.collection("chats").document(chatId).get()
                            .addOnSuccessListener { querySnapshot ->
                                val user1Id = querySnapshot.getString("user1.userId")
                                val user2Id = querySnapshot.getString("user2.userId")

                                if (user1Id == state.value.userData?.userId) {
                                    querySnapshot.reference.update("user1.unread", FieldValue.increment(-1))
                                }
                                if (user2Id == state.value.userData?.userId) {
                                    querySnapshot.reference.update("user2.unread", FieldValue.increment(-1))                    }

                            }
                    }
                }
            if(id==messages[0].msgId){
                if(messages.size==1){
                    firestore.collection("chats").document(chatId).update("last", Message(time = null))
                }
                else
                    firestore.collection("chats").document(chatId).update("last", messages[1])
                }
        }
    }
    fun editMessage(msgId: String, chatId: String, newMsg: String){
            firestore.collection("chats").document(chatId).collection("message").document(msgId).update("content", newMsg)
    }
    fun clearChat(chatId: String){
        firestore.collection("chats").document(chatId).collection("message").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                }
            }
        firestore.collection("chats").document(chatId).update("last", Message(time = null))
    }
    fun blockUser(userId: String){
        usersCollection.document(state.value.userData?.userId.toString()).update("blockedUsers", FieldValue.arrayUnion(userId))
    }
    fun unblockUser(userId: String){
        usersCollection.document(state.value.userData?.userId.toString()).update("blockedUsers", FieldValue.arrayRemove(userId))
    }
    fun isBlocked(userId: String, callback: (Boolean) -> Unit) {
        usersCollection.document(userId).addSnapshotListener { value, error ->
            if (value != null) {
                val isBlocked = value.toObject(UserData::class.java)?.blockedUsers
                    ?.contains(state.value.userData?.userId.toString()) ?: false
                callback(isBlocked)
            }
        }
    }
    fun isBLockedByMe(userId: String): Boolean {
        return state.value.userData?.blockedUsers?.contains(userId)!!
    }
    fun getFCMToken(userId: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isSuccessful){
                val tkn = it.result
                firestore.collection("users").document(userId).update("token", tkn)
            }
        }
    }
    fun removeFCMToken(userId: String){
        firestore.collection("users").document(userId).update("token", "")
    }
    fun sendNotification(msg: String, userId: String=""){
        val user2Id = if(userId!="") userId else state.value.User2?.userId.toString()
        usersCollection.document(user2Id).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val otherUsrToken = documentSnapshot.getString("token").toString()
                    val jsonObj = JSONObject()
                    val notObj = JSONObject()
                    notObj.put("title", state.value.userData?.username)
                    notObj.put("body", msg)
                    val dataObj = JSONObject()
                    dataObj.put("userId", state.value.userData?.userId)
                    jsonObj.put("notification", notObj)
                    jsonObj.put("data", dataObj)
                    jsonObj.put("to", otherUsrToken)
                    callApi(jsonObj)
                } else {
                    println("Document does not exist")
                }
            }
    }
    fun callApi(jsonObj: JSONObject) {
        val json = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val body = jsonObj.toString().toRequestBody(json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header(
                "Authorization",
                "[]"
            )
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("Response: $responseBody")
            }
        })
    }

    fun updateStatus(status: Boolean, userId: String = state.value.userData?.userId.toString(), v: Boolean = false){
            firestore.collection("chats").get()
                .addOnSuccessListener { querySnapshot ->
                    if(state.value.userData?.pref?.online ?: true || v){
                            for (document in querySnapshot.documents) {
                                val user1Id = document.getString("user1.userId")
                                val user2Id = document.getString("user2.userId")
                                if (user1Id == userId) {
                                    document.reference.update("user1.status", status)
                                }
                                if (user2Id == userId) {
                                    document.reference.update("user2.status", status)
                                }
                            }
                    }

                }
    }

    fun resetState() {
        _state.update { AppState() }
        stories = emptyList()
        chats = emptyList()
    }
    fun removeL() {
        storyListener?.remove()
        chatListener?.remove()
        userDataListener?.remove()
    }
    fun showDialog(){
        _state.update { it.copy( showDialog=true) }
    }
    fun hideDialog(){
        _state.update { it.copy( showDialog=false) }
    }
    fun setchatUser(usr: ChatUserData, id: String) {
        _state.update { it.copy( User2 = usr, chatId = id ) }
    }
    fun dltchatUser() {
        _state.update { it.copy(chatId = "" ) }
    }
    fun setSrEmail(email: String){
        _state.update { it.copy(srEmail = email ) }
    }
}
