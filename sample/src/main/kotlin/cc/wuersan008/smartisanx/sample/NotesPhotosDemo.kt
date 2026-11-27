package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.list.SmartisanPhotoSelectionTile
import cc.wuersan008.smartisanx.ui.list.SmartisanPhotoPreviewStrip
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog

@Composable
internal fun NotesPhotosDemo(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var preview by rememberSaveable { mutableIntStateOf(-1) }
    val colors = LocalSmartisanColors.current
    val demoColors = listOf(colors.surfacePressed, colors.selectionBackground, colors.surfaceRaised, colors.surface)
    SamplePageScaffold(title = "便签图片选择", onBack = onBack) {
        Row(Modifier.fillMaxWidth()) {
            demoColors.forEachIndexed { index, color ->
                val bit = 1 shl index
                SmartisanPhotoSelectionTile(
                    checked = selected and bit != 0,
                    onCheckedChange = { checked -> selected = if (checked) selected or bit else selected and bit.inv() },
                    painter = ColorPainter(color),
                    contentDescription = "缩略图 ${index + 1}",
                    modifier = Modifier.weight(1f),
                    onPreview = { preview = index },
                    previewDescription = "预览缩略图 ${index + 1}",
                )
            }
        }
        SampleSectionHeader("分享缩略图")
        SmartisanPhotoPreviewStrip(
            photos = demoColors.map(::ColorPainter),
            onPhotoClick = { preview = it },
            photoDescription = { "分享缩略图 ${it + 1}" },
        )
    }
    if (preview in demoColors.indices) {
        PhotoTilePreview(preview, demoColors[preview], onDismiss = { preview = -1 })
    }
}

@Composable
private fun PhotoTilePreview(index: Int, color: Color, onDismiss: () -> Unit) {
    SmartisanDialog(
        title = "预览缩略图 ${index + 1}",
        onDismissRequest = onDismiss,
        onConfirm = onDismiss,
    ) {
        Box(Modifier.fillMaxWidth().height(160.dp).background(color))
    }
}
