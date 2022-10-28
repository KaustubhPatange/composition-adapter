package com.example.compose_adapter.data.model

interface PaginatedResponse<T> {
    val articles: List<T>
    val totalResults: Int
}