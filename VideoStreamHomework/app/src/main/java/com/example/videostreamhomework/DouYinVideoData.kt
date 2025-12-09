package com.example.videostreamhomework

import java.util.Locale

// 视频/图片数据模型
data class VideoItem(
    val coverUrl: String,       // 封面图URL
    val videoUrl: String,       // 视频URL
    val authorName: String,     // 作者名
    val authorAvatar: String,   // 作者头像URL
    val likeCount: Int,         // 点赞数
    val commentCount: Int,      // 评论数
    val collectCount: Int,      // 收藏数
    val shareCount: Int,        // 分享数
    val videoTitle: String,     // 视频标题
    val isImageCard: Boolean    // 是否是图片卡片
)

// 提供假数据（运行时会加载这些数据）
object DouYinVideoData {

    private val VIDEO_URLS = listOf(
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4", // 稳定可用
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        "https://www.w3schools.com/html/mov_bbb.mp4"
    )

    // 生成50条数据，循环使用新的视频URL资源池
    private val ALL_FAKE_VIDEOS: List<VideoItem> = (1..50).map { i ->
        // 计算当前索引对应的视频URL（循环使用3个视频资源）
        val videoUrlIndex = (i - 1) % VIDEO_URLS.size
        VideoItem(
            coverUrl = "https://picsum.photos/1080/1920?random=${i}",
            // 非图片卡片时使用资源池中的视频URL
            videoUrl = if (i % 5 == 0) "" else VIDEO_URLS[videoUrlIndex],
            authorName = "用户 $i",
            authorAvatar = "https://picsum.photos/200/200?random=${100 + i}",
            likeCount = (1000 + i * 100),
            commentCount = (100 + i * 10),
            collectCount = (50 + i * 5),
            shareCount = (10 + i),
            videoTitle = "这是第 $i 条短视频标题 #Compose #分页",
            isImageCard = i % 5 == 0
        )
    }

    fun getVideosByPage(page: Int, pageSize: Int = 10): List<VideoItem> {
        val startIndex = (page - 1) * pageSize
        val endIndex = startIndex + pageSize

        if (startIndex >= ALL_FAKE_VIDEOS.size) {
            return emptyList()
        }

        return ALL_FAKE_VIDEOS.subList(
            startIndex,
            endIndex.coerceAtMost(ALL_FAKE_VIDEOS.size)
        )
    }
    
    fun getFakeVideos(): List<VideoItem> {
        return getVideosByPage(1, 10)
    }
}
