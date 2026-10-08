# Component overview

All public APIs grouped by package. Every component must be wrapped in `SmartisanTheme`.

[中文](组件总览.md) · **English**

- [Theme and core capabilities](#theme-and-core-capabilities)
- [Basic components](#basic-components-basic)
- [Controls](#controls-control)
- [Layout](#layout-layout)
- [List interaction](#list-interaction-list)
- [Overlays](#overlays-overlay)
- [Clock](#clock-clock)
- [Icons](#icons-icons)

---

## Theme and core capabilities

### `cc.wuersan008.smartisanx.core.theme`

| API | Description |
| --- | --- |
| `SmartisanTheme(colors, typography, shapes, content)` | Provides the theme, follows the system light/dark setting |
| `SmartisanTheme(controller, typography, shapes, content)` | Controller-based, switchable at runtime |
| `rememberSmartisanThemeController(mode)` | Creates a `ThemeController` |
| `ThemeController.colorSchemeMode` | `System` / `Light` / `Dark` (**dark is experimental** — the original had no dark mode) |
| `ThemeController.isDark()` / `colors()` | Current state |
| `lightSmartisanColors()` / `darkSmartisanColors()` | Default palettes (dark is experimental) |
| `SmartisanColors` | 30 semantic colors (see [Theme.md](Theme.md)) |
| `SmartisanTypography` | 12 text styles |
| `SmartisanShapes` | 7 shapes |
| `SmartisanDimens` | Dimension tokens |

### `cc.wuersan008.smartisanx.core.anim`

| API | Description |
| --- | --- |
| `SmartisanMotion.EaseInOut` | The original cosine ease-in-out |
| `SmartisanMotion.easeInOut(durationMillis)` | Convenience `TweenSpec` |
| `SmartisanMotion.PressSpring` / `SettleSpring` / `OffsetSpring` | Spring specs |
| `SmartisanMotion.switchSettleMillis(position, target)` | Switch knob settle duration |
| `SmartisanMotionSpec` / `LocalSmartisanMotion` | Replace the motion specs wholesale |

### `cc.wuersan008.smartisanx.core.interaction`

```kotlin
@Composable fun InteractionSource.collectSmartisanPressedAsState(): State<Boolean>
@Composable fun smartisanClick(onClick: () -> Unit): () -> Unit
@Composable fun smartisanHaptic(): () -> Unit
@Composable fun rememberSmartisanInteractionSource(): MutableInteractionSource
@Composable fun Modifier.smartisanClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier
```

`collectSmartisanPressedAsState` keeps a "press and release within the same frame" tap visible, and
uses the same duration as the platform's `ViewConfiguration.getPressedStateDuration()`.

### `cc.wuersan008.smartisanx.core.utils`

```kotlin
@Composable fun rememberSmartisanDrawablePainter(
    @DrawableRes drawableRes: Int,
    enabled: Boolean = true, pressed: Boolean = false, selected: Boolean = false,
    focused: Boolean = false, checked: Boolean = false, activated: Boolean = false,
): Painter

fun Modifier.smartisanPainterBackground(painter: Painter): Modifier
@Composable fun Modifier.smartisanDrawableBackground(...): Modifier
fun Modifier.smartisanProjectedShadow(elevation: Dp = 1.dp, shape: Shape = RectangleShape): Modifier
fun smartisanDrawableState(...): IntArray
```

Use these to draw your own selectors / NinePatches on the Compose canvas. State mapping and layout
direction handling match the original `View` behaviour.

---

## Basic components (`basic`)

```kotlin
@Composable fun SmartisanSurface(
    modifier: Modifier = Modifier, shape: Shape = RectangleShape,
    color: Color = LocalSmartisanColors.current.surface,
    contentColor: Color = smartisanContentColorFor(color),
    border: BorderStroke? = null, content: @Composable () -> Unit,
)

@Composable fun SmartisanText(
    text: String, modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body,
    color: Color = Color.Unspecified, fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified, textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE, minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
)

@Composable fun SmartisanPixelText(text: String, size: Dp, ...)

@Composable fun SmartisanIcon(
    imageVector: ImageVector, contentDescription: String?,
    modifier: Modifier = Modifier, tint: Color = Color.Unspecified, size: Dp = 24.dp,
)

@Composable fun SmartisanIconButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    size: Dp = SmartisanDimens.IconSize, contentDescription: String? = null,
    pressedScale: Float = SmartisanMotion.PressedScale, content: @Composable () -> Unit,
)

@Composable fun SmartisanDivider(modifier, color, thickness, startIndent, endIndent)
@Composable fun SmartisanRowDivider(modifier, startIndent, endIndent)
```

`SmartisanText` has both `String` and `AnnotatedString` overloads. Its color defaults to
`LocalSmartisanContentColor` (written by `SmartisanSurface`) and falls back to `textPrimary` when
unset. `SmartisanPixelText` reproduces the original `TextView` behaviour of rounding a dp font size
to whole physical pixels.

---

## Controls (`control`)

```kotlin
@Composable fun SmartisanSwitch(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, hapticsEnabled: Boolean = true,
)

@Composable fun SmartisanSwitchRow(
    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, summary: String? = null,
)

@Composable fun SmartisanCheckbox(
    checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier, enabled: Boolean = true,
)
@Composable fun Modifier.smartisanCheckboxBounds(onBounds: (Rect?) -> Unit): Modifier

@Composable fun SmartisanRadioButton(selected: Boolean, onClick: (() -> Unit)?, modifier, enabled)
@Composable fun SmartisanRadioRow(text: String, selected: Boolean, onClick: () -> Unit, modifier, enabled)

enum class SmartisanButtonStyle { Accent, Neutral, Text }

@Composable fun SmartisanButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, style: SmartisanButtonStyle = SmartisanButtonStyle.Accent,
    loading: Boolean = false,
)
@Composable fun SmartisanTextButton(text, onClick, modifier, enabled, color)

@Composable fun SmartisanRatingBar(
    rating: Int, onRatingChange: ((Int) -> Unit)?, modifier: Modifier = Modifier,
    starCount: Int = 5, enabled: Boolean = true, starSize: Dp = 24.dp,
)
fun smartisanRatingAt(x: Float, width: Float, starCount: Int = 5): Int
```

`SmartisanSwitch` reproduces all the behaviour of the original switch: a shadow appears on press and
fades out along a cosine curve on release, the knob can be dragged after pressing, the settle
duration scales with the travel distance, and a haptic fires on release.

`SmartisanSwitchRow` binds the row and the switch to a single state, so tapping the row and tapping
the switch each fire exactly one callback.

---

## Layout (`layout`)

```kotlin
@Composable fun SmartisanScaffold(
    modifier: Modifier = Modifier,
    titleBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    containerColor: Color = LocalSmartisanColors.current.pageBackground,
    content: @Composable () -> Unit,
)

data class SmartisanTitleBarAction(
    val icon: ImageVector, val contentDescription: String,
    val onClick: () -> Unit, val enabled: Boolean = true,
)

@Composable fun SmartisanTitleBar(
    title: String, modifier: Modifier = Modifier,
    navigationIcon: SmartisanTitleBarAction? = null,
    action: SmartisanTitleBarAction? = null,
    navigationActions: List<SmartisanTitleBarAction> = emptyList(),
    actions: List<SmartisanTitleBarAction> = emptyList(),
    includeStatusBar: Boolean = true, showShadow: Boolean = true,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    centerContent: (@Composable () -> Unit)? = null,
)

@Composable fun SmartisanTitleBarSurface(modifier, includeStatusBar, showShadow, contentHeight, content)
@Composable fun SmartisanTitleBarShadow(modifier, height)

@Composable fun SmartisanListItem(
    title: String, modifier: Modifier = Modifier, summary: String? = null,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true, selected: Boolean = false, showDivider: Boolean = false,
    dividerStartIndent: Dp = SmartisanDimens.RowContentStart,
    minHeight: Dp = SmartisanDimens.ListItemMinHeight,
    contentPadding: Dp = SmartisanDimens.RowContentStart,
    onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null,
)

@Composable fun SmartisanGroup(modifier, shape, color, horizontalMargin, content)
@Composable fun SmartisanGroupDivider(modifier, startIndent)
@Composable fun SmartisanSectionTitle(text: String, modifier: Modifier = Modifier, startIndent: Dp)
@Composable fun SmartisanCard(modifier, shape, color, content)

@Composable fun SmartisanTabRow(tabs: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit, modifier, scrollable: Boolean = false)

data class SmartisanBottomBarItem(val icon: ImageVector, val label: String, val selectedIcon: ImageVector = icon)
@Composable fun SmartisanBottomBar(items, selectedIndex, onSelected, modifier, includeNavigationBar = true, showTopDivider = true)

@Composable fun Modifier.smartisanVerticalScrollbar(state: ScrollState, width, margin, color): Modifier
@Composable fun Modifier.smartisanVerticalScrollbar(state: LazyListState, width, margin, color): Modifier

@Composable fun SmartisanEmptyHint(title: String, modifier, description, icon, action)
```

Notes:

- `SmartisanListItem` with `selected = true` uses the original pale blue multi-select background
  `selectionBackground`, and `surfacePressed` while pressed. Neither uses a ripple.
- `SmartisanTitleBar` reserves the status bar by default; pass `includeStatusBar = false` if the
  surrounding layout already handles insets.
- `SmartisanListItem`'s `onLongClick` is meant for entering multi-select mode (as the original
  library screen did on long press).

---

## List interaction (`list`)

```kotlin
@Composable fun <T> SmartisanReorderableColumn(
    items: List<T>, onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier, dragEnabled: Boolean = true,
    onDragIndexChange: (Int?) -> Unit = {},
    content: @Composable (index: Int, item: T, dragging: Boolean) -> Unit,
)

@Composable fun SmartisanSwipeToDelete(
    onDelete: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    directReveal: Dp = 65.dp, maximumTravel: Dp = 360.dp, threshold: Dp = 120.dp,
    deleteLabel: String = "Delete", content: @Composable () -> Unit,
)

@Composable fun SmartisanLetterIndexBar(
    letters: List<Char>, onLetterSelected: (Char) -> Unit, modifier: Modifier = Modifier,
    activeLetter: Char? = null, letterHeight: Dp = 13.dp, showOverlay: Boolean = true,
)
fun smartisanDefaultLetterIndex(): List<Char>   // '#' + A-Z
fun smartisanIndexLetter(name: String): Char
```

- `SmartisanReorderableColumn` uses a plain `Column`, which suits bounded lists such as settings or
  world clocks. Long-press to start dragging; other rows spring out of the way and the new order is
  committed once on release.
- `SmartisanSwipeToDelete`'s physics come from the Clock revival: the first 65dp move 1:1, further
  travel is damped to 1/5 speed, and the maximum is 360dp.

---

## Overlays (`overlay`)

```kotlin
@Composable fun SmartisanModal(
    onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    bottom: Boolean = false, dimAmount: Float = 0.54f,
    content: @Composable ColumnScope.() -> Unit,
)

@Composable fun SmartisanModalWindow(
    onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    dimAmount: Float = 0.54f, content: @Composable ColumnScope.() -> Unit,
)

@Composable fun SmartisanDialogTitleBar(
    title: String, onDismiss: () -> Unit, onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true, modifier: Modifier = Modifier,
)

@Composable fun SmartisanDialogButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, accent: Boolean = true,
)

@Composable fun SmartisanDialog(
    onDismissRequest: () -> Unit, title: String, modifier: Modifier = Modifier,
    confirmText: String = "OK", dismissText: String? = null,
    confirmEnabled: Boolean = true, onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
)

@Composable fun SmartisanConfirmDialog(
    onDismissRequest: () -> Unit, title: String, message: String,
    confirmText: String = "OK", dismissText: String = "Cancel", onConfirm: () -> Unit,
)

@Composable fun SmartisanMenuDialog(
    onDismissRequest: () -> Unit, title: String? = null,
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit,
)

@Composable fun SmartisanMenuItem(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, showDivider: Boolean = true, danger: Boolean = false,
)

@Composable fun SmartisanBottomSheet(
    onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    title: String? = null, content: @Composable ColumnScope.() -> Unit,
)

@Composable fun SmartisanSheetScaffold(
    visible: Boolean, onDismissRequest: () -> Unit, modifier: Modifier = Modifier,
    title: String? = null, scrimColor: Color = LocalSmartisanColors.current.scrim,
    content: @Composable ColumnScope.() -> Unit,
)
```

- The scrim defaults to 0.54, matching the original.
- The window is only removed after the exit animation finishes, avoiding the original's problem of
  the window being destroyed while the animation was still running.
- `SmartisanSheetScaffold` is for sheets hosted inside the page rather than in their own window
  (for example a side panel or an embedded surface).

---

## Clock (`clock`)

```kotlin
@Composable fun SmartisanAnalogClock(
    modifier: Modifier = Modifier, time: LocalTime = LocalTime.now(),
    showSecondHand: Boolean = true, showEars: Boolean = false, showNumerals: Boolean = true,
    dialColor: Color = Color.Unspecified, handColor: Color = Color.Unspecified,
    accentColor: Color = Color.Unspecified, size: Dp = 240.dp,
)

@Composable fun SmartisanCompactClock(
    modifier: Modifier = Modifier, hour: Int, minute: Int, second: Int = 0,
    showSecondHand: Boolean = false, size: Dp = 40.dp,
    dialColor: Color = Color.Unspecified, handColor: Color = Color.Unspecified,
)

@Composable fun SmartisanTimePicker(
    hour: Int, minute: Int, onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier, use24Hour: Boolean = true, minuteStep: Int = 1,
)

@Composable fun SmartisanWheelPicker(
    items: List<String>, selectedIndex: Int, onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier, visibleCount: Int = 5, itemHeight: Dp = 40.dp,
)

@Composable fun SmartisanRulerPicker(
    minutes: Int, onMinutesChange: (Int) -> Unit, modifier: Modifier = Modifier,
    range: IntRange = 0..180, enabled: Boolean = true,
)

@Composable fun SmartisanPullRingRuler(
    minutes: Int, onMinutesChange: (Int) -> Unit, modifier: Modifier = Modifier,
    range: IntRange = 0..180, enabled: Boolean = true,
)

@Composable fun SmartisanWeekdayPicker(
    selectedDays: Set<DayOfWeek>, onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
    labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
)

@Composable fun SmartisanWeekdayChips(
    selectedDays: Set<DayOfWeek>, onSelectedDaysChange: (Set<DayOfWeek>) -> Unit,
    modifier: Modifier = Modifier,
)

@Composable fun SmartisanWorldClockCard(
    city: String, zone: ZoneId, modifier: Modifier = Modifier,
    now: Instant = Instant.now(), localZone: ZoneId = ZoneId.systemDefault(),
    showSecondHand: Boolean = false, onClick: (() -> Unit)? = null,
)
```

- `SmartisanAnalogClock`'s second hand has the original slight rebound (it overshoots, then
  converges with a spring).
- `SmartisanRulerPicker` is the horizontal caliper (the 7.1.1 `TimerRulerView`) and
  `SmartisanPullRingRuler` is the vertical pull ring (the 6.8.0 `Classic680RulerView`); both timer
  feels are preserved.
- `SmartisanWeekdayPicker` supports not only tapping but also "hold the checkbox column and drag
  vertically to sweep the same state across every row you cross".
- `SmartisanWorldClockCard` computes local time and the offset from the current device zone using
  the city's `ZoneId`.

---

## Icons (`icons`)

```kotlin
SmartisanXIcons.Back / ChevronRight / ChevronDown / ChevronUp / Close / Check / Add / Remove
SmartisanXIcons.Search / More / Menu / Delete / Edit / Share / Refresh / Settings / DragHandle

SmartisanXStatusIcons.Star / StarOutline / Heart / Location / Sun / Moon / Info / Warning
SmartisanXStatusIcons.ArrowUp / ArrowDown / Copy / Calendar / ExternalLink / Folder

SmartisanXMediaIcons.Play / Pause / Next / Previous / Stop / Shuffle / Repeat / Queue
SmartisanXMediaIcons.Volume / VolumeMute / FastForward / Rewind / Lyrics

SmartisanXClockIcons.Clock / Alarm / Stopwatch / Hourglass / Globe / Bell / SleepTimer / KeepScreenOn
```

All of them are 24×24 `ImageVector`s, tinted through `SmartisanIcon`'s `tint`.

This vector set is for cases where **no original asset exists** (custom screens, custom actions).
Title bars, switches, dialogs, list rows and clock faces use the original bitmaps by default — see
the asset index below.

## Asset index

Original graphic assets are referenced through three objects, all in
`cc.wuersan008.smartisanx.ui.asset`:

| Object | Contents |
| --- | --- |
| `SmartisanDrawables` | Title bar, dialogs, menus, list rows, group cards, checkbox, rating, switch, letter bar, tab bar, search field |
| `SmartisanClockDrawables` | Large/small dials, hands and shadows, ticks and numerals, alarm ears, ringing-card clock, time wheels |
| `SmartisanTimerDrawables` | Timer caliper, 6.8.0 pull-ring frame sequence, stopwatch buttons, repeat-day switch, clock list rows |

Usage matches the vector icons, except the assets carry their own pressed/disabled states:

```kotlin
// Title bar icon (the selector handles its own states)
SmartisanIcon(res = SmartisanDrawables.IconBack, contentDescription = "Back")

// As a background (NinePatch stretches automatically)
Modifier.smartisanDrawableBackground(SmartisanDrawables.GroupRowSingle)

// Assets that must be drawn at their intrinsic size, like dials
SmartisanIntrinsicImage(res = SmartisanClockDrawables.Face)
```

Every `@DrawableRes` parameter is replaceable — pass your own resource id to swap the artwork.
