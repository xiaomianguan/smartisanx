package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * smartisanx 内置的原始图形资源索引。
 *
 * 这些资源直接来自三个复刻项目对原版 Smartisan OS 的还原（见 README 的「资源来源」一节），
 * 组件内部通过它们还原拟物质感；使用方也可以在自己的界面里引用。
 *
 * 命名说明：这里只做「可读别名」，资源文件名保持原样（例如原版把 highlight 拼成了
 * `hignlight`，本对象里对应 [IconHighlightDelete]）。
 */
object SmartisanDrawables {
    // ---- 标题栏 ----
    /** 标题栏底色（NinePatch，含夜间变体）。 */
    @DrawableRes val TitleBarBackground = R.drawable.titlebar_bg

    /** 标题栏下方投影。 */
    @DrawableRes val TitleBarShadow = R.drawable.title_bar_shadow

    /**
     * 标题栏下方投影的**短**版本（原版 `title_bar_shadow_short`）。
     *
     * 复合标题栏（[cc.wuersan008.smartisanx.ui.layout.SmartisanComboTitleBar]）有次级栏时用它：
     * 投影改成压在次级栏顶部，所以只要短短一截（素材高 25px，比 `title_bar_shadow` 的 48px 短）。
     */
    @DrawableRes val TitleBarShadowShort = R.drawable.title_bar_shadow_short

    /** 次级栏下方的投影（原版 `secondary_bar_shadow`，9-patch）。 */
    @DrawableRes val SecondaryBarShadow = R.drawable.secondary_bar_shadow

    /** 播放页标题栏底色。 */
    @DrawableRes val TitleBarPlayingBackground = R.drawable.titlebar_playing_bg

    // ---- 标题栏图标（selector，自带按下/禁用态） ----
    @DrawableRes val IconBack = R.drawable.standard_icon_back_selector
    @DrawableRes val IconCancel = R.drawable.standard_icon_cancel_selector
    @DrawableRes val IconComplete = R.drawable.standard_icon_complete_selector
    @DrawableRes val IconMultiSelect = R.drawable.standard_icon_multi_select_selector
    @DrawableRes val IconAdd = R.drawable.selector_title_add
    @DrawableRes val IconHighlightDelete = R.drawable.standard_icon_hignlight_delete_selector
    @DrawableRes val IconSearch = R.drawable.search_bar_left_icon_selector

    // ---- 弹窗与菜单 ----
    /** 底部弹层标题栏底色。 */
    @DrawableRes val BottomSheetTitleBarBackground = R.drawable.bottom_sheet_title_bar_bg

    /** 底部菜单弹窗内容底色（含夜间变体）。 */
    @DrawableRes val MenuDialogBackground = R.drawable.menu_dialog_background

    /** 弹窗红色长按钮（selector）。 */
    @DrawableRes val DialogButtonAccent = R.drawable.shrink_long_btn_red_selector

    /** 弹窗红色长按钮的投影（selector）。 */
    @DrawableRes val DialogButtonAccentShadow = R.drawable.shadow_button_shrink_shadow_selector

    /** 弹窗确认按钮底色。 */
    @DrawableRes val DialogConfirmBackground = R.drawable.smartisan_menu_confirm_background

    /** 弹窗确认按钮投影。 */
    @DrawableRes val DialogConfirmShadow = R.drawable.smartisan_menu_confirm_shadow

    // ---- 列表与分组 ----
    /** 通用列表行 selector：按下换原版按压位图，activated 换多选底色，默认 surface_card。 */
    @DrawableRes val ListRowSelector = R.drawable.listview_selector

    /** 分组第一行的 selector。 */
    @DrawableRes val GroupRowTop = R.drawable.group_list_item_bg_top

    /** 分组中间行的 selector。 */
    @DrawableRes val GroupRowMiddle = R.drawable.group_list_item_bg_mid

    /** 分组最后一行的 selector。 */
    @DrawableRes val GroupRowBottom = R.drawable.group_list_item_bg_bottom

    /** 分组只有一行时的 selector（带描边）。 */
    @DrawableRes val GroupRowSingle = R.drawable.group_list_item_bg_single

    /**
     * 卡片投影 9-patch（分组首行）。
     *
     * 锤子的卡片不是用 Compose 的 elevation，而是「内容底图 + 向外扩张的阴影 9-patch」两层；
     * 阴影的扩张量由 9-patch 自己的 padding 决定，见 `Modifier.smartisanShadowBackground`。
     */
    @DrawableRes val GroupRowTopShadow = R.drawable.list_content_item_top_shadow

