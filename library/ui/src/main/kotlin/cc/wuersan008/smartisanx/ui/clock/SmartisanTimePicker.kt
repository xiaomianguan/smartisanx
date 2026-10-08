package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.abs

/**
 * smartisanx 的滚轮选择器。
 *
 * 复刻自锤子时钟的自定义 View `SmartisanTimePickerView`（XML + Canvas + Scroller 实现的三列时间滚轮）。
 * 原实现自己维护 `scrollOffset`、用 `Scroller` 做惯性、`ValueAnimator` 做吸附，并按
 * 「离中心行的距离」在普通字号 15dp / 选中字号 18dp 与灰色 / 蓝色之间插值。
 *
 * 这里改用 `LazyColumn` + `rememberLazyListState` + `rememberSnapFlingBehavior(SnapPosition.Center)`：
 * 吸附交给框架的 targeted fling（真实可用、可无障碍滚动），渐隐与字号插值仍按原版的
 * 「距离中心行的比例」计算，并用上下两条参考线勾出选中行。
 *
 * @param items 滚轮内容。
 * @param selectedIndex 当前选中项下标（会被收敛到合法范围）。
 * @param onSelectedIndexChange 选中项变化回调，滚动停止后触发一次。
 * @param modifier 外部修饰符。
 * @param visibleCount 可见行数，偶数会被向上取整为奇数，至少 3 行。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeight: Dp = 40.dp,
) {
    if (items.isEmpty()) return
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val scope = rememberCoroutineScope()

    val rows = visibleCount.coerceAtLeast(MinVisibleCount).let { if (it % 2 == 0) it + 1 else it }
    val halfRows = rows / 2
    val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)

    // 首尾各放一个半视口高的占位项，让第一项与最后一项也能停在视口正中；
    // 不使用 contentPadding 是因为「滚到第 i 项」与「第 i 项居中」在带内边距时
    // 语义不一致，占位项 + 负偏移的写法与框架的 Start 语义完全对齐、不依赖实现细节。
    val spacerHeight = itemHeight * halfRows
    val spacerPx = with(LocalDensity.current) { spacerHeight.roundToPx() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = safeIndex + 1,
        initialFirstVisibleItemScrollOffset = -spacerPx,
    )
    // 中心吸附：视口正中始终对齐某一整项（含首尾项，因为占位项中心不会比整项更靠近中心）。
    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center,
    )

    // 列表下标 0 与 items.size + 1 是占位项，真实项下标 = 列表下标 - 1。
    val centeredListIndex by remember(listState, safeIndex) {
        derivedStateOf { listState.smartisanCenteredIndex(safeIndex + 1) }
    }
    val centeredIndex = (centeredListIndex - 1).coerceIn(0, items.lastIndex)

    // 外部改选中项 → 平滑滚到对应行（此时该项正好居中，不会与吸附互相打架）。
    LaunchedEffect(safeIndex) {
        if (safeIndex != centeredIndex) {
            listState.animateScrollToItem(safeIndex + 1, scrollOffset = -spacerPx)
        }
    }
    // 内部滚动 → 等惯性结束再上报，避免快速滚动时连续回调。
    LaunchedEffect(centeredIndex) {
        if (centeredIndex != safeIndex) {
            snapshotFlow { listState.isScrollInProgress }.first { !it }
            onSelectedIndexChange(centeredIndex)
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * rows)
            .drawWithContent {
                drawContent()
                // 选中行上下两条参考线，替代原版滚轮的高亮背景块。
                val centerY = size.height / 2f
                val halfItem = itemHeight.toPx() / 2f
                drawLine(
                    color = colors.divider,
                    start = Offset(0f, centerY - halfItem),
                    end = Offset(size.width, centerY - halfItem),
                    strokeWidth = 1f,
                )
                drawLine(
                    color = colors.divider,
                    start = Offset(0f, centerY + halfItem),
                    end = Offset(size.width, centerY + halfItem),
                    strokeWidth = 1f,
                )
            },
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item(key = TopSpacerKey) {
                Spacer(modifier = Modifier.height(spacerHeight))
            }
            itemsIndexed(items, key = { index, _ -> index }) { index, label ->
                val fraction by remember(index, items.size) {
                    derivedStateOf { listState.smartisanCenteredFraction(index + 1) }
                }
                val style = typography.numeric.copy(
                    fontSize = lerp(ItemFontSize, SelectedItemFontSize, fraction),
                    fontWeight = if (fraction > SelectedWeightThreshold) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    color = lerp(colors.textTertiary, colors.accent, fraction),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        // 上下行渐隐：alpha 只影响绘制，不触发重新布局。
                        .graphicsLayer { alpha = MinItemAlpha + (1f - MinItemAlpha) * fraction }
                        .smartisanClickable {
                            onSelectedIndexChange(index)
                            scope.launch {
                                listState.animateScrollToItem(index + 1, scrollOffset = -spacerPx)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    SmartisanText(text = label, style = style, maxLines = 1)
                }
            }
            item(key = BottomSpacerKey) {
                Spacer(modifier = Modifier.height(spacerHeight))
            }
        }
    }
}
/**
 * smartisanx 的三列时间滚轮（时 / 分 / 可选上午下午）。
 *
 * 复刻自锤子时钟的 `SmartisanTimePickerView`：固定 5 行可见高度、每行 40dp、
 * 24 小时制用补零两位数字（`08`），12 小时制用 1..12 与「上午 / 下午」列。
 * 原实现是一个自绘三列滚轮，这里由三个 [SmartisanWheelPicker] 列拼成，
 * 中间的细分隔线对应原版滚轮的列分隔背景。
 *
 * @param hour 当前小时（0..23）。
 * @param minute 当前分钟（0..59）。
 * @param onTimeChange 时间变化回调，始终返回 24 小时制的时与分。
 * @param modifier 外部修饰符。
 * @param use24Hour 是否使用 24 小时制；为 false 时额外显示「上午 / 下午」列。
 * @param minuteStep 分钟列步长（例如 5 表示只可选 00/05/10…），最小为 1。
 */
