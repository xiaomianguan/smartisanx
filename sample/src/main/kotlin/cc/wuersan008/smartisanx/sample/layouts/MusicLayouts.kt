package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIconButton
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanSmoothSeekBar
import cc.wuersan008.smartisanx.ui.input.SmartisanSearchBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanLetterSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterIndexBar
import kotlinx.coroutines.launch

/** 音乐列表里的两种条目（组标题 / 歌曲），拍平成一个列表方便索引条跳转。 */
private sealed interface MusicLibraryRow {
    data class Section(val letter: Char) : MusicLibraryRow

    data class Track(val song: SampleSong) : MusicLibraryRow
}

/**
 * 锤子音乐「我的音乐」页。
 *
 * 对应原版 `MusicPlayer.apk`（Android 11 darwin 版）的资料库页：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 标题栏 + 搜索 / 更多 | `base_title_layout.xml`，右侧两个 `TitleBar` 动作 |
 * | 搜索态 | 原版用同一个 `SearchBar` 控件在「标题 / 搜索」两态间切换，本页用 [SmartisanSearchBar] 顶掉标题栏 |
 * | 字母分组标题 | `letter_seperater` 底图（[SmartisanLetterSectionTitle]） |
 * | 右侧 A–Z 索引条 | `quickbar_left_letters_bar`（实测 74px = 29.6dp，正是 [SmartisanLetterIndexBar] 的宽度） |
 * | 底部标签栏 | `tabbar_*_selector` + `smartisan_bottom_bar_height`（54dp） |
 *
 * 列表行用 [SmartisanListRow]（原版 `list_content_item_layout.xml` 的母版，行高 60dp）：
 * 一级文字是歌名，二级是「艺术家 · 专辑」，右侧是时长。
 */
