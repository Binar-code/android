package com.example.hw_02.api.models

data class GiphyResponse(
    val data: List<GifData>,
    val pagination: GifPagination
)
