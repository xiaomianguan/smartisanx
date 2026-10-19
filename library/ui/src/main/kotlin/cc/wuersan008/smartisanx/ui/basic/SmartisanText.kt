package cc.wuersan008.smartisanx.ui.basic

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanFonts
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanContentColor
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import kotlin.math.roundToInt

/**
 * 为 true 时，[SmartisanText] 的省略号（[TextOverflow.Ellipsis]，带 `maxLines` 时）
 * 换成末行末端的渐隐：按段落方向淡出到透明，与底色无关（遮罩的是文字本身的 alpha）。
 * 默认 false，保持省略号。
 */
val LocalSmartisanTextFadeOverflow = staticCompositionLocalOf { false }

/** 文字溢出时，末行朝书写方向的末端在 [width] 内淡出。 */
private fun Modifier.fadeOverflow(width: androidx.compose.ui.unit.Dp = 32.dp, layout: () -> TextLayoutResult?): Modifier =
    graphicsLayer {
        // 只有溢出时才离屏合成，其余文字照常绘制。
        compositingStrategy = if (layout()?.hasVisualOverflow == true) CompositingStrategy.Offscreen else CompositingStrategy.Auto
    }.drawWithContent {
        drawContent()
        val l = layout()?.takeIf { it.hasVisualOverflow } ?: return@drawWithContent
        val line = l.lineCount - 1
        val top = l.getLineTop(line)
        val w = minOf(width.toPx(), size.width)
        val rtl = l.getParagraphDirection(l.getLineStart(line)) == ResolvedTextDirection.Rtl
        val start = if (rtl) 0f else size.width - w
        drawRect(
            Brush.horizontalGradient(
                if (rtl) listOf(Color.Transparent, Color.Black) else listOf(Color.Black, Color.Transparent),
                startX = start, endX = start + w,
            ),
            topLeft = Offset(start, top),
            size = Size(w, l.getLineBottom(line) - top),
            blendMode = BlendMode.DstIn,
        )
    }

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
    val fade = LocalSmartisanTextFadeOverflow.current && overflow == TextOverflow.Ellipsis && maxLines != Int.MAX_VALUE
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicText(
        text = text,
        modifier = if (fade) modifier.fadeOverflow { layout } else modifier,
        style = resolved,
        onTextLayout = {
            if (fade) layout = it
            onTextLayout?.invoke(it)
        },
        overflow = if (fade) TextOverflow.Clip else overflow,
        // 单行不折行：截断落在字中间，渐隐才看得出来（折行会在词边整词丢掉）。
        softWrap = !(fade && maxLines == 1),
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
    val fade = LocalSmartisanTextFadeOverflow.current && overflow == TextOverflow.Ellipsis && maxLines != Int.MAX_VALUE
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicText(
        text = text,
        modifier = if (fade) modifier.fadeOverflow { layout } else modifier,
        style = resolved,
        onTextLayout = {
            if (fade) layout = it
            onTextLayout?.invoke(it)
        },
        overflow = if (fade) TextOverflow.Clip else overflow,
        // 单行不折行：截断落在字中间，渐隐才看得出来（折行会在词边整词丢掉）。
        softWrap = !(fade && maxLines == 1),
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
    // Styles built inline (dialog titles and buttons, tab groups, tips...) name
    // no font; they get the theme's text font instead of the system default.
    val fontFamily = style.fontFamily ?: LocalSmartisanFonts.current.text
    return style.copy(
        color = resolvedColor,
        fontFamily = fontFamily,
        fontWeight = fontWeight ?: style.fontWeight,
        fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
        textAlign = textAlign ?: style.textAlign,
    )
}

/** 等宽数字字号助手，用于时间、时长等需要对齐的数字。 */
@Composable
fun smartisanNumericStyle(fontSize: TextUnit = 15.sp): TextStyle =
    LocalSmartisanTypography.current.numeric.copy(fontSize = fontSize)
