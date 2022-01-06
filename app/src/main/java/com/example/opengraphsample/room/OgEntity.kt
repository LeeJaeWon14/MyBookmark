package com.example.opengraphsample.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OgEntity(
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "site_name")
    val siteName: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "image")
    val image: String
)
