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
| `SmartisanListRow` family | framework `list_content_*` / `list_section_title_layout` / `list_board_section_title_layout` | ✅ 60dp row height, 60dp left icon slot (content centred at 42dp), divider indents 18dp / 60dp, 30dp section title, 40dp board title, all four type scales match; verified pixel by pixel on a real device (see section 4) |
| `SmartisanEditorRow` family | framework `AbsEditor` / `EditorLeftLabelWidget` / `EditorRightIconWidget` + three layouts | ✅ 44dp row height, 6dp sides, position-dependent original 9-patch background, 12sp label with a 12dp start margin, 40dp × 44dp icon container (26dp icon centred), trailing caption capped at 150dp, 2px inner divider; verified pixel by pixel on a real device (see section 4) |
| `SmartisanSmoothSeekBar` | framework `smartisanos.widget.SmoothSeekBar` + `SeekBarStyle` | ✅ thumb is the original `progress_control` / `progress_control_disabled` (118×147 / 108×144), track drawn at 2dp, dimensions taken from `SeekBarStyle.Thin.LargeThumb.Actived`; verified pixel by pixel on a real device (see section 4) |
| `SmartisanIconSlider` | framework `SliderWithIcons` + `slider_with_icons_layout.xml` | ✅ the original layout has no dimension constants (three `RelativeLayout` rules) and reuses the `SmoothSeekBar` above; end icons and slider share a vertical centre, verified pixel by pixel on a real device (see section 4) |

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
| Section-title background | framework `#f5f5f5` | theme `surfaceRaised` (light `#F7F8F9`) | Must follow the theme in dark mode; within 3 gray levels of the original in light mode |
| Section-title text colour | framework `#4c000000` (30% black) | theme `textTertiary` (`#66000000`, 40%) | Uses the semantic token and matches `SmartisanGroup`'s section title |
| Row summary colour | framework `#80000000` (50% black) | theme `textTertiary` (`#66000000`, 40%) | Same tier as `SmartisanListItem`'s summary, so the library has one gray, not two |
| Row disabled text colour | framework opaque `#bababa` | theme `textDisabled` (`#4c000000`, 30% black ⇒ ≈179 on white) | Semantic token; less than 8 gray levels off the original |
| Editor hint text colour | framework `#26000000` (15% black ⇒ ≈217 on white) | theme `textHint` (`#DBDBDB` ⇒ 219) | Semantic token; 2 gray levels off the original |
| Slider end icons | Original shows both icons or neither (only calls `setImageResource` when `leftIconRes > 0 && rightIconRes > 0`) | Two independent optional slots; you get whichever you pass | Slot-based API reads better; the original rule looks like an oversight |
| End icons when disabled | Only the thumb bitmap is swapped, the icons stay as they are | Same (the library does nothing) | Keeps the original behaviour; whether the icons should grey out is up to the caller |

## 3. Assets and fonts

- **Graphic assets**: all taken from the factory APKs' `res/drawable*` / `mipmap*`, deduplicated by
  content, keeping original file names and night/density qualifier directories.
- **Fonts**: body text uses the Smartisan OS system font `Smartisan Compact CNS`, taken from the nut
  R2 factory ROM dump (`system/system/fonts/`). The internal family name and weights were verified by
  parsing the OTF `name` table (family = `Smartisan Compact CNS`).
- **Icons**: each selector chain was resolved to its underlying bitmap and classified, excluding 2195
  nine-patch button backgrounds and 1647 large images, leaving only real icons.

## 4. Pixel-level verification on a real device

The environment is now macOS plus a real nut R2 phone (1264×2800 @ 560dpi, i.e. **3.5px per dp**),
so components can be installed, screenshotted and their **real sizes and colours measured back from
the pixels** instead of only reading values. The method: scan rows/columns of a screenshot (first and
last "inked" pixel, the edges and gray levels of flat colour bands) and compare against the framework
dimens and colours.

Components verified this way so far:

| Component | Item | Measured |
| --- | --- | --- |
| `SmartisanListRow` | row height | 60.0dp (the selected pale-blue background is exactly 210px) |
| | left icon slot | checkbox centre x = 42.0dp = 12dp card margin + 30dp (half of the 60dp slot) |
| | divider | 2px `#F2F2F2`; indent 18.0dp by default, 60.6dp on rows with a left slot |
| | primary text colour | gray 51 ⇒ `#cc000000` (80% black), matching the original |
| | summary / disabled | summary gray 153 (40% black), disabled gray 179 (30% black) |
| `SmartisanListSectionTitle` | band | 30.0dp tall, full width (x = 0..1263), text gray 149 (40% black) |
| `SmartisanListBoardSectionTitle` | band | 40dp white bar + 1px divider, text gray 101 (60% black) |
| `SmartisanEditorRow` | row height | 44.0dp (label centres of adjacent rows are exactly 154px = 44dp apart) |
| | background | position-dependent original 9-patch; the seam between rows is the two 9-patch borders stacked into a 2px line (gray 211) |
| | leading label | 12sp, text gray 153 ⇒ `editor_label_text_color` (40% black); 12dp start margin (text starts at 30dp = 12dp card margin + 6dp row padding + 12dp) |
| | icon container | 40dp × 44dp with the 26dp icon centred (measured centre 38.1dp = container centre) |
| | inner 2px divider | gray 233 ⇒ `list_divider_color` (8% black) |
| | trailing caption / hint / disabled | caption 40% black, hint gray 219 (framework 15% black ≈ 217), disabled 30% black |
| `SmartisanSmoothSeekBar` | track | 2dp thick (8px in the screenshot including antialiasing), idle track gray `#E9E9E9` = theme `divider` |
| | progress colour | screenshot reads `#D44E47`, which is theme `accent` `#E64040` encoded as Display P3 (see below) |
| | thumb bitmap | the 118×147px bitmap scaled to 49dp height ⇒ 39.3dp × 49dp; its white disc is 19.7dp across (the rest is transparent shadow) |
| | value → position | slider 872px wide, `value = 0.6` puts the thumb centre at 705px measured; the "half a thumb of padding at each end" formula gives 705.4px |
| `SmartisanIconSlider` | end icons | 26dp box (91px in the screenshot), sharing the slider's vertical centre: icon centre y = 2417.0, track centre 2416.5, thumb centre 2414.5 (within 1dp inside one row) |
| | icon ink | the assets carry their own padding (inside a 26dp box `volume_small_n` is only 11.1dp wide, `volume_high_n` 15.7dp) |

Still **not** verified item by item:

- animation smoothness and feel (the durations and easing values are verified, the look is not);
- gesture edge cases (fast drags, multi-touch, the exact feel of overscroll rebound);
- how the assets actually behave in dark mode (the on-device demo only covers light mode).

If you want, I can take one specific component and diff its drawing code against the original
`onDraw` line by line.

### Screenshot colours are Display P3

Colour pixels in a device screenshot are **Display P3** encoded, not sRGB: theme `accent` `#E64040`
reads as `#D44E47` in a screenshot, and converting the sRGB value to P3 coordinates
(`(230,64,64) → (212,78,71)`) matches the measurement exactly. So **do not change code colours based on
screenshot colours** — geometry and gray levels are unaffected (neutral grays have identical coordinates
in sRGB and P3), but chroma has to be converted first.
