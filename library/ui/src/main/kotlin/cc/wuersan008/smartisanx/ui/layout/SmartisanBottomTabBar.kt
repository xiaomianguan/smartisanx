package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * R2 固件的底部标签栏（framework `smartisanos.widget.BottomBar` 的可缩放样式）。
 *
 * 联系人（通话记录 / 联系人 / 拨号 / 收藏）等 R2 系统应用用的是它；[SmartisanBottomBar] 是更早的
 * `sb_tab_bar`（锤子音乐 / 时钟那一版：渐变底图、图标不缩放）。两者的区别照抄原版：
 *
 * | 行为 | 原版 |
 * | --- | --- |
 * | 白底，高 54dp | `BottomBar.setBackgroundColor(-1)`、`smartisan_bottom_bar_height` |
 * | 每项宽 = 屏宽 / 5，整排居中（4 项时左右各留 1/10） | `mItemWidth = widthPixels / 5`、容器 `gravity = CENTER` |
 * | 上方 11dp 投影 + 0.67dp 分隔线，画在栏外、盖在内容上 | `BarsHelper.BarShadowBuilder`：`bottom_bar_shadow`、`divider_bg`，`translationY = -高度` |
 * | 图标 30dp 矢量，按选中态着色：选中 `#99000000`、其余 `#33000000` | 联系人 `color/bottom_bar_icon_selector` |
 * | 文字 9sp 粗体、与图标间距 3dp；选中 `#9A000000`、其余 `#66000000` | `BottomBarItemView`、`smartisan_bottom_bar_drawablePadding`、`bottom_tab_text_color` |
 * | 未选中的图标与文字缩到 0.9，选中 1.0，按下 1.1，弹簧（阻尼 0.55、刚度 800） | `BottomBarItemView.startAnimIfNeed()`：`SpringForce(0.55, 800)` |
 *
 * R2 联系人实测（400dpi）：栏高 135px、四项各 216px 从 x=108 排到 972，选中图标 75px、其余 67px（0.9 倍）。
 *
 * 联系人四个标签的原版矢量图标是 [SmartisanDrawables.BottomTabRecent] / [SmartisanDrawables.BottomTabContacts] /
 * [SmartisanDrawables.BottomTabKeypad] / [SmartisanDrawables.BottomTabFavorite]。
 *
 * @param items 标签项（与 [SmartisanBottomBar] 共用 [SmartisanBottomBarItem]；`iconRes` 优先于矢量 `icon`）。
 * @param selectedIndex 当前选中项。
 * @param onSelected 点击某项时回调。
 * @param includeNavigationBar 是否把系统导航栏高度作为栏自己的下内边距（栏底色一直画到屏幕最底下）。
 */
@Composable
fun SmartisanBottomTabBar(
    items: List<SmartisanBottomBarItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    includeNavigationBar: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    Box(modifier.fillMaxWidth().background(colors.surface)) {
        // 原版 BarsHelper：投影与分隔线都往上平移自身高度，落在栏外、盖住内容底部。
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(y = -BottomTabShadowHeight)
                .fillMaxWidth()
                .height(BottomTabShadowHeight)
                .smartisanDrawableBackground(SmartisanDrawables.BottomBarShadowPlain),
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(y = -BottomTabDividerHeight)
                .fillMaxWidth()
                .height(BottomTabDividerHeight)
                .smartisanDrawableBackground(SmartisanDrawables.BarDivider),
        )
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .then(if (includeNavigationBar) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier),
        ) {
            val itemWidth = maxWidth / BottomTabSlots
            Row(
                modifier = Modifier.fillMaxWidth().height(SmartisanDimens.BottomBarHeight),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEachIndexed { index, item ->
                    BottomTabItem(
                        item = item,
                        selected = index == selectedIndex,
                        modifier = Modifier.width(itemWidth),
                        onClick = { onSelected(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    item: SmartisanBottomBarItem,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 原版 startAnimIfNeed：按下 1.1；松手后选中 1.0、未选中 0.9。
    val scale by animateFloatAsState(
        targetValue =
            when {
                pressed -> BottomTabPressedScale
                selected -> 1f
                else -> BottomTabNormalScale
            },
        animationSpec = spring(dampingRatio = BottomTabSpringDamping, stiffness = BottomTabSpringStiffness),
        label = "smartisan bottom tab scale",
    )
    val textColor = if (selected) colors.textSecondary else colors.textTertiary
    // 联系人 bottom_bar_icon_selector：选中 #99000000，其余 #33000000（未选中文字色再减半）。
    val iconColor = if (selected) colors.textSecondary else colors.textTertiary.copy(alpha = colors.textTertiary.alpha / 2f)
    Column(
        modifier =
            modifier.selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = smartisanClick(onClick),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val iconModifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        when {
            item.iconRes != null ->
                SmartisanIcon(
                    res = (if (selected) item.selectedIconRes else item.iconRes) ?: item.iconRes,
                    contentDescription = null,
                    modifier = iconModifier,
                    size = SmartisanDimens.BottomBarIconSize,
                    tint = iconColor,
                )
            item.icon != null ->
                SmartisanIcon(
                    imageVector = (if (selected) item.selectedIcon else item.icon) ?: item.icon,
                    contentDescription = null,
                    modifier = iconModifier,
                    tint = iconColor,
                    size = SmartisanDimens.BottomBarIconSize,
                )
        }
        SmartisanText(
            text = item.label,
            modifier = Modifier.padding(top = BottomTabTextGap).graphicsLayer { scaleX = scale; scaleY = scale },
            style = typography.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1,
        )
    }
}

/** 原版 `mItemWidth = 屏宽 / 5`：项宽固定为五等分，与项数无关。 */
private const val BottomTabSlots = 5
private const val BottomTabNormalScale = 0.9f
private const val BottomTabPressedScale = 1.1f
private const val BottomTabSpringDamping = 0.55f
private const val BottomTabSpringStiffness = 800f
private val BottomTabShadowHeight = 11.dp
private val BottomTabDividerHeight = 0.67.dp
private val BottomTabTextGap = 3.dp
