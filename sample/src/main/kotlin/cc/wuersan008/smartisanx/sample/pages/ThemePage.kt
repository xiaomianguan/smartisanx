package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.runtime.Composable
import cc.wuersan008.smartisanx.core.theme.SmartisanColorSchemeMode
import cc.wuersan008.smartisanx.core.theme.ThemeController
import cc.wuersan008.smartisanx.sample.ExperimentalNote
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanRadioRow

/** 主题页：展示色板、文字样式、形状，并提供深浅色切换。 */
@Composable
fun ThemePage(controller: ThemeController, onBack: () -> Unit) {
    SamplePageScaffold(title = "主题与设计变量", onBack = onBack) {
        SampleSectionHeader("深浅色模式")
        SmartisanGroup {
            SmartisanRadioRow(
                text = "浅色（默认）",
                summary = "原版 Smartisan OS 的原始设计，也是本库默认值",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.Light,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.Light },
            )
            SmartisanRadioRow(
                text = "跟随系统（实验性）",
                summary = "系统处于深色时会切到实验性的深色方案",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.System,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.System },
            )
            SmartisanRadioRow(
                text = "深色（实验性）",
                summary = "原版没有，由复刻项目新增",
                selected = controller.colorSchemeMode == SmartisanColorSchemeMode.Dark,
                onClick = { controller.colorSchemeMode = SmartisanColorSchemeMode.Dark },
            )
        }
        ExperimentalNote(
            title = "深色模式是实验性功能，默认不启用",
            body = "本库默认使用浅色，与原版 Smartisan OS 一致；" +
                "「跟随系统」与「深色」需要由开发者显式选择，并由你自己在产品里向用户说明。" +
                "原因是原版只有浅色一套设计，深色由三个复刻项目自行新增，" +
                "而且原版图形资源里只有约 2.5% 带夜间变体（7919 个 drawable 里 195 个），" +
                "颜色状态列表则完全没有夜间版本，深色下的质感会与原版浅色不一致。",
        )

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
