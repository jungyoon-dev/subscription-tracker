package com.example.datealarm.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
       val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if(notificationId == -1) return
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    companion object{
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}