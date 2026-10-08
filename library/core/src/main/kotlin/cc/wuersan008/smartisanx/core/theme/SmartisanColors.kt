package cc.wuersan008.smartisanx.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * smartisanx 的语义色板。
 *
 * 色值来自锤子音乐、锤子天气、锤子时钟三个复刻项目的实际取色，
 * 去重后统一为一套语义命名，浅色与深色各一份实现。
 *
 * 深色基线沿用三个项目已经互相校准过的炭灰色板：
 * 页底 `#25282D`、标题栏 `#292C31`、卡片 `#34373C`、较高表面 `#41464D`。
 */
@Immutable
class SmartisanColors(
    /** 页面底色。 */
    val pageBackground: Color,
    /** 卡片、列表、对话框的常规表面。 */
    val surface: Color,
    /** 比表面更高一层的分组背景。 */
    val surfaceRaised: Color,
    /** 按压态表面。 */
    val surfacePressed: Color,
    /** 禁用态表面。 */
    val surfaceDisabled: Color,
    /** 标题栏底色。 */
    val titleBarBackground: Color,
    /** 列表分隔线。 */
    val divider: Color,
    /** 分组内部更淡的分隔线。 */
    val rowDivider: Color,
    /** 一级文字。 */
    val textPrimary: Color,
    /** 二级文字。 */
    val textSecondary: Color,
    /** 三级文字、分组标题。 */
    val textTertiary: Color,
    /** 禁用文字。 */
    val textDisabled: Color,
    /** 输入框提示文字。 */
    val textHint: Color,
    /** 品牌强调色（锤子红）。 */
    val accent: Color,
    /** 强调色按压态。 */
    val accentPressed: Color,
    /** 强调色禁用态。 */
    val accentDisabled: Color,
    /** 强调色之上的文字色。 */
    val onAccent: Color,
    /** 链接、可点文字。 */
    val link: Color,
    /** 链接按压态。 */
    val linkPressed: Color,
    /** 成功 / 开关指示色（原版开关的绿色）。 */
    val success: Color,
    /** 警示色。 */
    val warning: Color,
    /** 多选列表的持续选中底色。 */
    val selectionBackground: Color,
    /** 列表按压时的蓝色高亮底色，原版点击反馈。 */
    val pressedHighlight: Color,
    /** 蓝色高亮底色上的文字色。 */
    val onPressedHighlight: Color,
    /** 开关轨道底色。 */
    val switchTrack: Color,
    /** 开关轨道描边。 */
    val switchTrackStroke: Color,
    /** 开关滑块。 */
    val switchKnob: Color,
    /** 开关开启指示色。 */
    val switchIndicator: Color,
    /** 滚动条滑块。 */
    val scrollbarThumb: Color,
    /** 弹层遮罩。 */
    val scrim: Color,
    /** 是否为深色色板。 */
    val isLight: Boolean,
)

/** 浅色色板，对应三个项目的日间视觉。 */
fun lightSmartisanColors(): SmartisanColors =
    SmartisanColors(
        pageBackground = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceRaised = Color(0xFFF7F8F9),
        surfacePressed = Color(0xFFECECEC),
        surfaceDisabled = Color(0xFFF2F2F2),
        titleBarBackground = Color(0xFFFFFFFF),
        divider = Color(0xFFE9E9E9),
        rowDivider = Color(0xFFF2F2F2),
        textPrimary = Color(0xCC000000),
        textSecondary = Color(0x9A000000),
        textTertiary = Color(0x66000000),
        textDisabled = Color(0x4C000000),
        textHint = Color(0xFFDBDBDB),
        accent = Color(0xFFE64040),
        accentPressed = Color(0xFFC14352),
        accentDisabled = Color(0x66E64040),
        onAccent = Color(0xFFFFFFFF),
        link = Color(0xFF5E80D0),
        linkPressed = Color(0xFF8A8A8A),
        success = Color(0xFF72B27E),
        warning = Color(0xFFE65C53),
        selectionBackground = Color(0xFFE5EEFF),
        pressedHighlight = Color(0xFF4A69B3),
        onPressedHighlight = Color(0xFFFFFFFF),
        switchTrack = Color(0xFFF4F4F4),
        switchTrackStroke = Color(0x1C000000),
        switchKnob = Color(0xFFFFFFFF),
        switchIndicator = Color(0xFF72B27E),
        scrollbarThumb = Color(0x33000000),
        scrim = Color(0x8A000000),
        isLight = true,
    )

/**
 * 深色色板，沿用锤子天气复刻的炭灰色基线。
 *
 * **实验性**：原版 Smartisan OS 没有深色模式，这套炭灰色板由复刻项目新增；
 * 并且原版图形资源里只有约 19% 带夜间变体，深色下的还原度不如浅色。
 */
fun darkSmartisanColors(): SmartisanColors =
    SmartisanColors(
        pageBackground = Color(0xFF25282D),
        surface = Color(0xFF34373C),
        surfaceRaised = Color(0xFF41464D),
        surfacePressed = Color(0xFF484D54),
        surfaceDisabled = Color(0xFF30343A),
        titleBarBackground = Color(0xFF292C31),
        divider = Color(0xFF3A3D42),
        rowDivider = Color(0xFF404348),
        textPrimary = Color(0xFFF2F2F2),
        textSecondary = Color(0xFFD2D3D5),
        textTertiary = Color(0xFF9FA1A4),
        textDisabled = Color(0xFF717377),
        textHint = Color(0xFF85878A),
        accent = Color(0xFFE64040),
        accentPressed = Color(0xFFFF5A5A),
        accentDisabled = Color(0x66E64040),
        onAccent = Color(0xFFFFFFFF),
        link = Color(0xFFFF7839),
        linkPressed = Color(0xFFFF9A6D),
        success = Color(0xFF72B27E),
        warning = Color(0xFFFF7433),
        selectionBackground = Color(0xFF26384F),
        pressedHighlight = Color(0xFF4A69B3),
        onPressedHighlight = Color(0xFFFFFFFF),
        switchTrack = Color(0xFF3A3F47),
        switchTrackStroke = Color(0x33FFFFFF),
        switchKnob = Color(0xFFF2F2F2),
        switchIndicator = Color(0xFF72B27E),
        scrollbarThumb = Color(0x40FFFFFF),
        scrim = Color(0x99000000),
        isLight = false,
    )
/** 兜底色板：未包裹 [SmartisanTheme] 时用于独立预览的浅色色板。 */
val DefaultSmartisanColors: SmartisanColors = lightSmartisanColors()

/**
 * 当前 [SmartisanColors]。
 *
 * 必须由 [SmartisanTheme] 提供；未包裹主题就读取会抛出明确异常，
 * 避免组件在深色模式下悄悄沿用浅色色板。
 */
val LocalSmartisanColors: ProvidableCompositionLocal<SmartisanColors> =
    staticCompositionLocalOf {
        error("未找到 SmartisanColors：请用 SmartisanTheme { ... } 包裹你的界面。")
    }

/** 内容默认色，由 SmartisanSurface 之类的容器写入。 */
val LocalSmartisanContentColor: ProvidableCompositionLocal<Color> =
    staticCompositionLocalOf { Color.Unspecified }
