package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 原版图标素材索引。
 *
 * 这里的每一个常量都直接指向 `library/ui/src/main/res/drawable*` 里**从原版 APK 还原出来的图形资源**，
 * 不是库自绘的矢量图标。绝大多数是 selector（自带按下 / 禁用 / 选中态）或含夜间变体的位图，
 * 因此能还原原版的拟物质感。
 *
 * 本对象是库内**唯一**的常用图标入口：原版没有对应素材时，
 * 才用 `SmartisanIcon(imageVector = ...)` 传自己的矢量图标。
 *
 * 用法（配合 `SmartisanIcon` 的位图重载）：
 *
 * ```kotlin
 * // selector 会自动按 enabled / pressed / selected 切换状态图
 * SmartisanIcon(
 *     res = SmartisanOriginalIcons.More,
 *     contentDescription = "更多",
 * )
 * ```
 *
 * 命名说明：常量名按用途起，资源文件名保持原样（原版把 highlight 拼成了 `hignlight`）。
 * 每个分组的注释里都写了对应的资源文件名，方便对照排查。
 */
object SmartisanOriginalIcons {
    // ------------------------------------------------------------------
    // 标题栏 / 通用动作
    // ------------------------------------------------------------------

    /** 返回（`standard_icon_back_selector`）。 */
    @DrawableRes val Back = R.drawable.standard_icon_back_selector

    /** 关闭 / 取消（`standard_icon_cancel_selector`）。 */
    @DrawableRes val Close = R.drawable.standard_icon_cancel_selector

    /** 完成（普通色，`standard_icon_complete_selector`）。 */
    @DrawableRes val Complete = R.drawable.standard_icon_complete_selector

    /** 完成（强调色，`standard_icon_hignlight_confirm_selector`）。 */
    @DrawableRes val Confirm = R.drawable.standard_icon_hignlight_confirm_selector

    /** 多选（`standard_icon_multi_select_selector`）。 */
    @DrawableRes val MultiSelect = R.drawable.standard_icon_multi_select_selector

    /** 新增（标准尺寸，`standard_icon_common_add_selector`）。 */
    @DrawableRes val Add = R.drawable.standard_icon_common_add_selector

    /** 新增（列表内小号，`add_icon_selector`）。 */
    @DrawableRes val AddSmall = R.drawable.add_icon_selector

    /** 标题栏新增（锤子天气，`selector_title_add`）。 */
    @DrawableRes val AddTitleBar = R.drawable.selector_title_add

    /** 更多（标题栏，`btn_more_selector`）。 */
    @DrawableRes val More = R.drawable.btn_more_selector

    /** 搜索（搜索框左侧放大镜，`search_bar_left_icon_selector`）。 */
    @DrawableRes val Search = R.drawable.search_bar_left_icon_selector

    /** 清空输入（`search_clear`）。 */
    @DrawableRes val SearchClear = R.drawable.search_clear

    /** 刷新（`standard_icon_refresh_selector`）。 */
    @DrawableRes val Refresh = R.drawable.standard_icon_refresh_selector

    /** 设置（`standard_icon_settings_selector`）。 */
    @DrawableRes val Settings = R.drawable.standard_icon_settings_selector

    /** 设置（普通位图，无 selector，`icon_setting_normal`）。 */
    @DrawableRes val SettingsPlain = R.drawable.icon_setting_normal

    /** 删除（强调色，`standard_icon_hignlight_delete_selector`）。 */
    @DrawableRes val Delete = R.drawable.standard_icon_hignlight_delete_selector

    /** 删除（标题栏，`titlebar_btn_delete_selector`）。 */
    @DrawableRes val DeleteTitleBar = R.drawable.titlebar_btn_delete_selector

    /** 删除（普通位图，无 selector，`icon_delete_normal`）。 */
    @DrawableRes val DeletePlain = R.drawable.icon_delete_normal

    /** 删除（列表快捷操作小图标，`quick_icon_delete`）。 */
    @DrawableRes val DeleteQuick = R.drawable.quick_icon_delete

    /** 分享（`more_select_icon_share`）。 */
    @DrawableRes val Share = R.drawable.more_select_icon_share

    /** 编辑列表（`btn_editlist2_selector`）。 */
    @DrawableRes val Edit = R.drawable.btn_editlist2_selector

    /** 重命名（`rename_playlist_selector`）。 */
    @DrawableRes val Rename = R.drawable.rename_playlist_selector

    /** 从列表移除（`remove_playlist_selector`）。 */
    @DrawableRes val Remove = R.drawable.remove_playlist_selector

    /** 名称编辑（`name_editor_icon`）。 */
    @DrawableRes val NameEditor = R.drawable.name_editor_icon

