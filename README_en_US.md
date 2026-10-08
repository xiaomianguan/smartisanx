# smartisanx

A Jetpack Compose UI library in the Smartisan OS (Smartisan / "hammer") visual language.

`smartisanx` extracts the custom UI components from three Smartisan app revival projects,
removes the duplicates, unifies their APIs, and packages them as a component library you can
drop into any Android Compose project:

- [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived) (Smartisan Music revival)
- [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived) (Smartisan Weather revival)
- [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived) (Smartisan Clock revival)

The project layout follows [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix):
modules split by responsibility, the demo app as its own module, components grouped by function.

[中文](README.md) · **English**

> This library contains no bitmaps, NinePatches, or fonts from the original APKs.
> Every icon is a 24×24 vector path and every surface is drawn by Compose,
> so you are free to use it in your own projects.

## Features

- **Pure Compose**: no Material / Material3 dependency — only `androidx.compose.foundation`,
  `androidx.compose.ui`, `androidx.compose.runtime` and `androidx.compose.animation`.
- **Smartisan visual language**: square layouts, hairline dividers, ripple-free press feedback,
  blue multi-select highlighting, charcoal dark mode.
- **Complete theming system**: semantic colors, text styles, shapes, dimension tokens and motion
  specs, all overridable through `CompositionLocal`.
- **Light/dark and night visuals**: the dark baseline reuses the charcoal palette already
  calibrated by the Weather revival (page `#25282D`, title bar `#292C31`, card `#34373C`).
- **Three original implementations merged into one**: switches, dialogs, title bars, drawable
  painting and press feedback have all been deduplicated.
- **Mechanical clock components**: dials, time wheels and timer rulers that used to be
  XML + custom `View`s are reimplemented entirely on the Compose Canvas.
- **Simplified Chinese docs first**: the primary docs are Chinese; this file and the
  `docs/*.md` English pages are translations of them.

## Modules

| Module | Description |
| --- | --- |
| `library/core` | Theme, colors, text styles, shapes, dimensions, motion specs, press feedback, drawable painting |
| `library/ui` | All components: basic, controls, layout, list interaction, overlays, clock |
| `library/icons` | Vector icon set (general / status / media / clock), usable standalone |
| `sample` | Demo app that walks through every component and its parameters |

## Repository layout

```
smartisanx/
├── library/
│   ├── core/src/main/kotlin/cc/wuersan008/smartisanx/core/
│   │   ├── anim/         SmartisanMotion specs
│   │   ├── interaction/  Press feedback, click sound, haptics
│   │   ├── theme/        Colors, text styles, shapes, dimensions, theme & controller
│   │   └── utils/        Drawable painting, shadow modifiers
│   ├── ui/src/main/kotlin/cc/wuersan008/smartisanx/ui/
│   │   ├── basic/        Surface, Text, Icon, Divider
│   │   ├── control/      Switch, checkbox, radio, button, rating bar
│   │   ├── layout/       Title bar, list item, group, tab row, scrollbar, empty state
│   │   ├── list/         Drag-to-reorder, swipe-to-delete, letter index
│   │   ├── overlay/      Dialogs, menu dialog, bottom sheet
│   │   └── clock/        Analog dial, compact dial, time wheels, rulers, weekday picker
│   └── icons/src/main/kotlin/cc/wuersan008/smartisanx/icons/
├── sample/               Demo app
└── docs/                 Chinese docs (English translations live beside them)
```

## Documentation

| Document | Contents |
| --- | --- |
| [QuickStart.md](docs/QuickStart.md) | Requirements, adding the dependency, first screen, dark mode, FAQ |
| [Components.md](docs/Components.md) | API and parameters for every component |
| [Theme.md](docs/Theme.md) | Full values for colors, text styles, shapes, dimensions and motion |
| [Migration.md](docs/Migration.md) | File-by-file mapping from the three revival projects, migration notes |

Chinese originals: [docs/快速开始.md](docs/快速开始.md) · [docs/组件总览.md](docs/组件总览.md) ·
[docs/主题与设计变量.md](docs/主题与设计变量.md) · [docs/从三个复刻项目迁移.md](docs/从三个复刻项目迁移.md)

## Quick start

### 1. Add the dependency

Include this repository in `settings.gradle.kts` (or publish it to your local Maven first):

```kotlin
// settings.gradle.kts
include(":library:core")
include(":library:ui")
include(":library:icons")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":library:ui"))    // components (pulls in core automatically)
    implementation(project(":library:icons")) // optional: vector icons
}
```

Minimum requirements: `minSdk 26`, Kotlin 2.x, Compose BOM 2025.05.01 or newer.

If you would rather not use a source dependency, publish to your local Maven cache first:

```bash
./gradlew publishToMavenLocal
```

