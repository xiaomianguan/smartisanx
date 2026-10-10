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
| `SmartisanTitleBar` | Music `SmartisanTitleBar` + Weather `WeatherTitleBar` | ✅ icon 36dp, edge margin 6dp, title 20sp, shadow 14dp, press scale 1.33 all match; height is 48dp (see below). The 14dp shadow is drawn **outside** the bar and **over the content** (`BarsHelper`), so bar + content have no empty band between them — re-checked against a real Nut R2 (section 5) |
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
| `SmartisanListRow` family | framework `list_content_*` / `list_section_title_layout` / `list_board_section_title_layout` + Contacts `list_section.xml` | ✅ 60dp row height, 60dp left icon slot (content centred at 42dp), divider indents 18dp / 60dp, 30dp section title, 40dp board title, the Contacts 18dp letter band + 1dp shadow, all four type scales match; verified pixel by pixel on a real device (see section 4) |
| `SmartisanEditorRow` family | framework `AbsEditor` / `EditorLeftLabelWidget` / `EditorRightIconWidget` + three layouts | ✅ 44dp row height, 6dp sides, position-dependent original 9-patch background, 12sp label with a 12dp start margin, 40dp × 44dp icon container (26dp icon centred), trailing caption capped at 150dp, 2px inner divider; verified pixel by pixel on a real device (see section 4) |
| `SmartisanSmoothSeekBar` | framework `smartisanos.widget.SmoothSeekBar` + `SeekBarStyle` | ✅ thumb is the original `progress_control` / `progress_control_disabled` (118×147 / 108×144), track drawn at 2dp, dimensions taken from `SeekBarStyle.Thin.LargeThumb.Actived`; verified pixel by pixel on a real device (see section 4) |
| `SmartisanIconSlider` | framework `SliderWithIcons` + `slider_with_icons_layout.xml` | ✅ the original layout has no dimension constants (three `RelativeLayout` rules) and reuses the `SmoothSeekBar` above; end icons and slider share a vertical centre, verified pixel by pixel on a real device (see section 4) |
| `SmartisanComboTitleBar` | framework `SmartisanComboTitleBar` + `combo_title_layout` / `primary_title_layout` | ✅ 48dp main bar (`titlebar_height`), the centred / left-aligned centre-slot branches copied from `adjustContainerParams()`, and the shadow switching between `title_bar_shadow` / `_short` with the secondary bar; verified pixel by pixel on a real device (see section 4) |

## 2. Known differences (deliberate)

These are **intentionally** different from the originals; each is noted in the component's KDoc:

