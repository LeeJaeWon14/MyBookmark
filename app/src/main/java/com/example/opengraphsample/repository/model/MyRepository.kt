package com.example.opengraphsample.repository.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.opengraphsample.repository.room.OgDAO
import com.example.opengraphsample.repository.room.OgEntity
import kotlinx.coroutines.flow.Flow

class MyRepository(private val ogDao: OgDAO) {
    fun getOgListByPaging() : Flow<PagingData<OgEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { MyPagingSource(ogDao) }
        ).flow
    }
}