Published coordinates: `cc.wuersan008.smartisanx:smartisanx-core`,
`cc.wuersan008.smartisanx:smartisanx-ui`, `cc.wuersan008.smartisanx:smartisanx-icons` (version `0.1.0`).

### 2. Wrap your UI in the theme

Every component must live inside `SmartisanTheme`; otherwise it throws an explicit exception:

```kotlin
@Composable
fun App() {
    val controller = rememberSmartisanThemeController()
    SmartisanTheme(controller) {
        // controller.colorSchemeMode = SmartisanColorSchemeMode.Dark
        SmartisanScaffold(
            titleBar = {
                SmartisanTitleBar(
                    title = "Smartisan style",
                    navigationIcon = SmartisanTitleBarAction(SmartisanXIcons.Back, "Back") { /* ... */ },
                )
            },
        ) {
            SmartisanGroup {
                SmartisanSwitchRow(
                    text = "Smart audio",
                    summary = "Whole row is tappable",
                    checked = true,
                    onCheckedChange = { /* ... */ },
                )
                SmartisanRowDivider()
                SmartisanListItem(title = "Library", summary = "12 songs", onClick = { /* ... */ })
            }
        }
    }
}
```

### 3. Use only part of the library

`core` and `icons` work standalone. For example, if you only want the palette and the icons:

```kotlin
SmartisanTheme {
    val colors = LocalSmartisanColors.current
    SmartisanIcon(SmartisanXMediaIcons.Play, contentDescription = "Play", tint = colors.accent)
}
```

## Component overview

### Theme (`cc.wuersan008.smartisanx.core.theme`)

| Name | Description |
| --- | --- |
| `SmartisanTheme` | Provides colors, text styles and shapes |
| `ThemeController` / `rememberSmartisanThemeController` | System / light / dark |
| `SmartisanColors` | 30 semantic colors, one set for light and one for dark |
| `SmartisanTypography` | Title bar, body, list primary/secondary, button, dialog, tabular numerals |
| `SmartisanShapes` | Square plus 2/4/8/10/16dp corners |
| `SmartisanDimens` | Dimension tokens for title bar, icons, lists, dialogs, bottom bar |

### Motion and interaction (`cc.wuersan008.smartisanx.core.anim` / `interaction`)

| Name | Description |
| --- | --- |
| `SmartisanMotion` | Original cosine ease-in-out, press springs, switch settle duration |
| `Modifier.smartisanClickable` | Click without a ripple |
| `collectSmartisanPressedAsState` | Keeps same-frame quick taps visible |
| `smartisanClick` / `smartisanHaptic` | System click sound and virtual-key haptics |

### Basic (`cc.wuersan008.smartisanx.ui.basic`)

`SmartisanSurface`, `SmartisanText`, `SmartisanPixelText`, `SmartisanIcon`,
`SmartisanIconButton`, `SmartisanDivider`, `SmartisanRowDivider`

### Controls (`cc.wuersan008.smartisanx.ui.control`)

`SmartisanSwitch`, `SmartisanSwitchRow`, `SmartisanCheckbox`, `SmartisanRadioButton`,
`SmartisanRadioRow`, `SmartisanButton`, `SmartisanTextButton`, `SmartisanRatingBar`

### Layout (`cc.wuersan008.smartisanx.ui.layout`)

`SmartisanScaffold`, `SmartisanTitleBar`, `SmartisanTitleBarSurface`, `SmartisanListItem`,
`SmartisanGroup`, `SmartisanSectionTitle`, `SmartisanCard`, `SmartisanTabRow`,
`SmartisanBottomBar`, `Modifier.smartisanVerticalScrollbar`, `SmartisanEmptyHint`

### List interaction (`cc.wuersan008.smartisanx.ui.list`)

`SmartisanReorderableColumn`, `SmartisanSwipeToDelete`, `SmartisanLetterIndexBar`

### Overlays (`cc.wuersan008.smartisanx.ui.overlay`)

`SmartisanModal`, `SmartisanModalWindow`, `SmartisanDialog`, `SmartisanConfirmDialog`,
`SmartisanDialogTitleBar`, `SmartisanDialogButton`, `SmartisanMenuDialog`, `SmartisanMenuItem`,
`SmartisanBottomSheet`, `SmartisanSheetScaffold`

### Clock (`cc.wuersan008.smartisanx.ui.clock`)

`SmartisanAnalogClock`, `SmartisanCompactClock`, `SmartisanTimePicker`, `SmartisanWheelPicker`,
`SmartisanRulerPicker`, `SmartisanPullRingRuler`, `SmartisanWeekdayPicker`

### Icons (`cc.wuersan008.smartisanx.icons`)

`SmartisanXIcons`, `SmartisanXStatusIcons`, `SmartisanXMediaIcons`, `SmartisanXClockIcons`

## Demo app

The `sample` module walks through every component with theme switching, light/dark previews and
interactive demos:

```bash
./gradlew :sample:assembleDebug
# output: sample/build/outputs/apk/debug/sample-debug.apk
```

