package top.smartisanx.ui.clock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import kotlin.math.max

/**
 * smartisanx 的小表盘，用于列表行、卡片与世界时钟。
 *
 * 合并了两个自定义 View 的实现：
 * - 锤子时钟 `CompactAlarmClockView`（响铃卡片里的 `_74` 小表盘，基准画布 188 × 196.1dp，
 *   指针锚点 6.9/8、中心轴双层、指针带 6dp 投影、秒针越界 0.2 秒后 50ms 收敛）；
 * - 锤子时钟 `SmallWorldClockView`（60dp 世界时钟行表盘，指针锚点 6.7/8 与 7.2/8，
 *   按当地时间在日间 / 夜间两套表盘之间切换）。
 *
 * 两者在本库里合并为一个组件：只保留 [size] 一个尺寸参数，所有比例按画布直径等比换算，
 * 日 / 夜表盘差异交给调用方通过 [dialColor] 表达（世界时钟卡片就是这么做的）。
 *
 * @param modifier 外部修饰符；尺寸由 [size] 控制。
 * @param hour 小时（0..23，内部按 12 小时制换算）。
 * @param minute 分钟（0..59）。
 * @param second 秒（0..59），仅在 [showSecondHand] 为 true 时参与绘制。
 * @param showSecondHand 是否绘制秒针；默认关闭，避免列表滚动时反复重绘。
 * @param size 表盘直径，原版列表行使用 60dp，响铃卡片使用 74dp。
 * @param dialColor 表盘底色，默认取色板的 `surface`。
 * @param handColor 指针色，默认取色板的 `textPrimary`。
 */
@Composable
fun SmartisanCompactClock(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    second: Int = 0,
    showSecondHand: Boolean = false,
    size: Dp = 40.dp,
    dialColor: Color = Color.Unspecified,
    handColor: Color = Color.Unspecified,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val resolvedDial = if (dialColor == Color.Unspecified) colors.surface else dialColor
    val resolvedHand = if (handColor == Color.Unspecified) colors.textPrimary else handColor
    val ringColor = if (colors.isLight) colors.divider else colors.surfaceRaised
    val shadowColor = colors.textTertiary.copy(alpha = ShadowAlpha)


    val secondFraction = second.toFloat()
    // 秒针回弹：原版越界 0.2 秒后用 50ms 减速插值收回，这里用低阻尼弹簧获得同样的越界收敛。
    val secondDegrees by animateFloatAsState(
        targetValue = if (showSecondHand) secondFraction * DEGREES_PER_SECOND else 0f,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = 1200f,
            visibilityThreshold = 0.05f,
        ),
        label = "小表盘秒针回弹",
    )
    val minuteDegrees by animateFloatAsState(
        targetValue = (minute + secondFraction / SECONDS_PER_MINUTE) * DEGREES_PER_MINUTE,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = HandSpringStiffness,
            visibilityThreshold = 0.01f,
        ),
        label = "小表盘分针",
    )
    val hourDegrees by animateFloatAsState(
        targetValue = (hour % HOURS_PER_CYCLE) * DEGREES_PER_HOUR +
            minute / MINUTES_PER_HOUR * DEGREES_PER_HOUR,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = HandSpringStiffness,
            visibilityThreshold = 0.01f,
        ),
        label = "小表盘时针",
    )

    val numeralStyle = typography.numeric.copy(
        fontSize = with(density) { (size * NumeralSizeRatio).toSp() },
        fontWeight = FontWeight.Medium,
        color = colors.textTertiary,
    )


    Canvas(modifier = modifier.size(size)) {
        val side = this.size.minDimension
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val dialRadius = side / 2f
        val ringWidth = max(side * RingWidthRatio, MinStrokePx)
        val tickColor = colors.textTertiary

        drawCircle(color = resolvedDial, radius = dialRadius, center = center)
        drawCircle(
            color = ringColor,
            radius = dialRadius - ringWidth / 2f,
            center = center,
            style = Stroke(width = ringWidth),
        )
        // 12 个小时刻度（小表盘不画分钟刻度，否则 40dp 内会糊成一圈）。
        for (index in 0 until HOURS_PER_CYCLE) {
            val degrees = index * DEGREES_PER_HOUR
            drawLine(
                color = tickColor,
                start = smartisanClockPolar(center, dialRadius * HourTickStartRatio, degrees),
                end = smartisanClockPolar(center, dialRadius * HourTickEndRatio, degrees),
                strokeWidth = max(side * TickWidthRatio, MinStrokePx),
                cap = StrokeCap.Round,
            )
        }
        // 原版小表盘只标 12 与 3，这里保持一致；过小的表盘不再标数字，避免糊在一起。
        if (side >= NumeralMinSizePx) {
            listOf(12 to 0f, 3 to 90f).forEach { (value, degrees) ->
                val layout = textMeasurer.measure(text = value.toString(), style = numeralStyle)
                val anchor = smartisanClockPolar(center, dialRadius * NumeralRadiusRatio, degrees)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = anchor.x - layout.size.width / 2f,
                        y = anchor.y - layout.size.height / 2f,
                    ),
                )
            }
        }
        drawCompactHand(
            center = center,
            degrees = hourDegrees,
            length = dialRadius * HourHandLengthRatio,
            tail = dialRadius * HourHandTailRatio,
            width = dialRadius * HourHandWidthRatio,
            color = resolvedHand,
            shadowColor = shadowColor,
            shadowOffset = side * ShadowOffsetRatio,
        )
        drawCompactHand(
            center = center,
            degrees = minuteDegrees,
            length = dialRadius * MinuteHandLengthRatio,
            tail = dialRadius * MinuteHandTailRatio,
            width = dialRadius * MinuteHandWidthRatio,
            color = resolvedHand,
            shadowColor = shadowColor,
            shadowOffset = side * ShadowOffsetRatio,
        )
        if (showSecondHand) {
            drawCompactHand(
                center = center,
                degrees = secondDegrees,
                length = dialRadius * SecondHandLengthRatio,
                tail = dialRadius * SecondHandTailRatio,
                width = max(dialRadius * SecondHandWidthRatio, MinStrokePx),
                color = colors.accent,
                shadowColor = shadowColor,
                shadowOffset = side * ShadowOffsetRatio,
            )
        }
        drawCircle(color = resolvedHand, radius = dialRadius * CenterCapRatio, center = center)
        drawCircle(color = colors.accent, radius = dialRadius * CenterDotRatio, center = center)
    }
}

