package top.smartisanx.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * smartisanx 的文字样式。
 *
 * 字号取自三个项目的实际资源：标题栏 20sp、弹窗标题 13.5sp、弹窗按钮 17sp、
 * 列表一级 15sp、列表二级 12.5sp、分组标题 13.5sp。
 */
@Immutable
class SmartisanTypography(
    /** 标题栏标题。 */
    val titleBar: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp,
    ),
    /** 页面大标题。 */
    val title: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 26.sp,
    ),
    /** 正文。 */
    val body: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
    ),
    /** 列表一级文字。 */
    val listItemPrimary: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    /** 列表二级文字。 */
    val listItemSecondary: TextStyle = TextStyle(
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 17.sp,
    ),
    /** 分组标题。 */
    val sectionTitle: TextStyle = TextStyle(
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    ),
    /** 按钮文字。 */
    val button: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 19.sp,
    ),
    /** 弹窗按钮文字。 */
    val dialogButton: TextStyle = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 23.sp,
    ),
    /** 弹窗标题文字。 */
    val dialogTitle: TextStyle = TextStyle(
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp,
    ),
    /** 说明、页脚文字。 */
    val caption: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    /** 数字（等宽数字，避免时间跳动）。 */
    val numeric: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum",
    ),
    /** 机械感大号数字，用于时钟、计时器。 */
    val displayNumeric: TextStyle = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 56.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
)

/** 当前 [SmartisanTypography]。 */
val LocalSmartisanTypography: ProvidableCompositionLocal<SmartisanTypography> =
    staticCompositionLocalOf { SmartisanTypography() }
