# Development guide

[中文](开发指南.md) · **English**

This document is for anyone building a whole app with smartisanx (including AI agents picking up this
repository). It answers three kinds of question: **how a page is put together**, **how to build the
common screens**, and **what the rules are when you change the library**. Per-component parameter
tables live in [Components.md](Components.md); the full theme values live in [Theme.md](Theme.md).

## 1. A five-minute mental model

An original Smartisan page has only four layers:

```
page     = pinstripe texture (common_bg tiled) + title bar (with its outside shadow) + content + system bar inset
content  = section titles + a stack of cards
card     = each row's own NinePatch (white + 10dp corners + 1px stroke + a 14dp outward shadow)
group    = a transparent container (it only adds 12dp of horizontal margin)
```

Four rules follow from that, and they cannot be broken:

| Rule | Why | If you break it |
| --- | --- | --- |
| Components must be inside `SmartisanTheme` | colours / text styles / shapes come from `CompositionLocal` | "SmartisanColors not found" is thrown |
| Rows inside a group must be told their `position` | a card is split into top / middle / bottom / single artwork | rounded corners appear on a middle row, card seams misalign |
| Put a `SmartisanListVerticalGap()` between neighbouring groups | the shadow is drawn **outside** the row's bounds and needs 14dp to land in | the two cards touch and their shadows pile up |
| Do not put content straight onto the texture | in the original everything lives on a white card | it instantly stops looking like Smartisan — exactly the bug the sample started with |

Vocabulary (used consistently here and in Components.md):

- **row**: one entry of a list, e.g. "Allow notifications" or "First song";
- **group**: the transparent container that turns several rows into one card;
- **card**: the white, rounded, shadowed block, drawn by the row or the group itself;
- **page texture**: the 270×270 pinstripe cloth texture covering the whole page (`common_bg`).

## 2. The standard page structure

```kotlin
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    SmartisanScaffold(
        titleBar = {
            SmartisanTitleBar(
                title = "Settings",
                navigationIcon = SmartisanTitleBarAction(SmartisanOriginalIcons.Back, "Back", onBack),
            )
        },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            // the page's first gap: the original's group_list_item_vertical_gap_layout
            SmartisanListVerticalGap()

            SmartisanSectionTitle("Notifications")
            SmartisanGroup {
                SmartisanGroupItem(SmartisanGroupRowPosition.Top, "Allow notifications")
                SmartisanGroupItem(SmartisanGroupRowPosition.Middle, "Show on lock screen", showDivider = true)
                SmartisanGroupItem(SmartisanGroupRowPosition.Bottom, "Notification sound")
            }

            // between two groups: another 14dp gap
            SmartisanListVerticalGap()

            SmartisanSectionTitle("About")
            SmartisanGroup {
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Single,
                    title = "Version",
                    summary = "1.0.0",
                    trailing = { SmartisanListRowArrow() },
                    onClick = { /* ... */ },
                )
            }

            // the page's last gap: so the card's shadow does not touch the system bar
            SmartisanListVerticalGap()
        }
    }
}
```

Key points:

1. `SmartisanScaffold` already provides the texture, the title bar (status bar inset plus the shadow
   drawn outside it) and the navigation bar inset; the content area only decides whether it scrolls.
2. **Leave a gap at both ends**: the original's `settings_item_top_bottom_margin` gives the list 14dp
   above and below.
3. **No** gap around a section title — `SmartisanSectionTitle` carries its own (19.5dp above, 5.5dp
   below).
4. Dividers between rows: `showDivider = true`, which already indents by 30dp
   (`SmartisanDimens.RowContentStart`) to line up with the text.

> Use this as a template: the sample's 13 component pages and 13 full-page layouts are all built this
> way.

## 3. Rows and groups: the full rules

### 3.1 Which of the three row components?

| Component | Background | Text layout | When to use |
| --- | --- | --- | --- |
| `SmartisanGroupItem(position = ...)` | one segment of the group card | 15sp title + 12.5sp summary | a row inside a group (the usual choice) |
| `SmartisanListItem` | a whole single-row card | same | a standalone row, outside any group |
| `SmartisanListRow(lines = ...)` | a whole single-row card | 17sp / 16sp title + two / three lines | you need the framework's original sizes, or three lines |

All three share the **exact same slot geometry** (a fixed 60dp left slot, a 6dp right margin and 18dp
of flexible space in the middle), so mixing them on one page never misaligns anything: behind a 24dp or
a 36dp icon, the title always starts 72dp from the left screen edge.

