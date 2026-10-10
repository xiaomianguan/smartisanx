# Theme and design tokens

[中文](主题与设计变量.md) · **English**

## Theme entry points

```kotlin
@Composable
fun SmartisanTheme(
    colors: SmartisanColors = lightSmartisanColors(),   // light by default
    typography: SmartisanTypography = SmartisanTypography(),
    shapes: SmartisanShapes = SmartisanShapes(),
    content: @Composable () -> Unit,
)

@Composable
fun SmartisanTheme(
    controller: ThemeController,
    typography: SmartisanTypography = SmartisanTypography(),
    shapes: SmartisanShapes = SmartisanShapes(),
    content: @Composable () -> Unit,
)
```

The theme is delivered through four `CompositionLocal`s:

| CompositionLocal | Contents |
| --- | --- |
| `LocalSmartisanColors` | Semantic palette |
| `LocalSmartisanTypography` | Text styles |
| `LocalSmartisanShapes` | Shapes |
| `LocalSmartisanContentColor` | Default content color (written by `SmartisanSurface`) |

`LocalSmartisanThemeController` additionally exposes the current controller (nullable).

## Palette

`SmartisanColors` merges the semantic colors of the three revival projects; after removing
duplicates it has 30 entries.

### Surfaces

| Name | Light | Dark | Usage |
| --- | --- | --- | --- |
| `pageBackground` | `#FFFFFF` | `#25282D` | Page background |
| `surface` | `#FFFFFF` | `#34373C` | Cards, lists, dialogs |
| `surfaceRaised` | `#F7F8F9` | `#41464D` | Raised group background |
| `surfacePressed` | `#ECECEC` | `#484D54` | Pressed state |
| `surfaceDisabled` | `#F2F2F2` | `#30343A` | Disabled state |
| `titleBarBackground` | `#FFFFFF` | `#292C31` | Title bar |

### Dividers

| Name | Light | Dark |
| --- | --- | --- |
| `divider` | `#E9E9E9` | `#3A3D42` |
| `rowDivider` | `#F2F2F2` | `#404348` |

### Text

| Name | Light | Dark | Usage |
| --- | --- | --- | --- |
| `textPrimary` | `#CC000000` | `#F2F2F2` | Primary text |
| `textSecondary` | `#9A000000` | `#D2D3D5` | Secondary text |
| `textTertiary` | `#66000000` | `#9FA1A4` | Tertiary text, section titles |
| `textDisabled` | `#4C000000` | `#717377` | Disabled |
| `textHint` | `#DBDBDB` | `#85878A` | Input hints |

### Accent and status

| Name | Light | Dark | Usage |
| --- | --- | --- | --- |
| `accent` | `#E64040` | `#E64040` | Smartisan red, brand accent |
| `accentPressed` | `#C14352` | `#FF5A5A` | Accent pressed |
| `accentDisabled` | `#66E64040` | `#66E64040` | Accent disabled |
| `onAccent` | `#FFFFFF` | `#FFFFFF` | Text on accent |
| `link` | `#5E80D0` | `#FF7839` | Links, tappable text |
| `linkPressed` | `#8A8A8A` | `#FF9A6D` | Link pressed |
| `success` | `#72B27E` | `#72B27E` | Success, switch indicator green |
| `warning` | `#E65C53` | `#FF7433` | Warning |

### Selection and switch

| Name | Light | Dark | Usage |
| --- | --- | --- | --- |
| `selectionBackground` | `#E5EEFF` | `#26384F` | Persistent multi-select background |
| `pressedHighlight` | `#4A69B3` | `#4A69B3` | Blue list press highlight |
| `onPressedHighlight` | `#FFFFFF` | `#FFFFFF` | Text on the blue highlight |
| `switchTrack` / `switchTrackStroke` / `switchKnob` / `switchIndicator` | see source | see source | Switch parts |
| `scrollbarThumb` | `#33000000` | `#40FFFFFF` | Scrollbar |
| `scrim` | `#8A000000` | `#99000000` | Overlay scrim |

`isLight` tells you whether the current palette is the light one, which is handy in custom drawing.

## Dark mode (experimental)

> **The original Smartisan OS has no dark mode, so this library is light by default; dark mode must
> be opted into.**

`SmartisanTheme` and `rememberSmartisanThemeController` both default to
`SmartisanColorSchemeMode.Light`. To follow the system or force dark you must pass it explicitly:

```kotlin
// default: light
SmartisanTheme { /* ... */ }

// opt-in: follow the system / dark (experimental)
val controller = rememberSmartisanThemeController(SmartisanColorSchemeMode.System)
SmartisanTheme(controller) { /* ... */ }

// or with the colors-only overload
SmartisanTheme(colors = systemSmartisanColors()) { /* ... */ }
```

The dark scheme comes from design work added by the three revival projects: the charcoal palette was
first calibrated in the Weather revival, the Music revival reused it, and this library packages it
as `darkSmartisanColors()`.

Dark mode is therefore **experimental**, with two known limitations:

| Limitation | Detail |
| --- | --- |
| Incomplete night artwork | Only 195 of 7919 original drawables (~2.5%) have night variants; the other 7724 are light-only and will keep showing light artwork in dark mode |
| No night colour state lists | There are no night variants under `res/color/` at all, so dialog and menu text colours use their light values; this library adds a night palette for the Clock revival's dialogs and menus |

`SmartisanTheme` writes "is the theme dark" into `LocalSmartisanDarkOverride`, and components resolve
`drawable-night` / `values-night` against the **app theme** rather than the system uiMode, so assets
follow an in-app light/dark switch.

If you need the same fidelity as the light theme, treat light as the reference.

### Page and overlay transitions

The timings come from the original APKs' `anim/` resources and the revival projects' implementations,
not from guesswork:

| Case | Original source | Behaviour |
| --- | --- | --- |
| Regular page (exit with back arrow) | Music `PageStackTransition` | New page **slides in from the right**, old page shifts left; 300ms, open `Smooth` = `(1-cos(t*PI))/2`, close `Decelerate` = `1-(1-t)^2` |
| Modal page (exit with x) | Weather `pop_up_in` / `slide_down_out` | **Slides up from the bottom**; enter `translateY` 100% -> 0, exit 0 -> 109%, both 300ms `decelerate_cubic`; the page underneath stays put (original `fake_anim`) |
| Centred dialog | Clock `smartisan_modal_enter/exit` | Scale 0.9 -> 1.0 while fading in; 300ms in, 250ms out, `decelerate` / `accelerate` |
| Bottom menu dialog | Clock `smartisan_menu_enter/exit` | `translateY` 100% -> 0 in 300ms, out in 250ms |

Usage:

```kotlin
// regular page: slides in from the right
SmartisanPageTransition(
    secondary = detail != null,
    primary = { Home(onOpen = { detail = it }) },
) {
    Detail(onBack = { detail = null })
}

// modal page: slides up from the bottom
SmartisanModalPageTransition(visible = sheetOpen) {
    SheetContent()
}
```

The easing constants live in `cc.wuersan008.smartisanx.ui.anim`:
`SmartisanNavigationOpenEasing`, `SmartisanNavigationCloseEasing`,
`SmartisanDecelerateCubic`, `SmartisanDecelerate`, `SmartisanAccelerate`;
the duration constant is `SmartisanNavigationDuration` (300ms).

## Fonts

**The original Smartisan fonts are used by default.**

| Purpose | Font | Source |
| --- | --- | --- |
| Body text | `Smartisan Compact CNS` (Founder, four weights) | Smartisan nut R2 factory ROM dump, `system/system/fonts/Smartisan_Compact-*.otf` |
| Mechanical numerals (clock, timer) | `SmartisanClock`, three weights | `assets/SmartisanClock*.ttf/otf` in `smartisanos_11.apk` |

The font files live in `library/core/src/main/res/font/`, wired through `SmartisanFonts.Original`,
and `SmartisanTheme` applies them to every text style via `withFonts()`.

### About smartisan-compact-cns

The Smartisan OS UI font is the system font **`Smartisan Compact CNS`**
(files `Smartisan_Compact-*.otf`, internal family name `Smartisan Compact CNS`, by Founder).
It lives in the ROM at `/system/fonts/`, not inside an APK - this library takes it from the nut R2
factory ROM dump: <https://dumps.tadiphone.dev/dumps/smartisan/darwin> -> `system/system/fonts/`.

The ROM ships six weights; four are wired in here (the other two are never referenced):

| Compose weight | ROM file | Wired in |
| --- | --- | --- |
| `Thin` (100) | `Smartisan_Compact-Thin.otf` | no (unused) |
| `Light` (300) | `Smartisan_Compact-Light.otf` | yes |
| `Normal` (400) | `Smartisan_Compact-Regular.otf` | yes |
| `Medium` (500) | `Smartisan_Compact-Medium.otf` | yes |
| `Bold` (700) | `Smartisan_Compact-Bold.otf` | yes |
| `Heavy` (900) | `Smartisan_Compact-Heavy.otf` | no (unused) |

