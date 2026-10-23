package com.example.datealarm.data.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.datealarm.data.local.AppDatabase
import com.example.datealarm.data.repository.SubscriptionRepositoryImpl
import com.example.datealarm.domain.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subscriptionId = intent.getLongExtra(EXTRA_SUBSCRIPTION_ID, -1L)
        val name = intent.getStringExtra(EXTRA_NAME) ?: return
        val price = intent.getIntExtra(EXTRA_PRICE, 0)
        val types = intent.getStringArrayExtra(EXTRA_REMINDER_TYPES)
            ?.map { ReminderType.valueOf(it) }
            ?.toSet()
            ?: emptySet()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).subscriptionDao()
                val subscription = SubscriptionRepositoryImpl(dao).getById(subscriptionId)
                    ?: return@launch
                ReminderScheduler(context).schedule(subscription)
            } finally {
                pendingResult.finish()
            }
        }

        createNotificationChannels(context)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if(!hasPermission) return

        if(ReminderType.NOTIFICATION in types) {
           postPlainNotification(context, subscriptionId, name, price)
        }

        if(ReminderType.ALARM in types) {
            postAlarmNotification(context, subscriptionId, name, price)
        }

    }

    private fun postPlainNotification(context: Context, subscriptionId: Long, name: String, price: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATION)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$name 결제 예정")
            .setContentText("${price}원 결제가 곧 예정되어 있어요")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_OFFSET + subscriptionId.toInt(), notification)
    }

    private fun postAlarmNotification(context: Context, subscriptionId: Long, name: String, price: Int) {
        val notificationId = ALARM_ID_OFFSET + subscriptionId.toInt()

        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java).apply {
            putExtra(AlarmDismissReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALARM)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$name 결제 알람")
            .setContentText("${price}원 결제 알람이 울리고 있어요")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(0, "확인", dismissPendingIntent)
            .build()
            .apply { flags = flags or Notification.FLAG_INSISTENT }

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)

        val notificationChannel = NotificationChannel(
            CHANNEL_ID_NOTIFICATION,
            "구독 결제 알림",
            NotificationManager.IMPORTANCE_HIGH
        )

        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            "구독 결제 알람",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "확인할 때까지 반복해서 울리는 알람"
            enableLights(true)
        }

        manager.createNotificationChannels(listOf(notificationChannel, alarmChannel))
    }
    companion object {
        const val CHANNEL_ID_NOTIFICATION = "subscription_reminder_channel"
        const val CHANNEL_ID_ALARM = "subscription_alarm_channel"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_REMINDER_TYPES = "extra_reminder_types"
        private const val NOTIFICATION_ID_OFFSET = 0
        private const val ALARM_ID_OFFSET = 100_000
    }
}