    /** 卡片投影 9-patch（分组中间行）。 */
    @DrawableRes val GroupRowMiddleShadow = R.drawable.list_content_item_middle_shadow

    /** 卡片投影 9-patch（分组末行）。 */
    @DrawableRes val GroupRowBottomShadow = R.drawable.list_content_item_bottom_shadow

    /** 卡片投影 9-patch（单行分组）。 */
    @DrawableRes val GroupRowSingleShadow = R.drawable.list_content_item_single_shadow

    /** 分组标题底色。 */
    @DrawableRes val SectionTitleBackground = R.drawable.list_title_bg

    /**
     * 板块分组标题底色（framework `list_board_section_bg`）。
     *
     * 是 selector：常态白色、按下 `#f2f2f2`；两个颜色值都写在
     * `values/smartisanx_framework_values.xml` 里，与原版 `values/drawables.xml` 一致。
     */
    @DrawableRes val ListBoardSectionBackground = R.drawable.list_board_section_bg

    /** 板块分组标题下方的 1px 分隔线（framework `list_board_section_title_divider`）。 */
    @DrawableRes val ListBoardSectionTitleDivider = R.drawable.list_board_section_title_divider

    /** 列表项右侧箭头 selector。 */
    @DrawableRes val ListItemArrow = R.drawable.selector_list_content_item_arrow

    /** 弹层菜单底色（NinePatch，含夜间变体）。 */
    @DrawableRes val MenuPopupBackground = R.drawable.pop_up_menu_bg

    /** 弹层菜单投影。 */
    @DrawableRes val MenuPopupShadow = R.drawable.popup_menu_bg_shadow

    /** 复选框 selector。 */
    @DrawableRes val CheckboxSelector = R.drawable.check_box_selector

    /** 单选按钮 selector。 */
    @DrawableRes val RadioSelector = R.drawable.selector_radio_choice

    /** 弹窗中性按钮底色 selector。 */
    @DrawableRes val DialogButtonNeutral = R.drawable.revone_dialog_button_bg_selector

    /** 弹窗左侧按钮底色 selector。 */
    @DrawableRes val DialogButtonLeft = R.drawable.revone_dialog_button_left_bg_selector

    /** 弹窗右侧按钮底色 selector。 */
    @DrawableRes val DialogButtonRight = R.drawable.revone_dialog_button_right_bg_selector

    /** 弹窗按钮之间的竖分隔线。 */
    @DrawableRes val DialogButtonDivider = R.drawable.revone_button_dialog_vertical_divider

    /** 页面底色（weather 的 list_bg，含夜间变体）。 */
    @DrawableRes val PageBackground = R.drawable.list_bg

    /** 标题栏刷新图标 selector。 */
    @DrawableRes val IconRefresh = R.drawable.standard_icon_refresh_selector

    /** 标题栏设置图标 selector。 */
    @DrawableRes val IconSettings = R.drawable.standard_icon_settings_selector

    /** 标题栏「完成」图标 selector。 */
    @DrawableRes val IconConfirm = R.drawable.standard_icon_hignlight_confirm_selector
    /** 页面列表底色（weather 的 list_bg，含夜间变体）。 */
    @DrawableRes val ListBackground = R.drawable.list_bg

    /** 菜单项按压背景。 */
    @DrawableRes val MenuItemSelector = R.drawable.menu_item_selector

    /** 列表分组分隔线。 */
    @DrawableRes val ListDivider = R.drawable.list_devider_bg

    /** 拖拽中的行背景。 */
    @DrawableRes val ListDragBackground = R.drawable.list_drag_bg

    /** 拖拽顶部投影。 */
    @DrawableRes val ListDragTopShadow = R.drawable.list_drag_top_shadow

    /** 拖拽底部投影。 */
    @DrawableRes val ListDragBottomShadow = R.drawable.list_drag_bottom_shadow

    /** 列表多选删除按钮背景。 */
    @DrawableRes val ListRemoveBackground = R.drawable.list_edit_remove_background

    // ---- 评分 ----
    /** 评分星（满）。 */
    @DrawableRes val ScoreFull = R.drawable.score_full

    /** 评分星（空）。 */
    @DrawableRes val ScoreEmpty = R.drawable.score_empty

    // ---- 开关 ----
    /** 开关轨道底部。 */
    @DrawableRes val SwitchBottom = R.drawable.switch_ex_bottom

    /** 开关遮罩。 */
    @DrawableRes val SwitchMask = R.drawable.switch_ex_mask