@Composable
fun SmartisanTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    use24Hour: Boolean = true,
    minuteStep: Int = 1,
) {
    val colors = LocalSmartisanColors.current
    val step = minuteStep.coerceAtLeast(1)
    val hourLabels = remember(use24Hour) {
        if (use24Hour) {
            (0 until HOURS_PER_DAY).map { it.toString().padStart(2, '0') }
        } else {
            (1..HOURS_PER_CYCLE).map { it.toString() }
        }
    }
    val minuteLabels = remember(step) {
        (0 until MINUTES_PER_HOUR step step).map { it.toString().padStart(2, '0') }
    }
    val periodLabels = remember { listOf(PeriodAm, PeriodPm) }

    val safeHour = hour.mod(HOURS_PER_DAY)
    val safeMinute = minute.coerceIn(0, MINUTES_PER_HOUR - 1)
    val hourIndex = if (use24Hour) safeHour else hourLabelIndex12(safeHour)
    val minuteIndex = ((safeMinute + step / 2) / step).coerceIn(0, minuteLabels.lastIndex)
    val periodIndex = if (safeHour < HOURS_PER_CYCLE) 0 else 1

    Row(modifier = modifier) {
        SmartisanWheelPicker(
            items = hourLabels,
            selectedIndex = hourIndex,
            onSelectedIndexChange = { index ->
                val newHour = if (use24Hour) index else combineHour12(index, periodIndex)
                onTimeChange(newHour, safeMinute)
            },
            modifier = Modifier.weight(1f),
            itemHeight = TimePickerItemHeight,
        )
        TimePickerColumnDivider()
        SmartisanWheelPicker(
            items = minuteLabels,
            selectedIndex = minuteIndex,
            onSelectedIndexChange = { index -> onTimeChange(safeHour, index * step) },
            modifier = Modifier.weight(1f),
            itemHeight = TimePickerItemHeight,
        )
        if (!use24Hour) {
            TimePickerColumnDivider()
            SmartisanWheelPicker(
                items = periodLabels,
                selectedIndex = periodIndex,
                onSelectedIndexChange = { index ->
                    onTimeChange(combineHour12(hourIndex, index), safeMinute)
                },
                modifier = Modifier.weight(PeriodColumnWeight),
                itemHeight = TimePickerItemHeight,
            )
        }
    }
}

