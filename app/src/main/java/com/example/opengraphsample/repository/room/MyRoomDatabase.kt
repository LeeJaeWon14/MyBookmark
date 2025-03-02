package com.example.opengraphsample.repository.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OgEntity::class], version = 3, exportSchema = true)
abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun getOgDAO() : OgDAO

    companion object {
        private var instance: MyRoomDatabase? = null

        fun getInstance(context: Context) : MyRoomDatabase {
            instance?.let {
                return it
            } ?: run {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                    "OpenGraph.db"
                )/*.addMigrations(MIGRATION_TO_2)*/
                        .fallbackToDestructiveMigration()
//                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                        .build() // Will adding migration
                return instance!!
            }
        }

        fun closeDatabase() {
            instance?.close()
            instance = null
        }

//        private val MIGRATION_TO_2 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL("")
//            }
//        }
    }
}