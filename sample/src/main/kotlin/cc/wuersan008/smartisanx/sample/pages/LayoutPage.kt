package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.icons.SmartisanXIcons
import cc.wuersan008.smartisanx.icons.SmartisanXStatusIcons
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction

/** 布局页：标题栏、列表行、分组、标签栏、滚动条、空态。 */
@Composable
fun LayoutPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current

    SamplePageScaffold(title = "布局与列表", onBack = onBack) {
        SampleSectionHeader("标题栏")
        SmartisanGroup {
            Column {
                SmartisanTitleBar(
                    title = "仅标题",
                    modifier = Modifier.fillMaxWidth(),
                    includeStatusBar = false,
                    showShadow = false,
                )
                SmartisanDivider()
                SmartisanTitleBar(
                    title = "带返回与动作",
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = SmartisanTitleBarAction(SmartisanXIcons.Back, "返回", onClick = {}),
                    actions =
                        listOf(
                            SmartisanTitleBarAction(SmartisanXIcons.Search, "搜索", onClick = {}),
                            SmartisanTitleBarAction(SmartisanXIcons.More, "更多", onClick = {}),
                        ),
                    includeStatusBar = false,
                    showShadow = false,
                )
                SmartisanDivider()
                SmartisanTitleBar(
                    title = "禁用动作",
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = SmartisanTitleBarAction(SmartisanXIcons.Back, "返回", onClick = {}),
                    action =
                        SmartisanTitleBarAction(
                            icon = SmartisanXIcons.Add,
                            contentDescription = "新增",
                            onClick = {},
                            enabled = false,
                        ),
                    includeStatusBar = false,
                    showShadow = false,
                )
            }
        }

        SampleSectionHeader("列表行")
        SmartisanGroup {
            SmartisanListItem(title = "只有标题")
            SmartisanRowDivider()
            SmartisanListItem(title = "标题与说明", summary = "二级说明文字 12.5sp")
            SmartisanRowDivider()
            SmartisanListItem(
                title = "带前置图标",
                summary = "来自锤子音乐的资料库行",
                leading = {
                    SmartisanIcon(
                        imageVector = SmartisanXStatusIcons.Folder,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        size = 24.dp,
                    )
                },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "带后置箭头",
                trailing = {
                    SmartisanIcon(
                        imageVector = SmartisanXIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.textDisabled,
                        size = 18.dp,
                    )
                },
                onClick = {},
            )
            SmartisanRowDivider()
            SmartisanListItem(title = "选中态（蓝色多选底色）", selected = true)
            SmartisanRowDivider()
            SmartisanListItem(title = "禁用态", summary = "不可点击", enabled = false)
        }

        SampleSectionHeader("分组与卡片")
        SmartisanGroup {
            SmartisanListItem(title = "分组内的行", summary = "SmartisanGroup 提供卡片底色与左右外边距")
            SmartisanRowDivider()
            SmartisanListItem(title = "分组内的第二行")
        }
        CardSection()

        SampleSectionHeader("标签栏与底部栏")
        TabSection()

        SampleSectionHeader("滚动条")
        ScrollbarSection()

        SampleSectionHeader("空态")
        EmptyHintSection()

        SampleFootnote(
            "标题栏合并了锤子音乐的 SmartisanTitleBar 与锤子天气的 WeatherTitleBar；" +
                "列表行合并了锤子音乐的资料库行与锤子天气的城市行；" +
                "滚动条来自锤子音乐资料库右侧的细滚动条。",
        )
    }
}
