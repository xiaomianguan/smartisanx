plugins {
    // AGP 9 起 Kotlin 编译由 AGP 内置支持（built-in Kotlin），不需要 org.jetbrains.kotlin.android。
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "cc.wuersan008.smartisanx.ui"
    compileSdk = 37

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

        // 下面这些检查针对的是「自己设计的资源」：重复图标、图标尺寸/密度不统一、
        // 用 px 当单位、字符串该用 plurals……本模块的资源是原版素材的逐字节复制，
        // 文件名、限定符目录与单位都照抄原版（见 README「资源来源与授权」），改了就丢还原度。
        // 关掉它们，让 lint 输出里只剩代码层面的真问题。
        //
        // PluralsCandidate：`smartisan_days_after` / `_before` 只会以 n ≥ 2 调用
        // （±1 天走 `smartisan_tomorrow` / `smartisan_yesterday`），英文单复数不会出错。
        // PrivateResource：`status_bg.xml` 引用的 `notify_panel_notification_icon_bg`
        // 本模块自己就有（mdpi/hdpi/xhdpi/xxhdpi 四份），同模块资源优先，只是名字和
        // androidx.core 的私有资源撞了。
        disable +=
            listOf(
                "IconDuplicates",
                "IconDuplicatesConfig",
                "IconDipSize",
                "IconXmlAndPng",
                "IconLocation",
                "IconDensities",
                "IconNoDpi",
                "IconExtension",
                "PxUsage",
                "PrivateResource",
                "PluralsCandidate",
            )
    }

    // 允许发布到本地或私有 Maven 仓库：./gradlew publishToMavenLocal
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// 内置 Kotlin 不需要单独设 kotlin.compilerOptions.jvmTarget：
// 它默认跟随 android.compileOptions.targetCompatibility（这里保持 Java 11）。

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
