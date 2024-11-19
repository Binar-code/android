package com.example.hw_02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayImages(vm: GifViewModel = viewModel()) {
    val gridState = rememberLazyStaggeredGridState()
    val showAlert by remember { vm.requestError }

    if (showAlert == 1) {
        BasicAlertDialog(
            onDismissRequest = { vm.getGif() },
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
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

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        verticalItemSpacing = 4.dp,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            items(vm.gifs) { gif ->
                GlideImage(
                    imageModel = { gif }
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 45.dp, start = 4.dp, end = 4.dp),
        state = gridState
    )

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (lastVisibleItemIndex == vm.gifs.size - 1 && !vm.isLoading) {
                    vm.getGif()
                }
            }
    }
}

class GifViewModel : ViewModel() {
    val gifs = mutableStateListOf<String>()
    var requestError = mutableIntStateOf(0)
    var isLoading = false

    init {
        getGif()
    }

    fun getGif() {
        if (isLoading) return

        isLoading = true
        viewModelScope.launch {
            requestError.intValue = 0
            try {
                for (i in 1..3) {
                    val request = NetworkService.api.getRandomGif(
                        "BISJs3DxO7gO4XAS5DwgE50rxUKYSsWc",
                        "cats"
                    )
                    gifs.add(request.data.images.original.url)
                }
            } catch (e: HttpException) {
                requestError.intValue = 1
            } finally {
                isLoading = false
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
