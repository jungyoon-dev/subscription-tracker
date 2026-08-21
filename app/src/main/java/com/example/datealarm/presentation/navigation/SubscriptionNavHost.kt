package com.example.datealarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.datealarm.domain.model.PaymentCycle
import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.presentation.addedit.AddEditSubscriptionScreen
import com.example.datealarm.presentation.list.SubscriptionListScreen
import java.time.LocalDate

@Composable
fun SubscriptionNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.List.route) {
        composable(Screen.List.route) {
            SubscriptionListScreen(
                subscriptions = listOf(
                    Subscription(1, "NetFlix", 17000, PaymentCycle.MONTHLY, LocalDate.now().plusDays(5)),
                    Subscription(2, "Melon", 10900, PaymentCycle.MONTHLY, LocalDate.now().plusDays(20))),
                onAddClick = {navController.navigate(
                Screen.AddEdit.route) }
            )
        }
        composable(Screen.AddEdit.route) {
            AddEditSubscriptionScreen()
        }
    }

}
