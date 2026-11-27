package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 短信附件预览。原版图片高度最大 90dp，暂存附件可显示移除操作。 */
@Composable
fun SmartisanMessageAttachmentPreview(
    label: String,
    modifier: Modifier = Modifier,
    photo: Painter? = null,
    onRemove: (() -> Unit)? = null,
    removeDescription: String = "Remove attachment",
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (photo != null) {
            Image(
                painter = photo,
                contentDescription = label,
                modifier = Modifier.heightIn(max = 90.dp),
                contentScale = ContentScale.Fit,
            )
        } else {
            SmartisanText(label, modifier = Modifier.padding(start = 8.dp, end = if (onRemove == null) 8.dp else 36.dp))
        }
        if (onRemove != null) {
            SmartisanIcon(
                res = R.drawable.mms_attachment_remove,
                contentDescription = removeDescription,
                modifier = Modifier.align(Alignment.TopEnd).smartisanClickable(onClick = onRemove),
            )
        }
    }
}