### 3.2 How to pick a `position`

| Position | When | Artwork |
| --- | --- | --- |
| `Top` | first row of a group (with more than one row) | top segment (rounded at the top) |
| `Middle` | a middle row | middle segment (no corners) |
| `Bottom` | last row of a group | bottom segment (rounded at the bottom) |
| `Single` | the group's only row | the whole card (all four corners) |

**Dynamic lists** compute it from the index:

```kotlin
SmartisanGroup {
    items.forEachIndexed { index, item ->
        SmartisanGroupItem(
            position = when (index) {
                0 -> SmartisanGroupRowPosition.Top
                items.lastIndex -> SmartisanGroupRowPosition.Bottom
                else -> SmartisanGroupRowPosition.Middle
            },
            title = item.title,
            showDivider = index != items.lastIndex,
        )
    }
}
```

### 3.3 Non-row content inside a group

Things that do not draw their own card (a row of controls, a custom block) let the group draw it:

```kotlin
SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
        SmartisanText("A custom block", style = typography.sectionTitle)
        SmartisanText("It draws no card of its own; the group adds the artwork", style = typography.caption)
    }
}
```

> Once you pass `position`, the group's `color` / `shape` no longer apply — the artwork and the shadow
> come from the original assets.

### 3.4 Dividers

- Built into rows: `showDivider = true` (recommended; the indent lines up with the text);
- Manual: `SmartisanRowDivider()` (between rows) and `SmartisanGroupDivider()` (the fainter one used
  inside a group);
- Both take a `startIndent`; pass `0.dp` for a full-bleed line.

### 3.5 The three most common mistakes

| Symptom | Cause | Fix |
| --- | --- | --- |
| A middle row is rounded at the top too | every row was given `Single` | assign Top / Middle / Bottom as in 3.2 |
| Cards touch and their shadows pile up | a missing `SmartisanListVerticalGap()` | add one between every pair of groups |
| The page looks "muddy" and the pinstripe is gone | the group was given an opaque `color` | drop `color` and let the rows draw their own card |

## 4. Screen recipes

Each recipe is "what it is for + which components + the key code + the pitfall", and the code is
copy-pasteable.

### 4.1 Settings page

Use `SmartisanGroupItem` together with the ready-made rows: `SmartisanSwitchRow`,
`SmartisanRadioRow` and the `SmartisanListRowArrow`.

```kotlin
SmartisanSectionTitle("Display")
SmartisanGroup {
    SmartisanGroupItem(SmartisanGroupRowPosition.Top, "Brightness", summary = "Auto")
    SmartisanSwitchRow(
        text = "Eye comfort",
        summary = "Reduce blue light",
        checked = eyeCare,
        onCheckedChange = { eyeCare = it },
        position = SmartisanGroupRowPosition.Bottom,
    )
}
```

`SmartisanSwitchRow`'s `position` switches the artwork to the right segment, so you do not need to
wrap it in anything.

### 4.2 List page

```kotlin
Box(Modifier.fillMaxSize()) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().smartisanVerticalScrollbar(listState),
    ) {
        item { SmartisanListVerticalGap() }
        itemsIndexed(contacts, key = { _, c -> c.id }) { index, contact ->
            SmartisanListItem(
                title = contact.name,
                summary = contact.phone,
                showDivider = index != contacts.lastIndex,
                leading = { SmartisanIcon(res = contact.icon, contentDescription = null) },
                onClick = { /* ... */ },
            )
        }
        item { SmartisanListVerticalGap() }
    }

    // letter index on the right
    SmartisanLetterIndexBar(
        letters = contacts.map { it.name.first() }.distinct(),
        onLetterSelected = { letter ->
            val index = contacts.indexOfFirst { it.name.first() == letter }
            if (index >= 0) scope.launch { listState.scrollToItem(index + 1) }
        },
        modifier = Modifier.align(Alignment.CenterEnd),
    )

    // empty state
    if (contacts.isEmpty()) {
        SmartisanEmptyHint(
            title = "No contacts",
            description = "Tap the top-right action to add one",
            actionText = "New contact",
            onActionClick = { /* ... */ },
        )
    }
}
```

