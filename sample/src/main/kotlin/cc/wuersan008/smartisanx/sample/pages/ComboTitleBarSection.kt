package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIconButton
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanChip
import cc.wuersan008.smartisanx.ui.control.SmartisanTextButton
import cc.wuersan008.smartisanx.ui.layout.SmartisanComboTitleBar

/**
 * framework 复合标题栏（`smartisanos.widget.SmartisanComboTitleBar`）演示。
 *
 * 三段分别对应原版的三种用法，看点是**中槽摆法**：
 * 1. 标题短 → 在整条栏里居中（即使左右按钮一宽一窄）；
 * 2. 标题长 → 取消居中，贴左按钮右侧排布，右侧被截断；
 * 3. 中槽换成自定义内容（分段按钮组）+ 次级栏 → 主栏投影换成短的那一截、压在次级栏顶部。
 */
@Composable
fun ComboTitleBarSection() {
    var tab by remember { mutableIntStateOf(0) }

    // 1) 短标题：左右一宽一窄，标题仍然按整条栏居中。
    SmartisanComboTitleBar(
        includeStatusBar = false,
        title = "全部邮件",
        subtitle = "共 128 封",
        leading = { ComboLeadingButton() },
        trailing = { SmartisanTextButton(text = "完成", onClick = {}) },
    )

    // 2) 长标题：2 × max(左,右) + 标题宽 已经超过栏宽，于是取消居中、贴左按钮右侧。
    SmartisanComboTitleBar(
        includeStatusBar = false,
        title = "一个很长很长的标题，长到左右按钮之间根本装不下",
        leading = { ComboLeadingButton() },
        trailing = { SmartisanTextButton(text = "完成", onClick = {}) },
    )

    // 3) 中槽自定义（原版 STYLE_CONTENT_RADIO_TAB）+ 次级栏：投影自动变成压在次级栏顶部的短投影。
    SmartisanComboTitleBar(
        includeStatusBar = false,
        title = "这一层会被 center 顶掉",
        center = {
            SmartisanButtonTabGroup(
                items = listOf("收件箱", "已发送", "草稿"),
                selectedIndex = tab,
                onSelectedChange = { tab = it },
            )
        },
        leading = { ComboLeadingButton() },
        trailing = { ComboTrailingIcons() },
        secondaryBar = { ComboSecondaryBar() },
    )

    SampleFootnote(
        "原版中槽有 6 种 style（普通文字 / 单选标签页 / 下拉 / 左右翻页 / 跑马灯 / 分隔条），" +
            "这里用 center 槽位代替；两侧按钮原版是一排 SmartisanButton，最多 10 个、相邻负 6dp 压住。",
    )
}

/** 左槽：原版是 `SmartisanButton`，这里用原版返回图标。 */
@Composable
private fun ComboLeadingButton() {
    SmartisanIconButton(onClick = {}, contentDescription = "返回") {
        SmartisanIcon(SmartisanOriginalIcons.Back, contentDescription = null)
    }
}

/** 右槽：一排图标按钮，按原版 `smartisan_small_blank_spacing_width`（6dp）互相压住。 */
@Composable
private fun ComboTrailingIcons() {
    val overlapPx = with(LocalDensity.current) { SmartisanDimens.ComboTitleActionSpacing.roundToPx() }
    // 原版是自己摆 LinearLayout 的子项（相邻右外边距取负），这里用 Layout 做同样的事：
    // 每个按钮往左挪 6dp，整体宽度也相应减掉，看起来就是「紧紧挨着的一排」。
    Layout(
        content = {
            SmartisanIconButton(onClick = {}, contentDescription = "搜索") {
                SmartisanIcon(SmartisanOriginalIcons.Search, contentDescription = null)
            }
            SmartisanIconButton(onClick = {}, contentDescription = "更多") {
                SmartisanIcon(SmartisanOriginalIcons.More, contentDescription = null)
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val width = placeables.sumOf { it.width } - overlapPx * (placeables.size - 1)
        layout(width, placeables.maxOfOrNull { it.height } ?: 0) {
            var x = 0
            placeables.forEach { placeable ->
                placeable.place(x, 0)
                x += placeable.width - overlapPx
            }
        }
    }
}

/** 次级栏：原版由 `secondaryBarLayout` 指定，这里放一排标签。 */
@Composable
private fun ComboSecondaryBar() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceRaised)
                .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmartisanText(
                text = "次级栏：",
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            SmartisanChip(text = "未读", selected = true)
            SmartisanChip(text = "星标", modifier = Modifier.padding(start = 8.dp))
            SmartisanChip(text = "附件", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
