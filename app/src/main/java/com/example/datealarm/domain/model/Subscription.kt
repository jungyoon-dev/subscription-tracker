package com.example.datealarm.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Subscription (
    val id: Long,
    val name: String,
    val price:Int,
    val cycle: PaymentCycle,
    val nextPaymentDate: LocalDate,
    val reminderDaysBefore: Int = 1,
    val reminderTime: LocalTime = LocalTime.of(9,0),
    val reminderType: Set<ReminderType> = setOf(ReminderType.NOTIFICATION)
)