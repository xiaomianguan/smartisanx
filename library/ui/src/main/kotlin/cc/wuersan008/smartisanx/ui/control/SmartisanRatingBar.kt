/**
 * 基础控件：锤子风格五星评分条。
 *
 * 复刻自锤子音乐（Compose）`ui/components/SmartisanRatingBar.kt`：
 * 原版用 `score_empty` / `score_full` 两张位图横向平铺（金色实心星 + 未选中的小圆点），
 * 按住后可以左右拖动连续选分，松手时才提交一次评分，垂直滚动会取消拖动且不改分。
 *
 * 这里同样直接使用这两张原版位图（93px @3x，每颗星占 31dp），按格子宽度等比缩放后居中绘制，
 * 不再手绘五角星；「越过 40% 即选中该星」的判定与原版一致（见 [smartisanRatingAt]）。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import kotlin.math.min
import kotlin.math.roundToInt

/** 禁用时的整体透明度，与原版 `accentDisabled`（0x66 ≈ 0.4）一致。 */
private const val RatingDisabledAlpha = 0.4f

/**
 * 计算某个横坐标对应的评分（纯函数，便于单元测试）。
 *
 * 还原原版 `RatingBar` 的取整方式：先把坐标取整到物理像素，再按「越过一颗星的 40%」
 * 判定选中；坐标超出右边界即满分，为负数即 0 分。
 */
fun smartisanRatingAt(x: Float, width: Float, starCount: Int = 5): Int {
    if (width <= 0f || starCount <= 0) return 0
    val rounded = x.roundToInt().toFloat()
    return when {
        rounded < 0f -> 0
        rounded > width -> starCount
        else -> ((rounded / width) * starCount + 0.6f).roundToInt().coerceIn(0, starCount)
    }
}

/**
 * 在一颗星自己的格子里居中绘制评分位图。
 *
 * 原版把 `score_full` / `score_empty` 按固有尺寸横向平铺（每颗星占 93px @3x = 31dp），
 * 这里保持宽高比缩放到格子宽度后居中；格子宽度等于位图宽度时就是 1:1 绘制。
 * 位图没有固有尺寸（例如被换成了纯色 shape）时直接跳过，避免出现 NaN 尺寸。
 */
private fun DrawScope.drawRatingStar(painter: Painter, left: Float, cell: Float, height: Float) {
    val intrinsic = painter.intrinsicSize
    val intrinsicWidth = intrinsic.width
    val intrinsicHeight = intrinsic.height
    if (!(cell > 0f) || !(intrinsicWidth > 0f) || !(intrinsicHeight > 0f)) return
    val scale = min(cell / intrinsicWidth, height / intrinsicHeight)
    val starWidth = intrinsicWidth * scale
    val starHeight = intrinsicHeight * scale
    translate(left = left + (cell - starWidth) / 2f, top = (height - starHeight) / 2f) {
        with(painter) { draw(Size(starWidth, starHeight)) }
    }
}

/**
 * 锤子风格评分条。
 *
 * ```kotlin
 * var rating by remember { mutableIntStateOf(3) }
 * SmartisanRatingBar(rating = rating, onRatingChange = { rating = it })
 * ```
 *
 * @param onRatingChange 传 `null` 时只展示评分，不响应手势。
 * @param starSize 单颗星所在格子的宽度（也就是星距）；控件高度不会低于
 *   `SmartisanDimens.MinimumTouchTarget`。原版每颗星占 93px @3x = 31dp，位图按格子宽度
 *   等比缩放后居中绘制，想 1:1 还原原版尺寸可以传 `starSize = 31.dp`。
 * @param fullRes 选中星的位图，默认是原版 `score_full`。
 * @param emptyRes 未选中星的位图，默认是原版 `score_empty`。
 */
