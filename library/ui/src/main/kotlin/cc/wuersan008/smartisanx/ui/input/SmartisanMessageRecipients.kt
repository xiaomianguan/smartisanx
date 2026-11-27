package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

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
    onPickContact: (() -> Unit)? = null,
    editorModifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val textStyle = LocalSmartisanTypography.current.body.copy(fontSize = 13.5.sp, color = colors.textPrimary)
    val pickerInteraction = rememberSmartisanInteractionSource()
    val pickerPressed by pickerInteraction.collectSmartisanPressedAsState()
    val canPick = onPickContact != null || address.isNotBlank()
    val frame = if (colors.isLight) Modifier.smartisanDrawableBackground(R.drawable.recipient_inbox)
    else Modifier.padding(horizontal = 3.dp, vertical = 3.dp)
        .background(colors.surfacePressed, RoundedCornerShape(24.dp))
    Box(modifier.fillMaxWidth().background(colors.surface)) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .then(frame)
                .padding(start = 7.dp, end = 46.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            recipients.forEach { recipient ->
                val interaction = rememberSmartisanInteractionSource()
                val pressed by interaction.collectSmartisanPressedAsState()
                val chip = if (colors.isLight) {
                    Modifier.smartisanDrawableBackground(
                        if (pressed) R.drawable.bubble_contact_b else R.drawable.bubble_contact,
                    )
                } else {
                    Modifier.background(colors.surface, RoundedCornerShape(12.dp))
                }
                SmartisanText(
                    text = recipient,
                    modifier = Modifier.align(Alignment.CenterVertically).widthIn(max = 128.dp)
                        .then(chip)
                        .smartisanClickable(interactionSource = interaction) { onRemove(recipient) }
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = address,
                onValueChange = onAddressChange,
                modifier = editorModifier.align(Alignment.CenterVertically).widthIn(min = 64.dp)
                    .weight(1f).padding(start = 12.dp),
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                decorationBox = { field ->
                    Box(Modifier.heightIn(min = 24.dp), contentAlignment = Alignment.CenterStart) {
                        if (address.isEmpty() && recipients.isEmpty()) {
                            SmartisanText(
                                text = placeholder.ifEmpty { label },
                                style = textStyle,
                                color = if (colors.isLight) Color(0x33000000) else colors.textHint,
                                maxLines = 1,
                            )
                        }
                        field()
                    }
                },
            )
        }
        if (colors.isLight) {
            Box(Modifier.matchParentSize().smartisanDrawableBackground(R.drawable.recipient_inbox_mask))
        }
        SmartisanIcon(
            res = R.drawable.mms_add_recipient_selector,
            contentDescription = addDescription,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp)
                .smartisanClickable(enabled = canPick, interactionSource = pickerInteraction) {
                    if (onPickContact != null) onPickContact() else onAdd()
                },
            pressed = pickerPressed,
            enabled = canPick,
            size = 36.dp,
        )
    }
}
