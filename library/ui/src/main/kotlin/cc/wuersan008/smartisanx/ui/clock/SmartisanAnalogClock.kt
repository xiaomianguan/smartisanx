package cc.wuersan008.smartisanx.ui.clock

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * smartisanx 的机械表盘。
 *
 * 复刻自锤子时钟的自定义 View `AnalogClockHandsView`（XML + Canvas + 位图指针），
 * 这里用 Compose Canvas 重写：原实现的刻度环、数字、指针、中心轴、闹钟耳朵全部由位图
 * （`blank_clock` / `d12` / `hour_hand` / `big_left` …）拼出来，本实现改为纯几何绘制，
 * 只保留其几何比例与运动参数，因此不依赖任何图片资源，也能跟随主题自动适配深色模式。
 *
 * 与原实现一致的运动细节：
 * - 秒针整秒跳动时带轻微回弹（原版越界 `0.15` 秒后收敛，这里用低阻尼弹簧收敛）；
 * - 时针、分针按「整点 + 分秒的小数部分」连续走动，不会在整点瞬间跳格；
 * - 指针越过中心轴留出一小段尾巴，中心轴由两层同心圆组成（对应 `hand_center` + `hand_center_middle`）。
 *
 * 组件本身不持有计时器，只渲染传入的 [time]，因此可预览、可测试；要走时请自行驱动时间：
 *
 * ```kotlin
 * val time by produceState(LocalTime.now()) {
 *     while (true) {
 *         value = LocalTime.now()
 *         withFrameNanos { }
 *     }
 * }
 * SmartisanAnalogClock(time = time, showEars = true)
 * ```
 *
 * @param modifier 外部修饰符；尺寸由 [size] 控制。
 * @param time 表盘显示的时间，默认当前时间。
 * @param showSecondHand 是否绘制秒针。
 * @param showEars 是否绘制闹钟式的左右「耳朵」。
 * @param showNumerals 是否绘制 12 / 3 / 6 / 9 四个数字。
 * @param dialColor 表盘底色，默认取色板的 `surface`。
 * @param handColor 指针与刻度色，默认取色板的 `textPrimary`。
 * @param accentColor 秒针与中心轴强调色，默认取色板的 `accent`（锤子红）。
 * @param size 表盘直径（含耳朵占位）。
 */
