package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 锤子计算器「点阵显示屏」的字形索引。
 *
 * 来源：`CalculatorSmartisan`（坚果 R2 上的原版计算器，`com.smartisanos.calculator`）。
 * 原版把显示区做成两块**自绘 View**，每个字符都画成一张小位图，而不是文字：
 *
 * | 原版类 | 画什么 | 字形素材 |
 * | --- | --- | --- |
 * | `DisplayInputView` | 输入行（点阵） | `dot_array_*`，53 张，每张 **38×46 px** |
 * | `DisplayResultView` | 结果行（段码） | `pic*`，18 张，每张 **90×114 px**（`pic_error` 是 450×114） |
 *
 * 两套素材在 `drawable-400dpi` 与 `drawable-xxhdpi` 里**逐像素相同**（都是 38×46 / 90×114），
 * 也就是说同一个字形在不同密度桶里给出的 dp 尺寸不同（400dpi 桶 90px = 36dp、xxhdpi 桶 90px = 30dp），
 * 这是原版的写法，本库照抄两份。
 *
 * 原版字符 → 字形的映射写在 `DisplayInputView.a(char)` / `DisplayResultView.a(char)` 里，
 * 本对象的 [InputGlyphs] / [ResultGlyphs] 就是照它整理出来的（差异见各自的注释）。
 *
 * ```kotlin
 * // 输入行的点阵格子底图与字形
 * SmartisanCalculatorDrawables.InputCell        // dot_array_bg
 * SmartisanCalculatorDrawables.InputGlyphs['5'] // dot_array_5
 *
 * // 结果行的段码格子底图与字形
 * SmartisanCalculatorDrawables.ResultCell        // pic_bg
 * SmartisanCalculatorDrawables.ResultGlyphs['8'] // pic8
 * ```
 */
object SmartisanCalculatorDrawables {
    /** 输入行的点阵格子底图（原版 `dot_array_bg`，38×46px）：整行从右往左平铺。 */
    @DrawableRes val InputCell = R.drawable.dot_array_bg

    /** 结果行的段码格子底图（原版 `pic_bg`，90×114px）：每个字符一格，空位也铺它。 */
    @DrawableRes val ResultCell = R.drawable.pic_bg

    /** 结果行的错误字形（原版 `pic_error`，450×114px = 5 格宽，段码风的 `ERROR`）。 */
    @DrawableRes val Error = R.drawable.pic_error

    /** 千位分隔符（原版 `comma`，9×14px，画在每三位数字之间的空隙里）。 */
    @DrawableRes val Comma = R.drawable.comma

    /** 小数点（原版 `point`，10×10px，贴格子右下角画，不占格）。 */
    @DrawableRes val Point = R.drawable.point

    /** 输入行「减号」（原版 `dot_array_minus`）：原版用它画减号**运算符**，与 [InputNegative] 仅差 72px。 */
    @DrawableRes val InputMinus = R.drawable.dot_array_minus

    /** 输入行「负号」（原版 `dot_array_negative`）：原版 `a(char)` 把字符串里的 `-` 画成它。 */
    @DrawableRes val InputNegative = R.drawable.dot_array_negative

    /** 原版 `more_digit`（37×47px）：位数超长时画 `···`，只用在剪贴板导出图里。 */
    @DrawableRes val MoreDigit = R.drawable.more_digit

    private val InputDigits =
        intArrayOf(
            R.drawable.dot_array_0,
            R.drawable.dot_array_1,
            R.drawable.dot_array_2,
            R.drawable.dot_array_3,
            R.drawable.dot_array_4,
            R.drawable.dot_array_5,
            R.drawable.dot_array_6,
            R.drawable.dot_array_7,
            R.drawable.dot_array_8,
            R.drawable.dot_array_9,
        )

    private val ResultDigits =
        intArrayOf(
            R.drawable.pic0,
            R.drawable.pic1,
            R.drawable.pic2,
            R.drawable.pic3,
            R.drawable.pic4,
            R.drawable.pic5,
            R.drawable.pic6,
            R.drawable.pic7,
            R.drawable.pic8,
            R.drawable.pic9,
        )

