package com.example.compose_adapter.data.model

val loaders: List<Loader> = buildList {
    repeat(5) { add(Loader) }
}

object Loader : Widget