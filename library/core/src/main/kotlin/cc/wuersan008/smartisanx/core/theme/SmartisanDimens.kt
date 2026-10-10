package cc.wuersan008.smartisanx.core.theme

import androidx.compose.ui.unit.dp

/**
 * smartisanx 的尺寸常量。
 *
 * 数值取自锤子音乐、锤子天气、锤子时钟三个复刻项目里已经互相校准过的 dp / sp 资源，
 * 去重后统一放在这里，方便使用方按需覆盖（组件本身都提供显式参数）。
 */
object SmartisanDimens {
    /**
     * 标题栏高度。
     *
     * 原版三个应用的取值并不一致：锤子音乐是 `title_bar_height` = 50dp，
     * 锤子天气与锤子时钟都是 48dp。本库取 48dp（三个里有两个），
     * 需要音乐那套 50dp 时直接传 `contentHeight = 50.dp` 即可。
     */
    val TitleBarHeight = 48.dp

    /** 标题栏下方投影高度，原版 `title_bar_shadow_height`。 */
    val TitleBarShadowHeight = 14.dp

    /** 标题栏左右留白，原版 `bar_margin_edge`。 */
    val TitleBarHorizontalMargin = 6.dp

    /** 复合标题栏中槽与两侧内容的间距，原版 `mid_container_margin`。 */
    val ComboTitleCenterMargin = 12.dp

    /**
     * 复合标题栏右侧按钮之间的间距，原版 `smartisan_small_blank_spacing_width`。
     *
     * 原版把右侧图标按钮排成一行、相邻按钮的右外边距设为 `-6dp`（互相压住），
     * 最后一个按钮再补回 `+6dp`，所以整体看起来是「紧密一排、两端留白」。
     */
    val ComboTitleActionSpacing = 6.dp

    /** 行内隐藏操作的左右内边距，原版 `hidden_list_action_left_right_padding`。 */
    val HiddenActionSidePadding = 12.dp

    /** 行内隐藏操作相邻图标之间的间距，原版 `hidden_list_action_icon_gap`。 */
    val HiddenActionIconGap = 6.dp

    /** 消息输入栏里输入区上下的外边距，原版 `message_field_margin_top_bottom`（8.329987dp）。 */
    val MessageFieldFieldGap = 8.33.dp

    /** 消息输入栏输入框的内边距，原版 `mid_container_top_bottom_padding`。 */
    val FieldInnerPadding = 6.dp

    /** 消息输入栏显示表情图标时输入框的右内边距，原版 `message_field_right_emoji_padding`。 */
    val MessageFieldEmojiPadding = 5.dp

    /** 标题栏图标尺寸，原版 `standard_icon_size`。 */
    val IconSize = 36.dp

    /** 列表行高度，原版 `listview_item_height`。 */
    val ListItemHeight = 60.dp

    /** 列表行最小高度，原版 `list_item_min_height`。
     *
     * 原版 `list_content_item_layout.xml` 的根布局写着
     * `android:minHeight="@dimen/list_item_min_height"`，而 `list_item_min_height` 是
     * **60dp**，与 [ListItemHeight] 一致 —— 也就是「单行文字的行也是 60dp，
     * 只有内容更多（两行副标题等）时才变高」。
     */
    val ListItemMinHeight = 60.dp

    /** 列表行左右外边距，原版 `list_item_left_right_margin`。 */
    val ListItemHorizontalMargin = 12.dp

    /** 分组之间 / 分组上下的间距，原版 `list_item_vertical_gap`（锤子音乐 14dp）。
     *
     * 原版把这个间距留给卡片投影，投影会画在行边界之外。
     */
    val ListItemVerticalGap = 14.dp

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

    /** 底部标签栏高度，原版 `smartisan_bottom_bar_height`（锤子音乐 54dp）。 */
    val BottomBarHeight = 54.dp

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

    // ------------------------------------------------------------------
    // framework 列表行矩阵（`framework-smartisanos-res.apk` 的 res/values/dimens.xml
    // 与 res/layout/list_content_*.xml）。数值全部照抄原版，用于 SmartisanListRow 家族。
    // ------------------------------------------------------------------

    /** 列表行最小高度，原版 `list_item_min_height`（= `list_item_height`）。 */
    val ListRowMinHeight = 60.dp

