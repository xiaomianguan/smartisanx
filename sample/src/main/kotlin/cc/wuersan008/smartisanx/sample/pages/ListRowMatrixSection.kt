package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListBoardSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRowArrow
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRowLines
import cc.wuersan008.smartisanx.ui.layout.SmartisanListSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap

/**
 * framework 列表行矩阵演示。
 *
 * 逐张对应 `framework-smartisanos-res.apk` 的 `res/layout/list_content_*.xml`，
 * 并演示左槽（60dp 图标区里的复选框 / 图标）与右槽（副标题 + 箭头、开关）这两个分支。
 */
@Composable
fun ListRowMatrixSection() {
    var checked by remember { mutableStateOf(true) }
    var switched by remember { mutableStateOf(true) }

    // 四种中间文字版式：字号严格照抄 framework 的 dimens。
    SmartisanGroup {
        SmartisanListRow(
            lines = SmartisanListRowLines.TwoLine,
            title = "两行版 17sp + 13.5sp",
            summary = "list_content_mid_primary_2line",
            onClick = {},
        )
        SmartisanRowDivider()
        SmartisanListRow(
            lines = SmartisanListRowLines.TwoLineAlt,
            title = "两行紧凑版 16sp + 12.5sp",
            summary = "list_content_mid_primary_2line_alt",
            onClick = {},
        )
        SmartisanRowDivider()
        SmartisanListRow(
            lines = SmartisanListRowLines.ThreeLine,
            title = "三行版 17sp + 15sp + 13.5sp",
            summary = "list_content_mid_primary_3line 的第二行",
            tertiary = "第三行用的是 tertiary_text_size",
            onClick = {},
        )
        SmartisanRowDivider()
        SmartisanListRow(
            lines = SmartisanListRowLines.ThreeLineAlt,
            title = "三行紧凑版 16sp + 13.5sp + 12sp",
            summary = "list_content_mid_primary_3line_alt",
            tertiary = "第三行用的是 quaternary_text_size",
            onClick = {},
        )
    }

    SmartisanListVerticalGap()

    // 左槽：原版是 60dp × 60dp 的方形区（left_icon_area_width），内容居中且不超过 36dp。
    SmartisanGroup {
        SmartisanListRow(
            title = "左槽放复选框",
            summary = "list_content_left_checkbox",
            leading = { SmartisanCheckbox(checked = checked, onCheckedChange = { checked = it }) },
            onClick = { checked = !checked },
        )
        SmartisanRowDivider(startIndent = 60.dp)
        SmartisanListRow(
            title = "左槽放图标",
            summary = "list_content_left_image_view",
            leading = {
                SmartisanIcon(
                    res = SmartisanOriginalIcons.TabFolder,
                    contentDescription = null,
                    size = 28.dp,
                )
            },
            onClick = {},
        )
    }

    SmartisanListVerticalGap()

    // 右槽：list_content_right_subtitle_arrow（副标题 + 箭头）与 list_content_right_switch（开关）。
    SmartisanGroup {
        SmartisanListRow(
            title = "右槽副标题 + 箭头",
            summary = "list_content_right_subtitle_arrow",
            trailing = { SmartisanListRowArrow(subtitle = "已开启") },
            onClick = {},
        )
        SmartisanRowDivider()
        SmartisanListRow(
            title = "右槽开关",
            summary = "list_content_right_switch",
            trailing = {
                SmartisanSwitch(checked = switched, onCheckedChange = { switched = it })
            },
            onClick = { switched = !switched },
        )
        SmartisanRowDivider()
        SmartisanListRow(
            title = "禁用态",
            summary = "enabled = false，文字与右侧小部件同时变灰",
            trailing = { SmartisanListRowArrow(subtitle = "不可用") },
            enabled = false,
            onClick = {},
        )
        SmartisanRowDivider()
        SmartisanListRow(
            title = "多选选中态",
            summary = "activated 换成主题的浅蓝底",
            selected = true,
            onClick = {},
        )
    }

    // 分组标题的两种版式。
    SmartisanListSectionTitle("list_section_title_layout")
    SmartisanGroup {
        SmartisanListRow(title = "30dp 高、13.5sp 加粗、左缩进 12dp", onClick = {})
    }

    SmartisanListVerticalGap()

    SmartisanListBoardSectionTitle(
        text = "list_board_section_title_layout（可点）",
        onClick = {},
    )
    SmartisanGroup {
        SmartisanListRow(
            title = "板块标题带原版底图与右侧箭头",
            summary = "list_board_section_bg + list_board_section_title_divider",
            onClick = {},
        )
    }

    SampleFootnote(
        "列表行矩阵照抄 framework 的 list_content_item_layout：左容器 60dp（left_icon_area_width）、" +
            "中容器四套文字版式、右容器按 right_container_margin(6dp) 留边。" +
            "副标题 + 箭头来自 list_content_right_subtitle_arrow，" +
            "板块标题来自 list_board_section_title_layout。",
    )
}
