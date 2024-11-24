package com.example.hw_02.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hw_02.R
import com.skydoves.landscapist.glide.GlideImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayImages(vm: GifViewModel = viewModel()) {
    val gridState = rememberLazyStaggeredGridState()
    val showHTTPAlert by remember { vm.requestError }
    val maxRequests by remember { vm.maxRequests }
    val maxRequestsAlert by remember { vm.maxRequestsAlert }

    val context = LocalContext.current
    val apiKey = remember { context.getString(R.string.api_key) }

    LaunchedEffect(apiKey) {
        vm.setKey(apiKey)
        vm.getGif()
    }

    if (showHTTPAlert) {
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
                        text = stringResource(R.string.api_request_err),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { vm.getGif() },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.retry_msg))
                    }
                }
            }
        }
    }

    if (maxRequests && !maxRequestsAlert) {
        BasicAlertDialog(
            onDismissRequest = {  },
        ) {
            Surface(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.api_limit_err),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { vm.maxRequestsAlert.value = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(stringResource(R.string.ok_msg))
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
                        imageModel = { gif.webp },
                        previewPlaceholder = painterResource(R.drawable.placeholder),
                        modifier = Modifier
                            .width((gif.width / LocalDensity.current.density).dp)
                            .height((gif.height / LocalDensity.current.density).dp),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .width((gif.width / LocalDensity.current.density).dp)
                                    .height((gif.height / LocalDensity.current.density).dp)
                                    .background(Color.Gray),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        failure = {
                            Box(
                                modifier = Modifier
                                    .width((gif.width / LocalDensity.current.density).dp)
                                    .height((gif.height / LocalDensity.current.density).dp)
                                    .background(Color.Gray),
                            ) {
                                Text(text = stringResource(R.string.img_err))
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 45.dp, start = 4.dp, end = 4.dp),
            state = gridState
        )

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
                if (lastVisibleItemIndex == vm.gifs.size - 1
                    && !vm.isLoading.value
                    && !vm.maxRequests.value
                    && vm.canLoad()
                ) {
                    vm.getGif()
                }
            }
    }
}
