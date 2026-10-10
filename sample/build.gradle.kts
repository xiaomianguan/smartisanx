plugins {
    // AGP 9 起 Kotlin 编译由 AGP 内置支持（built-in Kotlin），不需要 org.jetbrains.kotlin.android。
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cc.wuersan008.smartisanx.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "cc.wuersan008.smartisanx.sample"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            // 库里有上万个原版素材，发布构建靠资源压缩把没用到的剔掉。
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    bundle {
        language {
            // 示例应用在 SampleActivity.attachBaseContext 里把自己的资源上下文锁到 zh-CN，
            // 如果按语言拆分，拆出来的 APK 里可能没有中文资源。这里不拆分。
            enableSplit = false
        }
    }
}

// 内置 Kotlin 不需要单独设 kotlin.compilerOptions.jvmTarget：
// 它默认跟随 android.compileOptions.targetCompatibility（这里保持 Java 11）。

dependencies {
    implementation(project(":library:core"))
    implementation(project(":library:ui"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