    private val Superscripts =
        intArrayOf(
            R.drawable.dot_array_power_0,
            R.drawable.dot_array_power_1,
            R.drawable.dot_array_power_2,
            R.drawable.dot_array_power_3,
            R.drawable.dot_array_power_4,
            R.drawable.dot_array_power_5,
            R.drawable.dot_array_power_6,
            R.drawable.dot_array_power_7,
            R.drawable.dot_array_power_8,
            R.drawable.dot_array_power_9,
        )

    /**
     * 输入行（点阵）的字符 → 字形表。
     *
     * 与原版的两点差异（都只是**多做**一点，不影响原版会出现的字符）：
     * - 大小写合并：原版只认小写字母，大写会落到默认分支画成 `0`；
     * - 多认几个 ASCII 写法：`*` → `×`、`/` → `÷`、`^` → 幂、`%` → 取模。
     */
    val InputGlyphs: Map<Char, Int> =
        buildMap {
            InputDigits.forEachIndexed { digit, res -> put('0' + digit, res) }
            put('.', R.drawable.dot_array_dot)
            put('+', R.drawable.dot_array_plus)
            put('-', R.drawable.dot_array_negative)
            put('×', R.drawable.dot_array_multiply)
            put('*', R.drawable.dot_array_multiply)
            put('÷', R.drawable.dot_array_div)
            put('/', R.drawable.dot_array_div)
            put('=', R.drawable.dot_array_equals)
            put('(', R.drawable.dot_array_left_parent)
            put(')', R.drawable.dot_array_right_parent)
            put('%', R.drawable.dot_array_mod)
            put('!', R.drawable.dot_array_frac)
            put('√', R.drawable.dot_array_sqrt)
            put('π', R.drawable.dot_array_pi)
            put('^', R.drawable.dot_array_power)
            put('#', R.drawable.dot_array_pound)
            put('e', R.drawable.dot_array_e)
            put('E', R.drawable.dot_array_e)
            put('a', R.drawable.dot_array_a)
            put('A', R.drawable.dot_array_a)
            put('r', R.drawable.dot_array_r)
            put('R', R.drawable.dot_array_r)
            put('x', R.drawable.dot_array_x_small)
            put('X', R.drawable.dot_array_x_uper)
            put('s', R.drawable.dot_array_s_small)
            put('i', R.drawable.dot_array_i_small)
            put('n', R.drawable.dot_array_n_small)
            put('c', R.drawable.dot_array_c_small)
            put('o', R.drawable.dot_array_o_small)
            put('t', R.drawable.dot_array_t_small)
            put('l', R.drawable.dot_array_l_small)
            put('g', R.drawable.dot_array_g_small)
            put('h', R.drawable.dot_array_h_small)
            put('⁻', R.drawable.dot_array_power_minus_1)
            Superscripts.forEachIndexed { digit, res -> put('⁰' + digit, res) }
        }

    /**
     * 结果行（段码）的字符 → 字形表。
     *
     * 原版 `DisplayResultView.a(char)` 只认 `0-9 . , + - a e n s`（`ans` / `NaN` 一类用得上），
     * 其余一律落到默认分支画成 `pic0`；本表把大小写合并，并且**不画**表外的字符（留空格子）。
     */
    val ResultGlyphs: Map<Char, Int> =
        buildMap {
            ResultDigits.forEachIndexed { digit, res -> put('0' + digit, res) }
            put('.', R.drawable.point)
            put(',', R.drawable.comma)
            put('+', R.drawable.pic_add)
            put('-', R.drawable.pic_minus)
            put('e', R.drawable.pic_e)
            put('E', R.drawable.pic_e)
            put('a', R.drawable.pic_a)
            put('A', R.drawable.pic_a)
            put('n', R.drawable.pic_n)
            put('N', R.drawable.pic_n)
            put('s', R.drawable.pic_s)
            put('S', R.drawable.pic_s)
        }
}