@Composable
fun MusicLibraryLayout(onBack: () -> Unit) {
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 拍平成「组标题 + 歌曲」的列表；letterIndex 记录每个字母的下标，供索引条跳转。
    val rows =
        remember {
            buildList {
                SampleSongSections.forEach { (letter, songs) ->
                    add(MusicLibraryRow.Section(letter))
                    songs.forEach { add(MusicLibraryRow.Track(it)) }
                }
            }
        }
    val letterIndex =
        remember {
            rows.withIndex()
                .filter { it.value is MusicLibraryRow.Section }
                .associate { (index, row) -> (row as MusicLibraryRow.Section).letter to index }
        }
    val tabs =
        remember {
            listOf(
                SmartisanBottomBarItem(label = "歌曲", iconRes = SmartisanOriginalIcons.TabSong),
                SmartisanBottomBarItem(label = "专辑", iconRes = SmartisanOriginalIcons.TabAlbum),
                SmartisanBottomBarItem(label = "艺术家", iconRes = SmartisanOriginalIcons.TabArtist),
                SmartisanBottomBarItem(label = "文件夹", iconRes = SmartisanOriginalIcons.TabFolder),
            )
        }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            if (searching) {
                // 原版搜索栏只有 48dp 高、不含状态栏，所以这里补一层标题栏底图 + 状态栏留白。
                Column(
                    Modifier
                        .fillMaxWidth()
                        .smartisanDrawableBackground(SmartisanDrawables.TitleBarBackground),
                ) {
                    Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
                    SmartisanSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        expanded = true,
                        onCancel = {
                            searching = false
                            query = ""
                        },
                        placeholder = "搜索歌曲、专辑、艺术家",
                    )
                }
            } else {
                SmartisanTitleBar(
                    title = "我的音乐",
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
                                contentDescription = "搜索",
                                onClick = { searching = true },
                            ),
                            SmartisanTitleBarAction(
                                iconRes = SmartisanOriginalIcons.More,
                                contentDescription = "更多",
                                onClick = {},
                            ),
                        ),
                )
            }
        },
        bottomBar = {
            SmartisanBottomBar(items = tabs, selectedIndex = tab, onSelected = { tab = it })
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(rows) { row ->
                    when (row) {
                        is MusicLibraryRow.Section ->
                            SmartisanLetterSectionTitle(text = row.letter.toString())
                        is MusicLibraryRow.Track ->
                            SmartisanListRow(
                                title = row.song.title,
                                summary = "${row.song.artist} · ${row.song.album}",
                                trailing = { DurationLabel(row.song.duration) },
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

/** 列表行右侧的时长。 */
@Composable
private fun DurationLabel(text: String) {
    SmartisanText(
        text = text,
        // 右侧要给压在列表上的 A–Z 索引条让位（原版 `quickbar_number_width` = 40dp）。
        modifier = Modifier.padding(start = 6.dp, end = SmartisanDimens.ListIndexContentInset),
        style = LocalSmartisanTypography.current.listRowTertiary,
        color = LocalSmartisanColors.current.textTertiary,
        maxLines = 1,
    )
}

/**
 * 锤子音乐「正在播放」页。
 *
 * 对应原版 `a_playback.xml` + `audio_player.xml` + `ll_controller.xml`：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 页面底色 | `a_playback.xml` 写死 `#ffffffff`，所以这里不用页面底纹（`backgroundRes = null`） |
 * | 最上面的进度条 | `PlaybackSeekBar`（`audio_controlbar_height` = 41dp，左右 `seekbar_padding` = 51.3dp） |
 * | 黑胶转盘 | `playing_lp.png`（1080×1070）+ 封面 `noalbumcover_220.png`（330×330）+ 唱针 9-patch `playing_stylus_lp.9.png` |
 * | 传输控件 | `ll_controller.xml` 的顺序：循环 / 上一首 / 播放 / 下一首 / 随机 |
 * | 次级动作 | 收藏（`playing_btn_favorite_add` / `_cancel`）、歌词、音量、定时关闭 |
 *
 * 唱针尺寸照原版 432dp 宽设备的比例：容器 432×428dp、唱针 73.5×354.7dp
 * （宽 17%、高 83%），上边距 29.2dp、右边距 2.8dp。
 */
@Composable
fun MusicNowPlayingLayout(onBack: () -> Unit) {
    var playing by remember { mutableStateOf(true) }
    var favorite by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0.32f) }
    val song = remember { SampleSongSections.first().second.first() }
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        // 原版播放页是纯白底，不用页面底纹。
        backgroundRes = null,
        containerColor = colors.surface,
        titleBar = {
            SmartisanTitleBar(
                title = "正在播放",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.BackToLibrary,
                        contentDescription = "收起播放页",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.More,
                            contentDescription = "更多",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            // 进度：拖动条 + 两端时间。原版进度条就在最上面（seekbar_padding = 51.3dp）。
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                SmartisanSmoothSeekBar(
                    value = progress,
                    onValueChange = { progress = it },
                    contentDescription = "播放进度",
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SmartisanText("1:24", style = typography.numeric, color = colors.textTertiary)
                    SmartisanText(song.duration, style = typography.numeric, color = colors.textTertiary)
                }
            }
            // 黑胶转盘：转盘本体 + 居中封面 + 右上角的唱针。
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxHeight().aspectRatio(1f)) {
                    SmartisanIcon(
                        res = R.drawable.playing_lp,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                    SmartisanIcon(
                        res = R.drawable.noalbumcover_220,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).fillMaxSize(0.6f),
                        contentScale = ContentScale.Fit,
                    )
                    // 唱针是 9-patch，按「底图」拉伸才不会被拉进透明区。
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.17f)
                            .fillMaxHeight(0.83f)
                            .padding(top = 29.dp, end = 3.dp)
                            .smartisanDrawableBackground(R.drawable.playing_stylus_lp),
                    )
                }
            }
            // 歌曲信息。
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SmartisanText(text = song.title, style = typography.listItemPrimary, color = colors.textPrimary)
                SmartisanText(
                    text = "${song.artist} · ${song.album}",
                    modifier = Modifier.padding(top = 4.dp),
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
            // 传输控件：循环 / 上一首 / 播放 / 下一首 / 随机（原版 ll_controller 的顺序）。
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanIconButton(onClick = {}, size = 48.dp, contentDescription = "列表循环") {
                    SmartisanIcon(res = SmartisanOriginalIcons.RepeatOn, contentDescription = null)
                }
                SmartisanIconButton(onClick = {}, size = 56.dp, contentDescription = "上一首") {
                    SmartisanIcon(res = SmartisanOriginalIcons.PreviousPlain, contentDescription = null)
                }
                SmartisanIconButton(
                    onClick = { playing = !playing },
                    size = 72.dp,
                    contentDescription = if (playing) "暂停" else "播放",
                ) {
                    SmartisanIcon(
                        res =
                            if (playing) {
                                SmartisanOriginalIcons.PausePlain
                            } else {
                                SmartisanOriginalIcons.PlayPlain
                            },
                        contentDescription = null,
                    )
                }
                SmartisanIconButton(onClick = {}, size = 56.dp, contentDescription = "下一首") {
                    SmartisanIcon(res = SmartisanOriginalIcons.NextPlain, contentDescription = null)
                }
                SmartisanIconButton(onClick = {}, size = 48.dp, contentDescription = "随机播放") {
                    SmartisanIcon(res = SmartisanOriginalIcons.ShuffleOff, contentDescription = null)
                }
            }
            // 次级动作：收藏 / 歌词 / 音量 / 定时关闭。
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanIconButton(
                    onClick = { favorite = !favorite },
                    size = 40.dp,
                    contentDescription = "收藏",
                ) {
                    SmartisanIcon(
                        res =
                            if (favorite) {
                                SmartisanOriginalIcons.FavoriteCancelPlain
                            } else {
                                SmartisanOriginalIcons.FavoriteAddPlain
                            },
                        contentDescription = null,
                    )
                }
                SmartisanIconButton(onClick = {}, size = 40.dp, contentDescription = "歌词") {
                    SmartisanIcon(res = SmartisanOriginalIcons.Lyrics, contentDescription = null)
                }
                SmartisanIconButton(onClick = {}, size = 40.dp, contentDescription = "音量") {
                    SmartisanIcon(res = SmartisanOriginalIcons.Volume, contentDescription = null)
                }
                SmartisanIconButton(onClick = {}, size = 40.dp, contentDescription = "定时关闭") {
                    SmartisanIcon(res = SmartisanOriginalIcons.SleepTimer, contentDescription = null)
                }
            }
        }
    }
}