@Composable
fun SmartisanRatingBar(
    rating: Int,
    onRatingChange: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    starCount: Int = 5,
    enabled: Boolean = true,
    starSize: Dp = 24.dp,
    @DrawableRes fullRes: Int = SmartisanDrawables.ScoreFull,
    @DrawableRes emptyRes: Int = SmartisanDrawables.ScoreEmpty,
) {
    val stars = starCount.coerceAtLeast(1)
    val interactive = enabled && onRatingChange != null
    // 拖动过程中的本地预览值，松手时才提交一次评分。
    val previewState = remember(stars) { mutableIntStateOf(rating.coerceIn(0, stars)) }
    val trackingState = remember(stars) { mutableStateOf(false) }
    val latestRating = rememberUpdatedState(onRatingChange)
    val latestValue = rememberUpdatedState(rating)
    val preview by previewState
    val fullPainter = rememberSmartisanDrawablePainter(fullRes, enabled = enabled)
    val emptyPainter = rememberSmartisanDrawablePainter(emptyRes, enabled = enabled)
    LaunchedEffect(rating, stars) {
        if (!trackingState.value) previewState.intValue = rating.coerceIn(0, stars)
    }
    Canvas(
        modifier =
            modifier
                .size(
                    width = starSize * stars,
                    height = starSize.coerceAtLeast(SmartisanDimens.MinimumTouchTarget),
                )
                .graphicsLayer { alpha = if (enabled) 1f else RatingDisabledAlpha }
                .clipToBounds()
                .semantics {
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(
                            current = preview.toFloat(),
                            range = 0f..stars.toFloat(),
                            steps = (stars - 1).coerceAtLeast(0),
                        )
                    if (interactive) {
                        setProgress { value ->
                            val target = value.roundToInt().coerceIn(0, stars)
                            if (target != latestValue.value) latestRating.value?.invoke(target)
                            true
                        }
                    }
                }
                .then(
                    if (interactive) {
                        Modifier.ratingDragGesture(
                            stars = stars,
                            preview = previewState,
                            tracking = trackingState,
                            latestRating = latestRating,
                            latestValue = latestValue,
                        )
                    } else {
                        Modifier
                    },
                ),
    ) {
        val cell = size.width / stars
        for (index in 0 until stars) {
            drawRatingStar(
                painter = if (index < preview) fullPainter else emptyPainter,
                left = cell * index,
                cell = cell,
                height = size.height,
            )
        }
    }
}

/**
 * 评分条的手势：按下即可连续选分，松手提交一次，垂直滚动时取消且不改分。
 *
 * 逻辑取自原版 `RatingBar` 的 `onTouchEvent` / `onStopTrackingTouch`：
 * 水平超过触摸阈值才算拖动，拖动过程只改本地预览值，提交发生在松手时。
 */
@Composable
private fun Modifier.ratingDragGesture(
    stars: Int,
    preview: MutableIntState,
    tracking: MutableState<Boolean>,
    latestRating: State<((Int) -> Unit)?>,
    latestValue: State<Int>,
): Modifier =
    pointerInput(stars) {
        awaitEachGesture {
            val down = awaitFirstDown()
            val start = latestValue.value
            tracking.value = true
            try {
                val dragStart =
                    awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ ->
                        preview.intValue = smartisanRatingAt(change.position.x, size.width.toFloat(), stars)
                        change.consume()
                    }
                if (dragStart == null) {
                    // 点击在抬起时提交；被列表滚动吃掉的手势不改分。
                    val up = currentEvent.changes.firstOrNull { it.id == down.id }
                    if (up != null && !up.pressed && !up.isConsumed) {
                        preview.intValue = smartisanRatingAt(up.position.x, size.width.toFloat(), stars)
                        up.consume()
                        if (preview.intValue != start) latestRating.value?.invoke(preview.intValue)
                    } else {
                        preview.intValue = latestValue.value
                    }
                } else {
                    val released =
                        horizontalDrag(dragStart.id) { change ->
                            preview.intValue =
                                smartisanRatingAt(change.position.x, size.width.toFloat(), stars)
                            change.consume()
                        }
                    if (released) {
                        currentEvent.changes.firstOrNull { !it.pressed }?.let { up ->
                            preview.intValue =
                                smartisanRatingAt(up.position.x, size.width.toFloat(), stars)
                            up.consume()
                        }
                    }
                    if (preview.intValue != start) latestRating.value?.invoke(preview.intValue)
                }
            } finally {
                tracking.value = false
            }
        }
    }
