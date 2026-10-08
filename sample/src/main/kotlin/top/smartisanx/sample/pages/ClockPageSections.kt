package top.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.clock.SmartisanTimePicker
import top.smartisanx.ui.clock.SmartisanWheelPicker
import top.smartisanx.ui.layout.SmartisanGroup

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
