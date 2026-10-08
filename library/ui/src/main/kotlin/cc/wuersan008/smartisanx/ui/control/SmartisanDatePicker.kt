/**
 * 控件：锤子日期选择器（含贴底弹窗，以及「日期 + 时间」合体选择器）。
 *
 * 合并 framework（`smartisanos.jar` 里的 `classes.dex`）的 6 个类：
 *
 * | framework 类 | 原版布局 | 本库对应 |
 * | --- | --- | --- |
 * | `smartisanos.widget.SmartisanDatePicker` | `date_picker.xml` | [SmartisanDatePicker]（[SmartisanPickerVariant.Standard]） |
 * | `smartisanos.widget.SmartisanDatePickerEx` | `date_picker_ex.xml` | [SmartisanDatePicker]（[SmartisanPickerVariant.Enhanced]，默认） |
 * | `smartisanos.widget.SmartisanDatePickerDialog` | `date_picker_dialog.xml` | [SmartisanDatePickerDialog] |
 * | `smartisanos.widget.SmartisanDatePickerExDialog` | `date_picker_ex_dialog.xml` | [SmartisanDatePickerDialog] |
 * | `smartisanos.widget.SmartisanDateTimePicker` | `date_time_picker_ex.xml` | [SmartisanDateTimePicker] |
 * | `smartisanos.widget.SmartisanDateTimePickerDialog` | `date_time_picker_dialog.xml` | [SmartisanDateTimePickerDialog] |
 *
 * 用它的应用：锤子日历（`calendar.CalendarView` 的「跳转到日期」就是 `new SmartisanDatePickerExDialog`）、
 * 设置、便签、时钟等锤子应用。
 *
 * 原版的三个手感细节都在这里保留：
 * 1. **列顺序跟随系统日期格式**：原版 `reorderSpinners()` 用 ICU 的 `getDateFormatOrder("yyyyMMMdd")`，
 *    这里用等价的 [android.text.format.DateFormat.getDateFormatOrder]（年 / 月 / 日三列，等宽）；
 * 2. **月份名**取 `DateFormatSymbols#getShortMonths()`，首字符是数字（中文、日语等）时退回 1..12，
 *    与原版 `usingNumericMonths()` 一致；Ex 版中文下再给三列补「年 / 月 / 日」单位后缀；
 * 3. **改月 / 改年时「日」按新月份收敛**：原版是「旧值放得下就保留，放不下就回到 1」
 *    （`setDate(..., daySpinnerValue <= temp.getActualMaximum(DAY) ? daySpinnerValue : 1)`）。
 *
 * 底图与选中行照抄原版：整行铺 `time_picker_widget_bg`（Ex 版 `time_picker_widget_bg_ex_new`），
 * 选中行底纹用 `time_picker_widget_lens`（原版 `SmartisanNumberPickerEx#mDrawableBg`），
 * 上下两条参考线由库内 [SmartisanNumberPicker] 绘制，列间竖线是 1dp（Ex 版 2px）的
 * `date_time_picker_divider_bg`。
 *
 * 与原版的两处已知差异（受库内滚轮能力所限，见 [SmartisanNumberPicker]）：
 * 1. 字号固定为滚轮的 15sp / 18sp（原版普通版 16sp / 18sp、Ex 版 15sp / 20sp）；
 * 2. 文字色通过 [SmartisanPickerColors] 覆盖色板的 `textTertiary`（普通行）与 `accent`（选中行），
 *    取值与原版一致（见 [SmartisanPickerVariant]）。
 */
package cc.wuersan008.smartisanx.ui.control

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanColors
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBottomSheet
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialogTitleBar
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** 普通版三列滚轮的高度，原版 `date_picker.xml` 写死的 `208dp`。 */
private val PickerHeight = 208.dp

/** 列间分隔线宽度，原版普通版 `date_picker.xml` 写死的 `1dp`。 */
private val PickerDividerWidth = 1.dp

/** 滚轮单行高度，与库内 [SmartisanNumberPicker] 的默认行高一致。 */
private val PickerItemHeight = 40.dp

/** 默认可见行数，原版 `SELECTOR_WHEEL_ITEM_COUNT = 9`，但布局高度只够 5 行。 */
private const val DefaultVisibleCount = 5

/** 事件类日期的起始年份，原版 `SmartisanDatePicker.DEFAULT_EVENT_START_YEAR = 1970`。 */
private const val DefaultEventStartYear = 1970

/** 事件类日期的结束年份，原版 `SmartisanDatePicker.DEFAULT_EVENT_END_YEAR = 2037`。 */
private const val DefaultEventEndYear = 2037

/** 一年 12 个月，对应原版 `mNumberOfMonths`。 */
private const val MonthsPerYear = 12

