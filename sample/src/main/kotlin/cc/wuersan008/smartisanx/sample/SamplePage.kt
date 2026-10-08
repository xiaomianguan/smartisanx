package cc.wuersan008.smartisanx.sample

import androidx.compose.ui.graphics.vector.ImageVector
import cc.wuersan008.smartisanx.icons.SmartisanXClockIcons
import cc.wuersan008.smartisanx.icons.SmartisanXMediaIcons
import cc.wuersan008.smartisanx.icons.SmartisanXIcons
import cc.wuersan008.smartisanx.icons.SmartisanXStatusIcons

/**
 * 示例应用里的页面。
 *
 * 每个页面展示 smartisanx 的一个组件分组，全部使用简体中文说明。
 */
enum class SamplePage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    Theme(
        title = "主题与设计变量",
        subtitle = "色板、文字样式、形状、深浅色切换",
        icon = SmartisanXStatusIcons.Sun,
    ),
    Text(
        title = "文字",
        subtitle = "SmartisanText、像素字号、等宽数字",
        icon = SmartisanXIcons.Menu,
    ),
    Icon(
        title = "图标",
        subtitle = "原版图标素材与库自绘补充图标",
        icon = SmartisanXStatusIcons.Star,
    ),
    Button(
        title = "按钮",
        subtitle = "强调、中性、文字三种按钮",
        icon = SmartisanXIcons.Check,
    ),
    Control(
        title = "基础控件",
        subtitle = "开关、复选框、单选、评分条",
        icon = SmartisanXStatusIcons.Info,
    ),
    Layout(
        title = "布局与列表",
        subtitle = "标题栏、列表行、分组、标签栏、滚动条、空态",
        icon = SmartisanXMediaIcons.Queue,
    ),
    ListInteraction(
        title = "列表交互",
        subtitle = "拖动排序、侧滑删除、A–Z 字母索引",
        icon = SmartisanXIcons.DragHandle,
    ),
    Overlay(
        title = "浮层",
        subtitle = "弹窗、底部菜单、底部弹层",
        icon = SmartisanXIcons.Share,
    ),
    Clock(
        title = "时钟与机械控件",
        subtitle = "机械表盘、时间滚轮、标尺、星期选择",
        icon = SmartisanXClockIcons.Clock,
    ),
}
