package com.example.compose_adapter.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsResponse(
    val status: String,
    override val articles: List<Article>,
    override val totalResults: Int
) : PaginatedResponse<Widget>

@JsonClass(generateAdapter = true)
data class Article(
    val title: String,
    val description: String? = null,
    @Json(name = "urlToImage")
    val imageUrl: String? = null
) : Widget