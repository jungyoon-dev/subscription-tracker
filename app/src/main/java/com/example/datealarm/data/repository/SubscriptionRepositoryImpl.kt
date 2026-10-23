package com.example.datealarm.data.repository

import com.example.datealarm.data.local.SubscriptionDao
import com.example.datealarm.data.local.SubscriptionEntity
import com.example.datealarm.domain.model.Subscription
import com.example.datealarm.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubscriptionRepositoryImpl (
    private val dao: SubscriptionDao
) : SubscriptionRepository {
    override fun getAll(): Flow<List<Subscription>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: Long): Subscription? =
        dao.getById(id)?.toDomain()

    override suspend fun add(subscription: Subscription): Long =
        dao.insert(subscription.toEntity())

    override suspend fun update(subscription: Subscription) {
        dao.update(subscription.toEntity())
    }

    override suspend fun delete(subscription: Subscription) {
        dao.delete(subscription.toEntity())
    }
}

private fun SubscriptionEntity.toDomain() = Subscription(
    id = id,
    name = name,
    price = price,
    cycle = cycle,
    nextPaymentDate = nextPaymentDate,
    reminderDaysBefore = reminderDaysBefore,
    reminderTime = reminderTime,
    reminderType = reminderType
)

private fun Subscription.toEntity() = SubscriptionEntity(
    id = id,
    name = name,
    price = price,
    cycle = cycle,
    nextPaymentDate = nextPaymentDate,
    reminderDaysBefore = reminderDaysBefore,
    reminderTime = reminderTime,
    reminderType = reminderType
)