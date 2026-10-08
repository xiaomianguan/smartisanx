package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 自动缩字以适配宽度的文本。
 *
 * 对应原版 `smartisanos.widget.FontFitTextView`，在锤子日历、短信、便签三个 APK 里使用
 * （例如 `menu_dialog_list_multi_item.xml` 的菜单项文字，原版字号
 * `@dimen/menu_dialog_item_text_size` = 18sp，单行居中）。
 *
 * 原版 `refitText(String, int)` 的算法：
 * 1. 用 `Paint.measureText` 量整行宽度，放得下就什么都不做；
 * 2. 放不下就在 `[mFitMinSize, 当前字号]` 之间二分（收敛精度 0.5px），
 *    取「仍然放得下」的最大字号；
 * 3. 字号定下来后，如果行高仍超过 View 高度，再用同样的二分把字号压到行高放得下为止。
 *
 * Compose 里没有 `Paint`，这里用 `TextMeasurer` 做同样的二分，精度阈值同样是 0.5px。
 * 原版默认最小字号 `mFitMinSize = 12f`。
 *
 * ```kotlin
 * SmartisanAutoFitText(
 *     text = "一个很长很长、默认字号放不下的菜单项",
 *     style = LocalSmartisanTypography.current.body.copy(fontSize = 18.sp),
 * )
 * ```
 *
 * @param minFontSize 允许缩到的最小字号，对应原版 `mFitMinSize`。
 * @param maxFontSize 起始字号，原版取 `getTextSize()`；不指定时用 [style] 的字号。
 */
@Composable
fun SmartisanAutoFitText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body,
    color: Color = Color.Unspecified,
    minFontSize: TextUnit = 12.sp,
    maxFontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val baseFontSize = if (maxFontSize != TextUnit.Unspecified) maxFontSize else style.fontSize
    val baseSizePx = with(density) { baseFontSize.toPx() }
    val minSizePx = with(density) { minFontSize.toPx() }

    BoxWithConstraints(modifier = modifier) {
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val availableHeightPx =
            if (maxHeight == Dp.Infinity) Float.MAX_VALUE else with(density) { maxHeight.toPx() }
        val fittedSizePx =
            remember(
                measurer,
                text,
                style,
                density,
                availableWidthPx,
                availableHeightPx,
                baseSizePx,
                minSizePx,
            ) {
                fitFontSizePx(
                    measurer = measurer,
                    text = text,
                    style = style,
                    density = density,
                    availableWidthPx = availableWidthPx,
                    availableHeightPx = availableHeightPx,
                    baseSizePx = baseSizePx,
                    minSizePx = minSizePx,
                )
            }
        SmartisanText(
            text = text,
            style = style,
            color = color,
            fontSize = with(density) { fittedSizePx.toSp() },
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
        )
    }
}

/** 按原版 `refitText` 的二分逻辑，算出放得下的最大字号（单位：px）。 */
private fun fitFontSizePx(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    density: Density,
    availableWidthPx: Float,
    availableHeightPx: Float,
    baseSizePx: Float,
    minSizePx: Float,
): Float {
    if (availableWidthPx <= 0f || text.isEmpty() || baseSizePx <= minSizePx) return baseSizePx

    fun measure(sizePx: Float): IntSize =
        measurer
            .measure(
                text = AnnotatedString(text),
                style = style.copy(fontSize = with(density) { sizePx.toSp() }),
                softWrap = false,
                maxLines = 1,
            ).size

    if (measure(baseSizePx).width <= availableWidthPx) return baseSizePx

    // 宽度二分：原版收敛条件是 textSize - mFitMinSize > 0.5f。
    var high = baseSizePx
    var low = minSizePx
    while (high - low > 0.5f) {
        val mid = (high + low) / 2f
        if (measure(mid).width >= availableWidthPx) high = mid else low = mid
    }
    var fitted = low

    // 高度兜底：原版字号定下来后还会按 View 高度再缩一次。
    if (fitted > minSizePx + 0.5f && availableHeightPx != Float.MAX_VALUE) {
        if (measure(fitted).height > availableHeightPx) {
            var highHeight = fitted
            var lowHeight = minSizePx
            while (highHeight - lowHeight > 0.5f) {
                val mid = (highHeight + lowHeight) / 2f
                if (measure(mid).height >= availableHeightPx) highHeight = mid else lowHeight = mid
            }
            fitted = lowHeight
        }
    }
    return fitted
}
