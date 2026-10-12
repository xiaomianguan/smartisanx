package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIconButton
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorButton
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorDisplay
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorDisplayDefaults

/** 键盘高度，原版 `values-sw360dp/dimens.xml` 的 `controller_height`（432dp 档是 532.8dp）。 */
private val ControllerHeight = 444.dp

/** 键盘左右外边距，原版 `controller_padding_horizontal` = 5.33dp。 */
private val ControllerHorizontalMargin = 5.33.dp

/** 键盘上外边距，原版 `controller_margin_top` = 40dp。 */
private val ControllerTopMargin = 40.dp

/** 键盘下外边距，原版 `controller_margin_bottom` = 18dp。 */
private val ControllerBottomMargin = 18.dp

/** 计算器的一个按键。 */
@Composable
private fun RowScope.CalcKey(
    iconRes: Int,
    style: SmartisanCalculatorButtonStyle,
    description: String,
    highlighted: Boolean = false,
    weight: Float = 1f,
    onClick: () -> Unit = {},
) {
    SmartisanCalculatorButton(
        iconRes = iconRes,
        onClick = onClick,
        modifier = Modifier.weight(weight).fillMaxHeight(),
        style = style,
        highlighted = highlighted,
        contentDescription = description,
    )
}

/**
 * 锤子计算器。
 *
 * 对应原版 `CalculatorSmartisan.apk` 的 `main.xml`，是原版按键矩阵的逐格还原：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 根底色 | `cal_bg`（9-patch） |
 * | 显示区 | `typebox`（9-patch），左右 `typebox_margin_left/right` = 12dp、上 `top_marginTop` = 12dp，内容底部对齐 |
 * | 输入 / 结果 | `DisplayInputView` + `DisplayResultView` → [SmartisanCalculatorDisplay]（点阵输入行 + 段码结果行，尺寸取 `display_input_view_margin_right` 19dp / `display_result_view_margin_right` 16dp / `input_field_bottom` 20dp / `result_field_bottom` 26dp） |
 * | 记忆键行 | `mc` / `madd` / `mminus` / `mr`，黑色按键 `cal_selector_btn_black` |
 * | 功能键行 | `del` / `addminus` / `div` / `multiply`，灰色按键 `cal_selector_btn_grey_focus` |
 * | 数字键盘 | `d7`…`d0` 白色按键；`minus` / `add` 是灰底；`0` 双宽（`selector_digit_0`）、`=` 双高（`selector_amount`） |
 * | 键盘外边距 | `controller_padding_horizontal` = 5.33dp、`controller_margin_top` = 40dp、`controller_margin_bottom` = 18dp |
 * | 键盘高度 | `controller_height` = 444dp（`values-sw360dp`；432dp 档是 532.8dp）—— 原版是**固定高度**，不是权重 |
 *
 * 按键本身是 [SmartisanCalculatorButton]：底图按 [SmartisanCalculatorButtonStyle] 取，
 * 键面是原版位图（`d1`…`amount`），长按连按由 `onRepeat` 支持。
 *
 * 与原版的差别只有一处：原版计算器**没有标题栏**，示例里在左上角加了一个返回按钮，
 * 否则进了这张布局就出不去了。
 */
