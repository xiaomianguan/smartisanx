package cc.wuersan008.smartisanx.sample

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons

/**
 * 示例应用里的页面。
 *
 * 每个页面展示 smartisanx 的一个组件分组，全部使用简体中文说明。
 */
enum class SamplePage(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
) {
    Theme(
        title = "主题与设计变量",
        subtitle = "色板、文字样式、形状、深浅色切换（深色为实验性）",
        icon = SmartisanOriginalIcons.Settings,
    ),
    Text(
        title = "文字",
        subtitle = "SmartisanText、像素字号、等宽数字",
        icon = SmartisanOriginalIcons.Sort,
    ),
    Icon(
        title = "图标",
        subtitle = "原版图标素材与库自绘补充图标",
        icon = SmartisanOriginalIcons.FavoriteAdd,
    ),
    Button(
        title = "按钮",
        subtitle = "强调、中性、文字三种按钮",
        icon = SmartisanOriginalIcons.Confirm,
    ),
    Control(
        title = "基础控件",
        subtitle = "开关、复选框、单选、评分条、分段按钮组、数字滚轮",
        icon = SmartisanOriginalIcons.NameEditor,
    ),
    Input(
        title = "文本与输入",
        subtitle = "搜索栏、自动缩字与两端对齐文本、密码框、可清空输入框",
        icon = SmartisanOriginalIcons.Search,
    ),
    Layout(
        title = "布局与列表",
        subtitle = "标题栏、列表行、分组、标签栏、滚动条、空态、流式布局",
        icon = SmartisanOriginalIcons.PlayAll,
    ),
    ListInteraction(
        title = "列表交互",
        subtitle = "拖动排序、侧滑删除、A–Z 字母索引",
        icon = SmartisanOriginalIcons.Drag,
    ),
    Overlay(
        title = "浮层",
        subtitle = "弹窗、底部菜单、底部弹层",
        icon = SmartisanOriginalIcons.Share,
    ),
    Clock(
        title = "时钟与机械控件",
        subtitle = "机械表盘、时间滚轮、标尺、星期选择",
        icon = SmartisanOriginalIcons.TabWorldClock,
    ),
}
