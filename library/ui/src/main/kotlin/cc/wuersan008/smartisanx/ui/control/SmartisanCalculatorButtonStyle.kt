package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 计算器按键的底图样式，对应原版 `layout/main.xml` 里给按键设的 `android:background`。
 */
enum class SmartisanCalculatorButtonStyle {
    /** 白色按键（数字与常规运算）。 */
    White,

    /** 灰色按键（带焦点态的 `_focus` 变体）。 */
    Grey,

    /** 黑色按键（记忆键等）。 */
    Black,

    /** 数字 0，双宽。 */
    DigitZero,

    /** 等号，红色双高。 */
    Equal,
}

/**
 * 取该样式对应的原版 selector。
 *
 * 原版的「高亮」不是额外画一个角标，而是换成带焦点态的 selector
 * （`cal_selector_btn_grey_focus` / `cal_selector_btn_black_focus`）。
 * 白色按键没有焦点变体，[highlighted] 对它无效。
 */
@DrawableRes
internal fun SmartisanCalculatorButtonStyle.backgroundRes(highlighted: Boolean): Int =
    when (this) {
        SmartisanCalculatorButtonStyle.White -> R.drawable.cal_selector_btn_white
        SmartisanCalculatorButtonStyle.Grey ->
            if (highlighted) R.drawable.cal_selector_btn_grey_focus else R.drawable.cal_selector_btn_grey
        SmartisanCalculatorButtonStyle.Black ->
            if (highlighted) R.drawable.cal_selector_btn_black_focus else R.drawable.cal_selector_btn_black
        SmartisanCalculatorButtonStyle.DigitZero -> R.drawable.selector_digit_0
        SmartisanCalculatorButtonStyle.Equal -> R.drawable.selector_amount
    }
