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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
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
            // 越界后位置停在越界上限，等衰减结束后由边界弹簧接管回弹。
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
        val labelStep = if (unitPx >= MinLabelSpacing.toPx()) 1 else MajorTickEvery

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
                strokeWidth = if (isMajor) MajorTickStroke.toPx() else MinorTickStroke.toPx(),
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
            strokeWidth = SightStroke.toPx(),
        )
        drawRect(color = colors.divider, style = Stroke(width = FrameStroke.toPx()))
    }
}


/**
 * smartisanx 的竖向拉环标尺。
 *
 * 几何与物理参数取自锤子时钟 6.8.0 的 `Classic680RulerView`：视图宽 80dp、行程 400dp
 * （`classic_timer_680_ruler_length`）、拉环高 = 行程 × 282/1200、刻度带宽 = 行程 × 233/1200、
 * 拖到 45 秒以内的阈值触发一次触感、上拉速度超过「最小甩动速度 × 10」算快速释放。
 *
 * 与 680 原版的差异（已在实现里注明）：原版是「拉环在上、向下拉展开刻度」（文案
 * `Pull down to set the timer duration`），位图随拉环整体滚动；本组件按组件库的交互约定改为
 * **拉环在下方、向上拉增加时长**，并把刻度固定在行程内、拉环作为指针沿刻度移动，
 * 这样任何时长下都能看到完整刻度，不会出现「刻度滚出屏幕」的空档。
 *
 * 松手后：惯性（friction 1.1）→ 越界弹簧回弹（0.62 / 900）→ 整分钟吸附，
 * 吸附用弹簧（阻尼 0.5）形成「拉环回弹一下再落位」的手感。
 *
 * @param minutes 当前分钟数，会被收敛到 [range] 内。
 * @param onMinutesChange 分钟数变化回调。
 * @param modifier 外部修饰符；建议给出 400dp 以上的高度。
 * @param range 可选的分钟区间，整条刻度覆盖整个行程。
 * @param enabled 是否可拖动。
 */
