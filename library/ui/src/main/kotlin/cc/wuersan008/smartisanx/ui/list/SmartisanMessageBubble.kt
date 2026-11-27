package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 锤子短信的收发气泡。浅色使用原版 NinePatch，深色使用主题表面色。 */
@Composable
fun SmartisanMessageBubble(
    text: String,
    outgoing: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    avatar: (@Composable () -> Unit)? = null,
    attachment: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val bubbleWidth = if (LocalConfiguration.current.smallestScreenWidthDp >= 411) 305.5.dp else 233.33.dp
    val decoration = if (colors.isLight) {
        val res = if (outgoing) {
            if (pressed) R.drawable.mms_send_message_press else R.drawable.mms_send_message
        } else {
            if (pressed) R.drawable.mms_receive_message_press else R.drawable.mms_receive_message
        }
        Modifier.smartisanDrawableBackground(res)
    } else {
        Modifier.background(
            color = when {
                pressed -> colors.surfacePressed
                outgoing -> colors.selectionBackground
                else -> colors.surface
            },
            shape = RoundedCornerShape(12.dp),
        )
    }
    val click = if (onClick == null) Modifier else Modifier.smartisanClickable(
        interactionSource = interaction,
        role = null,
        onClick = onClick,
    )
    Box(modifier.fillMaxWidth()) {
        if (avatar != null) {
            Box(
                Modifier.align(if (outgoing) Alignment.TopEnd else Alignment.TopStart)
                    .padding(start = 12.dp, end = 12.dp, top = 2.dp),
            ) { avatar() }
        }
        Box(
            Modifier.fillMaxWidth().padding(
                start = if (outgoing) 16.dp else 59.33.dp,
                end = if (outgoing) 59.33.dp else 16.dp,
                bottom = 11.25.dp,
            ),
            contentAlignment = if (outgoing) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier.widthIn(max = bubbleWidth).heightIn(min = 50.33.dp)
                    .then(decoration).then(click)
                    .padding(
                        start = if (outgoing) 18.33.dp else 25.67.dp,
                        end = if (outgoing) 25.67.dp else 18.33.dp,
                        top = 14.dp,
                        bottom = 14.dp,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Column {
                    attachment?.invoke()
                    if (text.isNotEmpty()) {
                        SmartisanText(
                            text = text,
                            style = LocalSmartisanTypography.current.body.copy(fontSize = 15.sp),
                            color = when {
                                !colors.isLight -> colors.textPrimary
                                outgoing -> Color(0xFF3C5D9A)
                                else -> Color(0xFF666666)
                            },
                        )
                    }
                }
            }
        }
    }
}
