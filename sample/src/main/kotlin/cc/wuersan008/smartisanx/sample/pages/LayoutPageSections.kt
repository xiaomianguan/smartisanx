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
import cc.wuersan008.smartisanx.icons.SmartisanXClockIcons
import cc.wuersan008.smartisanx.icons.SmartisanXMediaIcons
import cc.wuersan008.smartisanx.icons.SmartisanXStatusIcons
import cc.wuersan008.smartisanx.icons.SmartisanXIcons
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
    SmartisanCard {
        Column(Modifier.padding(16.dp)) {
            SmartisanText("SmartisanCard", style = typography.listItemPrimary, color = colors.textPrimary)
            SmartisanText(
                text = "带圆角与描边的卡片容器，适合天气、专辑这类块状内容。",
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
    val barItems =
        listOf(
            SmartisanBottomBarItem(SmartisanXMediaIcons.Queue, "音乐"),
            SmartisanBottomBarItem(SmartisanXStatusIcons.Sun, "天气"),
            SmartisanBottomBarItem(SmartisanXClockIcons.Clock, "时钟"),
            SmartisanBottomBarItem(SmartisanXIcons.Settings, "设置"),
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
        SmartisanEmptyHint(
            title = "这里还没有内容",
            description = "空列表提示来自锤子音乐资料库与锤子天气城市列表。",
            icon = SmartisanXStatusIcons.Folder,
        )
    }
}
