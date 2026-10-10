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
 * 首页的版式与图标**照抄原版设置页**（`SettingsSmartisan.apk`，Android 11 的 darwin 版）：
 * `com.android.settings.MainSettingsFragment` 里每个设置项都是
 * `new SettingItem(index, style, R.drawable.secletor_setting_item_icon_xxx, titleRes, bgStyle)`，
 * 也就是「一档浅灰的 glyph（`setting_item_icon_*`，72×72px 画布、约 20dp glyph，
 * 常态 `#C5C5C7`、按下 / 聚焦态换成白色的 `setting_item_icon_focus_*`）+ 标题 + 右侧小箭头」。
 * 本页按**用途**挑同一套里的图标（下面每条都写了对应哪个设置项），不自己画图标：
 *
 * | 本页条目 | 原版设置页里的出处 |
 * | --- | --- |
 * | 主题与设计变量 | `secletor_setting_item_icon_brightness`（显示与亮度） |
 * | 文字 | `setting_item_icon_pencil`（编辑类设置项） |
 * | 图标 | `secletor_setting_item_icon_wallpaper`（壁纸） |
 * | 按钮 | `secletor_setting_item_icon_shortcut`（按键快捷方式） |
 * | 基础控件 | `selector_setting_item_navbar`（导航键） |
 * | 文本与输入 | `secletor_setting_item_icon_language`（语言与输入法，glyph 是键盘） |
 * | 布局与列表 | `secletor_setting_item_icon_launcher`（桌面设置） |
 * | 列表交互 | `secletor_setting_item_icon_application`（应用管理） |
 * | 浮层 | `secletor_setting_item_icon_notification`（通知） |
 * | 时钟与机械控件 | `secletor_setting_item_icon_keyguard`（锁屏设置，里面就是时钟样式） |
 * | 日历 | `button_small_calendar_selector`（设置页没有日历 glyph，用 framework 里日历应用的日历 glyph） |
 * | 关于本机 | `secletor_setting_item_icon_about`（设置页主菜单最后一项「关于本机」，页面照抄它的 `about_settings_layout`） |
 *
 * [iconSize] 是图标位图的固有尺寸：设置页这套是 24dp 画布（glyph 约 20dp），
 * 按固有尺寸渲染每行的 glyph 视觉大小才一致。
 */
enum class SamplePage(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val iconSize: Dp = 24.dp,
) {
    Theme(
        title = "主题与设计变量",
        subtitle = "色板、文字样式、形状、深浅色切换（深色为实验性）",
        // 原版设置页「显示与亮度」的图标。
        icon = R.drawable.secletor_setting_item_icon_brightness,
    ),
    Text(
        title = "文字",
        subtitle = "SmartisanText、像素字号、等宽数字",
        // 原版设置页编辑类设置项用的铅笔。
        icon = R.drawable.setting_item_icon_pencil,
    ),
    Icon(
        title = "图标",
        subtitle = "原版图标素材与库自绘补充图标",
        // 原版设置页「壁纸」的图标。
        icon = R.drawable.secletor_setting_item_icon_wallpaper,
    ),
    Button(
        title = "按钮",
        subtitle = "强调、中性、文字三种按钮",
        // 原版设置页「按键快捷方式」的图标。
        icon = R.drawable.secletor_setting_item_icon_shortcut,
    ),
    Control(
        title = "基础控件",
        subtitle = "开关、复选框、单选、评分条、分段按钮组、数字滚轮",
        // 原版设置页「导航键」的图标。
        icon = R.drawable.selector_setting_item_navbar,
    ),
    Input(
        title = "文本与输入",
        subtitle = "搜索栏、自动缩字与两端对齐文本、密码框、可清空输入框",
        // 原版设置页「语言与输入法」的图标（glyph 是键盘）。
        icon = R.drawable.secletor_setting_item_icon_language,
    ),
    Layout(
        title = "布局与列表",
        subtitle = "标题栏、列表行、分组、标签栏、滚动条、空态、流式布局",
        // 原版设置页「桌面设置」的图标。
        icon = R.drawable.secletor_setting_item_icon_launcher,
    ),
    ListInteraction(
        title = "列表交互",
        subtitle = "拖动排序、侧滑删除、A–Z 字母索引",
        // 原版设置页「应用管理」的图标。
        icon = R.drawable.secletor_setting_item_icon_application,
    ),
    Overlay(
        title = "浮层",
        subtitle = "弹窗、底部菜单、底部弹层",
        // 原版设置页「通知」的图标。
        icon = R.drawable.secletor_setting_item_icon_notification,
    ),
    Clock(
        title = "时钟与机械控件",
        subtitle = "机械表盘、时间滚轮、标尺、星期选择",
        // 原版设置页「锁屏设置」的图标（锁屏设置里就是时钟样式）。
        icon = R.drawable.secletor_setting_item_icon_keyguard,
    ),
    Calendar(
        title = "日历",
        subtitle = "月视图、单周视角、点标题跳转日期",
        // 设置页没有日历 glyph，用 framework 的 `button_small_calendar_selector`（17dp glyph）。
        icon = R.drawable.button_small_calendar_selector,
        iconSize = 36.dp,
    ),
    About(
        title = "关于本机",
        subtitle = "照抄坚果 R2 设置页：logo 卡片 + 本机信息列表",
        // 原版设置页主菜单最后一项就是「关于本机」，直接用它的图标。
        icon = R.drawable.secletor_setting_item_icon_about,
    ),
}

