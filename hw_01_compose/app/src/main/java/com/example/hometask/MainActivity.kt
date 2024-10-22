package com.example.hometask

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private var dataSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        dataSize = savedInstanceState?.getInt("size") ?: 0

        super.onCreate(savedInstanceState)
        setContent {
            dataSize = plate(dataSize)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("size", dataSize)
    }
}

fun getColor(n: Int): Color {
    return if (n % 2 == 0) {
        Color.Red
    }
    else {
        Color.Blue
    }
}


@Composable
fun plate(dataSize: Int): Int {
    val size = remember { mutableIntStateOf(dataSize) }
    val data = List(size.intValue) { "${it + 1}" }
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val columns = if (isPortrait) 3 else 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.weight(1f)
        ) {
            items(data) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(getColor(it.toInt()))
                ) {
                    Text(text = it)
                }
            }
        }

        Box(contentAlignment = Alignment.BottomCenter) {
            Button(onClick = { size.intValue += 1 }) {
                Text(text = "Добавить панель")
            }
        }
    }
    return size.intValue
}
