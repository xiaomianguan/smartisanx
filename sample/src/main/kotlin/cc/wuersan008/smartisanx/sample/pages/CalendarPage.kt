package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanCalendar
import cc.wuersan008.smartisanx.ui.control.SmartisanDatePickerDialog
import cc.wuersan008.smartisanx.ui.control.SmartisanPickerVariant
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import java.time.LocalDate

/**
 * 日历页：framework `smartisanos.widget.calendar` 那一套（便签 / 提醒里选日期的那个月历）。
 */
@Composable
fun CalendarPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var date by remember { mutableStateOf(LocalDate.of(2016, 8, 18)) }
    var weekDate by remember { mutableStateOf(LocalDate.now()) }
    var greyOutOtherMonths by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    SamplePageScaffold(title = "日历", onBack = onBack) {
        SampleSectionHeader("月视图（CalendarView / MonthWeekEventsView）")
        SmartisanText(
            text = "当前选中：${date.year} 年 ${date.monthValue} 月 ${date.dayOfMonth} 日",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            style = typography.listItemSecondary,
            color = colors.textTertiary,
        )
        SmartisanCalendar(
            selectedDate = date,
            onDateSelected = { date = it },
            modifier = Modifier.fillMaxWidth(),
        )

        SampleSectionHeader("交互")
        SmartisanGroup {
            SmartisanSwitchRow(
                text = "把非当月日期灰掉",
                summary = "对应原版 MonthByWeekAdapter#setHasFocus(false)：整屏都显示日期数字",
                checked = greyOutOtherMonths,
                onCheckedChange = { greyOutOtherMonths = it },
                position = SmartisanGroupRowPosition.Single,
            )
        }
        SmartisanCalendar(
            selectedDate = date,
            onDateSelected = { date = it },
            hasFocus = greyOutOtherMonths,
            modifier = Modifier.fillMaxWidth(),
        )

        SampleSectionHeader("单周视角（adapter 的 single_week）")
        SmartisanText(
            text = "只留选中那一周，行高从 44dp 变 67.6dp、日期基线从 30dp 下移到 38dp",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            style = typography.listItemSecondary,
            color = colors.textTertiary,
        )
        SmartisanCalendar(
            selectedDate = weekDate,
            onDateSelected = { weekDate = it },
            singleWeek = true,
            modifier = Modifier.fillMaxWidth(),
        )

        SampleSectionHeader("点标题跳转到日期")
        SmartisanText(
            text = "原版要宿主先 setNeedToHandleTitleClicked(true)，标题点开的是 SmartisanDatePickerExDialog",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            style = typography.listItemSecondary,
            color = colors.textTertiary,
        )
        SmartisanCalendar(
            selectedDate = date,
            onDateSelected = { date = it },
            onTitleClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        )

        SampleFootnote(
            "来自 framework 的 smartisanos.widget.calendar 包（CalendarView / MonthWeekEventsView / " +
                "MonthByWeekAdapter / DragViewSwitcher 等 15 个类，约 3.7k 行）。素材原样取自 " +
                "framework-smartisanos-res.apk：remind_month_grid_body_for_drop（周之间的细线）、" +
                "remind_month_view_grey_day_item（非当月灰块）、remind_calendar_month_view_day_focused / " +
                "_today_focused / _day_unfocused（三张药丸）、remind_month_content_frame（外框，左右各 " +
                "12.3dp 的 padding 就是靠它）、reminder_previous_arrow_selector / " +
                "remind_next_arrow_selector（51dp 箭头）。日历里的「今天」写法、蓝 / 灰药丸、" +
                "非当月格子「灰块 + 白字」的隐形效果都按原版绘制规则实现。",
        )
    }

    if (showDatePicker) {
        SmartisanDatePickerDialog(
            year = date.year,
            month = date.monthValue - 1,
            day = date.dayOfMonth,
            onDateChange = { _, _, _ -> },
            onDismissRequest = { showDatePicker = false },
            onConfirm = { year, month, day ->
                date = LocalDate.of(year, month + 1, day)
                showDatePicker = false
            },
            variant = SmartisanPickerVariant.Enhanced,
            title = "选择要跳转的日期",
        )
    }
}
