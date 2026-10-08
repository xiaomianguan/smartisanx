package cc.wuersan008.smartisanx.sample.pages

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import cc.wuersan008.smartisanx.icons.SmartisanXClockIcons
import cc.wuersan008.smartisanx.icons.SmartisanXMediaIcons
import cc.wuersan008.smartisanx.icons.SmartisanXIcons
import cc.wuersan008.smartisanx.icons.SmartisanXStatusIcons
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons

/**
 * 一项**原版图标**：常量名、资源文件名、资源 id。
 *
 * 资源文件名保留原版 APK 里的拼写（例如原版把 highlight 写成了 `hignlight`），
 * 方便和 `library/ui/src/main/res/drawable*` 下的文件一一对照。
 */
data class OriginalIconEntry(
    /** `SmartisanOriginalIcons` 里的常量名。 */
    val name: String,
    /** 原版资源文件名（不含扩展名）。 */
    val resourceName: String,
    /** 资源 id。 */
    @DrawableRes val res: Int,
)

/** 原版图标分组：分组名 + 该组下的图标列表。 */
data class OriginalIconCategory(
    val title: String,
    val icons: List<OriginalIconEntry>,
)

/** 构造一项原版图标，省去重复写 `OriginalIconEntry`。 */
private fun original(name: String, resourceName: String, @DrawableRes res: Int) =
    OriginalIconEntry(name, resourceName, res)

/**
 * 原版图标素材目录（[SmartisanOriginalIcons] 的可视化清单）。
 *
 * 这些全部是 `library/ui/src/main/res/drawable*` 下从原版 APK 还原出来的位图 / selector，
 * 也是组件内部优先使用的图标来源。
 */
