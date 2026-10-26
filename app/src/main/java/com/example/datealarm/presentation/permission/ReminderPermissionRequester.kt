package com.example.datealarm.presentation.permission

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun ReminderPermissionRequester() {
    val context = LocalContext.current
    var showExactAlarmDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        showExactAlarmDialog = needsExactAlarmPermission(context)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showExactAlarmDialog = needsExactAlarmPermission(context)
        }
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = {showExactAlarmDialog = false},
            title = { Text("알람 권한이 필요해요") },
            text = { Text("결제일에 정확히 알림을 받으려면 '알람 및 리마인더' 권한을 켜 주세요.")},
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    context.startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }) { Text("설정으로 이동")}
            }, dismissButton = {
                TextButton(onClick = {showExactAlarmDialog = false}) { Text("나중에") }
            }
        )
    }

}

private fun needsExactAlarmPermission(context: Context): Boolean {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    return !alarmManager.canScheduleExactAlarms()
}