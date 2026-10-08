package cc.wuersan008.smartisanx.ui.list

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import androidx.compose.ui.res.stringResource
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 侧滑删除行。
 *
 * 用 Compose 重写了锤子时钟的 `SmartisanSwipeDeleteRow`（`custom/SmartisanSwipeDeleteRow.kt`
 * 与 `SmartisanSwipeDeleteMotion.kt`）：
 * 向物理右侧滑动时，前 65dp 为 1:1 直接位移，之后按 1/5 速度阻尼，最大 360dp；
 * 松手后超过阈值（原版 `OPEN_THRESHOLD_DP` = 50dp）则删除，否则回弹归位。
 */
@Composable
fun SmartisanSwipeToDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    directReveal: Dp = 65.dp,
    maximumTravel: Dp = 360.dp,
    threshold: Dp = 50.dp,
    deleteLabel: String? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    // 默认取本地化文案；调用方传值即可覆盖。
    val resolvedDeleteLabel = deleteLabel ?: stringResource(R.string.smartisan_delete)
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
            modifier =
                Modifier
                    .matchParentSize()
                    // 原版删除面板用的是平铺的红色纹理 list_edit_remove_background。
                    .smartisanDrawableBackground(SmartisanDrawables.ListRemoveBackground),
            contentAlignment = Alignment.CenterStart,
        ) {
            // 面板上的图标就是原版的 slide_delete（按下换成 slide_delete_down）。
            SmartisanIcon(
                res =
                    if (dragging) {
                        SmartisanDrawables.SlideDeletePressed
                    } else {
                        SmartisanDrawables.SlideDelete
                    },
                contentDescription = resolvedDeleteLabel,
                modifier = Modifier.padding(start = 24.dp).size(DeleteIconSize),
                contentScale = ContentScale.Fit,
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

/** 删除面板上图标的尺寸（原版 slide_delete 是 90×90 的 30dp 图标）。 */
private val DeleteIconSize = 30.dp
