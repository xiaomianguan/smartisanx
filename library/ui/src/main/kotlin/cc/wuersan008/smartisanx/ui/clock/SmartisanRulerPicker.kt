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
 *
 * 绘制全部改用原版位图（`timer_caliper_bg1/2_disable/3`、`timer_scale_normal/disable`、
 * `timer_blank`、`timer_680_ruler`、`timer_680_loop_0001..0030`），坐标按位图的固有像素
 * 尺寸换算，不再手绘刻度、卡尺外框与拉环。
 */
package cc.wuersan008.smartisanx.ui.clock

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanTimerDrawables
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
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
 * 把 painter 画进指定的目标矩形（左上角 + 宽高）。
 *
 * 位图 / NinePatch 的拉伸交给 drawable 自己完成，等价于原版的
 * `drawable.setBounds(left, top, right, bottom)` + `drawable.draw(canvas)`。
 */
private fun DrawScope.drawPainterIn(
    painter: Painter,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
) {
    if (width <= 0f || height <= 0f) return
    translate(x, y) { with(painter) { draw(Size(width, height)) } }
}

/** 位图的固有宽度（像素，已含密度缩放）；资源没有固有尺寸时用 [fallback]。 */
private fun Painter.intrinsicWidthPx(fallback: Float): Float =
    intrinsicSize.width.takeIf { it.isFinite() && it > 0f } ?: fallback

/** 位图的固有高度（像素，已含密度缩放）；资源没有固有尺寸时用 [fallback]。 */
private fun Painter.intrinsicHeightPx(fallback: Float): Float =
    intrinsicSize.height.takeIf { it.isFinite() && it > 0f } ?: fallback


/**
 * smartisanx 的横向卡尺（计时器标尺）。
 *
 * 绘制完全由原版位图驱动（对应原版 `TimerRulerView` 用到的 6 张素材）：
 * - `timer_caliper_bg2_disable`：禁用区间底色，铺满整条卡尺；
 * - `timer_caliper_bg3`：可用区间底色，从零刻度一直铺到右端；
 * - `timer_scale_normal` / `timer_scale_disable`：刻度位图。原版 KDoc 写明
 *   「一分钟正好等于刻度位图的宽度」，所以刻度不是手绘线，而是**每分钟平铺一张位图**：
 *   第 n 分钟的位图左边缘在 `zeroX + n × 位图宽`，数字水平居中压在该位图上，
 *   基线固定在卡尺顶部 16.5dp 处（原版 `dp(16.5f)` + `Paint.Align.CENTER`）；
 * - `timer_blank`：零刻度左侧 3 分钟处的前导空白，在卡尺内垂直居中；
 * - `timer_caliper_bg1`：最后叠在最上层的外框，再画一条 0.66dp 的红色准星。
 *
 * 物理参数与原版一致：
 * - 拖到两端继续外拖时按 `over/12`、`over/20` 两段阻尼衰减，越界上限 500px；
 * - 松手速度超过 500dp/s 走惯性（`FlingAnimation` friction 1.1 → 指数衰减 1.1）；
 * - 惯性结束或速度不足时吸附到整分钟（原版 300ms 减速插值）；
 * - 越界时用阻尼 0.62 / 刚度 900 的弹簧回弹（原版 `SpringAnimation` 参数）。
 *
 * @param minutes 当前分钟数，会被收敛到 [range] 内。
 * @param onMinutesChange 分钟数变化回调。
 * @param modifier 外部修饰符；不给高度时按外框位图的固有高度（48dp）兜底。
 * @param range 可选的分钟区间。
 * @param enabled 是否可拖动；禁用时刻度改用 `timer_scale_disable`，数字改用 `textDisabled`。
 * @param frameRes 卡尺外框位图，默认 [SmartisanTimerDrawables.CaliperFrame]。
 * @param fieldRes 可用区间底色位图，默认 [SmartisanTimerDrawables.CaliperFieldEnabled]。
 * @param fieldDisabledRes 禁用区间底色位图，默认 [SmartisanTimerDrawables.CaliperFieldDisabled]。
 * @param scaleRes 刻度位图（一分钟一张），默认 [SmartisanTimerDrawables.CaliperScale]。
 * @param scaleDisabledRes 禁用态刻度位图，默认 [SmartisanTimerDrawables.CaliperScaleDisabled]。
 * @param blankRes 前导空白位图，默认 [SmartisanTimerDrawables.CaliperBlank]。
 */
