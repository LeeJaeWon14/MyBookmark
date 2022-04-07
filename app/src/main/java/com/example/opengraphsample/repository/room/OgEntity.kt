package com.example.opengraphsample.repository.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.opengraphsample.Constants

@Entity
data class OgEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

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
) {
    constructor(ogMap: HashMap<String, String>) : this(
        0,
        ogMap.get(Constants.URL)!!,
        ogMap.get(Constants.SITE_NAME)!!,
        ogMap.get(Constants.TITLE)!!,
        ogMap.get(Constants.DESCRIPTION)!!,
        ogMap.get(Constants.IMAGE)!!
    )
}
