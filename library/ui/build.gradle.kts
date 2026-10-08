import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "cc.wuersan008.smartisanx.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    lint {
        // 本模块的 drawable 全部从原厂 APK 原样复制，其中一些素材在原版里就只有横屏
        // 或特定密度变体（例如 drawable-land-xxhdpi/ac.png 没有 base 版本）。
        // 这是「忠实还原原版资源」的结果，不是缺陷，因此关闭这条检查。
        disable += "MissingDefaultResource"
    }

    // 允许发布到本地或私有 Maven 仓库：./gradlew publishToMavenLocal
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":library:core"))

    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.text)
    api(libs.androidx.compose.ui.unit)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.animation)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "cc.wuersan008.smartisanx"
                artifactId = "smartisanx-ui"
                version = "0.1.0"
            }
        }
    }
}