    /** 开关外框。 */
    @DrawableRes val SwitchFrame = R.drawable.switch_ex_frame

    /** 开关外框（按下）。 */
    @DrawableRes val SwitchFramePressed = R.drawable.switch_ex_frame_pressed

    /** 开关滑块。 */
    @DrawableRes val SwitchKnob = R.drawable.switch_ex_unpressed

    /** 开关滑块（按下）。 */
    @DrawableRes val SwitchKnobPressed = R.drawable.switch_ex_pressed

    // ---- 字母索引栏 ----
    @DrawableRes val LetterBarBackground = R.drawable.letters_bar_background
    @DrawableRes val LetterBarShadow = R.drawable.letters_bar_background_shadow
    @DrawableRes val LetterBarHighlight = R.drawable.letters_bar_highlight_icon
    @DrawableRes val LetterBarUnfold = R.drawable.letter_bar_unfold_btn
    @DrawableRes val LetterBarUnfoldPressed = R.drawable.letter_bar_unfold_btn_pressed

    // ---- 侧滑删除 ----
    @DrawableRes val SlideDelete = R.drawable.slide_delete
    @DrawableRes val SlideDeletePressed = R.drawable.slide_delete_down

    // ---- 底部标签栏 ----
    @DrawableRes val TabAlarm = R.drawable.selector_tab_alarm
    @DrawableRes val TabStopwatch = R.drawable.selector_tab_stopwatch
    @DrawableRes val TabTimer = R.drawable.selector_tab_timer
    @DrawableRes val TabWorldClock = R.drawable.selector_tab_worldclock

    /** 底部标签栏底色（原版 `sb_repeat_tabbar_bg`，内部平铺 `sb_tabbar_bg`）。 */
    @DrawableRes val BottomBarBackground = R.drawable.sb_repeat_tabbar_bg

    /** 底部标签栏上方投影（原版 `tab_bar_shadow`）。 */
    @DrawableRes val BottomBarShadow = R.drawable.tab_bar_shadow
    @DrawableRes val ClockDivider = R.drawable.clock_divider

    // ---- 搜索框 ----
    @DrawableRes val SearchField = R.drawable.search_field
    @DrawableRes val SearchFieldDisabled = R.drawable.search_field_disabled
    @DrawableRes val SearchFieldSelector = R.drawable.search_bar_edit_bg_selector
    // ---- 分段按钮组（原版 smartisanos.widget.ButtonTabGroup） ----
    /** 连续分段的首段底图 selector，原版 `selector_small_btn_filter_left`。 */
    @DrawableRes val ButtonTabGroupFilterLeft = R.drawable.selector_small_btn_filter_left

    /** 连续分段的中间段底图 selector，原版 `selector_small_btn_filter_middle`。 */
    @DrawableRes val ButtonTabGroupFilterMiddle = R.drawable.selector_small_btn_filter_middle

    /** 连续分段的尾段底图 selector，原版 `selector_small_btn_filter_right`。 */
    @DrawableRes val ButtonTabGroupFilterRight = R.drawable.selector_small_btn_filter_right

    /** 有间距分段（每个按钮独立底图）的 selector，原版 `selector_small_btn_standard`。 */
    @DrawableRes val ButtonTabGroupStandard = R.drawable.selector_small_btn_standard

    // ---- 计算器按键（原版 com.smartisanos.calculator.HammerButton） ----
    /** 白色按键底图 selector，原版 `cal_selector_btn_white`。 */
    @DrawableRes val CalculatorButtonWhite = R.drawable.cal_selector_btn_white

    /** 灰色按键底图 selector，原版 `cal_selector_btn_grey`。 */
    @DrawableRes val CalculatorButtonGrey = R.drawable.cal_selector_btn_grey

    /** 黑色按键底图 selector，原版 `cal_selector_btn_black`。 */
    @DrawableRes val CalculatorButtonBlack = R.drawable.cal_selector_btn_black

    /** 灰色按键（带焦点态）底图 selector，原版 `cal_selector_btn_grey_focus`。 */
    @DrawableRes val CalculatorButtonGreyFocus = R.drawable.cal_selector_btn_grey_focus

    /** 黑色按键（带焦点态）底图 selector，原版 `cal_selector_btn_black_focus`。 */
    @DrawableRes val CalculatorButtonBlackFocus = R.drawable.cal_selector_btn_black_focus

    /** 数字 0 键（双宽）底图 selector，原版 `selector_digit_0`。 */
    @DrawableRes val CalculatorButtonDigitZero = R.drawable.selector_digit_0

