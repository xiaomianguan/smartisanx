# smartisanx

锤子风格（Smartisan OS）Jetpack Compose 组件库。

`smartisanx` 把三个锤子应用复刻项目里的自定义 UI 组件抽出来、去重、统一 API，
做成一套可以直接在任意 Android Compose 工程里使用的组件库：

- [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived)（锤子音乐复刻）
- [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived)（锤子天气复刻）
- [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived)（锤子时钟复刻）

项目结构参考 [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)：
按职责拆分模块、示例应用独立成模块、组件按功能分包。

> 本库不包含任何来自原版 APK 的位图、NinePatch 或字体资源。
> 所有图标都是 24×24 的矢量路径，所有质感都由 Compose 绘制，
> 因此可以自由用于你自己的项目。

## 特性

- **纯 Compose 实现**：不依赖 Material / Material3，只用 `androidx.compose.foundation`、
  `androidx.compose.ui`、`androidx.compose.runtime`、`androidx.compose.animation`。
- **锤子视觉语言**：方正布局、细线分隔、无涟漪按压反馈、蓝色多选高亮、炭灰深色模式。
- **完整主题系统**：语义色板、文字样式、形状、尺寸常量、动画规格，全部可通过
  `CompositionLocal` 覆盖。
- **深浅色与夜间视觉**：深色基线沿用锤子天气复刻已经校准过的炭灰色板
  （页底 `#25282D`、标题栏 `#292C31`、卡片 `#34373C`）。
