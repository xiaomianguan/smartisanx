# Migrating from the three revival projects to smartisanx

[中文](从三个复刻项目迁移.md) · **English**

This document explains which smartisanx component each component from the Smartisan Music, Weather
and Clock revival projects maps to, and what to watch out for when migrating.

## 1. Component mapping

### Smartisan Music revival (Compose)

| Original file (`app/src/main/java/com/smartisan/music/ui/components/`) | smartisanx |
| --- | --- |
| `SmartisanTitleBar.kt` | `ui.layout.SmartisanTitleBar` (merged with the Weather title bar) |
| `SmartisanTitleBarSurface.kt` | `ui.layout.SmartisanTitleBarSurface` |
| `SmartisanDrawablePainter.kt` | `core.utils.rememberSmartisanDrawablePainter` (merged with Weather) |
| `SmartisanPressFeedback.kt` | `core.interaction.collectSmartisanPressedAsState` (merged with Weather) |
| `SmartisanClick.kt` | `core.interaction.smartisanClick` |
| `SmartisanMotion.kt` | `core.anim.SmartisanMotion.EaseInOut` |
| `SmartisanSwitch.kt` | `ui.control.SmartisanSwitch` (merged with the two Clock switches) |
| `SmartisanModal.kt` | `ui.overlay.SmartisanModal` / `SmartisanDialog` |
| `SmartisanAnimatedSheet.kt` | `ui.overlay.SmartisanBottomSheet` |
| `SmartisanScrollbar.kt` | `ui.layout.Modifier.smartisanVerticalScrollbar` |
| `SmartisanListDrag.kt` | `ui.list.SmartisanReorderableColumn` |
| `SmartisanSlideSelection.kt` | `ui.list.SmartisanLetterIndexBar` + list multi-select (`SmartisanListItem.selected`) |
| `SmartisanRatingBar.kt` | `ui.control.SmartisanRatingBar` |
| `SmartisanEmptyHint.kt` | `ui.layout.SmartisanEmptyHint` |
| `SmartisanCheckboxHit.kt` | `ui.control.SmartisanCheckbox` + `Modifier.smartisanCheckboxBounds` |
| `ShadowDrawable.kt` | `core.utils.Modifier.smartisanProjectedShadow` |
| `SmartisanTitleText.kt` | `ui.layout.SmartisanTitleBar` (title drawing is inlined) |
| `SmartisanPainterBackground.kt` | `core.utils.Modifier.smartisanPainterBackground` |
| `SmartisanTouchShield.kt` | Not migrated (page-level gesture shielding, out of scope) |
| `GlobalPlaybackBar.kt` | Not migrated (depends on Media3 playback state, app layer) |
| `TrackActionsOverlay.kt` | Not migrated (app-layer business logic) |
| `EmbeddedArtwork.kt` | Not migrated (depends on MediaStore, app layer) |
| `MediaStoreDeleteCoordinator.kt` | Not migrated (app-layer business logic) |
| `AudioPermission.kt` | Not migrated (app-layer business logic) |
| `SlideSelectionModel.kt` | Not migrated (app-layer business logic) |

### Smartisan Weather revival (Compose)

| Original file (`app/src/main/kotlin/com/smartisan/weather/ui/components/`) | smartisanx |
| --- | --- |
| `WeatherTitleBar` in `WeatherComponents.kt` | `ui.layout.SmartisanTitleBar` (merged with Music) |
| `WeatherIconButton` in `WeatherComponents.kt` | `ui.layout.SmartisanTitleBarAction` + `ui.basic.SmartisanIconButton` |
| `WeatherButton` in `WeatherComponents.kt` | `ui.control.SmartisanButton` |
| `WeatherScreenFrame` in `WeatherComponents.kt` | `ui.layout.SmartisanScaffold` |
| `rememberWeatherDrawablePainter` in `WeatherDrawable.kt` | `core.utils.rememberSmartisanDrawablePainter` |
| `collectWeatherPressedAsState` in `WeatherDrawable.kt` | `core.interaction.collectSmartisanPressedAsState` |
| `PixelText` in `OriginalControls.kt` | `ui.basic.SmartisanPixelText` |
| Weather background gradients, weather icons | Not migrated (app-layer content) |

### Smartisan Clock revival (XML + custom Views)

Every Clock component is a `View` subclass. This library rewrites them in Compose and
**does not port any View code**.

