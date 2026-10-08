/**
 * 控件：锤子环形下载进度。
 *
 * 对应原版 `smartisanos.widget.DownloadProgressView`，出现在锤子日历（Calendar 8.1.2）与
 * 锤子邮件（Mail 7.1.0）的附件下载条上。
 *
 * 还原要点（照抄原版 `onDraw` / `onAnimationUpdate` / `onMeasure`）：
 * - 四种状态（原版 `setCurrentState(1..4)`）：1 下载中、2 已暂停、3 失败 / 重试、4 处理中；
 * - 先画外圈（`#e6e6e6`、粗细 = `backRingWidth`），再画内圈（`#f2f2f2`、粗细 = `foreRingWidth`）；
 * - 进度弧从 -90° 起、扫过 `progress × 360 / 100` 度，同样画两遍：
 *   外圈用「纵向线性渐变」的 back 色、内圈用 fore 色；状态 3 改用单一失败色
 *   （原版 `mFailedProgressColor`）；
 * - 渐变的两个端点固定在 `(0, 圆心 − 半径 − 环宽)` 与 `(0, 圆心 + 半径 + 环宽)`（原版 `LinearGradient`）；
 * - 状态图标居中绘制：新状态透明度 0→255、旧状态 255→0，
 *   由 `ValueAnimator.ofInt(0, 255)` 驱动，默认时长 300ms、默认插值器
 *   `AccelerateDecelerateInterpolator`（即本库 [cc.wuersan008.smartisanx.core.anim.SmartisanMotion.EaseInOut]）；
 * - 状态 4 每帧把图标旋转 5°（原版 `onDraw` 里的 `mProcessingDegree += 5`，满 360° 归零，
 *   即 72 帧一圈，60fps 下约 1200ms 一圈）；
 * - 默认尺寸：宽度 36dp、内圈半径 15dp、外环宽 2dp、内环宽 1.3333dp
 *   （原版构造函数的 `a(context, 36f)` / `a(context, 15f)` / `a(context, 2f)` / `a(context, 1.3333334f)`）；
 *   原版 `onMeasure` 要求 `宽度 ≥ 2 × (内圈半径 + 外环宽)`，否则抛异常，这里改为收敛到该下限。
 *
 * 状态图标用的是原版素材 `sos_smartisanos_drawable_circular_progress_download / _pause / _redo /
 * _processing`（取自锤子邮件的 APK；日历里这几个图标来自 framework，拿不到）。
 * 进度弧颜色默认取原版 `res/values/colors.xml` 里的
 * `sos_smartisanos_color_def_back_progress_start/end_color`（`#608aff` → `#5076ff`）、
 * `sos_smartisanos_color_def_fore_progress_start/end_color`（`#33ffffff`）、
 * `sos_smartisanos_color_def_failed_progress_color`（`#26000000`）。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.ColorRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

/** 原版 `DownloadProgressView` 的四种状态（`setCurrentState(1..4)`）。 */
enum class SmartisanProgressState {
    /** 状态 1：下载中，进度弧按 `progress` 显示。 */
    Download,

    /** 状态 2：已暂停。 */
    Pause,

    /** 状态 3：失败 / 重试，进度弧改用失败色。 */
    Retry,

    /** 状态 4：处理中，状态图标每帧旋转 5°。 */
    Processing,
    ;

    /** 对应原版 `setCurrentState` 的整数值。 */
    val originalValue: Int
        get() =
            when (this) {
                Download -> 1
                Pause -> 2
                Retry -> 3
                Processing -> 4
            }
}

/** 状态切换时状态图标淡入淡出的时长，原版 `mAlphaAnimationDuration = 300ms`。 */
private const val AlphaAnimationDurationMillis = 300

/** 状态图标每帧旋转的角度，原版 `mProcessingDegree += 5`。 */
private const val ProcessingStepDegrees = 5f

/** 状态 4 转一圈的时长：原版每帧 5°、满 360° 归零，即 72 帧，60fps 下约 1200ms。 */
private const val ProcessingRevolutionMillis = (360f / ProcessingStepDegrees / 60f * 1000f).toInt()

/** 默认宽度，原版构造函数里的 `a(context, 36f)`。 */
private val DefaultSize = 36.dp

/** 默认内圈半径，原版构造函数里的 `a(context, 15f)`。 */
private val DefaultInnerCircleRadius = 15.dp

/** 默认外环宽度，原版构造函数里的 `a(context, 2f)`。 */
private val DefaultBackRingWidth = 2.dp

/** 默认内环宽度，原版构造函数里的 `a(context, 1.3333334f)`。 */
private val DefaultForeRingWidth = 1.3333334.dp

/** 外圈底色，原版 `onDraw` 里写死的 `#e6e6e6`。 */
private val BackRingColor = Color(0xFFE6E6E6)

