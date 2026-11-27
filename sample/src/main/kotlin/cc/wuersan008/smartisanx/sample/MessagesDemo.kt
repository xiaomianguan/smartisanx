package cc.wuersan008.smartisanx.sample

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.R as UiR
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.input.SmartisanShadedField
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachments
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachmentPreview
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageField
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageRecipients
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageAvatar
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageBubble
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageDeliveryRow
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageQuickReplyRow
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageTimestamp
import cc.wuersan008.smartisanx.ui.list.SmartisanReorderableColumn
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuItem
import kotlin.math.roundToInt

@Composable
internal fun MessagesDemo(onBack: () -> Unit) {
    var openThread by rememberSaveable { mutableStateOf<String?>(null) }
    var composing by rememberSaveable { mutableStateOf(false) }
    var recentEmoji by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(emptyList()) }
    val thread = openThread
    if (thread != null || composing) {
        MessageThreadDemo(
            recipient = thread,
            recentEmoji = recentEmoji,
            onEmojiUsed = { emoji -> recentEmoji = (listOf(emoji) + recentEmoji.filterNot { it == emoji }).take(15) },
            onBack = {
                openThread = null
                composing = false
            },
        )
    } else {
        SmartisanScaffold(titleBar = {
            MessagesTitleBar("短信", onBack, action = SmartisanTitleBarAction(
                UiR.drawable.mms_compose_selector, "新建短信", onClick = { composing = true },
            ))
        }) {
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
private fun MessageThreadDemo(
    recipient: String?,
    recentEmoji: List<String>,
    onEmojiUsed: (String) -> Unit,
    onBack: () -> Unit,
) {
    var address by rememberSaveable { mutableStateOf("") }
    var recipients by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(emptyList()) }
    val destination = recipient ?: recipients.joinToString("、")
    val canAddress = recipient != null || recipients.isNotEmpty()

    var draft by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable(stateSaver = listSaver<List<DemoMessage>, String>(
        save = { messages -> messages.flatMap { listOf(it.text, it.attachment.orEmpty()) } },
        restore = { fields ->
            fields.chunked(2).map { DemoMessage(it.first(), it.getOrNull(1)?.takeIf(String::isNotEmpty)) }
        },
    )) { mutableStateOf(if (recipient == null) emptyList() else listOf(DemoMessage("好，我会提前到。"))) }
    var showDelivery by rememberSaveable { mutableStateOf(false) }
    var showAttachments by rememberSaveable { mutableStateOf(false) }
    var menu by rememberSaveable { mutableStateOf<MessageMenu?>(null) }
    var pendingAttachment by rememberSaveable { mutableStateOf<String?>(null) }
    var replies by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(listOf("我稍后给你打电话。", "现在不方便接听，有什么事？", "我正在路上。")) }
    val focus = LocalFocusManager.current
    val editorFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val colors = LocalSmartisanColors.current
    val send = {
        val message = DemoMessage(draft.trim(), pendingAttachment)
        if (canAddress && (message.text.isNotEmpty() || message.attachment != null)) {
            sent = sent + message
            draft = ""
            pendingAttachment = null
        }
    }
    BackHandler {
        when {
            menu == MessageMenu.QuickReply -> menu = null
            showAttachments -> showAttachments = false
            else -> onBack()
        }
    }
    if (menu == MessageMenu.QuickReply) {
        QuickRepliesDemo(
            replies = replies,
            onRepliesChange = { replies = it },
            onSelect = {
                draft = it
                menu = null
            },
            onBack = { menu = null },
        )
        return
    }
    LaunchedEffect(sent.size, showAttachments) {
        if (sent.isNotEmpty() && (sent.size > 1 || showAttachments || recipient == null)) {
            listState.scrollToItem(1 + sent.lastIndex)
        }
    }
    SmartisanScaffold(
        modifier = Modifier.imePadding(),
        titleBar = { MessagesTitleBar(recipient ?: "新建短信", onBack) },
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
                    sendEnabled = canAddress && (draft.isNotBlank() || pendingAttachment != null),
                    editorModifier = Modifier.focusRequester(editorFocus),
                    leftIconDescription = if (showAttachments) "键盘" else "更多附件",
                )
                if (showAttachments) {
                    SmartisanMessageAttachments(
                        onEmoji = {
                            if (draft.length + it.length <= 2000) {
                                draft += it
                                onEmojiUsed(it)
                            }
                        },
                        onCamera = { menu = MessageMenu.Camera },
                        onQuickReply = { menu = MessageMenu.QuickReply },
                        onContact = { menu = MessageMenu.Contact },
                        onDelete = { if (draft.isNotEmpty()) draft = draft.substring(0, draft.offsetByCodePoints(draft.length, -1)) },
                        cameraLabel = "相机",
                        quickReplyLabel = "快捷回复",
                        contactLabel = "分享联系人",
                        deleteLabel = "删除",
                        recentEmoji = recentEmoji,
                    )
                }
            }
        },
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            item {
                if (recipient == null) {
                    val addRecipient = {
                        val candidate = address.trim()
                        if (candidate.isNotEmpty() && candidate !in recipients) {
                            recipients = recipients + candidate
                            address = ""
                        }
                    }
                    SmartisanMessageRecipients(
                        address, { address = it }, recipients, addRecipient,
                        onRemove = { recipients = recipients - it },
                        label = "收件人",
                        placeholder = "姓名或号码",
                        addDescription = "添加收件人",
                    )
                } else {
                    SmartisanMessageTimestamp("今天 14:28")
                    SmartisanMessageBubble(
                        text = if (recipient == "小锤") "明天下午三点见。" else "大家周六见。",
                        outgoing = false,
                        avatar = { SmartisanMessageAvatar(contentDescription = recipient, group = recipient == "周末小组") },
                    )
                }
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
            SmartisanMessageDeliveryRow(destination, timestamp = "今天 14:28", status = "示例送达")
        }
    }
    val replyMenu = menu
    if (replyMenu != null && replyMenu != MessageMenu.QuickReply) {
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
private fun MessagesTitleBar(title: String, onBack: () -> Unit, action: SmartisanTitleBarAction? = null) {
    SmartisanTitleBar(
        title = title,
        action = action,
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



@Composable
private fun QuickRepliesDemo(
    replies: List<String>,
    onRepliesChange: (List<String>) -> Unit,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    var editIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var text by rememberSaveable { mutableStateOf("") }
    val editing = editIndex
    var editMode by rememberSaveable { mutableStateOf(false) }
    var removingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val slide = remember { Animatable(0f) }
    val collapse = remember { Animatable(0f) }
    LaunchedEffect(removingIndex) {
        val index = removingIndex ?: return@LaunchedEffect
        slide.animateTo(1f, tween(durationMillis = 200, easing = EaseOutCubic))
        collapse.animateTo(1f, tween(durationMillis = 200, easing = EaseOut))
        onRepliesChange(replies.toMutableList().apply { removeAt(index) })
        removingIndex = null
        slide.snapTo(0f)
        collapse.snapTo(0f)
    }
    BackHandler(enabled = editMode) {
        if (removingIndex == null) editMode = false
    }
    SmartisanScaffold(
        backgroundRes = UiR.drawable.conversation_listview_background_tilemode.takeIf {
            LocalSmartisanColors.current.isLight
        },
        titleBar = {
            SmartisanTitleBar(
                title = "编辑",
                backgroundRes = null,
                navigationIcon = if (editMode) null else SmartisanTitleBarAction(
                    SmartisanDrawables.IconCancel, "返回", onBack,
                ),
                action = SmartisanTitleBarAction(
                    if (editMode) SmartisanDrawables.IconConfirm else UiR.drawable.standard_icon_edit_selector,
                    if (editMode) "完成" else "编辑",
                    { if (removingIndex == null) editMode = !editMode },
                ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SmartisanListVerticalGap(height = 15.dp)
            SmartisanReorderableColumn(
                items = replies,
                onMove = { from, to ->
                    onRepliesChange(replies.toMutableList().apply { add(to, removeAt(from)) })
                },
                dragEnabled = editMode && removingIndex == null,
                dragHandleWidth = 42.dp,
            ) { index, reply, _ ->
                SmartisanMessageQuickReplyRow(
                    text = reply,
                    modifier = if (index == removingIndex) {
                        Modifier.clipToBounds().layout { measurable, constraints ->
                            val row = measurable.measure(constraints)
                            val height = constraints.constrainHeight(
                                (row.height * (1f - collapse.value)).roundToInt(),
                            )
                            layout(row.width, height) { row.place(0, 0) }
                        }.graphicsLayer { translationX = size.width * slide.value }
                    } else {
                        Modifier
                    },
                    onClick = { onSelect(reply) },
                    enabled = !editMode,
                    onEdit = if (editMode) ({
                        if (removingIndex == null) {
                            editIndex = index
                            text = reply
                        }
                    }) else null,
                    editDescription = "编辑快捷回复",
                    onRemove = if (editMode) ({
                        if (removingIndex == null) removingIndex = index
                    }) else null,
                    removeDescription = "删除快捷回复",
                    reorderDescription = if (editMode) "拖动排序" else null,
                )
            }
            SmartisanMessageQuickReplyRow(
                text = "添加快捷回复",
                addRow = true,
                enabled = !editMode,
                onClick = {
                    editIndex = replies.size
                    text = ""
                },
            )
        }
    }
    if (editing != null) {
        SmartisanDialog(
            title = if (editing < replies.size) "编辑快捷回复" else "添加快捷回复",
            onDismissRequest = { editIndex = null },
            confirmEnabled = text.isNotBlank(),
            onConfirm = {
                val reply = text.trim()
                if (reply.isNotEmpty()) {
                    onRepliesChange(
                        if (editing < replies.size) replies.mapIndexed { index, value ->
                            if (index == editing) reply else value
                        } else replies + reply,
                    )
                }
                editIndex = null
            },
        ) {
            val focus = remember { FocusRequester() }
            SmartisanShadedField(
                value = text,
                onValueChange = { text = it.take(100) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)
                    .focusRequester(focus),
            )
            LaunchedEffect(focus) { focus.requestFocus() }
        }
    }
}
