# Component overview

All public APIs grouped by package. Every component must be wrapped in `SmartisanTheme`.

[中文](组件总览.md) · **English**

- [Theme and core capabilities](#theme-and-core-capabilities)
- [Basic components](#basic-components-basic)
- [Controls](#controls-control)
- [Input](#input-input)
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
| `SmartisanTheme(colors, typography, shapes, content)` | Provides the theme, **light by default** |
| `SmartisanTheme(controller, typography, shapes, content)` | Controller-based, switchable at runtime |
| `rememberSmartisanThemeController(mode)` | Creates a `ThemeController`, defaults to `Light` |
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

data class SmartisanButtonTabGroupItem(val text: String, val iconRes: Int? = null)

@Composable fun SmartisanButtonTabGroup(
    items: List<SmartisanButtonTabGroupItem>, selectedIndex: Int, onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier, hasGap: Boolean = false, alwaysKeepClickListen: Boolean = false,
    itemWidth: Dp? = null, disabledIndices: Set<Int> = emptySet(), contentColor: Color = Color.Unspecified,
)
@Composable fun SmartisanButtonTabGroup(items: List<String>, ...same, without icons...)

enum class SmartisanHammerButtonStyle { White, Grey, Black, GreyFocus, BlackFocus, DigitZero, Equal }

@Composable fun SmartisanHammerButton(
    iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier,
    style: SmartisanHammerButtonStyle = SmartisanHammerButtonStyle.White,
    highlighted: Boolean = false, onRepeat: (() -> Unit)? = null,
    contentDescription: String? = null, iconPadding: PaddingValues = PaddingValues(...),
)

@Composable fun SmartisanNumberPicker(
    value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier,
    minValue: Int = 0, maxValue: Int = 9, wrap: Boolean = true,
    formatter: (Int) -> String = { it.toString() }, unit: String? = null,
    visibleCount: Int = 5, itemHeight: Dp = 40.dp, showSelectionLines: Boolean = true,
    unitColor: Color = Color.Unspecified, hapticFeedbackOnChange: Boolean = true,
)

data class SmartisanPageIndicatorIcon(val normalRes: Int, val selectedRes: Int)

@Composable fun SmartisanPageIndicator(
    pageCount: Int, currentPage: Int, modifier: Modifier = Modifier, radius: Dp = 2.dp,
    pageColor: Color = Color.Unspecified, selectedColor: Color = Color.Unspecified,
    icons: List<SmartisanPageIndicatorIcon> = emptyList(), contentDescription: String? = null,
)

enum class SmartisanProgressState { Download, Pause, Retry, Processing }

@Composable fun SmartisanProgressIndicator(
    progress: Int, modifier: Modifier = Modifier,
    state: SmartisanProgressState = SmartisanProgressState.Download,
    size: Dp = 36.dp, innerCircleRadius: Dp = 15.dp,
    backRingWidth: Dp = 2.dp, foreRingWidth: Dp = 1.3333334.dp,
    backProgressStartColor: Color = Color.Unspecified, backProgressEndColor: Color = Color.Unspecified,
    foreProgressStartColor: Color = Color.Unspecified, foreProgressEndColor: Color = Color.Unspecified,
    failedProgressColor: Color = Color.Unspecified,
)

@Composable fun SmartisanTips(
    text: String, modifier: Modifier = Modifier,
    color: Color = Color.Unspecified, showShadow: Boolean = true,
)
```

`SmartisanSwitch` reproduces all the behaviour of the original switch: a shadow appears on press and
fades out along a cosine curve on release, the knob can be dragged after pressing, the settle
duration scales with the travel distance, and a haptic fires on release.

`SmartisanSwitchRow` binds the row and the switch to a single state, so tapping the row and tapping
the switch each fire exactly one callback.

### Ported original controls

These components come straight from the custom views of the stock APKs; the bitmaps, shadows, animation
durations and interpolators are copied from the originals:

| Component | Original class | Apps using it |
| --- | --- | --- |
| `SmartisanButtonTabGroup` | `smartisanos.widget.ButtonTabGroup` | Calendar, Messages, Music, Notes, Recorder (5) |
| `SmartisanNumberPicker` | `SmartisanNumberPicker` / `SmartisanNumberPickerEx` | Clock, Music (2) |
| `SmartisanPageIndicator` | `smartisanos.app.IndicatorView` | Calendar, Notes (2) |
| `SmartisanProgressIndicator` | `smartisanos.widget.DownloadProgressView` | Calendar, Mail (2) |
| `SmartisanTips` | `smartisanos.widget.TipsView` | Calendar, Mail (2) |
| `SmartisanHammerButton` | `com.smartisanos.calculator.HammerButton` | Calculator (1) |
| `SmartisanSmoothSeekBar` | `smartisanos.widget.SmoothSeekBar` | Settings and others (framework-wide slider) |
| `SmartisanIconSlider` | `smartisanos.widget.SliderWithIcons` | Settings and others (slider with end icons) |

`SmartisanNumberPicker` does not reimplement the wheel: it reuses `SmartisanWheelPicker` (which gained the
original's cyclic scrolling `wrap` in this change) and only adds the original's min/max range, formatter and
highlighted unit suffix semantics.

`SmartisanButtonTabGroup` uses the original `selector_small_btn_filter_left / _middle / _right` and
`selector_small_btn_standard` bitmaps, overlaps adjacent segments by the original
`button_tab_group_each_gap = 6dp`, and takes its text shadow from `color/filter_button_text_shadow_colors`.

`SmartisanHammerButton` gets its press offset from the original selectors themselves (the pressed bitmap is
inset by about 1dp), draws the highlight badge at the original `highlight_padding_right / top` (portrait and
landscape differ), and repeats on long press at the original `500ms` delay then `150ms` intervals.

Some colours and radii of `SmartisanPageIndicator` and `SmartisanProgressIndicator` live in the
**Smartisan private framework** (not available here), so they fall back to theme semantic colours and a 2dp
radius, all overridable per parameter.

### framework sliders (`SmartisanSmoothSeekBar` / `SmartisanIconSlider`)

```kotlin
@Composable fun SmartisanSmoothSeekBar(
    value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, steps: Int = 0,
    trackColor: Color = LocalSmartisanColors.current.divider,
    progressColor: Color = LocalSmartisanColors.current.accent,
    thumbRes: Int = R.drawable.progress_control,
    thumbDisabledRes: Int = R.drawable.progress_control_disabled,
    height: Dp = 48.dp, contentDescription: String? = null,
)

@Composable fun SmartisanIconSlider(
    value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, steps: Int = 0,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
)

@Composable fun SmartisanSliderIcon(
    @DrawableRes res: Int, contentDescription: String?, modifier: Modifier = Modifier,
    size: Dp? = null, tint: Color = Color.Unspecified,
)
```

Notes:

- `SmartisanSmoothSeekBar` ports the framework's `smartisanos.widget.SmoothSeekBar` (Settings uses it for
  brightness, volume and font size); the library had **no slider at all** before this one. The thumb is the
  original `progress_control` / `progress_control_disabled` bitmap (the two states of
  `seekbar_scrubber_control_selector`) and the track is drawn by the control at 2dp.
- `SmartisanIconSlider` ports `SliderWithIcons` + `slider_with_icons_layout.xml`: the original layout holds
  only three layout rules (left icon `alignParentLeft` + centred, right icon `alignParentRight` + centred,
  slider `match_parent` between the two) and **no dimension constants**, so no `SmartisanDimens` entries
  were added; the slider itself is the `SeekBarStyle.Thin.LargeThumb.Actived` style, the thickest thumb of
  the `SeekBarStyle` family.
- The original end icons are `wrap_content` (drawn at their intrinsic bitmap size), which is what
  `SmartisanSliderIcon(size = null)` does; pass an explicit size (for example 26dp) to normalise them.
- The original quirk "show both icons or neither" (the constructor only calls `setImageResource` when
  `leftIconRes > 0 && rightIconRes > 0`) was not carried over: the Compose version has two optional slots
  and draws whichever you provide.
- Label, slider and both end icons are vertically centred; the result was verified pixel by pixel on a real
  device (see `ComponentVerification.md`).
- Icons do not grey out when disabled: the original only swaps the thumb for the disabled bitmap, and the
  caller decides how the icons look.

---

## Input (`input`)

```kotlin
@Composable fun SmartisanSearchBar(
    query: String, onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier, expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {}, placeholder: String = "",
    onCancel: () -> Unit = {}, onSearchIconClick: () -> Unit = {}, onSearch: () -> Unit = {},
    filterIconRes: Int? = null, onFilterClick: () -> Unit = {},
    secondaryFilterText: String? = null, onSecondaryFilterClick: () -> Unit = {},
    showLeftIcon: Boolean = true, showShadow: Boolean = true, enabled: Boolean = true,
    autoFocus: Boolean = true, withAnimation: Boolean = true,
    onAnimationStart: (() -> Unit)? = null, onAnimationEnd: (() -> Unit)? = null,
    fieldHeight: Dp = SmartisanInputDefaults.FieldHeight,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions? = null,
)

@Composable fun SmartisanAutoFitText(
    text: String, modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body, color: Color = Color.Unspecified,
    minFontSize: TextUnit = 12.sp, maxFontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1, overflow: TextOverflow = TextOverflow.Clip, textAlign: TextAlign? = null,
)

@Composable fun SmartisanJustifyText(
    text: String, modifier: Modifier = Modifier,
    style: TextStyle = LocalSmartisanTypography.current.body, color: Color = Color.Unspecified,
    indentFirstLine: Boolean = true,
)

@Composable fun SmartisanPasswordField(
    value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    placeholder: String = "", enabled: Boolean = true, revealPassword: Boolean = false,
    onRevealPasswordChange: (Boolean) -> Unit = {}, singleLine: Boolean = true,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 15.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password, imeAction = ImeAction.Done,
    ),
    keyboardActions: KeyboardActions? = null,
    eyePaddingEnd: Dp = SmartisanInputDefaults.EyePaddingEnd,
)

@Composable fun SmartisanClearableField(
    value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    placeholder: String = "", enabled: Boolean = true, singleLine: Boolean = true,
    showClearOnlyWhenFocused: Boolean = true, onClear: (() -> Unit)? = null,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 15.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions? = null,
    clearIconRes: Int = R.drawable.quick_icon_delete,
    clearIconSize: Dp = SmartisanInputDefaults.QuickDeleteIconSize,
    clearPaddingEnd: Dp = SmartisanInputDefaults.QuickDeletePaddingEnd,
    animateClearIcon: Boolean = true,
)

object SmartisanInputDefaults
```

### Ported original input components

| Component | Original class | Apps in the original |
| --- | --- | --- |
| `SmartisanSearchBar` | `smartisanos.widget.SearchBar` | Messages, Calendar, Clock, Gallery, Music, Notes, Recorder (7) |
| `SmartisanAutoFitText` | `smartisanos.widget.FontFitTextView` | Calendar, Messages, Notes (3) |
| `SmartisanJustifyText` | `smartisanos.tablet.widget.SmartisanJustifyTextView` | Music (1) |
| `SmartisanPasswordField` | `smartisanos.widget.PasswordEditText` | Calendar, Mail, Music (3) |
| `SmartisanClearableField` | `smartisanos.widget.QuickDeleteEditText` | Calendar, Mail (2) |
| `SmartisanMessageField` | `smartisanos.widget.MessageField` | Message input bar (no caller inside the framework; a shared component for apps) |

### Message input bar (`SmartisanMessageField`)

```kotlin
@Composable fun SmartisanMessageField(
    value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    onSend: () -> Unit = {}, enabled: Boolean = true, hint: String? = null,
    maxLength: Int = 2000, maxLines: Int = 1,
    @DrawableRes leftIconRes: Int? = SmartisanDrawables.MessageFieldAddIcon,
    onLeftIconClick: (() -> Unit)? = null,
    @DrawableRes emojiIconRes: Int? = null, onEmojiClick: (() -> Unit)? = null,
    showShadow: Boolean = true,
    @DrawableRes backgroundRes: Int = SmartisanDrawables.MessageFieldBackground,
    @DrawableRes sendIconRes: Int = SmartisanDrawables.MessageFieldSendIcon,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 13.5.sp),
    keyboardOptions: KeyboardOptions = ...,
    keyboardActions: KeyboardActions = ...,
)
```

- Ported from the framework's `smartisanos.widget.MessageField` (layout `message_field.xml`, a `merge`):
  a 36dp leading icon (`standard_icon_size`), the input area (background `message_field.9.png`,
  32dp intrinsic height) and a 36dp send button (`selector_small_icon_send`, the green arrow), with
  `bar_margin_edge` (6dp) at both ends.
- Three rules copied straight out of the original code: the send button is **disabled while the field is
  empty** (the selector then picks `icon_send_disabled`); hiding the leading icon moves the input area's
  start margin from 12dp to 6dp (`adjustMessageFiledLayoutParams`); showing the emoji icon moves the
  editor's end padding from 6dp to 5dp (`updateEditorPaddingRight`).
- The length cap defaults to 2000 (the original's `integer/message_field_editor_max_length`) and extra
  input is rejected outright; with more lines the input area grows (the original's `editorMaxLine`).
- The framework's `bottom_bar_shadow` sits on the bar's top edge
  (`sos_smartisanos_drawable_bottom_bar_shadow`, 33px asset ⇒ 11dp, matching
  `bottom_bar_shadow_height`) together with a 0.67dp divider; both hug the top edge and the shadow is
  translated up by its own height (the original's `BarsHelper` `SHADOW_BOTTOM_TYPE`).
- Differences: the original `Listener`'s `beforeTextChanged` / `onTextChanged` have no Compose
  counterpart, so they collapse into `onValueChange`; text and hint colours use the theme's
  `textPrimary` / `textHint`; navigation-bar insets are left to the caller.

Notes:

- `SmartisanSearchBar` is 48dp tall (the original `title_bar_height`) with a 32dp edit area backed by the original NinePatch `search_field` (`search_bar_edit_bg_selector`, disabled `search_field_disabled`); the leading icon is `search_bar_left_icon` (24×30dp), the clear button `text_clear_btn` (30dp) and the cancel / filter buttons 36dp (the original `standard_icon_size`); the gaps come from `bar_margin_edge` 6dp, `search_bar_margin_search_view` 6dp and `search_bar_margin_each` 12dp.
- The expand / collapse animation copies the original `SearchBar.startAnimation(boolean)`: the edit area yields over 300ms when expanding and after a 100ms delay over 200ms when collapsing; the cancel button moves by `search_bar_anim_distance` (10dp) and fades in after 100ms when expanding, with no delay when collapsing — 200ms each, using the original `DecelerateInterpolator(1.5f)`; the filter container does the opposite.
- `SmartisanAutoFitText` reproduces the original `refitText` binary search with `TextMeasurer`: measure the line, and if it does not fit, bisect between `mFitMinSize` (12sp) and the current size with a 0.5px tolerance, then shrink once more against the available height.
- `SmartisanJustifyText` redraws line by line: every line except the last one, empty lines and lines ending in a newline is spread by `(line width − natural width) / (characters − 1)`; a two-space paragraph indent is never stretched.
- `SmartisanPasswordField` uses the original one-shot frame animation `pwd_eye_open_close_anim` (16 frames × 16ms, taken from Music); the total duration follows the original formula and the plain text / mask switch happens at **half** the animation.
- `SmartisanClearableField` keeps the original `quick_icon_delete` icon and shows it only when the text is non-empty **and** the field is focused (the original `updateDrawableVisibility`).
- The clear button is drawn by a single shared implementation, `SmartisanClearIcon`, used by both the search bar and the clearable field.
- The original cursor is the 9-patch `edittext_cursor_bbackground`; Compose's `cursorBrush` only accepts a `Brush`, so the theme `accent` color is used instead. The original text / hint colors are `editor_text_color` (#cc000000) and `editor_hint_text_color` (#26000000); the theme `textPrimary` / `textHint` semantic colors are used instead.

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

@Composable fun SmartisanFlowLayout(
    modifier: Modifier = Modifier, itemSpacing: Dp = 0.dp, lineSpacing: Dp = 0.dp,
    content: @Composable () -> Unit,
)
```

Notes:

- `SmartisanListItem` with `selected = true` uses the original pale blue multi-select background
  `selectionBackground`, and `surfacePressed` while pressed. Neither uses a ripple.
- `SmartisanTitleBar` reserves the status bar by default; pass `includeStatusBar = false` if the
  surrounding layout already handles insets.
- `SmartisanListItem`'s `onLongClick` is meant for entering multi-select mode (as the original
  library screen did on long press).
- `SmartisanFlowLayout` is ported from the original `smartisanos.widget.letters.SurnameFlowLayout`
  (the surname picker popup in Calendar, Clock, Messages and Music): children are laid out left to
  right and wrap once the container width is exceeded, with each row as tall as its tallest child.
  The original spaced children with their own margins, so `itemSpacing` / `lineSpacing` default to `0.dp`.

### framework list-row matrix (the `SmartisanListRow` family)

```kotlin
enum class SmartisanListRowLines { TwoLine, TwoLineAlt, ThreeLine, ThreeLineAlt }

@Composable fun SmartisanListRow(
    title: String, modifier: Modifier = Modifier,
    summary: String? = null, tertiary: String? = null,
    lines: SmartisanListRowLines = SmartisanListRowLines.TwoLine,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true, selected: Boolean = false, showDivider: Boolean = false,
    dividerStartIndent: Dp = SmartisanDimens.RowContentStart,
    minHeight: Dp = SmartisanDimens.ListRowMinHeight,
    contentPadding: Dp = SmartisanDimens.ListRowFlexibleSpace,
    @DrawableRes rowBackgroundRes: Int? = SmartisanDrawables.ListRowSelector,
    @DrawableRes rowShadowRes: Int? = null,
    onClick: (() -> Unit)? = null, onLongClick: (() -> Unit)? = null,
)

@Composable fun SmartisanListRowArrow(subtitle: String? = null, modifier, enabled, subtitleMaxWidth)
@Composable fun SmartisanListSectionTitle(text: String, modifier, startIndent)
@Composable fun SmartisanListBoardSectionTitle(text: String, modifier, enabled, onClick)
@Composable fun SmartisanListVerticalGap(modifier, height)
```

Notes:

- This is the **master** list row of the framework (`list_content_item_layout` plus
  `smartisanos.widget.ListContentItem`), with a 17sp / 16sp primary line. `SmartisanListItem` is the
  other line (15sp, from the three re-implemented apps); both are kept, pick by source.
- `lines` picks the middle text layout: `TwoLine` (17/13.5sp), `TwoLineAlt` (16/12.5sp),
  `ThreeLine` (17/15/13.5sp), `ThreeLineAlt` (16/13.5/12sp) — type sizes copied from the framework dimens.
- The left slot is a fixed 60dp square (`left_icon_area_width`) whose content is centred and capped at
  36dp; the right slot keeps the original `right_container_margin` (6dp), with
  `SmartisanListRowArrow` for the subtitle + arrow pair.
- Divider indent: 18dp (`flexible_space`) without a left slot, 60dp with one.
- Section titles are 30dp (`SmartisanListSectionTitle`); the board title is a 40dp bar drawn with the
  original selector bitmap plus a 1px divider (`SmartisanListBoardSectionTitle`). Sizes and colours
  were verified pixel by pixel on a real device.

### framework editor rows (the `SmartisanEditorRow` family)

```kotlin
@DrawableRes fun smartisanEditorRowBackground(position: SmartisanGroupRowPosition): Int

@Composable fun SmartisanEditorRow(
    modifier: Modifier = Modifier, label: String? = null,
    position: SmartisanGroupRowPosition = SmartisanGroupRowPosition.Single,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true, value: String = "", onValueChange: ((String) -> Unit)? = null,
    placeholder: String? = null, singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    content: (@Composable () -> Unit)? = null,
)

@Composable fun SmartisanEditorLabel(
    text: String, modifier: Modifier = Modifier, iconRes: Int? = null,
    iconContainerBackground: Int? = null, showDivider: Boolean = false,
    showArrow: Boolean = false, enabled: Boolean = true, onClick: (() -> Unit)? = null,
)

@Composable fun SmartisanEditorRightIcon(
    modifier: Modifier = Modifier, text: String? = null, iconRes: Int? = null,
    showDivider: Boolean = false, enabled: Boolean = true, onClick: (() -> Unit)? = null,
)
```

Notes:

- Ported from the framework's `AbsEditor` family (`abs_editor_layout` + `editor_left_label_layout` +
  `editor_right_icon_widget_layout`): the master row behind every Settings line made of
  "label + field + trailing caption/icon".