/** 绘制小表盘指针：先画一层偏移的投影，再画指针本体，对应原版的 `*_shadow` 位图。 */
private fun DrawScope.drawCompactHand(
    center: Offset,
    degrees: Float,
    length: Float,
    tail: Float,
    width: Float,
    color: Color,
    shadowColor: Color,
    shadowOffset: Float,
) {
    val shadowShift = smartisanClockPolar(Offset.Zero, shadowOffset, degrees + 90f)
    drawLine(
        color = shadowColor,
        start = smartisanClockPolar(center, tail, degrees + 180f) + shadowShift,
        end = smartisanClockPolar(center, length, degrees) + shadowShift,
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
    drawClockHand(
        center = center,
        degrees = degrees,
        length = length,
        tail = tail,
        width = width,
        color = color,
    )
}

/** 每秒对应的角度。 */
private const val DEGREES_PER_SECOND = 6f

/** 每分对应的角度。 */
private const val DEGREES_PER_MINUTE = 6f

/** 每小时对应的角度。 */
private const val DEGREES_PER_HOUR = 30f

/** 表盘一圈的小时数。 */
private const val HOURS_PER_CYCLE = 12

/** 一小时内的分钟数。 */
private const val MINUTES_PER_HOUR = 60f

/** 一分钟内的秒数。 */
private const val SECONDS_PER_MINUTE = 60f

/** 指针走动的弹簧刚度。 */
private const val HandSpringStiffness = 120f

/** 指针投影透明度，原版 `*_shadow` 位图约 25% 不透明度。 */
private const val ShadowAlpha = 0.25f

/** 指针投影偏移占表盘直径的比例，对应原版 6dp / 188dp 的阴影位移。 */
private const val ShadowOffsetRatio = 0.02f

/** 圆环描边占直径的比例。 */
private const val RingWidthRatio = 0.02f

/** 刻度线宽比例。 */
private const val TickWidthRatio = 0.015f

/** 刻度起点比例。 */
private const val HourTickStartRatio = 0.84f

/** 刻度终点比例。 */
private const val HourTickEndRatio = 0.94f

/** 数字距圆心比例。 */
private const val NumeralRadiusRatio = 0.62f

/** 数字字号占直径的比例。 */
private const val NumeralSizeRatio = 0.16f

/** 小于该像素尺寸时不再绘制数字。 */
private const val NumeralMinSizePx = 34f

/** 时针长度比例，取自原版 `alarm_ringing_hour_hand` 的 6.9/8 锚点。 */
private const val HourHandLengthRatio = 0.42f

/** 时针线宽比例。 */
private const val HourHandWidthRatio = 0.11f

/** 分针长度比例，取自原版 7.2/8 锚点。 */
private const val MinuteHandLengthRatio = 0.62f

/** 分针线宽比例。 */
private const val MinuteHandWidthRatio = 0.07f

/** 秒针长度比例，取自原版 6.5/8 锚点。 */
private const val SecondHandLengthRatio = 0.78f

/** 秒针线宽比例。 */
private const val SecondHandWidthRatio = 0.03f

/** 时针尾巴比例。 */
private const val HourHandTailRatio = 0.12f

/** 分针尾巴比例。 */
private const val MinuteHandTailRatio = 0.18f

/** 秒针尾巴比例。 */
private const val SecondHandTailRatio = 0.22f

/** 中心轴外层圆比例。 */
private const val CenterCapRatio = 0.1f

/** 中心轴内层圆比例。 */
private const val CenterDotRatio = 0.045f

/** 最小描边像素。 */
private const val MinStrokePx = 1f

