package com.example.hw_02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// TODO: разделить по файлам

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisplayImages()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayImages(vm: GifViewModel = viewModel()) {
    val gridState = rememberLazyStaggeredGridState()
    val showHTTPAlert by remember { vm.requestError }
    val showMaxRequestsAlert by remember { vm.maxRequests }

    if (showHTTPAlert) {
        BasicAlertDialog(
            onDismissRequest = { vm.getGif() },
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                // TODO: сообщения об ошибках вынести в ресурсы
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ошибка получения изображения",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { vm.getGif() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Повторить")
                    }
                }
            }
        }
    }

    // TODO: алерт должен пропадать
    if (showMaxRequestsAlert) {
        BasicAlertDialog(
            onDismissRequest = {  },
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                // TODO: сообщения об ошибках вынести в ресурсы
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Достигнуто максимальное количество изображений",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ок")
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            verticalItemSpacing = 4.dp,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                items(vm.gifs) { gif ->
                    GlideImage(
                        imageModel = { gif },
                        requestOptions = {
                            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp, start = 4.dp, end = 4.dp),
            state = gridState
        )
        // TODO: крутилка без мигания
        if (vm.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    .align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(50.dp)
                )
            }
        }
    }

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (lastVisibleItemIndex == vm.gifs.size - 1 && !vm.isLoading.value) {
                    vm.getGif()
                }
            }
    }
}


// TODO: разделить ошибки лимита апи и ошибки сети
class GifViewModel : ViewModel() {
    val gifs = mutableStateListOf<String>()
    var requestError = mutableStateOf(false)
    var isLoading = mutableStateOf(false)
    var maxRequests = mutableStateOf(false)

    init {
        getGif(true)
    }

    fun getGif(firstLoad: Boolean = false) {
        if (isLoading.value) return

        if (maxRequests.value) return

        isLoading.value = true
        viewModelScope.launch {
            requestError.value = false
            try {
                val repeats = if (firstLoad) 20 else 3
                for (i in 1..repeats) {
                    val request = NetworkService.api.getRandomGif(
                        // TODO: безопасное хранение ключа
                        "BISJs3DxO7gO4XAS5DwgE50rxUKYSsWc",
                        "cats"
                    )
                    gifs.add(request.data.images.original.url)
                }
            } catch (e: HttpException) {
                // TODO: код вынести в ресурсы
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
