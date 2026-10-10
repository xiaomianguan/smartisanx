package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.LocalSampleFeedback
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.list.SmartisanHiddenRowAction
import cc.wuersan008.smartisanx.ui.list.SmartisanHiddenRowActions
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterIndexBar
import cc.wuersan008.smartisanx.ui.list.SmartisanReorderableColumn
import cc.wuersan008.smartisanx.ui.list.SmartisanSwipeToDelete
import cc.wuersan008.smartisanx.ui.list.smartisanDefaultLetterIndex

/** 列表交互页：拖动排序、侧滑删除、字母索引。 */
@Composable
fun ListInteractionPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "列表交互", onBack = onBack) {
        SampleSectionHeader("长按拖动排序")
        ReorderSection()

        SampleSectionHeader("侧滑删除")
        SwipeDeleteSection()

        SampleSectionHeader("行内隐藏操作")
        HiddenActionsSection()

        SampleSectionHeader("A–Z 字母索引")
        LetterIndexSection()

        SampleFootnote(
            "拖动排序合并了锤子音乐队列拖拽与锤子时钟世界时钟列表的让位逻辑；" +
                "侧滑删除用 Compose 重写了锤子时钟的 SmartisanSwipeDeleteRow（前 65dp 直接位移，之后 1/5 阻尼）；" +
                "行内隐藏操作来自 framework 的 HiddenListActionLayout（左右 12dp 内边距、图标间距 6dp）；" +
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
    val feedback = LocalSampleFeedback.current
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
            SmartisanListItem(
                title = "已全部删除",
                summary = "重新进入页面即可恢复",
                onClick = { feedback("已全部删除 · 纯展示行") },
            )
        }
    }
}

@Composable
private fun HiddenActionsSection() {
    val feedback = LocalSampleFeedback.current
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val mails = remember { mutableStateListOf("邮件 · 项目周报", "邮件 · 账单提醒", "邮件 · 设计稿评审") }
    SmartisanGroup {
        mails.forEachIndexed { index, mail ->
            SwipeToRevealActions(
                actions = {
                    SmartisanHiddenRowActions(
                        actions =
                            listOf(
                                SmartisanHiddenRowAction(SmartisanOriginalIcons.Delete, "删除") {
                                    mails.removeAt(index)
                                },
                                SmartisanHiddenRowAction(
                                    iconRes = SmartisanOriginalIcons.More,
                                    contentDescription = "更多",
                                    // 禁用态走 selector 的禁用位图，和原版 setActionEnabled 一致。
                                    enabled = false,
                                ),
                            ),
                        iconSize = 24.dp,
                    )
                },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(colors.surface)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    SmartisanText(text = mail, style = typography.listItemPrimary, color = colors.textPrimary)
                }
            }
        }
        if (mails.isEmpty()) {
            SmartisanListItem(
                title = "已全部删除",
                summary = "重新进入页面即可恢复",
                onClick = { feedback("已全部删除 · 纯展示行") },
            )
        }
    }
}

/**
 * 演示用的「左滑露出」容器。
 *
 * 原版 framework 只给了 `HiddenListActionLayout` 这一排操作本身，露出容器是各个 App 自己写的
 * （dump 里没有任何布局引用它），所以这里用一段最简手势把用法演示出来；
 * 真要照抄原版那套滑动物理（65dp 直接位移 + 1/5 阻尼 + 50dp 阈值），参数在
 * `SmartisanSwipeToDelete` 里，或者自己按 `SmartisanMotion` 配。
 */
@Composable
private fun SwipeToRevealActions(
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    var revealedPx by remember { mutableFloatStateOf(0f) }
    var stripWidthPx by remember { mutableFloatStateOf(0f) }
    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .onSizeChanged { stripWidthPx = it.width.toFloat() },
        ) {
            actions()
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(-revealedPx.roundToInt(), 0) }
                    .pointerInput(stripWidthPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                revealedPx = if (revealedPx > stripWidthPx / 2f) stripWidthPx else 0f
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            revealedPx = (revealedPx - dragAmount).coerceIn(0f, stripWidthPx)
                        }
                    },
        ) {
            content()
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
