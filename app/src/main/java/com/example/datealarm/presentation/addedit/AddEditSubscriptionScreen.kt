package com.example.datealarm.presentation.addedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.datealarm.domain.model.PaymentCycle
import com.example.datealarm.domain.model.ReminderType
import com.example.datealarm.domain.model.Subscription
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(onSaveClick: (Subscription) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf(PaymentCycle.MONTHLY) }
    var reminderDaysBefore by remember { mutableIntStateOf(7) }
    var nextPaymentDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf(LocalTime.of(9,0)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var reminderType by remember { mutableStateOf(setOf(ReminderType.NOTIFICATION)) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") }
        )

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("가격") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )


        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = cycle == PaymentCycle.MONTHLY,
                onClick = { cycle = PaymentCycle.MONTHLY }
            )
            Text("월 간")

            RadioButton(
                selected = cycle == PaymentCycle.YEARLY,
                onClick = { cycle = PaymentCycle.YEARLY}
            )
            Text("연 간")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = reminderDaysBefore == 1,
                onClick = {reminderDaysBefore = 1}
            )
            Text("1일 전")

            RadioButton(
                selected = reminderDaysBefore == 3,
                onClick = {reminderDaysBefore = 3}
            )
            Text("3일 전")

            RadioButton(
                selected = reminderDaysBefore == 7,
                onClick = {reminderDaysBefore = 7}
            )
            Text("일주일 전")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = ReminderType.NOTIFICATION in reminderType,
                onCheckedChange = { checked ->
                    reminderType = if (checked) reminderType + ReminderType.NOTIFICATION
                    else reminderType - ReminderType.NOTIFICATION
                }
            )
            Text("알림(노티)")

            Checkbox(
                checked = ReminderType.ALARM in reminderType,
                onCheckedChange = {checked ->
                    reminderType = if (checked) reminderType + ReminderType.ALARM
                    else reminderType - ReminderType.ALARM
                }
            )

            Text("알람")
        }

        OutlinedButton(onClick = { showDatePicker = true}) {
            Text("다음 결제일 : $nextPaymentDate")
        }

        if(showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = nextPaymentDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = {showDatePicker = false},
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                nextPaymentDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {Text("취소")}
                }
            ) {
                DatePicker(state = datePickerState)
            }

        }

        OutlinedButton(onClick = {showTimePicker = true}) {
            Text("알림 시각 : $reminderTime")
        }

        if (showTimePicker) {
            val timePickerState  = rememberTimePickerState(
                initialHour = reminderTime.hour,
                initialMinute = reminderTime.minute,
                is24Hour = true
            )

            Dialog(
                onDismissRequest = { showTimePicker = false }) {
                Surface(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TimeInput(state = timePickerState)
                        Row {
                            TextButton(
                                onClick = { showTimePicker = false}) {
                                Text("취소")
                            }
                            TextButton(
                                onClick = {
                                    reminderTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    showTimePicker = false
                                }) {
                                Text("확인")
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = {
            onSaveClick(
                Subscription(
                    id = 0L,
                    name = name,
                    price = price.toIntOrNull() ?: 0,
                    cycle = cycle,
                    nextPaymentDate = nextPaymentDate,
                    reminderDaysBefore = reminderDaysBefore,
                    reminderTime = reminderTime,
                    reminderType = reminderType
                )
            )
        }) {
            Text("저장")
        }
    }


}