| Item | Original | Here | Why |
| --- | --- | --- | --- |
| Title bar height | Music 50dp / Weather & Clock 48dp | 48dp | The three apps disagree; taking the majority. Pass `contentHeight = 50.dp` for Music's |
| Dark mode | Does not exist | Experimental, off by default | Added by the revival projects; only ~2.5% of original assets have night variants |
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
| Combination bar centre natural width | Android measures `wrap_content` once, then decides on centring | Uses `maxIntrinsicWidth()` (Compose allows a single measure) | Custom layouts without intrinsic measurements are treated as "does not fit" |
| Combination bar trailing buttons | Ships up to ten `SmartisanButton`s with `-6dp` overlap | No button container; lay out the `trailing` slot yourself | `SmartisanButton` / `SmartisanIconButton` already cover the original button styles and icons, so the container logic is not worth building in |
| Message bar text / hint colours | framework `editor_text_color` (#cc000000) / `editor_hint_text_color` (#26000000) | theme `textPrimary` / `textHint` | Follows the smartisanx dark theme and matches the other input components |
| Message bar text listening | `beforeTextChanged` / `onTextChanged` / `afterTextChanged` | Collapsed into `onValueChange` | Compose offers a single change callback; the first two have no counterpart |
| Message bar navigation-bar insets | Not handled by the original (each app's layout does it) | Not handled either; the caller adds them | Keeps the original split of responsibilities |
| Progress dialog spinner animation | Uses the platform's indeterminate ProgressBar (`ProgressBarCircleStyle.Medium` sets no drawable) | Rotates the ROM's 48dp ring asset at a steady speed (1s per turn, overridable) | Compose has no platform indeterminate animation; the asset and the size still match the original |
| Progress dialog determinate progress | `setProgress(int)` is an empty method | Also only indeterminate | The original never supported determinate progress |
| BHM row disabled state | `BHMDrawerItem.isEnabled()` only blocks clicks; greying out is left to each app | Also only blocks clicks and switches the icon selector; the title colour comes from `SmartisanBhmItem.titleColor` | Keeps the original split |
| BHM header colour | `bhm_header_view_color` #4d000000 (30% black) | theme `textTertiary` (40% black) | Follows the dark theme |
| BHM list background | Hard-coded `@android:color/white` in the layout | theme `surface` | Follows the dark theme |

## 3. Assets and fonts

- **Graphic assets**: all taken from the factory APKs' `res/drawable*` / `mipmap*`, deduplicated by
  content, keeping original file names and night/density qualifier directories.
- **Fonts**: body text uses the Smartisan OS system font `Smartisan Compact CNS`, taken from the nut
  R2 factory ROM dump (`system/system/fonts/`). The internal family name and weights were verified by
  parsing the OTF `name` table (family = `Smartisan Compact CNS`).
- **Icons**: each selector chain was resolved to its underlying bitmap and classified, excluding 2195
  nine-patch button backgrounds and 1647 large images, leaving only real icons.

## 4. Pixel-level verification on a real device
The verification environment is macOS plus a 1264×2800 @ 560dpi test device (i.e. **3.5px per dp**),
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
| `SmartisanGroup` spacing | card → card (bare neighbours) | 14.3dp vs 14.0dp on the R2 Sound page — in the original this is a blank view **between** groups, not padding of the group |
| | card → section title | 21.4dp (R2 21.6dp) |
| | section title → card | 7.7dp (R2 7.2dp); the title carries its own spacing, no extra gap needed |
| `SmartisanSwitchRow` | row height | 59.7dp (white band), divider pitch 60.6dp; R2 reference 59.2dp / 60.0dp (the 6dp top/bottom padding belongs to the text column, not the row) |
| `SmartisanListSectionTitle` | band | 30.0dp tall, full width (x = 0..1263), text gray 149 (40% black) |
| `SmartisanListBoardSectionTitle` | band | 40dp white bar + 1px divider, text gray 101 (60% black) |
| `SmartisanLetterSectionTitle` | band | 63px = 18.0dp of `letter_seperater` (`#F5F5F5` with the 2px `#EBEBEB` bottom edge the asset itself carries), edge to edge (x = 0..1263); this asset and the shadow below are pixel-identical (same raw RGBA hash, 0 differing pixels) to `ContactsSmartisan.apk` `res/drawable-xxhdpi-v4/letter_seperater*.png` |
| | shadow | 4px ≈ 1dp of `letter_seperater_shadow`; the asset is 2px `#00000019` → `#00000007`, measured on screen as `#DADADA` → `#EBEBEB` (10.0% → 2.9% black over the page texture) |
| | text | 10sp bold at the 8dp start indent (ink starts at x = 31..32px, i.e. 8dp plus the glyph's left bearing), gray 147 `#939393` = 40% black on `#f5f5f5`; the glyph box is centred in the band (cap 1337..1360 around band centre 1349) |
| | total block | 67px = 19.1dp (18 + 1); the two bands ("A" and "B") measure identical 326px apart |
| `SmartisanAboutStaticItem` | row pitch | 52.9dp (185px) between separators; the original adds up to 7 + 13.5sp line + 3 + 12sp line + 7 + 1px + 5dp ≈ 52.5dp |
| | separator | 1px, x = 105..1210 (30dp start indent / 15dp end inset, both straight from the original dimens) |
| | text inset | title ink starts at x = 116px = the 30dp box plus the glyph's left bearing |
| | About page logo card | 180dp tall (the original `about_logo` 9-patch is 540px @ xxhdpi); the project logo is 88dp tall and 40dp below the card top (the original red lockup occupied 40..127.7dp); the version line's ink lands at 145dp and the trademark line's at 167dp (original 143dp / 165.7dp) |
| `SmartisanEditorRow` | row height | 44.0dp (label centres of adjacent rows are exactly 154px = 44dp apart) |
| | background | position-dependent original 9-patch; the seam between rows is the two 9-patch borders stacked into a 2px line (gray 211) |
| | leading label | 12sp, text gray 153 ⇒ `editor_label_text_color` (40% black); 12dp start margin (text starts at 30dp = 12dp card margin + 6dp row padding + 12dp) |
| | icon container | 40dp × 44dp with the 26dp icon centred (measured centre 38.1dp = container centre) |
| | inner 2px divider | gray 233 ⇒ theme `divider` (light `#E9E9E9`, i.e. the original `list_divider_color`'s 8% black over white) |
| | trailing caption / hint / disabled | caption 40% black, hint gray 219 (framework 15% black ≈ 217), disabled 30% black |
| `SmartisanSmoothSeekBar` | track | 2dp thick (8px in the screenshot including antialiasing), idle track gray `#E9E9E9` = theme `divider` |
| | progress colour | screenshot reads `#D44E47`, which is theme `accent` `#E64040` encoded as Display P3 (see below) |
| | thumb bitmap | the 118×147px bitmap scaled to 49dp height ⇒ 39.3dp × 49dp; its white disc is 19.7dp across (the rest is transparent shadow) |
| | value → position | slider 872px wide, `value = 0.6` puts the thumb centre at 705px measured; the "half a thumb of padding at each end" formula gives 705.4px |
| `SmartisanIconSlider` | end icons | 26dp box (91px in the screenshot), sharing the slider's vertical centre: icon centre y = 2417.0, track centre 2416.5, thumb centre 2414.5 (within 1dp inside one row) |
| | icon ink | the assets carry their own padding (inside a 26dp box `volume_small_n` is only 11.1dp wide, `volume_high_n` 15.7dp) |
| `SmartisanComboTitleBar` | bar height | 48.0dp (168px in the screenshot, the original `titlebar_height`) |
| | centre stays centred | with unequal side slots (147px / 182px) the title centre is still 632px = screen centre, same for the subtitle |
| | centre goes left | with a long title the left edge is 168px = 126px leading + 12dp (42px); the right edge is 1040px = trailing start 1082px − 12dp |
| | main bar shadow | first row gray 204 (asset row alpha 33 ≈ 13% black), fading down over 56px (16dp) back to the 241 background |
| | secondary bar shadow | first row gray 228 (asset row alpha 14 ≈ 5.5% black), fading down over 58px (16.6dp) |
| `SmartisanHiddenRowActions` | padding / gap | the whole strip is 273px = 12 + 24 + 6 + 24 + 12dp; the rightmost icon ends 42px (12dp) before the group card edge, and the two icon boxes are 21px (6dp) apart |
| | icon size | 84px boxes with an explicit 24dp; without `iconSize` it falls back to the bitmap's intrinsic size (`icon_delete_normal` is 36dp at xxhdpi) |
| | disabled state | `enabled = false` resolves the selector's disabled bitmap: in one strip the enabled action is the red `icon_delete_normal`, the disabled one is grey |
| `SmartisanMessageField` | overall height | 48.0dp (168px in the screenshot = the input area plus 8.33dp above and below) |
| | input area | `message_field.9.png` drawn at its intrinsic height (about 106px ≈ 30dp in the screenshot; the asset is 96px at xxhdpi) |
| | side icons | 36dp (126px, `standard_icon_size`); the leading icon starts 21px (6dp) in, the input area 42px (12dp) in |
| | bottom bar shadow | a 38px (11dp) whisper-light gradient above the bar's top edge (gray 247 → 242; the asset's peak alpha is only 4/255) plus a 2px divider (gray 233 = theme `divider`) |
| | send button | empty input resolves `icon_send_disabled` (pale green arrow); with text it switches to `sos_smartisanos_drawable_icon_send` (deep green) |
| `SmartisanProgressDialog` | card size | the node is 246dp (861px); the asset carries about 11dp of transparent margin on each side — which is also its padding — so the visible card is 224dp (789px) |
| | card height | the node is 156dp (546px): 18.67dp of 9-patch padding top and bottom plus title (8dp + a 20sp line box + 8dp), spinner (2dp + 48dp) and message (8dp + 20sp + 8dp) |
| | vertical positions | title text 1263–1336px, spinner ink 1392–1517px, message 1567–1620px — each one lines up with those spacings |
| | spinner | a 48dp box (168px); the 144px asset has 18px of transparent margin all round, so the visible ring is 36dp (126px) |
| | scrim | 0.54: white (255) behind the dialog measures 117 = 255 × 0.46 |
| | dark theme | swaps in `smartisan_progress_dialog_bg_dark` plus pure white text (the original's `setDarkTheme(true)`) |
| `SmartisanBhmSheet` | sheet position | bottom aligned; title bar 1398–1566px = 48dp; the list area starts at 1566px |
| | list top hairline | 2px across the width at about gray 235 (0.67dp of 8% black over the 245 surface), scrolling with the content |
| | section header | 24dp (1568–1652px) with the text starting 84px (24dp) from the left |
| | rows and icons | rows are 48dp (1652–1820px); the icon box is 18dp with a 15dp start margin, and the title text starts at 169px = 15 + 18 + 15dp |
| | subtitle | right edge at 1243px, i.e. 21px (6dp) from the screen edge |
| | count badge | the text's right edge lands near 1180px, so the 18dp margin is on top of the 9-patch's own padding |
| | page switch | driven by `pageKey`: the original animates for 400ms, this library uses the same duration with a quadratic decelerate easing |
| `SmartisanPreviewOptions` | cell height | 260px = 74.3dp (preview slot + 9dp + title line) |
| | two-column container | the cell's left edge sits at x = 42px (12dp, the original `SettingPreviewStyle` `paddingLeft/Right`); the divider between the columns comes from the `preview_options_two` 9-patch stretch column (asset 118x560px, stretch column x 56-61) |
| | head title | text left edge 105px = 30dp (the original `settings_item_title_left_margin`); 13.5sp, `#80000000` |
| | preview image | 108px asset, drawn at 126px on a 560dpi screen via drawable density scaling (xxhdpi asset x 560/480) |
| | check badge | 76px asset pinned to the preview image's top-right corner; the badge ink's top edge lines up with the cell's top edge (the 7px above it are the card 9-patch's top edge) |
| | title colours | measured `#353539` enabled (the original `setting_item_text_color`) and `#BABABA` disabled (`setting_item_text_color_disabled`), 15sp |
| | interaction | tapping the other column moves the selection; tapping the already-selected column fires no callback (the original `if (changed)`); the disabled switch greys the image, the title and the badge together |
| `SmartisanTwistGuide` | panel size | 303x453dp in portrait (1060x1585px) with another 16dp inset for the 9-patch: the measured white area is 939x1467px = 268.3x419.1dp, i.e. 271x421dp minus the 9-patch's own border pixels |
| | dim | `0x98000000` (about 60% black), fading out over 400ms on close |
| | close button | pinned to the panel's corner (not the background's); tapping it shrinks the panel towards the top-right corner over 300ms, rotates the button 320 degrees over 400ms and fades the dim over 400ms, and `onDismiss` fires only after all three finish (verified on device: the overlay page comes back) |
| `SmartisanCalendar` | title bar | 48dp (168px); the title `2016年8月` centred in a 174.6dp (611px) box; month arrows 51dp (179px) inset 57dp (200px) from either edge and vertically centred; a 1dp (3px) separator below — the on-device dump reports the buttons at `[200,598][379,766]`, matching 57dp / 48dp exactly |
| | weekday row | 29.3dp (102.6px) tall with 9dp / 7dp vertical margins and 12.3dp (43px) side margins; 10sp text in `black_60` `#666666`; order follows the country rule (Monday first in Simplified Chinese); the seven label centres are 168px apart, i.e. exactly one cell width, so the row lines up with the grid |
| | grid | rows 44dp (154px): the measured row hairlines sit at y = 931 / 1085 / 1239 / 1393 / 1547 / 1701, every row 154px; five weeks = 770px = 220dp; cell width `(1178 + 2.8) / 7` = 168.7px = 48.2dp; the 12.3dp side padding comes from the content frame 9-patch's own padding |
| | grid lines | the row background asset carries 10% black hairlines: about 2px at its top and bottom plus one vertical line per cell, measured at x = 210 / 380 / 549 / 718 / 888 / 1057 — exactly 1/7 intervals |
| | selected day | the blue pill (`#7098EF` family, the asset's own colour) sits in the cell centre; measured x 548..720 (173px = 49.4dp, i.e. cell width + 2 x 1dp minus the asset's own transparent edges) and y 1239..1393, inside the row plus 1.4dp on top and bottom that the asset's 5px transparent top edge eats, exactly as the original does |
| | other months | an 8% black grey block (`#E0E0E0` on white) plus a white day number, which leaves only a faint `#F8F8F8` antialiasing trace on the block, so the number is visually invisible; in the single-week view (progress = 1) the block fades out and the number switches to `black_60` |
| | today | that cell spells "today" (14sp, bold, white) with a blue pill when it is also the selected day and a light grey `#CDCDCD` pill otherwise; re-checked on device in the single-week view: selecting today gives the blue pill, selecting another day of the same week turns today's cell into the `#CCCCCC` grey pill with the white text still there |
| | interaction | tapping a cell moves the selection and the pill (tapping (633,1316) selected 2016-09-15 with the pill in column 3 of row 2); pressing "next month" retitles to `2016年9月` and moves the selection to the 1st (the original's `getAndCorrectDay`); tapping the title opens the date picker dialog (titled "选择要跳转的日期"); `hasFocus = false` shows every other-month number and drops the grey blocks |
| | single week | with `singleWeek = true` only the selected week is drawn, 67.6dp tall (measured 236px = 67.4dp) with the text baseline moved from 30dp down to 38dp, and the month stays the selected date's month (on device it showed `2026年10月` with only today's week) |
| `SmartisanActionButtonGroup` | bar and shadow | 48dp (168px) tall bar with the original `secondary_bar` background and the `smartisan_secondary_bar_shadow` asset above it (a different file from the `secondary_bar_shadow` the combo title bar uses, so each has its own constant) |
| | text buttons | 48dp (168px) tall, at least 66dp (231px) wide, 13.5sp bold and single-line ellipsised; with more than one button the background is picked by position from `selector_small_btn_filter_left/middle/right` (the same assets and `filter_button_text_shadow_colors` text shadow as the segmented group), a single button uses `selector_small_btn_standard` |
| | icon buttons | 60x48dp (210x168px) with `selector_small_btn_standard` and the icon inset 9dp (31.5px) from the start and 7dp (24.5px) from the end |
| | spacing | 6dp of bar padding, 6dp between buttons and 12dp between an icon button and the text buttons; passing `rightAction` left-aligns the text (the original `ACTION_MODE_BOTH`'s `setGravity(start|center)`); disabled buttons use an overall alpha of 0.3 |
| `SmartisanDialogAppInfo` / `SmartisanDialogSectionGroup` / `SmartisanDialogSingleChoiceRow` | app info | 36dp (126px) icon plus a 12dp (42px) gap; a 16sp bold `#9a000000` title over a 12.5sp `#66000000` summary, both single-line ellipsised (the `DialogPatternPrimaryText` / `DialogPatternSecondaryText` styles) |
| | three-part note | 15sp bold primary title, 12.5sp subtitle and 16sp message; 20dp / 18dp side padding (`dlg_text_view_padding_left` / `_right`) and an 18dp gap (`dlg_section_vertical_space`) |
| | two-line single choice | 60dp (210px) tall (`dlg_single_choice_height_has_summary`) with 20dp / 6dp padding; 16sp bold title over a 12.5sp summary that turns white while pressed (the original `dlg_single_choice_summary_colorlist`); a `selector_radio_choice` mark on the end that keeps its space when unselected via alpha 0 |

### `SmartisanFlipClock` / `SmartisanFlipCard` (lock-screen wireless-charging flip clock)

Original: `KeyguardSmartisan` (Android 11 darwin) — `widgets/flipnumber/FlipNumber` (one two-digit flip card),
`widgets/WirelessChargingTime` (hour and minute cards with an 18px gap) and `FlipClock`, drawn on the **landscape**
charging canvas (`layout/wireless_charging_display.xml`: 2242x1080px, `rotation="270"`). `Settings.Global.clock_theme_style = 0`
(the default) is this one, and the first preview in the Settings "Smartisan Clock Theme" page (`setting_clock_demo_01.9.png`)
shows it.

| Item | Original | Pixel check on device (nubia P0110, 560dpi; sample uses `digitWidth = 64.dp`) |
| --- | --- | --- |
| half-cell assets | `flip_{d}_{left,right}_{top,bottom}.png`, 454x468px; `left` = tens, `right` = units, `top` / `bottom` = upper / lower half; the upper digits are grey, the lower ones white | assets used as shipped (the 400dpi and xxhdpi buckets contain the very same file, both kept) |
| card size | 2 cells wide x 2 cells tall = 908x936px | one card measures 448x462px = **128.00dp x 132.00dp** (= 2x64dp x 2x64x468/454; the half height is snapped to whole pixels) |
| hour-to-minute gap | 18px (hard-coded `leftMargin = 18` in `WirelessChargingTime.onFinishInflate`) | measures 9px = **2.57dp** (= 64dp x 18/454 = 2.537dp, rounded to whole pixels); the whole clock is 905px wide |
| hinges | `flip_axle.png` 24x120px, aligned to the card's left / right edge (`alignLeft` / `alignRight`) and vertically centred on the fold | the left hinge sits 12px wide inside the card's left edge (x 408 onwards) with its centre exactly on the fold ✓ |
| fold line | each half asset keeps a **6px transparent strip** on the fold side, 12px together, and the original shows the charging canvas' pure black `#ff000000` through it | **found and fixed during the on-device check**: without a backing the fold showed a white seam (the sample page's white background). With `backingColor` (black by default) and a rounded clip of `digitWidth x 27/454` the fold is a dark line ✓ |
| cover layers | upper `cover_top` (alpha 229), lower `cover_bottom` (alpha 127); while flipping `UpperFlipAlphaAnimation(1→0)`, `LowerAlphaAnimation(1→0)`, `LowerFlipAlphaAnimation(1→0.5)` and `LowerShadowAlphaAnimation(0→1)` | all four alphas copied verbatim (Compose's `alpha` and the View `Transformation` alpha are both multiplicative) |
| flip animation | `UpperFlipAnimation(0→-180)` (pivot = bottom edge of the upper half) and `LowerFlipAnimation(180→0)` (pivot = top edge of the lower half), `Camera.setLocation(0, 0, -40)`, 1000ms, interpolator `FlipDownInterpolator(1.0f, 0.75f)` = ease-out-elastic | frame-by-frame from a screen recording (120Hz panel, ~109fps captured, three flips identical): the visible motion of 04 → 05 lasts **174–192ms** (about 65ms of main flip plus the rebound tail), matching an elastic curve that finishes early and converges; the perspective uses `cameraDistance = 40.dp` (5x Compose's 8dp default ↔ the original's -40 against the framework default of -8) |
| 12-hour mode | `FlipClock.updateTime`: with a 12-hour setting it shows AM / PM (`#ff7f00`, bold, 16dp) and maps hour 0 to 12 | with `use24Hour = false` it shows an orange `PM` (sRGB `#ff7f00` reads as `#ef8632` in a screenshot, consistent with the Display P3 note below) |
| sizing | the original is a fixed-pixel design on a 1080p landscape canvas with no scaling | the component defaults to the assets' intrinsic size (= the original's actual pixels at the same density) and accepts `digitWidth` for proportional scaling; the portrait sample uses 64dp |

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

## 5. Title-bar shadow placement (re-checked on a Nut R2)

The bar's 14dp shadow must be an **overlay**: it is drawn outside the bar's bounds and on top of the
content, so the content sits flush under the bar. The original does this in
`smartisanos.widget.BarsHelper.BarShadowBuilder`:

```java
shadow.setTranslationY(mIsBottomType ? -mBottomShadowHeight : mTopShadowHeight);  // moved out of bounds
parent.setClipChildren(false);                                                    // allowed to draw outside
mTargetView.setElevation(0.1f);                                                   // bar drawn above content
```

`SmartisanTitleBar` / `SmartisanTitleBarSurface` reproduce it with a child placed at the bar's bottom
edge and `offset(y = TitleBarShadowHeight)`, and `SmartisanScaffold` gives the bar `zIndex(1f)` for the
original's `setElevation(0.1f)`. An earlier version kept the shadow **in the layout flow**, which pushed
the content down by an extra 14dp and left a visible empty band between bar and content.

Measured on a real Nut R2 (`darwin`, 1080×2340, `ro.sf.lcd_density=560`, i.e. 3.5px/dp) with Settings
and with About this Phone (`about_settings_layout`, the layout our About page copies):

| | Nut R2 | Sample on the test device (1264×2800 @560dpi) |
| --- | --- | --- |
| bar background ends | y = 240 | y = 306 |
| shadow band | 240 → ~289 (14dp, `title_bar_shadow`) | 307 → ~357 (14dp) |
| content starts | at the bar's bottom edge | at the bar's bottom edge |
| first card's top edge | y = 273 — **the shadow falls on the card** | About: y = 366 (14dp `list_item_vertical_gap` + the card's 9-patch inset) — the shadow falls on the card |

Side-by-side captures of our About page and the R2's `About this Phone` were compared at the same
density; the bar/shadow/card relationship now matches (logo card, five-row group card and the plain
`AboutStaticItem` list all line up the same way).

