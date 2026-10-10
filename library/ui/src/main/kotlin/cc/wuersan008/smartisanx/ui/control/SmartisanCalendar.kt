/**
 * 控件：锤子日历（月视图 + 单周视图）。
 *
 * 对应 framework（`framework/smartisanos.jar` 的 `classes.dex`）`smartisanos.widget.calendar` 包：
 *
 * | framework 类 | 行数 | 本库对应 |
 * | --- | --- | --- |
 * | `calendar.CalendarView` | 615 | [SmartisanCalendar]：标题栏、星期栏、月份切换 |
 * | `calendar.MonthWeekEventsView` | 772 | [SmartisanCalendar] 的网格绘制 |
 * | `calendar.MonthByWeekAdapter` | 355 | 周行数据（[smartisanCalendarWeeks]） |
 * | `calendar.DragViewSwitcher` | 154 | 左右滑动切换月份（库内用 `AnimatedContent`） |
 * | `calendar.NormalDayCellDrawer` / `MonthByWeekDayViewDrawer` / `DayCellViewDrawer` / `PaintFactory` | 205 | 日期文字的取色、字号与基线 |
 * | `calendar.SequenceAnimUtils` | 45 | 切换动画的时长（见下） |
 * | `calendar.SmartisanCalendarPopupDialog` | 127 | 未移植：它只是「日历 + 时间滚轮 + 确定 / 取消」的弹窗壳，用 [SmartisanCalendar] + [SmartisanTimePicker] + [SmartisanDialogTitleBar] 就能拼出 |
 * | `calendar.SmartisanTimePicker1Day` / `SmartisanNumberPicker1Day` | 1314 | 时 / 分 / 上下午三个滚轮，已由 [SmartisanTimePicker] 的 [SmartisanPickerVariant.Calendar] 覆盖 |
 * | `calendar.DateTimeSavedState` / `ITimeChangeListener` | 73 | 无对应：前者是 `onSaveInstanceState` 的壳、后者是原版内部回调；本组件的状态由调用方持有 |
 *
 * 原版这套日历用在便签 / 提醒的「选择提醒时间」里（素材名都是 `remind_` 开头），绘制规则全部保留：
 *
 * 1. **不在当月的格子**是「灰块 + 看不见的白字」：8% 黑（`remind_month_view_grey_day_item`）
 *    的方块上，日期数字取 `month_day_number_other`（白色），在白底上等于隐形 —— 一屏看过去
 *    就是当月那块的形状（原版 `MonthWeekEventsView#drawSpecificBackground` +
 *    `NormalDayCellDrawer#drawView`）；
 * 2. **今天那一格写「今天」两个字**（14sp，`today_text_size_month_number`）而不是日期数字
 *    （18sp，`text_size_month_number`），并且带一个药丸底：今天同时是选中日 → 蓝底
 *    （`remind_calendar_month_view_today_focused`）、今天不是选中日 → 浅灰底
 *    （`remind_calendar_month_view_day_unfocused`）；今天与选中日的文字都加粗、取
 *    `month_today_number`（白色）；
 * 3. **选中日**用另一张蓝色药丸（`remind_calendar_month_view_day_focused`），比格子左右各宽
 *    1dp、上下各高 1.4dp（`monthbyweek_h` / `monthbyweek_v`）；
 * 4. **周与周之间的分割线**来自行底纹素材本身：`remind_month_grid_body_for_drop` 除了上下各
 *    1.33dp 的 10% 黑之外全透明；
 * 5. 格子宽度是 `(网格宽 + 0.8dp) / 7`（`monthbyweek_border_width`），行高 44dp
 *    （`monthweek_item_height`）；日期文字基线在月视图里距行顶 30dp
 *    （`monthweek_relative_month_num`）、单周视图里 38dp（`monthweek_relative_week_en_num`）；
 * 6. 网格左右各 12.3dp 的内边距来自外框 9-patch（`remind_month_content_frame`）自带的 padding，
 *    与星期栏的 `fullmonthheader_margin_left/right` 是同一个数，所以两者对齐。
 *
 * 与原版的差异（都是有意的）：
 *
 * 1. **时间类型**：原版全程用 `android.text.format.Time` 与儒略日（`CalendarUtils`），本库用
 *    `java.time` 的 [LocalDate] / [YearMonth] / [DayOfWeek]；周序计算与原版一致
 *    （见 [smartisanCalendarWeekStart]）；
 * 2. **竖直手势没有移植**：原版 `CalendarView#prepareFollowingView` 只处理 `changeKind == 1`
 *    （左右），竖直方向（`changeKind == 2`）直接返回 false —— 也就是「月 ↔ 单周」的收起手势
 *    在这套 framework 里是空实现，单周视角只由宿主通过 adapter 参数打开。本库把「单周」做成
 *    显式参数 [singleWeek]，切换时按原版 `updatePercent()` 的定义重新布局（行高 67.6dp、
 *    文字基线 38dp、灰块淡出），不做手势；
 * 3. **切换月份是同步的**：原版箭头连点会攒在 `mAddTimes` 里、由 200ms 的延迟消息合并成一次跳转，
 *    本库每次点按都直接换一个月（各配一段滑动）；
 * 4. **动画时长**：原版四个 `anim/remind_week_` 动画文件写的是 400ms，但代码里会被
 *    `SequenceAnimUtils#getDuration` 覆盖 —— 队列为空（正常单次切换）时是 **300ms** + 默认
 *    插值器（加速减速）。本库取 300ms + `FastOutSlowInEasing`；排队连点时的 100 / 150 / 200ms
 *    三档只出现在原版的队列路径上，没有移植；
 * 5. **死代码没有移植**：`MonthWeekEventsView#drawToday()`（今天的描边高亮）、`mAnimateToday` /
 *    `mTodayAnimator`、以及 `remind_today_blue_week_holo_light` 这张素材，在当前 framework 里
 *    都没有被真正调用（`mAnimateToday` 从未置 true，`mTodayDrawable` 只被 `loadColors()` 加载、
 *    无人使用）；`EXPANDED_HEIGHT/WIDTH`、`mOriginalView`、`mAnimationValue`、
 *    `adjustAnimationBGRect()` 是留给应用侧「拖动放下」动画的钩子，本库没有对应实现；
 * 6. **时区与一周第一天**：原版标题用的是便签 / 日历的「主页时区」偏好
 *    （`CalendarUtils#getTimeZone` 读 `com.android.calendar_preferences`），本库用系统时区；
 *    一周第一天照抄原版只看**国家**的规则（美国、印尼从周日开始，其余周一，见
 *    [smartisanCalendarFirstDayOfWeek]），不看系统设置里的「一周第一天」；
 * 7. **日期格式**：标题沿用原版的 `DateUtils.formatDateRange(..., 52, tz)`（「2016年8月」这种
 *    月 + 年），星期栏沿用 `DateUtils.getDayOfWeekString(i, 20)` 再转大写；
 * 8. 本组件**不画页面底色**（原版也是透明，由宿主决定），也没有夜间变体：原版这套素材只有一套
 *    浅色，深色下依旧是白底黑字。
 *
 * 用法：
 *
 * ```kotlin
 * var date by remember { mutableStateOf(LocalDate.now()) }
 * SmartisanCalendar(
 *     selectedDate = date,
 *     onDateSelected = { date = it },
 *     onTitleClick = { showDatePicker = true },   // 原版 setNeedToHandleTitleClicked(true)
 * )
 * ```
 *
 * @param selectedDate 选中日期（唯一状态源）；点按格子、切换月份都会通过 [onDateSelected] 回报。
 * @param onDateSelected 选中日期变化的回调；点已选中的那一天不回调（原版 `isTheSameDay` 判断）。
 * @param modifier 外部修饰符。
 * @param minDate 可选日期下限，对应原版 `initTimes(minMills, ...)`；范围外的日期不可见也不可点。
 * @param maxDate 可选日期上限。
 * @param firstDayOfWeek 一周的第一天；默认按原版的国家规则推导。
 * @param singleWeek 单周视角（原版 adapter 的 `single_week` 参数）：只显示选中日期所在那一周。
 * @param today 「今天」；默认取系统当前日期，测试与预览时可以固定。
 * @param showTitleBar 是否显示标题栏（含左右箭头与下方分割线）。
 * @param hasFocus 是否处于「聚焦」状态（原版 `MonthByWeekAdapter#setHasFocus`）：false 时不把非当月
 *   格子灰掉、日期数字全部可见，对应原版日历失去焦点（例如弹窗收起）时的样子。
 * @param onTitleClick 点击标题的回调；原版要宿主先 `setNeedToHandleTitleClicked(true)` 才响应。
 * @param enabled 是否可交互。
 */
