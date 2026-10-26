package com.example.datealarm.domain.usecase

import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

class GetSubscriptionUseCase(private val repository: SubscriptionRepository) {
    operator fun invoke(): Flow<List<Subscription>> = repository.getAll()
}