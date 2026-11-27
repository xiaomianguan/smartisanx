package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/** 短信会话的头像，38dp 照片叠上原版 130px @xxhdpi 外框。 */
@Composable
fun SmartisanMessageAvatar(
    modifier: Modifier = Modifier,
    photo: Painter? = null,
    contentDescription: String? = null,
    group: Boolean = false,
) {
    Box(modifier.size(130.dp / 3), contentAlignment = Alignment.Center) {
        if (photo == null) {
            SmartisanIcon(
                res = if (group) R.drawable.mms_group_contacts_picture else R.drawable.mms_contact_picture,
                contentDescription = contentDescription,
                modifier = Modifier.clip(CircleShape),
                size = 38.dp,
            )
        } else {
            Image(
                painter = photo,
                contentDescription = contentDescription,
                modifier = Modifier.size(38.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        SmartisanIcon(res = R.drawable.mms_avatar, contentDescription = null, modifier = Modifier.matchParentSize())
    }
}
