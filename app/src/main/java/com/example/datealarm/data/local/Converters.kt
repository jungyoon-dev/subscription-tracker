package com.example.datealarm.data.local

import androidx.room.TypeConverter
import com.example.datealarm.domain.model.PaymentCycle
import com.example.datealarm.domain.model.ReminderType
import java.time.LocalDate
import java.time.LocalTime

// Local Data/ LocalTime/ PaymentCycle <-> Room이 이해하는 기본 타입 변환
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromSecondOfDay(value: Int?): LocalTime? = value?.let{LocalTime.ofSecondOfDay(it.toLong())}

    @TypeConverter
    fun toSecondOfDay(time: LocalTime?): Int? = time?.toSecondOfDay()

    @TypeConverter
    fun fromCycleName(value: String?): PaymentCycle? = value?.let(PaymentCycle::valueOf)

    @TypeConverter
    fun toCycleName(cycle: PaymentCycle?): String? = cycle?.name

    @TypeConverter
    fun fromReminderTypeCsv(value: String?): Set<ReminderType> =
        value?.takeIf { it.isNotBlank() }
            ?.split(",")
            ?.map { ReminderType.valueOf(it) }
            ?.toSet()
            ?: emptySet()

    @TypeConverter
    fun toReminderTypeCsv(types: Set<ReminderType>): String =
        types.joinToString(",") {it.name}

}