- The row background is the original 9-patch for its position (`editor_bg_single` / `_top` /
  `_middle` / `_bottom`, i.e. the four `EditorStyle` variants); 6dp side padding, 44dp min height.
- The leading slot defaults to `SmartisanEditorLabel`: 12sp label with a 12dp start margin, an
  optional 40dp × 44dp icon container (26dp icon centred), a 2px divider on its right edge and an
  optional arrow after the label.
- The trailing slot can use `SmartisanEditorRightIcon`: caption capped at 150dp and ellipsized, with
  the icon 6dp away (0dp when the 2px divider is shown).
- Pass `content` to take over the middle: the framework's other two field variants map to
  `SmartisanPasswordField` (`pwd_edit_text`) and `SmartisanClearableField` (`quick_del_edit_text`).
- As in the original, a row with just the field (no leading/trailing slot) is clickable and hands
  focus to the field.

### framework combination title bar (`SmartisanComboTitleBar`)

```kotlin
enum class SmartisanComboTitleShadow { Normal, Short }

@Composable fun SmartisanComboTitleBar(
    modifier: Modifier = Modifier, title: String? = null, subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null, trailing: (@Composable () -> Unit)? = null,
    center: (@Composable () -> Unit)? = null, secondaryBar: (@Composable () -> Unit)? = null,
    includeStatusBar: Boolean = true, showTitleShadow: Boolean = true,
    showSecondaryShadow: Boolean = true, titleShadow: SmartisanComboTitleShadow? = null,
    titleColor: Color = Color.Unspecified, subtitleColor: Color = Color.Unspecified,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    centerMargin: Dp = SmartisanDimens.ComboTitleCenterMargin,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.TitleBarBackground,
)
```

