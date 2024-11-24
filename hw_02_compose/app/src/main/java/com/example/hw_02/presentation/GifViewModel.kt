package com.example.hw_02.presentation

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_02.api.NetworkService
import com.example.hw_02.api.models.GifDetails
import kotlinx.coroutines.launch
import retrofit2.HttpException

class GifViewModel : ViewModel() {
    val gifs = mutableStateListOf<GifDetails>()
    val requestError = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val maxRequests = mutableStateOf(false)
    val maxRequestsAlert = mutableStateOf(false)
    private val offset = mutableIntStateOf(0)
    private val total = mutableIntStateOf(0)

    private var key: String? = null

    fun setKey(apiKey: String) {
        key = apiKey
    }

    fun canLoad(): Boolean = total.intValue == 0 || gifs.size < total.intValue

    fun getGif() {
        if (isLoading.value || maxRequests.value) return

        isLoading.value = true
        viewModelScope.launch {
            requestError.value = false
            try {
                val request = NetworkService.api.getRandomGif(
                    key!!,
                    10,
                    offset.intValue

                )
                total.intValue = request.pagination.totalCount
                for (i in request.data)
                    gifs.add(i.images.original)
                offset.value += 10
            } catch (e: HttpException) {
                if (e.code() == 429) {
                    maxRequests.value = true
                } else {
                    requestError.value = true
                }
            } catch (e: Exception) {
                requestError.value = true
            } finally {
                isLoading.value = false
            }
        }
    }
}
