package top.smartisanx.sample.pages

import androidx.compose.runtime.Composable
import top.smartisanx.core.theme.SmartisanColorSchemeMode
import top.smartisanx.core.theme.ThemeController
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.layout.SmartisanGroup
import top.smartisanx.ui.control.SmartisanRadioRow

/** 主题页：展示色板、文字样式、形状，并提供深浅色切换。 */
@Composable
fun ThemePage(controller: ThemeController, onBack: () -> Unit) {
    SamplePageScaffold(title = "主题与设计变量", onBack = onBack) {
        SampleSectionHeader("深浅色模式")
        SmartisanGroup {
            SmartisanRadioRow(
                text = "跟随系统",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.System,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.System },
            )
            SmartisanRadioRow(
                text = "浅色",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.Light,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.Light },
            )
            SmartisanRadioRow(
                text = "深色",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.Dark,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.Dark },
            )
        }

        SampleSectionHeader("语义色板")
        ColorSwatchSection()

        SampleSectionHeader("文字样式")
        TypographySection()

        SampleSectionHeader("形状")
        ShapeSection()

        SampleSectionHeader("尺寸常量")
        DimensSection()

        SampleFootnote(
            "色板由锤子音乐、锤子天气、锤子时钟三个项目的语义色合并而成；" +
                "深色基线取自锤子天气复刻已经校准过的炭灰色板（页底 #25282D、标题栏 #292C31、卡片 #34373C）。",
        )
    }
}