    /**
     * 左侧图标区宽度，原版 `left_icon_area_width`。
     *
     * 原版列表行左侧是一个 60dp × 60dp 的方形区域，左图标 / 左复选框都在里面居中；
     * 它同时充当行内容的左缩进。
     */
    val ListRowLeftIconArea = 60.dp

    /** 左侧图标最大边长，原版 layout 里写死的 `maxWidth` / `maxHeight` = 36dp。 */
    val ListRowLeftIconMax = 36.dp

    /** 行内容与左侧图标区的间距，原版 `mid_container_margin`（列表行本身不用，见 `primary_title_layout`）。 */
    val ListRowMidContainerMargin = 12.dp

    /** 右侧容器的左右外边距，原版 `right_container_margin`。 */
    val ListRowRightContainerMargin = 6.dp

    /** 右侧副标题前的弹性间隔，原版 `flexible_space`。 */
    val ListRowFlexibleSpace = 18.dp

    /** 右侧副标题字号对应的最大宽度上限（原版未限制，这里给箭头留位）。 */
    val ListRowSubtitleMaxWidth = 200.dp

    /** 两 / 三行文字容器的上下内边距，原版 `mid_container_top_bottom_padding`。 */
    val ListRowTextVerticalPadding = 6.dp

    /** 行内多行文字之间的间距，原版 `mid_container_summary_margin`。 */
    val ListRowTextLineGap = 2.dp

    /** 分组标题高度，原版 `list_section_title_height`。 */
    val ListSectionTitleHeight = 30.dp

    /** 分组标题左内边距，原版 `list_section_header_padding_left`。 */
    val ListSectionHeaderPaddingStart = 12.dp

    /** 板块分组标题高度，原版 `list_board_section_title_layout` 里写死的 40dp。 */
    val ListBoardSectionTitleHeight = 40.dp

    /** 板块分组标题上方的留白，原版 `list_board_section_title_layout` 的 `top_space`。 */
    val ListBoardSectionTitleTopSpace = 6.dp

    /** 开关行标题的最大宽度，原版 `switch_title_max_width`。 */
    val SwitchRowTitleMaxWidth = 222.dp

    /** 开关行图标与标题的间距，原版 `item_icon_right_margin`。 */
    val SwitchRowIconMargin = 20.dp

    // ------------------------------------------------------------------
    // framework 编辑行（`framework-smartisanos-res.apk` 的 res/values/dimens.xml、
    // res/layout/abs_editor_layout.xml、editor_left_label_layout.xml、
    // editor_right_icon_widget_layout.xml）。数值全部照抄原版。
    // ------------------------------------------------------------------

    /** 编辑行左右内边距，原版 `editor_horizontal_padding`（即 `abs_editor_layout` 的 paddingLeft/Right）。 */
    val EditorHorizontalPadding = 6.dp

    /** 编辑行最小高度，原版 `abs_editor_layout` 里写死的 44dp（左右两个小部件自身只有 40dp）。 */
    val EditorRowMinHeight = 44.dp

    /** 编辑行左右小部件的最小高度，原版 `editor_left_right_widget_min_height`。 */
    val EditorWidgetMinHeight = 40.dp

    /** 编辑行左侧图标容器宽度，原版 `editor_left_icon_container_width`。 */
    val EditorLeftIconContainerWidth = 40.dp

    /** 编辑行左侧图标容器高度，原版 `editor_left_icon_container_height`。 */
    val EditorLeftIconContainerHeight = 44.dp

    /** 编辑行左侧图标尺寸，原版 `editor_left_label_layout` 里写死的 26dp。 */
    val EditorLeftIconSize = 26.dp

    /** 编辑行标签与图标 / 箭头的间距，原版 `editor_element_margin_left_right`。 */
    val EditorElementMargin = 12.dp

    /** 编辑行输入框的左右外边距，原版 `editor_horizontal_margin`（`EditorTextStyle`）。 */
    val EditorFieldHorizontalMargin = 12.dp

    /** 编辑行输入框的上下外边距，原版 `editor_small_vertical_margin`（`EditorTextStyle`）。 */
    val EditorFieldVerticalMargin = 6.dp

    /** 编辑行右侧说明文字的最大宽度，原版 `editor_right_icon_widget_layout` 里写死的 150dp。 */
    val EditorRightLabelMaxWidth = 150.dp

    /** 编辑行内部分隔线宽度，原版两个 layout 里都写死 2px（560dpi 下 = 0.57dp）。 */
    val EditorInnerDividerWidth = 0.57.dp
}
