package top.smartisanx.sample.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.clock.SmartisanAnalogClock
import top.smartisanx.ui.clock.SmartisanCompactClock
import top.smartisanx.ui.layout.SmartisanGroup

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

        SampleSectionHeader("世界时钟卡片")
        WorldClockSection()

        SampleFootnote(
            "这些组件用 Compose Canvas 重写了锤子时钟的自定义 View：" +
                "AnalogClockHandsView（机械表盘）、CompactAlarmClockView（小表盘）、" +
                "SmartisanTimePickerView（三列时间滚轮）、TimerRulerView（横向卡尺）、" +
                "Classic680RulerView（竖向拉环）、AlarmRepeatDaysView（重复日）、" +
                "SmallWorldClockView（世界时钟小表盘）。原版使用 XML + View，本库改为纯 Compose 实现。",
        )
    }
}
