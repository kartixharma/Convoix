package com.example.convoix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.convoix.screens.Chat
import com.example.convoix.screens.ChatScreen
import com.example.convoix.screens.Customization
import com.example.convoix.screens.OtherProfile
import com.example.convoix.screens.ProfileScreen
import com.example.convoix.screens.Settings
import com.example.convoix.screens.SignInScreen1
import com.example.convoix.ui.theme.ConvoixTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private val viewModel: ChatViewModel by viewModels()
    override fun onPause() {
        super.onPause()
        viewModel.updateStatus(false)
    }
    override fun onResume() {
        super.onResume()
        viewModel.updateStatus(true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDark by rememberSaveable {
                mutableStateOf(true)
            }
            ConvoixTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()
                    NavHost(navController = navController,
                        startDestination = "start"){
                        composable("start") {
                            LaunchedEffect(key1 = Unit) {
                                val userData = googleAuthUiClient.getSignedInUser()
                                if (userData != null) {
                                    viewModel.getFCMToken(userData.userId)
                                    viewModel.getUserData(userData.userId)
                                    viewModel.showChats(userData.userId)
                                    viewModel.updateStatus(true)
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
                                    userData?.let {
                                        viewModel.getFCMToken(userData.userId)
                                        viewModel.addUserDataToFirestore(it)
                                        viewModel.getUserData(userData.userId)
                                        viewModel.showChats(userData.userId)
                                    }
                                    viewModel.showAnim()
                                    viewModel.getFCMToken(userData?.userId.toString())
                                    delay(1500)
                                    navController.navigate("chats")
                                    viewModel.resetState()
                                    viewModel.getUserData(userData?.userId.toString())
                                }
                            }
                            SignInScreen1(state = state,
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
                                viewModel.setchatUser(usr, id)
                                viewModel.popMessage(id)
                                viewModel.getTp(id)
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
                            Chat(navController, viewModel, viewModel.messages, state.User2!!, state.chatId, state, onBack = {
                                navController.popBackStack()
                                viewModel.dePopMsg()
                                viewModel.depopTp()
                            })
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
                            },navController)

                        }
                        composable("settings",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}){
                            Settings(navController, changeTheme = {isDark = !isDark}, isDark)
                        }
                        composable("cus",enterTransition = { slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}, exitTransition = { slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(200)
                        )}){
                            Customization(viewModel, state,  isDark = isDark)
                        }
                    }
                }
            }
        }
    }
}