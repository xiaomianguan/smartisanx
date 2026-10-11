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
 * | 整页布局 | `selector_setting_item_tnt_assist`（原版「TNT 大屏」设置项的**显示器** glyph，表示「一整屏怎么拼」） |
 * | 列表交互 | `secletor_setting_item_icon_application`（应用管理） |
 * | 浮层 | `secletor_setting_item_icon_notification`（通知） |
 * | 时钟与机械控件 | `secletor_setting_item_icon_keyguard`（锁屏设置，里面就是时钟样式） |
 * | 日历 | 设置页那一套里没有日历 glyph，用锁屏（`KeyguardSmartisan`）的 `calendar_icon` —— 同一档浅灰的实心日历 |
 * | 关于 | `secletor_setting_item_icon_about`（原版设置页最后一项「关于本机」的图标；页面只借它的 `about_settings_layout` 版式，内容是 smartisanx 自己） |
 *
 * [iconSize] 是图标位图的固有尺寸：设置页这套是 24dp 画布（glyph 42–66px，
 * 见 `setting_item_icon_*`），按固有尺寸渲染每行的 glyph 视觉大小才一致。
 * 日历那张来自锁屏，画布是 153px、glyph 只有 74×70px，所以按 40dp 渲染，
 * 得到的 glyph 约 68×64px，与设置页那一档同粗同大。
 *
 * 图标大小**不影响**标题的 x：列表行的左槽是原版固定的 `left_icon_area_width`(60dp)，
 * 图标在槽里居中（所以 24dp 与 36dp 的图标后面，标题都从距屏幕左 72dp 处开始）。
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
    Layouts(
        title = "整页布局",
        subtitle = "13 张完整页面：原版应用复刻 + 通用骨架",
        // 原版设置页「TNT 大屏」那一项的显示器 glyph：表示「一整屏怎么拼」。
        icon = R.drawable.selector_setting_item_tnt_assist,
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
        // 设置页那一套里没有日历 glyph，用锁屏（KeyguardSmartisan）的 `calendar_icon`：
        // 153px 画布、glyph 74×70px，按 40dp 渲染后与设置页那一档同粗同大。
        icon = R.drawable.kg_calendar_icon,
        iconSize = 40.dp,
    ),
    About(
        title = "关于",
        subtitle = "组件库与示例应用的信息，版式照抄坚果 R2 设置页",
        // 借的是版式：原版设置页主菜单最后一项「关于本机」的排版。
        icon = R.drawable.secletor_setting_item_icon_about,
    ),
}

