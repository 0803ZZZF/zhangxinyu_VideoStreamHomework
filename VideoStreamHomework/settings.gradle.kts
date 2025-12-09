// 文件路径: [Project Root]/settings.gradle.kts

pluginManagement {
    repositories {
        // 必须保留 google() 和 mavenCentral()
        google()
        mavenCentral()

        gradlePluginPortal()
    }
    plugins {
        // 统一使用稳定版本
        id("com.android.application") version "8.1.0" apply false
        id("com.android.library") version "8.1.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.20" apply false
        id("org.jetbrains.kotlin.kapt") version "1.9.20" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 您可以保留默认仓库，或使用阿里云镜像来加速下载，这里使用默认
        google()
        mavenCentral()
        // maven { setUrl("https://maven.aliyun.com/repository/google") } // 阿里云镜像备用
        // maven { setUrl("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "VideoStreamHomework"
include(":app")