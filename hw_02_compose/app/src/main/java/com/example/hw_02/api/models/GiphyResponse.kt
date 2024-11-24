package com.example.hw_02.api.models

import com.example.hw_02.api.models.GifData
import com.example.hw_02.api.models.GifPagination

data class GiphyResponse(
    val data: List<GifData>,
    val pagination: GifPagination
)
