package com.example.convoix

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.convoix.Firebase.GoogleAuthUiClient
import com.example.convoix.schedule.AlarmItem
import com.example.convoix.schedule.AndroidAlarmSch
import com.example.convoix.screens.BlockedUsers
import com.example.convoix.screens.Chat
import com.example.convoix.screens.ChatScreen
import com.example.convoix.screens.Customization
import com.example.convoix.screens.ForwardMsg
import com.example.convoix.screens.OtherProfile
import com.example.convoix.screens.ProfileScreen
import com.example.convoix.screens.ScheduledMsgs
import com.example.convoix.screens.Settings
import com.example.convoix.screens.SignInScreen
import com.example.convoix.ui.theme.ConvoixTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()
    val scheduler by lazy {
        AndroidAlarmSch(applicationContext)
    }
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            viewModel = viewModel,
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onPause() {
        super.onPause()
        viewModel.updateStatus(false)
    }
    override fun onResume() {
        super.onResume()
        viewModel.updateStatus(true)
        if(viewModel.state.value.chatId!="")
            viewModel.readAllMessagesInChat(viewModel.state.value.chatId)
    }
    override fun onDestroy() {
        super.onDestroy()
        val alarmItem = AlarmItem(
            chatId = "",
            content = "",
            senderId = viewModel.state.value.userData?.userId.toString(),
            time = LocalDateTime.now().plusSeconds(2)
        )
        alarmItem.let(scheduler::schedule)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        enableEdgeToEdge()
        setContent {
            ConvoixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()
                    NavHost(navController = navController,
                        startDestination = "start") {
                        composable("start") {
                            LaunchedEffect(key1 = Unit) {
                                val userData = googleAuthUiClient.getSignedInUser()
                                if (userData != null) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.updateStatus(true, userData.userId)
                                        viewModel.popStory(userData.userId)
                                        viewModel.getFCMToken(userData.userId)
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                    }
                                        navController.navigate("chats")
                                } else {
                                    navController.navigate("signIn")
                                }
                            }
                        }
                        composable("signIn"){
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if(result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(key1 = state.isSignedIn) {
                                if (state.isSignedIn) {
                                    Toast.makeText(applicationContext, "Signed In Successfully", Toast.LENGTH_SHORT).show()
                                    val userData = googleAuthUiClient.getSignedInUser()
                                    userData?.run {
                                        viewModel.getFCMToken(userData.userId)
                                        viewModel.addUserDataToFirestore(userData)
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                        viewModel.popStory(userData.userId)
                                        viewModel.getFCMToken(userData.userId)
                                        navController.navigate("chats")
                                        viewModel.updateStatus(true)
                                    }
                                }
                            }
                            SignInScreen(state = state,
                                onSignInCLick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("chats"){
                            ChatScreen(navController, viewModel, state, showSingleChat = { usr, id ->
                                    viewModel.getTp(id)
                                    viewModel.readAllMessagesInChat(id)
                                    viewModel.setchatUser(usr, id)
                                    navController.navigate("chat")
                            })
                        }
                        composable("chat", enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}){
                            Chat(navController, viewModel, messages = viewModel.messages, userData = state.User2!!, chatId = state.chatId, state = state, onBack = {
                                navController.popBackStack()
                                viewModel.dePopMsg()
                                viewModel.depopTp()
                                viewModel.dltchatUser()
                            },
                                schedule = { time, content, chatId, senderId ->
                                    val alarmItem = AlarmItem(
                                        chatId = chatId ,
                                        content = content,
                                        time = time,
                                        senderId = senderId
                                    )
                                    alarmItem.let(scheduler::schedule)})
                                }
                        composable("otherprofile") {
                            OtherProfile(state.User2!!, navController)
                        }
                        composable("profile") {
                            ProfileScreen(viewModel = viewModel, state = state, onSignOut = {
                                lifecycleScope.launch {
                                    googleAuthUiClient.signOut()
                                    Toast.makeText(applicationContext, "Signed Out", Toast.LENGTH_SHORT).show()
                                    navController.navigate("signIn")
                                }
                            }, navController)
                        }
                        composable("settings",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}) {
                            Settings(navController, state, viewModel)
                        }
                        composable("cus",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}){
                            Customization(viewModel, state)
                        }
                        composable("blck",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}){
                            viewModel.getBlockedUsers(state.userData?.blockedUsers!!)
                            BlockedUsers(viewModel, state)
                        }
                        composable("forward",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}) {
                            ForwardMsg(navController, viewModel, state)
                        }
                        composable("scheduled",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}) {
                            ScheduledMsgs(navController, viewModel, state, scheduler)
                        }
                    }
                }
            }
        }
    }
}