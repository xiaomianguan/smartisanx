package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanCalculatorDrawables

/**
 * 锤子计算器的点阵显示屏。
 *
 * 对应原版 `CalculatorSmartisan` 的 `DisplayInputView` + `DisplayResultView`
 * （`res/layout-port-xxhdpi/main.xml` 里 `show_view` 的两行）。原版**不是用文字**显示算式，
 * 而是每个字符画一张位图，所以字形的「点阵感 / 段码感」直接来自素材：
 *
 * | 原版类 | 行 | 字形素材 | 画法（照抄原版 `onDraw`） |
 * | --- | --- | --- | --- |
 * | `DisplayInputView` | 输入行（点阵） | `dot_array_*`（38×46px） | 从右往左排，每格 38px + [SmartisanCalculatorDisplayDefaults.InputGlyphGap]；先整行平铺格子底图 `dot_array_bg`，再叠字形 |
 * | `DisplayResultView` | 结果行（段码） | `pic*`（90×114px） | 从右往左排，每格 90px；先铺 `pic_bg` 再叠字形，每三位数字插一个 `comma`；小数点 `point` 贴格子右下角、不占格；左侧空位继续铺 `pic_bg` |
 *
 * 尺寸全部来自原版 `res/values/dimens.xml`（横向留白取 `values-sw360dp` 的取值，
 * 因为绝大多数手机命中它），见 [SmartisanCalculatorDisplayDefaults]。
 *
 * 与原版的差异（都是显示层的取舍，不影响观感）：
 * - 原版输入行可以用手指左右拖动（`DisplayInputView.movePosition`）；本组件固定右对齐，
 *   超出宽度的部分从左侧裁掉 —— 也就是原版 `an = 0` 的初始状态；
 * - 原版结果行位数超长时由计算器逻辑先截断（`Utils.getDisplayByValueAndLength`），
 *   本组件按传入的字符串画；
 * - 原版每格字形的 alpha 走 `Map<String, Float>` 支持逐字动画（R2 版里恒为 1），本组件不做。
 *
 * ```kotlin
 * // 显示区（typebox 底图）里放这个组件，靠底对齐
 * Box(
 *     Modifier
 *         .fillMaxWidth()
 *         .height(SmartisanCalculatorDisplayDefaults.TypeBoxHeight)
 *         .padding(
 *             top = SmartisanCalculatorDisplayDefaults.TypeBoxTopMargin,
 *             start = SmartisanCalculatorDisplayDefaults.TypeBoxHorizontalMargin,
 *             end = SmartisanCalculatorDisplayDefaults.TypeBoxHorizontalMargin,
 *         )
 *         .smartisanDrawableBackground(R.drawable.typebox),
 *     contentAlignment = Alignment.BottomCenter,
 * ) {
 *     SmartisanCalculatorDisplay(expression = "12×34", result = "408")
 * }
 * ```
 */
