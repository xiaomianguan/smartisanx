package cc.wuersan008.smartisanx.ui.clock

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchStyle
import java.time.DayOfWeek

/**
 * smartisanx 的重复日选择器（闹钟「重复」设置）。
 *
 * 复刻自锤子时钟的 `AlarmRepeatDaysView`：七行「周一…周日」+ 行尾复选框，
 * 另有原版中文区才追加的「法定节假日」开关行（默认不显示）。行与复选框都用原版位图：
 *
 * - 行背景 `alarm_repeat_list_item_bg`（146px @3x = 48.67dp，正好等于原版
 *   `item_alarm_repeat_day.xml` 的 `48.6667dp` 行高，底部自带 2px 分隔线）；
 * - 复选框 `alarm_repeat_checkbox_selector`（36dp × 36dp，可见方块 24dp，
 *   选中 / 按下 / 禁用态都写在 selector 里）；
 * - 「法定节假日」开关用 [SmartisanSwitchStyle.Repeat]（`alarm_repeat_switch_*`）。
 *
 * 手势沿用原版：在复选框列（原版 `CHECKBOX_SIZE_DP + CHECKBOX_END_MARGIN_DP`
 * = 36dp + 6dp）按下时先决定目标状态（已选则取消、未选则选中），再纵向拖动，
 * 把同一状态刷过经过的每一行，并消费事件避免父级列表跟着滚动
 * （对应原版的 `requestDisallowInterceptTouchEvent`）。
 * 原版用 `duplicateParentState` 把行的按压态交给复选框，所以拖动经过的行只会让
 * 复选框换成按压位图，行本身不加额外高亮。
 *
 * @param selectedDays 当前选中的星期集合。
 * @param onSelectedDaysChange 选中集合变化回调，始终返回新的集合。
 * @param modifier 外部修饰符。
 * @param labels 七行文案，默认周一到周日。
 * @param showHolidaySwitch 是否显示原版「法定节假日」开关行（原版只在中文区追加）。
 * @param followHolidays 「法定节假日」开关的当前状态。
 * @param onFollowHolidaysChange 「法定节假日」开关的变化回调。
 * @param holidayLabel 「法定节假日」行的文案。
 * @param checkboxRes 复选框 selector，默认原版 `alarm_repeat_checkbox_selector`。
 * @param rowBackgroundRes 行背景位图，默认原版 `alarm_repeat_list_item_bg`。
 */
@Composable
fun SmartisanWeekdayPicker(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
    labels: List<String> = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
    showHolidaySwitch: Boolean = false,
    followHolidays: Boolean = false,
    onFollowHolidaysChange: (Boolean) -> Unit = {},
    holidayLabel: String = HolidayRowLabel,
    @DrawableRes checkboxRes: Int = WeekdayCheckboxSelector,
    @DrawableRes rowBackgroundRes: Int = WeekdayRowBackground,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    // 手势协程不能因为父级状态变化而重启（否则拖动会被打断），
    // 所以用 rememberUpdatedState 读取最新值，pointerInput 的 key 保持稳定。
    val currentSelection = rememberUpdatedState(selectedDays)
    val currentCallback = rememberUpdatedState(onSelectedDaysChange)
    var pressedDay by remember { mutableIntStateOf(NoDay) }
    // 原版最后一行与「法定节假日」行用纯色底抹掉分隔线：浅色下取原版
    // `setBackgroundColor(250, 250, 250)` 的色值，深色下跟随主题，避免整块亮白。
    val flatRowBackground = if (colors.isLight) WeekdayFlatRowBackground else colors.surface

    Column(modifier = modifier.fillMaxWidth()) {
        for (index in 0 until WeekdayCount) {
            val day = DayOfWeek.of(index + 1)
            val selected = day in selectedDays
            val interactionSource = remember { MutableInteractionSource() }
            val rowPressed by interactionSource.collectSmartisanPressedAsState()
            // 原版用 `duplicateParentState` 把行的按压态交给复选框，行本身不加高亮。
            val pressed = pressedDay == index || rowPressed
            // 原版最后一行用纯色底，把行背景位图底部的 2px 分隔线抹掉。
            val rowBackground =
                if (index == WeekdayCount - 1) {
                    Modifier.background(flatRowBackground)
                } else {
                    Modifier.smartisanDrawableBackground(rowBackgroundRes)
                }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WeekdayRowHeight)
                    .then(rowBackground)
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
                    color = colors.textPrimary,
                    maxLines = 1,
                )
                Box(
                    // 手势列就是原版的 `CHECKBOX_SIZE_DP + CHECKBOX_END_MARGIN_DP` = 42dp，
                    // 所以 pointerInput 挂在 padding 之前，可点范围才覆盖整列。
                    modifier = Modifier
                        .width(CheckboxColumnWidth)
                        .fillMaxHeight()
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
                        }
                        .padding(end = CheckboxEndMargin),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    WeekdayCheckbox(
                        selected = selected,
                        pressed = pressed,
                        checkboxRes = checkboxRes,
                    )
                }
            }
        }
        if (showHolidaySwitch) {
            WeekdayHolidayRow(
                label = holidayLabel,
                checked = followHolidays,
                enabled = selectedDays.isNotEmpty(),
                rowBackground = flatRowBackground,
                onCheckedChange = onFollowHolidaysChange,
            )
        }
    }
}

