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
| Incomplete night artwork | Only 194 of 1008 original drawables (~19%) have night variants; the other 814 are light-only and will keep showing light artwork in dark mode |
| No night colour state lists | There are no night variants under `res/color/` at all, so dialog and menu text colours use their light values; this library adds a night palette for the Clock revival's dialogs and menus |

`SmartisanTheme` writes "is the theme dark" into `LocalSmartisanDarkOverride`, and components resolve
`drawable-night` / `values-night` against the **app theme** rather than the system uiMode, so assets
follow an in-app light/dark switch.

If you need the same fidelity as the light theme, treat light as the reference.

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

| Name | Original resource | Value |
| --- | --- | --- |
| `SmartisanDimens.ListItemHorizontalMargin` | `list_item_left_right_margin` | 12dp |
| `SmartisanDimens.ListItemVerticalGap` | `list_item_vertical_gap` | 14dp |

`SmartisanGroup` already uses both.

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
IconSize 36              ListItemHeight 60           ListItemMinHeight 48
ListItemHorizontalMargin 12  RowContentStart 18     CheckboxMarginStart 18
DividerThickness 0.67    ListItemImageSize 48        DialogWidth 308
DialogTitleHeight 48     DialogButtonHeight 48       DialogCornerRadius 10
MenuHorizontalMargin 18  MenuButtonTopMargin 18      MenuButtonBottomMargin 24
MenuActionEdgeMargin 24  MenuActionGap 18            BottomBarHeight 50
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