@Composable
fun SmartisanCalculatorDisplay(
    expression: String,
    result: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    contentDescription: String? = null,
) {
    val inputGlyphs = rememberCalculatorGlyphPainters(SmartisanCalculatorDrawables.InputGlyphs)
    val resultGlyphs = rememberCalculatorGlyphPainters(SmartisanCalculatorDrawables.ResultGlyphs)
    val inputCell = rememberSmartisanDrawablePainter(SmartisanCalculatorDrawables.InputCell)
    val resultCell = rememberSmartisanDrawablePainter(SmartisanCalculatorDrawables.ResultCell)
    val error = rememberSmartisanDrawablePainter(SmartisanCalculatorDrawables.Error)
    val comma = rememberSmartisanDrawablePainter(SmartisanCalculatorDrawables.Comma)
    val point = rememberSmartisanDrawablePainter(SmartisanCalculatorDrawables.Point)

    val density = LocalDensity.current
    val inputGap = with(density) { SmartisanCalculatorDisplayDefaults.InputGlyphGap.toPx() }
    val commaMargin = with(density) { SmartisanCalculatorDisplayDefaults.CommaMargin.toPx() }
    // 行高取素材本身的高度（原版 onMeasure 就是 setMeasuredDimension(宽, 字形高)）。
    val inputHeight = with(density) { inputCell.intrinsicSize.height.toDp() }
    val resultHeight = with(density) { resultCell.intrinsicSize.height.toDp() }

    Column(
        modifier =
            modifier.semantics {
                contentDescription?.let { this.contentDescription = it }
            },
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SmartisanCalculatorDisplayDefaults.InputHorizontalMargin)
                .height(inputHeight)
                .clipToBounds()
                .semantics { this.contentDescription = expression },
        ) {
            drawDotMatrixLine(expression, inputGlyphs, inputCell, inputGap)
        }
        Spacer(Modifier.height(SmartisanCalculatorDisplayDefaults.InputBottomMargin))
        Canvas(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SmartisanCalculatorDisplayDefaults.ResultHorizontalMargin)
                .height(resultHeight)
                .clipToBounds()
                .semantics { this.contentDescription = contentDescription ?: result },
        ) {
            drawSegmentedLine(
                text = result,
                glyphs = resultGlyphs,
                cell = resultCell,
                point = point,
                comma = comma,
                error = error,
                commaMargin = commaMargin,
                isError = isError,
            )
        }
        Spacer(Modifier.height(SmartisanCalculatorDisplayDefaults.ResultBottomMargin))
    }
}

/**
 * 原版 `res/values/dimens.xml` 里显示屏那一组尺寸。
 *
 * | 常量 | 原版资源 | 值 |
 * | --- | --- | --- |
 * | [InputHorizontalMargin] | `display_input_view_margin_right` | 19dp |
 * | [ResultHorizontalMargin] | `display_result_view_margin_right` | 16dp |
 * | [InputBottomMargin] | `input_field_bottom` | 20dp |
 * | [ResultBottomMargin] | `result_field_bottom` | 26dp |
 * | [InputGlyphGap] | `display_input_gap` | 3dp |
 * | [CommaMargin] | `result_number_margin` | 4dp（横屏 `values-land` 是 3dp） |
 * | [TypeBoxHorizontalMargin] | `typebox_margin_left` / `_right` | 12dp（`values-sw360dp`；原版默认 14.4dp） |
 * | [TypeBoxTopMargin] | `top_marginTop` | 30dp |
 * | [TypeBoxHeight] | `typebox_height` | 164dp |
 */
object SmartisanCalculatorDisplayDefaults {
    /** 输入行左右留白，原版 `display_input_view_margin_right` = 19dp。 */
    val InputHorizontalMargin: Dp = 19.dp

    /** 结果行左右留白，原版 `display_result_view_margin_right` = 16dp。 */
    val ResultHorizontalMargin: Dp = 16.dp

    /** 输入行与结果行之间的间距，原版 `input_field_bottom` = 20dp。 */
    val InputBottomMargin: Dp = 20.dp

    /** 结果行下方的间距，原版 `result_field_bottom` = 26dp。 */
    val ResultBottomMargin: Dp = 26.dp

    /** 输入行点阵格子之间的间隙，原版 `display_input_gap` = 3dp。 */
    val InputGlyphGap: Dp = 3.dp

    /** 千位分隔符两侧的余量，原版 `result_number_margin` = 4dp（横屏 3dp）。 */
    val CommaMargin: Dp = 4.dp

    /** 显示区（`typebox`）左右留白，原版 `typebox_margin_left` / `_right`：12dp（`values-sw360dp`）。 */
    val TypeBoxHorizontalMargin: Dp = 12.dp

    /** 显示区距顶部的留白，原版 `top_marginTop` = 30dp。 */
    val TypeBoxTopMargin: Dp = 30.dp

    /** 显示区高度，原版 `typebox_height` = 164dp。 */
    val TypeBoxHeight: Dp = 164.dp
}

/**
 * 把整张字形表都取成 painter。
 *
 * 原版是在后台线程一次性 `BitmapFactory.decodeResource` 出全部字形（`DisplayInputView.a()` /
 * `DisplayResultView.a()`），这里同理：字形表是常量，`remember` 之后每帧只是复用。
 */
