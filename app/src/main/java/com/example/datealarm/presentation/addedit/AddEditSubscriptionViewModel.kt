package com.example.datealarm.presentation.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datealarm.data.alarm.ReminderScheduler
import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.domain.usecase.AddSubscriptionUseCase
import kotlinx.coroutines.launch

class AddEditSubscriptionViewModel(
    private val addSubscription: AddSubscriptionUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    fun save(subscription: Subscription) {
        viewModelScope.launch {
            val id = addSubscription(subscription)
            reminderScheduler.schedule(subscription.copy(id = id))
        }
    }
}