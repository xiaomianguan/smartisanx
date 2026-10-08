package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanContentColor
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.roundToInt

/**
 * 两端对齐的文本。
 *
 * 对应原版 `smartisanos.tablet.widget.SmartisanJustifyTextView`（锤子音乐，用在
 * `revone_alertdialog_message_layout.xml` 的弹窗正文，`lineSpacingMultiplier = 1.1`）。
 *
 * 原版重写了 `onDraw`：逐行取出文字，除最后一行、空行、以换行符结尾的行之外，
 * 其余每行都按 `(行宽 - 本行自然宽度) / (字符数 - 1)` 算出额外字距，
 * 再逐字 `canvas.drawText` 把整行拉满宽度，从而做到两端对齐。
 *
 * 段落首行（原版 `isFirstLineOfParagraph`）：如果一行以两个空格开头，
 * 原版会先画两个空格（`TWO_BLANK`），再把余下字符铺满整行，保证段首缩进不会被拉伸。
 * 反编译出的原版这里写的是 `str.substring(3)`，即在去掉两个空格之外又多丢了一个字符；
 * 这里按语义取 `substring(2)`，只去掉两个前导空格。
 *
 * Compose 侧的实现：
 * - 先用一个透明 [SmartisanText] 占位，让 Compose 正常的排版决定尺寸与换行位置；
 * - 再在同样大小的画布上按原版算法重绘两端对齐版本。
 *
 * 注意：两端对齐的行是逐字绘制的（与原版一致），长文本行会有多次测量开销。
 *
 * @param indentFirstLine 是否保留原版的「段首两空格」缩进处理。
 */
@Composable
fun SmartisanJustifyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body,
    color: Color = Color.Unspecified,
    indentFirstLine: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val contentColor = LocalSmartisanContentColor.current
    val resolvedColor =
        when {
            color != Color.Unspecified -> color
            contentColor != Color.Unspecified -> contentColor
            else -> colors.textPrimary
        }
    val measurer = rememberTextMeasurer()
    val measuredStyle = style.copy(color = resolvedColor)

    Box(modifier = modifier) {
        // 透明占位：只负责测量，可见文字全部画在下面的画布上。
        SmartisanText(
            text = text,
            style = style,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            drawSmartisanJustifiedText(
                measurer = measurer,
                text = text,
                style = measuredStyle,
                indentFirstLine = indentFirstLine,
            )
        }
    }
}

/** 按原版 `onDraw` 的逻辑，逐行绘制两端对齐文本。 */
private fun DrawScope.drawSmartisanJustifiedText(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    indentFirstLine: Boolean,
) {
    val viewWidth = size.width
    if (text.isEmpty() || viewWidth <= 0f) return
    val layout =
        measurer.measure(
            text = AnnotatedString(text),
            style = style,
            overflow = TextOverflow.Clip,
            softWrap = true,
            maxLines = Int.MAX_VALUE,
            constraints = Constraints(maxWidth = viewWidth.roundToInt().coerceAtLeast(1)),
        )
    // 原版 `needScale`：最后一行、空行、以换行符结尾的行都不拉伸。
    val justifiedLines =
        (0 until layout.lineCount).filter { line ->
            val start = layout.getLineStart(line)
            val end = layout.getLineEnd(line)
            line < layout.lineCount - 1 &&
                end - start >= 2 &&
                !text.substring(start, end).endsWith("\n")
        }
    if (justifiedLines.isEmpty()) {
        // 没有需要拉伸的行，直接按正常排版绘制。
        drawText(textLayoutResult = layout)
        return
    }
    for (line in 0 until layout.lineCount) {
        val start = layout.getLineStart(line)
        val end = layout.getLineEnd(line)
        val lineText = text.substring(start, end)
        if (lineText.isEmpty()) continue
        val baseline = layout.getLineBaseline(line)
        if (line in justifiedLines) {
            drawJustifiedLine(
                measurer = measurer,
                layout = layout,
                lineIndex = line,
                lineStart = start,
                lineText = lineText,
                baseline = baseline,
                style = style,
                viewWidth = viewWidth,
                indentFirstLine = indentFirstLine,
            )
        } else {
            val lineLayout =
                measurer.measure(
                    text = AnnotatedString(lineText),
                    style = style,
                    softWrap = false,
                    maxLines = 1,
                )
            drawText(
                textLayoutResult = lineLayout,
                topLeft = Offset(0f, baseline - lineLayout.firstBaseline),
            )
        }
    }
}

/**
 * 绘制单行两端对齐文字。
 *
 * 额外字距按原版公式 `(mViewWidth - 本行自然宽度) / (字符数 - 1)` 计算，
 * 每个字符画在「自然位置 + 序号 × 额外字距」上。
 */
private fun DrawScope.drawJustifiedLine(
    measurer: TextMeasurer,
    layout: TextLayoutResult,
    lineIndex: Int,
    lineStart: Int,
    lineText: String,
    baseline: Float,
    style: TextStyle,
    viewWidth: Float,
    indentFirstLine: Boolean,
) {
    // 原版 `isFirstLineOfParagraph`：以两个空格开头且长度大于 3 的段落首行。
    val paragraphStart = indentFirstLine && lineText.length > 3 && lineText.startsWith("  ")
    val indentChars = if (paragraphStart) 2 else 0
    val content = lineText.substring(indentChars)
    if (content.length < 2) {
        val singleLine =
            measurer.measure(
                text = AnnotatedString(lineText),
                style = style,
                softWrap = false,
                maxLines = 1,
            )
        drawText(
            textLayoutResult = singleLine,
            topLeft = Offset(0f, baseline - singleLine.firstBaseline),
        )
        return
    }
    val lineWidth = layout.getLineRight(lineIndex) - layout.getLineLeft(lineIndex)
    val extraSpacing = (viewWidth - lineWidth) / (content.length - 1)
    for (index in content.indices) {
        val naturalX =
            layout.getHorizontalPosition(
                offset = lineStart + indentChars + index,
                usePrimaryDirection = true,
            )
        val charLayout =
            measurer.measure(
                text = AnnotatedString(content.substring(index, index + 1)),
                style = style,
                softWrap = false,
                maxLines = 1,
            )
        drawText(
            textLayoutResult = charLayout,
            topLeft = Offset(naturalX + index * extraSpacing, baseline - charLayout.firstBaseline),
        )
    }
}
