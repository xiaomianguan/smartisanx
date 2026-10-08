# smartisanx

锤子风格（Smartisan OS）Jetpack Compose 组件库。

**中文** · [English](README_en_US.md)

`smartisanx` 把三个锤子应用复刻项目里的自定义 UI 组件抽出来、去重、统一 API，
做成一套可以直接在任意 Android Compose 工程里使用的组件库：

- [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived)（锤子音乐复刻）
- [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived)（锤子天气复刻）
- [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived)（锤子时钟复刻）

项目结构参考 [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)：
按职责拆分模块、示例应用独立成模块、组件按功能分包。

> **本库包含原始图形资源。** 锤子风格是拟物设计，质感来自 NinePatch、位图与 selector，
> 因此本库直接使用三个复刻项目从原版 APK 还原的素材（1382 个文件，含夜间与多密度变体），
> 而不是用 Compose 重新画一遍。这些素材的知识产权归原权利人所有，
> 使用前请先阅读[资源来源与授权](#资源来源与授权)。

## 特性

- **纯 Compose 实现**：不依赖 Material / Material3，只用 `androidx.compose.foundation`、
  `androidx.compose.ui`、`androidx.compose.runtime`、`androidx.compose.animation`。
- **锤子视觉语言**：方正布局、细线分隔、无涟漪按压反馈、蓝色多选高亮、炭灰深色模式。
- **完整主题系统**：语义色板、文字样式、形状、尺寸常量、动画规格，全部可通过
  `CompositionLocal` 覆盖。
- **默认浅色，深色可选（实验性）**：原版 Smartisan OS 只有浅色一套设计，
  所以本库默认也是浅色；「跟随系统」与「深色」需要开发者显式开启。
  深色基线沿用锤子天气复刻已经校准过的炭灰色板
  （页底 `#25282D`、标题栏 `#292C31`、卡片 `#34373C`）。
- **三套原始实现合一**：开关、弹窗、标题栏、drawable 绘制、按压反馈等重复实现已合并。
- **时钟机械控件**：机械表盘、时间滚轮、计时标尺等原本是 XML + 自定义 View 的组件，
  这里全部用 Compose Canvas 重写。
- **原始图形资源**：标题栏、开关、弹窗、列表行、分组卡片、机械表盘、标尺等
  全部使用原版素材，夜间模式也有对应的原版资源，不是简单反色。
- **简体中文文档**：所有 KDoc、示例与说明均为简体中文。

## 模块

| 模块 | 说明 |
| --- | --- |
| `library/core` | 主题、色板、文字样式、形状、尺寸、动画规格、按压反馈、drawable 绘制等基础能力 |
| `library/ui` | 全部组件：基础、控件、布局、列表交互、浮层、时钟 |
| `library/icons` | 矢量图标集（通用 / 状态 / 媒体 / 时钟），可独立使用 |
| `sample` | 示例应用，逐个展示所有组件的用法与参数 |

## 目录结构

```
smartisanx/
├── library/
│   ├── core/src/main/kotlin/cc/wuersan008/smartisanx/core/
│   │   ├── anim/         SmartisanMotion 动画规格
│   │   ├── interaction/  按压反馈、点击音效、触感
│   │   ├── theme/        色板、文字样式、形状、尺寸、主题与控制器
│   │   └── utils/        drawable 绘制、阴影修饰符
│   ├── ui/src/main/kotlin/cc/wuersan008/smartisanx/ui/
│   │   ├── basic/        Surface、Text、Icon、Divider
│   │   ├── control/      开关、复选框、单选、按钮、评分条、分段按钮组、数字滚轮
│   │   ├── input/        搜索栏、密码框、可清空输入框、自动缩字与两端对齐文本
│   │   ├── layout/       标题栏、列表行、分组、标签栏、滚动条、空态、流式布局
│   │   ├── list/         拖动排序、侧滑删除、字母索引
│   │   ├── overlay/      弹窗、菜单弹窗、底部弹层
│   │   └── clock/        机械表盘、小表盘、时间滚轮、标尺、星期选择
│   └── icons/src/main/kotlin/cc/wuersan008/smartisanx/icons/
├── sample/               示例应用
└── docs/                 中文文档
```

## 文档

| 中文文档 | English | 内容 |
| --- | --- | --- |
| [docs/快速开始.md](docs/快速开始.md) | [QuickStart.md](docs/QuickStart.md) | 环境要求、引入依赖、第一个界面、深浅色、常见问题 |
| [docs/组件总览.md](docs/组件总览.md) | [Components.md](docs/Components.md) | 全部组件的 API 与参数说明 |
| [docs/主题与设计变量.md](docs/主题与设计变量.md) | [Theme.md](docs/Theme.md) | 色板、文字样式、形状、尺寸、动画规格的完整取值 |
| [docs/从三个复刻项目迁移.md](docs/从三个复刻项目迁移.md) | [Migration.md](docs/Migration.md) | 原文件与 smartisanx 组件的逐项对照、迁移注意事项 |

## 快速开始

### 1. 添加依赖

在 `settings.gradle.kts` 里加入本仓库（或发布到本地 Maven 后直接依赖）：

```kotlin
// settings.gradle.kts
include(":library:core")
include(":library:ui")
include(":library:icons")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":library:ui"))   // 组件（会自动带上 core）
    implementation(project(":library:icons")) // 可选：矢量图标
}
```

最低要求：`minSdk 26`、Kotlin 2.x、Compose BOM 2025.05.01 及以上。

如果不想用源码依赖，也可以先发布到本地 Maven 再引用：

```bash
./gradlew publishToMavenLocal
```

发布坐标：`cc.wuersan008.smartisanx:smartisanx-core`、`cc.wuersan008.smartisanx:smartisanx-ui`、
`cc.wuersan008.smartisanx:smartisanx-icons`（版本 `0.1.0`）。

### 2. 包裹主题

所有组件都必须放在 `SmartisanTheme` 内，否则会抛出明确的异常提示：

```kotlin
@Composable
fun App() {
    // 默认就是浅色；要跟随系统或深色需显式传入（实验性，见下文）
    val controller = rememberSmartisanThemeController()
    SmartisanTheme(controller) {
        // controller.colorSchemeMode = SmartisanColorSchemeMode.System
        SmartisanScaffold(
            titleBar = {
                SmartisanTitleBar(
                    title = "锤子风格",
                    navigationIcon = SmartisanTitleBarAction(SmartisanXIcons.Back, "返回") { /* ... */ },
                )
            },
        ) {
            SmartisanGroup {
                SmartisanSwitchRow(
                    text = "智能音效",
                    summary = "整行可点",
                    checked = true,
                    onCheckedChange = { /* ... */ },
                )
                SmartisanRowDivider()
                SmartisanListItem(title = "资料库", summary = "12 首歌曲", onClick = { /* ... */ })
            }
        }
    }
}
```

### 3. 只用某一部分

`core` 与 `icons` 可以独立使用，例如只想要色板和图标：

```kotlin
SmartisanTheme {
    val colors = LocalSmartisanColors.current
    SmartisanIcon(SmartisanXMediaIcons.Play, contentDescription = "播放", tint = colors.accent)
}
```

## 组件总览

### 主题（`cc.wuersan008.smartisanx.core.theme`）

| 名称 | 说明 |
| --- | --- |
| `SmartisanTheme` | 提供色板、文字样式、形状 |
| `ThemeController` / `rememberSmartisanThemeController` | 跟随系统 / 浅色 / 深色 |
| `SmartisanColors` | 30 个语义色，浅色与深色各一套 |
| `SmartisanTypography` | 标题栏、正文、列表一二级、按钮、弹窗、等宽数字等 |
| `SmartisanShapes` | 直角、2/4/8/10/16dp 圆角 |
| `SmartisanDimens` | 标题栏、图标、列表、弹窗、底部栏等尺寸常量 |

### 动画与交互（`cc.wuersan008.smartisanx.core.anim` / `interaction`）

| 名称 | 说明 |
| --- | --- |
| `SmartisanMotion` | 原版余弦缓入缓出、按压弹簧、开关落位时长 |
| `Modifier.smartisanClickable` | 无涟漪点击 |
| `collectSmartisanPressedAsState` | 保留同帧快速点击的按压态 |
| `smartisanClick` / `smartisanHaptic` | 系统点击音效与虚拟按键触感 |

### 基础（`cc.wuersan008.smartisanx.ui.basic`）

`SmartisanSurface`、`SmartisanText`、`SmartisanPixelText`、`SmartisanIcon`、
`SmartisanIconButton`、`SmartisanDivider`、`SmartisanRowDivider`

### 控件（`cc.wuersan008.smartisanx.ui.control`）

`SmartisanSwitch`、`SmartisanSwitchRow`、`SmartisanCheckbox`、`SmartisanRadioButton`、
`SmartisanRadioRow`、`SmartisanButton`、`SmartisanTextButton`、`SmartisanRatingBar`、
`SmartisanButtonTabGroup`、`SmartisanHammerButton`、`SmartisanNumberPicker`、
`SmartisanPageIndicator`、`SmartisanProgressIndicator`、`SmartisanTips`

### 输入（`cc.wuersan008.smartisanx.ui.input`）

`SmartisanSearchBar`、`SmartisanAutoFitText`、`SmartisanJustifyText`、
`SmartisanPasswordField`、`SmartisanClearableField`、`SmartisanInputDefaults`

### 布局（`cc.wuersan008.smartisanx.ui.layout`）

`SmartisanScaffold`、`SmartisanTitleBar`、`SmartisanTitleBarSurface`、`SmartisanListItem`、
`SmartisanGroup`、`SmartisanSectionTitle`、`SmartisanCard`、`SmartisanTabRow`、
`SmartisanBottomBar`、`Modifier.smartisanVerticalScrollbar`、`SmartisanEmptyHint`、
`SmartisanFlowLayout`

### 列表交互（`cc.wuersan008.smartisanx.ui.list`）

`SmartisanReorderableColumn`、`SmartisanSwipeToDelete`、`SmartisanLetterIndexBar`

### 浮层（`cc.wuersan008.smartisanx.ui.overlay`）

`SmartisanModal`、`SmartisanModalWindow`、`SmartisanDialog`、`SmartisanConfirmDialog`、
`SmartisanDialogTitleBar`、`SmartisanDialogButton`、`SmartisanMenuDialog`、`SmartisanMenuItem`、
`SmartisanBottomSheet`、`SmartisanSheetScaffold`

### 时钟（`cc.wuersan008.smartisanx.ui.clock`）

`SmartisanAnalogClock`、`SmartisanCompactClock`、`SmartisanTimePicker`、`SmartisanWheelPicker`、
`SmartisanRulerPicker`、`SmartisanPullRingRuler`、`SmartisanWeekdayPicker`

### 图标（`cc.wuersan008.smartisanx.icons`）

`SmartisanXIcons`、`SmartisanXStatusIcons`、`SmartisanXMediaIcons`、`SmartisanXClockIcons`

## 示例应用

`sample` 模块逐个展示所有组件，包含主题切换、深浅色预览与交互演示：

```bash
./gradlew :sample:assembleDebug
# 产物：sample/build/outputs/apk/debug/sample-debug.apk
```

示例应用的分页与组件分组一一对应：主题与设计变量、文字、图标、按钮、基础控件、
文本与输入、布局与列表、列表交互、浮层、时钟与机械控件。

## 设计说明

### 色板

语义色板由三个项目的实际取色合并而成，去掉了同义重复：

- 浅色：页底 `#FFFFFF`、分隔线 `#E9E9E9`、一级文字 `#CC000000`、强调色 `#E64040`、
  多选底色 `#E5EEFF`、按压高亮 `#4A69B3`、开关指示绿 `#72B27E`。
- 深色：页底 `#25282D`、标题栏 `#292C31`、卡片 `#34373C`、较高表面 `#41464D`、
  多选底色 `#26384F`。

### 深色模式（实验性，默认不启用）

**原版 Smartisan OS 没有深色模式**，所以本库默认使用浅色：
`SmartisanTheme` 与 `rememberSmartisanThemeController` 的默认值都是
`SmartisanColorSchemeMode.Light`。跟随系统与深色必须显式传入，
并由开发者自行在产品里向用户说明。

深色方案本身来自三个复刻项目的新增设计：
炭灰色板最早在锤子天气复刻里校准，锤子音乐复刻沿用同一套，本库把它整理成 `darkSmartisanColors()`。

因此深色模式属于**实验性**特性，有两点已知限制：

- 原版图形资源里带夜间变体的只占少数：1008 个 drawable 中只有 194 个（约 19%），
  其余 814 个只有浅色版本，深色下会继续显示浅色素材；
- `res/color/` 下的颜色状态列表完全没有夜间版本，弹窗、菜单等文字色只能沿用浅色取值；
  本库另外为锤子时钟的弹窗与菜单补了一套夜间颜色。

如果你需要与浅色同等的还原度，请以浅色为准。

### 字体

**默认使用锤子原厂字体**：正文是 Smartisan OS 的系统字体 `Smartisan Compact CNS`
（四档字重），机械数字用 `SmartisanClock` 三档字重。
正文取自坚果 R2 官方 ROM 转储的 `system/system/fonts/`，机械数字取自 `smartisanos_11.apk` 的 `assets/`。

想换字体（系统默认 / 自己的字体 / 只换机械数字）见
[主题与设计变量](docs/主题与设计变量.md#字体)。

### 页面底纹与卡片投影

锤子应用的页面不是纯色底：**整页是一张细竖条纹布纹**，内容放在**带向外投影的卡片**里。

- 底纹是 `common_bg`（270×270 的浅灰细竖条纹），原版通过 `list_bg` / `account_background`
  以 `tileMode="repeat"` 平铺满屏。`SmartisanScaffold` 默认就用它，
  传 `backgroundRes = null` 可退回纯色。
- 卡片不是 Compose 的 elevation 阴影，而是**「内容底图 + 向外扩张的阴影 9-patch」两层**：
  内容底图用 `group_list_item_bg_top/mid/bottom/single`，
  阴影用 `list_content_item_top/middle/bottom/single_shadow`，
  阴影按自己的 9-patch padding 向四周扩张，因此投影落在控件边界**之外**。
  对应实现是 `Modifier.smartisanShadowBackground(backgroundRes, shadowRes)`。
- 因为投影画在边界外，行与分组需要留出边距：原版是左右 `list_item_left_right_margin`(12dp)、
  上下 `list_item_vertical_gap`(14dp)，本库对应 `SmartisanDimens.ListItemHorizontalMargin`
  与 `SmartisanDimens.ListItemVerticalGap`。

```kotlin
SmartisanScaffold {                       // 默认已带平铺底纹
    SmartisanGroup {
        // 自动配好该位置的原版底图与投影
        SmartisanGroupItem(SmartisanGroupRowPosition.Top, "第一行")
        SmartisanGroupItem(SmartisanGroupRowPosition.Middle, "中间行")
        SmartisanGroupItem(SmartisanGroupRowPosition.Bottom, "最后一行")
    }
}
```

### 尺寸

关键尺寸沿用原版资源：标题栏 48dp、图标 36dp、列表行最小高度 48dp、分隔线 0.67dp、
弹窗宽 308dp、弹窗圆角 10dp、弹窗按钮高 48dp、底部栏 50dp。

### 动效

原版使用 `ViewPropertyAnimator` / `AnimatorSet` 的默认插值器，
本库把它还原为余弦缓入缓出：`cos((t + 1) * π) / 2 + 0.5`（见 `SmartisanMotion.EaseInOut`）。
开关滑块落位时长按原版「每 350px 用 88/3 毫秒」等比换算。

### 不使用涟漪

锤子原版的按压反馈是切换按压态资源、位移或缩放，而不是 Material 涟漪。
因此库内所有可点组件都使用 `indication = null`，并自行绘制按压态。

## 资源来源与授权

### 资源清单

`library/ui/src/main/res/` 下共 **1382** 个图形资源与颜色状态列表文件，全部来自三个复刻项目：

| 来源 | 文件数 | 内容 |
| --- | --- | --- |
| [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived) | 607 | 标题栏底色与投影、标题栏图标 selector、开关位图、弹窗与菜单底色、
列表行与分组卡片 selector、复选框与单选 selector、评分星、底部标签栏图标、弹层菜单底色 |
| [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived) | 414 | 页面底纹 `list_bg`、标题栏图标 selector、红色长按钮 selector、城市项 selector |
| [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived) | 359 | 大/小机械表盘与指针、刻度与数字、响铃耳朵帧序列、时间滚轮、
计时器卡尺与刻度、6.8.0 拉环 30 帧序列、秒表按钮、字母索引栏、侧滑删除面板 |

资源按原始文件名与原始限定符目录（`drawable-night`、`drawable-xxhdpi` 等）原样保留，
只做了两处处理：

1. 三个仓库里同名同内容（或仅排版不同）的 XML selector 只保留一份；
2. 音乐与天气各有一张 `title_bar_shadow`，保留了音乐的 NinePatch 版本（可横向拉伸）。

另外把 selector 引用到的 8 个颜色与 1 个 `<drawable>` 补进了
`values/smartisanx_assets_colors.xml`（含 `values-night`）。

### 授权说明

- 这些图形资源是 **Smartisan OS 原版素材**，由三个复刻项目从原厂 APK 中提取与还原，
  知识产权归锤子科技及相关权利人所有。
- 三个复刻项目以学习、研究与保留旧软件体验为目的公开了这些素材；
  本库同样只应被用于学习、研究与个人项目，**请勿用于商业用途**。
- 如果你的项目需要商用，请把组件里的 `@DrawableRes` 参数替换成你自己的素材，
  或者把 `SmartisanDrawables` 指向你自己的资源；组件的所有资源入口都是可替换的参数。
- 组件代码（Kotlin）与图形资源（PNG/NinePatch/XML）是两部分，
  只保留代码、移除 `library/ui/src/main/res/drawable*` 即可得到一套不含原版素材的纯 Compose 实现。

## 组件去重说明

三个复刻项目各自实现过一批功能相同的组件，本库把它们合并为唯一实现：

| 合并后的组件 | 来自 |
| --- | --- |
| `SmartisanDrawablePainter`（`rememberSmartisanDrawablePainter`） | 锤子音乐 `SmartisanDrawablePainter` + 锤子天气 `WeatherDrawablePainter` |
| `collectSmartisanPressedAsState` | 锤子音乐 `collectSmartisanPressedAsState` + 锤子天气 `collectWeatherPressedAsState` |
| `SmartisanSwitch` | 锤子音乐 Compose 开关 + 锤子时钟 `SmartisanSwitchView` / `SmartisanSwitchExView` |
| `SmartisanTitleBar` | 锤子音乐 `SmartisanTitleBar` + 锤子天气 `WeatherTitleBar` |
| `SmartisanModal` / `SmartisanDialog` | 锤子音乐 `SmartisanModal` + 锤子时钟 `SmartisanModalDialog` |
| `SmartisanMenuDialog` | 锤子音乐底部菜单弹窗 + 锤子时钟 `SmartisanMenuDialog` |
| `SmartisanButton` | 锤子音乐红色收缩按钮 + 锤子天气操作按钮 |
| 投影阴影修饰符 | 锤子音乐 `ShadowDrawable` + 锤子时钟 `SmartisanShadowDrawable` |
| 拖拽排序 | 锤子音乐 `SmartisanListDrag` + 锤子时钟 `WorldClockListView` |
| 字母索引栏 | 锤子音乐字母快捷栏 + 锤子时钟 `QuickBarEx` |
| 按压缩放动画常量 | 锤子音乐标题栏图标 + 锤子天气图标按钮 |

锤子时钟原本使用 XML Layout + 自定义 View，本库用 Compose Canvas 与 `pointerInput` 重写，
不依赖任何 View 宿主。

## 致谢

这个库不是从零发明的，它把别人的成果整理成了可复用的形式。

### 组件来源

- **[Mangi-11](https://github.com/Mangi-11)** —— 三个原始复刻项目的作者，也是本库全部组件的来源：
  - [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived)（锤子音乐复刻，Apache 风格的开源复刻项目）
  - [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived)（锤子天气复刻）
  - [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived)（锤子时钟复刻）

  本库的色板、尺寸、动画曲线、按压反馈、弹窗比例、列表让位逻辑、机械表盘与标尺手感，
  都来自这三个项目对原版 Smartisan OS 的还原工作。
  `library/ui/src/main/res/` 下的全部图形资源（NinePatch、位图、selector）也直接取自这三个仓库，
  没有这些复刻，就没有这套组件库。

- **[People-11](https://github.com/People-11)** —— SmartisanOS_APP_Port 项目的作者。
  三个复刻项目都基于该项目提供的原厂 APK 进行逆向分析，本库的视觉基准同样间接来自这里。

- **[compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)** —— 本库的工程结构参考：
  按职责拆分模块、示例应用独立成模块、组件按功能分包、主题通过 `CompositionLocal` 下发。

### 设计来源

- **锤子科技 / Smartisan OS** —— 原始设计语言（方正布局、拟物质感、机械动效、
  蓝色多选高亮、炭灰夜间视觉）的知识产权归原权利人所有。
  本库是对这套设计语言的**重新实现**，不包含原版 APK 的任何资源文件。

## 免责声明

- 本项目是非官方项目，与锤子科技、字节跳动及 Smartisan 品牌权利人无关。
- Smartisan OS、相关商标与原始视觉设计的知识产权归原权利人所有。
- 本库**包含**三个复刻项目从原版 APK 还原的图形资源（位图、NinePatch、selector）。
  这些素材的知识产权归锤子科技及相关权利人所有，本库只是沿用三个复刻项目已经公开的还原成果，
  并未主张任何权利，详见[资源来源与授权](#资源来源与授权)。
- 本库不含原版 APK 的字体与音频资源。
- 本库仅供学习、研究与个人项目使用，请勿用于商业用途或侵犯他人权利的场景。

## 许可证

本项目使用 [Apache License 2.0](LICENSE)。

三个原始复刻项目与本项目均为独立实现，许可证以各自仓库为准。
