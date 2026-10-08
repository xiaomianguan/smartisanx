package top.smartisanx.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import top.smartisanx.core.anim.SmartisanMotion
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.smartisanClick
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格文字标签页。
 *
 * 对应锤子音乐资料库顶部的分类切换与锤子时钟的世界时钟/闹钟/秒表/计时器切换：
 * 选中项用一级文字色并在下方绘制 2dp 强调色指示条。
 */
@Composable
fun SmartisanTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    val scrollState = rememberScrollState()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.surface)
                .then(if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (scrollable) Arrangement.Start else Arrangement.SpaceEvenly,
    ) {
        tabs.forEachIndexed { index, label ->
            SmartisanTab(
                label = label,
                selected = index == selectedIndex,
                modifier = if (scrollable) Modifier else Modifier.weight(1f),
                onClick = { onSelected(index) },
            )
        }
    }
}

@Composable
private fun SmartisanTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectSmartisanPressedAsState()
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = SmartisanMotion.easeInOut(SmartisanMotion.DurationShort),
        label = "smartisan tab indicator",
    )
    val click = smartisanClick(onClick)
    val textColor =
        when {
            selected -> colors.textPrimary
            pressed -> colors.textSecondary
            else -> colors.textTertiary
        }
    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Tab,
                    onClick = click,
                )
                .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SmartisanText(
            text = label,
            style = typography.listItemPrimary,
            color = textColor,
            maxLines = 1,
        )
        Box(
            Modifier
                .padding(top = 6.dp)
                .fillMaxWidth(0.5f)
                .height(2.dp)
                .background(
                    color = colors.accent.copy(alpha = indicatorAlpha),
                ),
        )
    }
}

