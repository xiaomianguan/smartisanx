package cc.wuersan008.smartisanx.ui.control

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanThemedResources
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 锤子风格环形进度。
 *
 * 对应 framework 里的两个类（`framework/smartisanos.jar` 的 `classes.dex`）：
 *
 * | framework 类 | 说明 | 本组件 |
 * | --- | --- | --- |
 * | `smartisanos.widget.CircleProgressView` | 环形进度本体 | [SmartisanCircleProgress] / [SmartisanCircleProgressIndeterminate] |
 * | `smartisanos.widget.CircleProgressPopup` | 把上面那个 View 放进 `PopupWindow`，带进出场动画 | [SmartisanCircleProgressPopup] |
 *
 * 原版素材取自 framework 资源 `framework-smartisanos-res.apk`：
 *
 * | 资源 | 用途 | 尺寸 |
 * | --- | --- | --- |
 * | `circle` | 最外圈的环底图 | 202×202 px |
 * | `circle_mask` | 扇形遮罩（`PorterDuff.Mode.DST_IN`） | 202×202 px |
 * | `large_progress_indeterminate` | 大号不确定进度环（`progress_large_light` 的旋转层） | 216×216 px |
 * | `circular_progress_download` / `_pause` / `_processing` / `_redo` | 环心的四种状态图标 | 48 / 48 / 138 / 48 px |
 *
 * 还原要点（照抄原版 `CircleProgressView.onDraw` 与 `CircleProgressPopup.initAnim`）：
 * - 先在离屏图层里画一个**实心扇形**（从 `-90°` 起、扫过 `sweepAngle` 度，颜色
 *   `circle_progress_view_arc_color` = `#cccce5ff`），再用 `circle_mask` 做 `DST_IN` 裁成圆环；
 * - 最后把 `circle` 底图拉伸到整块画布叠在最上面（原版 `setBounds(0,0,mWidth,mHeight)`）；
 * - 默认尺寸 90dp（原版 `circle_progress_view_size`）；
 * - 不确定进度：原版用 `ValueAnimator.ofFloat(0f, 360f)` 配默认插值器
 *   `AccelerateDecelerateInterpolator`（本库 [SmartisanMotion.EaseInOut]），默认 1000ms 转一圈；
 * - 弹层进出场：进场 `alpha 0→1` + `scale 0.5→1`（100ms），出场反向（300ms）。
 *
 * ```kotlin
 * // 确定进度
 * SmartisanCircleProgress(progress = 0.65f)
 *
 * // 不确定进度（转圈）
 * SmartisanCircleProgressIndeterminate()
 *
 * // 环心带状态图标
 * SmartisanCircleProgress(progress = 0.3f, state = SmartisanCircleProgressState.Pause)
 *
 * // 弹层
 * SmartisanCircleProgressPopup(visible = loading)
 * ```
 */
/**
 * 确定进度的环形进度。
 *
 * @param progress 进度，`0f..1f`（超出范围会被收敛）；传 `null` 表示**不确定态**，一直转圈。
 * @param modifier 外部修饰符。
 * @param size 直径，默认 90dp（原版 `circle_progress_view_size`）。
 * @param arcColor 扇形颜色，默认取原版 `circle_progress_view_arc_color`。
 * @param state 环心状态图标；`null` 表示不画图标。
 * @param stateIconSize 状态图标尺寸，默认 24dp。
 * @param durationMillis 不确定态转一圈的时长，默认 1000ms（原版 `DEFAULT_CIRCLE_ANIM_DURATION`）。
 * @param contentDescription 无障碍描述。
 */
@Composable
fun SmartisanCircleProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    size: Dp = SmartisanCircleProgressDefaults.Size,
    arcColor: Color = colorResource(R.color.circle_progress_view_arc_color),
    state: SmartisanCircleProgressState? = null,
    stateIconSize: Dp = SmartisanCircleProgressDefaults.StateIconSize,
    durationMillis: Int = SmartisanCircleProgressDefaults.CircleAnimDuration,
    contentDescription: String? = null,
) {
    if (progress == null) {
        // 原版 CircleProgressPopup 的不确定态：一直转圈。
        SmartisanCircleProgressIndeterminate(
            modifier = modifier,
            size = size,
            arcColor = arcColor,
            durationMillis = durationMillis,
            state = state,
            stateIconSize = stateIconSize,
            contentDescription = contentDescription,
        )
        return
    }
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SmartisanCircleRing(
            sweepAngle = safeProgress * 360f,
            modifier =
                Modifier.semantics {
                    contentDescription?.let { this.contentDescription = it }
                    progressBarRangeInfo = ProgressBarRangeInfo(safeProgress, 0f..1f)
                },
            size = size,
            arcColor = arcColor,
        )
        if (state != null) {
            SmartisanIcon(
                res = state.iconRes,
                contentDescription = null,
                modifier = Modifier.size(stateIconSize),
            )
        }
    }
}