Notes:

- Ported from the framework's `smartisanos.widget.SmartisanComboTitleBar`
  (`combo_title_layout.xml` + `primary_title_layout.xml`): a 48dp main bar (`titlebar_height`,
  background `titlebar_bg`), its shadow, a secondary bar and a secondary shadow. **The shadow shares a
  slot with the secondary bar**: with a secondary bar the shadow becomes `title_bar_shadow_short` and
  sits on top of the secondary bar; without one it is `title_bar_shadow` showing below the main bar
  (the original's `setWithSecondaryLayout` / `setWithOutSecondaryLayout`).
- The centre slot follows the original `adjustContainerParams()`: when
  `2 × max(left, right) + centre < bar width - 2 × 12dp` the centre is centred in the whole bar,
  otherwise centring is dropped, the centre is placed after the leading content and its width becomes
  "what is left between the two sides"; the 12dp is `mid_container_margin`. The natural width comes from
  `maxIntrinsicWidth()` (Compose allows only one measure per `Measurable`); custom layouts without
  intrinsic measurements are always treated as "does not fit".
- The original's six centre `styles` (plain text / radio tabs / drop-down / range / marquee / separator)
  collapse into one `center` slot: put whichever library component you need in it. Supplying only
  `title` / `subtitle` gives the plain-text variant (20sp + 10sp, per `title_bar_title_text_size` and
  `item_sub_title_size`).
- Title and subtitle colours default to the theme's `textSecondary` (≈ the original
  `title_or_btn_text_color` `#99000000`) and `textTertiary`, and can be overridden individually;
  the 12dp gaps and the vertical centring of both side slots were verified pixel by pixel on a device.