package cc.wuersan008.smartisanx.ui.control

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePadding
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanThemedResources
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Formatter
import java.util.Locale
import kotlin.math.roundToInt

/** 日历用到的尺寸与范围，全部对应原版 framework 的 dimens 与常量。 */
object SmartisanCalendarDefaults {
    /** 标题栏高度（原版 `dimen/allinone_titilebar_height`）。 */
    val TitleBarHeight = 48.dp

    /** 标题文本的宽度（原版 `dimen/allinone_dateview_layout_width`）。 */
    val TitleWidth = 174.6.dp

    /** 标题左右箭头距边缘的距离（原版 `calendar_view.xml` 里写死的 57dp）。 */
    val ArrowMargin = 57.dp

    /** 箭头素材尺寸（`remind_previous_arrow.png` 等，153×153px @xxhdpi ⇒ 51dp）。 */
    val ArrowSize = 51.dp

    /** 标题栏下方分割线的高度（原版布局写死 1dp）。 */
    val SeparatorHeight = 1.dp

    /** 星期栏高度（原版 `dimen/fullmonthheader_layout_height`）。 */
    val DayLabelHeight = 29.3.dp

    /** 星期栏左右边距（原版 `dimen/fullmonthheader_margin_left` / `_right`）。 */
    val DayLabelHorizontalMargin = 12.3.dp

