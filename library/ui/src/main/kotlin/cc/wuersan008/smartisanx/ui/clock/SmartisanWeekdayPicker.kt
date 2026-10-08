package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import java.time.DayOfWeek

/**
 * smartisanx 的重复日选择器（闹钟「重复」设置）。
 *
 * 复刻自锤子时钟的 `AlarmRepeatDaysView`：七行「周一…周日」+ 行尾复选框。
 * 原实现除了常规点击，还支持**快速选择手势**——在复选框列按下时先决定目标状态
 * （已选则取消、未选则选中），再纵向拖动，把同一状态刷过经过的每一行。
 * 原实现是在 `onTouchEvent` 里处理这件事，这里改为 Compose 的手势检测：
 *
 * - 行点击（`smartisanClickable`，无涟漪）负责常规切换与无障碍语义；
 * - 复选框列单独挂一个 `pointerInput`：按下即定状态、拖动即刷过经过的行，
 *   并消费事件避免父级列表跟着滚动（对应原版的 `requestDisallowInterceptTouchEvent`）；
 * - 拖动经过的行用 `pressedHighlight` 蓝色底 + `onPressedHighlight` 文字高亮，
 *   与原版行按压态一致；
 * - 选中态是方正的蓝色方块 + 对勾（`pressedHighlight` / `onPressedHighlight`），
 *   未选中态是 `textTertiary` 描边方块。
 *
 * @param selectedDays 当前选中的星期集合。
 * @param onSelectedDaysChange 选中集合变化回调，始终返回新的集合。
 * @param modifier 外部修饰符。
 * @param labels 七行文案，默认周一到周日。
 */
@Composable
fun SmartisanWeekdayPicker(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
    labels: List<String> = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    // 手势协程不能因为父级状态变化而重启（否则拖动会被打断），
    // 所以用 rememberUpdatedState 读取最新值，pointerInput 的 key 保持稳定。
    val currentSelection = rememberUpdatedState(selectedDays)
    val currentCallback = rememberUpdatedState(onSelectedDaysChange)
    var pressedDay by remember { mutableIntStateOf(NoDay) }

    Column(modifier = modifier.fillMaxWidth()) {
        for (index in 0 until WeekdayCount) {
            val day = DayOfWeek.of(index + 1)
            val selected = day in selectedDays
            // 常规点击的按压态：行底色换成原版的蓝色高亮，与拖动刷选共用同一套视觉。
            val interactionSource = remember { MutableInteractionSource() }
            val rowPressed by interactionSource.collectSmartisanPressedAsState()
            val pressed = pressedDay == index || rowPressed
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WeekdayRowHeight)
                    .background(if (pressed) colors.pressedHighlight else Color.Transparent)
                    .smartisanClickable(
                        interactionSource = interactionSource,
                        role = Role.Checkbox,
                    ) {
                        onSelectedDaysChange(selectedDays.toggle(day))
                    }
                    .semantics {
                        stateDescription = if (selected) SelectedDescription else UnselectedDescription
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanText(
                    text = labels.getOrElse(index) { "" },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = SmartisanDimens.RowContentStart),
                    style = typography.listItemPrimary,
                    color = if (pressed) colors.onPressedHighlight else colors.textPrimary,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .width(CheckboxColumnWidth)
                        .fillMaxHeight()
                        .padding(end = CheckboxEndMargin)
                        .pointerInput(Unit) {
                            // 复选框列内的快速选择：按下定状态，拖动刷过经过的每一行。
                            val rowHeightPx = WeekdayRowHeight.toPx()
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startDay = (down.position.y / rowHeightPx).toInt()
                                    .coerceIn(0, WeekdayCount - 1)
                                val target = DayOfWeek.of(startDay + 1) !in currentSelection.value
                                pressedDay = startDay
                                currentCallback.value.applyTo(
                                    currentSelection.value,
                                    startDay,
                                    target,
                                )
                                down.consume()
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUp()) break
                                    val columnY = startDay * rowHeightPx + change.position.y
                                    val crossedDay = (columnY / rowHeightPx).toInt()
                                        .coerceIn(0, WeekdayCount - 1)
                                    pressedDay = crossedDay
                                    currentCallback.value.applyTo(
                                        currentSelection.value,
                                        crossedDay,
                                        target,
                                    )
                                    change.consume()
                                }
                                pressedDay = NoDay
                            }
                        },
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    WeekdayCheckbox(selected = selected, pressed = pressed)
                }
            }
        }
    }
}

