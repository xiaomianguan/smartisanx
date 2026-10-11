# Quick start

[中文](快速开始.md) · **English**

From nothing to a running Smartisan-style screen in about ten minutes. Every step gives code you can
copy as-is; the snippets match what the `sample` module actually does. Each section points at the
deeper document for that topic.

> Just want to see it? Jump to [section 0](#0-run-the-sample-first) — three commands install it on a
> phone.

## 0. Run the sample first

```bash
git clone https://github.com/xiaomianguan/smartisanx.git
cd smartisanx
./gradlew :sample:installDebug      # or :sample:assembleDebug and install the APK yourself
```

The sample app is this library's living documentation: 13 component pages (theme, text, icons,
buttons, basic controls, text & input, layout & lists, full-page layouts, list interaction, overlays,
clock & mechanical, calendar, about). The "full-page layouts" page holds 13 complete screens, each of
which can be copied as a starting point.

The APK is ~45MB because Smartisan's style is skeuomorphic: the texture comes from the original
bitmaps and NinePatches (10485 resource files in total), not from Compose redrawing them. More on
that under "Packaging and size".

## 1. Requirements

| Item | Requirement | Notes |
| --- | --- | --- |
| minSdk | 26 (Android 8.0) | |
| compileSdk | 37 | the library depends on Compose 1.12, so consumers cannot go lower |
| Kotlin | 2.4 | the Compose compiler plugin is required |
| Compose BOM | 2026.09.00 or newer | |
| AGP / Gradle / JDK | 9.4 / 9.8 / 17 or newer | since AGP 9 Kotlin compilation is built in, so modules no longer declare `kotlin-android` |