To add Thin or Heavy, drop the file into `library/core/src/main/res/font/` and add one
`Font(...)` line to `SmartisanFonts.Original`.

### Using a different font

Three options.

**Follow the system default** (loads no font files):

```kotlin
SmartisanTheme(fonts = SmartisanFonts.SystemDefault) { /* ... */ }
```

**Use your own font:**

```kotlin
val myFont = FontFamily(Font(R.font.my_font))
SmartisanTheme(fonts = SmartisanFonts.custom(myFont)) { /* ... */ }
```

**Custom font for the mechanical numerals only:**

```kotlin
SmartisanTheme(
    fonts = SmartisanFonts.custom(text = null, numerals = myClockFont),
) { /* ... */ }
```

### How fonts relate to text styles

Every style in `SmartisanTypography` carries a `fontFamily`. `SmartisanTheme` applies the chosen
fonts to all of them with `withFonts()`: `numeric` and `displayNumeric` get the mechanical numeral
font, everything else gets the body font. To override individual styles, pass your own
`SmartisanTypography`.
## Page texture and card shadows

Smartisan screens are not flat: **the whole page is a fine vertical-stripe linen texture**, and content
sits in **cards with an outward shadow**. Neither is done with Compose `elevation` or a flat colour —
both come from original bitmaps.

### Texture

`common_bg` is a 270×270 pale grey stripe pattern. The original tiles it through two wrapper drawables:

| Resource | Contents | Used by |
| --- | --- | --- |
| `list_bg` | `<bitmap src="@drawable/common_bg" tileMode="repeat"/>` | Weather pages, night window background |
| `account_background` | same | Music shell, settings pages, playback queue |

In this library that is `SmartisanDrawables.PageBackground` (= `list_bg`).
`SmartisanScaffold` tiles it full-screen by default; pass `backgroundRes = null` for a flat colour.

### Card shadow

A card is **two layers**:

| Layer | Resource | Purpose |
| --- | --- | --- |
| Content | `group_list_item_bg_top` / `_mid` / `_bottom` / `_single` | The row's own corners, border and pressed state |
| Shadow | `list_content_item_top_shadow` / `_middle_shadow` / `_bottom_shadow` / `_single_shadow` | A nine-patch drawn expanded by its own padding |

Draw order is shadow first, then content. The shadow expands **outwards** by its nine-patch padding,
so the projection lands **outside** the control's bounds — exactly what the original `ShadowDrawable`
does.

API:

```kotlin
// low level: pick the background and shadow yourself
Modifier.smartisanShadowBackground(backgroundRes, shadowRes)

// high level: paired automatically by position
SmartisanGroupItem(SmartisanGroupRowPosition.Top, "First row")
SmartisanCard(backgroundRes = ..., shadowRes = ...) { /* ... */ }
```

### Margin

Because the projection is drawn outside the bounds, rows and groups must leave margin or the shadow
gets clipped:

| Name | Original resource | Value | Usage |
| --- | --- | --- | --- |
| `SmartisanDimens.ListItemHorizontalMargin` | `list_item_left_right_margin` | 12dp | horizontal margin of a group, applied by `SmartisanGroup` |
| `SmartisanDimens.ListItemVerticalGap` | `list_item_vertical_gap` | 14dp | a blank view **between** two groups, inserted via `SmartisanListVerticalGap()` |

In the original the vertical gap is a spacer view inserted between two groups
(`group_list_item_vertical_gap_layout`), not padding of the group itself — the shadows of the two
cards (each nine-patch expands 14dp up and down) overlap inside that single spacer. `SmartisanGroup`
therefore adds no vertical spacing of its own and only keeps the horizontal margin: insert a
`SmartisanListVerticalGap` between two adjacent groups. No gap is needed around a section title —
the title carries its own spacing.

### Row height

| Name | Original resource | Value |
| --- | --- | --- |
| `SmartisanDimens.ListItemHeight` | `listview_item_height` | 60dp |
| `SmartisanDimens.ListItemMinHeight` | `list_item_min_height` | 60dp |

The original's `list_content_item_layout.xml` is a `RelativeLayout` with
`android:minHeight="@dimen/list_item_min_height"`, and `list_item_min_height` is **60dp** — the same
value as `listview_item_height`. So a one-line row is 60dp and only rows with more content (a second
summary line, a two-line title) grow taller. `SmartisanListItem`'s default `minHeight` is
`ListItemMinHeight`, so every row lands on that grid.

## Text styles

