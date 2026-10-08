package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanClockDrawables
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** 原版表盘的基准画布：宽 360dp、高 400dp。 */
private val ClockBaseWidth = 360.dp

/** 原版表盘的基准画布高度。 */
private val ClockBaseHeight = 400.dp

/** 顶部数字距画布顶部的距离（原版 `dp(108f)`）。 */
private val NumeralTopOffset = 108.dp

/** 右侧数字距画布右边的距离（原版 `dp(84.6f)`）。 */
private val NumeralRightOffset = 84.6.dp

/** 闹钟「耳朵」的绘制位置（原版 `dp(61f)` / `dp(83f)`）。 */
private val EarLeftOffset = 61.dp
private val EarTopOffset = 83.dp

/** 指针投影的偏移量（原版 `clock_hand_shadow_delta`，xxhdpi 下为 18px）。 */
private const val HandShadowDeltaDp = 6f

/** 时针 / 分针的旋转锚点（原版 `6.9f / 8f`）。 */
private const val HandAnchor = 6.9f / 8f

/** 秒针的旋转锚点（原版 `6.5f / 8f`）。 */
private const val SecondAnchor = 6.5f / 8f

/**
 * 锤子风格机械表盘。
 *
 * 完全使用锤子时钟还原的原版位图叠放绘制（表盘、刻度、数字、指针、投影、闹钟耳朵），
 * 坐标与叠放顺序与 `AnalogClockHandsView` 一致：基准画布 360×400dp，
 * 表盘、数字、指针都按原版 dp 坐标定位，因此不会出现「大致像」的偏差。
 *
 * - 深色模式下自动切换到原版的夜间表盘与深色指针（原版靠 `setDarkHands` 切换）；
 * - 秒针每次跳动带原版的弹簧回弹；
 * - [size] 是**表盘直径**（不是整块画布），组件会自动换算画布尺寸。
 *
 * ```kotlin
 * SmartisanAnalogClock(time = LocalTime.of(10, 9, 36), showEars = true, size = 220.dp)
 * ```
 */
@Composable
fun SmartisanAnalogClock(
    modifier: Modifier = Modifier,
    time: LocalTime = LocalTime.now(),
    showSecondHand: Boolean = true,
    showEars: Boolean = false,
    showNumerals: Boolean = true,
    darkHands: Boolean = !LocalSmartisanColors.current.isLight,
    size: Dp = 240.dp,
) {
    val density = LocalDensity.current
    val face = rememberSmartisanDrawablePainter(SmartisanClockDrawables.Face)
    val nightFace = rememberSmartisanDrawablePainter(SmartisanClockDrawables.FaceNight)
    val degree = rememberSmartisanDrawablePainter(SmartisanClockDrawables.Degree)
    val numeralTop =
        rememberSmartisanDrawablePainter(
            if (darkHands) SmartisanClockDrawables.NumeralTopTwelveNight else SmartisanClockDrawables.NumeralTopTwelve,
        )
    val numeralRight =
        rememberSmartisanDrawablePainter(
            if (darkHands) SmartisanClockDrawables.NumeralRightThreeNight else SmartisanClockDrawables.NumeralRightThree,
        )
    val hourHand =
        rememberSmartisanDrawablePainter(
            if (darkHands) SmartisanClockDrawables.HourHandBlack else SmartisanClockDrawables.HourHand,
        )
    val minuteHand =
        rememberSmartisanDrawablePainter(
            if (darkHands) SmartisanClockDrawables.MinuteHandBlack else SmartisanClockDrawables.MinuteHand,
        )
    val secondHand = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SecondHand)
    val hourShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.HourHandShadow)
    val minuteShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.MinuteHandShadow)
    val secondShadow = rememberSmartisanDrawablePainter(SmartisanClockDrawables.SecondHandShadow)
    val handCenter =
        rememberSmartisanDrawablePainter(
            if (darkHands) SmartisanClockDrawables.HandCenterBlack else SmartisanClockDrawables.HandCenter,
        )
    val handCenterMiddle = rememberSmartisanDrawablePainter(SmartisanClockDrawables.HandCenterMiddle)
    val earLeft = rememberSmartisanDrawablePainter(SmartisanClockDrawables.EarLeft)
    val earRight = rememberSmartisanDrawablePainter(SmartisanClockDrawables.EarRight)

    val secondSweep = rememberSecondHandSweep(time.second)
    val secondDegrees by animateFloatAsState(
        targetValue = secondSweep * 6f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
        label = "smartisan second hand",
    )

    val baseWidthPx = with(density) { ClockBaseWidth.toPx() }
    val baseHeightPx = with(density) { ClockBaseHeight.toPx() }
    // 表盘位图的固有宽度就是原版表盘直径，用它把基准画布缩放到 size。
    val faceWidthPx = face.intrinsicSize.width.takeIf { it > 0f } ?: with(density) { 254.dp.toPx() }
    val canvasScale = with(density) { size.toPx() } / faceWidthPx
    val boxWidth = with(density) { (baseWidthPx * canvasScale).toDp() }
    val boxHeight = with(density) { (baseHeightPx * canvasScale).toDp() }

    Canvas(modifier.size(boxWidth, boxHeight)) {
        scale(canvasScale, canvasScale, pivot = Offset.Zero) {
            drawSmartisanClockFace(
                baseWidth = baseWidthPx,
                baseHeight = baseHeightPx,
                densityScale = density.density,
                time = time,
                secondDegrees = secondDegrees,
                showSecondHand = showSecondHand,
                showEars = showEars,
                showNumerals = showNumerals,
                darkHands = darkHands,
                face = face,
                nightFace = nightFace,
                degree = degree,
                numeralTop = numeralTop,
                numeralRight = numeralRight,
                hourHand = hourHand,
                minuteHand = minuteHand,
                secondHand = secondHand,
                hourShadow = hourShadow,
                minuteShadow = minuteShadow,
                secondShadow = secondShadow,
                handCenter = handCenter,
                handCenterMiddle = handCenterMiddle,
                earLeft = earLeft,
                earRight = earRight,
            )
        }
    }
}