    /** 等号键（红色，双高）底图 selector，原版 `selector_amount`。 */
    @DrawableRes val CalculatorButtonEqual = R.drawable.selector_amount

    // ---- 环形下载进度（原版 smartisanos.widget.DownloadProgressView） ----
    /** 状态 1：下载中。 */
    @DrawableRes val ProgressStateDownload = R.drawable.sos_smartisanos_drawable_circular_progress_download

    /** 状态 2：已暂停。 */
    @DrawableRes val ProgressStatePause = R.drawable.sos_smartisanos_drawable_circular_progress_pause

    /** 状态 3：失败 / 重试。 */
    @DrawableRes val ProgressStateRetry = R.drawable.sos_smartisanos_drawable_circular_progress_redo

    /** 状态 4：处理中（每帧旋转 5°）。 */
    @DrawableRes val ProgressStateProcessing = R.drawable.sos_smartisanos_drawable_circular_progress_processing

    // ---- 搜索栏（原版 smartisanos.widget.SearchBar） ----
    /** 搜索栏编辑区底图 selector（含禁用态），原版 `search_bar_edit_bg_selector`。 */
    @DrawableRes val SearchBarEditBackground = R.drawable.search_bar_edit_bg_selector

    /** 搜索栏左侧放大镜 selector，原版 `search_bar_left_icon_selector`。 */
    @DrawableRes val SearchBarLeftIcon = R.drawable.search_bar_left_icon_selector

    /** 搜索栏收起态右侧的筛选按钮 selector，原版 `sorting_icon_selector`。 */
    @DrawableRes val SearchBarSorting = R.drawable.sorting_icon_selector

    /** 搜索栏展开态的取消按钮 selector，原版 `standard_icon_cancel_selector`。 */
    @DrawableRes val SearchBarCancel = R.drawable.standard_icon_cancel_selector

    // ---- 可清空输入框（原版 smartisanos.widget.QuickDeleteEditText） ----
    /** 一键清空按钮 selector，原版 `quick_icon_delete`（含按下态）。 */
    @DrawableRes val QuickDeleteIcon = R.drawable.quick_icon_delete

    // ---- 编辑行（原版 smartisanos.widget.editor.AbsEditor 家族） ----
    /**
     * 编辑行底图（原版 `editor_bg_single`，分组里唯一一行）。
     *
     * 另外三张按位置取：[EditorRowTop] / [EditorRowMiddle] / [EditorRowBottom]。
     */
    @DrawableRes val EditorRowSingle = R.drawable.sos_smartisanos_drawable_editor_bg_single

    /** 编辑行底图，分组第一行（原版 `editor_bg_top`）。 */
    @DrawableRes val EditorRowTop = R.drawable.sos_smartisanos_drawable_editor_bg_top

    /** 编辑行底图，分组中间行（原版 `editor_bg_middle`）。 */
    @DrawableRes val EditorRowMiddle = R.drawable.sos_smartisanos_drawable_editor_bg_middle

    /** 编辑行底图，分组最后一行（原版 `editor_bg_bottom`）。 */
    @DrawableRes val EditorRowBottom = R.drawable.sos_smartisanos_drawable_editor_bg_bottom

    // ---- 带图标滑杆两端的图标（原版 smartisanos.widget.SliderWithIcons 的用法） ----

    /**
     * 音量滑杆左端的「小音量」图标（原版 `volume_small_n`）。
     *
     * 这套音量图标出自原版音乐播放器的音量滑杆，是库内现成的成对图标；
     * 素材 144×144px（xxhdpi 下 48dp），用 [SmartisanSliderIcon] 时可以显式给尺寸。
     */
    @DrawableRes val VolumeSmall = R.drawable.volume_small_n

    /** 音量滑杆的「中等音量」图标（原版 `volume_middle_n`）。 */
    @DrawableRes val VolumeMiddle = R.drawable.volume_middle_n

    /** 音量滑杆右端的「大音量」图标（原版 `volume_high_n`）。 */
    @DrawableRes val VolumeHigh = R.drawable.volume_high_n

    /**
     * 音量滑杆的「静音 / 禁用」图标（原版 `volume_mute_d`）。
     *
     * 注意这张素材本身是**浅灰**（`_d` = disabled），原版用在深色的音量面板上；
     * 放到浅色底上几乎看不见，浅色背景别用它做端点图标。
     */
    @DrawableRes val VolumeMute = R.drawable.volume_mute_d

}
