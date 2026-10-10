# Quick start

[中文](快速开始.md) · **English**

## Requirements

| Item | Requirement |
| --- | --- |
| minSdk | 26 (Android 8.0) |
| compileSdk | 37 (the library depends on Compose 1.12, so consumers need 37 or higher) |
| Kotlin | 2.4 (the Compose compiler plugin is required; AGP 9 compiles Kotlin itself, no kotlin-android needed) |
| Compose BOM | 2026.09.00 or newer |
| AGP | 9.4 or newer (Gradle 9.8 or newer, JDK 17 or newer) |

The library does not depend on Material / Material3. If your project also uses Material the two can
coexist, but make sure the smartisanx components are wrapped in `SmartisanTheme`, not
`MaterialTheme`.

## Adding the dependency

### Option 1: source dependency (recommended if you plan to modify the library)

```kotlin
// settings.gradle.kts
include(":library:core")
include(":library:ui")
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":library:ui"))
}
```

### Option 2: only part of the library

```kotlin
dependencies {
    implementation(project(":library:core"))  // theme, motion, drawable painting only
}
```

### Option 3: publish to local Maven

```bash
./gradlew publishToMavenLocal
```

```kotlin
dependencies {
    implementation("cc.wuersan008.smartisanx:smartisanx-ui:0.1.0")
}
```

## Your first screen

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                            ) { /* navigate back */ },
                            actions = listOf(
                                SmartisanTitleBarAction(
                                    iconRes = SmartisanOriginalIcons.More,
                                    contentDescription = "More",
                                ) { /* open menu */ },
                            ),
                        )
                    },
                ) {
                    SmartisanGroup {
                        SmartisanListItem(title = "First row", summary = "Supporting text")
                        SmartisanRowDivider()
                        SmartisanSwitchRow(
                            text = "A switch",
                            checked = true,
                            onCheckedChange = { /* ... */ },
                        )
                    }
                }
            }
        }
    }
}
```

Key points:

1. **`SmartisanTheme` is mandatory.** Components read `LocalSmartisanColors`; without the theme
   they throw an explicit "SmartisanColors not found" error instead of silently using wrong colors.
2. **The title bar reserves the status bar.** `SmartisanTitleBar` defaults to
   `includeStatusBar = true`, which pushes content down by a spacer as tall as the status bar.
   If you already handle insets outside, pass `false`.
3. **Press feedback is not a ripple.** Smartisan's press feedback swaps colors, offsets or scales,
   so components use `indication = null` internally and you do not need to configure anything.

## Light and dark mode

**This library is light by default**, matching the original Smartisan OS.

"Follow the system" and "dark" are **experimental** and must be opted into explicitly, with a
notice to your own users — the original shipped a single light design, dark mode was added by the
three revival projects, and only ~2.5% of the original graphic assets have night variants.
See [Theme.md](Theme.md#dark-mode-experimental).

```kotlin
// default: light, nothing to do
SmartisanTheme { /* ... */ }

// opt-in: follow the system / dark (experimental)
val controller = rememberSmartisanThemeController(SmartisanColorSchemeMode.System)
SmartisanTheme(controller) { /* ... */ }

// switch at runtime
controller.colorSchemeMode = SmartisanColorSchemeMode.Light  // default
controller.colorSchemeMode = SmartisanColorSchemeMode.System // follow system (experimental)
controller.colorSchemeMode = SmartisanColorSchemeMode.Dark   // dark (experimental)
```

You can also use the colours-only overload without a controller:

```kotlin
SmartisanTheme(colors = lightSmartisanColors()) { /* ... */ }   // default
SmartisanTheme(colors = systemSmartisanColors()) { /* ... */ }  // follow system (experimental)
SmartisanTheme(colors = darkSmartisanColors()) { /* ... */ }    // dark (experimental)
```

It is a good idea to switch the system bar icons at the same time:

```kotlin
val view = LocalView.current
val isLight = LocalSmartisanColors.current.isLight
SideEffect {
    val window = (view.context as Activity).window
    val c = WindowCompat.getInsetsController(window, view)
    c.isAppearanceLightStatusBars = isLight
    c.isAppearanceLightNavigationBars = isLight
}
```

## Customizing the theme

All theme objects are immutable data classes, so you can derive new ones with `copy`:

```kotlin
val colors = if (isSystemInDarkTheme()) darkSmartisanColors() else lightSmartisanColors()
SmartisanTheme(
    colors = colors.copy(accent = Color(0xFF3482FF)),
    typography = SmartisanTypography(),
    shapes = SmartisanShapes(),
) {
    // ...
}
```

For finer-grained overrides (dimensions, motion) pass parameters directly to the component:

```kotlin
SmartisanTitleBar(title = "Title", contentHeight = 52.dp)
SmartisanSwitch(checked = on, onCheckedChange = { on = it }, hapticsEnabled = false)
```

## FAQ

**Q: Why do the components look so square?**
Smartisan's lists, title bars and group cards are all right-angled; only dialogs, sheets and cards
have small corner radii. If you want rounded corners, pass your own `SmartisanShapes` to the theme.

**Q: Do I have to use `SmartisanScaffold`?**
No. `SmartisanScaffold` is just a convenience combination of "title bar + content + bottom bar".
Every component can be used on its own.

**Q: Does the library ship assets from the original APKs?**
Yes, deliberately. Smartisan's style is skeuomorphic and its texture comes from NinePatches,
bitmaps and selectors, so `library/ui/src/main/res/` uses the original artwork recovered by the
three revival projects (10481 files). Icons come from those same assets too (see `SmartisanDrawables`
/ `SmartisanOriginalIcons`); you only need your own `ImageVector` where no original asset exists.
See "Asset sources and licensing" in the README.

**Q: Why not use Material's `Switch` / `Button`?**
Smartisan's switches, buttons and dialogs have their own visuals and feel (draggable knob, shrink
on press, bottom-anchored menu). Material components cannot express those without rewriting their
drawing, so everything here is drawn from scratch.
