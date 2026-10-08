package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanCard
import cc.wuersan008.smartisanx.ui.layout.SmartisanEmptyHint
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanTabRow
import cc.wuersan008.smartisanx.ui.layout.smartisanVerticalScrollbar

/** 卡片容器示例。 */
@Composable
fun CardSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    // 原版卡片：内容底图 + 向外扩张的阴影 9-patch 两层。
    SmartisanCard(
        backgroundRes = SmartisanDrawables.GroupRowSingle,
        shadowRes = SmartisanDrawables.GroupRowSingleShadow,
    ) {
        Column(Modifier.padding(16.dp)) {
            SmartisanText(
                text = "SmartisanCard（原版底图 + 投影）",
                style = typography.listItemPrimary,
                color = colors.textPrimary,
            )
            SmartisanText(
                text = "内容底图是 group_list_item_bg_single，投影是 list_content_item_single_shadow；" +
                    "投影按 9-patch 的 padding 向外扩张，画在卡片边界之外。",
                modifier = Modifier.padding(top = 4.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
        }
    }

    // 纯 Compose 卡片（不用原版素材时的退路）。
    SmartisanCard {
        Column(Modifier.padding(16.dp)) {
            SmartisanText("SmartisanCard（纯色退路）", style = typography.listItemPrimary, color = colors.textPrimary)
            SmartisanText(
                text = "不传 backgroundRes 时使用主题的 surface 纯色与圆角。",
                modifier = Modifier.padding(top = 4.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
        }
    }
}

/** 标签栏与底部栏示例。 */
@Composable
fun TabSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val tabs = listOf("歌曲", "专辑", "艺术家", "文件夹")
    var tab by remember { mutableIntStateOf(0) }
    var barIndex by remember { mutableIntStateOf(0) }
    // 使用原版底部标签栏图标：tabbar_*_selector 自带选中态，不需要 tint。
    val barItems =
        listOf(
            SmartisanBottomBarItem(label = "歌曲", iconRes = SmartisanOriginalIcons.TabSong),
            SmartisanBottomBarItem(label = "文件夹", iconRes = SmartisanOriginalIcons.TabFolder),
            SmartisanBottomBarItem(label = "收藏", iconRes = SmartisanOriginalIcons.TabFavorite),
            SmartisanBottomBarItem(label = "更多", iconRes = SmartisanOriginalIcons.TabMore),
        )
    SmartisanGroup {
        Column {
            SmartisanTabRow(
                tabs = tabs,
                selectedIndex = tab,
                onSelected = { tab = it },
            )
            SmartisanText(
                text = "当前选中：${tabs[tab]}",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
        }
    }
    SmartisanBottomBar(
        items = barItems,
        selectedIndex = barIndex,
        onSelected = { barIndex = it },
        includeNavigationBar = false,
    )
}

/** 滚动条示例：一个固定高度的可滚动区域。 */
@Composable
fun ScrollbarSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val scrollState = rememberScrollState()
    SmartisanGroup {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(colors.surface),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .verticalScroll(scrollState)
                    .smartisanVerticalScrollbar(scrollState)
                    .padding(horizontal = 18.dp),
            ) {
                repeat(20) { index ->
                    SmartisanText(
                        text = "第 ${index + 1} 行内容，用来触发滚动条绘制",
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = typography.listItemPrimary,
                        color = colors.textPrimary,
                    )
                }
            }
        }
    }
}

/** 空态示例。 */
@Composable
fun EmptyHintSection() {
    SmartisanGroup {
        // 空态插图用原版 blank_folder，与锤子音乐资料库空态一致。
        SmartisanEmptyHint(
            title = "这里还没有内容",
            description = "空列表提示来自锤子音乐资料库与锤子天气城市列表。",
            iconRes = SmartisanOriginalIcons.EmptyFolder,
        )
    }
}