@Composable
fun SmartisanPullRingRuler(
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
    val travelTargetPx = with(density) { PullRingTravel.toPx() }
    val minimumFlingVelocity = with(density) { MinFlingVelocity.toPx() }
    val animatable = remember { Animatable(0f) }

    // 行程依赖画布高度，所以先量出尺寸再算几何；量到之前 unitPx 为 0，位置保持 0。
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val travelPx = min(
        travelTargetPx,
        canvasSize.height.toFloat() / (1f + RingHeightRatio),
    ).coerceAtLeast(0f)
    val unitPx = if (span > 0) travelPx / span else travelPx
    val maximumPosition = travelPx
    val overscrollLimit = travelPx * OverscrollRatio

    val targetPosition = (minutes.coerceIn(firstMinute, firstMinute + span) - firstMinute) * unitPx
    var positionPx by remember(unitPx, span) { mutableFloatStateOf(targetPosition) }
    var interacting by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    var lastReported by remember { mutableIntStateOf(minutes) }
    var lastTickBucket by remember { mutableIntStateOf(Int.MIN_VALUE) }

    LaunchedEffect(targetPosition) {
        if (!interacting && abs(positionPx - targetPosition) > PositionEpsilon) {
            positionPx = targetPosition
        }
    }

    fun reportMinutes() {
        if (unitPx <= 0f) return
        val candidate = firstMinute + (positionPx / unitPx).roundToInt().coerceIn(0, span)
        if (candidate != lastReported) {
            lastReported = candidate
            onMinutesChange(candidate)
        }
    }

    fun hapticOnTickCrossing() {
        if (unitPx <= 0f) return
        val bucket = floor(positionPx / unitPx).toInt()
        val previous = lastTickBucket
        lastTickBucket = bucket
        if (previous != Int.MIN_VALUE && previous != bucket) haptic()
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .draggable(
                state = rememberDraggableState { delta ->
                    // 手指上移（delta < 0）等于拉环上拉，时长增加；越界时按阻尼衰减。
                    val movingFurtherOut =
                        (positionPx >= maximumPosition && delta <= 0f) ||
                            (positionPx <= 0f && delta >= 0f)
                    val adjusted = if (movingFurtherOut) delta * OverscrollDamping else delta
                    positionPx = (positionPx - adjusted).coerceIn(
                        -overscrollLimit,
                        maximumPosition + overscrollLimit,
                    )
                    hapticOnTickCrossing()
                    reportMinutes()
                },
                orientation = Orientation.Vertical,
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
                            snapSpec = spring(
                                dampingRatio = RingSnapDampingRatio,
                                stiffness = RingSnapStiffness,
                            ),
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
        if (travelPx <= 0f || unitPx <= 0f || span <= 0) return@Canvas

        val ringHeight = travelPx * RingHeightRatio
        val trackWidth = travelPx * TrackWidthRatio
        val centerX = width / 2f
        val trackLeft = centerX - trackWidth / 2f
        val trackRight = centerX + trackWidth / 2f
        val progress = (positionPx / maximumPosition).coerceIn(0f, 1f)
        val ringTop = travelPx * (1f - progress)
        val currentMinute = firstMinute + (progress * span).roundToInt()

        val stepValue = tickStep(unitPx, MinTickSpacing.toPx())
        val labelStep = TickStepCandidates
            .filter { it % stepValue == 0 }
            .firstOrNull { it * unitPx >= MinLabelSpacingLarge.toPx() } ?: stepValue
        val tickColor = if (enabled) colors.textTertiary else colors.textDisabled
        val labelColor = if (enabled) colors.textSecondary else colors.textDisabled
        val labelStyle = typography.numeric.copy(fontSize = RulerLabelFontSize, color = labelColor)

        // 标尺主体：与拉环同宽的胶囊形刻度带。
        drawRoundRect(
            color = if (enabled) colors.surfaceRaised else colors.surfaceDisabled,
            topLeft = Offset(trackLeft, 0f),
            size = Size(trackWidth, travelPx),
            cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f),
        )
        drawRoundRect(
            color = colors.divider,
            topLeft = Offset(trackLeft + FrameStroke.toPx() / 2f, FrameStroke.toPx() / 2f),
            size = Size(trackWidth - FrameStroke.toPx(), travelPx - FrameStroke.toPx()),
            cornerRadius = CornerRadius(trackWidth / 2f, trackWidth / 2f),
            style = Stroke(width = FrameStroke.toPx()),
        )

        var minute = firstMinute
        var index = 0
        while (index <= span) {
            val y = travelPx * (1f - index.toFloat() / span)
            val isMajor = index % labelStep == 0
            val tickLength =
                if (isMajor) trackWidth * MajorTickLengthRatio else trackWidth * MinorTickLengthRatio
            drawLine(
                color = tickColor,
                start = Offset(trackRight - TrackPadding.toPx(), y),
                end = Offset(trackRight - TrackPadding.toPx() - tickLength, y),
                strokeWidth = if (isMajor) MajorTickStroke.toPx() else MinorTickStroke.toPx(),
                cap = StrokeCap.Round,
            )
            if (isMajor) {
                val layout = textMeasurer.measure(text = minute.toString(), style = labelStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(
                        x = trackLeft + TrackPadding.toPx(),
                        y = y - layout.size.height / 2f,
                    ),
                )
            }
            index += stepValue
            minute += stepValue
        }

        // 拉环：胶囊 + 描边 + 顶部对齐线 + 当前读数。
        val ringRadius = ringHeight / 2f
        drawRoundRect(
            color = if (enabled) colors.surface else colors.surfaceDisabled,
            topLeft = Offset(trackLeft, ringTop),
            size = Size(trackWidth, ringHeight),
            cornerRadius = CornerRadius(ringRadius, ringRadius),
        )
        drawRoundRect(
            color = colors.divider,
            topLeft = Offset(trackLeft + FrameStroke.toPx() / 2f, ringTop + FrameStroke.toPx() / 2f),
            size = Size(trackWidth - FrameStroke.toPx(), ringHeight - FrameStroke.toPx()),
            cornerRadius = CornerRadius(ringRadius, ringRadius),
            style = Stroke(width = FrameStroke.toPx()),
        )
        drawLine(
            color = colors.accent,
            start = Offset(trackLeft, ringTop),
            end = Offset(trackRight, ringTop),
            strokeWidth = SightStroke.toPx(),
        )
        val readingStyle = typography.numeric.copy(
            fontSize = (ringHeight * RingTextRatio).toSp(),
            color = if (enabled) colors.accent else colors.textDisabled,
        )
        val reading = textMeasurer.measure(text = currentMinute.toString(), style = readingStyle)
        drawText(
            textLayoutResult = reading,
            topLeft = Offset(
                x = centerX - reading.size.width / 2f,
                y = ringTop + ringHeight / 2f - reading.size.height / 2f,
            ),
        )
    }
}



