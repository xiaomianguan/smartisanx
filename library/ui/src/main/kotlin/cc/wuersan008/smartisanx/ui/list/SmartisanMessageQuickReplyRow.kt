package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 短信快捷回复卡片。原版每行 66dp，支持普通文本、编辑按钮和新增行。 */
@Composable
fun SmartisanMessageQuickReplyRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    addRow: Boolean = false,
    onEdit: (() -> Unit)? = null,
    editDescription: String = "Edit",
    onRemove: (() -> Unit)? = null,
    removeDescription: String = "Remove",
    reorderDescription: String? = null,
    enabled: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Box(modifier.fillMaxWidth().height(66.dp)) {
        // NinePatch 的透明边距包含阴影，绘制范围比内容和触控区域更大。
        if (colors.isLight) {
            Box(
                Modifier.fillMaxWidth().height(65.6.dp)
                    .smartisanDrawableBackground(
                        if (pressed) R.drawable.mms_quick_snippet_text_bg_pressed
                        else R.drawable.mms_quick_snippet_text_bg,
                    ),
            )
        } else {
            Box(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 5.2.dp).height(53.6.dp)
                    .background(
                        if (pressed) colors.surfacePressed else colors.surface,
                        RoundedCornerShape(8.dp),
                    ),
            )
        }
        Box(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 7.5.dp).height(48.8.dp)
                .smartisanClickable(interactionSource = interaction, enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (addRow) {
                SmartisanIcon(
                    res = when {
                        !enabled -> R.drawable.add_quick_text_disabled
                        pressed -> R.drawable.add_quick_text_pressed
                        else -> R.drawable.mms_add_quick_text
                    },
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
                    size = 32.dp,
                )
                SmartisanText(
                    text,
                    modifier = Modifier.padding(horizontal = 42.dp),
                    fontSize = 16.sp,
                    color = when {
                        colors.isLight -> Color.Black.copy(alpha = if (enabled) 0.8f else 0.15f)
                        enabled -> colors.textPrimary
                        else -> colors.textDisabled
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onRemove != null) {
                        val removeInteraction = rememberSmartisanInteractionSource()
                        val removePressed by removeInteraction.collectSmartisanPressedAsState()
                        SmartisanIcon(
                            res = if (removePressed) R.drawable.quick_snippet_item_remove_pressed
                            else R.drawable.quick_snippet_item_remove_normal,
                            contentDescription = removeDescription,
                            modifier = Modifier.smartisanClickable(
                                interactionSource = removeInteraction, onClick = onRemove,
                            ),
                            size = 32.dp,
                        )
                    }
                    SmartisanText(
                        text,
                        modifier = Modifier.weight(1f).padding(start = if (onRemove == null) 10.dp else 8.dp, end = 8.dp),
                        fontSize = 16.sp,
                        color = if (colors.isLight) Color.Black.copy(alpha = 0.8f) else colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (onEdit != null) {
                        val editInteraction = rememberSmartisanInteractionSource()
                        val editPressed by editInteraction.collectSmartisanPressedAsState()
                        SmartisanIcon(
                            res = R.drawable.quick_snippet_icon_edit,
                            contentDescription = editDescription,
                            pressed = editPressed,
                            modifier = Modifier.size(30.dp, 48.dp).smartisanClickable(
                                interactionSource = editInteraction, onClick = onEdit,
                            ),
                        )
                    }
                    if (reorderDescription != null) {
                        Spacer(Modifier.width(12.dp))
                        SmartisanIcon(
                            res = R.drawable.quick_snippet_move,
                            contentDescription = reorderDescription,
                            modifier = Modifier.size(30.dp, 48.dp),
                        )
                    }
                }
            }
        }
    }
}
