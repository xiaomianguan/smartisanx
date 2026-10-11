package cc.wuersan008.smartisanx.sample.layouts

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons

/**
 * 整页布局示例的分类。
 *
 * - [Original]：照原版应用的真实界面拼出来的，每个布局的 KDoc 里都写了它对应哪个 APK 的哪张布局；
 * - [Generic]：常见的页面骨架，原版不一定有对应实现，但用的仍然是同一套组件与设计变量，
 *   可以直接当自己应用的起点。
 */
enum class SampleLayoutCategory(val title: String) {
    Original("原版应用复刻"),
    Generic("通用页面起点"),
}

/**
 * 一个**整页**布局示例。
 *
 * 组件页（[cc.wuersan008.smartisanx.sample.SamplePage]）展示的是「单个组件长什么样」，
 * 这里展示的是「一整屏怎么拼」：每个条目打开后都是一张带自己标题栏的完整页面，
 * 内容全部由 smartisanx 的组件搭出来，没有一行是示例自己画的假组件。
 *
 * 所有布局共用同一套约定：
 *
 * 1. 页面骨架用 [cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold]（标题栏 / 内容 / 底部栏 +
 *    原版页面底纹），转场用 [cc.wuersan008.smartisanx.ui.anim.SmartisanPageTransition]；
 * 2. 图标一律取原版素材（[SmartisanDrawables] / [SmartisanOriginalIcons]），不自己画；
 * 3. 颜色、字号、间距一律走主题（`LocalSmartisanColors` / `LocalSmartisanTypography` /
 *    `SmartisanDimens`），示例里出现的具体 dp 值都标注了原版出处；
 * 4. 示例数据是写死的假数据（[SampleSong] 之类），业务应用里换成自己的数据源即可。
 *
 * [iconSize] 与组件页一样是图标位图的固有尺寸：原版这套位图画布大小不一，
 * 按固有尺寸渲染视觉大小才一致。
 */
enum class SampleLayout(
    val category: SampleLayoutCategory,
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val iconSize: Dp = 24.dp,
) {
    // ---- 原版应用复刻 ----

    MusicLibrary(
        category = SampleLayoutCategory.Original,
        title = "音乐 · 我的音乐",
        subtitle = "字母分组列表 + A–Z 索引条 + 底部标签栏",
        // 原版锤子音乐底部标签栏的「歌曲」图标。
        icon = SmartisanOriginalIcons.TabSong,
    ),
    MusicNowPlaying(
        category = SampleLayoutCategory.Original,
        title = "音乐 · 正在播放",
        subtitle = "黑胶转盘 + 进度条 + 传输控件",
        // 原版音乐列表行里的播放小图标。
        icon = SmartisanOriginalIcons.Play,
    ),
    ClockAlarm(
        category = SampleLayoutCategory.Original,
        title = "时钟 · 闹钟",
        subtitle = "顶部机械表盘 + 开关行列表 + 底部标签栏",
        // 原版锤子时钟底部标签栏的「闹钟」图标。
        icon = SmartisanOriginalIcons.TabAlarm,
    ),
    ClockWorld(
        category = SampleLayoutCategory.Original,
        title = "时钟 · 世界时钟",
        subtitle = "城市行（小表盘 + 时间）+ A–Z 索引条",
        // 原版锤子时钟底部标签栏的「世界时钟」图标。
        icon = SmartisanOriginalIcons.TabWorldClock,
    ),
    Contacts(
        category = SampleLayoutCategory.Original,
        title = "联系人 · 通讯录",
        subtitle = "头像列表 + 字母分组 + A–Z 索引条",
        // 原版联系人编辑页的联系人 glyph。
        icon = R.drawable.selector_editor_contact_icon,
    ),
    Notes(
        category = SampleLayoutCategory.Original,
        title = "笔记 · 全部笔记",
        subtitle = "标题 + 时间摘要的列表，带收藏标记",
        // 原版设置页编辑类设置项用的铅笔。
        icon = R.drawable.setting_item_icon_pencil,
    ),
    Calculator(
        category = SampleLayoutCategory.Original,
        title = "计算器",
        subtitle = "原版按键矩阵（记忆键 / 运算键 / 数字键盘）",
        // 原版数字键的键面位图（`amount` 的「=」细得像一条线，当列表图标看不清）。
        icon = R.drawable.d7,
    ),
    SettingsSound(
        category = SampleLayoutCategory.Original,
        title = "设置 · 声音与振动",
        subtitle = "开关行 + 图标滑块 + 单选行 + 跳转行",
        // 原版音乐播放页的音量图标。
        icon = SmartisanDrawables.VolumeHigh,
    ),

    // ---- 通用页面起点 ----

    ListDetail(
        category = SampleLayoutCategory.Generic,
        title = "列表 → 详情",
        subtitle = "两级页面栈：点列表行进详情，返回回到列表",
        // 原版音乐专辑页的「列表 / 网格」切换图标。
        icon = SmartisanDrawables.IconAlbumSwitchList,
    ),
    FormEditor(
        category = SampleLayoutCategory.Generic,
        title = "表单填写",
        subtitle = "编辑器行 + 开关行 + 底部操作按钮组",
        // 原版列表重命名用的图标。
        icon = SmartisanOriginalIcons.NameEditor,
    ),
    Profile(
        category = SampleLayoutCategory.Generic,
        title = "个人资料",
        subtitle = "头像抬头 + 信息分组 + 危险操作按钮",
        // 原版联系人页的头像 glyph。
        icon = R.drawable.button_icon_face_avatar,
    ),
    EmptyState(
        category = SampleLayoutCategory.Generic,
        title = "空态页",
        subtitle = "原版空态插图 + 说明 + 圆形操作按钮",
        // 原版音乐「没有文件夹」的空态插图。
        icon = SmartisanOriginalIcons.EmptyFolder,
    ),
    MultiSelect(
        category = SampleLayoutCategory.Generic,
        title = "多选编辑",
        subtitle = "多选标题栏 + 复选框行 + 隐藏行操作 + 红色删除条",
        // 原版标题栏的多选图标。
        icon = SmartisanOriginalIcons.MultiSelect,
    ),
}
