package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlinx.coroutines.delay
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.clock.SmartisanFlipCard
import cc.wuersan008.smartisanx.ui.clock.SmartisanFlipClock
import cc.wuersan008.smartisanx.ui.clock.SmartisanTimePicker
import cc.wuersan008.smartisanx.ui.clock.SmartisanWheelPicker
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/** 三列时间滚轮示例。 */
@Composable
fun TimePickerSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(30) }
    SmartisanGroup {
        Column(
            modifier = Modifier.fillMaxWidth().background(colors.surface).padding(vertical = 12.dp),
        ) {
            SmartisanTimePicker(
                hour = hour,
                minute = minute,
                onTimeChange = { h, m ->
                    hour = h
                    minute = m
                },
            )
            SmartisanText(
                text = "当前时间：${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                style = typography.numeric,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 通用滚轮示例。 */
@Composable
fun WheelPickerSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val cities = listOf("北京", "上海", "广州", "深圳", "成都", "杭州", "西安")
    var cityIndex by remember { mutableIntStateOf(0) }
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            SmartisanWheelPicker(
                items = cities,
                selectedIndex = cityIndex,
                onSelectedIndexChange = { cityIndex = it },
            )
            SmartisanText(
                text = "当前城市：${cities[cityIndex]}",
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}


/**
 * 锁屏翻页时钟示例。
 *
 * 原版这张时钟是无线充电画布上的（横屏 2242×1080px、`clock_theme_style = 0` 时的默认样式），
 * 素材按 1080p 固定 px，所以在竖屏示例里显式按 [digit] 等比缩小。
 */
@Composable
fun FlipClockSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val digit = 64.dp
    var now by remember { mutableStateOf(LocalTime.now()) }
    var demo by remember { mutableIntStateOf(7) }

    // 真实时间：每分钟整点会翻页。
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalTime.now()
        }
    }
    // 单张卡片的翻页演示：每 2 秒翻一次，看得清楚一点。
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            demo = if (demo >= 60) 0 else demo + 1
        }
    }

    SmartisanGroup {
        Column(
            modifier = Modifier.fillMaxWidth().background(colors.surface).padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SmartisanFlipClock(hour = now.hour, minute = now.minute, digitWidth = digit)
            SmartisanText(
                text = "24 小时制（每整分翻页）",
                modifier = Modifier.padding(top = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(20.dp))
            SmartisanFlipClock(hour = now.hour, minute = now.minute, use24Hour = false, digitWidth = digit)
            SmartisanText(
                text = "12 小时制，右侧橙色 AM / PM",
                modifier = Modifier.padding(top = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(20.dp))
            SmartisanFlipCard(value = demo, digitWidth = digit)
            SmartisanText(
                text = "单卡翻页演示：$demo",
                modifier = Modifier.padding(top = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
        }
    }
}