@Composable
fun SmartisanRulerPicker(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..180,
    enabled: Boolean = true,
    @DrawableRes frameRes: Int = SmartisanTimerDrawables.CaliperFrame,
    @DrawableRes fieldRes: Int = SmartisanTimerDrawables.CaliperFieldEnabled,
    @DrawableRes fieldDisabledRes: Int = SmartisanTimerDrawables.CaliperFieldDisabled,
    @DrawableRes scaleRes: Int = SmartisanTimerDrawables.CaliperScale,
    @DrawableRes scaleDisabledRes: Int = SmartisanTimerDrawables.CaliperScaleDisabled,
    @DrawableRes blankRes: Int = SmartisanTimerDrawables.CaliperBlank,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val haptic = smartisanHaptic()
    val scope = rememberCoroutineScope()

    // 原版素材：painter 的固有尺寸就是位图像素尺寸（已含密度缩放），
    // 所有坐标都以它换算，不经过 Compose 的额外缩放。
    val framePainter = rememberSmartisanDrawablePainter(frameRes)
    val fieldPainter = rememberSmartisanDrawablePainter(fieldRes)
    val fieldDisabledPainter = rememberSmartisanDrawablePainter(fieldDisabledRes)
    val scalePainter = rememberSmartisanDrawablePainter(scaleRes)
    val scaleDisabledPainter = rememberSmartisanDrawablePainter(scaleDisabledRes)
    val blankPainter = rememberSmartisanDrawablePainter(blankRes)

    // 一分钟 = 一张刻度位图的宽度（xxhdpi 下 70px ≈ 23.33dp）。
    val unitPx = scalePainter.intrinsicWidthPx(with(density) { CaliperMinuteWidth.toPx() })
    val scaleHeightPx = scalePainter.intrinsicHeightPx(unitPx * CaliperScaleAspectRatio)
    val blankWidthPx = blankPainter.intrinsicWidthPx(0f)
    val blankHeightPx = blankPainter.intrinsicHeightPx(0f)
    // 外框位图的固有高度（48dp）：卡尺本体按它摆放，父级不给高度时也按它兜底。
    val frameHeightPx = framePainter.intrinsicHeightPx(with(density) { CaliperFrameHeight.toPx() })
    val frameHeightDp = with(density) { frameHeightPx.toDp() }
    val labelBaselinePx = with(density) { CaliperLabelBaseline.toPx() }
    val firstMinute = min(range.first, range.last)
    val span = abs(range.last - range.first)
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
            .fillMaxSize()
            // 原版卡尺视图（`page_timer_modern.xml` 里的 TimerRulerView）就是 48dp 高，
            // 父级给不出高度（例如放在可滚动列表里）时按它兜底，否则画布会塌成 0。
            .height(frameHeightDp),
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val zeroX = centerX - positionPx
        val labelColor = if (enabled) colors.textSecondary else colors.textDisabled
        val labelStyle = typography.numeric.copy(fontSize = LabelFontSize, color = labelColor)

        // 卡尺本体：按外框位图的固有高度居中摆放（位图 144px = 48dp）。
        val caliperHeight = frameHeightPx.coerceAtMost(height)
        val caliperTop = (height - caliperHeight) / 2f

        // 原版画刻度前先把画布裁到卡尺内缩 1px 的范围（`clipRect(1, 1, w - 1, h - 1)`）。
        clipRect(
            left = 1f,
            top = caliperTop + 1f,
            right = width - 1f,
            bottom = caliperTop + caliperHeight - 1f,
        ) {
            // 1. 禁用区间底色：原版 drawField(disabledField, 0, width)。
            drawPainterIn(fieldDisabledPainter, 0f, caliperTop, width, caliperHeight)
            // 2. 可用区间底色：原版 drawField(enabledField, zeroX, width)。
            if (enabled) {
                val fieldLeft = zeroX.coerceIn(0f, width)
                drawPainterIn(fieldPainter, fieldLeft, caliperTop, width - fieldLeft, caliperHeight)
            }
            // 3. 刻度：每分钟平铺一张位图，超出可用范围的分钟换成禁用位图。
            val firstVisible = floor(-zeroX / unitPx).toInt() - 1
            val lastVisible = ceil((width - zeroX) / unitPx).toInt() + 1
            for (index in firstVisible..lastVisible) {
                val inRange = index in 0..span
                val tickPainter = if (enabled && inRange) scalePainter else scaleDisabledPainter
                val tickX = zeroX + index * unitPx
                drawPainterIn(tickPainter, tickX, caliperTop, unitPx, scaleHeightPx)
                if (inRange) {
                    // 数字压在刻度位图上：水平居中，基线在卡尺顶部 16.5dp 处。
                    val layout = textMeasurer.measure(
                        text = (firstMinute + index).toString(),
                        style = labelStyle,
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            x = tickX + (unitPx - layout.size.width) / 2f,
                            y = caliperTop + labelBaselinePx - layout.firstBaseline,
                        ),
                    )
                }
            }
            // 4. 前导空白：零刻度左侧 3 分钟处，在卡尺内垂直居中（原版 leadingBlank）。
            if (zeroX > -blankWidthPx && zeroX < width + blankWidthPx) {
                drawPainterIn(
                    blankPainter,
                    zeroX - 3f * unitPx,
                    caliperTop + (caliperHeight - blankHeightPx) / 2f,
                    blankWidthPx,
                    blankHeightPx,
                )
            }
        }

        // 5. 外框：原版 setBounds(0, 0, width, height)，横向拉满、纵向用固有高度。
        drawPainterIn(framePainter, 0f, caliperTop, width, caliperHeight)
        // 6. 红色准星：原版 0.66dp 竖线，画在外框之上。
        drawLine(
            color = colors.accent,
            start = Offset(centerX, caliperTop),
            end = Offset(centerX, caliperTop + caliperHeight),
            strokeWidth = SightStroke.toPx(),
        )
    }
}