    /** 星期栏与标题栏 / 网格之间的间距（原版 `calendar_view.xml` 里 include 上的 9dp、7dp）。 */
    val DayLabelTopMargin = 9.dp
    val DayLabelBottomMargin = 7.dp

    /** 月视图里一周行的高度（原版 `dimen/monthweek_item_height`，只有 xxhdpi 有值）。 */
    val MonthRowHeight = 44.dp

    /** 单周视图里那一行的高度（原版 `dimen/monthweek_item_single_height`）。 */
    val SingleWeekRowHeight = 67.6.dp

    /** 日期文字基线在月视图里距行顶的距离（原版 `dimen/monthweek_relative_month_num`）。 */
    val DayNumberBaselineMonth = 30.dp

    /** 日期文字基线在单周视图里距行顶的距离（原版 `dimen/monthweek_relative_week_en_num`）。 */
    val DayNumberBaselineWeek = 38.dp

    /** 选中 / 今天药丸比格子向外多出的距离（原版 `dimen/monthbyweek_h` / `monthbyweek_v`）。 */
    val HighlightMarginHorizontal = 1.dp
    val HighlightMarginVertical = 1.4.dp

    /** 算格子宽度时的补偿量（原版 `dimen/monthbyweek_border_width`）。 */
    val CellBorderWidth = 0.8.dp

    /** 切换月份时的滑动时长（原版队列为空时 `SequenceAnimUtils#getDuration` 返回 300）。 */
    const val MonthSwitchDurationMillis = 300

    /** 可选日期下限（原版 `CalendarUtils.MIN_CALENDAR_YEAR` = 1970）。 */
    val MinDate: LocalDate = LocalDate.of(1970, 1, 1)

    /** 可选日期上限（原版 `CalendarUtils.MAX_CALENDAR_YEAR` = 2037）。 */
    val MaxDate: LocalDate = LocalDate.of(2037, 12, 31)
}

/** 一个格子（一天）的绘制信息。 */
private data class SmartisanCalendarCell(
    /** 这一天。 */
    val date: LocalDate,
    /** 是否属于当前显示的月份（原版 `MonthWeekEventsView.mFocusDay`）。 */
    val inFocusMonth: Boolean,
    /** 年份是否在 1970..2037 之外（原版 viewType 的 bit 32，`mIsDayOutOfRange`）。 */
    val outsideCalendarRange: Boolean,
    /** 是否在调用方给的可选范围之外（原版 viewType 的 bit 64，`mIsTodayBefore`）。 */
    val outsideSelectableRange: Boolean,
)

/** 一行（一周）。 */
private data class SmartisanCalendarWeek(
    /** 这一周的第一天。 */
    val start: LocalDate,
    /** 七天。 */
    val cells: List<SmartisanCalendarCell>,
)

/**
 * 一周从哪天开始。
 *
 * 照抄原版 `CalendarUtils#getDefaultWeekStartWithLocale`：只看**国家**，美国与印尼从周日开始、
 * 其余从周一开始；原版不看系统设置里的「一周第一天」（那套是 `getFirstDayOfWeekInCalendar`，
 * 日历里没用）。
 */
private fun smartisanCalendarFirstDayOfWeek(context: Context): DayOfWeek {
    val country = context.resources.configuration.locales[0].country
    return if (country == Locale.US.country || country == "ID") DayOfWeek.SUNDAY else DayOfWeek.MONDAY
}

/** 取某个日期所在那一周的第一天（原版 `CalendarUtils#adjustToBeginningOfWeek`）。 */
private fun smartisanCalendarWeekStart(date: LocalDate, weekStart: DayOfWeek): LocalDate =
    date.minusDays(((date.dayOfWeek.value - weekStart.value + 7) % 7).toLong())

/**
 * 这个月要画几行。
 *
 * 等价于原版 `MonthByWeekAdapter#getThisMonthWeekCount` 的 4 / 5 / 6：那三档分别对应
 * 「1 号前面空出的天数 + 当月天数」落在 28 / 29..35 / 36 以上。
 */
private fun smartisanCalendarRowCount(month: YearMonth, weekStart: DayOfWeek): Int {
    val lead =
        ChronoUnit.DAYS
            .between(smartisanCalendarWeekStart(month.atDay(1), weekStart), month.atDay(1))
            .toInt()
    return (lead + month.lengthOfMonth() + 6) / 7
}

/**
 * 生成要绘制的周行。
 *
 * 单周视角下原版只保留一行（`MonthByWeekAdapter#getCount` 返回 1，`sortWeeksLayout` 里唯一
 * 的 view 就是选中那一周），所以这里直接取选中日期所在那一周。
 */
