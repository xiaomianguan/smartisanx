package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 锤子便签图片选择器的正方形缩略图单元。点击照片切换勾选，右上角按钮可独立打开预览。
 * 缩略图留出 1dp 边缘，选中标记使用便签 APK 的原图。
 */
@Composable
fun SmartisanPhotoSelectionTile(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    painter: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onPreview: (() -> Unit)? = null,
    previewDescription: String? = null,
) {
    val selectionInteraction = rememberSmartisanInteractionSource()
    val selectionPressed by selectionInteraction.collectSmartisanPressedAsState()
    Box(modifier.aspectRatio(1f)) {
        Box(
            Modifier.matchParentSize().toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = selectionInteraction,
                indication = null,
                onValueChange = onCheckedChange,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painter ?: rememberSmartisanDrawablePainter(R.drawable.photo_unselected_large_bg),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().padding(1.dp),
            )
            SmartisanIcon(
                res = if (checked) R.drawable.notes_import_select else R.drawable.notes_import_unselect,
                contentDescription = null,
                size = 36.dp,
            )
            Box(
                Modifier.matchParentSize().smartisanDrawableBackground(
                    R.drawable.select_image_item_bg_selector,
                    pressed = selectionPressed,
                ),
            )
        }
        if (onPreview != null) {
            val previewInteraction = rememberSmartisanInteractionSource()
            val previewPressed by previewInteraction.collectSmartisanPressedAsState()
            SmartisanIcon(
                res = R.drawable.select_image_enlarge_button_selector,
                contentDescription = previewDescription,
                modifier = Modifier.align(Alignment.TopEnd).padding(1.dp).smartisanClickable(
                    enabled = enabled,
                    interactionSource = previewInteraction,
                    onClick = onPreview,
                ),
                enabled = enabled,
                pressed = previewPressed,
                size = 64.dp / 3,
            )
        }
    }
}
