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
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
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
    iconPadding: PaddingValues = PaddingValues(horizontal = KeyIconHorizontalInset),
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

/** 按键图标左右内缩，与原版 nine-patch 的内容区一致。 */
private val KeyIconHorizontalInset = 6.dp

/** 长按连发的起始延迟，原版删除键。 */
private const val RepeatStartDelayMillis = 500L

/** 长按连发的间隔，原版删除键。 */
private const val RepeatIntervalMillis = 150L
