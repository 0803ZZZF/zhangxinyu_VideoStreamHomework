package com.example.videostreamhomework

import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    onTogglePlay: () -> Unit,
    onProgressChange: (currentPosition: Int, duration: Int) -> Unit,
    seekTo: ((Int) -> Unit)? = null,
    onToggleFullScreen: () -> Unit // 横屏切换回调
) {
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    val context = LocalContext.current

    // 配置 VideoView 的 URI 和监听器
    LaunchedEffect(videoUrl) {
        if (videoUrl.isBlank()) {
            videoView?.stopPlayback()
            return@LaunchedEffect
        }

        videoView?.stopPlayback()
        val uri = Uri.parse(videoUrl)
        videoView?.setVideoURI(uri)

        videoView?.setOnPreparedListener { mp ->
            mp.isLooping = true
            Log.d("VideoPlayer", "视频准备完成")
            if (isPlaying) {
                videoView?.start()
            }
        }

        videoView?.setOnErrorListener { _, what, extra ->
            Log.e("VideoPlayer", "播放错误: what=$what, extra=$extra, URL: $videoUrl")
            true
        }
    }

    // 控制播放/暂停状态
    LaunchedEffect(isPlaying) {
        if (videoView == null) return@LaunchedEffect
        if (isPlaying) {
            if (!videoView!!.isPlaying) videoView?.start()
        } else {
            if (videoView!!.isPlaying) videoView?.pause()
        }
    }

    // 更新进度
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        while (videoView != null && isPlaying && videoView!!.duration > 0) {
            val currentPos = videoView!!.currentPosition
            val totalDuration = videoView!!.duration
            onProgressChange(currentPos, totalDuration)
            delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (videoView?.duration ?: 0 > 0) {
                    onTogglePlay()
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoView = this
                }
            },
            update = { view ->
                seekTo?.let { it(view.currentPosition) }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 播放/暂停图标
        if (!isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "播放/暂停",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
            )
        }

        // 全屏切换按钮
        Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = "切换全屏",
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable { onToggleFullScreen() }
        )
    }

    // 绑定进度跳转
    LaunchedEffect(videoView) {
        (context as? MainActivity)?.seekToVideo = { seekTime ->
            videoView?.seekTo(seekTime)
        }
    }

    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            (context as? MainActivity)?.seekToVideo = null
            videoView?.stopPlayback()
            videoView = null
        }
    }
}