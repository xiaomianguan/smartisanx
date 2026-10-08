package top.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.icons.SmartisanXIcons
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.basic.SmartisanIcon
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.layout.SmartisanGroup
import top.smartisanx.ui.layout.SmartisanListItem
import top.smartisanx.ui.list.SmartisanLetterIndexBar
import top.smartisanx.ui.list.SmartisanReorderableColumn
import top.smartisanx.ui.list.SmartisanSwipeToDelete
import top.smartisanx.ui.list.smartisanDefaultLetterIndex

/** 列表交互页：拖动排序、侧滑删除、字母索引。 */
@Composable
fun ListInteractionPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "列表交互", onBack = onBack) {
        SampleSectionHeader("长按拖动排序")
        ReorderSection()

        SampleSectionHeader("侧滑删除")
        SwipeDeleteSection()

        SampleSectionHeader("A–Z 字母索引")
        LetterIndexSection()

        SampleFootnote(
            "拖动排序合并了锤子音乐队列拖拽与锤子时钟世界时钟列表的让位逻辑；" +
                "侧滑删除用 Compose 重写了锤子时钟的 SmartisanSwipeDeleteRow（前 65dp 直接位移，之后 1/5 阻尼）；" +
                "字母索引合并了锤子音乐的字母快捷栏与锤子时钟的 QuickBarEx。",
        )
    }
}

@Composable
private fun ReorderSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val items = remember { mutableStateListOf("世界时钟 · 北京", "世界时钟 · 伦敦", "世界时钟 · 纽约", "世界时钟 · 东京") }
    SmartisanGroup {
        SmartisanReorderableColumn(
            items = items,
            onMove = { from, to ->
                val item = items.removeAt(from)
                items.add(to, item)
            },
        ) { _, item, dragging ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (dragging) colors.surfaceRaised else colors.surface)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                SmartisanText(
                    text = item,
                    style = typography.listItemPrimary,
                    color = colors.textPrimary,
                )
                SmartisanText(
                    text = if (dragging) "拖动中" else "长按后拖动",
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun SwipeDeleteSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val alarms = remember { mutableStateListOf("07:30 工作日", "09:00 周末", "13:00 午休") }
    SmartisanGroup {
        alarms.forEachIndexed { index, alarm ->
            SmartisanSwipeToDelete(onDelete = { alarms.removeAt(index) }) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.surface)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    SmartisanText(text = alarm, style = typography.listItemPrimary, color = colors.textPrimary)
                }
            }
        }
        if (alarms.isEmpty()) {
            SmartisanListItem(title = "已全部删除", summary = "重新进入页面即可恢复")
        }
    }
}

@Composable
private fun LetterIndexSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val letters = remember { smartisanDefaultLetterIndex() }
    var active by remember { mutableStateOf<Char?>(null) }
    SmartisanGroup {
        Row(
            modifier = Modifier.fillMaxWidth().height(220.dp).background(colors.surface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(horizontal = 18.dp)) {
                SmartisanText(
                    text = "当前字母：${active ?: "未选择"}",
                    style = typography.listItemPrimary,
                    color = colors.textPrimary,
                )
                SmartisanText(
                    text = "按住右侧字母栏上下拖动可以连续选择。",
                    modifier = Modifier.padding(top = 4.dp),
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
            SmartisanLetterIndexBar(
                letters = letters,
                onLetterSelected = { active = it },
                activeLetter = active,
            )
        }
    }
}

@Suppress("unused")
private val unusedIconSize: Modifier = Modifier.size(0.dp).width(0.dp)

@Suppress("unused")
private val unusedArrangement = Arrangement.Start

@Suppress("unused")
private val unusedInt = 0