private fun smartisanCalendarWeeks(
    focusMonth: YearMonth,
    selectedDate: LocalDate,
    weekStart: DayOfWeek,
    singleWeek: Boolean,
    minDate: LocalDate,
    maxDate: LocalDate,
): List<SmartisanCalendarWeek> {
    val rows = if (singleWeek) 1 else smartisanCalendarRowCount(focusMonth, weekStart)
    val first =
        smartisanCalendarWeekStart(
            if (singleWeek) selectedDate else focusMonth.atDay(1),
            weekStart,
        )
    return List(rows) { row ->
        val start = first.plusWeeks(row.toLong())
        SmartisanCalendarWeek(
            start = start,
            cells =
                List(7) { column ->
                    val date = start.plusDays(column.toLong())
                    SmartisanCalendarCell(
                        date = date,
                        inFocusMonth = YearMonth.from(date) == focusMonth,
                        outsideCalendarRange = date.year < 1970 || date.year > 2037,
                        outsideSelectableRange = date < minDate || date > maxDate,
                    )
                },
        )
    }
}

/**
 * 标题文字（原版 `CalendarView#buildMonthYearDate`）。
 *
 * 原版用 `DateUtils.formatDateRange(..., 52, tz)`：52 = `FORMAT_NO_MONTH_DAY |
 * FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR`，中文下就是「2016年8月」。
 */
private fun smartisanCalendarTitle(context: Context, month: YearMonth): String {
    val millis =
        month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val builder = StringBuilder(50)
    val formatter = Formatter(builder, Locale.getDefault())
    return DateUtils
        .formatDateRange(context, formatter, millis, millis, 52, ZoneId.systemDefault().id)
        .toString()
}

/**
 * 星期栏文字（原版 `CalendarView#initViews` + `updateDayNamesHeader`）。
 *
 * `DateUtils.getDayOfWeekString` 在新版 SDK 里被标记为 deprecated，但原版用的就是它，
 * 而且它给的正是原版那套「短星期」写法（中文「周一」、英文「MON」），所以照用。
 */
@Suppress("DEPRECATION")
private fun smartisanCalendarDayLabels(weekStart: DayOfWeek): List<String> =
    List(7) { index ->
        // DateUtils 要的是 Calendar 的 1..7（周日..周六），这里从 DayOfWeek 换算。
        val calendarDayOfWeek = (weekStart.plus(index.toLong()).value % 7) + 1
        DateUtils.getDayOfWeekString(calendarDayOfWeek, 20).uppercase(Locale.getDefault())
    }

/** 左右箭头是否还有效（原版 `CalendarUtils#turningMonthIsValid`，只看年份边界）。 */
private fun smartisanCalendarCanTurnMonth(month: YearMonth, forward: Boolean): Boolean =
    (if (forward) month.plusMonths(1) else month.minusMonths(1)).year in 1970..2037

/**
 * 点箭头之后选中日期会落到哪一天（原版 `CalendarView#getMonthCalendarByOffset`）。
 *
 * 原版从「显示月份里的一天」出发：显示月份就是选中日期所在月时从选中日期出发，否则取该月 15 号；
 * 加 / 减一个月后，如果回到选中日期所在月就保留原来的日，否则取 1 号。
 */
private fun smartisanCalendarDateAfterMonthSwitch(
    selectedDate: LocalDate,
    focusMonth: YearMonth,
    forward: Boolean,
    minDate: LocalDate,
    maxDate: LocalDate,
): LocalDate {
    val start = if (focusMonth == YearMonth.from(selectedDate)) selectedDate else focusMonth.atDay(15)
    val target = if (forward) start.plusMonths(1) else start.minusMonths(1)
    val day =
        if (YearMonth.from(target) == YearMonth.from(selectedDate)) {
            selectedDate.dayOfMonth.coerceAtMost(target.lengthOfMonth())
        } else {
            1
        }
    return target.withDayOfMonth(day).coerceIn(minDate, maxDate)
}

/**
 * 锤子日历：月视图 / 单周视图，点按选择日期。
 *
 * @see SmartisanCalendarDefaults
 */
