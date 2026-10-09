/**
 * 控件：锤子时间选择器（含贴底弹窗，以及日历提醒的「一天」视图）。
 *
 * 合并 framework（`smartisanos.jar` 里的 `classes.dex`）的 6 个类：
 *
 * | framework 类 | 原版布局 | 本库对应 |
 * | --- | --- | --- |
 * | `smartisanos.widget.SmartisanTimePicker` | `time_picker.xml` | [SmartisanTimePicker]（[SmartisanPickerVariant.Standard]） |
 * | `smartisanos.widget.SmartisanTimePickerEx` | `time_picker_ex.xml` | [SmartisanTimePicker]（[SmartisanPickerVariant.Enhanced]） |
 * | `smartisanos.widget.SmartisanTimePickerDialog` | `time_picker_dialog.xml` | [SmartisanTimePickerDialog] |
 * | `smartisanos.widget.SmartisanTimePickerExDialog` | `time_picker_ex_dialog.xml` | [SmartisanTimePickerDialog] |
 * | `smartisanos.widget.calendar.SmartisanTimePicker1Day` | `remind_time_picker_1_day.xml` | [SmartisanTimePicker]（[SmartisanPickerVariant.Calendar]） |
 * | `smartisanos.widget.calendar.SmartisanNumberPicker1Day` | — | 复用库内 [SmartisanNumberPicker]（原版是又一份滚轮实现） |
 *
 * 用它的应用：设置、便签、时钟等锤子应用的时间弹窗用普通版 / Ex 版；
 * 锤子日历的提醒弹层 `calendar.SmartisanCalendarPopupDialog` 用「一天」视图
 * （`SmartisanTimePicker1Day`，三列是 时 / 分 / 上午下午）。
 *
 * 原版手感都在这里保留：
 * 1. **12 小时制**：时列显示 1..12（原版没设 formatter，所以不补零），24 小时制显示 00..23（补零）；
 * 2. **11 ↔ 12 联动上午下午**：原版在时列从 11 拨到 12（或反过来）时会顺带切换上午 / 下午，
 *    这里按当前值判断后一起回调；
 * 3. **上午 / 下午列**取 `DateFormatSymbols#getAmPmStrings()`，24 小时制下整列不显示；
 * 4. **「一天」视图**：3 行可见、循环滚动、分钟列最前面有一项空白（原版用来表示「还没选分钟」，
 *    选中它回调 `minute = -1`），底图 `remind_time_picker_widget_bg` 里已经画好选中行与上下两条线。
 *
 * 与原版的两处已知差异（受库内滚轮能力所限，见 [SmartisanNumberPicker]）：
 * 1. 字号固定为滚轮的 15sp / 18sp（原版普通版 16sp / 18sp、Ex 版 15sp / 20sp、「一天」视图 16sp / 18sp）；
 * 2. 文字色通过 [SmartisanPickerColors] 覆盖色板的 `textTertiary`（普通行）与 `accent`（选中行）。
 */
package cc.wuersan008.smartisanx.ui.control

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBottomSheet
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialogTitleBar
import java.text.DateFormatSymbols

/** 普通版三列滚轮的高度，原版 `time_picker.xml` 写死的 `208dp`。 */
private val TimePickerHeight = 208.dp

/** 列间分隔线宽度，原版 `time_picker.xml` / `time_picker_ex.xml` / `remind_time_picker_1_day.xml` 都是 `1dp`。 */
private val TimePickerDividerWidth = 1.dp

/** 滚轮单行高度，与库内 [SmartisanNumberPicker] 的默认行高一致。 */
private val TimePickerItemHeight = 40.dp

/** 默认可见行数。 */
private const val DefaultVisibleCount = 5

/** 日历「一天」视图的可见行数，原版 `integer/time_picker_wheel_item_count = 3`。 */
private const val CalendarVisibleCount = 3

/** 12 小时制的一半，原版 `SmartisanTimePicker.HOURS_IN_HALF_DAY = 12`。 */
private const val HoursInHalfDay = 12

/** Ex 版弹窗压在滚轮上方的那条阴影高度，原版 `time_picker_ex_dialog.xml` 写死的 `13dp`。 */
private val EnhancedPickerShadowHeight = 13.dp

