/**
 * 控件：锤子计算器按键。
 *
 * 对应原版 `com.smartisanos.calculator.HammerButton`（`extends ImageButton implements IHighlight`），
 * 出现在锤子计算器（Calculator 8.1.0）的键盘区（`layout/main.xml` 里全部按键都是它）。
 *
 * 还原要点（照抄原版 `HammerButton.java`、`EventListener#onTouch` 与 `main.xml`）：
 * - 底图用原版按键 selector：白色数字键 `cal_selector_btn_white`、灰色功能键
 *   `cal_selector_btn_grey`、黑色内存键 `cal_selector_btn_black`、
 *   带焦点态的 `cal_selector_btn_grey_focus` / `cal_selector_btn_black_focus`、
 *   双宽数字 0 键 `selector_digit_0`、等号键 `selector_amount`；
 * - **按压位移由原版 selector 自带**：按下位图（`btn_*_p.9.png`）比常态位图整体内缩约 1dp
 *   （3 倍图下左右各缩 3px、底部抬升 7px），因此这里不做任何自绘缩放；
 * - 图标按原版 nine-patch 的内容内边距摆放（左右各约 1.67dp、底部 6dp，来自位图的内边距标记行）；
 * - 高亮角标（原版 `setHighlight()` / `IHighlight`）：把 `focus` 位图画在右上角，
 *   位置为 `(宽度 − 角标宽 − highlight_padding_right, highlight_padding_top)`，
 *   这两个值原版按横竖屏取不同资源（竖屏 30/16，横屏 24/30），这里直接读原版整型资源，
 *   系统会按当前方向自动选值；
 * - 按下时播放点击音效并触发虚拟按键触感（原版 `EventListener.onTouch` 的 `ACTION_DOWN`
 *   分支调用 `showVibrator()` 与 `b.play(0)`）；
 * - 长按连发（原版删除键）：`ACTION_DOWN` 后延迟 500ms 触发一次，之后每 150ms 一次
 *   （原版 `Timer().schedule(task, 500L, 150L)`）。
 */
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import kotlinx.coroutines.delay

/** 长按连发的起始延迟，原版 `Timer().schedule(task, 500L, 150L)` 里的 500ms。 */
private const val RepeatStartDelayMillis = 500L

/** 长按连发的间隔，原版 `Timer().schedule(task, 500L, 150L)` 里的 150ms。 */
private const val RepeatIntervalMillis = 150L

/** 原版按键 nine-patch 的内容内边距（左右各 5px @3x）。 */
private val KeyIconHorizontalInset = 1.67.dp

/** 原版按键 nine-patch 的内容内边距（底部 18px @3x）。 */
private val KeyIconBottomInset = 6.dp

/**
 * 计算器按键的底图样式，对应原版 `layout/main.xml` 里给 `HammerButton` 设的 `android:background`。
 */
enum class SmartisanHammerButtonStyle {
    /** 白色数字键，原版 `cal_selector_btn_white`。 */
    White,

    /** 灰色功能键，原版 `cal_selector_btn_grey`。 */
    Grey,

    /** 黑色内存键，原版 `cal_selector_btn_black`。 */
    Black,

    /** 灰色功能键（带焦点态），原版 `cal_selector_btn_grey_focus`。 */
    GreyFocus,

    /** 黑色内存键（带焦点态），原版 `cal_selector_btn_black_focus`。 */
    BlackFocus,

    /** 双宽数字 0 键，原版 `selector_digit_0`。 */
    DigitZero,

    /** 红色等号键，原版 `selector_amount`。 */
    Equal,
    ;

    /** 对应的原版底图 selector。 */
    @get:DrawableRes
    val backgroundRes: Int
        get() =
            when (this) {
                White -> SmartisanDrawables.CalculatorButtonWhite
                Grey -> SmartisanDrawables.CalculatorButtonGrey
                Black -> SmartisanDrawables.CalculatorButtonBlack
                GreyFocus -> SmartisanDrawables.CalculatorButtonGreyFocus
                BlackFocus -> SmartisanDrawables.CalculatorButtonBlackFocus
                DigitZero -> SmartisanDrawables.CalculatorButtonDigitZero
                Equal -> SmartisanDrawables.CalculatorButtonEqual
            }
}

