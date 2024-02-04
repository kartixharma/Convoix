package com.example.convoix.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.convoix.AppState
import com.example.convoix.ChatViewModel
import com.example.convoix.UserData

@Composable
fun MainScreen(viewModel: ChatViewModel, state: AppState, showSingleChat: (UserData, String) -> Unit,onSignOut: () -> Unit){
    val navController = rememberNavController()
    val navStackBackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navStackBackEntry?.destination
    data class BottomNavigationItem(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
    )
    val items = listOf(
        BottomNavigationItem(
            title = "Chats",
            selectedIcon = Icons.Filled.Message,
            unselectedIcon = Icons.Outlined.Message,
        ),
        BottomNavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed{index, item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.title  } == true,
                        onClick = { navController.navigate(route = item.title) },
                        icon = { Icon(
                            imageVector = if (currentDestination?.hierarchy?.any { it.route == item.title  } == true) {
                                item.selectedIcon
                            } else item.unselectedIcon,
                            contentDescription = item.title
                        )
                        },
                        label = {
                            Text(text = item.title)
                        }
                    )
                }
            }
        }
    ){it->
        NavHost(navController = navController,
            startDestination = "Chats",
            modifier = Modifier.padding(it)){
            composable("Chats"){
                ChatScreen(viewModel = viewModel, state = state, showSingleChat = {user, id-> showSingleChat(user, id)} )
            }
            composable("Profile"){
                ProfileScreen(userData = state.userData, onSignOut = onSignOut)
            }
            composable("Settings"){
            }
        }
    }

}