- **三套原始实现合一**：开关、弹窗、标题栏、drawable 绘制、按压反馈等重复实现已合并。
- **时钟机械控件**：机械表盘、时间滚轮、计时标尺等原本是 XML + 自定义 View 的组件，
  这里全部用 Compose Canvas 重写。
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
│   ├── core/src/main/kotlin/top/smartisanx/core/
│   │   ├── anim/         SmartisanMotion 动画规格
│   │   ├── interaction/  按压反馈、点击音效、触感
│   │   ├── theme/        色板、文字样式、形状、尺寸、主题与控制器
│   │   └── utils/        drawable 绘制、阴影修饰符
│   ├── ui/src/main/kotlin/top/smartisanx/ui/
│   │   ├── basic/        Surface、Text、Icon、Divider
│   │   ├── control/      开关、复选框、单选、按钮、评分条
│   │   ├── layout/       标题栏、列表行、分组、标签栏、滚动条、空态
│   │   ├── list/         拖动排序、侧滑删除、字母索引
│   │   ├── overlay/      弹窗、菜单弹窗、底部弹层
│   │   └── clock/        机械表盘、小表盘、时间滚轮、标尺、星期选择
│   └── icons/src/main/kotlin/top/smartisanx/icons/
├── sample/               示例应用
└── docs/                 中文文档
```

## 文档

| 文档 | 内容 |
| --- | --- |
| [docs/快速开始.md](docs/快速开始.md) | 环境要求、引入依赖、第一个界面、深浅色、常见问题 |
| [docs/组件总览.md](docs/组件总览.md) | 全部组件的 API 与参数说明 |
| [docs/主题与设计变量.md](docs/主题与设计变量.md) | 色板、文字样式、形状、尺寸、动画规格的完整取值 |
| [docs/从三个复刻项目迁移.md](docs/从三个复刻项目迁移.md) | 原文件与 smartisanx 组件的逐项对照、迁移注意事项 |

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

发布坐标：`top.smartisanx:smartisanx-core`、`top.smartisanx:smartisanx-ui`、
`top.smartisanx:smartisanx-icons`（版本 `0.1.0`）。

### 2. 包裹主题

所有组件都必须放在 `SmartisanTheme` 内，否则会抛出明确的异常提示：

```kotlin
@Composable
fun App() {
    val controller = rememberSmartisanThemeController()
    SmartisanTheme(controller) {
        // controller.colorSchemeMode = SmartisanColorSchemeMode.Dark
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

### 主题（`top.smartisanx.core.theme`）

| 名称 | 说明 |
| --- | --- |
| `SmartisanTheme` | 提供色板、文字样式、形状 |
| `ThemeController` / `rememberSmartisanThemeController` | 跟随系统 / 浅色 / 深色 |
| `SmartisanColors` | 30 个语义色，浅色与深色各一套 |
| `SmartisanTypography` | 标题栏、正文、列表一二级、按钮、弹窗、等宽数字等 |
| `SmartisanShapes` | 直角、2/4/8/10/16dp 圆角 |
| `SmartisanDimens` | 标题栏、图标、列表、弹窗、底部栏等尺寸常量 |

### 动画与交互（`top.smartisanx.core.anim` / `interaction`）

| 名称 | 说明 |
| --- | --- |
| `SmartisanMotion` | 原版余弦缓入缓出、按压弹簧、开关落位时长 |
| `Modifier.smartisanClickable` | 无涟漪点击 |
| `collectSmartisanPressedAsState` | 保留同帧快速点击的按压态 |
| `smartisanClick` / `smartisanHaptic` | 系统点击音效与虚拟按键触感 |

### 基础（`top.smartisanx.ui.basic`）

`SmartisanSurface`、`SmartisanText`、`SmartisanPixelText`、`SmartisanIcon`、
`SmartisanIconButton`、`SmartisanDivider`、`SmartisanRowDivider`

### 控件（`top.smartisanx.ui.control`）

`SmartisanSwitch`、`SmartisanSwitchRow`、`SmartisanCheckbox`、`SmartisanRadioButton`、
`SmartisanRadioRow`、`SmartisanButton`、`SmartisanTextButton`、`SmartisanRatingBar`

### 布局（`top.smartisanx.ui.layout`）

`SmartisanScaffold`、`SmartisanTitleBar`、`SmartisanTitleBarSurface`、`SmartisanListItem`、
`SmartisanGroup`、`SmartisanSectionTitle`、`SmartisanCard`、`SmartisanTabRow`、
`SmartisanBottomBar`、`Modifier.smartisanVerticalScrollbar`、`SmartisanEmptyHint`

### 列表交互（`top.smartisanx.ui.list`）

`SmartisanReorderableColumn`、`SmartisanSwipeToDelete`、`SmartisanLetterIndexBar`

### 浮层（`top.smartisanx.ui.overlay`）

`SmartisanModal`、`SmartisanModalWindow`、`SmartisanDialog`、`SmartisanConfirmDialog`、
`SmartisanDialogTitleBar`、`SmartisanDialogButton`、`SmartisanMenuDialog`、`SmartisanMenuItem`、
`SmartisanBottomSheet`、`SmartisanSheetScaffold`

### 时钟（`top.smartisanx.ui.clock`）

`SmartisanAnalogClock`、`SmartisanCompactClock`、`SmartisanTimePicker`、`SmartisanWheelPicker`、
`SmartisanRulerPicker`、`SmartisanPullRingRuler`、`SmartisanWeekdayPicker`

### 图标（`top.smartisanx.icons`）

`SmartisanXIcons`、`SmartisanXStatusIcons`、`SmartisanXMediaIcons`、`SmartisanXClockIcons`

## 示例应用

`sample` 模块逐个展示所有组件，包含主题切换、深浅色预览与交互演示：

```bash
./gradlew :sample:assembleDebug
# 产物：sample/build/outputs/apk/debug/sample-debug.apk
```

示例应用的分页与组件分组一一对应：主题与设计变量、文字、图标、按钮、基础控件、
布局与列表、列表交互、浮层、时钟与机械控件。

## 设计说明

### 色板

语义色板由三个项目的实际取色合并而成，去掉了同义重复：

- 浅色：页底 `#FFFFFF`、分隔线 `#E9E9E9`、一级文字 `#CC000000`、强调色 `#E64040`、
  多选底色 `#E5EEFF`、按压高亮 `#4A69B3`、开关指示绿 `#72B27E`。
- 深色：页底 `#25282D`、标题栏 `#292C31`、卡片 `#34373C`、较高表面 `#41464D`、
  多选底色 `#26384F`。

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
  都来自这三个项目对原版 Smartisan OS 的还原工作。没有这些复刻，就没有这套组件库。

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
- 本库不包含、也不分发任何来自原版 APK 的位图、NinePatch、字体或音频资源；
  所有绘制均由 Compose 代码完成。
- 本库仅供学习、研究与个人项目使用，请勿用于商业用途或侵犯他人权利的场景。

## 许可证

本项目使用 [Apache License 2.0](LICENSE)。

三个原始复刻项目与本项目均为独立实现，许可证以各自仓库为准。
