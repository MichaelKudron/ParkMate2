package com.parkmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.parkmate.data.local.entities.ParkingSession

@Database(entities = [ParkingSession::class], version = 2, exportSchema = false)
abstract class ParkMateDatabase : RoomDatabase() {
    abstract fun parkingSessionDao(): ParkingSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ParkMateDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE parking_sessions ADD COLUMN accuracyMeters REAL")
            }
        }

        fun getDatabase(context: Context): ParkMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkMateDatabase::class.java,
                    "parkmate_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
