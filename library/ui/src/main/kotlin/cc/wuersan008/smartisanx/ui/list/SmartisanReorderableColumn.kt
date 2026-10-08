package cc.wuersan008.smartisanx.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion

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
    content: @Composable (index: Int, item: T, dragging: Boolean) -> Unit,
) {
    // 只在手势回调里读取，不需要触发重组，因此用普通 Map 即可。
    val bounds = remember { HashMap<Int, IntRange>() }
    val heights = remember { HashMap<Int, Int>() }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var dragDelta by remember { mutableFloatStateOf(0f) }

    fun resetDrag() {
        draggingIndex = -1
        targetIndex = -1
        dragDelta = 0f
        onDragIndexChange(null)
    }

    Column(
        modifier =
            modifier.pointerInput(items, dragEnabled) {
                if (!dragEnabled) return@pointerInput
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        val index =
                            bounds.entries
                                .firstOrNull { offset.y >= it.value.first && offset.y <= it.value.last }
                                ?.key
                        if (index != null) {
                            draggingIndex = index
                            targetIndex = index
                            dragDelta = 0f
                            onDragIndexChange(index)
                        }
                    },
                    onDrag = { change, amount ->
                        if (draggingIndex >= 0) {
                            dragDelta += amount.y
                            change.consume()
                            val range = bounds[draggingIndex]
                            if (range != null) {
                                val center = (range.first + range.last) / 2f + dragDelta
                                val hit =
                                    bounds.entries
                                        .filter { it.key != draggingIndex }
                                        .firstOrNull { center >= it.value.first && center <= it.value.last }
                                        ?.key
                                targetIndex = hit ?: draggingIndex
                            }
                        }
                    },
                    onDragEnd = {
                        if (draggingIndex >= 0 && targetIndex >= 0 && targetIndex != draggingIndex) {
                            onMove(draggingIndex, targetIndex)
                        }
                        resetDrag()
                    },
                    onDragCancel = { resetDrag() },
                )
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
                        .graphicsLayer {
                            translationY = if (isDragging) dragDelta else animatedShift
                            shadowElevation = if (isDragging) 8f else 0f
                        }
                        .onGloballyPositioned { coordinates ->
                            val top = coordinates.positionInParent().y.roundToInt()
                            bounds[index] = top..(top + coordinates.size.height)
                            heights[index] = coordinates.size.height
                        },
            ) {
                content(index, item, isDragging)
            }
        }
    }
}