- **Drag to reorder**: `SmartisanReorderableColumn(items, onMove = { from, to -> ... }) { index, item, dragging -> ... }`;
- **Swipe to delete**: `SmartisanSwipeToDelete(onDelete = { ... }) { row }` — the row is the
  foreground and must be opaque (use `SmartisanListItem` or a row that draws its own background; leave
  no transparent area or the delete panel shows through);
- **Groups + sticky headers**: use `LazyColumn`'s `stickyHeader` and swap the header for
  `SmartisanLetterSectionTitle` or `SmartisanListBoardSectionTitle`;
- The `+ 1` in `scrollToItem(index + 1)` accounts for the `SmartisanListVerticalGap()` at the top of
  the list.

### 4.3 Detail page (with a bottom action bar)

```kotlin
SmartisanScaffold(
    titleBar = { SmartisanTitleBar(title = "Album", navigationIcon = backAction) },
    bottomBar = {
        SmartisanBottomBar(
            items = listOf(
                SmartisanBottomBarItem(label = "Play", iconRes = SmartisanOriginalIcons.Play),
                SmartisanBottomBarItem(label = "Favourite", iconRes = SmartisanOriginalIcons.FavoriteAdd),
            ),
            selectedIndex = tab,
            onSelected = { tab = it },
        )
    },
) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        SmartisanListVerticalGap()
        SmartisanCard {
            // cover + title + subtitle
        }
        SmartisanListVerticalGap()
        SmartisanGroup {
            SmartisanGroupItem(SmartisanGroupRowPosition.Single, "Tracks", summary = "12 songs")
        }
        SmartisanListVerticalGap()
    }
}
```

`SmartisanBottomBar` brings its own navigation bar inset (`includeNavigationBar = true`). If you pass it
as the `bottomBar` of `SmartisanScaffold`, remember to pass `applyNavigationBarInset = false` as well,
otherwise you get the inset twice.

### 4.4 Form page

```kotlin
Column(
    Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .imePadding(),          // move up with the keyboard
) {
    SmartisanListVerticalGap()
    SmartisanGroup {
        SmartisanGroupItem(
            position = SmartisanGroupRowPosition.Top,
            title = "Name",
            trailing = {
                SmartisanClearableField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Required",
                    modifier = Modifier.width(180.dp),
                )
            },
        )
        SmartisanGroupItem(
            position = SmartisanGroupRowPosition.Middle,
            title = "Password",
            trailing = {
                SmartisanPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    revealPassword = revealed,
                    onRevealPasswordChange = { revealed = it },
                    modifier = Modifier.width(180.dp),
                )
            },
        )
        SmartisanGroupItem(SmartisanGroupRowPosition.Bottom, "Note", summary = "Optional")
    }
    SmartisanListVerticalGap()
    SmartisanButton(text = "Save", onClick = { /* ... */ })
    SmartisanTextButton(text = "Cancel", onClick = { /* ... */ })
}
```

- Long-form text uses `SmartisanMessageField` (it has a send key and a left icon slot);
- A search field in the title bar uses `SmartisanSearchBar(query, onQueryChange)`;
- Pitfall: give a field inside a row a fixed width with `Modifier.width(...)` instead of letting it
  fill — the row's right slot only has 6dp of margin.

### 4.5 Multi-select editing page

```kotlin
var selected by remember { mutableStateOf(setOf<Long>()) }
SmartisanScaffold(
    titleBar = {
        SmartisanTitleBar(
            title = if (selected.isEmpty()) "All songs" else "${selected.size} selected",
            navigationIcon = SmartisanTitleBarAction(SmartisanOriginalIcons.Back, "Back", onBack),
            actions = listOf(
                SmartisanTitleBarAction(SmartisanOriginalIcons.MultiSelect, "Select") { editing = true },
            ),
        )
    },
    bottomBar = {
        if (editing) {
            SmartisanBottomBar(
                items = listOf(
                    SmartisanBottomBarItem(label = "Delete", iconRes = SmartisanOriginalIcons.Delete),
                ),
                selectedIndex = -1,
                onSelected = { /* ... */ },
            )
        }
    },
) {
    LazyColumn {
        itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
            SmartisanListItem(
                title = song.title,
                summary = song.artist,
                selected = song.id in selected,        // the original's pale blue selection
                showDivider = index != songs.lastIndex,
                onClick = {
                    selected = if (song.id in selected) selected - song.id else selected + song.id
                },
            )
        }
    }
}
```

### 4.6 Dialogs and overlays

