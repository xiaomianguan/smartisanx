package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanChip

/** 短信收件人输入区，地址与收件人列表由调用方管理。 */
@Composable
fun SmartisanMessageRecipients(
    address: String,
    onAddressChange: (String) -> Unit,
    recipients: List<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "To",
    placeholder: String = "",
    addDescription: String = "Add recipient",
) {
    Column(modifier.fillMaxWidth().background(LocalSmartisanColors.current.surface)) {
        if (recipients.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                recipients.forEach { recipient ->
                    SmartisanChip(text = recipient, onRemove = { onRemove(recipient) })
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 7.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanText(label, Modifier.padding(end = 8.dp))
            SmartisanClearableField(
                value = address,
                onValueChange = onAddressChange,
                modifier = Modifier.weight(1f),
                placeholder = placeholder,
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
            )
            val interaction = rememberSmartisanInteractionSource()
            val pressed by interaction.collectSmartisanPressedAsState()
            val enabled = address.isNotBlank()
            SmartisanIcon(
                res = R.drawable.mms_add_recipient_selector,
                contentDescription = addDescription,
                modifier = Modifier.smartisanClickable(enabled = enabled, interactionSource = interaction, onClick = onAdd),
                pressed = pressed,
                enabled = enabled,
                size = 36.dp,
            )
        }
        SmartisanRowDivider(startIndent = 0.dp)
    }
}
