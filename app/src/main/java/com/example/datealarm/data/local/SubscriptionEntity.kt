package com.example.datealarm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.datealarm.domain.model.PaymentCycle
import com.example.datealarm.domain.model.ReminderType
import java.time.LocalDate
import java.time.LocalTime

//Room 용 Entity
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Int,
    val cycle: PaymentCycle,
    val nextPaymentDate: LocalDate,
    val reminderDaysBefore: Int,
    val reminderTime: LocalTime,
    val reminderType: Set<ReminderType>
) {

}