| Scenario | Component |
| --- | --- |
| Confirm / alert | `SmartisanConfirmDialog(onDismissRequest, title, message, onConfirm)` |
| Custom content + buttons | `SmartisanDialog(onDismissRequest, title, confirmText, dismissText, onConfirm)` |
| Bottom menu | `SmartisanMenuDialog` + `SmartisanMenuItem(text, onClick, danger = ...)` |
| Bottom sheet (you lay it out) | `SmartisanBottomSheet(onDismissRequest, title)` |
| Full modal page | `SmartisanModalPageTransition(visible = ...)` |
| Lightweight toast | `SmartisanSnackbarHost(state = ...)` + `rememberSmartisanSnackbarState()` |
| Spinner / progress | `SmartisanProgressDialog`, `SmartisanCircleProgress` |

Pitfall: dialogs and sheets consume the back key themselves; you do not need a `BackHandler`.

### 4.7 Clock and mechanical controls

- Dials: `SmartisanAnalogClock` (large) and `SmartisanCompactClock` (small);
- Wheels: `SmartisanWheelPicker(items, selectedIndex, onSelectedIndexChange)`;
- Rulers: `SmartisanRulerPicker`, `SmartisanPullRingRuler`;
- Weekdays: `SmartisanWeekdayPicker`, `SmartisanWeekdayChips`;
- Flip clock: `SmartisanFlipClock` (together with `SmartisanFlipClockDrawables` and `SmartisanFlipEasing`);
- World clock: `SmartisanWorldClockCard`.

These were XML + custom Views in the original; here they are rewritten with Compose Canvas and
`pointerInput`, so they need no View host and drop into any layout.

### 4.8 Calendar and date/time pickers

`SmartisanCalendar` (month view), `SmartisanDatePicker`, `SmartisanTimePicker` and
`SmartisanDateTimePicker`; the matching `...Dialog` variants pop up directly:

```kotlin
SmartisanDatePicker(
    year = year,
    month = month,
    day = day,
    onDateChange = { y, m, d -> year = y; month = m; day = d },
)
```

## 5. Theming: change only what you want

| Want to change | How |
| --- | --- |
| A few colours | `SmartisanTheme(colors = lightSmartisanColors().copy(accent = Color(0xFF3482FF)))` |
| Typography | `SmartisanTheme(typography = SmartisanTypography(...))`, or read `smartisanTypography` and override per component |
| Shapes | `SmartisanTheme(shapes = SmartisanShapes(medium = RoundedCornerShape(12.dp)))` |
| Fonts | `SmartisanTheme(fonts = SmartisanFonts(...))`; `SmartisanFontMode` picks the original fonts or the system ones |
| Overscroll | `SmartisanTheme(overscrollFactory = null)` (off) or your own `OverscrollFactory` |
| One component's dimensions | pass parameters: `contentHeight`, `minHeight`, `contentPadding`, `iconSize`, … |

The full colour, text style and shape values are in [Theme.md](Theme.md). To read them use
`LocalSmartisanColors.current` / `LocalSmartisanTypography.current`, or the three read-only accessors
`smartisanColors` / `smartisanTypography` / `smartisanShapes`.

## 6. Artwork, icons and alignment

Three objects — pick by what you need:

| Object | Contents | How to use |
| --- | --- | --- |
| `SmartisanOriginalIcons` | commonly used icons named by purpose (~90) | `SmartisanIcon(res = SmartisanOriginalIcons.Play, contentDescription = null)` |
| `SmartisanDrawables` | the backgrounds / icons / panels components use by default | pass to a component's `@DrawableRes` parameter, or use it yourself |
| `SmartisanIconSet` | the **catalogue** of 3617 icons in 9 groups | by name: `SmartisanIconSet.find("btn_more_selector")`; inside a group: `SmartisanIconSet.listRow.find("setting_item_icon_pencil")` |

- Every entry is a `SmartisanIconEntry(name, res)`, a group is a `SmartisanIconGroup(title, icons)`,
  `SmartisanIconSet.groups` holds all 9 groups and `SmartisanIconSet.size` the total;
- **Icon alignment comes from the slot, not the icon size**: a list row's left slot is a fixed 60dp and
  the icon is centred in it, so behind a 24dp or a 36dp icon the title always starts 72dp from the left
  screen edge (the `SmartisanListItem` KDoc has the comparison table);
- To use your own artwork: every component's artwork entry point is a `@DrawableRes` parameter, so pass
  your own resource. To replace it wholesale, keep the code, delete
  `library/ui/src/main/res/drawable*` and point `SmartisanDrawables` / `SmartisanIconSet` at your own
  resources.

