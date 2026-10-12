package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePadding
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import kotlinx.coroutines.delay

/**
 * 锤子计算器按键。
 *
 * 对应原版 `com.smartisanos.calculator.HammerButton`（`extends ImageButton implements IHighlight`）。
 *
 * 原版按键的质感来自 `android:background` 上的一组 selector，本组件照原样使用：
 *
 * | 样式 | 原版 selector |
 * | --- | --- |
 * | [SmartisanCalculatorButtonStyle.White] | `cal_selector_btn_white` |
 * | [SmartisanCalculatorButtonStyle.Grey] | `cal_selector_btn_grey`（高亮时 `_focus`） |
 * | [SmartisanCalculatorButtonStyle.Black] | `cal_selector_btn_black`（高亮时 `_focus`） |
 * | [SmartisanCalculatorButtonStyle.DigitZero] | `selector_digit_0`（数字 0，双宽） |
 * | [SmartisanCalculatorButtonStyle.Equal] | `selector_amount`（等号，红色双高） |
 *
 * selector 自带按下态，所以不需要再手绘按压反馈。
 *
 * 键面位图（`d1`…`amount`）按 `fitCenter` 画在键里，和原版 `HammerButton extends ImageButton`
 * 的默认 `scaleType` 一致；画之前先吃掉底图 nine-patch 自己声明的内边距（`btn_white_n.9.png`
 * 是左右 6px、上 1px、下 20px），原版的 `ImageButton` 就是这么用背景 padding 的
 * —— 也正因为下方那 20px，原版的数字才是**略高于键面中线**的。
 */
@Composable
fun SmartisanCalculatorButton(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SmartisanCalculatorButtonStyle = SmartisanCalculatorButtonStyle.White,
    highlighted: Boolean = false,
    onRepeat: (() -> Unit)? = null,
    contentDescription: String? = null,
    iconPadding: PaddingValues =
        rememberSmartisanDrawablePadding(style.backgroundRes(highlighted)),
) {
    val interaction = rememberSmartisanInteractionSource()
    val iconPainter = rememberSmartisanDrawablePainter(iconRes)
    val haptic = smartisanHaptic()
    val click = smartisanClick(onClick)
    var held by remember { mutableStateOf(false) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { item ->
            when (item) {
                is PressInteraction.Press -> held = true
                is PressInteraction.Release, is PressInteraction.Cancel -> held = false
                else -> Unit
            }
        }
    }
    LaunchedEffect(held) {
        if (held) haptic()
    }
    // 原版删除键：按下 500ms 后开始连发，之后每 150ms 一次。
    LaunchedEffect(held, onRepeat) {
        if (held && onRepeat != null) {
            delay(RepeatStartDelayMillis)
            while (true) {
                onRepeat()
                delay(RepeatIntervalMillis)
            }
        }
    }
    Box(
        modifier =
            modifier
                .smartisanDrawableBackground(
                    drawableRes = style.backgroundRes(highlighted),
                    pressed = held,
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = click,
                ),
    ) {
        Image(
            painter = iconPainter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().padding(iconPadding),
            contentScale = ContentScale.Fit,
        )
    }
}

/** 长按连发的起始延迟，原版删除键。 */
private const val RepeatStartDelayMillis = 500L

/** 长按连发的间隔，原版删除键。 */
private const val RepeatIntervalMillis = 150L
