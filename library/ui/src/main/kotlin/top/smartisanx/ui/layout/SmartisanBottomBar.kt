package top.smartisanx.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.smartisanx.core.anim.SmartisanMotion
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.rememberSmartisanInteractionSource
import top.smartisanx.core.interaction.smartisanClick
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanDivider
import top.smartisanx.ui.basic.SmartisanIcon
import top.smartisanx.ui.basic.SmartisanText

/**
 * 底部标签栏的一项。
 *
 * 对应锤子音乐的可排序底部导航与锤子时钟的四页底部栏：
 * 选中态用强调色并轻微放大，未选中态用三级文字色。
 */
@Immutable
data class SmartisanBottomBarItem(
    /** 未选中时的图标。 */
    val icon: ImageVector,
    /** 文字标签。 */
    val label: String,
    /** 选中时的图标，默认与未选中一致。 */
    val selectedIcon: ImageVector = icon,
)

/**
 * 锤子风格底部标签栏。
 *
 * 高 50dp、顶部 0.67dp 分隔线、图标 30dp、文字 10sp；
 * 选中项使用 `accent` 色，按压时轻微缩放。
 */
@Composable
fun SmartisanBottomBar(
    items: List<SmartisanBottomBarItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    includeNavigationBar: Boolean = true,
    showTopDivider: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    Column(modifier.fillMaxWidth().background(colors.surface)) {
        if (showTopDivider) {
            SmartisanDivider(color = colors.divider)
        }
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(SmartisanDimens.BottomBarHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { index, item ->
                SmartisanBottomBarItemView(
                    item = item,
                    selected = index == selectedIndex,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(index) },
                )
            }
        }
        if (includeNavigationBar) {
            Box(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun SmartisanBottomBarItemView(
    item: SmartisanBottomBarItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick(onClick)
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = SmartisanMotion.PressSpring,
        label = "smartisan bottom bar press",
    )
    val tint = if (selected) colors.accent else colors.textTertiary
    Column(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Tab,
                    onClick = click,
                )
                .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SmartisanIcon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.label,
            tint = tint,
            size = SmartisanDimens.BottomBarIconSize,
        )
        SmartisanText(
            text = item.label,
            modifier = Modifier.padding(top = 1.dp),
            style = typography.caption.copy(fontSize = 10.sp),
            color = tint,
            maxLines = 1,
        )
    }
}