The demo pages map one-to-one onto the component groups: theme and design tokens, text, icons,
buttons, controls, layout and lists, list interaction, overlays, clock and mechanical controls.

## Design notes

### Palette

The semantic palette merges the actual colors used by the three projects and drops duplicates:

- Light: page `#FFFFFF`, divider `#E9E9E9`, primary text `#CC000000`, accent `#E64040`,
  multi-select background `#E5EEFF`, press highlight `#4A69B3`, switch indicator green `#72B27E`.
- Dark: page `#25282D`, title bar `#292C31`, card `#34373C`, raised surface `#41464D`,
  multi-select background `#26384F`.

### Dimensions

Key dimensions follow the original resources: title bar 48dp, icon 36dp, list row minimum height
48dp, divider 0.67dp, dialog width 308dp, dialog corner radius 10dp, dialog button height 48dp,
bottom bar 50dp.

### Motion

The original app relied on the default `ViewPropertyAnimator` / `AnimatorSet` interpolator; this
library expresses it as a cosine ease-in-out: `cos((t + 1) * PI) / 2 + 0.5`
(see `SmartisanMotion.EaseInOut`). The switch knob settle duration is scaled from the original
"88/3 ms per 350 px".

### No ripples

Smartisan's press feedback swaps pressed-state resources, offsets or scales — it is not a Material
ripple. Every tappable component in this library therefore uses `indication = null` and draws its
own pressed state.

## Deduplication notes

The three revival projects each implemented a set of equivalent components. This library merges
them into a single implementation:

| Merged component | Came from |
| --- | --- |
| `SmartisanDrawablePainter` (`rememberSmartisanDrawablePainter`) | Music `SmartisanDrawablePainter` + Weather `WeatherDrawablePainter` |
| `collectSmartisanPressedAsState` | Music `collectSmartisanPressedAsState` + Weather `collectWeatherPressedAsState` |
| `SmartisanSwitch` | Music Compose switch + Clock `SmartisanSwitchView` / `SmartisanSwitchExView` |
| `SmartisanTitleBar` | Music `SmartisanTitleBar` + Weather `WeatherTitleBar` |
| `SmartisanModal` / `SmartisanDialog` | Music `SmartisanModal` + Clock `SmartisanModalDialog` |
| `SmartisanMenuDialog` | Music bottom menu dialog + Clock `SmartisanMenuDialog` |
| `SmartisanButton` | Music red shrink button + Weather action button |
| Projected shadow modifier | Music `ShadowDrawable` + Clock `SmartisanShadowDrawable` |
| Drag-to-reorder | Music `SmartisanListDrag` + Clock `WorldClockListView` |
| Letter index bar | Music letter quick bar + Clock `QuickBarEx` |
| Press-scale animation constants | Music title bar icons + Weather icon button |

The Clock revival used XML layouts and custom `View`s; this library rewrites them with the Compose
Canvas and `pointerInput`, with no `View` host involved.

## Credits

This library was not invented from scratch — it packages other people's work in a reusable form.

### Where the components come from

- **[Mangi-11](https://github.com/Mangi-11)** — author of the three original revival projects and
the source of every component in this library:
  - [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived) (Smartisan Music revival)
  - [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived) (Smartisan Weather revival)
  - [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived) (Smartisan Clock revival)

  The palette, dimensions, easing curves, press feedback, dialog proportions, list displacement
  logic and the feel of the mechanical dials and rulers all come from these three projects'
  restoration of the original Smartisan OS. Without them there would be no component library.

- **[People-11](https://github.com/People-11)** — author of the SmartisanOS_APP_Port project.
  All three revival projects reverse-engineered factory APKs provided by that project, and this
  library's visual baseline traces back to it as well.

- **[compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)** — the engineering layout
  reference for this library: modules split by responsibility, the demo app as its own module,
  components grouped by function, theme delivered through `CompositionLocal`.

### Design attribution

- **Smartisan Technology / Smartisan OS** — the intellectual property of the original design
  language (square layouts, skeuomorphic textures, mechanical motion, blue multi-select
  highlighting, charcoal night visuals) belongs to its respective rights holders.
  This library is a **reimplementation** of that design language and ships no asset files from the
  original APKs.

## Disclaimer

- This is an unofficial project, not affiliated with Smartisan Technology, ByteDance, or the
  owners of the Smartisan brand.
- Smartisan OS, related trademarks and the original visual design belong to their respective
  rights holders.
- This library does not contain or distribute any bitmap, NinePatch, font or audio asset from the
  original APKs; all rendering is done by Compose code.
- It is intended for learning, research and personal projects. Do not use it commercially or in
  ways that infringe on the rights of others.

## License

This project is licensed under the [Apache License 2.0](LICENSE).

The three original revival projects and this project are independent implementations; refer to each
repository for its own license.