/**
 * 不确定进度的环形进度（一直转圈）。
 *
 * @param modifier 外部修饰符。
 * @param size 直径，默认 90dp。
 * @param arcColor 扇形颜色。
 * @param durationMillis 转一圈的时长，默认 1000ms（原版 `DEFAULT_CIRCLE_ANIM_DURATION`）。
 * @param state 环心状态图标。
 * @param stateIconSize 状态图标尺寸。
 * @param contentDescription 无障碍描述。
 */
@Composable
fun SmartisanCircleProgressIndeterminate(
    modifier: Modifier = Modifier,
    size: Dp = SmartisanCircleProgressDefaults.Size,
    arcColor: Color = colorResource(R.color.circle_progress_view_arc_color),
    durationMillis: Int = SmartisanCircleProgressDefaults.CircleAnimDuration,
    state: SmartisanCircleProgressState? = null,
    stateIconSize: Dp = SmartisanCircleProgressDefaults.StateIconSize,
    contentDescription: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "smartisan circle progress")
    val sweep by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis, easing = SmartisanMotion.EaseInOut),
                ),
            label = "sweep",
        )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SmartisanCircleRing(
            sweepAngle = sweep,
            modifier =
                Modifier.semantics {
                    contentDescription?.let { this.contentDescription = it }
                },
            size = size,
            arcColor = arcColor,
        )
        if (state != null) {
            SmartisanIcon(
                res = state.iconRes,
                contentDescription = null,
                modifier = Modifier.size(stateIconSize),
            )
        }
    }
}

/**
 * 大号不确定进度（原版 `progress_large_light`：把 `large_progress_indeterminate`
 * 从 `0°` 转到 `1080°`，即连转三圈）。
 *
 * @param modifier 外部修饰符。
 * @param size 尺寸，默认 72dp（`large_progress_indeterminate` 固有尺寸 216px @3x）。
 * @param durationMillis 连转三圈的时长，默认 1000ms。
 * @param contentDescription 无障碍描述。
 */
@Composable
fun SmartisanCircleProgressLarge(
    modifier: Modifier = Modifier,
    size: Dp = SmartisanCircleProgressDefaults.LargeSize,
    durationMillis: Int = SmartisanCircleProgressDefaults.CircleAnimDuration,
    contentDescription: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "smartisan large circle progress")
    val rotation by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1080f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis, easing = LinearEasing),
                ),
            label = "rotation",
        )
    SmartisanIcon(
        res = R.drawable.large_progress_indeterminate,
        contentDescription = contentDescription,
        modifier =
            modifier.size(size).graphicsLayer {
                rotationZ = rotation
            },
    )
}

/**
 * 环形进度弹层。
 *
 * 对应 framework `smartisanos.widget.CircleProgressPopup`：把 [SmartisanCircleProgressIndeterminate]
 * 叠在内容之上，进场 `alpha 0→1` + `scale 0.5→1`（100ms），出场反向（300ms）。
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     Content()
 *     SmartisanCircleProgressPopup(visible = loading, modifier = Modifier.align(Alignment.Center))
 * }
 * ```
 *
 * @param visible 是否显示。
 * @param modifier 外部修饰符。
 * @param size 直径。
 * @param arcColor 扇形颜色。
 * @param durationMillis 转一圈的时长。
 * @param contentDescription 无障碍描述。
 */
@Composable
fun SmartisanCircleProgressPopup(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = SmartisanCircleProgressDefaults.Size,
    arcColor: Color = colorResource(R.color.circle_progress_view_arc_color),
    durationMillis: Int = SmartisanCircleProgressDefaults.CircleAnimDuration,
    contentDescription: String? = null,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            fadeIn(tween(SmartisanCircleProgressDefaults.EnterDurationMillis)) +
                scaleIn(
                    tween(SmartisanCircleProgressDefaults.EnterDurationMillis),
                    initialScale = SmartisanCircleProgressDefaults.MinScale,
                ),
        exit =
            fadeOut(tween(SmartisanCircleProgressDefaults.ExitDurationMillis)) +
                scaleOut(
                    tween(SmartisanCircleProgressDefaults.ExitDurationMillis),
                    targetScale = SmartisanCircleProgressDefaults.MinScale,
                ),
    ) {
        SmartisanCircleProgressIndeterminate(
            size = size,
            arcColor = arcColor,
            durationMillis = durationMillis,
            contentDescription = contentDescription,
        )
    }
}

