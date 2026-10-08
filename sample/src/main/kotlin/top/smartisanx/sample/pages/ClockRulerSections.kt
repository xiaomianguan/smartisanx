package top.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.ZoneId
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.clock.SmartisanPullRingRuler
import top.smartisanx.ui.clock.SmartisanRulerPicker
import top.smartisanx.ui.clock.SmartisanWeekdayChips
import top.smartisanx.ui.clock.SmartisanWeekdayPicker
import top.smartisanx.ui.clock.SmartisanWorldClockCard
import top.smartisanx.ui.layout.SmartisanGroup

/** 计时标尺示例：横向卡尺与竖向拉环。 */
@Composable
fun RulerPickerSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var caliperMinutes by remember { mutableIntStateOf(25) }
    var ringMinutes by remember { mutableIntStateOf(10) }
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            SmartisanText(
                text = "横向卡尺（TimerRulerView）",
                modifier = Modifier.padding(start = 18.dp, bottom = 4.dp),
                style = typography.sectionTitle,
                color = colors.textTertiary,
            )
            SmartisanRulerPicker(
                minutes = caliperMinutes,
                onMinutesChange = { caliperMinutes = it },
            )
            SmartisanText(
                text = "拖动阻尼、惯性、整分钟吸附：$caliperMinutes 分钟",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = typography.listItemSecondary,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            SmartisanText(
                text = "竖向拉环（Classic680RulerView）",
                modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 4.dp),
                style = typography.sectionTitle,
                color = colors.textTertiary,
            )
            SmartisanPullRingRuler(
                minutes = ringMinutes,
                onMinutesChange = { ringMinutes = it },
            )
            SmartisanText(
                text = "拉环越界有阻尼，松手按力度回弹：$ringMinutes 分钟",
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                style = typography.listItemSecondary,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 重复日选择示例。 */
@Composable
fun WeekdayPickerSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var days by remember {
        mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY))
    }
    var chipDays by remember { mutableStateOf(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) }
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            SmartisanWeekdayPicker(selectedDays = days, onSelectedDaysChange = { days = it })
            SmartisanText(
                text = "已选：${days.joinToString("、") { it.smartisanLabel() }}",
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            SmartisanText(
                text = "紧凑样式",
                modifier = Modifier.padding(start = 18.dp, top = 20.dp, bottom = 4.dp),
                style = typography.sectionTitle,
                color = colors.textTertiary,
            )
            SmartisanWeekdayChips(selectedDays = chipDays, onSelectedDaysChange = { chipDays = it })
        }
    }
}

/** 世界时钟卡片示例。 */
@Composable
fun WorldClockSection() {
    val zones =
        listOf(
            "北京" to "Asia/Shanghai",
            "伦敦" to "Europe/London",
            "纽约" to "America/New_York",
            "东京" to "Asia/Tokyo",
        )
    SmartisanGroup {
        zones.forEach { (city, zone) ->
            SmartisanWorldClockCard(city = city, zone = ZoneId.of(zone), onClick = {})
        }
    }
}

private fun DayOfWeek.smartisanLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