/**
 * smartisanx 的竖向拉环标尺。
 *
 * 绘制用原版 6.8.0 的两套素材：
 * - `timer_680_ruler`（233 × 1482px）作底图：前 1200px 是带刻度的标尺段，其后 282px 是拉环段。
 *   原版 `Classic680RulerView` 把整张位图按「1200px = 400dp 行程」等比缩放，
 *   这里沿用同一个比例，但**只画标尺段**（底图自带的拉环裁掉），刻度因此固定在行程内；
 * - `timer_680_loop_0001..0030` 作拉环本体：帧位图与底图是同一像素比例
 *   （两者绿带宽度都是 133px），按当前分钟在 30 帧里取一帧，顶端对齐拉环位置绘制。
 *
 * 几何与物理参数取自原版：视图宽 80dp、行程 400dp（`classic_timer_680_ruler_length`）、
 * 拉环高 = 行程 × 282/1200、刻度带宽 = 行程 × 233/1200、45 秒阈值触感、快速上拉释放。
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
 * @param modifier 外部修饰符；不给高度时按「行程 + 拉环段」（494dp）兜底。
 * @param range 可选的分钟区间，整条刻度覆盖整个行程。
 * @param enabled 是否可拖动。
 * @param rulerRes 标尺底图，默认 [SmartisanTimerDrawables.PullRingRuler]。
 * @param frames 拉环帧序列（索引 0 为最底、最后一帧为最顶），
 *   默认 [SmartisanTimerDrawables.PullRingFrames]。
 */
@Composable
fun SmartisanPullRingRuler(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..180,
    enabled: Boolean = true,
    @DrawableRes rulerRes: Int = SmartisanTimerDrawables.PullRingRuler,
    frames: List<Int> = SmartisanTimerDrawables.PullRingFrames,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val haptic = smartisanHaptic()
    val scope = rememberCoroutineScope()

    // 标尺底图：固有尺寸 233 × 1482px，刻度段占前 1200px。
    val rulerPainter = rememberSmartisanDrawablePainter(rulerRes)
    val rulerWidthPx = rulerPainter.intrinsicWidthPx(0f)
    val rulerHeightPx = rulerPainter.intrinsicHeightPx(0f)

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

    // 拉环帧：按分钟在序列里取一帧（索引 0 为最底、最后一帧为最顶）。
    // 用 derivedStateOf 收敛重组：只有帧号变化时才重新取位图，拖动过程中不会每帧重组。
    val ringFrameRes = remember(frames, maximumPosition) {
        derivedStateOf {
            if (frames.isEmpty() || maximumPosition <= 0f) {
                frames.firstOrNull()
            } else {
                val progress = (positionPx / maximumPosition).coerceIn(0f, 1f)
                frames[(progress * frames.lastIndex).roundToInt().coerceIn(0, frames.lastIndex)]
            }
        }
    }.value
    val ringPainter = if (ringFrameRes != null) {
        rememberSmartisanDrawablePainter(ringFrameRes)
    } else {
        null
    }
    val ringWidthPx = ringPainter?.intrinsicWidthPx(0f) ?: 0f
    val ringHeightPx = ringPainter?.intrinsicHeightPx(0f) ?: 0f

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
            .fillMaxSize()
            // 原版拉环视图是 80 × 500dp（400dp 行程 + 拉环段）；
            // 父级给不出高度（例如放在可滚动列表里）时按它兜底。
            .height(PullRingMinHeight),
    ) {
        val width = size.width
        if (travelPx <= 0f || unitPx <= 0f || span <= 0) return@Canvas

        val trackWidth = travelPx * TrackWidthRatio
        val centerX = width / 2f
        val trackLeft = centerX - trackWidth / 2f
        // 在绘制作用域里读位置，拖动 / 惯性 / 吸附时逐帧重绘，不需要重组。
        val ringProgress = if (maximumPosition > 0f) {
            (positionPx / maximumPosition).coerceIn(0f, 1f)
        } else {
            0f
        }
        val ringTop = travelPx * (1f - ringProgress)
        val currentMinute = firstMinute + (ringProgress * span).roundToInt()

        // 底图与拉环帧共用同一像素比例：底图的 1200px 刻度段正好等于整个行程。
        val bitmapScale = if (rulerWidthPx > 0f) trackWidth / rulerWidthPx else 0f

        // 1. 标尺底图：只画刻度段（前 1200/1482），底图自带的拉环段裁掉，拉环交给帧序列。
        if (bitmapScale > 0f) {
            clipRect(trackLeft, 0f, trackLeft + trackWidth, travelPx) {
                drawPainterIn(
                    rulerPainter,
                    trackLeft,
                    0f,
                    rulerWidthPx * bitmapScale,
                    rulerHeightPx * bitmapScale,
                )
            }
        }

        // 2. 拉环本体：帧位图的顶端就是标尺末端，所以顶端对齐拉环位置、水平居中。
        val ringDrawWidth = ringWidthPx * bitmapScale
        val ringDrawHeight = ringHeightPx * bitmapScale
        if (ringPainter != null && ringDrawWidth > 0f && ringDrawHeight > 0f) {
            drawPainterIn(
                ringPainter,
                centerX - ringDrawWidth / 2f,
                ringTop,
                ringDrawWidth,
                ringDrawHeight,
            )
        }

        // 3. 读数：原版把读数放在拉环下方的时间文本里，这里沿用组件库的排版，
        //    把数字放进拉环内圈：内圈高约帧高的 0.5，文字再占内圈高度的 0.55。
        val ringSlotHeight = if (ringDrawHeight > 0f) ringDrawHeight else travelPx * RingHeightRatio
        val readingCenterY = if (ringDrawHeight > 0f) {
            ringTop + ringDrawHeight * RingTextCenterRatio
        } else {
            ringTop + ringSlotHeight / 2f
        }
        val readingStyle = typography.numeric.copy(
            fontSize = (ringSlotHeight * RingHoleRatio * RingTextRatio).toSp(),
            color = if (enabled) colors.accent else colors.textDisabled,
        )
        val reading = textMeasurer.measure(text = currentMinute.toString(), style = readingStyle)
        drawText(
            textLayoutResult = reading,
            topLeft = Offset(
                x = centerX - reading.size.width / 2f,
                y = readingCenterY - reading.size.height / 2f,
            ),
        )
    }
}


