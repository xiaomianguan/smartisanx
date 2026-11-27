package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R

/** 便签分享页的横向缩略图条，对应 NinePicLayout 和 ninepic_item.xml。 */
@Composable
fun SmartisanPhotoPreviewStrip(
    photos: List<Painter>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    photoDescription: (Int) -> String? = { null },
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        itemsIndexed(photos) { index, photo ->
            Image(
                painter = photo,
                contentDescription = photoDescription(index),
                modifier = Modifier.size(width = 56.dp, height = 57.dp)
                    .smartisanDrawableBackground(R.drawable.notes_photo_preview_frame)
                    .smartisanClickable { onPhotoClick(index) }
                    .padding(5.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