/** 内圈底色，原版 `onDraw` 里写死的 `#f2f2f2`。 */
private val ForeRingColor = Color(0xFFF2F2F2)


/**
 * 锤子环形下载进度。
 *
 * ```kotlin
 * SmartisanProgressIndicator(progress = 42, state = SmartisanProgressState.Download)
 * ```
 *
 * @param progress 进度百分比 0..100，对应原版 `setProgress`。
 * @param state 当前状态，对应原版 `setCurrentState`；切换时状态图标做 300ms 淡入淡出。
 * @param modifier 外部修饰符。
 * @param size 整体尺寸，对应原版 `onMeasure` 得到的正方形边长；会被收敛到
 *   `2 × (innerCircleRadius + backRingWidth)` 以上（原版是直接抛异常）。
 * @param innerCircleRadius 进度环半径，对应原版 `inner_circle_radius`。
 * @param backRingWidth 外环宽度，对应原版 `back_ring_width`。
 * @param foreRingWidth 内环宽度，对应原版 `fore_ring_width`。
 * @param backProgressStartColor 外环渐变起始色，对应原版 `back_progress_start_color`。
 * @param backProgressEndColor 外环渐变结束色，对应原版 `back_progress_end_color`。
 * @param foreProgressStartColor 内环渐变起始色，对应原版 `fore_progress_start_color`。
 * @param foreProgressEndColor 内环渐变结束色，对应原版 `fore_progress_end_color`。
 * @param failedProgressColor 状态 3 的进度弧颜色，对应原版 `failed_progress_color`。
 */
@Composable
fun SmartisanProgressIndicator(
    progress: Int,
    modifier: Modifier = Modifier,
    state: SmartisanProgressState = SmartisanProgressState.Download,
    size: Dp = DefaultSize,
    innerCircleRadius: Dp = DefaultInnerCircleRadius,
    backRingWidth: Dp = DefaultBackRingWidth,
    foreRingWidth: Dp = DefaultForeRingWidth,
    backProgressStartColor: Color = Color.Unspecified,
    backProgressEndColor: Color = Color.Unspecified,
    foreProgressStartColor: Color = Color.Unspecified,
    foreProgressEndColor: Color = Color.Unspecified,
    failedProgressColor: Color = Color.Unspecified,
) {
    val density = LocalDensity.current
    val safeProgress = progress.coerceIn(0, 100)
    // 原版 onMeasure 要求 width >= 2 * (inner_circle_radius + back_ring_width)。
    val minSize = (innerCircleRadius + backRingWidth) * 2
    val resolvedSize = if (size < minSize) minSize else size
    val downloadPainter = rememberSmartisanDrawablePainter(SmartisanDrawables.ProgressStateDownload)
    val pausePainter = rememberSmartisanDrawablePainter(SmartisanDrawables.ProgressStatePause)
    val retryPainter = rememberSmartisanDrawablePainter(SmartisanDrawables.ProgressStateRetry)
    val processingPainter = rememberSmartisanDrawablePainter(SmartisanDrawables.ProgressStateProcessing)
    val painterOf: (SmartisanProgressState) -> Painter =
        remember(downloadPainter, pausePainter, retryPainter, processingPainter) {
            { target ->
                when (target) {
                    SmartisanProgressState.Download -> downloadPainter
                    SmartisanProgressState.Pause -> pausePainter
                    SmartisanProgressState.Retry -> retryPainter
                    SmartisanProgressState.Processing -> processingPainter
                }
            }
        }
    // 原版：新状态淡入（0→255）、旧状态淡出（255→0），动画结束后不再画旧状态。
    var previousState by remember { mutableStateOf(state) }
    val stateAlpha = remember { Animatable(1f) }
    LaunchedEffect(state) {
        if (previousState != state) {
            stateAlpha.snapTo(0f)
            stateAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(AlphaAnimationDurationMillis, easing = SmartisanMotion.EaseInOut),
            )
        }
        previousState = state
    }
    // 状态 4：每帧旋转 5°，72 帧一圈。
    val spin =
        rememberInfiniteTransition(label = "smartisan progress spin")
            .animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(ProcessingRevolutionMillis, easing = LinearEasing)),
                label = "smartisan progress spin angle",
            ).value
    val rotation = if (state == SmartisanProgressState.Processing) spin else 0f
    val backStart = backProgressStartColor.orOriginal(R.color.sos_smartisanos_color_def_back_progress_start_color)
    val backEnd = backProgressEndColor.orOriginal(R.color.sos_smartisanos_color_def_back_progress_end_color)
    val foreStart = foreProgressStartColor.orOriginal(R.color.sos_smartisanos_color_def_fore_progress_start_color)
    val foreEnd = foreProgressEndColor.orOriginal(R.color.sos_smartisanos_color_def_fore_progress_end_color)
    val failed = failedProgressColor.orOriginal(R.color.sos_smartisanos_color_def_failed_progress_color)
    Canvas(modifier = modifier.size(resolvedSize)) {
        drawSmartisanProgress(
            progress = safeProgress,
            state = state,
            innerCircleRadius = innerCircleRadius,
            backRingWidth = backRingWidth,
            foreRingWidth = foreRingWidth,
            backStart = backStart,
            backEnd = backEnd,
            foreStart = foreStart,
            foreEnd = foreEnd,
            failed = failed,
            alpha = stateAlpha.value,
            previousState = previousState,
            rotation = rotation,
            painterOf = painterOf,
            density = density.density,
        )
    }
}


