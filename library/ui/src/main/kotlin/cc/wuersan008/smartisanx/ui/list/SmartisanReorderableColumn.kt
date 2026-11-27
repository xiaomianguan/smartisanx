package cc.wuersan008.smartisanx.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

/**
 * 可拖动排序的纵向列表。
 *
 * 合并了锤子音乐资料库队列拖拽（`SmartisanListDrag.kt`）与锤子时钟世界时钟列表
 * （`WorldClockListView.kt`）的让位逻辑：长按后拖动被拖行，其余行用弹簧动画让出空位，
 * 松手时只提交一次顺序变更。
 *
 * 使用普通 `Column` 而非 `LazyColumn`，适合设置项、世界时钟这类数量有限的列表。
 */
@Composable
fun <T> SmartisanReorderableColumn(
    items: List<T>,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    dragEnabled: Boolean = true,
    onDragIndexChange: (Int?) -> Unit = {},
    @DrawableRes dragBackgroundRes: Int? = SmartisanDrawables.ListDragBackground,
    dragHandleWidth: Dp? = null,
    content: @Composable (index: Int, item: T, dragging: Boolean) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val move by rememberUpdatedState(onMove)
    val notifyDrag by rememberUpdatedState(onDragIndexChange)
    // 只在手势回调里读取，不需要触发重组，因此用普通 Map 即可。
    val heights = remember { HashMap<Int, Int>() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragDelta by remember { mutableFloatStateOf(0f) }

    fun resetDrag() {
        draggingIndex = -1
        targetIndex = -1
        dragDelta = 0f
        notifyDrag(null)
    }

    Column(
        modifier =
            modifier.pointerInput(items, dragEnabled, dragHandleWidth, layoutDirection) {
                if (!dragEnabled) return@pointerInput
                fun indexAt(y: Float): Int? {
                    var bottom = 0
                    return items.indices.firstOrNull { index ->
                        val top = bottom
                        bottom += heights[index] ?: 0
                        y >= top && y < bottom
                    }
                }

                fun startDrag(index: Int) {
                    draggingIndex = index
                    targetIndex = index
                    dragDelta = 0f
                    notifyDrag(index)
                }

                fun updateDrag(amount: Float) {
                    dragDelta += amount
                    val top = (0 until draggingIndex).sumOf { heights[it] ?: 0 }
                    val center = top + (heights[draggingIndex] ?: 0) / 2f + dragDelta
                    targetIndex = indexAt(center) ?: if (center < 0f) 0 else items.lastIndex
                }

                fun finishDrag() {
                    if (draggingIndex >= 0 && targetIndex >= 0 && targetIndex != draggingIndex) {
                        move(draggingIndex, targetIndex)
                    }
                    resetDrag()
                }

                if (dragHandleWidth == null) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset -> indexAt(offset.y)?.let(::startDrag) },
                        onDrag = { change, amount ->
                            if (draggingIndex >= 0) {
                                change.consume()
                                updateDrag(amount.y)
                            }
                        },
                        onDragEnd = ::finishDrag,
                        onDragCancel = ::resetDrag,
                    )
                } else {
                    val handlePx = dragHandleWidth.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        val distanceFromEnd = if (layoutDirection == LayoutDirection.Ltr) {
                            size.width - down.position.x
                        } else {
                            down.position.x
                        }
                        val index = indexAt(down.position.y)
                        if (index != null && distanceFromEnd <= handlePx) {
                            down.consume()
                            startDrag(index)
                            var released = false
                            while (!released) {
                                val change = awaitPointerEvent(PointerEventPass.Initial)
                                    .changes.firstOrNull { it.id == down.id } ?: break
                                released = !change.pressed
                                if (!released) updateDrag(change.position.y - change.previousPosition.y)
                                change.consume()
                            }
                            if (released) finishDrag() else resetDrag()
                        }
                    }
                }
            },
    ) {
        items.forEachIndexed { index, item ->
            val draggedHeight = heights[draggingIndex] ?: 0
            val shift =
                when {
                    draggingIndex < 0 || targetIndex < 0 || index == draggingIndex -> 0f
                    draggingIndex < targetIndex && index in (draggingIndex + 1)..targetIndex -> -draggedHeight.toFloat()
                    draggingIndex > targetIndex && index in targetIndex until draggingIndex -> draggedHeight.toFloat()
                    else -> 0f
                }
            val isDragging = index == draggingIndex
            val animatedShift by animateFloatAsState(
                targetValue = shift,
                animationSpec = SmartisanMotion.SettleSpring,
                label = "smartisan reorder shift",
            )
            Box(
                modifier =
                    Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        // 拖拽中的行用原版 list_drag_bg 作为背景。
                        .then(
                            if (isDragging && dragBackgroundRes != null) {
                                Modifier.smartisanDrawableBackground(dragBackgroundRes)
                            } else {
                                Modifier
                            },
                        )
                        .onSizeChanged { heights[index] = it.height }
                        .graphicsLayer {
                            translationY = if (isDragging) dragDelta else animatedShift
                            shadowElevation = if (isDragging) 8f else 0f
                        },
            ) {
                content(index, item, isDragging)
            }
        }
    }
}
