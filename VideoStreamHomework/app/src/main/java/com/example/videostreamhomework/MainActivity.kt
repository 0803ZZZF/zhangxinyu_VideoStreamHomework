package com.example.videostreamhomework

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    var seekToVideo: ((Int) -> Unit)? = null
    private var isLandscape by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            DouYinVideoApp()
        }
    }

    private fun toggleScreenOrientation() {
        isLandscape = !isLandscape
        requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Composable
    fun DouYinVideoApp() {
        val scope = rememberCoroutineScope() // 修复：添加协程作用域
        val context = LocalContext.current

        var currentPage by remember { mutableStateOf(1) }
        var videos by remember { mutableStateOf(DouYinVideoData.getVideosByPage(1, 10)) }
        var isLoading by remember { mutableStateOf(false) }
        var hasMore by remember { mutableStateOf(true) }

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp

        val listState = rememberLazyListState()
        var currentlyPlayingIndex by remember { mutableStateOf(0) }
        var isVideoManuallyPlaying by remember { mutableStateOf(true) }

        var sliderPosition by remember { mutableStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }
        var currentPosition by remember { mutableStateOf(0) }
        var duration by remember { mutableStateOf(1) }

        // 监听列表滚动切换播放项
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { index: Int ->
                    if (index != currentlyPlayingIndex) {
                        isVideoManuallyPlaying = true
                    }
                    currentlyPlayingIndex = index
                }
        }

        // 切换播放项时重置进度
        LaunchedEffect(currentlyPlayingIndex) {
            currentPosition = 0
            duration = 1
            isDragging = false
        }

        // 分页加载
        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastVisibleIndex ->
                    if (lastVisibleIndex != null && lastVisibleIndex >= videos.size - 3
                        && !isLoading && hasMore) {
                        isLoading = true
                        delay(500)
                        val nextPage = currentPage + 1
                        val newVideos = DouYinVideoData.getVideosByPage(nextPage, 10)
                        if (newVideos.isNotEmpty()) {
                            videos = videos + newVideos
                            currentPage = nextPage
                        } else {
                            hasMore = false
                        }
                        isLoading = false
                    }
                }
        }

        // 横屏/竖屏布局切换
        if (isLandscape) {
            // 横屏布局
            Box(modifier = Modifier.fillMaxSize()) {
                val currentVideo = videos.getOrNull(currentlyPlayingIndex)
                if (currentVideo != null && !currentVideo.isImageCard) {
                    VideoPlayer(
                        videoUrl = currentVideo.videoUrl,
                        isPlaying = isVideoManuallyPlaying,
                        onTogglePlay = { isVideoManuallyPlaying = !isVideoManuallyPlaying },
                        onProgressChange = { pos, dur ->
                            if (!isDragging) {
                                currentPosition = pos
                                duration = if (dur > 0) dur else 1
                            }
                        },
                        onToggleFullScreen = { toggleScreenOrientation() }
                    )

                    // 横屏进度条
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Slider(
                            value = if (isDragging) sliderPosition else currentPosition.toFloat(),
                            onValueChange = { newValue ->
                                isDragging = true
                                sliderPosition = newValue
                                currentPosition = newValue.toInt()
                            },
                            onValueChangeFinished = {
                                scope.launch {
                                    seekToVideo?.invoke(sliderPosition.toInt())
                                    delay(800)
                                    isDragging = false
                                }
                            },
                            valueRange = 0f..duration.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // 竖屏列表布局
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().background(Color.Black)
            ) {
                itemsIndexed(videos, key = { _, item -> item.videoUrl + item.coverUrl }) { index: Int, videoItem: VideoItem ->
                    val isPlaying = index == currentlyPlayingIndex && isVideoManuallyPlaying

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(screenHeight.dp)
                    ) {
                        // 视频/图片内容
                        if (!videoItem.isImageCard) {
                            VideoPlayer(
                                videoUrl = videoItem.videoUrl,
                                isPlaying = isPlaying,
                                onTogglePlay = { isVideoManuallyPlaying = !isVideoManuallyPlaying },
                                onProgressChange = { pos, dur ->
                                    if (index == currentlyPlayingIndex && !isDragging) {
                                        currentPosition = pos
                                        duration = if (dur > 0) dur else 1
                                    }
                                },
                                onToggleFullScreen = { toggleScreenOrientation() }
                            )

                            // 视频封面
                            AsyncImage(
                                model = videoItem.coverUrl,
                                contentDescription = "Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                alpha = if (index == currentlyPlayingIndex && duration < 500) 1f else 0f
                            )
                        } else {
                            // 图片卡片
                            AsyncImage(
                                model = videoItem.coverUrl,
                                contentDescription = "Image Card",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Text(
                                text = "图片",
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // 进度条
                        if (index == currentlyPlayingIndex && !videoItem.isImageCard) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(bottom = 80.dp)
                            ) {
                                Slider(
                                    value = if (isDragging) sliderPosition else currentPosition.toFloat()
                                        .coerceIn(0f, duration.toFloat()),
                                    onValueChange = { newValue ->
                                        isDragging = true
                                        sliderPosition = newValue
                                        currentPosition = newValue.toInt()
                                    },
                                    onValueChangeFinished = {
                                        scope.launch {
                                            seekToVideo?.invoke(sliderPosition.toInt())
                                            delay(200)
                                            isDragging = false
                                        }
                                    },
                                    valueRange = 0f..duration.toFloat(),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.Red,
                                        inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                Text(
                                    text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // 右侧互动区
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            AsyncImage(
                                model = videoItem.authorAvatar,
                                contentDescription = "Author Avatar",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                            InteractiveIcon(Icons.Default.Favorite, formatNumber(videoItem.likeCount))
                            Spacer(modifier = Modifier.height(32.dp))
                            InteractiveIcon(Icons.AutoMirrored.Filled.Comment, formatNumber(videoItem.commentCount))
                            Spacer(modifier = Modifier.height(32.dp))
                            InteractiveIcon(Icons.Default.Star, formatNumber(videoItem.collectCount))
                            Spacer(modifier = Modifier.height(32.dp))
                            InteractiveIcon(Icons.Default.Share, formatNumber(videoItem.shareCount))
                            Spacer(modifier = Modifier.height(48.dp))
                        }

                        // 左下角视频信息
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = videoItem.videoTitle,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "@${videoItem.authorName}",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(56.dp))
                        }
                    }
                }

                // 加载更多
                item {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else if (!hasMore) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "没有更多视频了",
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }

    // 格式化时间
    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    // 互动图标组件
    @Composable
    fun InteractiveIcon(icon: ImageVector, countText: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = countText,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }

    // 格式化数字
    private fun formatNumber(count: Int): String {
        return when {
            count >= 10000 -> String.format(Locale.getDefault(), "%.1fw", count / 10000.0)
            count >= 1000 -> String.format(Locale.getDefault(), "%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }
}