/** 一天的毫秒数，原版用 `TrackerConstant.ONE_DAY`。 */
private const val MillisPerDay = 86_400_000L

/** 一小时的毫秒数，原版 `SmartisanDateTimePicker#getTimMillsForDayValue` 里写死的 3600000。 */
private const val MillisPerHour = 3_600_000L

/** 一分钟的毫秒数，原版 `getTimMillsForDayValue` 里写死的 60000。 */
private const val MillisPerMinute = 60_000L

/** 「年份未设置」时那一列的文字，原版 `SmartisanNumberPickerEx.UNSET_STRING = "--"`。 */
private const val UnsetYearLabel = "--"

/** 日期列顺序的兜底值（系统格式解析失败时用「年 / 月 / 日」）。 */
private val DefaultDateFieldOrder = listOf('y', 'M', 'd')

/** 日期字段标识，对应原版 `reorderSpinners()` 里 ICU 顺序字符。 */
private const val DateFieldYear = 'y'
private const val DateFieldMonth = 'M'
private const val DateFieldDay = 'd'

/** 中文下的单位后缀，原版 `R.string.date_picker_year` / `_month` / `_day`。 */
private const val SuffixYear = "年"
private const val SuffixMonth = "月"
private const val SuffixDay = "日"

/** 中文下「时 / 分」的单位后缀，原版 `R.string.date_time_picker_hour` / `_minute`；时间选择器也用它。 */
internal const val SuffixHour = "时"
internal const val SuffixMinute = "分"

/** 日期时间的格式化，原版 `SmartisanDateTimePicker.DATE_FORMAT = "yyyy/MM/dd"`。 */
private const val DateTimeFormatPattern = "yyyy/MM/dd"

/**
 * 「年份未设置」的哨兵值。
 *
 * 对应原版 `SmartisanNumberPickerEx.UNSET_YEAR`（原版用 4，这里用 0 更直观）：
 * [SmartisanDatePicker] 在 `allowUnsetYear = true` 时把该值显示成 `--`，
 * 并原样从 [SmartisanDatePicker.onDateChange] 传出，用于生日这类「可以不知道年份」的日期。
 */
const val SmartisanUnsetYear: Int = 0

/**
 * 选择器外观变体。
 *
 * 原版同一个组件都有「普通版 / Ex 版」两套，日历提醒里还有第三套「一天」视图，
 * 差异只在底图、文字色与单位后缀，这里用同一个枚举表达：
 *
 * | 取值 | 原版类 | 底图 | 选中行文字色 |
 * | --- | --- | --- | --- |
 * | [Standard] | `SmartisanDatePicker` / `SmartisanTimePicker` | `time_picker_widget_bg` | `date_pick_select_day_color` #ff55585c |
 * | [Enhanced] | `SmartisanDatePickerEx` / `SmartisanTimePickerEx` / `SmartisanDateTimePicker` | `time_picker_widget_bg_ex_new` | `date_pick_ex_select_day_color` #9a000000 |
 * | [Calendar] | `calendar.SmartisanTimePicker1Day` | `remind_time_picker_widget_bg` | `calander_date_pick_select_day_color` #5079d9 |
 */
enum class SmartisanPickerVariant {
    /** 普通版：灰阶文字（`date_pick_normal_day_color` → `date_pick_select_day_color`），无单位后缀。 */
    Standard,

    /** Ex 版：黑色透明度文字（`date_pick_ex_*`），中文下带「年 / 月 / 日」等单位后缀。 */
    Enhanced,

    /**
     * 日历提醒的「一天」视图：3 行可见、循环滚动、蓝色选中色，底图里已经画好选中行。
     *
     * 只对 [SmartisanTimePicker] 有意义；[SmartisanDatePicker] 收到它时按 [Enhanced] 处理。
     */
    Calendar,
}

/**
 * 选择器用到的原版「普通行 / 选中行」文字色。
 *
 * 库内滚轮（[SmartisanNumberPicker]）的普通行取色板的 `textTertiary`、选中行取 `accent`，
 * 而原版这两个色是组件专有的灰阶 / 蓝色，所以这里按变体覆盖色板再交给滚轮，
 * 不改滚轮本身的实现。
 *
 * @param variant 外观变体。
 * @return 第一个是普通行文字色，第二个是选中行文字色（也是单位后缀的颜色，原版取高亮色）。
 */