The library does **not** depend on Material / Material3. If your project also uses Material the two
can coexist, but smartisanx components must be wrapped in `SmartisanTheme`, not `MaterialTheme` (see
[the FAQ](#13-faq)).

## 2. Adding the dependency

### 2.1 Source dependency (recommended: you can edit the library)

```kotlin
// settings.gradle.kts
include(":library:core")
include(":library:ui")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":library:ui"))   // ui brings core along
}
```

### 2.2 Theme layer only

`core` works on its own (theme, colour scheme, text styles, shapes, motion, press feedback, drawable
painting):

```kotlin
dependencies {
    implementation(project(":library:core"))
}
```

### 2.3 Publish to local Maven

```bash
./gradlew publishToMavenLocal
```

```kotlin
dependencies {
    implementation("cc.wuersan008.smartisanx:smartisanx-ui:0.1.0")
}
```

Module graph: `sample` → `library:ui` → `library:core`. Components, original artwork and the icon
catalogue all live in `library:ui`.

## 3. Wrap everything in the theme

**Every component must be inside `SmartisanTheme`**, otherwise it throws an explicit "SmartisanColors
not found" error instead of silently using the wrong colours:

```kotlin
setContent {
    // light by default, matching the original Smartisan OS
    SmartisanTheme {
        App()
    }
}
```

`SmartisanTheme` provides three things — colours, text styles and shapes — and it also swaps the
**overscroll effect** for the original one (rigid translation + springy rebound rather than Material's
stretch), so `LazyColumn` / `Modifier.verticalScroll` inside your pages need no extra setup.

## 4. Your first screen

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // let content extend under the system bars; components handle their own insets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val controller = rememberSmartisanThemeController()
            SmartisanTheme(controller) {
                SmartisanScaffold(
                    titleBar = {
                        SmartisanTitleBar(
                            title = "My app",
                            navigationIcon = SmartisanTitleBarAction(
                                iconRes = SmartisanOriginalIcons.Back,
                                contentDescription = "Back",
                            ) { /* go back */ },
                            actions = listOf(
                                SmartisanTitleBarAction(
                                    iconRes = SmartisanOriginalIcons.More,
                                    contentDescription = "More",
                                ) { /* open menu */ },
                            ),
                        )
                    },
                ) {
                    SmartisanListVerticalGap()
                    SmartisanSectionTitle("First group")
                    SmartisanGroup {
                        SmartisanGroupItem(
                            position = SmartisanGroupRowPosition.Top,
                            title = "First row",
                            summary = "Supporting text",
                            leading = {
                                SmartisanIcon(
                                    res = SmartisanOriginalIcons.Settings,
                                    contentDescription = null,
                                )
                            },
                            trailing = { SmartisanListRowArrow() },
                            onClick = { /* ... */ },
                        )
                        SmartisanGroupItem(
                            position = SmartisanGroupRowPosition.Middle,
                            title = "A switch",
                            showDivider = true,
                        )
                        SmartisanGroupItem(
                            position = SmartisanGroupRowPosition.Bottom,
                            title = "Last row",
                        )
                    }
                }
            }
        }
    }
}
```

Four things you must know from this snippet:

1. **`SmartisanTheme` is mandatory** — see the previous section.
2. **`SmartisanScaffold` already looks like the original page**: the whole page is the `common_bg`
   pinstripe texture, the title bar reserves the status bar and draws its shadow outside its own
   bounds, and the bottom reserves the system navigation bar. Pass `backgroundRes = null` for a flat
   colour, or `applyNavigationBarInset = false` to handle insets yourself.
3. **The content area does not scroll by itself**: put `Modifier.verticalScroll(...)` or a
   `LazyColumn` inside it if you need scrolling.
4. **`SmartisanGroupItem` must be told its `position`**: the original splits a group card into
   top / middle / bottom / single artwork, with the rounded corners only on the first and last rows,
   so every row has to know which segment it is (next section).

## 5. Laying out content: cards, rows, gaps

An original page is "pinstripe texture + a stack of white cards". The white background, the rounded
corners and the outward shadow all come from **each row's own NinePatch**; the group container itself
is transparent. That gives two hard rules:

1. **Put a `SmartisanListVerticalGap()` between two neighbouring groups** (14dp, the original's
   `list_item_vertical_gap`) — both cards' shadows land in that gap and overlap there. No gap is
   needed around `SmartisanSectionTitle`: it carries its own padding.
2. **Every row inside a group must be told its `position`** (`SmartisanGroupRowPosition`:
   `Top` / `Middle` / `Bottom` / `Single`), which selects the card segment (rounded corners only on
   the first and last).

| What you need | What to use |
| --- | --- |
| A row inside a group (several rows forming one card) | `SmartisanGroupItem(position = ...)` |
| A standalone row (draws its own whole card and shadow) | `SmartisanListItem` |
| 2 / 3 lines of text with the adaptive layout | `SmartisanListRow` (`lines = SmartisanListRowLines.TwoLine`, …) |
| Non-row content inside a group (a row of controls, a custom block) | `SmartisanGroup(position = SmartisanGroupRowPosition.Single) { ... }` |
| A divider between rows | the row's `showDivider = true`, or `SmartisanRowDivider()` / `SmartisanGroupDivider()` |
| A standalone card (weather, album tiles) | `SmartisanCard { ... }` |

Switches and radios have ready-made rows too: `SmartisanSwitchRow(text, checked, onCheckedChange)`
and `SmartisanRadioRow(text, selected, onClick)` drop straight into a group
(`SmartisanSwitchRow` also takes a `position` directly).

```kotlin
SmartisanScaffold(titleBar = { SmartisanTitleBar(title = "Settings") }) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        SmartisanListVerticalGap()
        SmartisanSectionTitle("Notifications")
        SmartisanGroup {
            SmartisanGroupItem(SmartisanGroupRowPosition.Top, "Allow notifications")
            SmartisanGroupItem(SmartisanGroupRowPosition.Middle, "Show on lock screen", showDivider = true)
            SmartisanGroupItem(SmartisanGroupRowPosition.Bottom, "Notification sound")
        }
        SmartisanListVerticalGap()
        SmartisanSectionTitle("General")
        SmartisanGroup {
            SmartisanGroupItem(SmartisanGroupRowPosition.Single, "Language & input")
        }
        SmartisanListVerticalGap()
    }
}
```

> Why doesn't a group carry vertical spacing itself? In the original that 14dp is a **spacer View
> inserted between two groups**, not the group's own padding — see
> [Theme.md](Theme.md) for the measurements.

## 6. A second screen: transitions and back

The library ships the original transitions (regular pages slide in from the right, modal pages slide
up from the bottom) and needs no navigation library:

```kotlin
var detail by remember { mutableStateOf(false) }
SmartisanPageTransition(
    secondary = detail,
    primary = { HomeScreen(onOpen = { detail = true }) },
) {
    DetailScreen(onBack = { detail = false })
}
```

A modal page (say "New playlist") slides up instead:

```kotlin
SmartisanModalPageTransition(visible = showModal) { ModalScreen() }
```

The child screen owns its own back handling: the title bar's left action plus the system back key.

```kotlin
BackHandler(enabled = detail) { detail = false }
SmartisanTitleBar(
    title = "Detail",
    navigationIcon = SmartisanTitleBarAction(SmartisanOriginalIcons.Back, "Back") { detail = false },
)
```

## 7. Long lists

```kotlin
val listState = rememberLazyListState()
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize().smartisanVerticalScrollbar(listState),
) {
    item { SmartisanListVerticalGap() }
    items(songs, key = { it.id }) { song ->
        SmartisanListItem(
            title = song.title,
            summary = song.artist,
            onClick = { /* ... */ },
        )
    }
    item { SmartisanListVerticalGap() }
}
```

- Overscroll is already the original behaviour (rigid translation + springy rebound), nothing to
  configure.
- `Modifier.smartisanVerticalScrollbar(state)` draws the original scrollbar; there are overloads for
  both `ScrollState` and `LazyListState`.
- Drag to reorder: `SmartisanReorderableColumn(items, onMove) { index, item, dragging -> ... }`.
- Swipe to delete: `SmartisanSwipeToDelete(onDelete = { ... }) { row }`.
- Letter index: `SmartisanLetterIndexBar(letters, onLetterSelected = { ... })`.
- Empty list: `SmartisanEmptyHint(title, description = ..., actionText = ..., onActionClick = ...)`.

## 8. Dialogs and sheets

```kotlin
// confirmation dialog
if (showConfirm) {
    SmartisanConfirmDialog(
        onDismissRequest = { showConfirm = false },
        title = "This cannot be undone",
        message = "Delete this song?",
        confirmText = "Delete",
        onConfirm = { /* ... */ },
    )
}

