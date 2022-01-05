package com.example.opengraphsample.room

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class OgEntity(
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
