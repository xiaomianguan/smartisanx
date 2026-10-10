package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.basic.SmartisanDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 底部标签栏的一项。
 *
 * 对应锤子音乐的可排序底部导航与锤子时钟的四页底部栏：
 * 选中态用强调色并轻微放大，未选中态用三级文字色。
 *
 * 图标支持两种来源，原版素材优先：
 *
 * ```kotlin
 * // 原版标签栏图标（tabbar_*_selector，选中态会自动切到按下图）
 * SmartisanBottomBarItem(label = "歌曲", iconRes = SmartisanOriginalIcons.TabSong)
 *
 * // 自定义矢量图标（原版没有对应素材时才用）
 * SmartisanBottomBarItem(icon = myVectorIcon, label = "音乐")
 * ```
 */
@Immutable
data class SmartisanBottomBarItem(
    /** 未选中时的矢量图标；只使用原版位图图标时可以不传。 */
    val icon: ImageVector? = null,
    /** 文字标签。 */
    val label: String,
    /** 选中时的矢量图标，默认与未选中一致。 */
    val selectedIcon: ImageVector? = icon,
    /** 未选中时的原版位图图标，传了它优先于 [icon]。 */
    @DrawableRes val iconRes: Int? = null,
    /** 选中时的原版位图图标，默认与 [iconRes] 一致。 */
    @DrawableRes val selectedIconRes: Int? = iconRes,
)

/**
 * 锤子风格底部标签栏。
 *
 * 高 54dp（原版 `smartisan_bottom_bar_height`）、顶部 0.67dp 分隔线、图标 30dp、文字 10sp；
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
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.BottomBarBackground,
) {
    val colors = LocalSmartisanColors.current
    // 原版：底色是 sb_repeat_tabbar_bg，上方叠一张 tab_bar_shadow 再叠一条 0.67dp 分隔线。
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.surface)
        }
    val shadowHeight =
        with(LocalDensity.current) {
            rememberSmartisanDrawablePainter(SmartisanDrawables.BottomBarShadow)
                .intrinsicSize.height.toDp()
        }
    Box(modifier.fillMaxWidth().then(backgroundModifier)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    // 原版把导航栏 insets 作为 Row 的内边距，底色会一起盖住。
                    .then(
                        if (includeNavigationBar) {
                            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        } else {
                            Modifier
                        },
                    )
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
        if (showTopDivider) {
            Box(
                Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(shadowHeight)
                    .smartisanDrawableBackground(SmartisanDrawables.BottomBarShadow),
            )
            Box(
                Modifier.align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(SmartisanDimens.DividerThickness)
                    .background(colors.divider),
            )
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
    val res = if (selected) item.selectedIconRes else item.iconRes
    val vector = if (selected) item.selectedIcon else item.icon
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
        when {
            // 原版位图图标：tabbar_*_selector 自带选中态（state_selected → 按下图），不需要 tint。
            res != null ->
                SmartisanIcon(
                    res = res,
                    contentDescription = item.label,
                    size = SmartisanDimens.BottomBarIconSize,
                    selected = selected,
                )
            vector != null ->
                SmartisanIcon(
                    imageVector = vector,
                    contentDescription = item.label,
                    tint = tint,
                    size = SmartisanDimens.BottomBarIconSize,
                )
        }
        SmartisanText(
            text = item.label,
            modifier = Modifier.padding(top = 1.dp),
            style = typography.caption.copy(fontSize = 10.sp),
            color = tint,
            maxLines = 1,
        )
    }
}