/** 横向卡尺中一分钟的宽度兜底值：原版 `timer_scale_normal` 在 xxhdpi 下 70px ≈ 23.33dp。 */
private val CaliperMinuteWidth = 23.33.dp

/** 刻度位图的宽高比兜底值：`timer_scale_normal` 是 70 × 21px。 */
private const val CaliperScaleAspectRatio = 21f / 70f

/** 卡尺外框的兜底高度：原版视图与 `timer_caliper_bg1` 都是 48dp（xxhdpi 下 144px）。 */
private val CaliperFrameHeight = 48.dp

/** 刻度数字的基线距卡尺顶部的距离：原版 `dp(16.5f)`。 */
private val CaliperLabelBaseline = 16.5.dp

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

/** 刻度数字字号，取自原版 8sp 位图标签的视觉大小。 */
private val LabelFontSize = 10.sp

/** 准星线宽：原版 0.66dp。 */
private val SightStroke = 0.66.dp

/** 竖向拉环标尺的行程上限：原版 `classic_timer_680_ruler_length = 400dp`。 */
private val PullRingTravel = 400.dp

/** 拉环段高度与行程的比例：原版 `(1482 - 1200) / 1200`。 */
private const val RingHeightRatio = 282f / 1200f

/** 刻度带宽度与行程的比例：原版 `233f / 1200f`。 */
private const val TrackWidthRatio = 233f / 1200f

/** 拉环视图的最小高度：行程 + 拉环段（原版视图 500dp 高、400dp 行程）。 */
private val PullRingMinHeight = PullRingTravel * (1f + RingHeightRatio)

/** 竖向标尺越界距离占行程的比例。 */
private const val OverscrollRatio = 0.12f

/** 竖向标尺越界时的位移阻尼。 */
private const val OverscrollDamping = 0.35f

/** 竖向标尺吸附弹簧的阻尼比，小于 1 形成松手回弹。 */
private const val RingSnapDampingRatio = 0.5f

/** 竖向标尺吸附弹簧的刚度。 */
private const val RingSnapStiffness = 700f

/** 拉环内圈（帧位图里环内侧的镂空）高度占帧高的比例：约 123/246px。 */
private const val RingHoleRatio = 0.5f

/** 拉环内读数文字高度占内圈高度的比例。 */
private const val RingTextRatio = 0.55f

/** 拉环内圈中心的纵向位置占帧位图高度的比例（30 帧的中位帧）。 */
private const val RingTextCenterRatio = 0.36f
