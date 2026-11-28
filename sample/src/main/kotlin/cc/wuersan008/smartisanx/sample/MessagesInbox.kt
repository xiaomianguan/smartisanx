package cc.wuersan008.smartisanx.sample

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.R as UiR
import cc.wuersan008.smartisanx.ui.anim.SmartisanDecelerateCubic
import cc.wuersan008.smartisanx.ui.anim.SmartisanNavigationOpenEasing
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.input.SmartisanSearchBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarShadow
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageAvatar
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog
import kotlin.math.roundToInt

private enum class InboxCategory(val label: String) {
    All("全部"), Notifications("通知"), Personal("个人"),
}

internal data class InboxConversation(
    val id: String,
    val title: String,
    val messages: List<String>,
    val notification: Boolean = false,
    val group: Boolean = false,
)

internal val DemoInbox = listOf(
    InboxConversation("hammer", "小锤", listOf("地点还是图书馆。", "明天下午三点见。")),
    InboxConversation("delivery", "快递通知", listOf("包裹已送达，取件码 4826。"), notification = true),
    InboxConversation("weekend", "周末小组", listOf("大家周六见。"), group = true),
    InboxConversation("carrier", "运营商", listOf("本月账单已出，请查收。"), notification = true),
)

private val ConversationIdsSaver = listSaver<Set<String>, String>(
    save = { it.toList() },
    restore = { it.toSet() },
)

