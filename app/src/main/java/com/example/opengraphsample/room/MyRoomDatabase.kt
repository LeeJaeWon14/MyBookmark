package com.example.opengraphsample.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OgEntity::class], version = 1, exportSchema = true)
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
                ).fallbackToDestructiveMigration().build()
                return instance!!
            }
        }
    }
}