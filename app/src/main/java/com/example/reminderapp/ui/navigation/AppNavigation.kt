package com.example.reminderapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reminderapp.ui.screen.EditListScreen
import com.example.reminderapp.ui.screen.EditReminderScreen
import com.example.reminderapp.ui.screen.MainScreen
import com.example.reminderapp.ui.screen.ReminderListDetailScreen
import com.example.reminderapp.ui.viewmodel.ReminderViewModel

object Routes {
    const val MAIN_SCREEN = "main"
    const val LIST_DETAIL_SCREEN = "listDetail/{listId}"
    const val ADD_LIST_SCREEN = "addList"
    const val EDIT_LIST_SCREEN = "editList/{listId}"
    const val ADD_REMINDER_SCREEN = "addReminder/{listId}"
    const val EDIT_REMINDER_SCREEN = "editReminder/{listId}/{reminderId}"

    fun listDetail(listId: String) = "listDetail/$listId"
    fun editList(listId: String) = "editList/$listId"
    fun addReminder(listId: String) = "addReminder/$listId"
    fun editReminder(listId: String, reminderId: String) = "editReminder/$listId/$reminderId"
}

@Composable
fun AppNavigation(reminderViewModel: ReminderViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN_SCREEN) {
        composable(Routes.MAIN_SCREEN) {
            MainScreen(navController = navController, viewModel = reminderViewModel)
        }
        composable(
            route = Routes.LIST_DETAIL_SCREEN,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            if (listId != null) {
                ReminderListDetailScreen(
                    navController = navController,
                    viewModel = reminderViewModel,
                    listId = listId
                )
            }
        }
        composable(Routes.ADD_LIST_SCREEN) {
            EditListScreen(navController = navController, viewModel = reminderViewModel, listId = null)
        }
        composable(
            route = Routes.EDIT_LIST_SCREEN,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            EditListScreen(navController = navController, viewModel = reminderViewModel, listId = listId)
        }
        composable(
            route = Routes.ADD_REMINDER_SCREEN,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            if (listId != null) {
                EditReminderScreen(navController = navController, viewModel = reminderViewModel, listId = listId, reminderId = null)
            }
        }
        composable(
            route = Routes.EDIT_REMINDER_SCREEN,
            arguments = listOf(
                navArgument("listId") { type = NavType.StringType },
                navArgument("reminderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")
            val reminderId = backStackEntry.arguments?.getString("reminderId")
            if (listId != null && reminderId != null) {
                EditReminderScreen(navController = navController, viewModel = reminderViewModel, listId = listId, reminderId = reminderId)
            }
        }
    }
}
