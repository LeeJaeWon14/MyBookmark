package com.example.opengraphsample.repository.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

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

    @Query("SELECT * FROM OgEntity ORDER BY id DESC LIMIT :loadSize OFFSET :page * :loadSize")
    fun getOgPage(page: Int, loadSize: Int) : List<OgEntity>

    @Query("SELECT * FROM OgEntity WHERE title = :title")
    fun checkDistinct(title: String) : OgEntity?
}