/** 时间滚轮的列分隔线：只覆盖选中行附近的区域，与原版滚轮的列分隔背景一致。 */
@Composable
private fun RowScope.TimePickerColumnDivider() {
    Box(
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .width(SmartisanDimens.DividerThickness)
            .height(TimePickerItemHeight * DividerRowSpan)
            .background(LocalSmartisanColors.current.rowDivider),
    )
}

/** 12 小时制下 1..12 在列中的下标（`0` 点对应标签 `12`）。 */
private fun hourLabelIndex12(hour24: Int): Int = (hour24 % HOURS_PER_CYCLE + HOURS_PER_CYCLE - 1) % HOURS_PER_CYCLE

/** 把 12 小时制的列下标与上午 / 下午合并回 24 小时制。 */
private fun combineHour12(labelIndex: Int, periodIndex: Int): Int =
    (labelIndex + 1) % HOURS_PER_CYCLE + periodIndex * HOURS_PER_CYCLE



/**
 * 视口正中那一项的可见比例：1 表示完全居中，0 表示已到视口边缘。
 *
 * 原版滚轮用同一套「离中心行的距离」同时驱动字号、颜色与渐隐，这里保持一致。
 */
private fun LazyListState.smartisanCenteredFraction(index: Int): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return 0f
    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val halfViewport = (info.viewportEndOffset - info.viewportStartOffset) / 2f
    if (halfViewport <= 0f) return 0f
    val distance = abs(item.offset + item.size / 2f - viewportCenter)
    return (1f - distance / halfViewport).coerceIn(0f, 1f)
}

/** 距离视口中心最近的一项下标，即当前选中行。 */
private fun LazyListState.smartisanCenteredIndex(fallback: Int): Int {
    val info = layoutInfo
    if (info.visibleItemsInfo.isEmpty()) return fallback
    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    return info.visibleItemsInfo
        .minByOrNull { abs(it.offset + it.size / 2f - viewportCenter) }
        ?.index
        ?: fallback
}

/** 普通行字号，取自原版 `normalTextSize = 15dp`。 */
private val ItemFontSize = 15.sp

/** 选中行字号，取自原版 `selectedTextSize = 18dp`。 */
private val SelectedItemFontSize = 18.sp

/** 超过该比例时使用粗体，对应原版选中行的加粗。 */
private const val SelectedWeightThreshold = 0.5f

/** 远离中心行的最低不透明度，形成上下渐隐。 */
private const val MinItemAlpha = 0.15f

/** 最少可见行数。 */
private const val MinVisibleCount = 3

/** 顶部占位项的 key。 */
private const val TopSpacerKey = "smartisan-wheel-top-spacer"

/** 底部占位项的 key。 */
private const val BottomSpacerKey = "smartisan-wheel-bottom-spacer"

/** 时间滚轮的固定行高，原版 `SmartisanTimePickerView` 的 40dp 行高。 */
private val TimePickerItemHeight = 40.dp

/** 上午文案。 */
private const val PeriodAm = "上午"

/** 下午文案。 */
private const val PeriodPm = "下午"

/** 上午下午列相对小时列的宽度权重。 */
private const val PeriodColumnWeight = 0.8f

/** 列分隔线覆盖的行数。 */
private const val DividerRowSpan = 3

/** 一天的小时数。 */
private const val HOURS_PER_DAY = 24

/** 表盘一圈的小时数。 */
private const val HOURS_PER_CYCLE = 12

/** 一小时的分钟数。 */
private const val MINUTES_PER_HOUR = 60

