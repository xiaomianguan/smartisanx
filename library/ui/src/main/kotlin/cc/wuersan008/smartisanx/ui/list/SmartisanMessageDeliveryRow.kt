package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 短信送达报告的 60dp 行，对应 msg_delivery_report_item.xml。 */
@Composable
fun SmartisanMessageDeliveryRow(
    recipient: String,
    timestamp: String,
    status: String,
    modifier: Modifier = Modifier,
    failed: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    Row(
        modifier.fillMaxWidth().heightIn(min = 60.dp).background(colors.surface).padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            SmartisanText(recipient, fontSize = 16.sp, maxLines = 1)
            SmartisanText(timestamp, modifier = Modifier.padding(top = 2.dp), fontSize = 13.5.sp, color = colors.textTertiary, maxLines = 1)
        }
        SmartisanText(status, fontSize = 13.5.sp, color = if (failed) colors.warning else colors.textSecondary, maxLines = 1)
    }
}
