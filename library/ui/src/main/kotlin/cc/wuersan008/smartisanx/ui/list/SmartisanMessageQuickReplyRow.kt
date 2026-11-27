package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val background = if (colors.isLight) {
        Modifier.smartisanDrawableBackground(
            if (pressed) R.drawable.mms_quick_snippet_text_bg_pressed else R.drawable.mms_quick_snippet_text_bg,
        )
    } else {
        Modifier.background(
            if (pressed) colors.surfacePressed else colors.surface,
            RoundedCornerShape(8.dp),
        )
    }
    Box(
        modifier.fillMaxWidth().heightIn(min = 66.dp).padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 49.dp)
                .then(background)
                .smartisanClickable(interactionSource = interaction, onClick = onClick)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (addRow) {
                SmartisanIcon(res = R.drawable.mms_add_quick_text, contentDescription = null, size = 36.dp)
            }
            SmartisanText(
                text,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 16.sp,
                color = if (addRow) colors.textTertiary else colors.textPrimary,
                maxLines = 2,
            )
            if (onEdit != null) {
                SmartisanIcon(
                    res = R.drawable.mms_list_edit,
                    contentDescription = editDescription,
                    modifier = Modifier.smartisanClickable(onClick = onEdit),
                    size = 36.dp,
                )
            }
        }
    }
}