/**
 * 时间选择器：时 / 分 / 上午下午三列滚轮，对应原版 `SmartisanTimePicker`（普通版）、
 * `SmartisanTimePickerEx`（Ex 版）与 `calendar.SmartisanTimePicker1Day`（日历「一天」视图）。
 *
 * ```kotlin
 * var hour = 9
 * var minute = 30
 * SmartisanTimePicker(
 *     hour = hour,
 *     minute = minute,
 *     onTimeChange = { h, m -> hour = h; minute = m },
 * )
 * ```
 *
 * @param hour 当前小时，0..23（[is24Hour] 为 false 时对外仍然是 0..23，界面上的 1..12 与上午 / 下午
 *   由它换算）。
 * @param minute 当前分钟：0..59；[SmartisanPickerVariant.Calendar] 下 `-1` 表示原版的「还没选分钟」
 *   （分钟列最前面那一项空白）。
 * @param onTimeChange 时间变化回调（滚动停止后触发一次），对应原版 `OnTimeChangedListener`。
 * @param modifier 外部修饰符；整行宽度取原版 `dimen/time_picker_width = 360dp` 与可用宽度的较小值，
 *   需要别的宽度时在 [modifier] 里显式给 `width`。
 * @param is24Hour 是否 24 小时制，对应原版 `setIs24HourView`；默认跟随系统设置
 *   （原版读 `Settings.System.TIME_12_24`，与 [DateFormat.is24HourFormat] 等价）。
 * @param variant 外观变体：默认 [SmartisanPickerVariant.Standard]，Ex 版传
 *   [SmartisanPickerVariant.Enhanced]，日历「一天」视图传 [SmartisanPickerVariant.Calendar]。
 * @param visibleCount 可见行数（「一天」视图固定 3 行，与它的底图一致）。
 * @param itemHeight 单行高度（「一天」视图按底图取 `time_picker_height / 3`）。
 */
@Composable
fun SmartisanTimePicker(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
    variant: SmartisanPickerVariant = SmartisanPickerVariant.Standard,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = TimePickerItemHeight,
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val amPmStrings = remember(locale) { DateFormatSymbols(locale).amPmStrings }
    val calendarVariant = variant == SmartisanPickerVariant.Calendar
    // 「一天」视图的底图（remind_time_picker_widget_bg）是按 3 行、time_picker_height 画的，
    // 所以行高取高度的 1/3，选中行才能和底图里的选中行对齐。
    val pickerHeight =
        when (variant) {
            SmartisanPickerVariant.Standard -> TimePickerHeight
            SmartisanPickerVariant.Enhanced -> dimensionResource(R.dimen.date_time_picker_height)
            SmartisanPickerVariant.Calendar -> dimensionResource(R.dimen.time_picker_height)
        }
    val rows = if (calendarVariant) CalendarVisibleCount else visibleCount
    val rowHeight = if (calendarVariant) pickerHeight / CalendarVisibleCount else itemHeight
    val (normalColor, selectedColor) = smartisanPickerTextColors(variant)
    val backgroundRes =
        when (variant) {
            SmartisanPickerVariant.Standard -> R.drawable.time_picker_widget_bg
            SmartisanPickerVariant.Enhanced -> R.drawable.time_picker_widget_bg_ex
            SmartisanPickerVariant.Calendar -> R.drawable.remind_time_picker_widget_bg
        }
    // 12 小时制下「时」列显示 1..12，0 点和 12 点都显示 12。
    val hourInHalfDay = if (hour % HoursInHalfDay == 0) HoursInHalfDay else hour % HoursInHalfDay
    val isAm = hour < HoursInHalfDay
    SmartisanPickerColors(normal = normalColor, selected = selectedColor) {
        SmartisanPickerRow(
            backgroundRes = backgroundRes,
            height = pickerHeight,
            modifier = modifier.widthIn(max = dimensionResource(R.dimen.time_picker_width)),
            itemHeight = rowHeight,
            // 「一天」视图的底图里已经画了选中行底纹与上下两条线。
            showSelectionLens = !calendarVariant,
        ) {
            // 时列。
            SmartisanPickerColumn(
                value = if (is24Hour) hour.coerceIn(0, HoursInHalfDay * 2 - 1) else hourInHalfDay,
                minValue = if (is24Hour) 0 else 1,
                maxValue = if (is24Hour) HoursInHalfDay * 2 - 1 else HoursInHalfDay,
                onValueChange = { value ->
                    if (is24Hour) {
                        onTimeChange(value, minute)
                    } else {
                        // 原版：11 拨到 12（或 12 拨到 11）时顺带切换上午 / 下午。
                        val flipped = (hourInHalfDay == 11 && value == 12) || (hourInHalfDay == 12 && value == 11)
                        val nextIsAm = if (flipped) !isAm else isAm
                        onTimeChange(smartisanHourOfDay(value, nextIsAm), minute)
                    }
                },
                modifier = Modifier.weight(1f),
                // 原版 24 小时制用 `getTwoDigitFormatter()` 补零，12 小时制不补零。
                formatter = { value -> if (is24Hour) "%02d".format(value) else value.toString() },
                wrap = calendarVariant,
                showSelectionLines = !calendarVariant,
                visibleCount = rows,
                itemHeight = rowHeight,
            )
            SmartisanPickerDivider(TimePickerDividerWidth)
            // 分列。「一天」视图最前面多一项空白（原版 `format` 里 value == 0 返回空串）。
            SmartisanPickerColumn(
                value = if (calendarVariant) minute + 1 else minute,
                minValue = if (calendarVariant && minute < 0) 0 else if (calendarVariant) 1 else 0,
                maxValue = if (calendarVariant) 60 else 59,
                onValueChange = { value ->
                    if (calendarVariant) {
                        // value 0 是那一项空白，回调 -1 表示原版的「还没选分钟」。
                        onTimeChange(hour, value - 1)
                    } else {
                        onTimeChange(hour, value)
                    }
                },
                modifier = Modifier.weight(1f),
                formatter = { value ->
                    if (calendarVariant) {
                        if (value == 0) "" else "%02d".format(value - 1)
                    } else {
                        "%02d".format(value)
                    }
                },
                wrap = calendarVariant,
                showSelectionLines = !calendarVariant,
                visibleCount = rows,
                itemHeight = rowHeight,
            )
            if (!is24Hour) {
                SmartisanPickerDivider(TimePickerDividerWidth)
                // 上午 / 下午列，24 小时制下整列不显示（原版 `updateAmPmControl`）。
                SmartisanPickerColumn(
                    value = if (isAm) 0 else 1,
                    minValue = 0,
                    maxValue = 1,
                    onValueChange = { value -> onTimeChange(smartisanHourOfDay(hourInHalfDay, value == 0), minute) },
                    modifier = Modifier.weight(1f),
                    formatter = { value -> amPmStrings.getOrElse(value.coerceIn(0, 1)) { "" } },
                    showSelectionLines = !calendarVariant,
                    visibleCount = rows,
                    itemHeight = rowHeight,
                )
            }
        }
    }
}

