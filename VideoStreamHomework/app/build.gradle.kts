// 文件路径: [Project Root]/app/build.gradle.kts

plugins {
    // 基础插件
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // 应用 Dagger Hilt 插件
    id("com.google.dagger.hilt.android")
    // 启用 Kotlin 注解处理器，用于 Hilt
    kotlin("kapt")
}

android {
    namespace = "com.example.videostreamhomework"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.videostreamhomework"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 启用 Jetpack Compose
    buildFeatures {
        compose = true
    }
    composeOptions {
        // 🚨 关键修正：从 1.5.1 修正为 1.5.4，解决 Unresolved reference 错误
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 基础 Android KTX 和 Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose BOM (统一管理 Compose 版本，推荐)
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    // Compose 核心库
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")

    implementation("com.google.android.material:material:1.12.0")

    // Coil for 异步图片加载 (用于 AsyncImage)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Dagger Hilt 依赖
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Test dependencies (默认测试库)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.compose.material:material-icons-extended")
}