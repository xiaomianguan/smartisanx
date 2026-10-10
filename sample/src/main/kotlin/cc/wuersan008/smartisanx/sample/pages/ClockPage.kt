package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.clock.SmartisanAnalogClock
import cc.wuersan008.smartisanx.ui.clock.SmartisanCompactClock
import cc.wuersan008.smartisanx.ui.control.SmartisanDatePickerDialog
import cc.wuersan008.smartisanx.ui.control.SmartisanDateTimePickerDialog
import cc.wuersan008.smartisanx.ui.control.SmartisanTimePickerDialog
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem

/** 时钟页：机械表盘、小表盘、时间滚轮。 */
@Composable
fun ClockPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    SamplePageScaffold(title = "时钟与机械控件", onBack = onBack) {
        SampleSectionHeader("机械表盘")
        SmartisanGroup {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SmartisanAnalogClock(
                    time = LocalTime.of(10, 9, 36),
                    showSecondHand = true,
                    showEars = true,
                    size = 220.dp,
                )
                SmartisanText(
                    text = "带闹钟耳朵，秒针带轻微回弹",
                    modifier = Modifier.padding(top = 12.dp),
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
        }

        SampleSectionHeader("无数字表盘")
        SmartisanGroup {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanAnalogClock(
                    time = LocalTime.of(7, 45, 0),
                    showNumerals = false,
                    showSecondHand = false,
                    size = 140.dp,
                )
                SmartisanText(
                    text = "showNumerals = false\nshowSecondHand = false",
                    modifier = Modifier.padding(start = 16.dp),
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
        }

        SampleSectionHeader("小表盘")
        SmartisanGroup {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(24.dp, 32.dp, 40.dp, 56.dp).forEach { size ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SmartisanCompactClock(
                            hour = 9,
                            minute = 30,
                            showSecondHand = true,
                            size = size,
                        )
                        SmartisanText(
                            text = "${size.value.toInt()}dp",
                            modifier = Modifier.padding(top = 6.dp),
                            style = typography.caption,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
        }

        SampleSectionHeader("时间滚轮")
        TimePickerSection()

        SampleSectionHeader("通用滚轮")
        WheelPickerSection()

        SampleSectionHeader("计时标尺")
        RulerPickerSection()

        SampleSectionHeader("重复日选择")
        WeekdayPickerSection()

        SampleSectionHeader("日期 / 时间选择器弹窗")
        PickerDialogSection()

        SampleSectionHeader("世界时钟卡片")
        WorldClockSection()

        SampleSectionHeader("翻页时钟（锁屏无线充电）")
        FlipClockSection()

        SampleFootnote(
            "这些组件用 Compose Canvas 重写了锤子时钟的自定义 View：" +
                "AnalogClockHandsView（机械表盘）、CompactAlarmClockView（小表盘）、" +
                "SmartisanTimePickerView（三列时间滚轮）、TimerRulerView（横向卡尺）、" +
                "Classic680RulerView（竖向拉环）、AlarmRepeatDaysView（重复日）、" +
                "SmallWorldClockView（世界时钟小表盘）。原版使用 XML + View，本库改为纯 Compose 实现。" +
                "翻页时钟来自锁屏应用 KeyguardSmartisan 的 FlipNumber / WirelessChargingTime" +
                "（无线充电画布上的时钟，素材与 1000ms elastic 翻页曲线都是原版的）。",
        )
    }
}


/** 日期 / 时间选择器弹窗：原版 `SmartisanDatePickerDialog` 家族（含 Ex 版与「日期 + 时间」合体版）。 */
@Composable
private fun PickerDialogSection() {
    var showDate by remember { mutableStateOf(false) }
    var showDateTime by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(Triple(2026, 10, 10)) }
    var dateTime by remember { mutableLongStateOf(1_760_000_000_000L) }
    var time by remember { mutableStateOf(Pair(9, 30)) }

    SmartisanGroup {
        SmartisanListItem(
            title = "日期选择（SmartisanDatePickerDialog）",
            summary = "当前：%d-%02d-%02d".format(date.first, date.second, date.third),
            onClick = { showDate = true },
        )
        SmartisanRowDivider()
        SmartisanListItem(
            title = "日期 + 时间（SmartisanDateTimePickerDialog）",
            summary = "当前时间戳：$dateTime",
            onClick = { showDateTime = true },
        )
        SmartisanRowDivider()
        SmartisanListItem(
            title = "时间选择（SmartisanTimePickerDialog）",
            summary = "当前：%02d:%02d".format(time.first, time.second),
            onClick = { showTime = true },
        )
    }

    if (showDate) {
        SmartisanDatePickerDialog(
            year = date.first,
            month = date.second,
            day = date.third,
            onDateChange = { y, m, d -> date = Triple(y, m, d) },
            onDismissRequest = { showDate = false },
            onConfirm = { y, m, d ->
                date = Triple(y, m, d)
                showDate = false
            },
        )
    }
    if (showDateTime) {
        SmartisanDateTimePickerDialog(
            timeMillis = dateTime,
            onDateTimeChange = { dateTime = it },
            onDismissRequest = { showDateTime = false },
            onConfirm = { dateTime = it },
            title = "设置提醒时间",
        )
    }
    if (showTime) {
        SmartisanTimePickerDialog(
            hour = time.first,
            minute = time.second,
            onTimeChange = { h, m -> time = Pair(h, m) },
            onDismissRequest = { showTime = false },
            onConfirm = { h, m ->
                time = Pair(h, m)
                showTime = false
            },
        )
    }
}
