/**
 * 时钟与机械控件：两种计时器标尺。
 *
 * 复刻自锤子时钟的两个自定义 View：
 * - `TimerRulerView`：横向卡尺（0..180 分钟，刻度 + 数字 + 中间红色准星）；
 * - `Classic680RulerView`：竖向拉环标尺（80 × 500dp 视图、400dp 行程、拉环高 282/1200 行程、
 *   标尺宽 233/1200 行程、45 秒阈值触感、快速上拉释放）。
 *
 * 原实现分别用 `FlingAnimation` / `SpringAnimation`（androidx.dynamicanimation）与
 * `ValueAnimator` 拼出「阻尼 → 越界 → 吸附」三段式收尾，这里统一改成
 * `Animatable.animateDecay`（指数衰减，friction 1.1，与原版 `FlingAnimation.friction` 一致）
 * + `animateTo(spring)`（越界回弹，dampingRatio 0.62 / stiffness 900，取自原版边界弹簧）
 * + 整分钟吸附（横向用 300ms 减速插值，竖向用弹簧回弹）。
 */
package top.smartisanx.ui.clock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.smartisanx.core.interaction.smartisanHaptic
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 标尺松手后的三段式收尾：惯性 → 越界回弹 → 整格吸附。
 *
 * @param animatable 承载动画的 [Animatable]，调用方需长期持有。
 * @param startPosition 松手瞬间的位置（像素，0 表示 [range] 起点）。
 * @param maximumPosition 位置上限（像素）。
 * @param initialVelocity 松手速度（像素 / 秒，正方向与位置增长方向一致）。
 * @param unitPx 一格（一分钟）对应的像素数。
 * @param overscrollLimit 允许的越界距离，越界后会被弹簧拉回。
 * @param minimumFlingVelocity 低于该速度不做惯性，直接吸附。
 * @param snapSpec 吸附动画规格：横向卡尺用 300ms 减速插值，竖向拉环用弹簧回弹。
 * @param onUpdate 每一帧的位置回调。
 */
private suspend fun settleRulerPosition(
    animatable: Animatable<Float, AnimationVector1D>,
    startPosition: Float,
    maximumPosition: Float,
    initialVelocity: Float,
    unitPx: Float,
    overscrollLimit: Float,
    minimumFlingVelocity: Float,
    snapSpec: AnimationSpec<Float>,
    onUpdate: (Float) -> Unit,
) {
    var position = startPosition
    if (unitPx > 0f && abs(initialVelocity) >= minimumFlingVelocity) {
        animatable.snapTo(position)
        animatable.animateDecay(initialVelocity, RulerFlingDecay) {
            val clamped = value.coerceIn(-overscrollLimit, maximumPosition + overscrollLimit)
            position = clamped
            onUpdate(clamped)
            // 撞到越界上限就停下，交给后面的边界弹簧接管。
            if (clamped != value) cancelAnimation()
        }
    }

    val boundaryTarget = when {
        position < 0f -> 0f
        position > maximumPosition -> maximumPosition
        else -> null
    }
    if (boundaryTarget != null) {
        animatable.snapTo(position)
        animatable.animateTo(boundaryTarget, RulerBoundarySpring) {
            position = value
            onUpdate(value)
        }
    }

    if (unitPx > 0f) {
        val lastUnit = (maximumPosition / unitPx).roundToInt()
        val snapped = (position / unitPx).roundToInt().coerceIn(0, lastUnit) * unitPx
        if (abs(snapped - position) > PositionEpsilon) {
            animatable.snapTo(position)
            animatable.animateTo(snapped, snapSpec) {
                position = value
                onUpdate(value)
            }
        }
        onUpdate(snapped)
    }
}

/**
 * 按可用像素间距挑选刻度步长：取第一个使间距不小于 [minimumSpacingPx] 的候选值。
 *
 * 原版卡尺的刻度位图宽 70px（xxhdpi）≈ 23.33dp，正好够放一个数字，因此每分钟都画刻度与数字；
 * 竖向拉环的行程要容纳整个区间，就必须按间距自动降采样，否则 180 分钟会糊成一条线。
 */
private fun tickStep(unitPx: Float, minimumSpacingPx: Float): Int {
    if (unitPx <= 0f) return 1
    return TickStepCandidates.firstOrNull { it * unitPx >= minimumSpacingPx } ?: TickStepCandidates.last()
}


/**
 * smartisanx 的横向卡尺（计时器标尺）。
 *
 * 复刻自锤子时钟的 `TimerRulerView`：一分钟正好等于刻度位图的宽度（`timer_scale_normal`
 * 在 xxhdpi 下 70px ≈ 23.33dp），刻度与数字每分钟一格，视口正中是一条红色准星，
 * 拖动方向与刻度滚动方向相反（左拖 = 时长增加）。
 *
 * 物理参数与原版一致：
 * - 拖到两端继续外拖时按 `over/12`、`over/20` 两段阻尼衰减，越界上限 500px；
 * - 松手速度超过 500dp/s 走惯性（`FlingAnimation` friction 1.1 → 指数衰减 1.1）；
 * - 惯性结束或速度不足时吸附到整分钟（原版 300ms 减速插值）；
 * - 越界时用阻尼 0.62 / 刚度 900 的弹簧回弹（原版 `SpringAnimation` 参数）。
 *
 * @param minutes 当前分钟数，会被收敛到 [range] 内。
 * @param onMinutesChange 分钟数变化回调。
 * @param modifier 外部修饰符；建议给出 48dp 左右的高度。
 * @param range 可选的分钟区间。
 * @param enabled 是否可拖动；禁用时刻度与数字改用 `textDisabled`，与原版禁用态位图一致。
 */