| Original file (`app/src/main/kotlin/com/smartisan/clock/`) | smartisanx |
| --- | --- |
| `custom/AnalogClockHandsView.kt` | `ui.clock.SmartisanAnalogClock` |
| `custom/CompactAlarmClockView.kt` | `ui.clock.SmartisanCompactClock` |
| `custom/SmallWorldClockView.kt` | `ui.clock.SmartisanCompactClock` (small size) |
| `custom/SmartisanSwitchView.kt` | `ui.control.SmartisanSwitch` |
| `custom/SmartisanSwitchExView.kt` | `ui.control.SmartisanSwitch` (merged into one implementation) |
| `custom/SmartisanTimePickerView.kt` | `ui.clock.SmartisanTimePicker` + `ui.clock.SmartisanWheelPicker` |
| `custom/TimerRulerView.kt` | `ui.clock.SmartisanRulerPicker` (horizontal caliper) |
| `custom/Classic680RulerView.kt` | `ui.clock.SmartisanPullRingRuler` (vertical pull ring) |
| `custom/TimerRulerPhysics.kt` | Physics inlined into `SmartisanRulerPicker` |
| `custom/AlarmRepeatDaysView.kt` | `ui.clock.SmartisanWeekdayPicker` |
| `custom/FastSelectionListView.kt` | The fast-selection gesture in `SmartisanWeekdayPicker` + `ui.control.SmartisanCheckbox` |
| `custom/WorldClockListView.kt` | `ui.list.SmartisanReorderableColumn` + `ui.clock.SmartisanCompactClock` |
| `custom/QuickBarEx.kt` | `ui.list.SmartisanLetterIndexBar` |
| `custom/SmartisanSwipeDeleteRow.kt` | `ui.list.SmartisanSwipeToDelete` |
| `custom/SmartisanSwipeDeleteMotion.kt` | Damping parameters inlined into `SmartisanSwipeToDelete` |
| `widget/SmartisanModalDialog.kt` | `ui.overlay.SmartisanDialog` |
| `widget/SmartisanMenuDialog.kt` | `ui.overlay.SmartisanMenuDialog` |
| `widget/SmartisanShadowDrawable.kt` | `core.utils.Modifier.smartisanProjectedShadow` |
| `widget/ClockBottomBar.kt` | `ui.layout.SmartisanBottomBar` |
| `widget/ClockContentFrame.kt` | `ui.layout.SmartisanScaffold` |
| `custom/AdaptiveFrameSequenceView.kt` | Not migrated (depends on original bitmap frame sequences) |
| `custom/AlarmEarAnimationView.kt` | Not migrated (depends on original bitmap frame sequences) |
| `custom/AlarmRingingPanelView.kt` | Not migrated (depends on the original ringing-card bitmaps) |

## 2. Main differences when migrating

### 1. No more `R.drawable.*` dependency

The original implementations leaned heavily on selectors, NinePatches and bitmaps under
`R.drawable`. smartisanx draws everything with Compose instead, which means:

- you no longer need to ship anything under `res/drawable-*`;
- there is no more paired maintenance of `values-night` / `drawable-night`;
- colors, corner radii and shadows all come from the theme and adapt to light/dark automatically.

If you really do need your own drawable (for example an in-app brand icon), you can still use
`rememberSmartisanDrawablePainter` and `Modifier.smartisanDrawableBackground`; they keep selector
state handling and NinePatch stretching intact.

### 2. Components no longer read `R.dimen.*`

The original implementations took their dimensions from `R.dimen`. This library uses
`SmartisanDimens` constants plus explicit parameters. When migrating, replace
`dimensionResource(R.dimen.xxx)` with the matching parameter or constant.

### 3. Components no longer carry business callbacks

The original implementations mixed in business callbacks (such as `onQueueChanged` or
`onCityDeleted`). This library keeps only pure UI callbacks (`onClick`, `onCheckedChange`,
`onMove`, `onDelete`); the owning app holds the state.

### 4. Unified press feedback

The original implementations handled "blue list press highlight", "icon scales to 1.33x" and
"button shrinks on press" separately. This library factors the shared part into
`collectSmartisanPressedAsState` plus per-component pressed-state drawing. The behaviour is the
same, but there is only one place to maintain.

## 3. Migration checklist

- [ ] Wrap the root of your UI in `SmartisanTheme`
- [ ] Replace `R.dimen` references with `SmartisanDimens` or component parameters
- [ ] Replace `R.color` references with semantic colors from `LocalSmartisanColors.current`
- [ ] Replace `Text` / `BasicText` with `SmartisanText`
- [ ] Replace Material `Switch` / `Checkbox` / `Button` with the smartisanx equivalents
- [ ] Delete drawable / values-night resources that are no longer used
- [ ] Compare against the demo app (`sample` module) to check the visuals still match