/**
 * 环心状态图标。
 *
 * 素材取自 framework 的 `circular_progress_*`（原版 `DownloadProgressView` 用它们
 * 表示下载 / 暂停 / 处理中 / 重试四种状态）。
 */
enum class SmartisanCircleProgressState(
    /** 对应的原版图标资源。 */
    @DrawableRes val iconRes: Int,
) {
    /** 下载中：`circular_progress_download`。 */
    Download(R.drawable.sos_smartisanos_drawable_circular_progress_download),

    /** 已暂停：`circular_progress_pause`。 */
    Pause(R.drawable.sos_smartisanos_drawable_circular_progress_pause),

    /** 处理中：`circular_progress_processing`。 */
    Processing(R.drawable.sos_smartisanos_drawable_circular_progress_processing),

    /** 失败 / 重试：`circular_progress_redo`。 */
    Redo(R.drawable.sos_smartisanos_drawable_circular_progress_redo),
}

/** 环形进度默认值，取自 framework 的 `dimens.xml` 与 `CircleProgressPopup`。 */
object SmartisanCircleProgressDefaults {
    /** 默认直径，原版 `circle_progress_view_size = 90dp`。 */
    val Size: Dp = 90.dp

    /** 大号不确定进度尺寸，`large_progress_indeterminate` 固有 216px @3x。 */
    val LargeSize: Dp = 72.dp

    /** 环心状态图标尺寸。 */
    val StateIconSize: Dp = 24.dp

    /** 转一圈时长，原版 `CircleProgressPopup.DEFAULT_CIRCLE_ANIM_DURATION = 1000`。 */
    const val CircleAnimDuration = 1000

    /** 进场时长，原版 `mStartAnimor.setDuration(100L)`。 */
    const val EnterDurationMillis = 100

    /** 出场时长，原版 `mEndAnimor.setDuration(300L)`。 */
    const val ExitDurationMillis = 300

    /** 进场 / 出场的起始（结束）缩放，原版 `0.5f`。 */
    const val MinScale = 0.5f
}

/**
 * 环形进度本体：画扇形 → 用 `circle_mask` 裁成圆环 → 叠上 `circle` 底图。
 *
 * 逐行对应原版 `CircleProgressView.onDraw`：
 * ```java
 * int sc = canvas.saveLayer(0, 0, mWidth, mHeight, null, 31);
 * canvas.drawArc(0, 0, mWidth, mHeight, -90, mSweepAngle, true, mPaint);
 * mPaint.setXfermode(mXfermode);                       // DST_IN
 * canvas.drawBitmap(mMastBitmap, null, mRect, mPaint);
 * mPaint.setXfermode(null);
 * canvas.restoreToCount(sc);
 * mCircleDrawable.setBounds(0, 0, mWidth, mHeight);
 * mCircleDrawable.draw(canvas);
 * ```
 */
@Composable
private fun SmartisanCircleRing(
    sweepAngle: Float,
    modifier: Modifier,
    size: Dp,
    arcColor: Color,
) {
    val resources = smartisanThemedResources()
    // 遮罩位图按原版从 `circle_mask` 取（原版是 BitmapDrawable）。
    val maskBitmap =
        remember(resources) {
            // 原版从 `circle_mask` 取 BitmapDrawable 的位图；这里沿用主题资源（mask 是纯位图，不依赖 theme）。
            @Suppress("DEPRECATION")
            (resources.getDrawable(R.drawable.circle_mask) as? BitmapDrawable)?.bitmap
        }
    val ringPainter = rememberSmartisanDrawablePainter(R.drawable.circle)
    val arcColorArgb = arcColor.toArgb()
    Canvas(modifier = modifier.size(size)) {
        val width = this.size.width
        val height = this.size.height
        if (width <= 0f || height <= 0f) return@Canvas
        val mask = maskBitmap
        if (mask != null) {
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val layer = native.saveLayer(0f, 0f, width, height, null)
                val paint =
                    Paint().apply {
                        isAntiAlias = true
                        color = arcColorArgb
                    }
                native.drawArc(0f, 0f, width, height, -90f, sweepAngle, true, paint)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                native.drawBitmap(mask, null, RectF(0f, 0f, width, height), paint)
                paint.xfermode = null
                native.restoreToCount(layer)
            }
        }
        // 最外圈的环底图拉伸到整块画布（原版 setBounds(0, 0, mWidth, mHeight)）。
        with(ringPainter) { draw(Size(width, height)) }
    }
}