// bottom menu
if (showMenu) {
    SmartisanMenuDialog(onDismissRequest = { showMenu = false }, title = "Song actions") {
        SmartisanMenuItem(text = "Play next", onClick = { /* ... */ })
        SmartisanMenuItem(text = "Delete", onClick = { /* ... */ }, danger = true)
    }
}

// bottom sheet (you lay out the content)
if (showSheet) {
    SmartisanBottomSheet(onDismissRequest = { showSheet = false }, title = "Pick a ringtone") { /* ... */ }
}
```

For custom content and buttons use
`SmartisanDialog(onDismissRequest, title, confirmText, dismissText, onConfirm) { ... }`; for lightweight
feedback use `SmartisanSnackbarHost(state = snackbar, ...)` together with
`rememberSmartisanSnackbarState()` and call `snackbar.show("Saved")`.

## 9. Light and dark (experimental)

**Light is the default, matching the original.** "Follow the system" and "dark" must be opted into
explicitly, and you have to tell your own users — the original shipped a single light design, dark mode
was added by the three revival projects, and only ~2.5% of the original graphic assets have night
variants (see [Theme.md](Theme.md#dark-mode-experimental)).

```kotlin
val controller = rememberSmartisanThemeController(SmartisanColorSchemeMode.System)
SmartisanTheme(controller) { /* ... */ }

controller.colorSchemeMode = SmartisanColorSchemeMode.Light   // default
controller.colorSchemeMode = SmartisanColorSchemeMode.System  // follow system (experimental)
controller.colorSchemeMode = SmartisanColorSchemeMode.Dark    // dark (experimental)
```

Switch the system bar icons along with it:

```kotlin
val view = LocalView.current
val isLight = LocalSmartisanColors.current.isLight
SideEffect {
    val window = (view.context as Activity).window
    val insets = WindowCompat.getInsetsController(window, view)
    insets.isAppearanceLightStatusBars = isLight
    insets.isAppearanceLightNavigationBars = isLight
}
```

## 10. Icons and artwork

Icons are **not** a hand-drawn vector set — they use the original artwork directly (the selectors
already carry pressed / disabled states):

| Object | Purpose |
| --- | --- |
| `SmartisanOriginalIcons` | Commonly used icons named by purpose: `Back`, `More`, `Search`, `Settings`, `Delete`, `Play`, … |
| `SmartisanDrawables` | The artwork the components use by default (title bar, dialogs, group cards, clock dials, …); you can use it directly too |
| `SmartisanIconSet` | The full catalogue of 3617 icons extracted from the original APKs, in 9 groups, browsable group by group |

```kotlin
// a named icon
SmartisanIcon(res = SmartisanOriginalIcons.More, contentDescription = "More")

