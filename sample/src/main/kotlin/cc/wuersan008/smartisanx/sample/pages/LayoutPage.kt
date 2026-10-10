package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.anim.SmartisanModalPageTransition
import cc.wuersan008.smartisanx.ui.anim.SmartisanPageTransition
import cc.wuersan008.smartisanx.ui.basic.SmartisanDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIntrinsicImage
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanSurface
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupDivider
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarShadow
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarSurface

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
        // 两张卡片直接相邻：按原版插一条 14dp 的分组间距。
        SmartisanListVerticalGap()
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

        SampleSectionHeader("基础容器与投影")
        ContainerSection()

        SampleSectionHeader("页面转场")
        TransitionSection()

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

/** 基础容器与投影：SmartisanSurface、SmartisanGroupItem、IntrinsicImage、TitleBarShadow。 */
@Composable
private fun ContainerSection() {
    var page by remember { mutableStateOf(0) }
    SmartisanGroup {
        // SmartisanGroupItem：按位置自动取原版分组底图。
        SmartisanGroupItem(position = SmartisanGroupRowPosition.Top, title = "GroupItem 顶部")
        SmartisanGroupItem(position = SmartisanGroupRowPosition.Middle, title = "GroupItem 中间", summary = "底图会拼成一体")
        SmartisanGroupItem(position = SmartisanGroupRowPosition.Bottom, title = "GroupItem 底部", showDivider = false)
    }
    // SmartisanSurface：带形状 / 颜色 / 内容色 / 边框的容器。
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmartisanSurface(
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, LocalSmartisanColors.current.divider),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SmartisanText("SmartisanSurface", style = LocalSmartisanTypography.current.caption)
            }
        }
        // SmartisanIntrinsicImage：按位图固有尺寸绘制（原版 ImageView 的 wrap_content）。
        SmartisanIntrinsicImage(
            res = SmartisanDrawables.IconBack,
            contentDescription = null,
        )
    }
    // SmartisanTitleBarSurface：标题栏 + 状态栏留白 + 投影的一体容器（原版标题栏外层）。
    SmartisanTitleBarSurface(modifier = Modifier.fillMaxWidth(), includeStatusBar = false) {
        SmartisanTitleBar(
            title = "TitleBarSurface",
            modifier = Modifier.fillMaxWidth(),
            includeStatusBar = false,
            showShadow = false,
        )
    }
    // SmartisanGroupDivider：按原版分组分隔线（可指定左缩进）。
    SmartisanGroupDivider()
    SmartisanText(
        text = "SmartisanGroupDivider（分组分隔线）",
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        style = LocalSmartisanTypography.current.caption,
        color = LocalSmartisanColors.current.textTertiary,
    )
    // SmartisanTitleBarShadow：标题栏下方那条原版投影。
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        SmartisanTitleBarShadow(modifier = Modifier.fillMaxWidth())
        SmartisanText(
            text = "SmartisanTitleBarShadow（title_bar_shadow）",
            modifier = Modifier.padding(vertical = 6.dp),
            style = LocalSmartisanTypography.current.caption,
            color = LocalSmartisanColors.current.textTertiary,
        )
    }
}

/** 页面转场：原版锤子音乐的 PageStackTransition 与弹窗式页面进出场。 */
@Composable
private fun TransitionSection() {
    var secondary by remember { mutableStateOf(false) }
    var modalVisible by remember { mutableStateOf(false) }
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp)) {
            SmartisanPageTransition(
                secondary = secondary,
                modifier = Modifier.fillMaxWidth().height(96.dp),
                primary = {
                    Box(Modifier.fillMaxSize().background(LocalSmartisanColors.current.surface), contentAlignment = Alignment.Center) {
                        SmartisanText("主页面")
                    }
                },
                secondaryContent = {
                    Box(Modifier.fillMaxSize().background(LocalSmartisanColors.current.surfaceRaised), contentAlignment = Alignment.Center) {
                        SmartisanText("副页面（从右侧滑入）")
                    }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartisanButton(
                    text = if (secondary) "返回主页" else "进入副页",
                    style = SmartisanButtonStyle.Neutral,
                    onClick = { secondary = !secondary },
                )
                SmartisanButton(
                    text = "弹窗式页面",
                    style = SmartisanButtonStyle.Neutral,
                    onClick = { modalVisible = !modalVisible },
                )
            }
        }
    }
    SmartisanModalPageTransition(
        visible = modalVisible,
        // 转场按「容器高度」算位移，所以要给定高容器（整页或像这里的固定高页面）。
        modifier = Modifier.fillMaxWidth().height(320.dp),
    ) {
        SmartisanSurface(
            modifier = Modifier.padding(24.dp).padding(top = 96.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp)) {
                SmartisanText("SmartisanModalPageTransition", style = LocalSmartisanTypography.current.listItemPrimary)
                SmartisanText(
                    text = "缩放 + 淡入淡出，对应原版弹窗式页面的进出场。",
                    modifier = Modifier.padding(top = 8.dp),
                    style = LocalSmartisanTypography.current.caption,
                    color = LocalSmartisanColors.current.textTertiary,
                )
                SmartisanButton(
                    text = "关闭",
                    modifier = Modifier.padding(top = 12.dp),
                    style = SmartisanButtonStyle.Neutral,
                    onClick = { modalVisible = false },
                )
            }
        }
    }
}

