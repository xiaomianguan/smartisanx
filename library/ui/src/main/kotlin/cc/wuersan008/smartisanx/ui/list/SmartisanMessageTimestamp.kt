package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 短信会话中的居中时间标签；调用方负责提供已格式化的文字。 */
@Composable
fun SmartisanMessageTimestamp(text: String, modifier: Modifier = Modifier) {
    val colors = LocalSmartisanColors.current
    Box(
        modifier.fillMaxWidth().padding(top = 8.25.dp, bottom = 4.5.dp),
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            color = colors.textTertiary,
            style = LocalSmartisanTypography.current.caption.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
        )
    }
}