/**
 * 锤子计算器按键。
 *
 * ```kotlin
 * SmartisanHammerButton(
 *     iconRes = R.drawable.d7,
 *     onClick = { input("7") },
 *     style = SmartisanHammerButtonStyle.White,
 * )
 * ```
 *
 * @param iconRes 按键图标（原版 `android:src`），按原版 `fitCenter` 缩放。
 * @param onClick 点击回调。
 * @param modifier 外部修饰符（原版按键靠 `layout_weight` 等分父容器，尺寸由调用方决定）。
 * @param style 底图样式，对应原版 `android:background`。
 * @param highlighted 是否画高亮角标，对应原版 `setHighlight()` / `cancelHighlight()`。
 * @param onRepeat 长按连发回调（原版删除键），延迟 500ms 后每 150ms 触发一次；null 表示不连发。
 * @param contentDescription 无障碍描述，对应原版 `android:contentDescription`。
 * @param iconPadding 图标内边距，默认取原版 nine-patch 的内容内边距。
 */
@Composable
fun SmartisanHammerButton(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SmartisanHammerButtonStyle = SmartisanHammerButtonStyle.White,
    highlighted: Boolean = false,
    onRepeat: (() -> Unit)? = null,
    contentDescription: String? = null,
    iconPadding: PaddingValues = PaddingValues(start = KeyIconHorizontalInset, end = KeyIconHorizontalInset, bottom = KeyIconBottomInset),
) {
    val interaction = rememberSmartisanInteractionSource()
    val iconPainter = rememberSmartisanDrawablePainter(iconRes)
    val highlightPainter = rememberSmartisanDrawablePainter(SmartisanDrawables.CalculatorKeyHighlight)
    // 原版 setHighlight() 后由 invalidate() 重绘，这里直接用 pressed 作为高亮条件。
    var held by remember { mutableStateOf(false) }
    val haptic = smartisanHaptic()
    val click = smartisanClick(onClick)
    LaunchedEffect(interaction) {
        interaction.interactions.collect { interactionItem ->
            when (interactionItem) {
                is PressInteraction.Press -> held = true
                is PressInteraction.Release, is PressInteraction.Cancel -> held = false
                else -> Unit
            }
        }
    }
    // 原版 EventListener.onTouch：ACTION_DOWN 时震动 + 播放按键音。
    LaunchedEffect(held) {
        if (held) {
            haptic()
        }
    }
    // 原版删除键的连发：500ms 后开始，每 150ms 一次。
    LaunchedEffect(held, onRepeat) {
        val repeat = onRepeat ?: return@LaunchedEffect
        if (!held) return@LaunchedEffect
        delay(RepeatStartDelayMillis)
        while (true) {
            repeat()
            delay(RepeatIntervalMillis)
        }
    }
    val context = LocalContext.current
    val density = LocalDensity.current
    // 原版按横竖屏取不同的角标位置（竖屏 30/16，横屏 24/30），直接读原版整型资源即可自动适配。
    val highlightPaddingRight = remember(context) { context.resources.getInteger(R.integer.highlight_padding_right) }
    val highlightPaddingTop = remember(context) { context.resources.getInteger(R.integer.highlight_padding_top) }
    Box(
        modifier =
            modifier
                .smartisanDrawableBackground(
                    drawableRes = style.backgroundRes,
                    pressed = held,
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = click,
                )
                .drawWithContent {
                    drawContent()
                    if (!highlighted) return@drawWithContent
                    val highlightSize = highlightPainter.smartisanSize()
                    if (highlightSize == null) return@drawWithContent
                    with(density) {
                        translate(
                            left = size.width - highlightSize.width - highlightPaddingRight.dp.toPx(),
                            top = highlightPaddingTop.dp.toPx(),
                        ) {
                            with(highlightPainter) { draw(highlightSize) }
                        }
                    }
                },
    ) {
        Image(
            painter = iconPainter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().padding(iconPadding),
            contentScale = ContentScale.Fit,
        )
    }
}

/** 取 Painter 的固有尺寸；未知（矢量 / 无固有尺寸）时返回 null。 */
private fun Painter.smartisanSize(): Size? {
    val width = intrinsicSize.width
    val height = intrinsicSize.height
    if (!width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return null
    return Size(width, height)
}

