package top.smartisanx.ui.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.smartisanx.core.anim.SmartisanMotion
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanText

/**
 * 侧滑删除行。
 *
 * 用 Compose 重写了锤子时钟的 `SmartisanSwipeDeleteRow`（`custom/SmartisanSwipeDeleteRow.kt`
 * 与 `SmartisanSwipeDeleteMotion.kt`）：
 * 向物理右侧滑动时，前 65dp 为 1:1 直接位移，之后按 1/5 速度阻尼，最大 360dp；
 * 松手后超过阈值则删除，否则回弹归位。
 */
@Composable
fun SmartisanSwipeToDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    directReveal: Dp = 65.dp,
    maximumTravel: Dp = 360.dp,
    threshold: Dp = 120.dp,
    deleteLabel: String = "删除",
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val directPx = with(density) { directReveal.toPx() }
    val maximumPx = with(density) { maximumTravel.toPx() }
    val thresholdPx = with(density) { threshold.toPx() }

    val settle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var dragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rawTravel by remember { mutableFloatStateOf(0f) }

    fun resisted(raw: Float): Float =
        if (raw <= directPx) raw else (directPx + (raw - directPx) / 5f).coerceAtMost(maximumPx)

    val displayedOffset = if (dragging) dragOffset else settle.value

    Box(modifier) {
        // 删除面板由内容决定整体尺寸：matchParentSize 不参与父级测量，
        // 因此行高仍然由 content 决定，面板只是覆盖在内容下面。
        Box(
            modifier = Modifier.matchParentSize().background(colors.accent),
            contentAlignment = Alignment.CenterStart,
        ) {
            SmartisanText(
                text = deleteLabel,
                modifier = Modifier.padding(start = 24.dp),
                style = typography.button,
                color = colors.onAccent,
                maxLines = 1,
            )
        }
        Box(
            modifier =
                Modifier
                    .graphicsLayer { translationX = displayedOffset }
                    .then(
                        if (enabled) {
                            Modifier.pointerInput(directPx, maximumPx, thresholdPx) {
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        dragging = true
                                        rawTravel = settle.value
                                        dragOffset = settle.value
                                    },
                                    onDragEnd = {
                                        dragging = false
                                        val finalOffset = dragOffset
                                        scope.launch {
                                            settle.snapTo(finalOffset)
                                            if (finalOffset >= thresholdPx) {
                                                settle.animateTo(maximumPx, tween(SmartisanMotion.DurationShort))
                                                onDelete()
                                                settle.snapTo(0f)
                                            } else {
                                                settle.animateTo(0f, SmartisanMotion.easeInOut())
                                            }
                                        }
                                        rawTravel = 0f
                                    },
                                    onDragCancel = {
                                        dragging = false
                                        rawTravel = 0f
                                        scope.launch {
                                            settle.snapTo(dragOffset)
                                            settle.animateTo(0f, SmartisanMotion.easeInOut())
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        rawTravel = (rawTravel + dragAmount).coerceIn(0f, maximumPx)
                                        dragOffset = resisted(rawTravel)
                                    },
                                )
                            }
                        } else {
                            Modifier
                        },
                    )
                    .background(colors.surface),
        ) {
            content()
        }
    }
}
