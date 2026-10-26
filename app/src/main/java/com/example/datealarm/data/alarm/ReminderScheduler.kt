package com.example.datealarm.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.datealarm.domain.model.nextReminderAt
import com.example.datealarm.domain.model.Subscription
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {

    fun schedule(subscription: Subscription) {
        /*val triggerAtMillis = subscription.nextPaymentDate
            .minusDays(subscription.reminderDaysBefore.toLong())
            .atTime(subscription.reminderTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if(triggerAtMillis <= System.currentTimeMillis()) return*/
        val triggerAtMillis = subscription.nextReminderAt()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        //val triggerAtMillis = System.currentTimeMillis() + 30_000   //테스트용 30초뒤
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_SUBSCRIPTION_ID, subscription.id)
            putExtra(AlarmReceiver.EXTRA_NAME, subscription.name)
            putExtra(AlarmReceiver.EXTRA_PRICE, subscription.price)
            putExtra(AlarmReceiver.EXTRA_REMINDER_TYPES, subscription.reminderType.map {it.name}.toTypedArray())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            subscription.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val canSchedulerExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
        if (!canSchedulerExact) return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}