@Composable
internal fun smartisanPickerTextColors(variant: SmartisanPickerVariant): Pair<Color, Color> {
    val normal =
        colorResource(
            if (variant == SmartisanPickerVariant.Enhanced) {
                R.color.date_pick_ex_normal_day_color
            } else {
                R.color.date_pick_normal_day_color
            }
        )
    val selected =
        colorResource(
            when (variant) {
                SmartisanPickerVariant.Standard -> R.color.date_pick_select_day_color
                SmartisanPickerVariant.Enhanced -> R.color.date_pick_ex_select_day_color
                SmartisanPickerVariant.Calendar -> R.color.calander_date_pick_select_day_color
            }
        )
    return normal to selected
}

/**
 * 把原版的「普通行 / 选中行」文字色写进色板，供 [SmartisanNumberPicker] 使用。
 *
 * 只替换 `textTertiary`（普通行）与 `accent`（选中行、单位后缀）两支颜色，其余原样透传，
 * 因此子树里除滚轮以外的取色行为不受影响。
 *
 * @param normal 普通行文字色。
 * @param selected 选中行文字色。
 * @param content 选择器内容。
 */
@Composable
internal fun SmartisanPickerColors(
    normal: Color,
    selected: Color,
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val overridden =
        remember(colors, normal, selected) {
            SmartisanColors(
                pageBackground = colors.pageBackground,
                surface = colors.surface,
                surfaceRaised = colors.surfaceRaised,
                surfacePressed = colors.surfacePressed,
                surfaceDisabled = colors.surfaceDisabled,
                titleBarBackground = colors.titleBarBackground,
                divider = colors.divider,
                rowDivider = colors.rowDivider,
                textPrimary = colors.textPrimary,
                textSecondary = colors.textSecondary,
                // 普通行文字色。
                textTertiary = normal,
                textDisabled = colors.textDisabled,
                textHint = colors.textHint,
                // 选中行文字色与单位后缀色。
                accent = selected,
                accentPressed = colors.accentPressed,
                accentDisabled = colors.accentDisabled,
                onAccent = colors.onAccent,
                link = colors.link,
                linkPressed = colors.linkPressed,
                success = colors.success,
                warning = colors.warning,
                selectionBackground = colors.selectionBackground,
                pressedHighlight = colors.pressedHighlight,
                onPressedHighlight = colors.onPressedHighlight,
                switchTrack = colors.switchTrack,
                switchTrackStroke = colors.switchTrackStroke,
                switchKnob = colors.switchKnob,
                switchIndicator = colors.switchIndicator,
                scrollbarThumb = colors.scrollbarThumb,
                scrim = colors.scrim,
                isLight = colors.isLight,
            )
        }
    CompositionLocalProvider(LocalSmartisanColors provides overridden) { content() }
}

/**
 * 一列滚轮：直接复用库内 [SmartisanNumberPicker]（内部是
 * [cc.wuersan008.smartisanx.ui.clock.SmartisanWheelPicker]），不重复实现滚轮。
 *
 * @param value 当前值，会被收敛到 `minValue..maxValue`。
 * @param minValue 最小值，对应原版 `setMinValue`。
 * @param maxValue 最大值，对应原版 `setMaxValue`。
 * @param onValueChange 值变化回调（滚动停止后触发一次），对应原版 `OnValueChangeListener`。
 * @param modifier 作用于这一列，通常传 `Modifier.weight(1f)`。
 * @param formatter 取值到文字的格式化，对应原版 `setFormatter`。
 * @param unit 选中行右侧的单位后缀，对应原版 `setHightlightSuffix`。
 * @param wrap 是否循环滚动，对应原版 `setWrapSelectorWheel`（原版日期 / 时间列默认不循环）。
 * @param showSelectionLines 是否绘制选中行上下两条参考线，对应原版 `mPaintIndicator` 的两条线。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
internal fun SmartisanPickerColumn(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    formatter: (Int) -> String = { number -> number.toString() },
    unit: String? = null,
    wrap: Boolean = false,
    showSelectionLines: Boolean = true,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = PickerItemHeight,
) {
    SmartisanNumberPicker(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        minValue = minValue,
        maxValue = maxValue,
        wrap = wrap,
        formatter = formatter,
        unit = unit,
        visibleCount = visibleCount,
        itemHeight = itemHeight,
        showSelectionLines = showSelectionLines,
    )
}

/**
 * 两列之间的竖分隔线：原版是一个铺满整行高度的 1dp（Ex 版 `smartisan_datepicker_divider_width = 2px`）View，
 * 颜色 `date_time_picker_divider_bg`。
 *
 * @param width 线宽。
 */
@Composable
internal fun SmartisanPickerDivider(width: Dp) {
    Box(
        modifier =
            Modifier.width(width)
                .fillMaxHeight()
                .background(colorResource(R.color.date_time_picker_divider_bg)),
    )
}

