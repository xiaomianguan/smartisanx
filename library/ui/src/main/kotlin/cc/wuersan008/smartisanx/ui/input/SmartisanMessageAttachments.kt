package cc.wuersan008.smartisanx.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanPageIndicator

/** 短信输入扩展面板：五列三行表情与底部相机、快捷回复、联系人、退格操作。 */
@Composable
fun SmartisanMessageAttachments(
    onEmoji: (String) -> Unit,
    onCamera: () -> Unit,
    onQuickReply: () -> Unit,
    onContact: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    cameraLabel: String = "Camera",
    quickReplyLabel: String = "Quick Reply",
    contactLabel: String = "Share Contact",
    deleteLabel: String = "Delete",
) {
    val colors = LocalSmartisanColors.current
    val pages = rememberPagerState { MessageEmoji.size / 15 }
    Column(modifier.fillMaxWidth().background(colors.surface)) {
        SmartisanPageIndicator(
            pageCount = pages.pageCount,
            currentPage = pages.currentPage,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(12.dp),
        )
        HorizontalPager(pages, modifier = Modifier.fillMaxWidth().height(175.dp)) { page ->
            Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                repeat(3) { row ->
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        repeat(5) { column ->
                            val emoji = MessageEmoji[page * 15 + row * 5 + column]
                            Box(
                                Modifier.weight(1f).fillMaxSize().smartisanClickable { onEmoji(emoji) },
                                contentAlignment = Alignment.Center,
                            ) {
                                SmartisanText(emoji, fontSize = 27.sp)
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(0.67.dp).background(colors.divider))
        Row(Modifier.fillMaxWidth().height(53.33.dp)) {
            MessageAttachmentButton(R.drawable.mms_input_extension_camera, cameraLabel, onCamera, Modifier.weight(1.19f))
            MessageAttachmentButton(R.drawable.mms_input_extension_quick_reply, quickReplyLabel, onQuickReply, Modifier.weight(1f))
            MessageAttachmentButton(R.drawable.mms_input_extension_share_contact, contactLabel, onContact, Modifier.weight(1f))
            MessageAttachmentButton(R.drawable.mms_input_extension_delete, deleteLabel, onDelete, Modifier.weight(1.19f), showLabel = false)
        }
    }
}

@Composable
private fun MessageAttachmentButton(
    res: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    showLabel: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val decoration = if (colors.isLight) {
        val background = when {
            showLabel && pressed -> R.drawable.mms_input_extension_left_bg_pressed
            showLabel -> R.drawable.mms_input_extension_left_bg
            pressed -> R.drawable.mms_input_extension_right_bg_pressed
            else -> R.drawable.mms_input_extension_right_bg
        }
        Modifier.smartisanDrawableBackground(background)
    } else {
        Modifier.background(if (pressed) colors.surfacePressed else colors.surface)
    }
    Column(
        modifier.height(53.33.dp).then(decoration)
            .smartisanClickable(interactionSource = interaction, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SmartisanIcon(
            res = res,
            contentDescription = if (showLabel) null else label,
            tint = if (colors.isLight) Color.Unspecified else colors.textTertiary,
        )
        if (showLabel) SmartisanText(label, color = colors.textTertiary, fontSize = 10.sp, maxLines = 1)
    }
}

// EmojiView 原版六页，每页 15 个，顺序来自 EmojiParser。
private val MessageEmoji = listOf(
    "😄", "😊", "😃", "☺", "😉", "😍", "😘", "😚", "😳", "😌", "😁", "😜", "😝", "😒", "😏",
    "😓", "😔", "😞", "😖", "😥", "😰", "😨", "😣", "😢", "😭", "😂", "😲", "😱", "😠", "😡",
    "😪", "😷", "👿", "👽", "💛", "💙", "💜", "💕", "💚", "❤", "💔", "💖", "💘", "✨", "🌟",
    "⭐", "💢", "🌙", "❕", "☔", "❔", "💤", "💨", "💦", "🎶", "🎵", "🔥", "💩", "👍", "👎",
    "👌", "👊", "✊", "✌", "👋", "✋", "👐", "👆", "👇", "👉", "👈", "🙌", "🙏", "☝", "👏",
    "💪", "🚶", "🏃", "👫", "💃", "👯", "🙆", "🙅", "💁", "🙇", "💏", "💑", "💆", "💇", "💅",
)
