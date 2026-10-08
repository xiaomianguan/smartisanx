package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * 秒针的单调计数。
 *
 * 原版秒针每跳一次都会回弹，且 59→0 时必须继续向前扫而不是倒着退回去。
 * 这里用累加的秒数代替 `LocalTime.second`，让动画目标值始终单调递增。
 */
@Composable
internal fun rememberSecondHandSweep(second: Int): Int {
    var lastSecond by remember { mutableIntStateOf(second) }
    var sweep by remember { mutableIntStateOf(second) }
    LaunchedEffect(second) {
        if (second != lastSecond) {
            sweep += (second - lastSecond + 60) % 60
            lastSecond = second
        }
    }
    return sweep
}

/**
 * 按原版 `AnalogClockHandsView.onDraw` 的顺序叠放所有图层。
 *
 * 顺序（与原版一致）：闹钟耳朵 → 表盘 → 夜间表盘 → 刻度 → 数字 →
 * 时针/分针 → 中心轴 → 秒针 → 中心轴中层。
 */
internal fun DrawScope.drawSmartisanClockFace(
    baseWidth: Float,
    baseHeight: Float,
    densityScale: Float,
    time: LocalTime,
    secondDegrees: Float,
    showSecondHand: Boolean,
    showEars: Boolean,
    showNumerals: Boolean,
    darkHands: Boolean,
    face: Painter,
    nightFace: Painter,
    degree: Painter,
    numeralTop: Painter,
    numeralRight: Painter,
    hourHand: Painter,
    minuteHand: Painter,
    secondHand: Painter,
    hourShadow: Painter,
    minuteShadow: Painter,
    secondShadow: Painter,
    handCenter: Painter,
    handCenterMiddle: Painter,
    earLeft: Painter,
    earRight: Painter,
) {
    fun dp(value: Float): Float = value * densityScale

    val centerX = baseWidth / 2f
    val centerY = baseHeight / 2f
    val shadowDelta = dp(6f)

    // 1. 闹钟「耳朵」：原版把左右耳朵分别放在 dp(61) / dp(83) 处。
    if (showEars) {
        drawPainterAt(earLeft, dp(61f), dp(83f))
        drawPainterAt(earRight, baseWidth - dp(61f) - earRight.intrinsicSize.width, dp(83f))
    }

    // 2. 表盘：日间底图始终绘制，深色模式再叠一张原版夜间底图。
    drawCentered(face, baseWidth, baseHeight)
    if (darkHands) {
        drawCentered(nightFace, baseWidth, baseHeight)
    }

    // 3. 刻度。
    drawCentered(degree, baseWidth, baseHeight)

    // 4. 数字：顶部「12」距顶 108dp，右侧「3」距右 84.6dp。
    if (showNumerals) {
        drawPainterAt(
            numeralTop,
            (baseWidth - numeralTop.intrinsicSize.width) / 2f,
            dp(108f),
        )
        drawPainterAt(
            numeralRight,
            baseWidth - dp(84.6f) - numeralRight.intrinsicSize.width,
            (baseHeight - numeralRight.intrinsicSize.height) / 2f,
        )
    }

    // 5. 指针：锚点与原版一致（时针/分针 6.9/8，秒针 6.5/8）。
    val hourDegrees = (time.hour % 12) * 30f + time.minute * 0.5f
    val minuteDegrees = time.minute * 6f + time.second * 0.1f
    drawClockHand(hourHand, hourShadow, hourDegrees, 6.9f / 8f, centerX, centerY, shadowDelta)
    drawClockHand(minuteHand, minuteShadow, minuteDegrees, 6.9f / 8f, centerX, centerY, shadowDelta)
    drawPainterAt(handCenter, centerX - handCenter.intrinsicSize.width / 2f, centerY - handCenter.intrinsicSize.height / 2f)
    if (showSecondHand) {
        drawClockHand(secondHand, secondShadow, secondDegrees, 6.5f / 8f, centerX, centerY, shadowDelta)
    }
    drawPainterAt(
        handCenterMiddle,
        centerX - handCenterMiddle.intrinsicSize.width / 2f,
        centerY - handCenterMiddle.intrinsicSize.height / 2f,
    )
}

/** 把 painter 按位图固有像素尺寸画在 (x, y)。 */
internal fun DrawScope.drawPainterAt(
    painter: Painter,
    x: Float,
    y: Float,
    alpha: Float = 1f,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.width <= 0f || intrinsic.height <= 0f) return
    translate(x, y) { with(painter) { draw(intrinsic, alpha = alpha) } }
}

/** 把 painter 居中画在基准画布上。 */
internal fun DrawScope.drawCentered(
    painter: Painter,
    baseWidth: Float,
    baseHeight: Float,
    alpha: Float = 1f,
) {
    val intrinsic = painter.intrinsicSize
    drawPainterAt(painter, (baseWidth - intrinsic.width) / 2f, (baseHeight - intrinsic.height) / 2f, alpha)
}

/**
 * 画一根指针及其投影。
 *
 * 原版先把画布绕中心旋转，再绘制「投影」和「指针」，两者都定位在
 * `centerX - w/2`、`centerY - h * anchor`；投影再沿指针方向偏移 `delta`。
 */
internal fun DrawScope.drawClockHand(
    hand: Painter,
    shadow: Painter,
    degrees: Float,
    anchor: Float,
    centerX: Float,
    centerY: Float,
    shadowDelta: Float,
    alpha: Float = 1f,
) {
    val intrinsic = hand.intrinsicSize
    if (intrinsic.width <= 0f || intrinsic.height <= 0f || alpha <= 0f) return
    val handX = centerX - intrinsic.width / 2f
    val handY = centerY - intrinsic.height * anchor
    val radians = Math.toRadians(degrees.toDouble())
    rotate(degrees, pivot = Offset(centerX, centerY)) {
        drawPainterAt(
            shadow,
            handX + (sin(radians) * shadowDelta).toFloat(),
            handY + (cos(radians) * shadowDelta).toFloat(),
            alpha,
        )
        drawPainterAt(hand, handX, handY, alpha)
    }
}
