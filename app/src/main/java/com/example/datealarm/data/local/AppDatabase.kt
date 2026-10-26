package com.example.datealarm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

//  DB 인스턴스
@Database(entities = [SubscriptionEntity::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    companion object {
        @Volatile
        private var INSTANT: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANT ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "subscription_tracker.db"
            ).fallbackToDestructiveMigration(true).build().also {INSTANT = it}
        }
    }
}