/** 横向卡尺中一分钟的宽度：原版 `timer_scale_normal` 在 xxhdpi 下 70px ≈ 23.33dp。 */
private val CaliperMinuteWidth = 23.33.dp

/** 横向卡尺的越界上限：原版 `OVERSCROLL_LIMIT_PX = 500`（xxhdpi 下约 166.67dp）。 */
private val CaliperOverscrollLimit = 166.67.dp

/** 触发惯性的最小松手速度：原版 `500f * density`。 */
private val MinFlingVelocity = 500.dp

/** 惯性衰减系数：原版 `FlingAnimation.friction = 1.1f`。 */
private val RulerFlingDecay = exponentialDecay<Float>(frictionMultiplier = 1.1f)

/** 越界回弹弹簧：原版 `BOUNDARY_SPRING_DAMPING_RATIO / STIFFNESS`。 */
private val RulerBoundarySpring = spring<Float>(dampingRatio = 0.62f, stiffness = 900f)

/** 横向卡尺的吸附时长：原版 `SETTLE_DURATION_MS = 300`。 */
private const val SettleDurationMillis = 300

/** 横向卡尺的吸附缓动：原版 `DecelerateInterpolator`（factor 1.0）。 */
private val RulerSettleEasing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

/** 位置比较容差（像素），小于它认为已经对齐。 */
private const val PositionEpsilon = 0.5f

/** 越界后单帧允许的最小位移，对应原版 `abs(delta) > 20f` 的判断。 */
private const val MinResistanceDeltaPx = 20f

/** 刻度步长候选值（分钟）。 */
private val TickStepCandidates = listOf(1, 2, 5, 10, 15, 30, 60)

/** 横向卡尺每隔几格画一条长刻度。 */
private const val MajorTickEvery = 5

/** 刻度数字字号，取自原版 8sp 位图标签的视觉大小。 */
private val LabelFontSize = 10.sp

/** 竖向拉环标尺的数字字号。 */
private val RulerLabelFontSize = 10.sp

/** 横向卡尺长刻度占高度的比例。 */
private const val MajorTickHeightRatio = 0.28f

/** 横向卡尺短刻度占高度的比例。 */
private const val MinorTickHeightRatio = 0.16f

/** 横向卡尺数字的纵向起点比例。 */
private const val LabelTopRatio = 0.5f

/** 横向卡尺数字之间的最小间距，低于它只标长刻度。 */
private val MinLabelSpacing = 20.dp

/** 竖向标尺刻度之间的最小间距。 */
private val MinTickSpacing = 8.dp

/** 竖向标尺数字之间的最小间距。 */
private val MinLabelSpacingLarge = 26.dp

/** 竖向拉环标尺的行程上限：原版 `classic_timer_680_ruler_length = 400dp`。 */
private val PullRingTravel = 400.dp

/** 拉环高度与行程的比例：原版 `(1482 - 1200) / 1200`。 */
private const val RingHeightRatio = 282f / 1200f

/** 刻度带宽度与行程的比例：原版 `233f / 1200f`。 */
private const val TrackWidthRatio = 233f / 1200f

/** 竖向标尺越界距离占行程的比例。 */
private const val OverscrollRatio = 0.12f

/** 竖向标尺越界时的位移阻尼。 */
private const val OverscrollDamping = 0.35f

/** 竖向标尺吸附弹簧的阻尼比，小于 1 形成松手回弹。 */
private const val RingSnapDampingRatio = 0.5f

/** 竖向标尺吸附弹簧的刚度。 */
private const val RingSnapStiffness = 700f

/** 拉环内读数文字高度占拉环高度的比例。 */
private const val RingTextRatio = 0.3f

/** 竖向标尺长刻度占刻度带宽度的比例。 */
private const val MajorTickLengthRatio = 0.58f

/** 竖向标尺短刻度占刻度带宽度的比例。 */
private const val MinorTickLengthRatio = 0.3f

/** 刻度与刻度带边缘的间距。 */
private val TrackPadding = 4.dp

/** 刻度线宽。 */
private val MajorTickStroke = 1.4.dp

/** 短刻度线宽。 */
private val MinorTickStroke = 1.dp

/** 准星（横向卡尺）/ 对齐线（竖向拉环）线宽：原版 0.66dp。 */
private val SightStroke = 0.66.dp

/** 外框线宽。 */
private val FrameStroke = 1.dp

