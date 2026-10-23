package com.example.datealarm.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.domain.model.upcomingPaymentDate
import com.example.datealarm.domain.usecase.GetSubscriptionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SubscriptionListViewModel(
    getSubscriptions: GetSubscriptionUseCase
) : ViewModel() {

    val subscriptions: StateFlow<List<Subscription>> = getSubscriptions()
        .map { list -> list.sortedBy { it.upcomingPaymentDate() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}