/**
 * 时间选择弹窗：贴底弹窗 + 标题栏（左取消 / 右完成）+ [SmartisanTimePicker]。
 *
 * 对应原版 `SmartisanTimePickerDialog` / `SmartisanTimePickerExDialog`
 * （`time_picker_dialog.xml` / `time_picker_ex_dialog.xml`）：原版是 `gravity = bottom` 的 Dialog，
 * 标题栏是 `MenuDialogTitleBar`（左取消、右完成），标题默认为空（由调用方 `setTitle` 设置）。
 * Ex 版还会在滚轮上方压一条 13dp 的 `time_picker_shadow` 阴影，这里同样保留。
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanTimePickerDialog(
 *         hour = hour, minute = minute,
 *         onTimeChange = { h, m -> hour = h; minute = m },
 *         onDismissRequest = { visible = false },
 *         onConfirm = { h, m -> pick(h, m) },
 *     )
 * }
 * ```
 *
 * @param hour 当前小时，0..23。
 * @param minute 当前分钟，0..59。
 * @param onTimeChange 时间变化回调。
 * @param onDismissRequest 关闭回调（点遮罩、返回键、取消图标）。
 * @param modifier 作用于弹窗面板。
 * @param onConfirm 点「完成」图标的回调，参数是当前选中的时 / 分。
 * @param is24Hour 是否 24 小时制，默认跟随系统设置。
 * @param variant 外观变体。
 * @param title 标题文字；为 null 时按原版留空（只显示左右两个图标）。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanTimePickerDialog(
    hour: Int,
    minute: Int,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
    variant: SmartisanPickerVariant = SmartisanPickerVariant.Standard,
    title: String? = null,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = TimePickerItemHeight,
) {
    SmartisanBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        SmartisanDialogTitleBar(
            title = title.orEmpty(),
            onDismiss = onDismissRequest,
            onConfirm = { onConfirm(hour, minute) },
        )
        // 原版弹窗是「三列滚轮居中、Ex 版上方压一条阴影」。
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SmartisanTimePicker(
                hour = hour,
                minute = minute,
                onTimeChange = onTimeChange,
                is24Hour = is24Hour,
                variant = variant,
                visibleCount = visibleCount,
                itemHeight = itemHeight,
            )
            if (variant == SmartisanPickerVariant.Enhanced) {
                // matchParentSize 不参与父 Box 的尺寸计算，所以阴影宽度正好是滚轮的宽度。
                Box(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier =
                            Modifier.align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(EnhancedPickerShadowHeight)
                                .smartisanDrawableBackground(R.drawable.time_picker_shadow),
                    )
                }
            }
        }
    }
}

/**
 * 12 小时制的「时 + 上午 / 下午」换算成 24 小时制，对应原版 `getCurrentHour()`：
 * 12 点时上午是 0 点、下午是 12 点。
 *
 * @param hourInHalfDay 12 小时制的小时，1..12。
 * @param isAm 是否上午。
 */
private fun smartisanHourOfDay(hourInHalfDay: Int, isAm: Boolean): Int =
    if (isAm) {
        if (hourInHalfDay == HoursInHalfDay) 0 else hourInHalfDay
    } else {
        if (hourInHalfDay == HoursInHalfDay) HoursInHalfDay else hourInHalfDay + HoursInHalfDay
    }