@Composable
fun SmartisanAnalogClock(
    modifier: Modifier = Modifier,
    time: LocalTime = LocalTime.now(),
    showSecondHand: Boolean = true,
    showEars: Boolean = false,
    showNumerals: Boolean = true,
    dialColor: Color = Color.Unspecified,
    handColor: Color = Color.Unspecified,
    accentColor: Color = Color.Unspecified,
    size: Dp = 240.dp,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val resolvedDial = if (dialColor == Color.Unspecified) colors.surface else dialColor
    val resolvedHand = if (handColor == Color.Unspecified) colors.textPrimary else handColor
    val resolvedAccent = if (accentColor == Color.Unspecified) colors.accent else accentColor
    // 深色模式下 surfaceRaised 比 surface 更亮，用它与页底拉开层次；浅色模式沿用极淡的分隔线色。
    val ringColor = if (colors.isLight) colors.divider else colors.surfaceRaised
    val tickColor = colors.textTertiary
    val numeralColor = colors.textSecondary


    val secondFraction = time.second + time.nano / 1_000_000_000f
    // 秒针：弹簧目标就是当前秒数对应的角度，低阻尼让弹簧自然越过目标再收回，
    // 形成原版「越界 0.15 秒后收敛」的机械回弹，不需要额外的 Animator 队列。
    val secondDegrees by animateFloatAsState(
        targetValue = secondFraction * DEGREES_PER_SECOND,
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = 1400f,
            visibilityThreshold = 0.05f,
        ),
        label = "秒针回弹",
    )
    val minuteDegrees by animateFloatAsState(
        targetValue = (time.minute + secondFraction / SECONDS_PER_MINUTE) * DEGREES_PER_MINUTE,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = SpringStiffnessHand,
            visibilityThreshold = 0.01f,
        ),
        label = "分针走动",
    )
    val hourDegrees by animateFloatAsState(
        targetValue = (time.hour % HOURS_PER_CYCLE) * DEGREES_PER_HOUR +
            time.minute / MINUTES_PER_HOUR * DEGREES_PER_HOUR,
        animationSpec = spring(
            dampingRatio = 0.9f,
            stiffness = SpringStiffnessHand,
            visibilityThreshold = 0.01f,
        ),
        label = "时针走动",
    )

    val numeralFontSize = with(density) { (size * NumeralSizeRatio).toSp() }
    val numeralStyle = typography.numeric.copy(
        fontSize = numeralFontSize,
        fontWeight = FontWeight.Medium,
        color = numeralColor,
    )
    val description =
        "表盘 " +
            time.hour.toString().padStart(2, '0') + ":" +
            time.minute.toString().padStart(2, '0') + ":" +
            time.second.toString().padStart(2, '0')

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
    ) {
        val side = this.size.minDimension
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outerRadius = side / 2f
        val earRadius = outerRadius * EarRadiusRatio
        val dialRadius = if (showEars) outerRadius - earRadius * EarInsetRatio else outerRadius
        val ringWidth = max(side * RingWidthRatio, MinStrokePx)

        // 耳朵先画，随后表盘底色会盖掉朝内的一半，只留下两个小半圆。
        if (showEars) {
            drawClockEars(
                center = center,
                dialRadius = dialRadius,
                earRadius = earRadius,
                fill = resolvedDial,
                stroke = ringColor,
                strokeWidth = ringWidth,
            )
        }
        drawCircle(color = resolvedDial, radius = dialRadius, center = center)
        drawCircle(
            color = ringColor,
            radius = dialRadius - ringWidth / 2f,
            center = center,
            style = Stroke(width = ringWidth),
        )
        drawClockTicks(
            center = center,
            dialRadius = dialRadius,
            tickColor = tickColor,
            baseStroke = max(side * TickWidthRatio, MinStrokePx),
        )
        if (showNumerals) {
            drawClockNumerals(
                textMeasurer = textMeasurer,
                style = numeralStyle,
                center = center,
                dialRadius = dialRadius,
            )
        }
        // 时针、分针在下，秒针在上，与原版位图叠放顺序一致。
        drawClockHand(
            center = center,
            degrees = hourDegrees,
            length = dialRadius * HourHandLengthRatio,
            tail = dialRadius * HourHandTailRatio,
            width = dialRadius * HourHandWidthRatio,
            color = resolvedHand,
        )
        drawClockHand(
            center = center,
            degrees = minuteDegrees,
            length = dialRadius * MinuteHandLengthRatio,
            tail = dialRadius * MinuteHandTailRatio,
            width = dialRadius * MinuteHandWidthRatio,
            color = resolvedHand,
        )
        if (showSecondHand) {
            drawClockHand(
                center = center,
                degrees = secondDegrees,
                length = dialRadius * SecondHandLengthRatio,
                tail = dialRadius * SecondHandTailRatio,
                width = max(dialRadius * SecondHandWidthRatio, MinStrokePx),
                color = resolvedAccent,
            )
        }
        // 中心轴：外层实心圆 + 内层强调色小圆（对应原版 hand_center + hand_center_middle）。
        drawCircle(color = resolvedHand, radius = dialRadius * CenterCapRatio, center = center)
        drawCircle(color = resolvedAccent, radius = dialRadius * CenterDotRatio, center = center)
    }
}



/**
 * 极坐标换算：以 [degrees] 表示相对 12 点方向的顺时针角度。
 *
 * 供表盘类组件共用（大表盘、小表盘、世界时钟卡片），避免重复实现同样的三角函数。
 */
internal fun smartisanClockPolar(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = (degrees - 90f) * PI.toFloat() / 180f
    return Offset(
        x = center.x + cos(radians) * radius,
        y = center.y + sin(radians) * radius,
    )
}

