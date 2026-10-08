# Component verification record

[中文](组件核对记录.md) · **English**

This document records a component-by-component comparison of smartisanx against the original
implementations. The primary reference is the three revival projects (also Compose, so directly
comparable), then the custom Views and resources decompiled from the factory APKs.

Method: for each component, find the original implementation and compare **dimension tokens,
geometry, durations, easing, gesture physics and the assets used** — not just whether it "looks
similar".

## 1. Summary

| Component | Reference | Result |
| --- | --- | --- |
| `SmartisanTitleBar` | Music `SmartisanTitleBar` + Weather `WeatherTitleBar` | ✅ icon 36dp, edge margin 6dp, title 20sp, shadow 14dp, press scale 1.33 all match; height is 48dp (see below) |
| `SmartisanSwitch` | Music Compose switch + Clock `SmartisanSwitchView` / `SmartisanSwitchExView` | ✅ settle formula, 200ms cosine shadow fade, draggable knob and haptics all match |
| `SmartisanSwipeToDelete` | Clock `SmartisanSwipeDeleteMotion` | ✅ 65dp direct travel, 1/5 damping, 360dp cap match; threshold corrected to the original 50dp |
| `SmartisanAnalogClock` | Clock `AnalogClockHandsView` | ✅ 360×400 base canvas, hand anchors 6.9/8 and 6.5/8, numerals at 108dp / 84.6dp all match |
| `SmartisanCompactClock` | Clock `SmallWorldClockView` | ✅ anchors 6.7/8, 7.2/8, 6.5/8 and the 4px shadow offset match; 18:00–06:00 night dial matches |
| `SmartisanRulerPicker` | Clock `TimerRulerView` | ✅ "one minute = one scale bitmap width" tiling matches, as do the three caliper layers |
| `SmartisanModal` / `SmartisanDialog` | Music `SmartisanModal` + Clock `SmartisanModalDialog` | ✅ 308dp width, 10dp radius, 48dp title bar and buttons, 1px divider, 0.54 scrim all match |
| `SmartisanMenuDialog` | Clock `SmartisanMenuDialog` | ✅ bottom-anchored, 18dp sides, 18/24dp button margins, 48dp buttons match |
| `SmartisanBottomSheet` | Music `SmartisanAnimatedSheet` | ✅ slides up from the bottom, 300ms in / 250ms out match |
| `SmartisanLetterIndexBar` | Music letter quick bar + Clock `QuickBarEx` | ✅ hold-and-drag selection and the magnifier bubble match |
| `SmartisanPageTransition` | Music `PageStackTransition` | ✅ right slide, 300ms, `Smooth` / `Decelerate` match |
| `SmartisanModalPageTransition` | Weather `pop_up_in` / `slide_down_out` | ✅ bottom slide, 100%→0 and 0→109%, `decelerate_cubic` match |

## 2. Known differences (deliberate)

These are **intentionally** different from the originals; each is noted in the component's KDoc:

| Item | Original | Here | Why |
| --- | --- | --- | --- |
| Title bar height | Music 50dp / Weather & Clock 48dp | 48dp | The three apps disagree; taking the majority. Pass `contentHeight = 50.dp` for Music's |
| Dark mode | Does not exist | Experimental, off by default | Added by the revival projects; only ~19% of original assets have night variants |
| Text / hint colours | `editor_text_color` etc. from the framework | Theme semantic colours | Those resources only exist in the Smartisan framework APK |
| Cursor bar | nine-patch `edittext_cursor_bbackground` | Theme `accent` | Compose's `cursorBrush` only accepts a `Brush` |
| Clear-button visibility | Instant `setVisibility` | 200ms fade by default | Pass `animateClearIcon = false` to match the original exactly |
| Scrollbar | Platform default | Custom thin bar | The original has no matching bitmap |
| Text tab row | Does not exist | Custom | The originals switch tabs with icons only |

## 3. Assets and fonts

- **Graphic assets**: all taken from the factory APKs' `res/drawable*` / `mipmap*`, deduplicated by
  content, keeping original file names and night/density qualifier directories.
- **Fonts**: body text uses the Smartisan OS system font `Smartisan Compact CNS`, taken from the nut
  R2 factory ROM dump (`system/system/fonts/`). The internal family name and weights were verified by
  parsing the OTF `name` table (family = `Smartisan Compact CNS`).
- **Icons**: each selector chain was resolved to its underlying bitmap and classified, excluding 2195
  nine-patch button backgrounds and 1647 large images, leaving only real icons.

## 4. What has not been verified item by item

To be straight about it: the following could **not** be checked to pixel level, because there is no
device here to run the app on:

- actual rendered appearance (colours, shadow strength, how type sizes look on a real screen);
- animation smoothness and feel (the durations and easing values are verified, the look is not);
- gesture edge cases (fast drags, multi-touch, the exact feel of overscroll rebound);
- how the assets actually behave in dark mode.

If you want, I can take one specific component and diff its drawing code against the original
`onDraw` line by line.
