package com.example.datealarm.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.datealarm.domain.model.Subscription
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add


@Composable
fun SubscriptionListScreen(
    subscriptions: List<Subscription>,
    onAddClick:() -> Unit
) {
    Scaffold(floatingActionButton = {
        FloatingActionButton(onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "구독 추가")
        }

    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(subscriptions) { subscription ->
                SubscriptionListItem(subscription)

            }
        }
    }

}

@Composable
fun SubscriptionListItem(subscription: Subscription, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(subscription.name, style = MaterialTheme.typography.titleMedium)
            Text("${subscription.price}원 , ${subscription.cycle}")
            Text("다음 결제일: ${subscription.nextPaymentDate}")
        }

    }
}