    /** 排序（`saved_songs_sort_btn_selector`）。 */
    @DrawableRes val Sort = R.drawable.saved_songs_sort_btn_selector

    /** 按名称排序（`icon_sort_by_name`）。 */
    @DrawableRes val SortByName = R.drawable.icon_sort_by_name

    /** 按时间排序（`icon_sort_by_time`）。 */
    @DrawableRes val SortByTime = R.drawable.icon_sort_by_time

    /** 下拉箭头（`arrow3_selector`）。 */
    @DrawableRes val ArrowDown = R.drawable.arrow3_selector

    /** 下拉箭头（常态，`arrow3`）。 */
    @DrawableRes val ArrowDownNormal = R.drawable.arrow3

    /** 下拉箭头（按下态，`arrow3_down`）。 */
    @DrawableRes val ArrowDownPressed = R.drawable.arrow3_down
    // ------------------------------------------------------------------
    // 播放控制
    // ------------------------------------------------------------------

    /** 播放（列表行内小图标，`btn_icon_play_selector`）。 */
    @DrawableRes val Play = R.drawable.btn_icon_play_selector

    /** 播放（全局播放条大按钮，`float_btn_play_selector`）。 */
    @DrawableRes val PlayLarge = R.drawable.float_btn_play_selector

    /** 暂停（全局播放条大按钮，`float_btn_pause_selector`）。 */
    @DrawableRes val PauseLarge = R.drawable.float_btn_pause_selector

    /** 上一首（全局播放条，`float_btn_prev_selector`）。 */
    @DrawableRes val Previous = R.drawable.float_btn_prev_selector

    /** 下一首（全局播放条，`float_btn_next_selector`）。 */
    @DrawableRes val Next = R.drawable.float_btn_next_selector

    /** 播放（播放页，`btn_playing_play`）。 */
    @DrawableRes val PlayPlain = R.drawable.btn_playing_play

    /** 暂停（播放页，`btn_playing_pause`）。 */
    @DrawableRes val PausePlain = R.drawable.btn_playing_pause

    /** 上一首（播放页，`btn_playing_prev`）。 */
    @DrawableRes val PreviousPlain = R.drawable.btn_playing_prev

    /** 下一首（播放页，`btn_playing_next`）。 */
    @DrawableRes val NextPlain = R.drawable.btn_playing_next

    /** 收起播放页（`btn_playing_back`）。 */
    @DrawableRes val BackToLibrary = R.drawable.btn_playing_back

    /** 列表循环关闭（`btn_playing_cycle_off`）。 */
    @DrawableRes val CycleOff = R.drawable.btn_playing_cycle_off

    /** 列表循环开启（`btn_playing_cycle_on`）。 */
    @DrawableRes val CycleOn = R.drawable.btn_playing_cycle_on

    /** 单曲循环（`btn_playing_repeat_on`）。 */
    @DrawableRes val RepeatOn = R.drawable.btn_playing_repeat_on

    /** 随机关闭（`btn_playing_shuffle_off`）。 */
    @DrawableRes val ShuffleOff = R.drawable.btn_playing_shuffle_off

    /** 随机开启（`btn_playing_shuffle_on`）。 */
    @DrawableRes val ShuffleOn = R.drawable.btn_playing_shuffle_on

    /** 随机播放（列表行内小图标，`btn_icon_shuffle_selector`）。 */
    @DrawableRes val Shuffle = R.drawable.btn_icon_shuffle_selector

    /** 随机播放专辑（`btn_album_shuffle3_selector`）。 */
    @DrawableRes val ShuffleAlbum = R.drawable.btn_album_shuffle3_selector

    /** 收藏（`btn_favorite_add_selector`）。 */
    @DrawableRes val FavoriteAdd = R.drawable.btn_favorite_add_selector

    /** 收藏（播放页，`playing_btn_favorite_add`）。 */
    @DrawableRes val FavoriteAddPlain = R.drawable.playing_btn_favorite_add

    /** 取消收藏（播放页，`playing_btn_favorite_cancel`）。 */
    @DrawableRes val FavoriteCancelPlain = R.drawable.playing_btn_favorite_cancel

    /** 歌词（`more_select_icon_lyric`）。 */
    @DrawableRes val Lyrics = R.drawable.more_select_icon_lyric

    /** 播放全部（`btn_play_all_selector`）。 */
    @DrawableRes val PlayAll = R.drawable.btn_play_all_selector

    /** 音量（`playing_control_volume`）。 */
    @DrawableRes val Volume = R.drawable.playing_control_volume

    /** 定时关闭（`more_select_icon_timer`）。 */
    @DrawableRes val SleepTimer = R.drawable.more_select_icon_timer

