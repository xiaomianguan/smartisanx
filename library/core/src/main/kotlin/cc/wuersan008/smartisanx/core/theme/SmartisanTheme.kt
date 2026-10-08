package cc.wuersan008.smartisanx.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/** 主题模式：跟随系统、始终浅色、始终深色。 */
enum class SmartisanColorSchemeMode {
    /** 跟随系统深浅色设置。 */
    System,

    /** 始终使用浅色色板。 */
    Light,

    /** 始终使用深色色板。 */
    Dark,
}

/**
 * 主题控制器，用于在运行时切换深浅色。
 *
 * ```kotlin
 * val controller = rememberSmartisanThemeController()
 * SmartisanTheme(controller) {
 *     // controller.colorSchemeMode = SmartisanColorSchemeMode.Dark
 * }
 * ```
 */
@Stable
class ThemeController(
    colorSchemeMode: SmartisanColorSchemeMode = SmartisanColorSchemeMode.System,
) {
    /** 当前主题模式，可直接赋值以切换深浅色。 */
    var colorSchemeMode: SmartisanColorSchemeMode by mutableStateOf(colorSchemeMode)

    /** 当前是否处于深色。 */
    @Composable
    @ReadOnlyComposable
    fun isDark(): Boolean =
        when (colorSchemeMode) {
            SmartisanColorSchemeMode.System -> isSystemInDarkTheme()
            SmartisanColorSchemeMode.Light -> false
            SmartisanColorSchemeMode.Dark -> true
        }

    /** 当前生效的色板。 */
    @Composable
    @ReadOnlyComposable
    fun colors(): SmartisanColors = if (isDark()) darkSmartisanColors() else lightSmartisanColors()
}

/** 创建一个 [ThemeController]，默认跟随系统。 */
@Composable
fun rememberSmartisanThemeController(
    colorSchemeMode: SmartisanColorSchemeMode = SmartisanColorSchemeMode.System,
): ThemeController =
    remember(colorSchemeMode) { ThemeController(colorSchemeMode) }

/** 由 [SmartisanTheme] 提供的当前控制器。 */
val LocalSmartisanThemeController: ProvidableCompositionLocal<ThemeController?> =
    staticCompositionLocalOf { null }

/** 默认色板：跟随系统深浅色。 */
@Composable
internal fun defaultSmartisanColors(): SmartisanColors =
    if (isSystemInDarkTheme()) darkSmartisanColors() else lightSmartisanColors()

/**
 * smartisanx 主题。
 *
 * 提供色板、文字样式与形状，必须包裹所有 smartisanx 组件：
 *
 * ```kotlin
 * SmartisanTheme {
 *     SmartisanTitleBar(title = "锤子风格")
 * }
 * ```
 */
@Composable
fun SmartisanTheme(
    colors: SmartisanColors = defaultSmartisanColors(),
    typography: SmartisanTypography = SmartisanTypography(),
    shapes: SmartisanShapes = SmartisanShapes(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSmartisanColors provides colors,
        LocalSmartisanTypography provides typography,
        LocalSmartisanShapes provides shapes,
        content = content,
    )
}

/**
 * 使用 [ThemeController] 的 [SmartisanTheme] 重载，支持运行时切换深浅色。
 */
@Composable
fun SmartisanTheme(
    controller: ThemeController,
    typography: SmartisanTypography = SmartisanTypography(),
    shapes: SmartisanShapes = SmartisanShapes(),
    content: @Composable () -> Unit,
) {
    val colors = controller.colors()
    CompositionLocalProvider(
        LocalSmartisanColors provides colors,
        LocalSmartisanTypography provides typography,
        LocalSmartisanShapes provides shapes,
        LocalSmartisanThemeController provides controller,
        content = content,
    )
}

/** 当前色板，等价于 `LocalSmartisanColors.current`。 */
val smartisanColors: SmartisanColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSmartisanColors.current

/** 当前文字样式。 */
val smartisanTypography: SmartisanTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalSmartisanTypography.current

/** 当前形状。 */
val smartisanShapes: SmartisanShapes
    @Composable
    @ReadOnlyComposable
    get() = LocalSmartisanShapes.current
