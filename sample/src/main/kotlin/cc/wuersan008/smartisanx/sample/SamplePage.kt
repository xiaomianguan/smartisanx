package cc.wuersan008.smartisanx.sample

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R

/**
 * 示例应用里的页面。
 *
 * 每个页面展示 smartisanx 的一个组件分组，全部使用简体中文说明。
 *
 * 首页的图标一律取自原版**动作图标**（标题栏 / 标签栏 selector）：
 * 它们本身是透明底的 glyph，直接当列表图标用才合适。
 * 原版里 `btn_play_all`、`btn_favorite_add` 这类名字虽然像图标，
 * 实际是 **nine-patch 按钮底图**，当图标用会出现一整块蓝色背景。
 *
 * 图标的选择规则与设置页一致：**同一套黑色 / 灰色 glyph、同一档粗细（约 54% 黑）**，
 * 优先用 framework 的 `standard_icon_*` 标准动作图标（齿轮、铅笔、加号、对勾、
 * 多选列表、放大镜、三点），其余用锤子自家应用里同一档的 glyph（邮件菜单的三条杠、
 * framework 搜索栏的排序图标、时钟的日历 glyph、时钟标签页的时钟 glyph）。
 * 每个条目都在 [icon] 的注释里写了资源来源，不自己画图标。
 *
 * [iconSize] 是这个 glyph 所在位图的固有尺寸：这些图标都是「大画布 + 居中 glyph」，
 * 按固有尺寸渲染才能让每行的 glyph 视觉大小一致（都是约 18dp）。
 */
enum class SamplePage(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val iconSize: Dp = 36.dp,
) {
    Theme(
        title = "主题与设计变量",
        subtitle = "色板、文字样式、形状、深浅色切换（深色为实验性）",
        // 齿轮：framework `standard_icon_settings_selector` → `icon_setting_*`（18dp glyph）。
        icon = R.drawable.standard_icon_settings_selector,
    ),
    Text(
        title = "文字",
        subtitle = "SmartisanText、像素字号、等宽数字",
        // 铅笔：framework `standard_icon_edit_selector` → `standard_icon_edit*`（18dp）。
        icon = R.drawable.standard_icon_edit_selector,
    ),
    Icon(
        title = "图标",
        subtitle = "原版图标素材与库自绘补充图标",
        // 多选列表：framework `standard_icon_multi_select_selector`（18dp，一格格排开的列表）。
        icon = R.drawable.standard_icon_multi_select_selector,
    ),
    Button(
        title = "按钮",
        subtitle = "强调、中性、文字三种按钮",
        // 加号：framework `standard_icon_common_add_selector` → `standard_icon_common_add*`（14dp）。
        icon = R.drawable.standard_icon_common_add_selector,
    ),
    Control(
        title = "基础控件",
        subtitle = "开关、复选框、单选、评分条、分段按钮组、数字滚轮",
        // 对勾：framework `standard_icon_complete_selector` → `standard_icon_complete*`（15dp）。
        icon = R.drawable.standard_icon_complete_selector,
    ),
    Input(
        title = "文本与输入",
        subtitle = "搜索栏、自动缩字与两端对齐文本、密码框、可清空输入框",
        // 放大镜：framework 搜索栏左侧图标 `search_bar_left_icon_selector`（15dp glyph，30% 黑）。
        icon = R.drawable.search_bar_left_icon_selector,
        iconSize = 34.dp,
    ),
    Layout(
        title = "布局与列表",
        subtitle = "标题栏、列表行、分组、标签栏、滚动条、空态、流式布局",
        // 三条杠：锤子邮件的菜单图标 `menu_icon_selector` → `menu_icon*`（30dp glyph，按 22dp 渲染）。
        icon = R.drawable.menu_icon_selector,
        iconSize = 22.dp,
    ),
    ListInteraction(
        title = "列表交互",
        subtitle = "拖动排序、侧滑删除、A–Z 字母索引",
        // 排序：framework 搜索栏的 `sorting_icon_selector` → `sorting_icon`（18dp glyph，灰色）。
        icon = R.drawable.sorting_icon_selector,
        iconSize = 48.dp,
    ),
    Overlay(
        title = "浮层",
        subtitle = "弹窗、底部菜单、底部弹层",
        // 三点：framework 标题栏的「更多」`btn_more_selector` → `btn_more*`（15dp glyph）——
        // 原版就是用它弹出浮层菜单的。
        icon = R.drawable.btn_more_selector,
    ),
    Clock(
        title = "时钟与机械控件",
        subtitle = "机械表盘、时间滚轮、标尺、星期选择",
        // 时钟：锤子时钟的标签页 glyph `selector_tab_worldclock`（30dp 矢量，按 22dp 渲染）。
        icon = R.drawable.selector_tab_worldclock,
        iconSize = 22.dp,
    ),
    Calendar(
        title = "日历",
        subtitle = "月视图、单周视角、点标题跳转日期",
        // 日历：framework 的 `button_small_calendar_selector` → `button_small_calendar*`（17dp glyph）。
        icon = R.drawable.button_small_calendar_selector,
    ),
}
