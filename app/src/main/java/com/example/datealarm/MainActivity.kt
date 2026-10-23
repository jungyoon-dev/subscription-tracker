package com.example.datealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.datealarm.presentation.permission.ReminderPermissionRequester
import com.example.datealarm.presentation.navigation.SubscriptionNavHost
import com.example.datealarm.presentation.theme.DateAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateAlarmTheme {
                ReminderPermissionRequester()
                SubscriptionNavHost()
            }
        }
    }
}