    /** 音效 / 均衡器（`more_select_icon_djing`）。 */
    @DrawableRes val SoundEffect = R.drawable.more_select_icon_djing

    /** 播放进度条底线（`playing_progress_bar_line`）。 */
    @DrawableRes val ProgressLine = R.drawable.playing_progress_bar_line


    // ------------------------------------------------------------------
    // 列表操作
    // ------------------------------------------------------------------

    /** 拖拽手柄（`btn_drag_selector`）。 */
    @DrawableRes val Drag = R.drawable.btn_drag_selector

    /** 拖拽手柄（普通位图，`list_icon_drag`）。 */
    @DrawableRes val DragPlain = R.drawable.list_icon_drag

    /** 移动（`list_item_move_icon`）。 */
    @DrawableRes val Move = R.drawable.list_item_move_icon

    /** 移动（禁用态，`list_icon_move_disable`）。 */
    @DrawableRes val MoveDisabled = R.drawable.list_icon_move_disable

    /** 删除歌曲（`btn_delete_song2_selector`）。 */
    @DrawableRes val DeleteSong = R.drawable.btn_delete_song2_selector

    /** 从列表移除歌曲（`btn_remove_song2`）。 */
    @DrawableRes val RemoveSong = R.drawable.btn_remove_song2

    /** 删除列表（`btn_deletelist2_selector`）。 */
    @DrawableRes val DeleteList = R.drawable.btn_deletelist2_selector

    /** 加入播放队列（`album_btn_add_to_queue_selector`）。 */
    @DrawableRes val AddToQueue = R.drawable.album_btn_add_to_queue_selector

    /** 加入播放列表（`album_btn_add_to_playlist_selector`）。 */
    @DrawableRes val AddToPlaylist = R.drawable.album_btn_add_to_playlist_selector

    /** 加入播放队列（底部菜单项，`more_select_icon_addplay`）。 */
    @DrawableRes val AddToQueuePlain = R.drawable.more_select_icon_addplay

    /** 加入播放列表（底部菜单项，`more_select_icon_addlist`）。 */
    @DrawableRes val AddToPlaylistPlain = R.drawable.more_select_icon_addlist

    /** 删除（底部菜单项，`more_select_icon_delete`）。 */
    @DrawableRes val DeleteMenuPlain = R.drawable.more_select_icon_delete

    /** 收藏（底部菜单项，`more_select_icon_favorite_add`）。 */
    @DrawableRes val FavoriteAddMenuPlain = R.drawable.more_select_icon_favorite_add

    /** 取消收藏（底部菜单项，`more_select_icon_favorite_cancel`）。 */
    @DrawableRes val FavoriteCancelMenuPlain = R.drawable.more_select_icon_favorite_cancel

    /** 侧滑删除底色（`slide_delete`）。 */
    @DrawableRes val SlideDelete = R.drawable.slide_delete

    /** 侧滑删除底色（按下态，`slide_delete_down`）。 */
    @DrawableRes val SlideDeletePressed = R.drawable.slide_delete_down

    /** 多选删除条底色（`list_edit_remove_background`）。 */
    @DrawableRes val RemoveBarBackground = R.drawable.list_edit_remove_background

    // ------------------------------------------------------------------
    // 底部标签栏
    // ------------------------------------------------------------------

    /** 歌曲（`tabbar_song_selector`）。 */
    @DrawableRes val TabSong = R.drawable.tabbar_song_selector

    /** 专辑（`tabbar_album_selector`）。 */
    @DrawableRes val TabAlbum = R.drawable.tabbar_album_selector

    /** 艺术家（`tabbar_artist_selector`）。 */
    @DrawableRes val TabArtist = R.drawable.tabbar_artist_selector

    /** 文件夹（`tabbar_folder_selector`）。 */
    @DrawableRes val TabFolder = R.drawable.tabbar_folder_selector

    /** 播放列表（`tabbar_playlist_selector`）。 */
    @DrawableRes val TabPlaylist = R.drawable.tabbar_playlist_selector

    /** 我的收藏（`tabbar_like_selector`）。 */
    @DrawableRes val TabFavorite = R.drawable.tabbar_like_selector

    /** 曲风（`tabbar_style_selector`）。 */
    @DrawableRes val TabStyle = R.drawable.tabbar_style_selector

    /** 更多（`tabbar_more_selector`）。 */
    @DrawableRes val TabMore = R.drawable.tabbar_more_selector

    /** 文件夹（白色描边版，用于深色底，`tabbar_folder_white`）。 */
    @DrawableRes val TabFolderWhite = R.drawable.tabbar_folder_white