/**
 * 选择器整行的外壳：铺原版底图、居中铺选中行底纹，再横向排各列。
 *
 * @param backgroundRes 原版底图（NinePatch）。
 * @param height 整行高度。
 * @param modifier 外部修饰符。
 * @param itemHeight 单行高度，决定选中行底纹的高度（原版底纹高 = 选中行高）。
 * @param showSelectionLens 是否铺选中行底纹 `time_picker_widget_lens`
 *   （原版 `SmartisanNumberPickerEx#mDrawableBg`）；日历「一天」视图的底图里已经画了选中行，传 false。
 * @param columns 各列内容，可对每列使用 `Modifier.weight(1f)`。
 */
@Composable
internal fun SmartisanPickerRow(
    @DrawableRes backgroundRes: Int,
    height: Dp,
    modifier: Modifier = Modifier,
    itemHeight: Dp = PickerItemHeight,
    showSelectionLens: Boolean = true,
    columns: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .smartisanDrawableBackground(backgroundRes),
        contentAlignment = Alignment.Center,
    ) {
        if (showSelectionLens) {
            // 选中行底纹：原版把 time_picker_widget_lens 铺在选中行上，再画上下两条线。
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(itemHeight)
                        .smartisanDrawableBackground(R.drawable.time_picker_widget_lens),
            )
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            content = columns,
        )
    }
}