/**
 * 紧凑版重复日选择：七个方正小片，点击切换，适合放在设置行右侧或弹窗里。
 *
 * 与 [SmartisanWeekdayPicker] 共用同一套选中语义（点一下切换，已选为蓝底白字），
 * 但不提供拖动刷选——小片只有 34dp，纵向拖动会与父级滚动冲突。
 * 原版没有这种紧凑变体（只有 36dp 的复选框位图），所以这里仍用主题色手绘。
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

/**
 * 重复日的复选框：直接用原版 `alarm_repeat_checkbox_selector` 位图。
 *
 * 原版 `item_alarm_repeat_day.xml` 里是一个 36dp × 36dp 的 `CheckBox`，
 * `button` 指向这个 selector（选中 / 按下 / 禁用态都在里面）；位图里的可见方块
 * 只有 24dp，四周留白也由位图自己提供，所以这里按固有尺寸绘制，不做任何缩放。
 */
@Composable
private fun WeekdayCheckbox(
    selected: Boolean,
    pressed: Boolean,
    @DrawableRes checkboxRes: Int,
) {
    SmartisanIcon(
        res = checkboxRes,
        contentDescription = null,
        pressed = pressed,
        checked = selected,
        size = CheckboxSize,
        contentScale = ContentScale.None,
    )
}

/**
 * 原版的「法定节假日」开关行（`item_alarm_repeat_holiday.xml`，原版只在中文区追加）。
 *
 * 纯色底、文案左边距 18dp；右侧是原版 `alarm_repeat_switch_*` 位图开关，
 * 与行右边缘留 8dp。原版在一天都没选时把这一行整体置灰。
 */
@Composable
private fun WeekdayHolidayRow(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    rowBackground: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HolidayRowHeight)
            .background(rowBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmartisanText(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = SmartisanDimens.RowContentStart),
            style = typography.listItemPrimary,
            color = if (enabled) colors.textPrimary else colors.textDisabled,
            maxLines = 1,
        )
        SmartisanSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(end = HolidaySwitchEndMargin),
            enabled = enabled,
            style = SmartisanSwitchStyle.Repeat,
        )
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

/** 行高：原版 `item_alarm_repeat_day.xml` 的 48.6667dp，正好是行背景位图 146px ÷ 3。 */
private val WeekdayRowHeight = 48.6667.dp

/** 复选框边长：原版 `CheckBox` 的 36dp（位图 108px @3x，可见方块 24dp）。 */
private val CheckboxSize = 36.dp

/** 复选框与行右边缘的间距，原版 `layout_marginEnd="6dp"`。 */
private val CheckboxEndMargin = 6.dp

/** 复选框列宽（快速选择手势的可点范围）：原版 `CHECKBOX_SIZE_DP + CHECKBOX_END_MARGIN_DP`。 */
private val CheckboxColumnWidth = CheckboxSize + CheckboxEndMargin

/** 原版重复日复选框的 selector，即 `item_alarm_repeat_day.xml` 里 `CheckBox` 的 `button`。 */
@DrawableRes
private val WeekdayCheckboxSelector = R.drawable.alarm_repeat_checkbox_selector

/** 原版重复日的行背景位图：2px × 146px @3x，`#FAFAFA` 底 + 底部 2px `#EDEDED` 分隔线。 */
@DrawableRes
private val WeekdayRowBackground = R.drawable.alarm_repeat_list_item_bg

/** 原版最后一行与「法定节假日」行的纯色底，对应原版 `Color.rgb(250, 250, 250)`。 */
private val WeekdayFlatRowBackground = Color(0xFFFAFAFA)

/**
 * 「法定节假日」行的高度：原版 `item_alarm_repeat_holiday.xml` 是 48dp，
 * 这里取开关画布高度（66dp × 52dp，上下各留 2dp 给投影），免得开关位图被压扁。
 */
private val HolidayRowHeight = 52.dp

/** 「法定节假日」开关与行右边缘的间距，原版 `layout_marginEnd="8dp"`。 */
private val HolidaySwitchEndMargin = 8.dp

/** 「法定节假日」行的默认文案。 */
private const val HolidayRowLabel = "法定节假日"

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


