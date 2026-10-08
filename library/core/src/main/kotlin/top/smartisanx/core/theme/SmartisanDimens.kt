package top.smartisanx.core.theme

import androidx.compose.ui.unit.dp

/**
 * smartisanx 的尺寸常量。
 *
 * 数值取自锤子音乐、锤子天气、锤子时钟三个复刻项目里已经互相校准过的 dp / sp 资源，
 * 去重后统一放在这里，方便使用方按需覆盖（组件本身都提供显式参数）。
 */
object SmartisanDimens {
    /** 标题栏高度，原版 `title_bar_height`。 */
    val TitleBarHeight = 48.dp

    /** 标题栏下方投影高度，原版 `title_bar_shadow_height`。 */
    val TitleBarShadowHeight = 14.dp

    /** 标题栏左右留白，原版 `bar_margin_edge`。 */
    val TitleBarHorizontalMargin = 6.dp

    /** 标题栏图标尺寸，原版 `standard_icon_size`。 */
    val IconSize = 36.dp

    /** 列表行高度，原版 `listview_item_height`。 */
    val ListItemHeight = 60.dp

    /** 列表行最小高度，用于多行内容。 */
    val ListItemMinHeight = 48.dp

    /** 列表行左右外边距，原版 `list_item_left_right_margin`。 */
    val ListItemHorizontalMargin = 12.dp

    /** 列表内容起始位置，原版 `settings_row_content_margin_start`。 */
    val RowContentStart = 18.dp

    /** 多选复选框左边距，原版 `check_box_margin_left`。 */
    val CheckboxMarginStart = 18.dp

    /** 分隔线粗细，原版 `listview_dividerHeight`。 */
    val DividerThickness = 0.67.dp

    /** 列表图标尺寸，原版 `listview_item_image_width`。 */
    val ListItemImageSize = 48.dp

    /** 弹窗内容宽度，原版 `smartisan_modal_width` / `revone_global_dialog_content_width`。 */
    val DialogWidth = 308.dp

    /** 弹窗标题栏高度，原版 `smartisan_modal_title_height`。 */
    val DialogTitleHeight = 48.dp

    /** 弹窗按钮高度，原版 `smartisan_modal_button_height`。 */
    val DialogButtonHeight = 48.dp

    /** 弹窗圆角，原版 `smartisan_modal_corner_radius`。 */
    val DialogCornerRadius = 10.dp

    /** 底部菜单弹窗左右留白，原版 `smartisan_menu_horizontal_margin`。 */
    val MenuHorizontalMargin = 18.dp

    /** 底部菜单弹窗按钮上边距，原版 `smartisan_menu_button_top_margin`。 */
    val MenuButtonTopMargin = 18.dp

    /** 底部菜单弹窗按钮下边距，原版 `smartisan_menu_button_bottom_margin`。 */
    val MenuButtonBottomMargin = 24.dp

    /** 底部菜单弹窗底部动作区的左右留白，原版 `smartisan_menu_action_edge_margin`。 */
    val MenuActionEdgeMargin = 24.dp

    /** 底部菜单弹窗动作项间距，原版 `smartisan_menu_action_gap`。 */
    val MenuActionGap = 18.dp

    /** 底部标签栏高度，原版 `bottom_bar_height`。 */
    val BottomBarHeight = 50.dp

    /** 底部标签栏图标尺寸，原版 `clock_tab_icon_size`。 */
    val BottomBarIconSize = 30.dp

    /** 开关宽度（含投影），取自原版开关阴影图 66dp × 48dp。 */
    val SwitchWidth = 51.dp

    /** 开关高度。 */
    val SwitchHeight = 31.dp

    /** 开关滑块直径。 */
    val SwitchKnobSize = 27.dp

    /** 开关投影尺寸。 */
    val SwitchShadowWidth = 66.dp

    /** 开关投影高度。 */
    val SwitchShadowHeight = 48.dp

    /** 滚动条宽度。 */
    val ScrollbarWidth = 3.dp

    /** 滚动条与内容边缘的间距。 */
    val ScrollbarMargin = 2.dp

    /** 字母索引栏宽度，原版 `smartisan_letterbar` 列宽。 */
    val LetterIndexBarWidth = 24.dp

    /** 通用图标按钮的点击区域，保证至少 48dp 的可点范围。 */
    val MinimumTouchTarget = 48.dp

    /** 分组卡片之间的间距。 */
    val GroupSpacing = 12.dp

    /** 分组卡片内部的行内边距。 */
    val GroupRowPadding = 12.dp
}
