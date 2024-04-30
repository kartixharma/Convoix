package com.example.convoix.Firebase

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.google.firebase.Timestamp
import java.time.LocalDateTime

data class SignInResult (
    val data: UserData?,
    val errmsg: String?
)

data class UserData (
    var userId: String = "",
    var username: String? = "",
    val bio: String? = "",
    val token: String? = "",
    val ppurl: String? = "",
    var email: String = "",
    val pref: Pref = Pref(),
    val blockedUsers: List<String> = emptyList(),
    val scheduledMsgs: List<ScheduledMsg> = emptyList()
)

data class Pref (
    val online: Boolean = true,
    val rr: Boolean = true,
    val fontSize: Float = 16f,
    val back: Float = 1f,
    val doodles: Float = 0.1f,
    val anim: Int = 3,
    val customImg: String = ""
)

data class ScheduledMsg (
    val username: String = "",
    val ppurl: String = "",
    val chatId: String = "",
    val content: String = "",
    val senderId: String = "",
    val time: Timestamp? = Timestamp.now()
)

data class ChatData (
    val chatId: String = "",
    val last: Message? = null,
    val user1: ChatUserData? = null,
    val user2: ChatUserData? = null
)

@Immutable
data class Message (
    val msgId: String = "",
    val senderId: String = "",
    val repliedMsg: Message? = null,
    val reaction: List<Reaction> = emptyList(),
    val imgUrl: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: String = "",
    val vidUrl: String = "",
    val progress: String = "",
    val imgUri: String = "",
    val content: String? = "",
    val time: Timestamp? = null,
    val read: Boolean = false,
    val forwarded: Boolean = false
)

@Immutable
data class Reaction (
    val ppurl: String = "",
    val username: String = "",
    val userId: String = "",
    val reaction: String = ""
)

data class ChatUserData (
    val userId: String = "",
    val typing:Boolean= false,
    val bio: String = "",
    val username: String? = "",
    val ppurl: String = "",
    val email: String = "",
    val status: Boolean = false,
    val unread: Int = 0
)

data class Story (
    val id: String = "",
    val userId: String = "",
    val username: String? = "",
    val images: List<Image> = emptyList(),
    val ppurl: String = "",
)

data class Image (
    val imgUrl: String = "",
    val time: Timestamp? = Timestamp.now(),
    val viewedBy: List<StoryViewer> = emptyList()
)

data class StoryViewer (
    val ppurl: String = "",
    val username: String = "",
    val userId: String = "",
    val reaction: String = "",
    val time: Timestamp? = Timestamp.now()
)