/** 绘制表盘刻度：整点刻度更长更粗，分钟刻度短而细，对应原版 `degree` 位图的疏密。 */
internal fun DrawScope.drawClockTicks(
    center: Offset,
    dialRadius: Float,
    tickColor: Color,
    baseStroke: Float,
) {
    val startRadius = dialRadius * TickStartRatio
    for (index in 0 until MINUTE_TICKS) {
        val degrees = index * DEGREES_PER_MINUTE
        val isHourTick = index % MINUTES_PER_HOUR_TICK == 0
        val length = dialRadius * if (isHourTick) HourTickLengthRatio else MinuteTickLengthRatio
        val stroke = if (isHourTick) baseStroke * HourTickWidthFactor else baseStroke
        drawLine(
            color = tickColor,
            start = smartisanClockPolar(center, startRadius, degrees),
            end = smartisanClockPolar(center, startRadius - length, degrees),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/** 绘制 12 / 3 / 6 / 9 四个数字，内缩比例与原版 `d12` / `d3` / `d60` / `d15` 位图一致。 */
internal fun DrawScope.drawClockNumerals(
    textMeasurer: TextMeasurer,
    style: TextStyle,
    center: Offset,
    dialRadius: Float,
) {
    val numerals = listOf(12 to 0f, 3 to 90f, 6 to 180f, 9 to 270f)
    val radius = dialRadius * NumeralRadiusRatio
    numerals.forEach { (value, degrees) ->
        val layout = textMeasurer.measure(text = value.toString(), style = style)
        val anchor = smartisanClockPolar(center, radius, degrees)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(
                x = anchor.x - layout.size.width / 2f,
                y = anchor.y - layout.size.height / 2f,
            ),
        )
    }
}

/** 绘制一根指针：从中心轴后方 [tail] 处延伸到 [length] 处，两端圆角。 */
internal fun DrawScope.drawClockHand(
    center: Offset,
    degrees: Float,
    length: Float,
    tail: Float,
    width: Float,
    color: Color,
) {
    drawLine(
        color = color,
        start = smartisanClockPolar(center, tail, degrees + 180f),
        end = smartisanClockPolar(center, length, degrees),
        strokeWidth = width,
        cap = StrokeCap.Round,
    )
}

/** 绘制闹钟耳朵：两个小圆，朝内的一半随后会被表盘底色覆盖。 */
private fun DrawScope.drawClockEars(
    center: Offset,
    dialRadius: Float,
    earRadius: Float,
    fill: Color,
    stroke: Color,
    strokeWidth: Float,
) {
    val offset = dialRadius * EarOffsetRatio
    listOf(-1f, 1f).forEach { direction ->
        val earCenter = Offset(center.x + offset * direction, center.y)
        drawCircle(color = fill, radius = earRadius, center = earCenter)
        drawCircle(
            color = stroke,
            radius = earRadius - strokeWidth / 2f,
            center = earCenter,
            style = Stroke(width = strokeWidth),
        )
    }
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

/** 分钟刻度总数。 */
private const val MINUTE_TICKS = 60

/** 每隔几个刻度出现一个整点刻度。 */
private const val MINUTES_PER_HOUR_TICK = 5

/** 指针走动的弹簧刚度，越大越跟手。 */
private const val SpringStiffnessHand = 120f

/** 表盘圆环描边占直径的比例，原版 `blank_clock` 边框约 2.5dp / 240dp。 */
private const val RingWidthRatio = 0.008f

/** 刻度线基础线宽比例。 */
private const val TickWidthRatio = 0.004f

/** 整点刻度相对分钟刻度的线宽倍数。 */
private const val HourTickWidthFactor = 2.2f

/** 刻度起点（相对外圈向内缩进）比例。 */
private const val TickStartRatio = 0.92f

/** 整点刻度长度比例。 */
private const val HourTickLengthRatio = 0.1f

/** 分钟刻度长度比例。 */
private const val MinuteTickLengthRatio = 0.05f

/** 数字距离圆心的比例，对应原版 `d12` 顶端内缩 30dp / 240dp。 */
private const val NumeralRadiusRatio = 0.7f

/** 数字字号占表盘直径的比例。 */
private const val NumeralSizeRatio = 0.13f

/** 时针长度比例，对应原版 `hour_hand` 的 152/184 锚点。 */
private const val HourHandLengthRatio = 0.5f

/** 时针线宽比例。 */
private const val HourHandWidthRatio = 0.075f

/** 分针长度比例。 */
private const val MinuteHandLengthRatio = 0.74f

/** 分针线宽比例。 */
private const val MinuteHandWidthRatio = 0.05f

/** 秒针长度比例。 */
private const val SecondHandLengthRatio = 0.84f

/** 秒针线宽比例。 */
private const val SecondHandWidthRatio = 0.014f

/** 时针越过中心轴的尾巴比例。 */
private const val HourHandTailRatio = 0.14f

/** 分针越过中心轴的尾巴比例。 */
private const val MinuteHandTailRatio = 0.16f

/** 秒针越过中心轴的尾巴比例。 */
private const val SecondHandTailRatio = 0.24f

/** 中心轴外层圆半径比例。 */
private const val CenterCapRatio = 0.055f

/** 中心轴内层圆半径比例。 */
private const val CenterDotRatio = 0.024f

/** 耳朵半径占表盘半径的比例。 */
private const val EarRadiusRatio = 0.1f

/** 耳朵圆心相对表盘半径的外移比例。 */
private const val EarOffsetRatio = 0.94f

/** 有耳朵时表盘半径相对总半径的内缩比例。 */
private const val EarInsetRatio = 0.6f

/** 最小描边像素，保证小尺寸下表盘仍然可见。 */
private const val MinStrokePx = 1f
