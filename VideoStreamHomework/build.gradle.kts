// 文件路径: [Project Root]/build.gradle.kts (不能是空文件！)

plugins {
    // 基础插件版本 (统一使用稳定版本 8.1.0)
    id("com.android.application") version "8.1.0" apply false
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false

    // 声明 Hilt 插件版本 (关键行: 解决 Hilt 插件找不到的问题)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}