package com.example.compose_adapter.ui

import androidx.lifecycle.ViewModel
import com.example.compose_adapter.api.NewsApi
import com.example.compose_adapter.data.model.Article
import com.example.compose_adapter.data.model.NewsResponse
import com.example.compose_adapter.data.model.Widget
import com.example.compose_adapter.data.repo.PagingList
import com.example.compose_adapter.data.repo.PagingRepo

class MainViewModel : ViewModel() {
    private val service = NewsApi.get()

    fun getTopNews(): PagingList<Widget> = PagingRepo.getPaged { page ->
        service.getTopHeadlines("us", "business", page)
    }
}