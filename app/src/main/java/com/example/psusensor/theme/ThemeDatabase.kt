package com.example.psusensor.theme

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Theme::class], version = 1, exportSchema = true)
abstract class ThemeDatabase: RoomDatabase() {

    abstract fun themeDao(): ThemeDao

    companion object {
        @Volatile
        private var INSTANCE: ThemeDatabase? = null

        fun getDatabase(context: Context): ThemeDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThemeDatabase::class.java,
                    "theme_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}