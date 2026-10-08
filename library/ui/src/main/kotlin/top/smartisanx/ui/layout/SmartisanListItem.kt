package top.smartisanx.ui.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.rememberSmartisanInteractionSource
import top.smartisanx.core.interaction.smartisanClick
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanRowDivider
import top.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格列表行。
 *
 * 合并了锤子音乐的资料库行 / 设置行与锤子天气的城市行：60dp 行高、12dp 左右外边距、
 * 一级文字 15sp、二级文字 12.5sp。按压时切换为 `surfacePressed`，
 * 多选选中时切换为 `selectionBackground`（原版的浅蓝高亮），不使用涟漪。
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
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }

    val background =
        when {
            selected -> colors.selectionBackground
            pressed && enabled -> colors.surfacePressed
            else -> Color.Transparent
        }
    val titleColor = if (enabled) colors.textPrimary else colors.textDisabled

    Column(modifier.fillMaxWidth().background(background)) {
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
                    .padding(horizontal = contentPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (leading != null) {
                leading()
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
                trailing()
            }
        }
        if (showDivider) {
            SmartisanRowDivider(startIndent = dividerStartIndent)
        }
    }
}
