package com.example.datealarm.domain.repository

import com.example.datealarm.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getAll(): Flow<List<Subscription>>
    suspend fun getById(id: Long): Subscription?
    suspend fun add(subscription: Subscription)
    suspend fun update(subscription: Subscription)
    suspend fun delete(subscription: Subscription)
}