val smartisanOriginalIconCatalog: List<OriginalIconCategory> =
    listOf(
        OriginalIconCategory(
            title = "标题栏 / 通用动作（原版素材）",
            icons =
                listOf(
                    original("Back", "standard_icon_back_selector", SmartisanOriginalIcons.Back),
                    original("Close", "standard_icon_cancel_selector", SmartisanOriginalIcons.Close),
                    original("Complete", "standard_icon_complete_selector", SmartisanOriginalIcons.Complete),
                    original(
                        "Confirm",
                        "standard_icon_hignlight_confirm_selector",
                        SmartisanOriginalIcons.Confirm,
                    ),
                    original(
                        "MultiSelect",
                        "standard_icon_multi_select_selector",
                        SmartisanOriginalIcons.MultiSelect,
                    ),
                    original("Add", "standard_icon_common_add_selector", SmartisanOriginalIcons.Add),
                    original("AddSmall", "add_icon_selector", SmartisanOriginalIcons.AddSmall),
                    original("AddTitleBar", "selector_title_add", SmartisanOriginalIcons.AddTitleBar),
                    original("More", "btn_more_selector", SmartisanOriginalIcons.More),
                    original("Search", "search_bar_left_icon_selector", SmartisanOriginalIcons.Search),
                    original("SearchClear", "search_clear", SmartisanOriginalIcons.SearchClear),
                    original("Refresh", "standard_icon_refresh_selector", SmartisanOriginalIcons.Refresh),
                    original("Settings", "standard_icon_settings_selector", SmartisanOriginalIcons.Settings),
                    original("SettingsPlain", "icon_setting_normal", SmartisanOriginalIcons.SettingsPlain),
                    original(
                        "Delete",
                        "standard_icon_hignlight_delete_selector",
                        SmartisanOriginalIcons.Delete,
                    ),
                    original(
                        "DeleteTitleBar",
                        "titlebar_btn_delete_selector",
                        SmartisanOriginalIcons.DeleteTitleBar,
                    ),
                    original("DeletePlain", "icon_delete_normal", SmartisanOriginalIcons.DeletePlain),
                    original("DeleteQuick", "quick_icon_delete", SmartisanOriginalIcons.DeleteQuick),
                    original("Share", "more_select_icon_share", SmartisanOriginalIcons.Share),
                    original("Edit", "btn_editlist2_selector", SmartisanOriginalIcons.Edit),
                    original("Rename", "rename_playlist_selector", SmartisanOriginalIcons.Rename),
                    original("Remove", "remove_playlist_selector", SmartisanOriginalIcons.Remove),
                    original("NameEditor", "name_editor_icon", SmartisanOriginalIcons.NameEditor),
                    original("Sort", "saved_songs_sort_btn_selector", SmartisanOriginalIcons.Sort),
                    original("SortByName", "icon_sort_by_name", SmartisanOriginalIcons.SortByName),
                    original("SortByTime", "icon_sort_by_time", SmartisanOriginalIcons.SortByTime),
                    original("ArrowDown", "arrow3_selector", SmartisanOriginalIcons.ArrowDown),
                    original("ArrowDownNormal", "arrow3", SmartisanOriginalIcons.ArrowDownNormal),
                    original("ArrowDownPressed", "arrow3_down", SmartisanOriginalIcons.ArrowDownPressed),
                ),
        ),
        OriginalIconCategory(
            title = "播放控制（原版素材）",
            icons =
                listOf(
                    original("Play", "btn_icon_play_selector", SmartisanOriginalIcons.Play),
                    original("PlayLarge", "float_btn_play_selector", SmartisanOriginalIcons.PlayLarge),
                    original("PauseLarge", "float_btn_pause_selector", SmartisanOriginalIcons.PauseLarge),
                    original("Previous", "float_btn_prev_selector", SmartisanOriginalIcons.Previous),
                    original("Next", "float_btn_next_selector", SmartisanOriginalIcons.Next),
                    original("PlayPlain", "btn_playing_play", SmartisanOriginalIcons.PlayPlain),
                    original("PausePlain", "btn_playing_pause", SmartisanOriginalIcons.PausePlain),
                    original("PreviousPlain", "btn_playing_prev", SmartisanOriginalIcons.PreviousPlain),
                    original("NextPlain", "btn_playing_next", SmartisanOriginalIcons.NextPlain),
                    original("BackToLibrary", "btn_playing_back", SmartisanOriginalIcons.BackToLibrary),
                    original("CycleOff", "btn_playing_cycle_off", SmartisanOriginalIcons.CycleOff),
                    original("CycleOn", "btn_playing_cycle_on", SmartisanOriginalIcons.CycleOn),
                    original("RepeatOn", "btn_playing_repeat_on", SmartisanOriginalIcons.RepeatOn),
                    original("ShuffleOff", "btn_playing_shuffle_off", SmartisanOriginalIcons.ShuffleOff),
                    original("ShuffleOn", "btn_playing_shuffle_on", SmartisanOriginalIcons.ShuffleOn),
                    original("Shuffle", "btn_icon_shuffle_selector", SmartisanOriginalIcons.Shuffle),
                    original("ShuffleAlbum", "btn_album_shuffle3_selector", SmartisanOriginalIcons.ShuffleAlbum),
                    original("FavoriteAdd", "btn_favorite_add_selector", SmartisanOriginalIcons.FavoriteAdd),
                    original(
                        "FavoriteAddPlain",
                        "playing_btn_favorite_add",
                        SmartisanOriginalIcons.FavoriteAddPlain,
                    ),
                    original(
                        "FavoriteCancelPlain",
                        "playing_btn_favorite_cancel",
                        SmartisanOriginalIcons.FavoriteCancelPlain,
                    ),
                    original("Lyrics", "more_select_icon_lyric", SmartisanOriginalIcons.Lyrics),
                    original("PlayAll", "btn_play_all_selector", SmartisanOriginalIcons.PlayAll),
                    original("Volume", "playing_control_volume", SmartisanOriginalIcons.Volume),
                    original("SleepTimer", "more_select_icon_timer", SmartisanOriginalIcons.SleepTimer),
                    original("SoundEffect", "more_select_icon_djing", SmartisanOriginalIcons.SoundEffect),
                    original("ProgressLine", "playing_progress_bar_line", SmartisanOriginalIcons.ProgressLine),
                ),
        ),
        OriginalIconCategory(
            title = "列表操作（原版素材）",
            icons =
                listOf(
                    original("Drag", "btn_drag_selector", SmartisanOriginalIcons.Drag),
                    original("DragPlain", "list_icon_drag", SmartisanOriginalIcons.DragPlain),
                    original("Move", "list_item_move_icon", SmartisanOriginalIcons.Move),
                    original("MoveDisabled", "list_icon_move_disable", SmartisanOriginalIcons.MoveDisabled),
                    original("DeleteSong", "btn_delete_song2_selector", SmartisanOriginalIcons.DeleteSong),
                    original("RemoveSong", "btn_remove_song2", SmartisanOriginalIcons.RemoveSong),
                    original("DeleteList", "btn_deletelist2_selector", SmartisanOriginalIcons.DeleteList),
                    original(
                        "AddToQueue",
                        "album_btn_add_to_queue_selector",
                        SmartisanOriginalIcons.AddToQueue,
                    ),
                    original(
                        "AddToPlaylist",
                        "album_btn_add_to_playlist_selector",
                        SmartisanOriginalIcons.AddToPlaylist,
                    ),
                    original("AddToQueuePlain", "more_select_icon_addplay", SmartisanOriginalIcons.AddToQueuePlain),
                    original(
                        "AddToPlaylistPlain",
                        "more_select_icon_addlist",
                        SmartisanOriginalIcons.AddToPlaylistPlain,
                    ),
                    original("DeleteMenuPlain", "more_select_icon_delete", SmartisanOriginalIcons.DeleteMenuPlain),
                    original(
                        "FavoriteAddMenuPlain",
                        "more_select_icon_favorite_add",
                        SmartisanOriginalIcons.FavoriteAddMenuPlain,
                    ),
                    original(
                        "FavoriteCancelMenuPlain",
                        "more_select_icon_favorite_cancel",
                        SmartisanOriginalIcons.FavoriteCancelMenuPlain,
                    ),
                    original("SlideDelete", "slide_delete", SmartisanOriginalIcons.SlideDelete),
                    original(
                        "SlideDeletePressed",
                        "slide_delete_down",
                        SmartisanOriginalIcons.SlideDeletePressed,
                    ),
                    original(
                        "RemoveBarBackground",
                        "list_edit_remove_background",
                        SmartisanOriginalIcons.RemoveBarBackground,
                    ),
                ),
        ),
        OriginalIconCategory(
            title = "底部标签栏（原版素材）",
            icons =
                listOf(
                    original("TabSong", "tabbar_song_selector", SmartisanOriginalIcons.TabSong),
                    original("TabAlbum", "tabbar_album_selector", SmartisanOriginalIcons.TabAlbum),
                    original("TabArtist", "tabbar_artist_selector", SmartisanOriginalIcons.TabArtist),
                    original("TabFolder", "tabbar_folder_selector", SmartisanOriginalIcons.TabFolder),
                    original("TabPlaylist", "tabbar_playlist_selector", SmartisanOriginalIcons.TabPlaylist),
                    original("TabFavorite", "tabbar_like_selector", SmartisanOriginalIcons.TabFavorite),
                    original("TabStyle", "tabbar_style_selector", SmartisanOriginalIcons.TabStyle),
                    original("TabMore", "tabbar_more_selector", SmartisanOriginalIcons.TabMore),
                    original("TabFolderWhite", "tabbar_folder_white", SmartisanOriginalIcons.TabFolderWhite),
                    original("TabFavoriteWhite", "tabbar_like_white", SmartisanOriginalIcons.TabFavoriteWhite),
                    original("TabStyleWhite", "tabbar_style_white", SmartisanOriginalIcons.TabStyleWhite),
                ),
        ),
        OriginalIconCategory(
            title = "时钟（原版素材）",
            icons =
                listOf(
                    original("TabAlarm", "selector_tab_alarm", SmartisanOriginalIcons.TabAlarm),
                    original("TabStopwatch", "selector_tab_stopwatch", SmartisanOriginalIcons.TabStopwatch),
                    original("TabTimer", "selector_tab_timer", SmartisanOriginalIcons.TabTimer),
                    original("TabWorldClock", "selector_tab_worldclock", SmartisanOriginalIcons.TabWorldClock),
                    original("StopwatchStart", "selector_stopwatch_start", SmartisanOriginalIcons.StopwatchStart),
                    original("StopwatchPlay", "selector_stopwatch_play", SmartisanOriginalIcons.StopwatchPlay),
                    original("StopwatchStop", "selector_stopwatch_stop", SmartisanOriginalIcons.StopwatchStop),
                    original("StopwatchReset", "selector_stopwatch_reset", SmartisanOriginalIcons.StopwatchReset),
                    original(
                        "StopwatchLightOn",
                        "selector_stopwatch_light_on",
                        SmartisanOriginalIcons.StopwatchLightOn,
                    ),
                    original(
                        "StopwatchLightOff",
                        "selector_stopwatch_light_off",
                        SmartisanOriginalIcons.StopwatchLightOff,
                    ),
                    original("StopwatchDot", "selector_stopwatch_dot", SmartisanOriginalIcons.StopwatchDot),
                    original("StopwatchTopLeft", "stopwatch_top_left_btn", SmartisanOriginalIcons.StopwatchTopLeft),
                    original(
                        "StopwatchTopMiddle",
                        "stopwatch_top_mid_btn",
                        SmartisanOriginalIcons.StopwatchTopMiddle,
                    ),
                    original(
                        "StopwatchTopRight",
                        "stopwatch_top_right_btn",
                        SmartisanOriginalIcons.StopwatchTopRight,
                    ),
                    original("AlarmSnooze", "ic_alarm_snooze", SmartisanOriginalIcons.AlarmSnooze),
                    original("AlarmDismiss", "ic_alarm_dismiss", SmartisanOriginalIcons.AlarmDismiss),
                    original(
                        "AlarmNotification",
                        "ic_alarm_notification",
                        SmartisanOriginalIcons.AlarmNotification,
                    ),
                    original(
                        "AlarmEditorArrow",
                        "ic_alarm_editor_arrow",
                        SmartisanOriginalIcons.AlarmEditorArrow,
                    ),
                    original("AlarmLabelClear", "alarm_label_clear", SmartisanOriginalIcons.AlarmLabelClear),
                    original("BlankDial", "blank_clock", SmartisanOriginalIcons.BlankDial),
                    original("BlankCircle", "blank_circle", SmartisanOriginalIcons.BlankCircle),
                ),
        ),
        OriginalIconCategory(
            title = "空态插图（原版素材）",
            icons =
                listOf(
                    original("EmptySong", "blank_song", SmartisanOriginalIcons.EmptySong),
                    original("EmptyFolder", "blank_folder", SmartisanOriginalIcons.EmptyFolder),
                    original("EmptyPlaylist", "blank_playlist", SmartisanOriginalIcons.EmptyPlaylist),
                    original("EmptySearch", "blank_search", SmartisanOriginalIcons.EmptySearch),
                    original("EmptyStyle", "blank_style", SmartisanOriginalIcons.EmptyStyle),
                ),
        ),
    )