| Name | Size | Weight | Usage |
| --- | --- | --- | --- |
| `titleBar` | 20sp | Bold | Title bar title |
| `title` | 20sp | Medium | Page heading |
| `body` | 15sp | Normal | Body text |
| `listItemPrimary` | 15sp | Normal | List primary line |
| `listItemSecondary` | 12.5sp | Normal | List secondary line |
| `sectionTitle` | 13.5sp | Normal | Section title |
| `button` | 14sp | Bold | Buttons |
| `dialogButton` | 17sp | Bold | Dialog actions |
| `dialogTitle` | 13.5sp | Bold | Dialog title |
| `caption` | 12sp | Normal | Captions, footers |
| `numeric` | 15sp | Normal | Tabular numerals (`tnum`) |
| `displayNumeric` | 48sp | Light | Large clock/timer numerals |
| `listRowPrimary` | 17sp | Normal | Framework list-row primary (`primary_text_size`) |
| `listRowPrimaryAlt` | 16sp | Normal | Framework list-row primary, compact (`primary_text_size_alt`) |
| `listRowSecondary` | 15sp | Normal | Framework list-row secondary (`secondary_text_size`) |
| `listRowTertiary` | 13.5sp | Normal | Framework list-row tertiary (`tertiary_text_size`) |
| `listRowTertiaryAlt` | 12.5sp | Normal | Framework list-row tertiary, compact (`tertiary_text_size_alt`) |
| `listRowQuaternary` | 12sp | Normal | Framework list-row quaternary (`quaternary_text_size`) |
| `listRowSwitchTitle` | 18sp | Normal | Switch-row title (`switch_title_size`) |
| `listItemCaptionSmall` | 10sp | Normal | Small in-row caption (`item_sub_title_size`) |
| `editorLabel` | 12sp | Normal | Framework editor-row label (`EditorLabelTextStyle`) |
| `editorField` | 15sp | Normal | Framework editor-row input text (`EditorTextStyle`) |

Note that the framework list-row sizes and the app versions inside the three revival projects come
from **different sources**: `listItemPrimary` is 15sp, `listRowPrimary` is 17sp.

`SmartisanText` defaults its `style` parameter to `body`. You can also read
`LocalSmartisanTypography.current` and `copy` from it.

## Shapes

| Name | Corner radius |
| --- | --- |
| `none` | 0dp (lists, title bars, groups by default) |
| `extraSmall` | 2dp |
| `small` | 4dp |
| `medium` | 8dp |
| `dialog` | 10dp |
| `sheet` | 10dp on the top corners |
| `large` | 16dp |

## Dimension tokens

The important values in `SmartisanDimens` (all in dp):

```
TitleBarHeight 48        TitleBarShadowHeight 14     TitleBarHorizontalMargin 6
IconSize 36              ListItemHeight 60           ListItemMinHeight 60
ListItemHorizontalMargin 12  RowContentStart 18     CheckboxMarginStart 18
DividerThickness 0.67    ListItemImageSize 48        DialogWidth 308
DialogTitleHeight 48     DialogButtonHeight 48       DialogCornerRadius 10
MenuHorizontalMargin 18  MenuButtonTopMargin 18      MenuButtonBottomMargin 24
MenuActionEdgeMargin 24  MenuActionGap 18            BottomBarHeight 54
BottomBarIconSize 30     ScrollbarWidth 3            ScrollbarMargin 2
LetterIndexBarWidth 24   MinimumTouchTarget 48
```

## Motion specs

`SmartisanMotion`:

| Name | Value | Notes |
| --- | --- | --- |
| `EaseInOut` | `cos((t + 1) * PI) / 2 + 0.5` | The original default interpolator |
| `DurationShort` | 200ms | Presses, tab switches |
| `DurationMedium` | 300ms | Element enter/exit |
| `DurationLong` | 400ms | Full-page expand/collapse |
| `PressedScale` | 1.33f | Title bar icon press scale |
| `PressSpring` | damping 0.55 / stiffness 800 | Press rebound |
| `SettleSpring` | low bounce | List displacement, drag settle |
| `switchSettleMillis` | derived from distance | Switch knob settle duration |

## Overriding and extending

- Colors only: `SmartisanTheme(colors = lightSmartisanColors().copy(accent = ...))`
- Shapes only: `SmartisanTheme(shapes = SmartisanShapes(medium = RoundedCornerShape(12.dp)))`
- Per component: almost every component exposes explicit `color` / `size` / `shape` /
  `contentHeight` parameters. Prefer those over changing the global theme.
