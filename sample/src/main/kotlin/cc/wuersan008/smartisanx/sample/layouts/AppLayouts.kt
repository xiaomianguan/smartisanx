package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.layout.LocalSmartisanContentBottomInset
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanLetterSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterIndexBar
import kotlinx.coroutines.launch

/** 联系人列表里的两种条目（组标题 / 联系人）。 */
private sealed interface ContactRow {
    data class Section(val letter: Char) : ContactRow

    data class Person(val contact: SampleContact) : ContactRow
}

/**
 * 锤子联系人「通讯录」页。
 *
 * 对应原版 `ContactsSmartisan.apk` 的 `contact_list_content.xml` + `contact_list_item.xml`：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 顶部账号筛选条 | `account_filter_header.xml`（示例里省略，它是多账号场景才需要的） |
 * | 字母分组列表 | `PinnedHeaderListView` + `contact_list_item.xml`，行高 `list_item_height` = 60dp |
 * | 头像 | `AvatarImageView`，在行左侧 `list_item_left_width` 的方形区里居中 |
 * | 右侧 A–Z 索引条 | `quickbar_left_letters_bar`（74px = 29.6dp） |
 *
 * 头像在示例里用 [SampleAvatar] 画（主题底色 + 首字），原版显示的是联系人照片。
 */
@Composable
fun ContactsLayout(onBack: () -> Unit) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val rows =
        remember {
            buildList {
                SampleContactSections.forEach { (letter, people) ->
                    add(ContactRow.Section(letter))
                    people.forEach { add(ContactRow.Person(it)) }
                }
            }
        }
    val letterIndex =
        remember {
            rows.withIndex()
                .filter { it.value is ContactRow.Section }
                .associate { (index, row) -> (row as ContactRow.Section).letter to index }
        }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        // 内容会滚动：内容区一直画到屏幕最底下，导航栏留白补在滚动内容的末尾。
        contentScrollable = true,
        titleBar = {
            SmartisanTitleBar(
                title = "联系人",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Search,
                            contentDescription = "搜索联系人",
                            onClick = {},
                        ),
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.AddTitleBar,
                            contentDescription = "新建联系人",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // 列表可以滚到透明导航栏底下，末尾这条留白让最后一行还能整个滚上来。
                contentPadding = PaddingValues(bottom = LocalSmartisanContentBottomInset.current),
            ) {
                items(rows) { row ->
                    when (row) {
                        is ContactRow.Section ->
                            SmartisanLetterSectionTitle(text = row.letter.toString())
                        is ContactRow.Person ->
                            SmartisanListRow(
                                title = row.contact.name,
                                summary = row.contact.phone,
                                leading = {
                                    // 原版联系人头像 `listview_photo_width/height` 是 38dp，
                                    // 居中放在 60dp 的 `list_item_left_width` 里。
                                    SampleAvatar(text = row.contact.name.take(1), size = 38.dp)
                                },
                                showDivider = true,
                                onClick = {},
                            )
                    }
                }
            }
            SmartisanLetterIndexBar(
                letters = letterIndex.keys.toList(),
                onLetterSelected = { letter ->
                    letterIndex[letter]?.let { index ->
                        scope.launch { listState.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

/**
 * 锤子笔记「全部笔记」页。
 *
 * 对应原版 `NotesSmartisan.apk` 的 `fragment_list.xml` + `extend_list_note_item.xml`：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 列表 | `NotesListView`（支持拖拽排序，示例里只用普通列表） |
 * | 行内容 | `extend_list_note_item.xml`：左侧 30dp 缩进，第一行是时间（`extend_list_item_time_text_size`），第二行是正文（`extend_list_item_title_text_size`） |
 * | 收藏标记 | `extend_note_list_item_star` / `_unstar` 位图，右对齐 |
 * | 空态 | `notes_list_empty_smartisanos`，见「空态页」那张布局 |
 *
 * 原版行是「时间在上、正文在下」，这里用分组行的「标题 + 摘要」两行表达：
 * 标题是笔记的第一行，摘要是「时间 · 正文预览」。
 */
@Composable
fun NotesLayout(onBack: () -> Unit) {
    var notes by remember { mutableStateOf(SampleNotes) }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        // 内容会滚动：内容区一直画到屏幕最底下，导航栏留白补在滚动内容的末尾。
        contentScrollable = true,
        titleBar = {
            SmartisanTitleBar(
                title = "全部笔记",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Search,
                            contentDescription = "搜索笔记",
                            onClick = {},
                        ),
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.AddTitleBar,
                            contentDescription = "新建笔记",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // 留白加在滚动修饰符之后：它属于滚动内容（列表能滚到导航栏底下，
                    // 滚到底时最后一行还在导航栏之上）。
                    .padding(bottom = LocalSmartisanContentBottomInset.current),
        ) {
            SmartisanGroup {
                notes.forEachIndexed { index, note ->
                    SmartisanGroupItem(
                        position =
                            when (index) {
                                0 -> SmartisanGroupRowPosition.Top
                                notes.lastIndex -> SmartisanGroupRowPosition.Bottom
                                else -> SmartisanGroupRowPosition.Middle
                            },
                        title = note.title,
                        summary = "${note.time} · ${note.summary}",
                        trailing = {
                            if (note.starred) {
                                // 原版的收藏星标位图。
                                SmartisanIcon(
                                    res = R.drawable.extend_note_list_item_star,
                                    contentDescription = "已收藏",
                                    size = 20.dp,
                                )
                            }
                        },
                        onClick = {
                            // 点一下切换收藏，能看出行状态是活的。
                            notes =
                                notes.mapIndexed { i, item ->
                                    if (i == index) item.copy(starred = !item.starred) else item
                                }
                        },
                    )
                }
            }
            SmartisanListVerticalGap()
        }
    }
}
