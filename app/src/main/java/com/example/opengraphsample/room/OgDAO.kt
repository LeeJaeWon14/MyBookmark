package com.example.opengraphsample.room

import androidx.room.*

@Dao
interface OgDAO {
    @Query("SELECT * FROM OgEntity")
    fun getOg() : List<OgEntity>

    @Insert
    fun insertOg(entity: OgEntity)

    @Update
    fun updateOg(entity: OgEntity)

    @Delete
    fun deleteOg(entity: OgEntity)
}