package cc.wuersan008.smartisanx.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.R as UiR
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachments
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachmentPreview
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageField
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageAvatar
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageBubble
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageDeliveryRow
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageTimestamp
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuItem

@Composable
internal fun MessagesDemo(onBack: () -> Unit) {
    var openThread by rememberSaveable { mutableStateOf<String?>(null) }
    val thread = openThread
    if (thread != null) {
        MessageThreadDemo(thread, onBack = { openThread = null })
    } else {
        SmartisanScaffold(titleBar = { MessagesTitleBar("短信", onBack) }) {
            Column {
                SmartisanListItem(
                    title = "小锤",
                    summary = "明天下午三点见。",
                    leading = { SmartisanMessageAvatar() },
                    trailing = { SmartisanIcon(res = SmartisanDrawables.ListItemArrow, contentDescription = null) },
                    onClick = { openThread = "小锤" },
                )
                SmartisanListItem(
                    title = "周末小组",
                    summary = "大家周六见。",
                    leading = { SmartisanMessageAvatar(group = true) },
                    trailing = { SmartisanIcon(res = SmartisanDrawables.ListItemArrow, contentDescription = null) },
                    onClick = { openThread = "周末小组" },
                )
            }
        }
    }
}

private enum class MessageMenu { Camera, QuickReply, Contact }
private const val ExamplePhoto = "示例照片"
private data class DemoMessage(val text: String, val attachment: String? = null)

