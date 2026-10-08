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

/**
 * 主题模式：跟随系统、始终浅色、始终深色。
 *
 * **注意：深色模式是实验性特性。**
 * 原版 Smartisan OS 只有浅色一套设计，深色由三个复刻项目自行新增；
 * 而且原版图形资源里只有约 19%（1008 个 drawable 中的 194 个）带夜间变体，
 * 颜色状态列表则完全没有夜间版本。因此深色下的还原度不如浅色，
 * 需要完全还原原版观感时请以浅色为准。
 */
enum class SmartisanColorSchemeMode {
    /** 跟随系统深浅色设置。 */
    System,

    /** 始终使用浅色色板。 */
    Light,

    /**
     * 始终使用深色色板。
     *
     * 实验性：原版没有深色模式，且部分原版资源没有夜间变体。
     */
    Dark,
}

/**
 * 主题控制器，用于在运行时切换深浅色。
 *
 * 深色模式是实验性特性，原因见 [SmartisanColorSchemeMode]。
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

    /** 当前是否处于深色（实验性，见 [SmartisanColorSchemeMode]）。 */
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

/**
 * 当前 smartisanx 主题是否为深色；`null` 表示未包裹主题、跟随系统。
 *
 * 原版图形资源的夜间变体放在 `drawable-night` / `values-night`，
 * 而 Android 只按系统 uiMode 选择。应用内切换深浅色时两者可能不一致，
 * 组件据此强制按应用主题解析资源（见 `smartisanThemedResources`）。
 */
val LocalSmartisanDarkOverride: ProvidableCompositionLocal<Boolean?> =
    staticCompositionLocalOf { null }

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
        LocalSmartisanDarkOverride provides !colors.isLight,
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
        LocalSmartisanDarkOverride provides !colors.isLight,
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