**Night resources**: when the in-app theme and the system uiMode disagree (you switched to dark inside
the app but the system is still light), use `smartisanThemedResources` to get a `Resources` resolved
against the app theme, `smartisanWantsNightResources` to ask whether night resources are wanted, and
`smartisanColor` / `rememberSmartisanStateListColor` to resolve colour state lists inside selectors.

## 7. Interaction and motion

| What you want | What to use |
| --- | --- |
| Ripple-free tapping (self-drawn pressed state) | `Modifier.smartisanClickable { ... }`, or `rememberSmartisanInteractionSource()` + `collectSmartisanPressedAsState()` |
| System click sound / virtual key haptics | `smartisanClick { ... }` / `smartisanHaptic()` |
| Swap a text colour while pressed | `smartisanPressedTextColor(normal, highlight, pressed)` |
| The 48dp hit area of a checkbox | `smartisanCheckboxHit()` |
| Monospaced numerals | `smartisanNumericStyle(fontSize)` (mechanical digits use the original `SmartisanClock` font) |
| Overscroll rebound | provided by the theme; for one region use `Modifier.overscroll(...)`, or customise `SmartisanOverscrollFactory` / read `SmartisanOverscrollDefaults` |
| Page transitions | `SmartisanPageTransition` (regular), `SmartisanModalPageTransition` (modal) |
| Scrollbar | `Modifier.smartisanVerticalScrollbar(state)` |
| Group card artwork (when drawing your own row) | `smartisanGroupRowBackground(position)` / `smartisanGroupRowShadow(position)` |

Using **no ripple** is deliberate: the original changes colour / offsets / scales, and a Material ripple
instantly stops looking like Smartisan.

## 8. Localization and copy