/**
 * 日期选择器：年 / 月 / 日三列滚轮，对应原版 `SmartisanDatePicker`（普通版）与
 * `SmartisanDatePickerEx`（Ex 版）。
 *
 * ```kotlin
 * var year = 2026
 * var month = 10
 * var day = 8
 * SmartisanDatePicker(
 *     year = year,
 *     month = month,
 *     day = day,
 *     onDateChange = { y, m, d -> year = y; month = m; day = d },
 * )
 * ```
 *
 * @param year 当前年份；`allowUnsetYear` 为 true 时可用 [SmartisanUnsetYear] 表示「未设置」。
 * @param month 当前月份，1..12（对外用人类习惯的 1 起始，原版内部是 0 起始）。
 * @param day 当前日期，1..31（超过当月天数时按 1 显示）。
 * @param onDateChange 日期变化回调（滚动停止后触发一次），对应原版 `OnDateChangedListener`。
 * @param modifier 外部修饰符，整行默认铺满可用宽度。
 * @param minYear 最小年份：原版事件类是 1970（`DEFAULT_EVENT_START_YEAR`）、生日类是 1800。
 * @param maxYear 最大年份，原版事件类是 2037（`DEFAULT_EVENT_END_YEAR`）。
 * @param variant 外观变体，默认 [SmartisanPickerVariant.Enhanced]（原版当前在用的 Ex 版）；
 *   传 [SmartisanPickerVariant.Calendar] 时按 Ex 版处理。
 * @param allowUnsetYear 是否允许「年份未设置」（原版 `DatePickerType.BIRTHDAY`）：
 *   开启后年份列最前面多一项 `--`（排在最小年份之前，与原版把 `UNSET_YEAR` 放在区间外一致），
 *   选中它时回调里的年份是 [SmartisanUnsetYear]。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanDatePicker(
    year: Int,
    month: Int,
    day: Int,
    onDateChange: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier,
    minYear: Int = DefaultEventStartYear,
    maxYear: Int = DefaultEventEndYear,
    variant: SmartisanPickerVariant = SmartisanPickerVariant.Enhanced,
    allowUnsetYear: Boolean = false,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = PickerItemHeight,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val fieldOrder = remember(configuration) { smartisanDateFieldOrder(context) }
    val monthLabels = remember(locale) { smartisanMonthLabels(locale) }
    val useSuffix = smartisanUseChineseSuffix(locale)
    val dayCount = remember(year, month) { smartisanDaysInMonth(year, month) }
    val lowYear = minOf(minYear, maxYear)
    val highYear = maxOf(minYear, maxYear)
    // 年份列里「未设置」那一项的滚轮取值：原版把 UNSET_YEAR 放在正常区间之外，这里放在最小年份之前。
    val unsetColumnValue = lowYear - 1
    val resolvedMonth = month.coerceIn(1, MonthsPerYear)
    val resolvedDay = day.coerceIn(1, dayCount)
    val resolvedYear =
        if (allowUnsetYear && year == SmartisanUnsetYear) {
            SmartisanUnsetYear
        } else {
            year.coerceIn(lowYear, highYear)
        }
    val useEnhancedLook = variant != SmartisanPickerVariant.Standard
    val (normalColor, selectedColor) =
        smartisanPickerTextColors(
            if (useEnhancedLook) SmartisanPickerVariant.Enhanced else SmartisanPickerVariant.Standard
        )
    SmartisanPickerColors(normal = normalColor, selected = selectedColor) {
        SmartisanPickerRow(
            backgroundRes =
                if (useEnhancedLook) {
                    R.drawable.time_picker_widget_bg_ex_new
                } else {
                    R.drawable.time_picker_widget_bg
                },
            height =
                if (useEnhancedLook) {
                    dimensionResource(R.dimen.date_time_picker_height)
                } else {
                    PickerHeight
                },
            modifier = modifier,
            itemHeight = itemHeight,
        ) {
            fieldOrder.forEachIndexed { index, field ->
                if (index > 0) {
                    // Ex 版的列间线是 2px，普通版是 1dp。
                    SmartisanPickerDivider(
                        width =
                            if (useEnhancedLook) {
                                dimensionResource(R.dimen.smartisan_datepicker_divider_width)
                            } else {
                                PickerDividerWidth
                            }
                    )
                }
                when (field) {
                    DateFieldYear ->
                        SmartisanPickerColumn(
                            value = if (resolvedYear == SmartisanUnsetYear) unsetColumnValue else resolvedYear,
                            minValue = if (allowUnsetYear) unsetColumnValue else lowYear,
                            maxValue = highYear,
                            onValueChange = { value ->
                                if (value == unsetColumnValue) {
                                    onDateChange(SmartisanUnsetYear, resolvedMonth, resolvedDay)
                                } else {
                                    val newDayCount = smartisanDaysInMonth(value, resolvedMonth)
                                    onDateChange(
                                        value,
                                        resolvedMonth,
                                        if (resolvedDay <= newDayCount) resolvedDay else 1,
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            formatter = { value ->
                                if (allowUnsetYear && value == unsetColumnValue) {
                                    UnsetYearLabel
                                } else {
                                    value.toString()
                                }
                            },
                            unit = if (useSuffix) SuffixYear else null,
                            visibleCount = visibleCount,
                            itemHeight = itemHeight,
                        )

                    DateFieldMonth ->
                        SmartisanPickerColumn(
                            value = resolvedMonth - 1,
                            minValue = 0,
                            maxValue = MonthsPerYear - 1,
                            onValueChange = { value ->
                                val newMonth = value + 1
                                val newDayCount = smartisanDaysInMonth(resolvedYear, newMonth)
                                onDateChange(
                                    resolvedYear,
                                    newMonth,
                                    if (resolvedDay <= newDayCount) resolvedDay else 1,
                                )
                            },
                            modifier = Modifier.weight(1f),
                            formatter = { value -> monthLabels[value.coerceIn(0, MonthsPerYear - 1)] },
                            unit = if (useSuffix) SuffixMonth else null,
                            visibleCount = visibleCount,
                            itemHeight = itemHeight,
                        )

                    else ->
                        SmartisanPickerColumn(
                            value = resolvedDay,
                            minValue = 1,
                            maxValue = dayCount,
                            onValueChange = { value -> onDateChange(resolvedYear, resolvedMonth, value) },
                            modifier = Modifier.weight(1f),
                            // 原版「日」列用 `getTwoDigitFormatter()`，即 %02d 补零。
                            formatter = { value -> "%02d".format(value) },
                            unit = if (useSuffix) SuffixDay else null,
                            visibleCount = visibleCount,
                            itemHeight = itemHeight,
                        )
                }
            }
        }
    }
}

/**
 * 日期选择弹窗：贴底弹窗 + 标题栏（左取消 / 右完成）+ 三列滚轮。
 *
 * 对应原版 `SmartisanDatePickerDialog` / `SmartisanDatePickerExDialog`
 * （`date_picker_dialog.xml` / `date_picker_ex_dialog.xml`）：原版是 `gravity = bottom` 的 Dialog，
 * 标题栏是 `MenuDialogTitleBar`（左取消图标、右完成图标），标题文字就是当前选中的日期，
 * 并随滚轮实时更新（原版 `updateTitle` → `DateUtils.formatDateTime`）。
 *
 * 原版布局末尾那个 `time_picker_widget_bottom` 的 TextView 只是给窗口多留 17dp 高度、本身不可见，
 * 这里略去。
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanDatePickerDialog(
 *         year = year, month = month, day = day,
 *         onDateChange = { y, m, d -> year = y; month = m; day = d },
 *         onDismissRequest = { visible = false },
 *         onConfirm = { y, m, d -> pick(y, m, d) },
 *     )
 * }
 * ```
 *
 * @param year 当前年份。
 * @param month 当前月份，1..12。
 * @param day 当前日期。
 * @param onDateChange 日期变化回调，滚轮每次变化都会触发（调用方更新状态后标题会实时跟着变）。
 * @param onDismissRequest 关闭回调（点遮罩、返回键、取消图标）。
 * @param modifier 作用于弹窗面板。
 * @param onConfirm 点「完成」图标的回调，参数是当前选中的日期。
 * @param minYear 最小年份。
 * @param maxYear 最大年份。
 * @param variant 外观变体。
 * @param allowUnsetYear 是否允许「年份未设置」。
 * @param title 标题文字；为 null 时按原版用 `DateUtils.formatDateTime` 的日期文案
 *   （年份未设置时自动去掉年份）。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanDatePickerDialog(
    year: Int,
    month: Int,
    day: Int,
    onDateChange: (year: Int, month: Int, day: Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit = { _, _, _ -> },
    minYear: Int = DefaultEventStartYear,
    maxYear: Int = DefaultEventEndYear,
    variant: SmartisanPickerVariant = SmartisanPickerVariant.Enhanced,
    allowUnsetYear: Boolean = false,
    title: String? = null,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = PickerItemHeight,
) {
    val context = LocalContext.current
    SmartisanBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        SmartisanDialogTitleBar(
            title = title ?: smartisanDateTitle(context, year, month, day),
            onDismiss = onDismissRequest,
            onConfirm = { onConfirm(year, month, day) },
        )
        SmartisanDatePicker(
            year = year,
            month = month,
            day = day,
            onDateChange = onDateChange,
            minYear = minYear,
            maxYear = maxYear,
            variant = variant,
            allowUnsetYear = allowUnsetYear,
            visibleCount = visibleCount,
            itemHeight = itemHeight,
        )
    }
}

/**
 * 日期 + 时间选择器：一列「日期」+ 一列「时」+ 一列「分」。
 *
 * 对应原版 `SmartisanDateTimePicker`（`date_time_picker_ex.xml`）：日期列不是年 / 月 / 日三列，
 * 而是把 `minTimeMillis..maxTimeMillis` 之间的每一天格式化成 `yyyy/MM/dd` 排成一列
 * （原版 `setFormatter` 用 `SimpleDateFormat("yyyy/MM/dd")`），列的取值是「距起始日期第几天」
 * （原版 `getDayValueForTimeMills`）。小时列显示 0..23（原版没设 formatter，所以不补零），
 * 分钟列补零成两位。中文下给时 / 分两列补「时 / 分」单位后缀。
 *
 * ```kotlin
 * var time = System.currentTimeMillis()
 * SmartisanDateTimePicker(
 *     timeMillis = time,
 *     onDateTimeChange = { time = it },
 * )
 * ```
 *
 * @param timeMillis 当前选中的时间戳。
 * @param onDateTimeChange 时间变化回调（滚动停止后触发一次），参数是新的时间戳。
 * @param modifier 外部修饰符。
 * @param minTimeMillis 可选范围下界（原版 `CalendarUtils.getMinTimeMills()` = 1970-01-01）。
 * @param maxTimeMillis 可选范围上界（原版 `CalendarUtils.getMaxTimeMills()` = 2037-12-31）。
 * @param variant 外观变体，默认 [SmartisanPickerVariant.Enhanced]（原版这个类就是 Ex 版外观）。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanDateTimePicker(
    timeMillis: Long,
    onDateTimeChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    minTimeMillis: Long = DateTimeMinMillis,
    maxTimeMillis: Long = DateTimeMaxMillis,
    variant: SmartisanPickerVariant = SmartisanPickerVariant.Enhanced,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = PickerItemHeight,
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val useSuffix = smartisanUseChineseSuffix(locale)
    val baseMillis = remember(minTimeMillis) { smartisanStartOfDay(minTimeMillis) }
    // 原版 `getDayValueForTimeMills` 就是整数除法取「第几天」，所以这里同样向下取整。
    val maxDayIndex =
        remember(maxTimeMillis, baseMillis) {
            ((maxTimeMillis - baseMillis) / MillisPerDay).toInt().coerceAtLeast(0)
        }
    val currentDayIndex =
        remember(timeMillis, baseMillis, maxDayIndex) {
            ((timeMillis - baseMillis) / MillisPerDay).toInt().coerceIn(0, maxDayIndex)
        }
    val currentCalendar = remember(timeMillis) { Calendar.getInstance().apply { timeInMillis = timeMillis } }
    val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val minute = currentCalendar.get(Calendar.MINUTE)
    val dayFormatter = remember(locale) { SimpleDateFormat(DateTimeFormatPattern, locale) }
    val (normalColor, selectedColor) = smartisanPickerTextColors(SmartisanPickerVariant.Enhanced)
    SmartisanPickerColors(normal = normalColor, selected = selectedColor) {
        SmartisanPickerRow(
            backgroundRes = R.drawable.time_picker_widget_bg_ex_new,
            height = dimensionResource(R.dimen.date_time_picker_height),
            modifier = modifier,
            itemHeight = itemHeight,
        ) {
            // 日期列：取值是「距起始日期第几天」，显示成 yyyy/MM/dd。
            SmartisanPickerColumn(
                value = currentDayIndex,
                minValue = 0,
                maxValue = maxDayIndex,
                onValueChange = { index ->
                    onDateTimeChange(smartisanDateTimeMillis(baseMillis, index, hour, minute))
                },
                modifier = Modifier.weight(1f),
                formatter = { index -> dayFormatter.format(baseMillis + index * MillisPerDay) },
                visibleCount = visibleCount,
                itemHeight = itemHeight,
            )
            SmartisanPickerDivider(dimensionResource(R.dimen.smartisan_datepicker_divider_width))
            // 小时列：原版没设 formatter，所以按原样显示 0..23（不补零）。
            SmartisanPickerColumn(
                value = hour,
                minValue = 0,
                maxValue = 23,
                onValueChange = { newHour ->
                    onDateTimeChange(smartisanDateTimeMillis(baseMillis, currentDayIndex, newHour, minute))
                },
                modifier = Modifier.weight(1f),
                unit = if (useSuffix) SuffixHour else null,
                visibleCount = visibleCount,
                itemHeight = itemHeight,
            )
            SmartisanPickerDivider(dimensionResource(R.dimen.smartisan_datepicker_divider_width))
            // 分钟列：原版用 `getTwoDigitFormatter()`，即 %02d 补零。
            SmartisanPickerColumn(
                value = minute,
                minValue = 0,
                maxValue = 59,
                onValueChange = { newMinute ->
                    onDateTimeChange(smartisanDateTimeMillis(baseMillis, currentDayIndex, hour, newMinute))
                },
                modifier = Modifier.weight(1f),
                formatter = { value -> "%02d".format(value) },
                unit = if (useSuffix) SuffixMinute else null,
                visibleCount = visibleCount,
                itemHeight = itemHeight,
            )
        }
    }
}

/**
 * 日期时间选择弹窗：贴底弹窗 + 标题栏（左取消 / 右完成）+ [SmartisanDateTimePicker]。
 *
 * 对应原版 `SmartisanDateTimePickerDialog`（`date_time_picker_dialog.xml`）：原版是
 * `gravity = bottom` 的 Dialog，标题栏是 `MenuDialogTitleBar`，标题由调用方通过 `updateTitle` 设置，
 * 这里换成 [title] 参数（为 null 时用日期文案）。
 *
 * @param timeMillis 当前选中的时间戳。
 * @param onDateTimeChange 时间变化回调。
 * @param onDismissRequest 关闭回调。
 * @param modifier 作用于弹窗面板。
 * @param onConfirm 点「完成」图标的回调，参数是当前选中的时间戳。
 * @param minTimeMillis 可选范围下界。
 * @param maxTimeMillis 可选范围上界。
 * @param title 标题文字。
 * @param visibleCount 可见行数。
 * @param itemHeight 单行高度。
 */
