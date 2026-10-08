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

> **This library ships the original graphic assets.** Smartisan's style is skeuomorphic —
> its texture comes from NinePatches, bitmaps and selectors — so instead of redrawing everything
> in Compose, the library uses the assets the three revival projects recovered from the original
> APKs (1382 files, including night-mode and per-density variants).
> Those assets belong to their respective rights holders; please read
> [Asset sources and licensing](#asset-sources-and-licensing) before using them.

## Features

- **Pure Compose**: no Material / Material3 dependency — only `androidx.compose.foundation`,
  `androidx.compose.ui`, `androidx.compose.runtime` and `androidx.compose.animation`.
- **Smartisan visual language**: square layouts, hairline dividers, ripple-free press feedback,
  blue multi-select highlighting, charcoal dark mode.
- **Complete theming system**: semantic colors, text styles, shapes, dimension tokens and motion
  specs, all overridable through `CompositionLocal`.
- **Light by default, dark optional (experimental)**: the original Smartisan OS shipped a single
  light design, so this library defaults to light too; "follow the system" and "dark" must be opted
  into explicitly. The dark baseline reuses the charcoal palette already calibrated by the Weather
  revival (page `#25282D`, title bar `#292C31`, card `#34373C`).
- **Three original implementations merged into one**: switches, dialogs, title bars, drawable
  painting and press feedback have all been deduplicated.
- **Mechanical clock components**: dials, time wheels and timer rulers that used to be
  XML + custom `View`s are reimplemented entirely on the Compose Canvas.
- **Original graphic assets**: title bars, switches, dialogs, list rows, group cards, mechanical
  dials and rulers all use the original artwork, with real night-mode variants rather than
  simple colour inversion.
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

### Dark mode (experimental, off by default)

**The original Smartisan OS has no dark mode**, so this library is light by default:
`SmartisanTheme` and `rememberSmartisanThemeController` both default to
`SmartisanColorSchemeMode.Light`. Following the system and forcing dark must be passed explicitly,
and it is up to you to tell your own users about it.

The dark scheme itself comes from design work added by the three revival projects: the charcoal
palette was first calibrated in the Weather revival, the Music revival reused it, and this library
packages it as `darkSmartisanColors()`.

Dark mode is therefore **experimental**, with two known limitations:

- only a minority of the original graphic assets have night variants: 194 of 1008 drawables
  (~19%); the remaining 814 are light-only and will keep showing light artwork in dark mode;
- the colour state lists under `res/color/` have no night versions at all, so dialog and menu text
  colours fall back to their light values; this library adds a night palette for the Clock revival's
  dialogs and menus on top of that.

If you need the same fidelity as the light theme, treat light as the reference.

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

## Asset sources and licensing

### Inventory

Every graphic asset and colour state list under `library/ui/src/main/res/` — **1382 files** — comes
from one of the three revival projects:

| Source | Files | Contents |
| --- | --- | --- |
| [SmartisanMusic-Revived](https://github.com/Mangi-11/SmartisanMusic-Revived) | 607 | Title bar background and shadow, title bar icon selectors, switch bitmaps, dialog and menu backgrounds, list row and group card selectors, checkbox and radio selectors, rating stars, bottom tab icons, popup menu backgrounds |
| [SmartisanWeather-Revived](https://github.com/Mangi-11/SmartisanWeather-Revived) | 414 | Page texture `list_bg`, title bar icon selectors, red long-button selector, city item selectors |
| [SmartisanClock-Revived](https://github.com/Mangi-11/SmartisanClock-Revived) | 359 | Large and small mechanical dials with hands, tick marks and numerals, ringing ear frame sequence, time wheels, timer caliper and scale, the 6.8.0 pull-ring 30-frame sequence, stopwatch buttons, letter index bar, swipe-delete panel |

Assets keep their original file names and qualifier directories (`drawable-night`,
`drawable-xxhdpi`, and so on). Only two adjustments were made:

1. XML selectors that were identical (or differed only in formatting) across repos were kept once;
2. Music and Weather each had a `title_bar_shadow`; the Music NinePatch version was kept because it
   stretches horizontally.

In addition, the 8 colours and 1 `<drawable>` referenced by those selectors were added to
`values/smartisanx_assets_colors.xml` (with a `values-night` counterpart).

### Licensing

- These assets are **original Smartisan OS artwork**, extracted and restored from factory APKs by
  the three revival projects. The intellectual property belongs to Smartisan Technology and the
  relevant rights holders.
- The three revival projects publish these assets for learning, research and preserving the
  experience of legacy software. This library should likewise be used only for learning, research
  and personal projects — **not commercially**.
- If you need this for commercial work, replace the `@DrawableRes` parameters with your own assets,
  or point `SmartisanDrawables` at your own resources. Every asset entry point in the components is
  a replaceable parameter.
- The Kotlin component code and the graphic assets are separate concerns: delete
  `library/ui/src/main/res/drawable*` and you are left with a pure-Compose implementation that
  ships none of the original artwork.

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
  restoration of the original Smartisan OS. Every graphic asset under
  `library/ui/src/main/res/` is taken directly from these three repositories as well.
  Without them there would be no component library.

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
- This library **does contain** graphic assets (bitmaps, NinePatches, selectors) restored from the
  original APKs by the three revival projects. That artwork belongs to Smartisan Technology and the
  relevant rights holders; this library merely reuses work those projects already published and
  claims no rights over it. See [Asset sources and licensing](#asset-sources-and-licensing).
- This library contains no fonts or audio from the original APKs.
- It is intended for learning, research and personal projects. Do not use it commercially or in
  ways that infringe on the rights of others.

## License

This project is licensed under the [Apache License 2.0](LICENSE).

The three original revival projects and this project are independent implementations; refer to each
repository for its own license.
