package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanClockDrawables

/** 小表盘指针投影的偏移量（原版 `delta_for_small_*_hand_shadow`，单位是原始像素）。 */
private const val SmallHandShadowDelta = 4f

/** 原版小表盘在 18:00–06:00 之间自动使用夜间底图与深色指针。 */
private const val NightStartHour = 18
private const val NightEndHour = 6

/**
 * 小表盘（列表行、世界时钟卡片里用）。
 *
 * 完全使用锤子时钟还原的原版小表盘位图（`small_blank_clock` 系列），
 * 指针锚点与投影偏移与原版 `SmallWorldClockView` 一致：
 * 时针 6.7/8、分针 7.2/8、秒针 6.5/8，投影沿指针方向偏移 4px。
 *
 * 与原版一样，[hour] 落在 18:00–06:00 时自动切换到夜间底图与深色指针。
 *
 * ```kotlin
 * SmartisanCompactClock(hour = 21, minute = 30, showSecondHand = true, size = 40.dp)
 * ```
 */
@Composable
fun SmartisanCompactClock(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    second: Int = 0,
    showSecondHand: Boolean = false,
    size: Dp = 40.dp,
    night: Boolean = hour >= NightStartHour || hour < NightEndHour,
) {
    val density = LocalDensity.current
    val face =
        rememberSmartisanDrawablePainter(
            if (night) SmartisanClockDrawables.SmallFaceBlack else SmartisanClockDrawables.SmallFace,
        )
    val hourHand =
        rememberSmartisanDrawablePainter(
            if (night) SmartisanClockDrawables.SmallHourHandBlack else SmartisanClockDrawables.SmallHourHand,
        )
    val minuteHand =
        rememberSmartisanDrawablePainter(
            if (night) SmartisanClockDrawables.SmallMinuteHandBlack else SmartisanClockDrawables.SmallMinuteHand,
        )
    val secondHand = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SmallSecondHand)
    val hourShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SmallHourHandShadow)
    val minuteShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SmallMinuteHandShadow)
    val secondShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SmallSecondHandShadow)
    val center =
        rememberSmartisanDrawablePainter(
            if (night) SmartisanClockDrawables.SmallHandCenterBlack else SmartisanClockDrawables.SmallHandCenter,
        )
    val centerMiddle = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SmallHandCenterMiddle)

    val faceWidthPx = face.intrinsicSize.width.takeIf { it > 0f } ?: with(density) { 52.dp.toPx() }
    val canvasScale = with(density) { size.toPx() } / faceWidthPx

    Canvas(modifier.size(size)) {
        scale(canvasScale, canvasScale, pivot = Offset.Zero) {
            val centerX = faceWidthPx / 2f
            val centerY = faceWidthPx / 2f
            drawCentered(face, faceWidthPx, faceWidthPx)

            val hourUnits = ((hour % 12f + minute / 60f) / 12f) * 60f
            drawClockHand(hourHand, hourShadow, hourUnits * 6f, 6.7f / 8f, centerX, centerY, SmallHandShadowDelta)
            drawClockHand(
                minuteHand,
                minuteShadow,
                (minute + second / 60f) * 6f,
                7.2f / 8f,
                centerX,
                centerY,
                SmallHandShadowDelta,
            )
            drawCentered(center, faceWidthPx, faceWidthPx)
            if (showSecondHand) {
                drawClockHand(secondHand, secondShadow, second * 6f, 6.5f / 8f, centerX, centerY, SmallHandShadowDelta)
            }
            drawCentered(centerMiddle, faceWidthPx, faceWidthPx)
        }
    }
}