@Composable
fun CalculatorLayout(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .smartisanDrawableBackground(R.drawable.cal_bg),
    ) {
        // 原版没有标题栏，这里只放一个返回按钮（顶上让出状态栏）。
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 6.dp, top = 6.dp),
        ) {
            SmartisanIconButton(onClick = onBack, size = 44.dp, contentDescription = "返回") {
                SmartisanIcon(res = SmartisanOriginalIcons.Back, contentDescription = null)
            }
        }
        // 显示区：typebox 底图，内容底部对齐（原版 show_view 是 gravity=bottom）。
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    top = SmartisanCalculatorDisplayDefaults.TypeBoxTopMargin,
                    start = SmartisanCalculatorDisplayDefaults.TypeBoxHorizontalMargin,
                    end = SmartisanCalculatorDisplayDefaults.TypeBoxHorizontalMargin,
                )
                .smartisanDrawableBackground(R.drawable.typebox),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // 原版显示区是两块自绘 View（点阵输入行 + 段码结果行），字形全是位图。
            SmartisanCalculatorDisplay(expression = "12×34", result = "408")
        }
        // 键盘：六行等高的按键，最后一行双高（`=` 跨两行、`0` 双宽）。
        // 原版 `controller` 是固定 444dp 高（`values-sw360dp/dimens.xml` 的 `controller_height`），
        // 用权重会把键压扁（数字看着偏上、键面变成扁片）；
        // 上下那 40dp / 18dp 是 **margin**（在 444dp 之外），所以这里用 Spacer 而不是 padding。
        Spacer(Modifier.height(ControllerTopMargin))
        Column(
            Modifier
                .fillMaxWidth()
                .height(ControllerHeight)
                .padding(horizontal = ControllerHorizontalMargin),
        ) {
            // 记忆键（黑底）。
            Row(Modifier.fillMaxWidth().weight(1f)) {
                CalcKey(R.drawable.mc, SmartisanCalculatorButtonStyle.Black, "清除记忆")
                CalcKey(R.drawable.madd, SmartisanCalculatorButtonStyle.Black, "记忆加")
                CalcKey(R.drawable.mminus, SmartisanCalculatorButtonStyle.Black, "记忆减")
                CalcKey(R.drawable.mr, SmartisanCalculatorButtonStyle.Black, "读取记忆")
            }
            // 功能键（灰底）：原版这几格用的是带焦点态的 `cal_selector_btn_grey_focus`。
            Row(Modifier.fillMaxWidth().weight(1f)) {
                CalcKey(R.drawable.del, SmartisanCalculatorButtonStyle.Grey, "退格")
                CalcKey(R.drawable.addminus, SmartisanCalculatorButtonStyle.Grey, "正负号")
                CalcKey(R.drawable.div, SmartisanCalculatorButtonStyle.Grey, "除", highlighted = true)
                CalcKey(R.drawable.multiply, SmartisanCalculatorButtonStyle.Grey, "乘", highlighted = true)
            }
            Row(Modifier.fillMaxWidth().weight(1f)) {
                CalcKey(R.drawable.d7, SmartisanCalculatorButtonStyle.White, "7")
                CalcKey(R.drawable.d8, SmartisanCalculatorButtonStyle.White, "8")
                CalcKey(R.drawable.d9, SmartisanCalculatorButtonStyle.White, "9")
                CalcKey(R.drawable.minus, SmartisanCalculatorButtonStyle.Grey, "减", highlighted = true)
            }
            Row(Modifier.fillMaxWidth().weight(1f)) {
                CalcKey(R.drawable.d4, SmartisanCalculatorButtonStyle.White, "4")
                CalcKey(R.drawable.d5, SmartisanCalculatorButtonStyle.White, "5")
                CalcKey(R.drawable.d6, SmartisanCalculatorButtonStyle.White, "6")
                CalcKey(R.drawable.add, SmartisanCalculatorButtonStyle.Grey, "加", highlighted = true)
            }
            Row(Modifier.fillMaxWidth().weight(2f)) {
                Column(Modifier.weight(3f).fillMaxHeight()) {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        CalcKey(R.drawable.d1, SmartisanCalculatorButtonStyle.White, "1")
                        CalcKey(R.drawable.d2, SmartisanCalculatorButtonStyle.White, "2")
                        CalcKey(R.drawable.d3, SmartisanCalculatorButtonStyle.White, "3")
                    }
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        CalcKey(
                            iconRes = R.drawable.d0,
                            style = SmartisanCalculatorButtonStyle.DigitZero,
                            description = "0",
                            weight = 2f,
                        )
                        CalcKey(R.drawable.dot, SmartisanCalculatorButtonStyle.White, "小数点")
                    }
                }
                CalcKey(R.drawable.amount, SmartisanCalculatorButtonStyle.Equal, "等于")
            }
        }
        Spacer(Modifier.height(ControllerBottomMargin))
    }
}
