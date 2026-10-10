package cc.wuersan008.smartisanx.sample

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 示例应用里的页面。
 *
 * 每个页面展示 smartisanx 的一个组件分组，全部使用简体中文说明。
 *
 * 首页的图标一律取自原版的**动作图标**（标题栏 / 标签栏 selector）：
 * 它们本身是透明底的 glyph，直接当列表图标用才合适。
 * 原版里 `btn_play_all`、`btn_favorite_add` 这类名字虽然像图标，
 * 实际是 **nine-patch 按钮底图**，当图标用会出现一整块蓝色背景。
 */
enum class SamplePage(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
) {
    Theme(
        title = "主题与设计变量",
        subtitle = "色板、文字样式、形状、深浅色切换（深色为实验性）",
        icon = R.drawable.standard_icon_settings_selector,
    ),
    Text(
        title = "文字",
        subtitle = "SmartisanText、像素字号、等宽数字",
        icon = R.drawable.standard_icon_edit_selector,
    ),
    Icon(
        title = "图标",
        subtitle = "原版图标素材与库自绘补充图标",
        icon = R.drawable.album_switch_selector,
    ),
    Button(
        title = "按钮",
        subtitle = "强调、中性、文字三种按钮",
        icon = R.drawable.standard_icon_common_add_selector,
    ),
    Control(
        title = "基础控件",
        subtitle = "开关、复选框、单选、评分条、分段按钮组、数字滚轮",
        icon = R.drawable.standard_icon_hignlight_confirm_selector,
    ),
    Input(
        title = "文本与输入",
        subtitle = "搜索栏、自动缩字与两端对齐文本、密码框、可清空输入框",
        icon = R.drawable.search_btn_selector,
    ),
    Layout(
        title = "布局与列表",
        subtitle = "标题栏、列表行、分组、标签栏、滚动条、空态、流式布局",
        icon = R.drawable.standard_icon_menu_selector,
    ),
    ListInteraction(
        title = "列表交互",
        subtitle = "拖动排序、侧滑删除、A–Z 字母索引",
        icon = R.drawable.standard_icon_filter_selector,
    ),
    Overlay(
        title = "浮层",
        subtitle = "弹窗、底部菜单、底部弹层",
        icon = R.drawable.btn_more_selector,
    ),
    Clock(
        title = "时钟与机械控件",
        subtitle = "机械表盘、时间滚轮、标尺、星期选择",
        icon = R.drawable.selector_tab_worldclock,
    ),
    Calendar(
        title = "日历",
        subtitle = "月视图、单周视角、点标题跳转日期",
        icon = R.drawable.calendar_icon,
    ),
}
