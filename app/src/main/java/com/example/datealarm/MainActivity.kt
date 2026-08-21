package com.example.datealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.datealarm.domain.model.PaymentCycle
import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.presentation.list.SubscriptionListScreen
import com.example.datealarm.presentation.navigation.SubscriptionNavHost
import java.time.LocalDate
import com.example.datealarm.presentation.theme.DateAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DateAlarmTheme {
                SubscriptionNavHost()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DateAlarmTheme {
        Greeting("Android")
    }
}