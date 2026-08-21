package com.example.datealarm.presentation.navigation

sealed class Screen(val route: String) {
    data object List : Screen("list")
    data object AddEdit : Screen("add_edit")
}