package com.example.compose_adapter.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class Nested(val type: String)

@JsonClass(generateAdapter = true)
data class Single(@Json(name = "url") val imageUrl: String) : Nested(type = "single")

@JsonClass(generateAdapter = true)
data class Multiple(val items: List<Single>) : Nested(type = "multiple")

@JsonClass(generateAdapter = true)
data class SampleResponse(
    val items: List<Nested>
)