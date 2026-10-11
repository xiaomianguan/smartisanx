package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanShadowBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格列表行。
 *
 * 合并了锤子音乐的资料库行 / 设置行与锤子天气的城市行：60dp 行高、12dp 左右外边距、
 * 一级文字 15sp、二级文字 12.5sp。按压时切换为 `surfacePressed`，
 * 多选选中时切换为 `selectionBackground`（原版的浅蓝高亮），不使用涟漪。
 *
 * 三槽位的横向几何与 framework 的列表行母版（`list_content_item_layout.xml`）一致，
 * 所以同一张卡片里的标题**永远对齐**：
 *
 * | 槽位 | 原版 | 本组件 |
 * | --- | --- | --- |
 * | 左槽 | `left_container` 固定 `left_icon_area_width`(60dp) 宽，内容居中 | `leading` 放在 60dp 宽的居中盒子里 |
 * | 中槽 | `mid_container` 紧贴左槽（`layout_toRightOf`），无左图标时补 `flexible_space`(18dp) | 有 `leading` 时左内边距 0，否则 [contentPadding] |
 * | 右槽 | `right_container` 的 `right_container_margin`(6dp) | 有 `trailing` 时右内边距 6dp，否则 [contentPadding] |
 *
 * 左右槽都**不能按内容宽度算**：原版左槽是固定 60dp，所以 24dp 与 36dp 的图标
 * 后面文字都落在同一个 x 上（有左图标时距屏幕左 72dp）。副标题 / 箭头所在的右槽
 * 左边留 `flexible_space`(18dp)，右边留 6dp。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartisanListItem(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    showDivider: Boolean = false,
    dividerStartIndent: Dp = SmartisanDimens.RowContentStart,
    minHeight: Dp = SmartisanDimens.ListItemMinHeight,
    contentPadding: Dp = SmartisanDimens.RowContentStart,
    @DrawableRes rowBackgroundRes: Int? = SmartisanDrawables.ListRowSelector,
    @DrawableRes rowShadowRes: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }

    val titleColor = if (enabled) colors.textPrimary else colors.textDisabled
    // 原版列表行底色由 selector 决定：按下换成按压位图、activated 换成多选底色、
    // 默认是 surface_card。传 rowBackgroundRes = null 时才退回主题的纯色。
    val backgroundModifier =
        if (rowBackgroundRes != null && rowShadowRes != null) {
            // 原版卡片：内容底图 + 向外扩张的阴影 9-patch（投影落在控件边界之外，
            // 所以调用方需要给行留出外边距）。
            Modifier.smartisanShadowBackground(
                backgroundRes = rowBackgroundRes,
                shadowRes = rowShadowRes,
                enabled = enabled,
                pressed = pressed && enabled,
                activated = selected,
            )
        } else if (rowBackgroundRes != null) {
            Modifier.smartisanDrawableBackground(
                drawableRes = rowBackgroundRes,
                enabled = enabled,
                pressed = pressed && enabled,
                activated = selected,
            )
        } else {
            Modifier.background(
                when {
                    selected -> colors.selectionBackground
                    pressed && enabled -> colors.surfacePressed
                    else -> Color.Transparent
                },
            )
        }

    Column(modifier.fillMaxWidth().then(backgroundModifier)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = minHeight)
                    .then(
                        if (enabled && (onClick != null || onLongClick != null)) {
                            Modifier.combinedClickable(
                                interactionSource = interaction,
                                indication = null,
                                enabled = true,
                                role = Role.Button,
                                onLongClick = onLongClick,
                                onClick = click,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .padding(
                        // 原版左槽是固定 `left_icon_area_width`(60dp) 的方块，紧贴行的左边界；
                        // 没有左图标时才用 `flexible_space`(18dp) 缩进文字。
                        start = if (leading != null) 0.dp else contentPadding,
                        // 原版右槽的 `right_container_margin`(6dp)；没有右槽时文字自己留边。
                        end = if (trailing != null) SmartisanDimens.ListRowRightContainerMargin else contentPadding,
                        top = 10.dp,
                        bottom = 10.dp,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                // 宽度按原版固定成 60dp（内容居中），所以 24dp / 36dp 的图标后面
                // 文字都从同一个 x 开始 —— 这是「同一张卡片里标题对齐」的关键。
                Box(
                    modifier = Modifier.widthIn(min = SmartisanDimens.ListRowLeftIconArea),
                    contentAlignment = Alignment.Center,
                ) {
                    leading()
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                SmartisanText(
                    text = title,
                    style = typography.listItemPrimary,
                    color = titleColor,
                    maxLines = 2,
                )
                if (summary != null) {
                    SmartisanText(
                        text = summary,
                        modifier = Modifier.padding(top = 2.dp),
                        style = typography.listItemSecondary,
                        color = if (enabled) colors.textTertiary else colors.textDisabled,
                        maxLines = 2,
                    )
                }
            }
            if (trailing != null) {
                // 原版右槽左边留 `flexible_space`(18dp) 与正文隔开。
                Row(
                    modifier = Modifier.padding(start = SmartisanDimens.ListRowFlexibleSpace),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trailing()
                }
            }
        }
        if (showDivider) {
            SmartisanRowDivider(startIndent = dividerStartIndent)
        }
    }
}