@Composable
fun SmartisanDateTimePickerDialog(
    timeMillis: Long,
    onDateTimeChange: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onConfirm: (Long) -> Unit = {},
    minTimeMillis: Long = DateTimeMinMillis,
    maxTimeMillis: Long = DateTimeMaxMillis,
    title: String? = null,
    visibleCount: Int = DefaultVisibleCount,
    itemHeight: Dp = PickerItemHeight,
) {
    val context = LocalContext.current
    SmartisanBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        SmartisanDialogTitleBar(
            title = title ?: smartisanDateTimeTitle(context, timeMillis),
            onDismiss = onDismissRequest,
            onConfirm = { onConfirm(timeMillis) },
        )
        SmartisanDateTimePicker(
            timeMillis = timeMillis,
            onDateTimeChange = onDateTimeChange,
            minTimeMillis = minTimeMillis,
            maxTimeMillis = maxTimeMillis,
            visibleCount = visibleCount,
            itemHeight = itemHeight,
        )
    }
}

/** 原版 `CalendarUtils.getMinTimeMills()`：1970-01-01 00:00。 */
private val DateTimeMinMillis: Long = smartisanCalendarMillis(1970, 1, 1)

/** 原版 `CalendarUtils.getMaxTimeMills()`：2037-12-31 23:59:59.999。 */
private val DateTimeMaxMillis: Long = smartisanCalendarMillis(2037, 12, 31, 23, 59, 59, 999)