/** 未指定颜色时回退到原版 `res/values/colors.xml` 里的同名颜色。 */
@Composable
private fun Color.orOriginal(
    @ColorRes colorRes: Int,
): Color = if (this == Color.Unspecified) colorResource(colorRes) else this

/**
 * 原版 `onDraw` 的绘制顺序：两圈底色 → 两条进度弧（外环用渐变、内环用渐变，
 * 状态 3 换成单一失败色）→ 旧状态图标淡出 → 新状态图标淡入（状态 4 时旋转）。
 */
private fun DrawScope.drawSmartisanProgress(
    progress: Int,
    state: SmartisanProgressState,
    innerCircleRadius: Dp,
    backRingWidth: Dp,
    foreRingWidth: Dp,
    backStart: Color,
    backEnd: Color,
    foreStart: Color,
    foreEnd: Color,
    failed: Color,
    alpha: Float,
    previousState: SmartisanProgressState,
    rotation: Float,
    painterOf: (SmartisanProgressState) -> Painter,
    density: Float,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radiusPx = innerCircleRadius.value * density
    val backWidthPx = backRingWidth.value * density
    val foreWidthPx = foreRingWidth.value * density
    drawCircle(
        color = BackRingColor,
        radius = radiusPx,
        center = center,
        style = Stroke(width = backWidthPx),
    )
    drawCircle(
        color = ForeRingColor,
        radius = radiusPx,
        center = center,
        style = Stroke(width = foreWidthPx),
    )
    val sweep = progress * 360f / 100f
    // 原版两条弧的粗细不同（外环 backRingWidth、内环 foreRingWidth），各用一个 Stroke。
    val outerStroke =
        Stroke(
            width = backWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    val innerStroke =
        Stroke(
            width = foreWidthPx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    val arcTopLeft = Offset(center.x - radiusPx, center.y - radiusPx)
    val arcSize = Size(radiusPx * 2f, radiusPx * 2f)
    val outerBrush =
        if (state == SmartisanProgressState.Retry) {
            Brush.verticalGradient(listOf(failed, failed))
        } else {
            // 原版 LinearGradient 的纵向范围：圆心 ± (半径 + 环宽)。
            Brush.verticalGradient(
                colors = listOf(backStart, backEnd),
                startY = center.y - radiusPx - backWidthPx,
                endY = center.y + radiusPx + backWidthPx,
            )
        }
    drawArc(
        brush = outerBrush,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = outerStroke,
    )
    val innerBrush =
        if (state == SmartisanProgressState.Retry) {
            Brush.verticalGradient(listOf(failed, failed))
        } else {
            Brush.verticalGradient(
                colors = listOf(foreStart, foreEnd),
                startY = center.y - radiusPx - foreWidthPx,
                endY = center.y + radiusPx + foreWidthPx,
            )
        }
    drawArc(
        brush = innerBrush,
        startAngle = -90f,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = innerStroke,
    )
    if (previousState != state) {
        drawSmartisanProgressIcon(painterOf(previousState), center, 1f - alpha, rotation = 0f)
    }
    drawSmartisanProgressIcon(painterOf(state), center, alpha, rotation)
}

/** 居中绘制状态图标（原版按固有尺寸居中、按 `mProcessingDegree` 旋转）。 */
private fun DrawScope.drawSmartisanProgressIcon(
    painter: Painter,
    center: Offset,
    alpha: Float,
    rotation: Float,
) {
    val width = painter.intrinsicSize.width.takeIf { it.isFinite() && it > 0f } ?: return
    val height = painter.intrinsicSize.height.takeIf { it.isFinite() && it > 0f } ?: return
    translate(left = center.x - width / 2f, top = center.y - height / 2f) {
        rotate(degrees = rotation, pivot = Offset(width / 2f, height / 2f)) {
            with(painter) {
                draw(size = Size(width, height), alpha = alpha)
            }
        }
    }
}