@Composable
private fun MessageThreadDemo(recipient: String, onBack: () -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable(stateSaver = listSaver<List<DemoMessage>, String>(
        save = { messages -> messages.flatMap { listOf(it.text, it.attachment.orEmpty()) } },
        restore = { fields ->
            fields.chunked(2).map { DemoMessage(it.first(), it.getOrNull(1)?.takeIf(String::isNotEmpty)) }
        },
    )) { mutableStateOf(listOf(DemoMessage("好，我会提前到。"))) }
    var showDelivery by rememberSaveable { mutableStateOf(false) }
    var showAttachments by rememberSaveable { mutableStateOf(false) }
    var menu by rememberSaveable { mutableStateOf<MessageMenu?>(null) }
    var pendingAttachment by rememberSaveable { mutableStateOf<String?>(null) }
    val focus = LocalFocusManager.current
    val editorFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val colors = LocalSmartisanColors.current
    val send = {
        val message = DemoMessage(draft.trim(), pendingAttachment)
        if (message.text.isNotEmpty() || message.attachment != null) {
            sent = sent + message
            draft = ""
            pendingAttachment = null
        }
    }
    BackHandler {
        if (showAttachments) showAttachments = false else onBack()
    }
    LaunchedEffect(sent.size, showAttachments) {
        if (sent.size > 1 || showAttachments) listState.scrollToItem(sent.size)
    }
    SmartisanScaffold(
        modifier = Modifier.imePadding(),
        titleBar = { MessagesTitleBar(recipient, onBack) },
        bottomBar = {
            Column(Modifier.fillMaxWidth().background(colors.surface).navigationBarsPadding()) {
                pendingAttachment?.let { attachment ->
                    DemoAttachment(attachment, onRemove = { pendingAttachment = null })
                }
                SmartisanMessageField(
                    value = draft,
                    onValueChange = {
                        if (it.isNotEmpty()) showAttachments = false
                        draft = it
                    },
                    onSend = send,
                    hint = "输入短信",
                    leftIconRes = if (showAttachments) UiR.drawable.mms_keyboard_selector else UiR.drawable.mms_more_selector,
                    onLeftIconClick = {
                        if (showAttachments) {
                            showAttachments = false
                            editorFocus.requestFocus()
                            keyboard?.show()
                        } else {
                            focus.clearFocus()
                            keyboard?.hide()
                            showAttachments = true
                        }
                    },
                    maxLines = 4,
                    keyboardActions = KeyboardActions(onSend = { send() }),
                    sendIconRes = UiR.drawable.mms_send_selector,
                    backgroundRes = if (colors.isLight) UiR.drawable.mms_compose_inputbox else SmartisanDrawables.MessageFieldBackground,
                    iconEdgePadding = 18.dp,
                    fieldSideSpacing = 3.33.dp,
                    sendEnabled = draft.isNotBlank() || pendingAttachment != null,
                    editorModifier = Modifier.focusRequester(editorFocus),
                    leftIconDescription = if (showAttachments) "键盘" else "更多附件",
                )
                if (showAttachments) {
                    SmartisanMessageAttachments(
                        onEmoji = { if (draft.length + it.length <= 2000) draft += it },
                        onCamera = { menu = MessageMenu.Camera },
                        onQuickReply = { menu = MessageMenu.QuickReply },
                        onContact = { menu = MessageMenu.Contact },
                        onDelete = { if (draft.isNotEmpty()) draft = draft.substring(0, draft.offsetByCodePoints(draft.length, -1)) },
                        cameraLabel = "相机",
                        quickReplyLabel = "快捷回复",
                        contactLabel = "分享联系人",
                        deleteLabel = "删除",
                    )
                }
            }
        },
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            item {
                SmartisanMessageTimestamp("今天 14:28")
                SmartisanMessageBubble(
                    text = if (recipient == "小锤") "明天下午三点见。" else "大家周六见。",
                    outgoing = false,
                    avatar = { SmartisanMessageAvatar(contentDescription = recipient, group = recipient == "周末小组") },
                )
            }
            items(sent) { message ->
                val attachment = message.attachment
                SmartisanMessageBubble(
                    text = message.text,
                    outgoing = true,
                    onClick = { showDelivery = true },
                    avatar = { SmartisanMessageAvatar(contentDescription = "我") },
                    attachment = if (attachment == null) null else { { DemoAttachment(attachment) } },
                )
            }
        }
    }

    if (showDelivery) {
        SmartisanDialog(
            title = "送达报告",
            onDismissRequest = { showDelivery = false },
            onConfirm = { showDelivery = false },
        ) {
            SmartisanMessageDeliveryRow(recipient, timestamp = "今天 14:28", status = "示例送达")
        }
    }
    val replyMenu = menu
    if (replyMenu != null) {
        val options = when (replyMenu) {
            MessageMenu.Camera -> listOf("添加示例照片")
            MessageMenu.QuickReply -> listOf("现在不方便接听。", "我稍后回复你。", "我马上到。")
            MessageMenu.Contact -> listOf("小锤", "小星")
        }
        SmartisanMenuDialog(
            title = when (replyMenu) {
                MessageMenu.Camera -> "相机"
                MessageMenu.QuickReply -> "快捷回复"
                MessageMenu.Contact -> "分享联系人"
            },
            onDismissRequest = { menu = null },
        ) {
            options.forEachIndexed { index, option ->
                SmartisanMenuItem(option, showDivider = index > 0, onClick = {
                    when (replyMenu) {
                        MessageMenu.Camera -> pendingAttachment = ExamplePhoto
                        MessageMenu.QuickReply -> draft = option
                        MessageMenu.Contact -> pendingAttachment = "联系人：$option"
                    }
                    menu = null
                })
            }
        }
    }
}

@Composable
private fun MessagesTitleBar(title: String, onBack: () -> Unit) {
    SmartisanTitleBar(
        title = title,
        navigationIcon = SmartisanTitleBarAction(
            iconRes = SmartisanDrawables.IconBack,
            contentDescription = "返回",
            onClick = onBack,
        ),
    )
}


@Composable
private fun DemoAttachment(label: String, onRemove: (() -> Unit)? = null) {
    SmartisanMessageAttachmentPreview(
        label = label,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        photo = if (label == ExamplePhoto) painterResource(R.drawable.about_logo) else null,
        onRemove = onRemove,
        removeDescription = "移除附件",
    )
}