@Composable
fun SmartisanCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = SmartisanCalendarDefaults.MinDate,
    maxDate: LocalDate = SmartisanCalendarDefaults.MaxDate,
    firstDayOfWeek: DayOfWeek? = null,
    singleWeek: Boolean = false,
    today: LocalDate = LocalDate.now(),
    showTitleBar: Boolean = true,
    hasFocus: Boolean = true,
    onTitleClick: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val context = LocalContext.current
    val weekStart =
        remember(context, firstDayOfWeek) {
            firstDayOfWeek ?: smartisanCalendarFirstDayOfWeek(context)
        }
    // 显示月份（原版 `CalendarView.mCurrentMonthDisplayed` / adapter 的 focus_month）。
    var focusMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    // 原版 goTo() 会把显示月份跟着选中日期走，这里在外部改了选中日期时同步一次。
    LaunchedEffect(selectedDate) { focusMonth = YearMonth.from(selectedDate) }

    Column(modifier.fillMaxWidth()) {
        if (showTitleBar) {
            SmartisanCalendarTitleBar(
                title = remember(context, focusMonth) { smartisanCalendarTitle(context, focusMonth) },
                showPrevious = smartisanCalendarCanTurnMonth(focusMonth, forward = false),
                showNext = smartisanCalendarCanTurnMonth(focusMonth, forward = true),
                enabled = enabled,
                onPrevious = {
                    onDateSelected(
                        smartisanCalendarDateAfterMonthSwitch(
                            selectedDate = selectedDate,
                            focusMonth = focusMonth,
                            forward = false,
                            minDate = minDate,
                            maxDate = maxDate,
                        ),
                    )
                    focusMonth = focusMonth.minusMonths(1)
                },
                onNext = {
                    onDateSelected(
                        smartisanCalendarDateAfterMonthSwitch(
                            selectedDate = selectedDate,
                            focusMonth = focusMonth,
                            forward = true,
                            minDate = minDate,
                            maxDate = maxDate,
                        ),
                    )
                    focusMonth = focusMonth.plusMonths(1)
                },
                onTitleClick = onTitleClick,
            )
        }
        SmartisanCalendarDayLabels(weekStart = weekStart)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .smartisanDrawableBackground(SmartisanDrawables.CalendarContentFrame)
                    .padding(
                        rememberSmartisanDrawablePadding(SmartisanDrawables.CalendarContentFrame),
                    ),
        ) {
            AnimatedContent(
                targetState = focusMonth,
                transitionSpec = {
                    val forward = targetState > initialState
                    val spec =
                        tween<IntOffset>(
                            durationMillis = SmartisanCalendarDefaults.MonthSwitchDurationMillis,
                            easing = FastOutSlowInEasing,
                        )
                    // 原版 `anim/remind_week_left_*` / `remind_week_right_*`：整块平移一屏宽。
                    (slideInHorizontally(animationSpec = spec) { width -> if (forward) width else -width } togetherWith
                        slideOutHorizontally(animationSpec = spec) { width -> if (forward) -width else width })
                },
                label = "smartisan calendar month",
            ) { month ->
                SmartisanCalendarGrid(
                    month = month,
                    selectedDate = selectedDate,
                    today = today,
                    weekStart = weekStart,
                    singleWeek = singleWeek,
                    minDate = minDate,
                    maxDate = maxDate,
                    hasFocus = hasFocus,
                    enabled = enabled,
                    onDateSelected = onDateSelected,
                )
            }
        }
    }
}

/** 标题颜色：原版布局里写死的 `#9a000000`（60% 黑）。 */
private val SmartisanCalendarTitleColor = Color(0x9A000000)

/** 标题栏：居中标题 + 左右箭头 + 下方 1dp 分割线（原版 `calendar_view.xml`）。 */
@Composable
private fun SmartisanCalendarTitleBar(
    title: String,
    showPrevious: Boolean,
    showNext: Boolean,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: (() -> Unit)?,
) {
    val density = LocalDensity.current
    val titleSize = with(density) { dimensionResource(R.dimen.remind_title_text_size).toSp() }
    Box(
        Modifier
            .fillMaxWidth()
            .height(SmartisanCalendarDefaults.TitleBarHeight),
    ) {
        // 标题：原版 `date_title` 宽 174.6dp、居中、14dp 加粗、#9a000000。
        SmartisanText(
            text = title,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .width(SmartisanCalendarDefaults.TitleWidth)
                    .then(
                        if (onTitleClick != null && enabled) {
                            Modifier.smartisanClickable(onClick = onTitleClick)
                        } else {
                            Modifier
                        },
                    ),
            style = TextStyle(fontSize = titleSize, fontWeight = FontWeight.Bold),
            color = SmartisanCalendarTitleColor,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        SmartisanCalendarArrow(
            iconRes = SmartisanDrawables.CalendarPreviousArrow,
            visible = showPrevious,
            enabled = enabled,
            contentDescription = "上一个月",
            onClick = onPrevious,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = SmartisanCalendarDefaults.ArrowMargin),
        )
        SmartisanCalendarArrow(
            iconRes = SmartisanDrawables.CalendarNextArrow,
            visible = showNext,
            enabled = enabled,
            contentDescription = "下一个月",
            onClick = onNext,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = SmartisanCalendarDefaults.ArrowMargin),
        )
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(SmartisanCalendarDefaults.SeparatorHeight)
            .smartisanDrawableBackground(SmartisanDrawables.CalendarTitleBarSeparator),
    )
}

