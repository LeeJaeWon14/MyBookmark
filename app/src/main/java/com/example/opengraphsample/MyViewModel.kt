package com.example.opengraphsample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.opengraphsample.repository.model.MyRepository
import com.example.opengraphsample.repository.room.OgEntity
import kotlinx.coroutines.flow.Flow

class MyViewModel(private val repo: MyRepository) : ViewModel() {
    fun getContent() : Flow<PagingData<OgEntity>> {
        return repo.getOgListByPaging()
            .cachedIn(viewModelScope)
    }
}