package com.example.datealarm.domain.usecase

import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.domain.repository.SubscriptionRepository

class AddSubscriptionUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(subscription: Subscription): Long =
        repository.add(subscription)
}