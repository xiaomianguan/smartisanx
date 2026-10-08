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

    /** 分组标题底色。 */
    @DrawableRes val SectionTitleBackground = R.drawable.list_title_bg

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
    @DrawableRes val ClockDivider = R.drawable.clock_divider

    // ---- 搜索框 ----
    @DrawableRes val SearchField = R.drawable.search_field
    @DrawableRes val SearchFieldDisabled = R.drawable.search_field_disabled
    @DrawableRes val SearchFieldSelector = R.drawable.search_bar_edit_bg_selector
}