/**
 * 某年某月的天数，对应原版 `Calendar#getActualMaximum(Calendar.DAY_OF_MONTH)`。
 *
 * @param year 年份（[SmartisanUnsetYear] 时按 0 年算，只用于取当月天数，不影响显示）。
 * @param month 月份，1..12。
 */
private fun smartisanDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(year, (month - 1).coerceIn(0, MonthsPerYear - 1), 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

/**
 * 月份列的文字。
 *
 * 原版取 `DateFormatSymbols#getShortMonths()`，若首字符是数字（中文、日语等）就退回 1..12 纯数字，
 * 见原版 `usingNumericMonths()`；中文的「月」由 Ex 版的单位后缀补上。
 *
 * @param locale 当前语言。
 */
private fun smartisanMonthLabels(locale: Locale): List<String> {
    val shortMonths = DateFormatSymbols(locale).shortMonths
    val numeric = shortMonths.firstOrNull()?.firstOrNull()?.isDigit() == true
    return if (numeric) {
        (1..MonthsPerYear).map { month -> month.toString() }
    } else {
        (0 until MonthsPerYear).map { index -> shortMonths.getOrElse(index) { (index + 1).toString() } }
    }
}

/**
 * 三列的排列顺序。
 *
 * 对应原版 `SmartisanDatePickerEx#reorderSpinners()`：原版用 ICU 的
 * `getDateFormatOrder("yyyyMMMdd")`，这里用等价的 [DateFormat.getDateFormatOrder]，
 * 返回 `y` / `M` / `d` 三个字符；取不到时退回「年 / 月 / 日」。
 *
 * @param context 用来读当前区域设置。
 */
private fun smartisanDateFieldOrder(context: Context): List<Char> {
    val order = DateFormat.getDateFormatOrder(context).toList()
    val valid = order.size == DefaultDateFieldOrder.size && order.all { field -> field in DefaultDateFieldOrder }
    return if (valid) order else DefaultDateFieldOrder
}

/**
 * 是否给各列加「年 / 月 / 日 / 时 / 分」单位后缀。
 *
 * 原版只在中文下加（`"zh".equals(Locale.getDefault().getLanguage())`），这里保持一致。
 *
 * @param locale 当前语言。
 */
private fun smartisanUseChineseSuffix(locale: Locale): Boolean = locale.language == Locale.CHINESE.language

/**
 * 日期弹窗的标题：原版 `SmartisanDatePickerDialog#updateTitle` 用 `DateUtils.formatDateTime`，
 * 年份未设置（生日）时去掉年份（原版 flags 65560，否则 98326）。
 */
private fun smartisanDateTitle(context: Context, year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(year, (month - 1).coerceIn(0, MonthsPerYear - 1), day.coerceAtLeast(1))
    val flags =
        if (year == SmartisanUnsetYear) {
            // 原版 65560 = ABBREV_MONTH | SHOW_DATE | NO_YEAR。
            DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR
        } else {
            // 原版 98326 = ABBREV_MONTH | ABBREV_WEEKDAY | SHOW_DATE | SHOW_YEAR | SHOW_WEEKDAY。
            DateUtils.FORMAT_ABBREV_MONTH or
                DateUtils.FORMAT_ABBREV_WEEKDAY or
                DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_SHOW_WEEKDAY
        }
    return DateUtils.formatDateTime(context, calendar.timeInMillis, flags)
}

/**
 * 日期时间弹窗的标题。
 *
 * 原版 `SmartisanDateTimePickerDialog` 的标题由调用方设置，这里默认按原版
 * `SmartisanDateTimePicker#onPopulateAccessibilityEvent` 的 flags = 20（`SHOW_DATE | SHOW_YEAR`）格式化。
 */
private fun smartisanDateTimeTitle(context: Context, timeMillis: Long): String =
    DateUtils.formatDateTime(
        context,
        timeMillis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR,
    )

/**
 * 用日历算时间戳（月份用人类习惯的 1..12）。
 *
 * @param year 年。
 * @param month 月，1..12。
 * @param day 日。
 * @param hour 时。
 * @param minute 分。
 * @param second 秒。
 * @param millisecond 毫秒。
 */
private fun smartisanCalendarMillis(
    year: Int,
    month: Int,
    day: Int,
    hour: Int = 0,
    minute: Int = 0,
    second: Int = 0,
    millisecond: Int = 0,
): Long {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(year, month - 1, day, hour, minute, second)
    calendar.set(Calendar.MILLISECOND, millisecond)
    return calendar.timeInMillis
}

/** 取某天 0 点的时间戳，原版 `changeMinDate` 就是 `set(year, month, day, 0, 0, 0)`。 */
private fun smartisanStartOfDay(timeMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timeMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** 由「距起始日期的天数 + 时 + 分」算时间戳，对应原版 `getTimMillsForDayValue`。 */
private fun smartisanDateTimeMillis(baseMillis: Long, dayIndex: Int, hour: Int, minute: Int): Long =
    baseMillis + dayIndex * MillisPerDay + hour * MillisPerHour + minute * MillisPerMinute