/**
 * 紧凑版重复日选择：七个方正小片，点击切换，适合放在设置行右侧或弹窗里。
 *
 * 与 [SmartisanWeekdayPicker] 共用同一套选中语义（`pressedHighlight` 蓝色底），
 * 但不提供拖动刷选——小片只有 34dp，纵向拖动会与父级滚动冲突。
 *
 * @param selectedDays 当前选中的星期集合。
 * @param onSelectedDaysChange 选中集合变化回调。
 * @param modifier 外部修饰符。
 */
@Composable
fun SmartisanWeekdayChips(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 0 until WeekdayCount) {
            val day = DayOfWeek.of(index + 1)
            val selected = day in selectedDays
            Box(
                modifier = Modifier
                    .size(ChipSize)
                    .background(if (selected) colors.pressedHighlight else Color.Transparent)
                    .border(
                        width = ChipStroke,
                        color = if (selected) colors.pressedHighlight else colors.textTertiary,
                    )
                    .smartisanClickable { onSelectedDaysChange(selectedDays.toggle(day)) }
                    .semantics {
                        stateDescription = if (selected) SelectedDescription else UnselectedDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                SmartisanText(
                    text = ChipLabels.getOrElse(index) { "" },
                    style = typography.listItemSecondary,
                    color = if (selected) colors.onPressedHighlight else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

/** 七行选择器的复选框：方正的蓝色方块 + 白色对勾，未选中为描边方块。 */
@Composable
private fun WeekdayCheckbox(selected: Boolean, pressed: Boolean) {
    val colors = LocalSmartisanColors.current
    Canvas(modifier = Modifier.size(CheckboxSize)) {
        val strokeWidth = CheckboxStroke.toPx()
        if (selected) {
            drawRect(color = colors.pressedHighlight)
            val check = Path().apply {
                moveTo(size.width * 0.22f, size.height * 0.52f)
                lineTo(size.width * 0.42f, size.height * 0.72f)
                lineTo(size.width * 0.78f, size.height * 0.28f)
            }
            drawPath(
                path = check,
                color = if (pressed) colors.onPressedHighlight else colors.onAccent,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        } else {
            drawRect(
                color = if (pressed) colors.onPressedHighlight else colors.textTertiary,
                style = Stroke(width = strokeWidth),
            )
        }
    }
}

/** 切换某一天并返回新的集合。 */
private fun Set<DayOfWeek>.toggle(day: DayOfWeek): Set<DayOfWeek> =
    if (day in this) this - day else this + day

/** 把某一天设为指定状态并返回新的集合。 */
private fun Set<DayOfWeek>.withDay(day: DayOfWeek, selected: Boolean): Set<DayOfWeek> =
    if (selected) this + day else this - day

/** 快速选择：只在集合真的变化时才回调，避免重复触发父级重组。 */
private fun ((Set<DayOfWeek>) -> Unit).applyTo(
    current: Set<DayOfWeek>,
    dayIndex: Int,
    target: Boolean,
) {
    val day = DayOfWeek.of(dayIndex + 1)
    val next = current.withDay(day, target)
    if (next != current) this(next)
}

/** 一周七天。 */
private const val WeekdayCount = 7

/** 没有正在按压的行。 */
private const val NoDay = -1

/** 行高，沿用列表行最小高度 48dp。 */
private val WeekdayRowHeight = SmartisanDimens.ListItemMinHeight

/** 复选框列宽度，原版为 36dp 复选框 + 6dp 右边距，这里给到 48dp 保证可点范围。 */
private val CheckboxColumnWidth = 48.dp

/** 复选框与行右边缘的间距。 */
private val CheckboxEndMargin = 12.dp

/** 复选框边长。 */
private val CheckboxSize = 22.dp

/** 复选框描边宽度。 */
private val CheckboxStroke = 1.4.dp

/** 紧凑小片边长。 */
private val ChipSize = 34.dp

/** 紧凑小片描边宽度。 */
private val ChipStroke = 1.dp

/** 紧凑小片的短文案。 */
private val ChipLabels = listOf("一", "二", "三", "四", "五", "六", "日")

/** 无障碍：已选择。 */
private const val SelectedDescription = "已选择"

/** 无障碍：未选择。 */
private const val UnselectedDescription = "未选择"


