package com.example.datealarm.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

fun Subscription.paymentDateAt(n: Long): LocalDate = when (cycle) {
    PaymentCycle.MONTHLY -> nextPaymentDate.plusMonths(n)
    PaymentCycle.YEARLY -> nextPaymentDate.plusYears(n)
}


fun Subscription.upcomingPaymentDate(today: LocalDate = LocalDate.now()): LocalDate {
    var n = 0L
    while (paymentDateAt(n).isBefore(today)) n++
    return paymentDateAt(n)
}


fun Subscription.nextReminderAt(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
    var n = 0L
    while (true) {
        val reminderAt = paymentDateAt(n)
            .minusDays(reminderDaysBefore.toLong())
            .atTime(reminderTime)
        if (reminderAt.isAfter(now)) return reminderAt
        n++
    }
}