@Composable
internal fun MessagesInbox(
    active: Boolean,
    onBack: () -> Unit,
    onCompose: () -> Unit,
    onOpenThread: (InboxConversation) -> Unit,
) {
    var category by rememberSaveable { mutableStateOf(InboxCategory.All) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var managing by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var removed by rememberSaveable(stateSaver = ConversationIdsSaver) { mutableStateOf(emptySet()) }
    var unread by rememberSaveable(stateSaver = ConversationIdsSaver) { mutableStateOf(setOf("delivery", "weekend")) }
    var selected by rememberSaveable(stateSaver = ConversationIdsSaver) { mutableStateOf(emptySet()) }
    val editTransition = updateTransition(managing, label = "Messages management")
    val editHeader = editTransition.animateFloat(
        transitionSpec = { tween(200, easing = SmartisanNavigationOpenEasing) },
        label = "Selection header",
    ) { if (it) 1f else 0f }
    val editFooter = editTransition.animateFloat(
        transitionSpec = { tween(200, easing = SmartisanDecelerateCubic) },
        label = "Batch actions",
    ) { if (it) 1f else 0f }
    // 原版先缩小并淡出原来的图标，再用相同的动画展开替代图标。
    val avatarVisibility = editTransition.animateFloat(
        transitionSpec = { tween(150, delayMillis = if (targetState) 0 else 150, easing = SmartisanDecelerateCubic) },
        label = "Conversation avatar",
    ) { if (it) 0f else 1f }
    val checkboxVisibility = editTransition.animateFloat(
        transitionSpec = { tween(150, delayMillis = if (targetState) 150 else 0, easing = SmartisanDecelerateCubic) },
        label = "Conversation checkbox",
    ) { if (it) 1f else 0f }
    val arrowOffset = editTransition.animateFloat(
        transitionSpec = { tween(150, easing = SmartisanDecelerateCubic) },
        label = "Conversation arrow",
    ) { if (it) 1f else 0f }
    val changingMode = editTransition.currentState != managing || editTransition.isRunning
    val showEditControls = managing || editTransition.currentState
    val colors = LocalSmartisanColors.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val term = query.trim()
    val visible = DemoInbox.filter { conversation ->
        conversation.id !in removed &&
            (category == InboxCategory.All || conversation.notification == (category == InboxCategory.Notifications)) &&
            (term.isEmpty() || conversation.title.contains(term, ignoreCase = true) ||
                conversation.messages.any { it.contains(term, ignoreCase = true) })
    }
    val allSelected = visible.isNotEmpty() && selected.size == visible.size
    val markRead = selected.any { it in unread }
    val dismissSearch = {
        focus.clearFocus()
        keyboard?.hide()
        searching = false
        query = ""
    }
    val stopManaging = {
        managing = false
        selected = emptySet()
    }
    BackHandler(enabled = active) {
        when {
            changingMode -> Unit
            managing -> stopManaging()
            searching -> dismissSearch()
            else -> onBack()
        }
    }
    SmartisanScaffold(
        modifier = Modifier.imePadding(),
        backgroundRes = UiR.drawable.conversation_listview_background_tilemode.takeIf { colors.isLight },
        titleBar = {
            Column(Modifier.background(colors.titleBarBackground).statusBarsPadding()) {
                AnimatedVisibility(
                    visible = !searching,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    SmartisanTitleBar(
                        title = "短信",
                        navigationIcon = if (managing) {
                            SmartisanTitleBarAction(SmartisanDrawables.IconCancel, "取消管理", { if (!changingMode) stopManaging() })
                        } else {
                            SmartisanTitleBarAction(SmartisanDrawables.IconBack, "返回", { if (!changingMode) onBack() })
                        },
                        navigationActions = if (managing) emptyList() else listOf(
                            SmartisanTitleBarAction(
                                SmartisanDrawables.IconMultiSelect, "管理短信", { if (!changingMode) managing = true },
                                enabled = visible.isNotEmpty(),
                            ),
                        ),
                        action = if (managing) null else SmartisanTitleBarAction(
                            UiR.drawable.mms_compose_selector, "新建短信", { if (!changingMode) onCompose() },
                        ),
                        includeStatusBar = false,
                        showShadow = false,
                        backgroundRes = null,
                    )
                }
                Box(Modifier.fillMaxWidth().height(48.dp).clipToBounds()) {
                    SmartisanSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        expanded = searching,
                        onExpandedChange = { if (!changingMode) searching = it },
                        onSearchIconClick = { if (!changingMode) searching = true },
                        onCancel = dismissSearch,
                        showShadow = false,
                        enabled = !managing,
                        fieldHeight = 31.dp,
                        modifier = Modifier.semantics { contentDescription = "搜索短信" },
                    )
                    if (!searching && colors.isLight) MessagesDivider()
                    if (showEditControls) {
                        Box(
                            Modifier.fillMaxWidth().graphicsLayer {
                                translationY = (editHeader.value - 1f) * size.height
                            }.background(colors.titleBarBackground),
                        ) {
                            Row(
                                Modifier.fillMaxWidth().height(48.dp).padding(start = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val selectLabel = if (allSelected) "取消全选" else "全选"
                                SmartisanCheckbox(
                                    checked = allSelected,
                                    onCheckedChange = if (managing && !changingMode) ({
                                        selected = if (allSelected) emptySet() else visible.map { it.id }.toSet()
                                    }) else null,
                                    modifier = Modifier.semantics { contentDescription = selectLabel },
                                    markSize = 36.dp,
                                )
                                Box(Modifier.width(11.2.dp))
                                Box(Modifier.width(0.67.dp).height(18.67.dp).background(colors.divider))
                                SmartisanText(
                                    "已选择（${selected.size}/${visible.size}）",
                                    modifier = Modifier.padding(start = 11.dp),
                                    fontSize = 13.5.sp,
                                    color = colors.textSecondary,
                                )
                            }
                            if (colors.isLight) MessagesDivider()
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    SmartisanButtonTabGroup(
                        items = InboxCategory.entries.map { it.label },
                        selectedIndex = category.ordinal,
                        onSelectedChange = { if (!changingMode) category = InboxCategory.entries[it] },
                        disabledIndices = if (managing) InboxCategory.entries.indices.toSet() else emptySet(),
                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 6.dp),
                    )
                    if (colors.isLight) MessagesDivider(Modifier.align(Alignment.TopCenter))
                }
            }
        },
        bottomBar = {
            Box(Modifier.navigationBarsPadding()) {
                if (showEditControls) Box(
                    Modifier.clipToBounds().layout { measurable, constraints ->
                        val footer = measurable.measure(constraints)
                        val height = constraints.constrainHeight((footer.height * editFooter.value).roundToInt())
                        layout(footer.width, height) { footer.place(0, 0) }
                    }.background(colors.surface),
                ) {
                    if (colors.isLight) MessagesDivider()
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SmartisanButton(
                            text = "删除",
                            onClick = { confirmDelete = true },
                            enabled = managing && !changingMode && selected.isNotEmpty(),
                            style = SmartisanButtonStyle.Destructive,
                            backgroundRes = UiR.drawable.selector_small_btn_highlight_red,
                            showShadow = false,
                            modifier = Modifier.weight(1f).height(42.dp).alpha(if (selected.isEmpty()) 0.3f else 1f),
                        )
                        SmartisanButton(
                            text = if (markRead || selected.isEmpty()) "标为已读" else "标为未读",
                            onClick = {
                                unread = if (markRead) unread - selected else unread + selected
                                stopManaging()
                            },
                            enabled = managing && !changingMode && selected.isNotEmpty(),
                            style = SmartisanButtonStyle.Neutral,
                            backgroundRes = UiR.drawable.selector_small_btn_standard,
                            showShadow = false,
                            modifier = Modifier.weight(1f).height(42.dp).alpha(if (selected.isEmpty()) 0.3f else 1f),
                        )
                    }
                }
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SmartisanText(
                        if (searching && term.isNotEmpty()) "没有匹配的短信" else "没有短信",
                        color = colors.textSecondary,
                    )
                }
            } else {
                LazyColumn {
                    items(visible, key = { it.id }) { conversation ->
                        val checked = conversation.id in selected
                        val toggle = {
                            selected = if (checked) selected - conversation.id else selected + conversation.id
                        }
                        val preview = conversation.messages.lastOrNull { term.isNotEmpty() && it.contains(term, true) }
                            ?: conversation.messages.last()
                        SmartisanListItem(
                            title = conversation.title,
                            summary = preview,
                            leading = {
                                // 原版 conversation_list_item_content.xml：头像在 60dp 的左槽里居中，
                                // 文字列 layout_marginLeft = 76dp —— 竖分隔线（行底图里）和文字之间留 16dp。
                                Box(
                                    Modifier.width(ConversationTextStart).height(130.dp / 3),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Box(
                                        Modifier.width(60.dp).height(130.dp / 3),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (!managing || changingMode) {
                                            SmartisanMessageAvatar(
                                                modifier = Modifier.graphicsLayer {
                                                    val visibility = avatarVisibility.value
                                                    alpha = visibility
                                                    scaleX = 0.1f + 0.9f * visibility
                                                    scaleY = scaleX
                                                },
                                                group = conversation.group,
                                            )
                                        }
                                        if (showEditControls) {
                                            SmartisanCheckbox(
                                                checked = checked,
                                                onCheckedChange = if (managing && !changingMode) ({ toggle() }) else null,
                                                modifier = Modifier.graphicsLayer {
                                                    val visibility = checkboxVisibility.value
                                                    alpha = visibility
                                                    scaleX = 0.1f + 0.9f * visibility
                                                    scaleY = scaleX
                                                }.semantics { contentDescription = conversation.title },
                                                markSize = 36.dp,
                                            )
                                        }
                                    }
                                }
                            },
                            trailing = {
                                if (conversation.id in unread) {
                                    SmartisanText(
                                        "未读", modifier = Modifier.padding(end = 6.dp),
                                        fontSize = 12.sp, color = colors.accent,
                                    )
                                }
                                SmartisanIcon(
                                    res = UiR.drawable.conversation_arrow,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp).width(6.dp).height(10.dp)
                                        .graphicsLayer { translationX = 20.dp.toPx() * arrowOffset.value },
                                    tint = if (colors.isLight) Color.Unspecified else colors.textTertiary,
                                )
                            },
                            minHeight = 60.dp,
                            showDivider = true,
                            rowBackgroundRes = UiR.drawable.conversation_list_item_background_selector.takeIf { colors.isLight },
                            // 原版会话行：联系人 16sp、摘要 13.5sp（conversation_list_item_content.xml）。
                            titleStyle = LocalSmartisanTypography.current.listRowPrimary.copy(fontSize = 16.sp),
                            summaryStyle = LocalSmartisanTypography.current.listItemSecondary.copy(fontSize = 13.5.sp),
                            onClick = {
                                if (changingMode) return@SmartisanListItem
                                if (managing) toggle() else {
                                    unread = unread - conversation.id
                                    focus.clearFocus()
                                    keyboard?.hide()
                                    onOpenThread(conversation)
                                }
                            },
                        )
                    }
                }
            }
            SmartisanTitleBarShadow(
                height = (46f / 3).dp,
                shadowRes = SmartisanDrawables.SecondaryBarShadow,
            )
            if (colors.isLight) MessagesDivider()
        }
    }
    if (confirmDelete) {
        SmartisanDialog(
            title = "删除所选短信？",
            confirmText = "删除",
            onDismissRequest = { confirmDelete = false },
            onConfirm = {
                removed = removed + selected
                unread = unread - selected
                stopManaging()
            },
        ) {
            SmartisanText("将删除 ${selected.size} 个会话。")
        }
    }
}

/** 原版会话行文字列的左边距（`layout_marginLeft = 76dp`）。 */
private val ConversationTextStart = 76.dp