/**
 * 标题栏上的月份箭头。
 *
 * 箭头素材自带按下 / 聚焦态 selector，但照抄原版之后，按下的反馈由整块按钮提供
 * （原版只有获得焦点时才会用 `_down` 那张，见 `reminder_previous_arrow_selector.xml` 的注释）。
 */
@Composable
private fun SmartisanCalendarArrow(
    @DrawableRes iconRes: Int,
    visible: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Image(
        painter = rememberSmartisanDrawablePainter(iconRes, enabled = enabled, pressed = pressed),
        contentDescription = contentDescription,
        modifier =
            modifier
                .size(SmartisanCalendarDefaults.ArrowSize)
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    onClick = onClick,
                ),
    )
}

/** 星期栏（原版 `remind_full_month_header.xml` + `TextAppearance.MonthView_DayLabel`）。 */
@Composable
private fun SmartisanCalendarDayLabels(weekStart: DayOfWeek) {
    val density = LocalDensity.current
    val labels = remember(weekStart) { smartisanCalendarDayLabels(weekStart) }
    val textSize = with(density) { dimensionResource(R.dimen.monthview_day_label_text_size).toSp() }
    val color = colorResource(R.color.black_60)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = SmartisanCalendarDefaults.DayLabelHorizontalMargin,
                top = SmartisanCalendarDefaults.DayLabelTopMargin,
                end = SmartisanCalendarDefaults.DayLabelHorizontalMargin,
                bottom = SmartisanCalendarDefaults.DayLabelBottomMargin,
            ).height(SmartisanCalendarDefaults.DayLabelHeight),
    ) {
        labels.forEach { label ->
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                SmartisanText(
                    text = label,
                    style = TextStyle(fontSize = textSize),
                    color = color,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}


/** 网格的绘制尺寸与颜色（一次算好，供 [drawSmartisanCalendarGrid] 使用）。 */
private class SmartisanCalendarGridMetrics(
    /** 一格宽度 = (网格宽 + 0.8dp) / 7（原版 `onMeasure`）。 */
    val cellWidth: Float,
    /** 一行高度（月视图 44dp / 单周视图 67.6dp）。 */
    val rowHeight: Float,
    /** 原版 `monthbyweek_border_width`，只参与算格子宽度。 */
    val borderWidth: Float,
    /** 药丸比格子向外多出的距离（`monthbyweek_h` / `monthbyweek_v`）。 */
    val highlightMarginHorizontal: Float,
    val highlightMarginVertical: Float,
    /** 日期文字基线距行顶的距离（月视图 30dp / 单周视图 38dp）。 */
    val baselineMonth: Float,
    val baselineWeek: Float,
    /** 日期文字字号：常规 18sp、今天 14sp（都是 sp，跟随系统字号）。 */
    val dayNumberSize: Float,
    val todayTextSize: Float,
    /** 颜色：常规 `black_60`、非当月 `month_day_number_other`、今天 / 选中 `month_today_number`。 */
    val normalColor: Int,
    val otherMonthColor: Int,
    val todayColor: Int,
    /** 原版 `mSwitchAnimProgress`：月视图 0、单周视图 1。 */
    val progress: Float,
    /** 原版 `MonthByWeekAdapter#setHasFocus`。 */
    val hasFocus: Boolean,
)

/** 日历用到的几张 9-patch / 位图（都按 smartisanx 主题解析）。 */
private class SmartisanCalendarDrawables(
    val weekRowBackground: Drawable?,
    val otherMonthCell: Drawable?,
    val selectedDay: Drawable?,
    val todaySelected: Drawable?,
    val today: Drawable?,
)

@Composable
private fun rememberSmartisanCalendarDrawables(): SmartisanCalendarDrawables {
    val resources = smartisanThemedResources()
    return remember(resources) {
        SmartisanCalendarDrawables(
            weekRowBackground =
                resources.getDrawable(SmartisanDrawables.CalendarWeekRowBackground, null)?.mutate(),
            otherMonthCell =
                resources.getDrawable(SmartisanDrawables.CalendarOtherMonthCell, null)?.mutate(),
            selectedDay =
                resources.getDrawable(SmartisanDrawables.CalendarSelectedDay, null)?.mutate(),
            todaySelected =
                resources.getDrawable(SmartisanDrawables.CalendarTodaySelected, null)?.mutate(),
            today = resources.getDrawable(SmartisanDrawables.CalendarToday, null)?.mutate(),
        )
    }
}


/** 网格（原版 `MonthWeekEventsView` 的绘制部分 + `MonthByWeekAdapter` 的行数据）。 */
@Composable
private fun SmartisanCalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    weekStart: DayOfWeek,
    singleWeek: Boolean,
    minDate: LocalDate,
    maxDate: LocalDate,
    hasFocus: Boolean,
    enabled: Boolean,
    onDateSelected: (LocalDate) -> Unit,
) {
    val density = LocalDensity.current
    val drawables = rememberSmartisanCalendarDrawables()
    val haptic = smartisanHaptic()
    val todayText = stringResource(R.string.smartisan_calendar_today)
    val weeks =
        remember(month, selectedDate, weekStart, singleWeek, minDate, maxDate) {
            smartisanCalendarWeeks(month, selectedDate, weekStart, singleWeek, minDate, maxDate)
        }
    // 原版 `MonthWeekEventsView#updatePercent()`：月视图 0、单周视图 1。
    // 灰块透明度、日期文字基线、非当月格子的取色都按它插值。
    val progress = if (singleWeek) 1f else 0f
    val rowHeight =
        if (singleWeek) {
            SmartisanCalendarDefaults.SingleWeekRowHeight
        } else {
            SmartisanCalendarDefaults.MonthRowHeight
        }
    val textPaint =
        remember {
            Paint().apply {
                isAntiAlias = true
                // 原版是右对齐 + 手工居中（`getCenterPosition`），居中对齐结果一致。
                textAlign = Paint.Align.CENTER
            }
        }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val borderWidth = with(density) { SmartisanCalendarDefaults.CellBorderWidth.toPx() }
        // 原版 onMeasure：格子宽 = (视图宽 + 0.8dp) / 7。
        val cellWidth = (constraints.maxWidth + borderWidth) / 7f
        val rowHeightPx = with(density) { rowHeight.toPx() }
        val metrics =
            with(density) {
                SmartisanCalendarGridMetrics(
                    cellWidth = cellWidth,
                    rowHeight = rowHeightPx,
                    borderWidth = borderWidth,
                    highlightMarginHorizontal =
                        SmartisanCalendarDefaults.HighlightMarginHorizontal.toPx(),
                    highlightMarginVertical =
                        SmartisanCalendarDefaults.HighlightMarginVertical.toPx(),
                    baselineMonth = SmartisanCalendarDefaults.DayNumberBaselineMonth.toPx(),
                    baselineWeek = SmartisanCalendarDefaults.DayNumberBaselineWeek.toPx(),
                    dayNumberSize = dimensionResource(R.dimen.text_size_month_number).toPx(),
                    todayTextSize = dimensionResource(R.dimen.today_text_size_month_number).toPx(),
                    normalColor = colorResource(R.color.black_60).toArgb(),
                    otherMonthColor = colorResource(R.color.month_day_number_other).toArgb(),
                    todayColor = colorResource(R.color.month_today_number).toArgb(),
                    progress = progress,
                    hasFocus = hasFocus,
                )
            }
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(with(density) { (rowHeightPx * weeks.size).toDp() })
                    .pointerInput(weeks, enabled, selectedDate, minDate, maxDate) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { offset ->
                            val row = (offset.y / rowHeightPx).toInt()
                            val column = (offset.x / cellWidth).toInt()
                            val date =
                                weeks.getOrNull(row)?.cells?.getOrNull(column)?.date
                                    ?: return@detectTapGestures
                            // 原版 `CalendarUtils.isValidDay` + `isTheSameDay`。
                            if (date < minDate || date > maxDate) return@detectTapGestures
                            if (date != selectedDate) {
                                haptic()
                                onDateSelected(date)
                            }
                        }
                    },
        ) {
            drawSmartisanCalendarGrid(
                weeks = weeks,
                metrics = metrics,
                drawables = drawables,
                selectedDate = selectedDate,
                today = today,
                todayText = todayText,
                textPaint = textPaint,
            )
        }
    }
}


