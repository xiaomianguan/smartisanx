package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R

/** 短信快捷回复编辑框使用的单行内凹输入框。 */
@Composable
fun SmartisanShadedField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val focused by interaction.collectIsFocusedAsState()
    val background = if (colors.isLight) {
        when {
            !enabled -> R.drawable.textfield_disabled_smartisanos_light
            focused -> R.drawable.textfield_activated_smartisanos_light
            else -> R.drawable.textfield_default_smartisanos_light
        }
    } else {
        when {
            !enabled -> R.drawable.sos_android_drawable_textfield_disabled_smartisanos_dark
            focused -> R.drawable.sos_android_drawable_textfield_activated_smartisanos_dark
            else -> R.drawable.sos_android_drawable_textfield_default_smartisanos_dark
        }
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        textStyle = LocalSmartisanTypography.current.body.copy(
            color = if (enabled) colors.textPrimary else colors.textDisabled,
            fontSize = 14.sp,
        ),
        cursorBrush = SolidColor(colors.accent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        interactionSource = interaction,
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().heightIn(min = 39.2.dp)
                    .smartisanDrawableBackground(background).padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                inner()
            }
        },
    )
}