- The library's 22 strings follow the system language; nothing for you to do;
- Your app's strings live in `values-*/strings.xml` as usual;
- To pin the language for demos or screenshots override `attachBaseContext` (see
  [QuickStart.md](QuickStart.md#11-localization));
- When adding a component: **all user-visible copy goes through `stringResource`**, with the default
  language in `library/ui/src/main/res/values/smartisanx_strings.xml`, plus at least Simplified Chinese,
  Traditional Chinese, Japanese and Korean.

## 9. Auxiliary API cheat sheet

The "small but useful" set — no need to go digging in the sources:

| API | Purpose |
| --- | --- |
| `SmartisanSwitchStyle` | the switch's two styles (music / clock); the original's two knobs differ in length |
| `SmartisanSnackbarData` | the current toast content (message / action / duration); the type of `SmartisanSnackbarState.current` |
| `SmartisanIconEntry` / `SmartisanIconGroup` | the catalogue's entry and group types (see section 6) |
| `SmartisanBlankDefaults` | default dimensions and copy for empty states (used by `SmartisanEmptyHint`) |
| `SmartisanCircleProgressDefaults` | default diameter and stroke width of the circular progress |
| `SmartisanMarqueeDefaults` | default marquee speed and delay |
| `SmartisanSpinnerDefaults` | default spinner dimensions |
| `SmartisanInputDefaults` | default field height and clear-button size |
| `SmartisanSnackbarDefaults` | toast durations |
| `SmartisanProgressDialogDefaults` | default progress dialog dimensions |
| `SmartisanBhmDefaults` | default dimensions of the multi-select sheet |

Prefer reusing these defaults when you write your own components so everything keeps the same feel.

## 10. Common pitfalls (symptom → cause → fix)

| Symptom | Cause | Fix |
| --- | --- | --- |
| "SmartisanColors not found" is thrown | no `SmartisanTheme` | wrap the outermost `setContent` |
| Content is hidden behind the system bar | you handle insets yourself, or used `applyNavigationBarInset = false` | add `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` |
| The keyboard covers the field | no IME insets | add `Modifier.imePadding()` to the content |
| Rounded corners appear on a middle row | wrong row `position` | see 3.2 |
| Card shadows smear together | a missing `SmartisanListVerticalGap()` | add one between every pair of groups |
| The page looks flat with no pinstripe | the group was given an opaque `color`, or `SmartisanScaffold` got `backgroundRes = null` | drop `color` / keep the default texture |
| The delete panel shows through during a swipe | the row is the foreground and must be opaque | use a row that draws its own background (`SmartisanListItem`); leave no transparent area |
| Some artwork stays light in dark mode | only ~2.5% of the original assets have night variants | a known limitation; dark mode is experimental |
| Icons look wrong after switching theme inside the app | resources are resolved by the system uiMode | use `smartisanThemedResources` / `smartisanWantsNightResources` |
| The launcher icon became a compass | `mipmap-anydpi-v26` was renamed to `mipmap-anydpi` | keep `-v26` (see `sample/lint.xml`) |
| The pressed state "flashes and disappears" on fast taps | you used `interactionSource.collectIsPressedAsState()` | use `collectSmartisanPressedAsState()`, which keeps the pressed state for same-frame taps |

## 11. On-device verification workflow

Do not stop at "it compiles" — the standard in this repository is **pixel-level verification on a real
device**.

Test device: a nubia P0110, 1264×2800 @560dpi (**1dp = 3.5px**); there is also a Nut R2
(1080×2340 @400dpi, 1dp = 2.5px) used to compare against the original.

```bash
# 1) Install (plain `adb install -r` hangs on the P0110; use push + pm install)
adb push sample/build/outputs/apk/debug/sample-debug.apk /data/local/tmp/s.apk
adb shell pm install -r -t /data/local/tmp/s.apk      # a USB install dialog appears: tap ACCEPT

# 2) This device's AOD is aggressive: it returns to Dozing + NotificationShade
adb shell input keyevent 224        # wake
adb shell input swipe 632 2200 632 700 200   # swipe up
# enter the PIN → confirm the focus is the target Activity before screenshotting
adb shell dumpsys window | grep mCurrentFocus

# 3) Reading widget bounds beats guessing coordinates
adb shell uiautomator dump /sdcard/p.xml && adb shell cat /sdcard/p.xml

# 4) Screenshot + measure pixels (ImageMagick, scanning a column for colour bands and edges)
adb shell screencap -p /sdcard/pg.png && adb pull /sdcard/pg.png /tmp/pg.png
magick /tmp/pg.png -crop 1x800+100+250 +repage -colorspace gray txt:- | head
```

Clean up afterwards (`/sdcard/*.png`, `/data/local/tmp/*.apk`, probe XML) and restore any device
settings you changed.

## 12. Adding a component to the library: the conventions

1. **Put it in the right package**: `library/ui/src/main/kotlin/cc/wuersan008/smartisanx/ui/<group>/`,
   choosing among the existing eight (basic / control / input / layout / list / overlay / clock / asset);
2. **Naming**: public API takes the `Smartisan` prefix (lower-case `smartisan*` is reserved for
   `Modifier` extensions and utility functions);
3. **KDoc is mandatory**: say which original resource / layout it corresponds to, quote the measured
   numbers, say when *not* to use it, and cross-reference the siblings people confuse it with;
4. **Copy the original artwork**: if a NinePatch / bitmap / selector exists, use it instead of drawing
   your own; expose it as a `@DrawableRes` parameter with a default;
5. **User-visible copy goes through resources**, default language in `values/smartisanx_strings.xml`;
6. **Add it to the sample**: show it on the matching page (`sample/src/main/kotlin/.../pages/`) and
   document the API and parameters in `docs/组件总览.md` + `docs/Components.md`;
7. **Run the gates**: `./gradlew lintDebug` (all three modules must say `No issues found.`) plus
   `python3 tools/doc_audit.py --check` (must say 全部一致。);
8. **Verify on a real device** and add the measurements to `docs/组件核对记录.md` +
   `docs/ComponentVerification.md` (both, always in pairs).

## 13. Pre-release checklist

- [ ] The app's root wraps `SmartisanTheme` and the system bar icons follow the colour scheme;
- [ ] Every row inside a group has the right `position`, and every pair of groups has a
      `SmartisanListVerticalGap()`;
- [ ] Long lists use `LazyColumn` + `key`, with `Modifier.smartisanVerticalScrollbar` for the scrollbar;
- [ ] Form pages add `Modifier.imePadding()`;
- [ ] Empty, loading and error states all have a screen (`SmartisanEmptyHint` / `SmartisanProgressDialog`);
- [ ] Dark mode is either off, or clearly labelled "experimental" in your own settings page;
- [ ] `./gradlew lintDebug` and `python3 tools/doc_audit.py --check` are both clean;
- [ ] You walked through the app on a device: overscroll rebound, swipe to delete, dialog back key,
      selection highlight, keyboard behaviour.