/** 药丸（今天 / 选中日）的绘制区域（原版 `drawSpecificBackground` 里的 `bgRect`）。 */
private fun smartisanCalendarHighlightBounds(
    index: Int,
    rowTop: Float,
    metrics: SmartisanCalendarGridMetrics,
): Rect =
    Rect(
        (index * metrics.cellWidth - metrics.highlightMarginHorizontal).roundToInt(),
        (rowTop - metrics.highlightMarginVertical).roundToInt(),
        ((index + 1) * metrics.cellWidth + metrics.highlightMarginHorizontal).roundToInt(),
        (rowTop + metrics.rowHeight + metrics.highlightMarginVertical).roundToInt(),
    )

/**
 * 一格日期文字的颜色（原版 `NormalDayCellDrawer#drawView` 的取色分支）。
 *
 * - 今天 / 选中日：`month_today_number`（白，画在药丸上）；
 * - 非当月：`month_day_number_other`（白，画在灰块上 —— 看起来就是隐形），
 *   单周视角（progress ≥ 0.5）下换成常规色，因为那时灰块已经淡出；
 * - 超出可选范围：一律用非当月色（白 → 隐形），原版就是这样把不可选的日期藏起来的；
 * - 其余：`black_60`。
 */
private fun smartisanCalendarDayColor(
    cell: SmartisanCalendarCell,
    isToday: Boolean,
    isSelected: Boolean,
    metrics: SmartisanCalendarGridMetrics,
): Int {
    if (isToday || isSelected) return metrics.todayColor
    val faded =
        if (metrics.progress < 0.5f) metrics.otherMonthColor else metrics.normalColor
    if (!metrics.hasFocus) {
        return if (cell.outsideSelectableRange) metrics.otherMonthColor else metrics.normalColor
    }
    return when {
        !cell.inFocusMonth ->
            if (!cell.outsideCalendarRange && !cell.outsideSelectableRange) {
                faded
            } else {
                metrics.otherMonthColor
            }

        cell.outsideSelectableRange -> metrics.otherMonthColor
        else -> metrics.normalColor
    }
}


