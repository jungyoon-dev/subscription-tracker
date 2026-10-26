package com.example.datealarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.datealarm.data.alarm.ReminderScheduler
import com.example.datealarm.data.local.AppDatabase
import com.example.datealarm.data.repository.SubscriptionRepositoryImpl
import com.example.datealarm.domain.usecase.AddSubscriptionUseCase
import com.example.datealarm.domain.usecase.GetSubscriptionUseCase
import com.example.datealarm.presentation.addedit.AddEditSubscriptionScreen
import com.example.datealarm.presentation.addedit.AddEditSubscriptionViewModel
import com.example.datealarm.presentation.list.SubscriptionListScreen
import com.example.datealarm.presentation.list.SubscriptionListViewModel

@Composable
fun SubscriptionNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Screen.List.route) {
        composable(Screen.List.route) {
            val viewModel: SubscriptionListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val dao = AppDatabase.getInstance(context).subscriptionDao()
                        val repository = SubscriptionRepositoryImpl(dao)
                        SubscriptionListViewModel(GetSubscriptionUseCase(repository))
                    }
                }
            )
            val subscriptions by viewModel.subscriptions.collectAsState()

            SubscriptionListScreen(
                subscriptions = subscriptions,
                onAddClick = {navController.navigate(Screen.AddEdit.route)}

            )
        }
        composable(Screen.AddEdit.route) {
            val addEditViewModel: AddEditSubscriptionViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        val dao = AppDatabase.getInstance(context).subscriptionDao()
                        val repository = SubscriptionRepositoryImpl(dao)
                        val scheduler = ReminderScheduler(context)
                        AddEditSubscriptionViewModel(AddSubscriptionUseCase(repository), scheduler)
                    }
                }
            )
            AddEditSubscriptionScreen(
                onSaveClick = { subscription ->
                    addEditViewModel.save(subscription)
                    navController.popBackStack()
                }
            )
        }
    }

}
