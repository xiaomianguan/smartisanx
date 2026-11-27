package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 原版蓝色标题栏文字按钮，放入 [SmartisanTitleBar] 的 trailingContent。 */
@Composable
fun SmartisanTitleBarTextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val colors = LocalSmartisanColors.current
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 32.dp)
            .smartisanDrawableBackground(R.drawable.selector_title_button_ok_blue_background, enabled = enabled, pressed = pressed)
            .smartisanClickable(interactionSource = interaction, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            color = if (enabled) colors.onAccent else colors.textDisabled,
            style = LocalSmartisanTypography.current.caption,
            maxLines = 1,
        )
    }
}
