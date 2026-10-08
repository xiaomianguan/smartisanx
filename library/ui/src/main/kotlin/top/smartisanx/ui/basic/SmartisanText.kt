package top.smartisanx.ui.basic

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanContentColor
import top.smartisanx.core.theme.LocalSmartisanTypography
import kotlin.math.roundToInt

/**
 * smartisanx 的基础文字组件。
 *
 * 不依赖 Material，直接使用 `BasicText`，因此可以在任何 Compose 工程里使用。
 * 颜色默认取 `LocalSmartisanContentColor`（由 [SmartisanSurface] 写入），
 * 未指定时回退到色板的 `textPrimary`。
 */
@Composable
fun SmartisanText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val resolved = rememberSmartisanTextStyle(style, color, fontWeight, fontSize, textAlign)
    BasicText(
        text = text,
        modifier = modifier,
        style = resolved,
        onTextLayout = onTextLayout,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines,
    )
}

/** [SmartisanText] 的 [AnnotatedString] 重载。 */
@Composable
fun SmartisanText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val resolved = rememberSmartisanTextStyle(style, color, fontWeight, fontSize, textAlign)
    BasicText(
        text = text,
        modifier = modifier,
        style = resolved,
        onTextLayout = onTextLayout,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines,
    )
}

/**
 * 还原原版 XML `TextView` 的字号行为：把 dp 字号按屏幕密度取整到物理像素后再换算回 sp。
 *
 * 三个复刻项目都保留了这个细节（锤子天气的 `PixelText`、锤子音乐的 `smartisanTextSize`），
 * 它让同一份设计在高密度屏上与原版逐像素对齐。
 */
@Composable
fun SmartisanPixelText(
    text: String,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalSmartisanTypography.current.body,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val density = LocalDensity.current
    val pixelSize = with(density) { size.toPx().roundToInt().toSp() }
    SmartisanText(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        fontSize = pixelSize,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Composable
internal fun rememberSmartisanTextStyle(
    style: TextStyle,
    color: Color,
    fontWeight: FontWeight?,
    fontSize: TextUnit,
    textAlign: TextAlign?,
): TextStyle {
    val colors = LocalSmartisanColors.current
    val contentColor = LocalSmartisanContentColor.current
    val resolvedColor =
        when {
            color != Color.Unspecified -> color
            contentColor != Color.Unspecified -> contentColor
            else -> colors.textPrimary
        }
    return style.copy(
        color = resolvedColor,
        fontWeight = fontWeight ?: style.fontWeight,
        fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
        textAlign = textAlign ?: style.textAlign,
    )
}

/** 等宽数字字号助手，用于时间、时长等需要对齐的数字。 */
@Composable
fun smartisanNumericStyle(fontSize: TextUnit = 15.sp): TextStyle =
    LocalSmartisanTypography.current.numeric.copy(fontSize = fontSize)
