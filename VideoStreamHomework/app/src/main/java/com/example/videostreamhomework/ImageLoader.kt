package com.example.videostreamhomework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

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

@Composable
fun loadLocalImage(resId: Int): ImageBitmap? {
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(resId) {
        withContext(Dispatchers.IO) {
            imageBitmap.value = try {

                BitmapFactory.decodeResource(context.resources, resId).asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    return imageBitmap.value
}

// 网络图片下载实现（采用OkHttp）
private fun downloadImage(context: Context, url: String): Bitmap? {
    // 创建 OkHttp 客户端
    val client = OkHttpClient.Builder()
        .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS) // 连接超时
        .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时
        .build()

    // 构建请求
    val request = Request.Builder()
        .url(url)
        .build()

    return try {
        // 发送请求并获取响应
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("请求失败: ${response.code}")
            }
            // 从响应体获取输入流并解码为 Bitmap
            val inputStream = response.body?.byteStream()
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
