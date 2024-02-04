package com.example.convoix

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.convoix.screens.Chat
import com.example.convoix.screens.ChatScreen
import com.example.convoix.screens.ProfileScreen
import com.example.convoix.screens.SignInScreen1
import com.example.convoix.ui.theme.ConvoixTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvoixTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = viewModel<ChatViewModel>()
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()
                    var userData1 : UserData? =null
                    var chatId: String = ""
                    NavHost(navController = navController,
                        startDestination = "signIn"){
                        composable("signIn"){
                            LaunchedEffect(key1 = Unit){
                                if(googleAuthUiClient.getSignedInUser() != null){
                                    viewModel.setUserData(googleAuthUiClient.getSignedInUser()!!)
                                    viewModel.showChats()
                                    navController.navigate("main")
                                }
                            }
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
                                        viewModel.addUserDataToFirestore(it)
                                        viewModel.setUserData(userData)
                                        viewModel.showChats()
                                    }
                                    viewModel.showAnim()
                                    // delay(2000)
                                    navController.navigate("main")
                                    viewModel.resetState()
                                    viewModel.setUserData(userData!!)
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
                        composable("profile"){
                            ProfileScreen(userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(applicationContext, "Signed Out", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                    }
                                }
                            )
                        }
                        composable("main"){
                            ChatScreen(viewModel, state, showSingleChat = { usr, id ->
                                userData1=usr
                                chatId=id
                                viewModel.popMessage(id)
                                navController.navigate("chat")
                            })
                        }
                        composable("chat"){
                            Chat(viewModel.messages, userData1!!, sendReply = {msg, id->
                                viewModel.sendReply(msg = msg, chatId = id)
                            },chatId, state)
                        }
                    }
                }
            }
        }
    }
}