/**
 * 画整个网格。
 *
 * 顺序照抄原版 `MonthWeekEventsView#onDraw`：先把所有行的背景（行底纹 + 非当月灰块 + 今天 /
 * 选中药丸）画完，再画所有行的日期文字 —— 药丸上下各多出 1.4dp、会压到相邻行，
 * 分两轮画才不会互相盖住。
 */
private fun DrawScope.drawSmartisanCalendarGrid(
    weeks: List<SmartisanCalendarWeek>,
    metrics: SmartisanCalendarGridMetrics,
    drawables: SmartisanCalendarDrawables,
    selectedDate: LocalDate,
    today: LocalDate,
    todayText: String,
    textPaint: Paint,
) {
    val gridWidth = size.width
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        // ---- 第一轮：行底纹、非当月灰块、今天 / 选中药丸 ----
        weeks.forEachIndexed { rowIndex, week ->
            val rowTop = rowIndex * metrics.rowHeight
            // 原版 drawBasicBackgound：行底纹宽 = 视图宽 + 0.8dp，高 = 行高。
            drawables.weekRowBackground?.let { drawable ->
                drawable.setBounds(
                    0,
                    rowTop.roundToInt(),
                    (gridWidth + metrics.borderWidth).roundToInt(),
                    (rowTop + metrics.rowHeight).roundToInt(),
                )
                drawable.draw(native)
            }
            // 原版 drawBackground() 里只有 hasFocus 时才画下面这些。
            if (metrics.hasFocus) {
                drawables.otherMonthCell?.let { drawable ->
                    week.cells.forEachIndexed { index, cell ->
                        val grey =
                            !cell.inFocusMonth ||
                                cell.outsideCalendarRange ||
                                cell.outsideSelectableRange
                        if (!grey) return@forEachIndexed
                        drawable.setBounds(
                            (index * metrics.cellWidth).roundToInt(),
                            rowTop.roundToInt(),
                            ((index + 1) * metrics.cellWidth).roundToInt(),
                            (rowTop + metrics.rowHeight).roundToInt(),
                        )
                        // 原版按 (1 - progress) 淡出；1970 / 2037 边界外那几天它不设 alpha
                        // （沿用上一个格子的值），这里显式取 255，避免受绘制顺序影响。
                        drawable.alpha =
                            if (cell.outsideCalendarRange || cell.outsideSelectableRange) {
                                255
                            } else {
                                ((1f - metrics.progress) * 255f).roundToInt()
                            }
                        drawable.draw(native)
                    }
                }
                val todayIndex = week.cells.indexOfFirst { it.date == today }
                val selectedIndex = week.cells.indexOfFirst { it.date == selectedDate }
                // 今天：选中时用蓝底，否则用浅灰底。
                if (todayIndex >= 0) {
                    val drawable =
                        if (todayIndex == selectedIndex) drawables.todaySelected else drawables.today
                    drawable?.let {
                        it.setBounds(smartisanCalendarHighlightBounds(todayIndex, rowTop, metrics))
                        it.draw(native)
                    }
                }
                // 选中日：另一张蓝色药丸。
                if (selectedIndex >= 0 && selectedIndex != todayIndex) {
                    drawables.selectedDay?.let {
                        it.setBounds(
                            smartisanCalendarHighlightBounds(selectedIndex, rowTop, metrics),
                        )
                        it.draw(native)
                    }
                }
            }
        }
        // ---- 第二轮：日期文字 ----
        weeks.forEachIndexed { rowIndex, week ->
            val rowTop = rowIndex * metrics.rowHeight
            val baseline =
                rowTop +
                    metrics.baselineMonth +
                    (metrics.baselineWeek - metrics.baselineMonth) * metrics.progress
            week.cells.forEachIndexed { index, cell ->
                val isToday = cell.date == today
                val isSelected = cell.date == selectedDate
                textPaint.color = smartisanCalendarDayColor(cell, isToday, isSelected, metrics)
                textPaint.isFakeBoldText = isToday || isSelected
                textPaint.textSize = if (isToday) metrics.todayTextSize else metrics.dayNumberSize
                native.drawText(
                    if (isToday) todayText else cell.date.dayOfMonth.toString(),
                    (index + 0.5f) * metrics.cellWidth,
                    baseline,
                    textPaint,
                )
            }
        }
    }
}

