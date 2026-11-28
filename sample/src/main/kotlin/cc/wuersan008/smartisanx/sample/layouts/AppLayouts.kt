package cc.wuersan008.smartisanx.sample.layouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroupItem
import cc.wuersan008.smartisanx.ui.input.SmartisanSearchBar
import cc.wuersan008.smartisanx.ui.layout.LocalSmartisanContentBottomInset
import cc.wuersan008.smartisanx.ui.layout.SmartisanBarDivider
import cc.wuersan008.smartisanx.ui.layout.SmartisanBarShadow
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomTabBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanListSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterSymbol
import cc.wuersan008.smartisanx.ui.list.SmartisanQuickBar
import cc.wuersan008.smartisanx.ui.list.rememberSmartisanQuickBarState
import cc.wuersan008.smartisanx.ui.list.smartisanDefaultLetterIndex
import kotlinx.coroutines.launch

/** 联系人字母栏最上面的「我」：常态 / 持有态照抄联系人 APK，按下时着白。 */
private val ContactsMeSymbol =
    SmartisanLetterSymbol(
        key = '@',
        iconRes = SmartisanDrawables.QuickBarMe,
        holderIconRes = SmartisanDrawables.QuickBarMeHolder,
    )

/** 联系人列表里的两种条目（组标题 / 联系人）。 */
private sealed interface ContactRow {
    data class Section(val letter: Char) : ContactRow

    data class Person(val contact: SampleContact) : ContactRow
}

/**
 * 锤子联系人「通讯录」页（R2 `ContactsSmartisan` 的 `DialtactsActivity` → `contact_city_fragment.xml`）。
 *
 * | 本页 | 原版出处（R2 400dpi 实测） |
 * | --- | --- |
 * | 白色标题栏：左多选、中「群组 / 全部」分段、右「＋」 | `smartisanos.widget.TitleBar`；分段各 200 × 120px（80 × 48dp），图标槽 90px |
 * | 标题栏下 48dp 搜索行，右侧排序按钮 | `ShadowRelativeLayout` + `smartisanos.widget.SearchBar`（`rightIcon = standard_icon_sorting_selector`） |
 * | 「我」分组 +「设置我的个人资料」 | `user_profile_contacts_list_header` / `profile_display_name`；文字左沿 30px（12dp） |
 * | 字母分组列表，行只有头像和名字 | `contact_list_item.xml`，行高 150px（60dp），名字左沿 152px |
 * | 右侧索引：「我」+ A–Z + #，往左拖出九宫格 | `QuickBarEx`，在搜索行下方 → [SmartisanQuickBar] |
 * | 底部四个标签：通话记录 / 联系人 / 拨号 / 收藏 | framework `BottomBar`（可缩放样式）→ [SmartisanBottomTabBar] |
 *
 * 头像在示例里用 [SampleAvatar] 画（主题底色 + 首字），原版显示的是联系人照片；
 * 「我」那一行右侧运营商的「充值」按钮属于话费服务，示例里省略。
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
    // 列表前面还有「我」分组的两行（组标题 + 个人资料行）。
    val headerRows = 2
    val letterIndex =
        remember {
            rows.withIndex()
                .filter { it.value is ContactRow.Section }
                .associate { (index, row) -> (row as ContactRow.Section).letter to index + headerRows }
        }
    var filter by rememberSaveable { mutableIntStateOf(1) }
    var tab by rememberSaveable { mutableIntStateOf(1) }
    var query by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    val quickBar = rememberSmartisanQuickBarState()
    // 返回键：九宫格展开时先收起，否则退出示例页。
    BackHandler {
        if (quickBar.isGridHidden) onBack() else scope.launch { quickBar.hideGrid() }
    }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "联系人",
                // R2 系统应用的标题栏是纯白（framework TitleBar），不是早期应用的渐变底图；
                // 它的投影被下面的搜索行盖住，只露出 0.67dp 分隔线（画在搜索行顶上）。
                backgroundRes = null,
                showShadow = false,
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.MultiSelect,
                        contentDescription = "多选",
                        onClick = {},
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanDrawables.IconAdd,
                            contentDescription = "新建联系人",
                            onClick = {},
                        ),
                    ),
                centerContent = {
                    SmartisanButtonTabGroup(
                        items = listOf(SmartisanButtonTabGroupItem("群组"), SmartisanButtonTabGroupItem("全部")),
                        selectedIndex = filter,
                        onSelectedChange = { filter = it },
                        itemWidth = 80.dp,
                    )
                },
            )
        },
        bottomBar = {
            SmartisanBottomTabBar(
                items = ContactsTabs,
                selectedIndex = tab,
                onSelected = { tab = it },
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            // 原版搜索行：白底 48dp，右侧是 framework 的排序按钮；投影由 ShadowRelativeLayout 画在下方列表上。
            Box(Modifier.fillMaxWidth().background(LocalSmartisanColors.current.surface)) {
                SmartisanSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    expanded = searching,
                    onExpandedChange = { searching = it },
                    onCancel = {
                        query = ""
                        searching = false
                    },
                    filterIconRes = SmartisanDrawables.IconSort,
                    showShadow = false,
                )
                SmartisanBarDivider()
            }
            Box(Modifier.fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    item { SmartisanListSectionTitle(text = "我") }
                    item {
                        // 原版 contact_list_item_profile：没有头像，文字从 12dp 开始。
                        Box(
                            Modifier.fillMaxWidth().height(SmartisanDimens.ListItemHeight)
                                .background(LocalSmartisanColors.current.surface)
                                .padding(start = SmartisanDimens.ListItemHorizontalMargin),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            SmartisanText(
                                text = "设置我的个人资料",
                                style = LocalSmartisanTypography.current.listRowPrimary,
                                color = LocalSmartisanColors.current.textPrimary,
                            )
                        }
                    }
                    items(rows) { row ->
                        when (row) {
                            is ContactRow.Section ->
                                SmartisanListSectionTitle(text = row.letter.toString())
                            is ContactRow.Person ->
                                // 原版通讯录行只有头像和名字（`contact_list_item.xml` 没有号码行）。
                                SmartisanListRow(
                                    title = row.contact.name,
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
                // 原版 QuickBarEx：字母栏一直是完整的「我」+ A–Z + #，没有联系人的字母停在最近的分组；
                // 往左拖出 A–Z 九宫格。
                SmartisanQuickBar(
                    letters = smartisanDefaultLetterIndex(),
                    symbols = listOf(ContactsMeSymbol),
                    onLetterSelected = { letter ->
                        val index =
                            if (letter == ContactsMeSymbol.key) {
                                0
                            } else {
                                letterIndex.entries.lastOrNull { it.key <= letter }?.value ?: 0
                            }
                        scope.launch { listState.scrollToItem(index) }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd),
                    state = quickBar,
                )
                SmartisanBarShadow(Modifier.align(Alignment.TopStart))
            }
        }
    }
}

/** R2 联系人底栏四个标签：图标是联系人 APK 的 30dp 矢量图，文字取自 `dialtacts_tab_*` 中文。 */
private val ContactsTabs =
    listOf(
        SmartisanBottomBarItem(label = "通话记录", iconRes = SmartisanDrawables.BottomTabRecent),
        SmartisanBottomBarItem(label = "联系人", iconRes = SmartisanDrawables.BottomTabContacts),
        SmartisanBottomBarItem(label = "拨号", iconRes = SmartisanDrawables.BottomTabKeypad),
        SmartisanBottomBarItem(label = "收藏", iconRes = SmartisanDrawables.BottomTabFavorite),
    )

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