/** 图标分组：分组名 + （图标名, 图标）列表。 */
data class IconCategory(
    val title: String,
    val icons: List<Pair<String, ImageVector>>,
)

/**
 * 库自绘的**补充矢量图标**。
 *
 * 注意：这些不是原版图标，只是 24×24 的矢量路径，用于原版没有对应素材的场合。
 * 只要有原版素材（见 [smartisanOriginalIconCatalog]），就应该优先使用原版素材。
 */
val smartisanIconCatalog: List<IconCategory> =
    listOf(
        IconCategory(
            title = "通用图标 SmartisanXIcons（自绘补充）",
            icons =
                listOf(
                    "Back" to SmartisanXIcons.Back,
                    "ChevronRight" to SmartisanXIcons.ChevronRight,
                    "ChevronDown" to SmartisanXIcons.ChevronDown,
                    "ChevronUp" to SmartisanXIcons.ChevronUp,
                    "Close" to SmartisanXIcons.Close,
                    "Check" to SmartisanXIcons.Check,
                    "Add" to SmartisanXIcons.Add,
                    "Remove" to SmartisanXIcons.Remove,
                    "Search" to SmartisanXIcons.Search,
                    "More" to SmartisanXIcons.More,
                    "Menu" to SmartisanXIcons.Menu,
                    "Delete" to SmartisanXIcons.Delete,
                    "Edit" to SmartisanXIcons.Edit,
                    "Share" to SmartisanXIcons.Share,
                    "Refresh" to SmartisanXIcons.Refresh,
                    "Settings" to SmartisanXIcons.Settings,
                    "DragHandle" to SmartisanXIcons.DragHandle,
                ),
        ),
        IconCategory(
            title = "状态图标 SmartisanXStatusIcons（自绘补充）",
            icons =
                listOf(
                    "Star" to SmartisanXStatusIcons.Star,
                    "StarOutline" to SmartisanXStatusIcons.StarOutline,
                    "Heart" to SmartisanXStatusIcons.Heart,
                    "Location" to SmartisanXStatusIcons.Location,
                    "Sun" to SmartisanXStatusIcons.Sun,
                    "Moon" to SmartisanXStatusIcons.Moon,
                    "Info" to SmartisanXStatusIcons.Info,
                    "Warning" to SmartisanXStatusIcons.Warning,
                    "ArrowUp" to SmartisanXStatusIcons.ArrowUp,
                    "ArrowDown" to SmartisanXStatusIcons.ArrowDown,
                    "Copy" to SmartisanXStatusIcons.Copy,
                    "Calendar" to SmartisanXStatusIcons.Calendar,
                    "ExternalLink" to SmartisanXStatusIcons.ExternalLink,
                    "Folder" to SmartisanXStatusIcons.Folder,
                ),
        ),
        IconCategory(
            title = "媒体图标 SmartisanXMediaIcons（自绘补充）",
            icons =
                listOf(
                    "Play" to SmartisanXMediaIcons.Play,
                    "Pause" to SmartisanXMediaIcons.Pause,
                    "Next" to SmartisanXMediaIcons.Next,
                    "Previous" to SmartisanXMediaIcons.Previous,
                    "Stop" to SmartisanXMediaIcons.Stop,
                    "Shuffle" to SmartisanXMediaIcons.Shuffle,
                    "Repeat" to SmartisanXMediaIcons.Repeat,
                    "Queue" to SmartisanXMediaIcons.Queue,
                    "Volume" to SmartisanXMediaIcons.Volume,
                    "VolumeMute" to SmartisanXMediaIcons.VolumeMute,
                    "FastForward" to SmartisanXMediaIcons.FastForward,
                    "Rewind" to SmartisanXMediaIcons.Rewind,
                    "Lyrics" to SmartisanXMediaIcons.Lyrics,
                ),
        ),
        IconCategory(
            title = "时钟图标 SmartisanXClockIcons（自绘补充）",
            icons =
                listOf(
                    "Clock" to SmartisanXClockIcons.Clock,
                    "Alarm" to SmartisanXClockIcons.Alarm,
                    "Stopwatch" to SmartisanXClockIcons.Stopwatch,
                    "Hourglass" to SmartisanXClockIcons.Hourglass,
                    "Globe" to SmartisanXClockIcons.Globe,
                    "Bell" to SmartisanXClockIcons.Bell,
                    "SleepTimer" to SmartisanXClockIcons.SleepTimer,
                    "KeepScreenOn" to SmartisanXClockIcons.KeepScreenOn,
                ),
        ),
    )
