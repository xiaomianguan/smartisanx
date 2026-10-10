package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction

/** 布局页：标题栏、列表行、分组、标签栏、滚动条、空态。 */
@Composable
fun LayoutPage(onBack: () -> Unit) {
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
                    navigationIcon = SmartisanTitleBarAction(SmartisanDrawables.IconBack, "返回", onClick = {}),
                    actions =
                        listOf(
                            SmartisanTitleBarAction(SmartisanDrawables.IconSearch, "搜索", onClick = {}),
                            SmartisanTitleBarAction(SmartisanOriginalIcons.More, "更多", onClick = {}),
                        ),
                    includeStatusBar = false,
                    showShadow = false,
                )
                SmartisanDivider()
                SmartisanTitleBar(
                    title = "禁用动作",
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = SmartisanTitleBarAction(SmartisanDrawables.IconBack, "返回", onClick = {}),
                    action =
                        SmartisanTitleBarAction(
                            iconRes = SmartisanDrawables.IconAdd,
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
                    // 原版文件夹图标（tabbar_folder_selector），不再用自绘矢量图标。
                    SmartisanIcon(
                        res = SmartisanOriginalIcons.TabFolder,
                        contentDescription = null,
                        size = 24.dp,
                    )
                },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "带后置箭头",
                trailing = {
                    // 原版列表箭头（selector_list_content_item_arrow）。
                    SmartisanIcon(
                        res = SmartisanDrawables.ListItemArrow,
                        contentDescription = null,
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

        SampleSectionHeader("分组卡片（原版底图）")
        // SmartisanGroupItem 会按位置挑原版分组底图：top / middle / bottom / single，
        // 每张都自带圆角、描边与按压态。
        SmartisanGroup {
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Top,
                title = "第一行 group_list_item_bg_top",
                summary = "顶部行有上圆角",
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Middle,
                title = "中间行 group_list_item_bg_mid",
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Bottom,
                title = "最后一行 group_list_item_bg_bottom",
                summary = "底部行有下圆角",
                onClick = {},
            )
        }
        SmartisanGroup {
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Single,
                title = "单独一行 group_list_item_bg_single",
                summary = "单行分组带完整描边",
                onClick = {},
            )
        }

        SampleSectionHeader("framework 列表行矩阵")
        // 对应 framework 的 list_content_item_layout 家族，见 ListRowMatrixSection.kt。
        ListRowMatrixSection()

        SampleSectionHeader("卡片")
        CardSection()

        SampleSectionHeader("标签栏与底部栏")
        TabSection()

        SampleSectionHeader("滚动条")
        ScrollbarSection()

        SampleSectionHeader("空态")
        EmptyHintSection()

        SampleSectionHeader("framework 复合标题栏")
        // 对应 framework 的 combo_title_layout / primary_title_layout，见 ComboTitleBarSection.kt。
        ComboTitleBarSection()

        // 原版 APK 里直接移植过来的布局，单独成文件，见 OriginalLayoutSections.kt。
        OriginalLayoutSections()

        SampleFootnote(
            "标题栏合并了锤子音乐的 SmartisanTitleBar 与锤子天气的 WeatherTitleBar；" +
                "列表行合并了锤子音乐的资料库行与锤子天气的城市行；" +
                "滚动条来自锤子音乐资料库右侧的细滚动条；" +
                "流式布局来自原版 smartisanos.widget.letters.SurnameFlowLayout。",
        )
    }
}
