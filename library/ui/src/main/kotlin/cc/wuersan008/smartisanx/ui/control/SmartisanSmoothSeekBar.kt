package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.R
import kotlin.math.roundToInt

/**
 * 锤子风格滑杆。
 *
 * 对应 framework 里的 `smartisanos.widget.SmoothSeekBar`
 * （锤子自有 framework，`framework/smartisanos.jar` 的 `classes.dex`）。
 * 设置页里的亮度、音量、字体大小等都用它。**此前本库完全没有滑杆**，这是补上的第一个。
 *
 * 原版素材取自 framework 资源 `framework-smartisanos-res.apk`：
 *
 * | 资源 | 用途 | 尺寸 |
 * | --- | --- | --- |
 * | `progress_control` | 滑块 | 118×147 px |
 * | `progress_control_disabled` | 禁用态滑块 | 108×144 px |
 *
 * ```kotlin
 * var brightness by remember { mutableFloatStateOf(0.6f) }
 * SmartisanSmoothSeekBar(value = brightness, onValueChange = { brightness = it })
 * ```
 *
 * @param value 当前值，0f..1f
 * @param steps 大于 0 时按等分吸附（原版刻度的用法）
 */
@Composable
fun SmartisanSmoothSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    trackColor: Color = LocalSmartisanColors.current.divider,
    progressColor: Color = LocalSmartisanColors.current.accent,
    thumbRes: Int = R.drawable.progress_control,
    thumbDisabledRes: Int = R.drawable.progress_control_disabled,
    height: Dp = 48.dp,
    contentDescription: String? = null,
) {
    val density = LocalDensity.current
    val haptic = smartisanHaptic()
    val thumb = rememberSmartisanDrawablePainter(if (enabled) thumbRes else thumbDisabledRes)
    var widthPx by remember { mutableFloatStateOf(1f) }

    val thumbHeightPx = with(density) { 49.dp.toPx() }

    fun snap(raw: Float): Float {
        if (steps <= 0) return raw.coerceIn(0f, 1f)
        val step = 1f / steps
        return (raw / step).roundToInt().coerceIn(0, steps) * step
    }

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .semantics {
                    contentDescription?.let { this.contentDescription = it }
                    progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                }
                .pointerInput(enabled, steps, widthPx) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            haptic()
                            onValueChange(snap(offset.x / widthPx))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            onValueChange(snap(change.position.x / widthPx))
                        },
                    )
                },
    ) {
        widthPx = size.width
        val centerY = size.height / 2f
        val stroke = 2.dp.toPx()
        val thumbWidth = thumb.intrinsicSize.width.takeIf { it > 0f } ?: stroke * 6f
        // 轨道两端各留半个滑块，滑块才不会超出边界。
        val trackStart = thumbWidth / 2f
        val trackEnd = size.width - thumbWidth / 2f
        val trackWidth = (trackEnd - trackStart).coerceAtLeast(1f)
        val thumbX = trackStart + trackWidth * value.coerceIn(0f, 1f)
        drawLine(trackColor, Offset(trackStart, centerY), Offset(trackEnd, centerY), stroke)
        drawLine(progressColor, Offset(trackStart, centerY), Offset(thumbX, centerY), stroke)
        drawThumb(thumb, thumbX, centerY, thumbHeightPx)
    }
}

/** 把滑块位图按固有尺寸居中画在 (centerX, centerY)。 */
private fun DrawScope.drawThumb(
    painter: Painter,
    centerX: Float,
    centerY: Float,
    thumbHeight: Float,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.width <= 0f || intrinsic.height <= 0f) return
    val scale = if (thumbHeight > 0f) thumbHeight / intrinsic.height else 1f
    val w = intrinsic.width * scale
    val h = intrinsic.height * scale
    translate(centerX - w / 2f, centerY - h / 2f) {
        scale(scale, scale, pivot = Offset.Zero) {
            with(painter) { draw(intrinsic) }
        }
    }
}
