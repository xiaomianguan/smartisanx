package cc.wuersan008.smartisanx.sample

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R as UiR
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.anim.SmartisanDecelerate
import cc.wuersan008.smartisanx.ui.anim.SmartisanDecelerateCubic
import cc.wuersan008.smartisanx.ui.anim.SmartisanNavigationDuration
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.input.SmartisanShadedField
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachments
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageAttachmentPreview
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageField
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageRecipients
import cc.wuersan008.smartisanx.ui.layout.SmartisanBarDivider
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarShadow
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterIndexBar
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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@Composable
internal fun MessagesDemo(onBack: () -> Unit) {
    var threadId by rememberSaveable { mutableStateOf(DemoInbox.first().id) }
    val inboxState = rememberSaveableStateHolder()
    var threadOpen by rememberSaveable { mutableStateOf(false) }
    var composing by rememberSaveable { mutableStateOf(false) }
    var recentEmoji by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(emptyList()) }
    val recordEmoji: (String) -> Unit = { emoji ->
        recentEmoji = (listOf(emoji) + recentEmoji.filterNot { it == emoji }).take(15)
    }
    Box(Modifier.fillMaxSize().clipToBounds()) {
        AnimatedContent(
            targetState = threadOpen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val direction = if (targetState) 1 else -1
                val timing = tween<IntOffset>(SmartisanNavigationDuration, easing = SmartisanDecelerate)
                (slideInHorizontally(timing) { direction * it } togetherWith
                    slideOutHorizontally(timing) { -direction * it }).apply {
                    targetContentZIndex = direction.toFloat()
                }
            },
            label = "Messages conversation",
        ) { showThread ->
            if (showThread) {
                val thread = DemoInbox.first { it.id == threadId }
                MessageThreadDemo(thread, threadOpen, recentEmoji, recordEmoji) { threadOpen = false }
            } else {
                inboxState.SaveableStateProvider("messages-inbox") {
                    MessagesInbox(
                        active = !threadOpen && !composing,
                        onBack = onBack,
                        onCompose = { composing = true },
                        onOpenThread = {
                            threadId = it.id
                            threadOpen = true
                        },
                    )
                }
            }
        }
        MessagesModalPage(composing) {
            MessageThreadDemo(null, composing, recentEmoji, recordEmoji) { composing = false }
        }
    }
}

@Composable
private fun MessagesModalPage(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { awaitCancellation() },
        enter = slideInVertically(tween(SmartisanNavigationDuration, easing = SmartisanDecelerateCubic)) { it },
        exit = slideOutVertically(tween(SmartisanNavigationDuration, easing = SmartisanDecelerateCubic)) {
            (it * 1.09f).roundToInt()
        },
    ) {
        content()
    }
}

private enum class MessageMenu { Camera, QuickReply, Contact }
private const val ExamplePhoto = "示例照片"
private data class DemoMessage(val text: String, val attachment: String? = null)