// one group of the catalogue (here: the list-row icons)
SmartisanIconSet.listRow.icons.forEach { entry ->
    SmartisanIcon(res = entry.res, contentDescription = entry.name)
}

// no matching original asset? pass your own vector
SmartisanIcon(imageVector = myImageVector, contentDescription = null)
```

## 11. Localization

- Every user-visible string inside the library is a resource: 19 of the 22 strings carry AOSP
  translations for 80 languages; the 3 added later (the calendar's "Today" and the twist-guide title
  and body) have Simplified Chinese, Traditional Chinese, Japanese and Korean, falling back to English
  elsewhere.
- Components follow the system language by default; you do not have to do anything.
- Your own strings live in your own module's `values-*/strings.xml` as usual.

To pin the language (for demo screenshots, say) override `attachBaseContext`:

```kotlin
override fun attachBaseContext(newBase: Context) {
    val locale = Locale.SIMPLIFIED_CHINESE
    Locale.setDefault(locale)
    val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
    super.attachBaseContext(newBase.createConfigurationContext(config))
}
```

## 12. Packaging and size

- **Most of the APK is original artwork**: `library/ui/src/main/res` holds 10485 resource files and the
  debug APK is ~45MB. R8 affects code only, not resources — to slim it down, delete the `drawable*`
  folders you do not use, or keep only the assets you need (every component's artwork entry point is a
  parameter, so you can swap in your own images).
- **No extra ProGuard rules are needed**: the library uses no reflection and Compose ships its own
  rules through AGP.
- When replacing the launcher icon **do not rename `mipmap-anydpi-v26` to `mipmap-anydpi`**: AGP then
  silently drops the adaptive icon from the APK and the launcher falls back to the original compass
  bitmap (the reason is written up in `sample/lint.xml`).

## 13. FAQ

**Q: Why do the components look so square?**
Smartisan's lists, title bars and group cards are all right-angled; only dialogs, sheets and cards have
small corner radii. Want rounded corners? Pass your own `SmartisanShapes` to `SmartisanTheme`.

**Q: Why is there no ripple on tap?**
The original press feedback changes colour / offsets / scales the element, so components use
`indication = null` and draw their own pressed state. To get the same feel in your own composables:
`Modifier.smartisanClickable { ... }`.

**Q: Do I have to use `SmartisanScaffold`?**
No. It is just a convenience combination of "title bar + content + bottom bar + navigation bar inset";
every component works on its own.

**Q: Why is `SmartisanTheme` mandatory?**
Components read colours, text styles and shapes from `CompositionLocal`. Without the theme they throw
an explicit error instead of quietly falling back to defaults — deliberate, so you never ship a screen
whose colours "look a bit off" without anyone noticing.

**Q: Why is dark mode experimental?**
The original has no dark mode. The dark colour scheme and part of the artwork (~2.5%) were added by the
three revival projects, so dark is less faithful than light. Whether to ship it is your call.

**Q: Can I use it together with Material?**
Yes, the two do not interfere; smartisanx components just have to be inside `SmartisanTheme` rather
than `MaterialTheme`.

**Q: Does the library ship assets from the original APKs?**
Yes, deliberately: Smartisan's style is skeuomorphic and its texture comes from NinePatches, bitmaps
and selectors. See "Asset sources and licensing" in the README.

## 14. Next steps

- [DevelopmentGuide.md](DevelopmentGuide.md): screen recipes (settings / list / form / overlay pages),
  common pitfalls, and the conventions for adding components to the library.
- [Components.md](Components.md): the API and parameters of every component.
- [Theme.md](Theme.md): the full set of colour, text style, shape, dimension and motion values.
- Read the `sample` module's source: every page there is a copyable example.