@Composable
fun SmartisanRulerPicker(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..180,
    enabled: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val haptic = smartisanHaptic()
    val scope = rememberCoroutineScope()

    val firstMinute = min(range.first, range.last)
    val span = abs(range.last - range.first)
    val unitPx = with(density) { CaliperMinuteWidth.toPx() }
    val maximumPosition = span * unitPx
    val overscrollLimit = with(density) { CaliperOverscrollLimit.toPx() }
    val minimumFlingVelocity = with(density) { MinFlingVelocity.toPx() }
    val animatable = remember { Animatable(0f) }

    val targetPosition = (minutes.coerceIn(firstMinute, firstMinute + span) - firstMinute) * unitPx
    var positionPx by remember(unitPx, span) { mutableFloatStateOf(targetPosition) }
    var interacting by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    var lastReported by remember { mutableIntStateOf(minutes) }
    var lastTickBucket by remember { mutableIntStateOf(Int.MIN_VALUE) }

    // 外部改值（非拖动 / 非收尾中）时直接对齐，避免与手势打架。
    LaunchedEffect(targetPosition) {
        if (!interacting && abs(positionPx - targetPosition) > PositionEpsilon) {
            positionPx = targetPosition
        }
    }

    fun reportMinutes() {
        val candidate = firstMinute + (positionPx / unitPx).roundToInt().coerceIn(0, span)
        if (candidate != lastReported) {
            lastReported = candidate
            onMinutesChange(candidate)
        }
    }

    /** 原版 `applyDragResistance`：越界后按两段阻尼衰减，最后完全拖不动。 */
    fun applyDragResistance(current: Float, delta: Float): Float {
        val movingFurtherOut =
            (current >= maximumPosition && delta >= 0f) || (current <= 0f && delta <= 0f)
        val adjusted = if (!movingFurtherOut) {
            delta
        } else {
            val overscroll = if (current > 0f) current - maximumPosition else -current
            when {
                overscroll < overscrollLimit * 0.5f ->
                    overscrollLimit / (overscroll * 12f + overscrollLimit) * delta

                overscroll < overscrollLimit * 0.67f ->
                    overscrollLimit / (overscroll * 20f + overscrollLimit) * delta

                abs(delta) > MinResistanceDeltaPx -> if (delta > 0f) 1f else -1f
                else -> 0f
            }
        }
        return (current + adjusted).coerceIn(-overscrollLimit, maximumPosition + overscrollLimit)
    }

    /** 每越过一格刻度触发一次触感，对应原版 30ms 的刻度振动。 */
    fun hapticOnTickCrossing() {
        val bucket = floor(positionPx / unitPx).toInt()
        val previous = lastTickBucket
        lastTickBucket = bucket
        if (previous != Int.MIN_VALUE && previous != bucket) haptic()
    }

    Canvas(
        modifier = modifier
            .draggable(
                state = rememberDraggableState { delta ->
                    // 手指右移（delta > 0）等于把刻度往右推，时长减小。
                    positionPx = applyDragResistance(positionPx, -delta)
                    hapticOnTickCrossing()
                    reportMinutes()
                },
                orientation = Orientation.Horizontal,
                enabled = enabled,
                onDragStarted = {
                    settleJob?.cancel()
                    interacting = true
                    lastTickBucket = Int.MIN_VALUE
                },
                onDragStopped = { velocity ->
                    settleJob = scope.launch {
                        settleRulerPosition(
                            animatable = animatable,
                            startPosition = positionPx,
                            maximumPosition = maximumPosition,
                            initialVelocity = -velocity,
                            unitPx = unitPx,
                            overscrollLimit = overscrollLimit,
                            minimumFlingVelocity = minimumFlingVelocity,
                            snapSpec = tween(SettleDurationMillis, easing = RulerSettleEasing),
                            onUpdate = { positionPx = it },
                        )
                        reportMinutes()
                        interacting = false
                    }
                },
            )
            .fillMaxSize(),
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val zeroX = centerX - positionPx
        val tickColor = if (enabled) colors.textTertiary else colors.textDisabled
        val labelColor = if (enabled) colors.textSecondary else colors.textDisabled
        val labelStyle = typography.numeric.copy(fontSize = LabelFontSize, color = labelColor)
        val majorLength = height * MajorTickHeightRatio
        val minorLength = height * MinorTickHeightRatio
        val labelStep = if (unitPx >= MinLabelSpacingPx) 1 else MajorTickEvery

        val firstVisible = floor((0f - zeroX) / unitPx).toInt() - 1
        val lastVisible = ceil((width - zeroX) / unitPx).toInt() + 1
        for (index in firstVisible..lastVisible) {
            if (index < 0 || index > span) continue
            val x = zeroX + index * unitPx
            val isMajor = index % MajorTickEvery == 0
            drawLine(
                color = tickColor,
                start = Offset(x, 0f),
                end = Offset(x, if (isMajor) majorLength else minorLength),
                strokeWidth = if (isMajor) MajorTickStroke else MinorTickStroke,
                cap = StrokeCap.Round,
            )
            if (index % labelStep == 0) {
                val layout = textMeasurer.measure(
                    text = (firstMinute + index).toString(),
                    style = labelStyle,
                )
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = x - layout.size.width / 2f,
                        y = height * LabelTopRatio,
                    ),
                )
            }
        }
        // 中间红色准星：贯穿整个卡尺的细线。
        drawLine(
            color = colors.accent,
            start = Offset(centerX, 0f),
            end = Offset(centerX, height),
            strokeWidth = SightStroke,
        )
        drawRect(color = colors.divider, style = Stroke(width = FrameStroke))
    }
}

