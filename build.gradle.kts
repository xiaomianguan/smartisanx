// 顶层构建脚本：只声明插件，具体配置放在各模块中。

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    // AGP 9 起 Kotlin 编译由 AGP 内置支持，不再声明 org.jetbrains.kotlin.android；
    // 这里只需要 Compose 编译器插件。
    alias(libs.plugins.kotlin.compose) apply false
}
