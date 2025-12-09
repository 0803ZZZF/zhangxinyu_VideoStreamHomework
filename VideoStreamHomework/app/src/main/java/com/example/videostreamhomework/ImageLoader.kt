package com.example.videostreamhomework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// 加载网络图片（兼容所有版本）
@Composable
fun loadNetworkImage(url: String): ImageBitmap? {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            imageBitmap.value = try {
                downloadImage(context, url)?.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    return imageBitmap.value
}

// 加载本地资源图片（drawable图标）
@Composable
fun loadLocalImage(resId: Int): ImageBitmap? {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(resId) {
        withContext(Dispatchers.IO) {
            imageBitmap.value = try {
                // 最稳定的本地图片加载方式
                BitmapFactory.decodeResource(context.resources, resId).asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    return imageBitmap.value
}

// 网络图片下载实现
private fun downloadImage(context: Context, url: String): Bitmap? {
    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.doInput = true
        connection.connect()
        val inputStream = connection.inputStream
        BitmapFactory.decodeStream(inputStream) // 直接解码输入流
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}