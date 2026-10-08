// smartisanx —— 锤子风格 Compose UI 组件库
// 结构参考 compose-miuix-ui/miuix：按职责拆分模块，示例应用单独成模块。

@file:Suppress("UnstableApiUsage")

rootProject.name = "smartisanx"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// 组件库
include(":library:core")
include(":library:ui")
include(":library:icons")

// 示例应用
include(":sample")
