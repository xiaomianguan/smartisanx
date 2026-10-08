/**
 * 控件：锤子数字滚轮选择器。
 *
 * 对应原版 `SmartisanNumberPicker` 系列：锤子时钟的 `com.smartisanos.clock.view.SmartisanNumberPicker`
 * （Clock 7.1.1）、锤子音乐的 `com.smartisanos.ui_widget.SmartisanNumberPicker`（Music 8.1.0），
 * 以及锤子日历的 `smartisanos.widget.SmartisanNumberPickerEx`（Calendar 8.1.2）。
 * 三者都是「范围 + 格式化 + 循环 + 高亮单位后缀」的数字滚轮。
 *
 * **去重说明**：本库已经有一套滚轮实现 `ui.clock.SmartisanWheelPicker`
 * （LazyColumn + 中心吸附 + 按离中心行的距离插值字号 / 颜色 / 渐隐）。
 * 因此这里**不重复实现滚轮**，只做两件事：
 * 1. 给 [cc.wuersan008.smartisanx.ui.clock.SmartisanWheelPicker] 补上原版的
 *    「循环滚动」（`wrap`，原版 `setWrapSelectorWheel`），三份内容原地接续、视觉无跳变；
 * 2. 本组件负责原版数字选择器的其余语义：**取值区间**（`setMinValue` / `setMaxValue`）、
 *    **格式化**（`setFormatter`）、**高亮单位后缀**（`mHightlightSuffix`）
 *    以及每滚过一行时的触感反馈（原版 `VibratorSmt` / `SoundPool` 的声音与震动）。
 *
 * 单位后缀按原版排版：紧跟在选中行文字右侧，字号
 * `dimen/smartisan_numberpicker_hightlight_suffix_font_size = 12sp`，
 * 间距 `dimen/smartisan_numberpicker_hightlight_suffix_margin = 12dp`，
 * 颜色取高亮色（原版 `mHighlightColor`，日历里是 `calander_date_pick_select_day_color`，
 * 本库默认用主题强调色，可用参数覆盖）。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.clock.SmartisanWheelPicker
import kotlin.math.roundToInt

/** 单位后缀字号，原版 `dimen/smartisan_numberpicker_hightlight_suffix_font_size = 12sp`。 */
private val SuffixFontSize = 12.sp

/** 单位后缀与数字之间的间距，原版 `dimen/smartisan_numberpicker_hightlight_suffix_margin = 12dp`。 */
private val SuffixMargin = 12.dp

/** 选中行的字号，与 [SmartisanWheelPicker] 的选中字号一致（原版选中行 18sp）。 */
private val SelectedLabelFontSize = 18.sp

/**
 * 锤子数字滚轮选择器。
 *
 * ```kotlin
 * SmartisanNumberPicker(
 *     value = minute,
 *     onValueChange = { minute = it },
 *     minValue = 0,
 *     maxValue = 59,
 *     formatter = { "%02d".format(it) },
 *     unit = "分",
 * )
 * ```
 *
 * @param value 当前值，会被收敛到 `minValue..maxValue`。
 * @param onValueChange 值变化回调（滚动停止后触发一次），对应原版 `OnValueChangeListener`。
 * @param modifier 外部修饰符。
 * @param minValue 最小值，对应原版 `setMinValue`。
 * @param maxValue 最大值，对应原版 `setMaxValue`。
 * @param wrap 是否循环滚动，对应原版 `setWrapSelectorWheel`。
 * @param formatter 取值到文字的格式化，对应原版 `setFormatter`（如 `%02d` 补零）。
 * @param unit 选中行右侧的单位后缀（原版 `mHightlightSuffix`）；null 表示不显示。
 * @param visibleCount 可见行数，原版 `SELECTOR_WHEEL_ITEM_COUNT = 9`（本库沿用滚轮默认的 5 行）。
 * @param itemHeight 单行高度。
 * @param showSelectionLines 是否绘制选中行上下两条参考线。
 * @param unitColor 单位后缀颜色，默认取主题强调色（原版取高亮色）。
 * @param hapticFeedbackOnChange 每次值变化是否触发虚拟按键触感，对应原版滚轮的声音 + 震动反馈。
 */
@Composable
fun SmartisanNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 9,
    wrap: Boolean = true,
    formatter: (Int) -> String = { number -> number.toString() },
    unit: String? = null,
    visibleCount: Int = 5,
    itemHeight: Dp = 40.dp,
    showSelectionLines: Boolean = true,
    unitColor: Color = Color.Unspecified,
    hapticFeedbackOnChange: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val haptic = smartisanHaptic()
    val low = minOf(minValue, maxValue)
    val high = maxOf(minValue, maxValue)
    val count = high - low + 1
    val labels = remember(low, high, formatter) { (low..high).map(formatter) }
    val selectedIndex = (value - low).coerceIn(0, count - 1)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        SmartisanWheelPicker(
            items = labels,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = { index ->
                if (hapticFeedbackOnChange) haptic()
                onValueChange(low + index)
            },
            visibleCount = visibleCount,
            itemHeight = itemHeight,
            showSelectionLines = showSelectionLines,
            wrap = wrap,
        )
        if (unit != null) {
            // 原版把单位后缀画在选中行文字右侧：间距 12dp、字号 12sp、颜色取高亮色。
            val measurer = rememberTextMeasurer()
            val labelStyle = typography.numeric.copy(fontSize = SelectedLabelFontSize, fontWeight = FontWeight.Bold)
            val labelWidth = remember(measurer, labels, selectedIndex, labelStyle) {
                measurer.measure(text = labels[selectedIndex], style = labelStyle).size.width
            }
            val offsetX = labelWidth / 2f + with(density) { SuffixMargin.toPx() }
            SmartisanText(
                text = unit,
                modifier = Modifier.offset { IntOffset(offsetX.roundToInt(), 0) },
                color = if (unitColor == Color.Unspecified) colors.accent else unitColor,
                fontWeight = FontWeight.Bold,
                fontSize = SuffixFontSize,
                maxLines = 1,
            )
        }
    }
}