@Composable
private fun MessageThreadDemo(
    thread: InboxConversation?,
    active: Boolean,
    recentEmoji: List<String>,
    onEmojiUsed: (String) -> Unit,
    onBack: () -> Unit,
) {
    val recipient = thread?.title
    var address by rememberSaveable { mutableStateOf("") }
    var recipients by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(emptyList()) }
    val destination = recipient ?: recipients.joinToString("、")
    val validAddress = address.isDemoPhoneNumber()
    val canAddress = recipient != null || recipients.isNotEmpty() || validAddress
    var pickingContact by rememberSaveable { mutableStateOf(false) }
    var recipientFocused by remember { mutableStateOf(false) }
    val recipientFocus = remember { FocusRequester() }
    val addRecipient: (String) -> Unit = { candidate ->
        if (candidate !in recipients) recipients = recipients + candidate
        address = ""
    }

    var draft by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable(stateSaver = listSaver<List<DemoMessage>, String>(
        save = { messages -> messages.flatMap { listOf(it.text, it.attachment.orEmpty()) } },
        restore = { fields ->
            fields.chunked(2).map { DemoMessage(it.first(), it.getOrNull(1)?.takeIf(String::isNotEmpty)) }
        },
    )) { mutableStateOf(if (thread?.id == "hammer") listOf(DemoMessage("好，我会提前到。")) else emptyList()) }
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
            if (recipient == null && validAddress) addRecipient(address.trim())
            if (recipientFocused) focus.clearFocus()
            sent = sent + message
            draft = ""
            pendingAttachment = null
        }
    }
    val quickRepliesOpen = menu == MessageMenu.QuickReply
    val navigateBack = {
        focus.clearFocus()
        keyboard?.hide()
        onBack()
    }
    BackHandler(enabled = active) {
        when {
            pickingContact -> pickingContact = false
            quickRepliesOpen -> menu = null
            showAttachments -> showAttachments = false
            else -> navigateBack()
        }
    }
    LaunchedEffect(active, pickingContact) {
        if (active && recipient == null && !pickingContact && recipients.isEmpty()) {
            recipientFocus.requestFocus()
            keyboard?.show()
        }
    }
    LaunchedEffect(sent.size, showAttachments) {
        if (sent.isNotEmpty() && (sent.size > 1 || showAttachments || recipient == null)) {
            listState.scrollToItem(sent.lastIndex + if (recipient == null) 0 else 1)
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds()) {
        SmartisanScaffold(
            modifier = Modifier.imePadding(),
            backgroundRes = UiR.drawable.conversation_listview_background_tilemode.takeIf { colors.isLight },
            titleBar = {
                SmartisanTitleBar(
                    title = recipient ?: "新建短信",
                    backgroundRes = null,
                    showShadow = recipient != null,
                    navigationIcon = SmartisanTitleBarAction(
                        if (recipient == null) SmartisanDrawables.IconCancel else SmartisanDrawables.IconBack,
                        if (recipient == null) "取消" else "返回",
                        navigateBack,
                    ),
                )
            },
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
            Column(Modifier.fillMaxSize()) {
                if (recipient == null) {
                    Box {
                        SmartisanMessageRecipients(
                            address = address,
                            onAddressChange = { address = it },
                            recipients = recipients,
                            onAdd = {
                                if (validAddress) {
                                    addRecipient(address.trim())
                                    editorFocus.requestFocus()
                                }
                            },
                            onRemove = { recipients = recipients - it },
                            label = "接收者",
                            addDescription = "联系人",
                            onPickContact = {
                                focus.clearFocus()
                                keyboard?.hide()
                                pickingContact = true
                            },
                            editorModifier = Modifier.focusRequester(recipientFocus)
                                .onFocusChanged { recipientFocused = it.isFocused },
                        )
                        if (colors.isLight) MessagesDivider()
                    }
                }
                val suggestions = if (recipient == null && recipientFocused) {
                    DemoContacts.filter { it.name.contains(address) || it.number.contains(address) }
                } else {
                    emptyList()
                }
                if (suggestions.isNotEmpty()) {
                    Box(Modifier.weight(1f)) {
                        MessageContactsList(suggestions, recipients) { contact ->
                            if (contact.name in recipients) recipients = recipients - contact.name
                            else addRecipient(contact.name)
                        }
                        if (colors.isLight) {
                            SmartisanTitleBarShadow(height = 15.dp, shadowRes = UiR.drawable.sos_smartisanos_drawable_title_bar_shadow)
                            MessagesDivider()
                        }
                    }
                } else {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(top = if (recipient == null) 0.dp else 16.dp)) {
                        if (thread != null) {
                            item {
                                SmartisanMessageTimestamp("今天 14:28")
                                thread.messages.forEach { message ->
                                    SmartisanMessageBubble(
                                        text = message,
                                        outgoing = false,
                                        avatar = {
                                            SmartisanMessageAvatar(
                                                contentDescription = thread.title,
                                                group = thread.group,
                                            )
                                        },
                                    )
                                }
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
            }
        }

        MessagesModalPage(quickRepliesOpen) {
            QuickRepliesDemo(
                replies = replies,
                onRepliesChange = { replies = it },
                onSelect = {
                    draft = it
                    menu = null
                },
                onBack = { menu = null },
                active = active && quickRepliesOpen,
            )
        }
        MessagesModalPage(pickingContact) {
            MessageContactPicker(recipients, onClose = { pickingContact = false }) {
                recipients = it
                address = ""
                pickingContact = false
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

private data class DemoContact(val section: Char, val name: String, val number: String)

private val DemoContacts = listOf(
    DemoContact('C', "陈晓", "555 0101"),
    DemoContact('L', "李明", "555 0102"),
    DemoContact('W', "王芳", "555 0103"),
    DemoContact('X', "小锤", "555 0104"),
    DemoContact('Z', "周雨", "555 0105"),
)

private fun String.isDemoPhoneNumber(): Boolean =
    count(Char::isDigit) >= 3 && all { it.isDigit() || it in "+- ()" }

@Composable
private fun MessageContactsList(
    contacts: List<DemoContact>,
    selected: List<String>,
    onClick: (DemoContact) -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val state = rememberLazyListState()
    val sections = contacts.groupBy { it.section }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(colors.surface)) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            sections.forEach { (letter, entries) ->
                item(key = letter) {
                    Box(
                        Modifier.fillMaxWidth().height(30.dp)
                            .background(if (colors.isLight) Color(0xFFF5F5F5) else colors.pageBackground)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        SmartisanText(letter.toString(), fontSize = 14.sp, color = colors.textSecondary)
                    }
                }
                items(entries, key = { it.number }) { contact ->
                    val interaction = rememberSmartisanInteractionSource()
                    val pressed by interaction.collectSmartisanPressedAsState()
                    val checked = contact.name in selected
                    val rowBackground = if (colors.isLight) {
                        Modifier.smartisanDrawableBackground(
                            if (pressed) UiR.drawable.compose_phonelist_down else UiR.drawable.compose_phonelist,
                        )
                    } else {
                        Modifier.background(if (pressed) colors.surfacePressed else colors.surface)
                    }
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 60.dp).then(rowBackground)
                            .smartisanClickable(interactionSource = interaction) { onClick(contact) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SmartisanIcon(
                            res = if (checked) UiR.drawable.phone_list_checkbox_checked
                            else UiR.drawable.phone_list_checkbox_normal,
                            contentDescription = if (checked) "已选择" else "未选择",
                            size = 59.dp,
                        )
                        Column {
                            SmartisanText(contact.name, fontSize = 18.sp, color = colors.textPrimary)
                            SmartisanText("${contact.number}  手机", fontSize = 13.5.sp, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
        if (sections.size > 1) {
            SmartisanLetterIndexBar(
                letters = sections.keys.toList(),
                onLetterSelected = { letter ->
                    val index = sections.entries.takeWhile { it.key != letter }.sumOf { it.value.size + 1 }
                    scope.launch { state.scrollToItem(index) }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
internal fun MessagesDivider(modifier: Modifier = Modifier) {
    SmartisanBarDivider(modifier)
}

@Composable
private fun MessageContactPicker(
    recipients: List<String>,
    onClose: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    var selected by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it }, restore = { it.toList() },
    )) { mutableStateOf(recipients) }
    SmartisanScaffold(titleBar = {
        SmartisanTitleBar(
            title = "选择联系人",
            backgroundRes = null,
            navigationIcon = SmartisanTitleBarAction(SmartisanDrawables.IconCancel, "取消", onClose),
            action = SmartisanTitleBarAction(SmartisanDrawables.IconConfirm, "完成", { onConfirm(selected) }),
        )
    }) {
        MessageContactsList(DemoContacts, selected) { contact ->
            selected = if (contact.name in selected) selected - contact.name else selected + contact.name
        }
    }
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
    active: Boolean,
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
    BackHandler(enabled = active && editMode) {
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