- Deliberately different: the original packs up to ten trailing buttons with a `-6dp` right margin;
  this library does not ship that button container, so lay the row out yourself using
  `SmartisanDimens.ComboTitleActionSpacing` (6dp) if you want the same overlap.

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

@Composable fun SmartisanHiddenRowActions(
    actions: List<SmartisanHiddenRowAction>, modifier: Modifier = Modifier,
    iconSize: Dp? = null,                     // null = intrinsic bitmap size (the original's wrap_content)
    spacing: Dp = SmartisanDimens.HiddenActionIconGap,        // 6dp
    sidePadding: Dp = SmartisanDimens.HiddenActionSidePadding, // 12dp
)

data class SmartisanHiddenRowAction(
    @DrawableRes val iconRes: Int, val contentDescription: String? = null,
    val enabled: Boolean = true, val onClick: () -> Unit = {},
)
```

- `SmartisanReorderableColumn` uses a plain `Column`, which suits bounded lists such as settings or
  world clocks. Long-press to start dragging; other rows spring out of the way and the new order is
  committed once on release.
- `SmartisanSwipeToDelete`'s physics come from the Clock revival: the first 65dp move 1:1, further
  travel is damped to 1/5 speed, and the maximum is 360dp.
- `SmartisanHiddenRowActions` ports the framework's `smartisanos.widget.HiddenListActionLayout`: a
  horizontal strip of icon buttons with 12dp on both sides (`hidden_list_action_left_right_padding`),
  6dp between icons (`hidden_list_action_icon_gap`), icons at `wrap_content` and vertically centred,
  and `enabled` driving the selector's disabled state. The original sets icon / listener / enabled by
  index and throws on out-of-range indices; here it is a `List<SmartisanHiddenRowAction>` instead.
  Note the original **only ships the strip itself** — nothing in the dump inflates or instantiates it,
  the reveal container is written per app — so this library also only provides the strip; wrap it in
  your own gesture if you need the swipe.

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
| `SmartisanDrawables` | Title bar, dialogs, menus, list rows, group cards, checkbox, rating, switch, letter bar, tab bar, search field, segmented button group, calculator keys, circular download progress |
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