    /** 我的收藏（白色描边版，`tabbar_like_white`）。 */
    @DrawableRes val TabFavoriteWhite = R.drawable.tabbar_like_white

    /** 曲风（白色描边版，`tabbar_style_white`）。 */
    @DrawableRes val TabStyleWhite = R.drawable.tabbar_style_white


    // ------------------------------------------------------------------
    // 时钟
    // ------------------------------------------------------------------

    /** 闹钟标签页（`selector_tab_alarm`）。 */
    @DrawableRes val TabAlarm = R.drawable.selector_tab_alarm

    /** 秒表标签页（`selector_tab_stopwatch`）。 */
    @DrawableRes val TabStopwatch = R.drawable.selector_tab_stopwatch

    /** 计时器标签页（`selector_tab_timer`）。 */
    @DrawableRes val TabTimer = R.drawable.selector_tab_timer

    /** 世界时钟标签页（`selector_tab_worldclock`）。 */
    @DrawableRes val TabWorldClock = R.drawable.selector_tab_worldclock

    /** 秒表开始（`selector_stopwatch_start`）。 */
    @DrawableRes val StopwatchStart = R.drawable.selector_stopwatch_start

    /** 秒表播放 / 继续（`selector_stopwatch_play`）。 */
    @DrawableRes val StopwatchPlay = R.drawable.selector_stopwatch_play

    /** 秒表停止（`selector_stopwatch_stop`）。 */
    @DrawableRes val StopwatchStop = R.drawable.selector_stopwatch_stop

    /** 秒表复位（`selector_stopwatch_reset`）。 */
    @DrawableRes val StopwatchReset = R.drawable.selector_stopwatch_reset

    /** 秒表背光开（`selector_stopwatch_light_on`）。 */
    @DrawableRes val StopwatchLightOn = R.drawable.selector_stopwatch_light_on

    /** 秒表背光关（`selector_stopwatch_light_off`）。 */
    @DrawableRes val StopwatchLightOff = R.drawable.selector_stopwatch_light_off

    /** 秒表计次圆点（`selector_stopwatch_dot`）。 */
    @DrawableRes val StopwatchDot = R.drawable.selector_stopwatch_dot

    /** 秒表顶部左侧按钮（`stopwatch_top_left_btn`）。 */
    @DrawableRes val StopwatchTopLeft = R.drawable.stopwatch_top_left_btn

    /** 秒表顶部中间按钮（`stopwatch_top_mid_btn`）。 */
    @DrawableRes val StopwatchTopMiddle = R.drawable.stopwatch_top_mid_btn

    /** 秒表顶部右侧按钮（`stopwatch_top_right_btn`）。 */
    @DrawableRes val StopwatchTopRight = R.drawable.stopwatch_top_right_btn

    /** 闹钟响铃「稍后提醒」（`ic_alarm_snooze`）。 */
    @DrawableRes val AlarmSnooze = R.drawable.ic_alarm_snooze

    /** 闹钟响铃「关闭」（`ic_alarm_dismiss`）。 */
    @DrawableRes val AlarmDismiss = R.drawable.ic_alarm_dismiss

    /** 闹钟通知小图标（`ic_alarm_notification`）。 */
    @DrawableRes val AlarmNotification = R.drawable.ic_alarm_notification

    /** 闹钟编辑页下拉箭头（`ic_alarm_editor_arrow`）。 */
    @DrawableRes val AlarmEditorArrow = R.drawable.ic_alarm_editor_arrow

    /** 闹钟标签输入框清除（`alarm_label_clear`）。 */
    @DrawableRes val AlarmLabelClear = R.drawable.alarm_label_clear

    /** 空白表盘底图（世界时钟小表盘遮罩，`blank_clock`）。 */
    @DrawableRes val BlankDial = R.drawable.blank_clock

    /** 空白圆底图（指针视图遮罩，`blank_circle`）。 */
    @DrawableRes val BlankCircle = R.drawable.blank_circle

    // ------------------------------------------------------------------
    // 空态插图
    // ------------------------------------------------------------------

    /** 空态：没有歌曲（`blank_song`）。 */
    @DrawableRes val EmptySong = R.drawable.blank_song

    /** 空态：没有文件夹（`blank_folder`）。 */
    @DrawableRes val EmptyFolder = R.drawable.blank_folder

    /** 空态：没有播放列表（`blank_playlist`）。 */
    @DrawableRes val EmptyPlaylist = R.drawable.blank_playlist

    /** 空态：没有搜索结果（`blank_search`）。 */
    @DrawableRes val EmptySearch = R.drawable.blank_search

    /** 空态：没有曲风（`blank_style`）。 */
    @DrawableRes val EmptyStyle = R.drawable.blank_style


}
