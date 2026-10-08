package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.control.SmartisanRatingBar
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider

/** 基础控件页：开关、复选框、按钮、评分条。 */
@Composable
fun ControlPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var switchOn by remember { mutableStateOf(true) }
    var switchOff by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var rating by remember { mutableIntStateOf(3) }

    SamplePageScaffold(title = "基础控件", onBack = onBack) {
        SampleSectionHeader("开关")
        SmartisanGroup {
            SmartisanSwitchRow(
                text = "智能音效",
                summary = "整行可点，行内点击只触发一次回调",
                checked = switchOn,
                onCheckedChange = { switchOn = it },
            )
            SmartisanRowDivider()
            SmartisanSwitchRow(
                text = "睡眠定时",
                checked = switchOff,
                onCheckedChange = { switchOff = it },
            )
            SmartisanRowDivider()
            SmartisanSwitchRow(
                text = "禁用示例",
                checked = true,
                enabled = false,
                onCheckedChange = {},
            )
        }
        SmartisanGroup {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanSwitch(checked = switchOn, onCheckedChange = { switchOn = it })
                SmartisanSwitch(checked = switchOff, onCheckedChange = { switchOff = it })
                SmartisanSwitch(checked = true, enabled = false, onCheckedChange = {})
                SmartisanText("可拖动滑块", style = typography.caption, color = colors.textTertiary)
            }
        }

        SampleSectionHeader("复选框")
        SmartisanGroup {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanCheckbox(checked = checked, onCheckedChange = { checked = it })
                SmartisanCheckbox(checked = false, onCheckedChange = {})
                SmartisanCheckbox(checked = true, enabled = false, onCheckedChange = {})
                SmartisanText("选中 / 未选中 / 禁用", style = typography.caption, color = colors.textTertiary)
            }
        }

        SampleSectionHeader("评分条")
        SmartisanGroup {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                SmartisanRatingBar(rating = rating, onRatingChange = { rating = it })
                SmartisanText(
                    text = "当前评分：$rating 星（按住可以连续拖动选分）",
                    modifier = Modifier.padding(top = 8.dp),
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
        }

        SampleFootnote(
            "开关合并了锤子音乐的 Compose 开关与锤子时钟的两套自定义 View 开关；" +
                "复选框与评分条来自锤子音乐；按钮合并了锤子音乐的红色收缩按钮与锤子天气的操作按钮。",
        )
    }
}
