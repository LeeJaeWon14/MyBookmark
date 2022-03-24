package com.example.opengraphsample.repository.model

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.opengraphsample.repository.room.OgDAO
import com.example.opengraphsample.repository.room.OgEntity

class MyPagingSource constructor(
    private val ogDao: OgDAO
) : PagingSource<Int, OgEntity>() {
    override fun getRefreshKey(state: PagingState<Int, OgEntity>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OgEntity> {
        val page = params.key ?: 1
        val data = ogDao.getOgPage(page, params.loadSize)
        return LoadResult.Page(
            data = data,
            prevKey = if(page == 1) null else page - 1,
            nextKey = if(data.isEmpty()) null else page + 1
        )
    }
}