package top.smartisanx.sample.pages

import androidx.compose.foundation.layout.Arrangement
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
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.control.SmartisanButton
import top.smartisanx.ui.control.SmartisanButtonStyle
import top.smartisanx.ui.control.SmartisanTextButton
import top.smartisanx.ui.layout.SmartisanGroup

/** 按钮页：强调、中性、文字三种按钮。 */
@Composable
fun ButtonPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var clicks by remember { mutableStateOf(0) }

    SamplePageScaffold(title = "按钮", onBack = onBack) {
        SampleSectionHeader("三种样式")
        SmartisanGroup {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartisanButton(
                    text = "强调按钮（原版红色收缩按钮）",
                    onClick = { clicks++ },
                    modifier = Modifier.fillMaxWidth(),
                )
                SmartisanButton(
                    text = "中性按钮",
                    onClick = { clicks++ },
                    modifier = Modifier.fillMaxWidth(),
                    style = SmartisanButtonStyle.Neutral,
                )
                SmartisanTextButton(text = "文字按钮", onClick = { clicks++ })
            }
        }

        SampleSectionHeader("状态")
        SmartisanGroup {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartisanButton(
                    text = "禁用按钮",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )
                SmartisanButton(
                    text = "加载中",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    loading = true,
                )
            }
        }

        SampleSectionHeader("交互")
        SmartisanGroup {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                SmartisanText(
                    text = "已点击 $clicks 次（按下时会播放系统点击音效）",
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
        }

        SampleFootnote(
            "按钮合并了锤子音乐的红色收缩长按钮（shrink_long_btn_red_selector）与" +
                "锤子天气的操作按钮（WeatherButton）；按压时切换按压态底色与文字色，不使用涟漪。",
        )
    }
}
