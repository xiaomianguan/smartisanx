package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageField
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageBubble
import cc.wuersan008.smartisanx.ui.list.SmartisanMessageTimestamp

@Composable
internal fun MessagesDemo(onBack: () -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    var sent by rememberSaveable(stateSaver = listSaver<List<String>, String>(
        save = { it },
        restore = { it.toList() },
    )) { mutableStateOf(listOf("好，我会提前到。")) }
    val listState = rememberLazyListState()
    val send = {
        if (draft.isNotBlank()) {
            sent = sent + draft.trim()
            draft = ""
        }
    }
    LaunchedEffect(sent.size) {
        if (sent.size > 1) listState.scrollToItem(sent.size)
    }
    SmartisanScaffold(
        modifier = Modifier.imePadding(),
        titleBar = {
            SmartisanTitleBar(
                title = "短信",
                navigationIcon = SmartisanTitleBarAction(
                    iconRes = SmartisanDrawables.IconBack,
                    contentDescription = "返回",
                    onClick = onBack,
                ),
            )
        },
        bottomBar = {
            SmartisanMessageField(
                value = draft,
                onValueChange = { draft = it },
                onSend = send,
                hint = "输入短信",
                leftIconRes = null,
                maxLines = 4,
                keyboardActions = KeyboardActions(onSend = { send() }),
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        ) {
            item {
                SmartisanMessageTimestamp("今天 14:28")
                SmartisanMessageBubble("明天下午三点见。", outgoing = false)
            }
            items(sent) { message ->
                SmartisanMessageBubble(message, outgoing = true)
            }
        }
    }
}
