package top.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.basic.SmartisanPixelText
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.layout.SmartisanGroup

/** 文字页：展示 [SmartisanText] 与 [SmartisanPixelText] 的用法。 */
@Composable
fun TextPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    SamplePageScaffold(title = "文字", onBack = onBack) {
        SampleSectionHeader("文字样式")
        TypographySection()

        SampleSectionHeader("像素字号")
        SmartisanGroup {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                SmartisanPixelText(
                    text = "13.5dp 字号（原版弹窗按钮）",
                    size = 13.5.dp,
                    color = colors.textPrimary,
                )
                SmartisanPixelText(
                    text = "17dp 字号（原版弹窗动作）",
                    size = 17.dp,
                    color = colors.textPrimary,
                )
                SmartisanPixelText(
                    text = "20dp 字号（原版标题栏）",
                    size = 20.dp,
                    color = colors.textPrimary,
                )
            }
        }

        SampleSectionHeader("等宽数字")
        SmartisanGroup {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                SmartisanText(
                    text = "01:23:45.678",
                    style = typography.numeric.copy(fontSize = 24.sp),
                    color = colors.textPrimary,
                )
                SmartisanText(
                    text = "2026-10-08 13:04",
                    style = typography.numeric,
                    color = colors.textSecondary,
                )
            }
        }

        SampleSectionHeader("颜色与截断")
        SmartisanGroup {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                SmartisanText("一级文字 textPrimary", color = colors.textPrimary)
                SmartisanText("二级文字 textSecondary", color = colors.textSecondary)
                SmartisanText("三级文字 textTertiary", color = colors.textTertiary)
                SmartisanText("禁用文字 textDisabled", color = colors.textDisabled)
                SmartisanText("强调色 accent", color = colors.accent)
                SmartisanText("链接色 link", color = colors.link)
                SmartisanText(
                    text = "单行截断：这是一段很长的文字，用来展示 ellipsis 效果，超出的部分会被省略号替换掉。",
                    maxLines = 1,
                    color = colors.textPrimary,
                )
            }
        }

        SampleFootnote(
            "SmartisanText 基于 BasicText，不依赖 Material；" +
                "SmartisanPixelText 还原了原版 XML TextView 把 dp 字号取整到物理像素的行为。",
        )
    }
}
