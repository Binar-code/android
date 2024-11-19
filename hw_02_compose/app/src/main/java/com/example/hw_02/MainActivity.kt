package com.example.hw_02

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.DefaultStrokeLineWidth
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisplayImages()
        }
    }
}

@Composable
fun DisplayImages(vm: GifViewModel = viewModel()) {
    LazyColumn {
        items(vm.gifs) { url ->
            GlideImage(
                imageModel = { url }
            )
        }
    }
}

class GifViewModel : ViewModel() {
    val gifs = mutableStateListOf<String>()

    init {
        getGif()
    }

    fun getGif() {
        viewModelScope.launch {
            val request = NetworkService.api.getRandomGif(
                "whqMjOPYTj8KZ0JCLoqhN8biPEaWiiNx",
                "dogs"
            )

            gifs.add(request.data.images.original.url)
            Log.d("NETWORK", "${request.data.images.original.url}")
        }
    }
}

data class GiphyResponse(
    val data: GifData
)

data class GifData(
    val images: GifImages
)

data class GifImages(
    val original: GifDetails
)

data class GifDetails(
    val url: String
)

interface GiphyApi {
    @GET("v1/gifs/random")

    suspend fun getRandomGif(
        @Query("api_key") apiKey: String,
        @Query("tag") tag: String
    ): GiphyResponse
}

object NetworkService {
    private const val BASE_URL = "https://api.giphy.com/"

    val api: GiphyApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GiphyApi::class.java)
    }
}