@Composable
private fun rememberCalculatorGlyphPainters(glyphs: Map<Char, Int>): Map<Char, Painter> {
    val entries = remember(glyphs) { glyphs.entries.map { it.key to it.value } }
    val painters = entries.map { (char, res) -> char to rememberSmartisanDrawablePainter(res) }
    return remember(painters) { painters.toMap() }
}

/** 照抄 `DisplayInputView.onDraw`：先整行平铺点阵格子，再从右往左叠字形。 */
private fun DrawScope.drawDotMatrixLine(
    text: String,
    glyphs: Map<Char, Painter>,
    cell: Painter,
    gap: Float,
) {
    val cellWidth = cell.intrinsicSize.width
    // 原版：i2 = width; while (i2 >= width2) { i3 = i2 - width2; draw(bg, i3); i2 = i3 - gap }
    var x = size.width
    while (x >= cellWidth) {
        x -= cellWidth
        drawGlyph(cell, x, 0f)
        x -= gap
    }
    // 原版：f = width; for (i = len - 1; i >= 0; i--) { draw(glyph, f - 字形宽); f -= gap + width2 }
    var right = size.width
    for (index in text.indices.reversed()) {
        val glyph = glyphs[text[index]]
        if (glyph != null) drawGlyph(glyph, right - glyph.intrinsicSize.width, 0f)
        right -= cellWidth + gap
    }
}

/** 照抄 `DisplayResultView.onDraw`：从右往左每格铺 `pic_bg` + 字形，每三位插千位分隔符。 */
private fun DrawScope.drawSegmentedLine(
    text: String,
    glyphs: Map<Char, Painter>,
    cell: Painter,
    point: Painter,
    comma: Painter,
    error: Painter,
    commaMargin: Float,
    isError: Boolean,
) {
    val cellWidth = cell.intrinsicSize.width
    val centeredTop = (size.height - cell.intrinsicSize.height) / 2f
    if (isError) {
        // 原版错误态：把 pic_error（5 格宽）贴右画，左边继续铺格子。
        val left = size.width - error.intrinsicSize.width
        drawGlyph(error, left, (size.height - error.intrinsicSize.height) / 2f)
        fillCells(cell, cellWidth, left, centeredTop)
        return
    }
    val hasDot = text.contains('.')
    val hasExponent = text.any { it == 'e' || it == 'E' }
    var afterDot = !hasDot
    var group = 0
    var right = size.width
    for (index in text.indices.reversed()) {
        val char = text[index]
        if (char == '.') {
            // 原版：小数点贴当前格子的右下角，且不占格。
            drawGlyph(point, right - point.intrinsicSize.width, size.height - point.intrinsicSize.height)
            afterDot = true
            continue
        }
        val glyph = glyphs[char]
        val x = right - cellWidth
        drawGlyph(cell, x, centeredTop)
        if (glyph != null) drawGlyph(glyph, x, centeredTop)
        right -= cellWidth
        // 原版：画过小数点之后才插分隔符；带指数不插；首位的负号不算一位。
        if (afterDot && !hasExponent && !(char == '-' && index == 0)) {
            group++
            if (group == 4) {
                drawGlyph(comma, x + cellWidth + (commaMargin - comma.intrinsicSize.width) / 2f, centeredTop)
                group = 1
            }
        }
    }
    fillCells(cell, cellWidth, right, centeredTop)
}

/** 从右往左把剩下的格子铺满（原版 `onDraw` 结尾的 while 循环）。 */
private fun DrawScope.fillCells(cell: Painter, cellWidth: Float, from: Float, top: Float) {
    var x = from
    while (x >= cellWidth) {
        x -= cellWidth
        drawGlyph(cell, x, top)
    }
}

/** 按素材自身尺寸画一张字形（原版是 `canvas.drawBitmap(bitmap, x, y, paint)`）。 */
private fun DrawScope.drawGlyph(painter: Painter, x: Float, y: Float) {
    translate(x, y) {
        with(painter) { draw(size = painter.intrinsicSize) }
    }
}
