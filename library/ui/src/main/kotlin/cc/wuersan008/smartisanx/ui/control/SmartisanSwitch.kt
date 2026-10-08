/**
 * 基础控件：锤子风格开关。
 *
 * 合并了两个复刻项目里的重复实现，并直接使用它们还原的原版位图：
 * - 锤子音乐（Compose）`ui/components/SmartisanSwitch.kt`：`switch_ex_*` 位图开关的叠放顺序、
 *   按下后拖动滑块、松开后投影按余弦缓动淡出、以及按「每 350px 用 88/3 毫秒」换算的落位时长；
 * - 锤子时钟（XML + 自定义 View）`custom/SmartisanSwitchExView.kt`：闹钟重复日开关用的
 *   `alarm_repeat_switch_*` 位图、按下出现投影、松手落位与触感反馈。
 *
 * 绘制顺序与原版完全一致：遮罩 → 轨道底色（用 `SrcIn` 裁进遮罩形状）→ 外框 → 按下外框（投影层）→
 * 滑块。六张位图由 [SmartisanSwitchStyle] 选择，也可以用 `@DrawableRes` 参数逐张替换。
 * 禁用时整体降到 191/255（原版透明度），落位时长取自 [SmartisanMotion.switchSettleMillis]。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanTimerDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.abs
import kotlin.math.roundToInt

/** 外框与遮罩位图的宽度：原版 198px @3x = 66dp。 */
private val SwitchBitmapWidth = SmartisanDimens.SwitchShadowWidth

/** 外框与遮罩位图的高度：原版 144px @3x = 48dp。 */
private val SwitchBitmapHeight = SmartisanDimens.SwitchShadowHeight

/** 滑块行程：原版滑动层比外框宽 88/3 dp，也就是滑块可以移动的距离。 */
private val SwitchKnobTravel = (88f / 3f).dp

/** 滑动层（轨道底色与滑块位图）的宽度：原版 286px @3x。 */
private val SwitchMovingWidth = SwitchBitmapWidth + SwitchKnobTravel

/** 位图在画布上下各留 2dp，与原版 66dp × 52dp 的画布一致。 */
private val SwitchCanvasInset = 2.dp

/** 画布宽度，与位图同宽。 */
private val SwitchCanvasWidth = SwitchBitmapWidth

/** 画布高度：48dp 的位图加上下各 2dp。 */
private val SwitchCanvasHeight = SwitchBitmapHeight + SwitchCanvasInset * 2

/**
 * 开关位图风格。
 *
 * 两种风格共用同一套外框 / 遮罩 / 滑块素材（原版即如此），只有轨道底色不同。
 */
enum class SmartisanSwitchStyle {
    /** 音乐：原版 `switch_ex_*` 位图，浅灰轨道。 */
    Music,

    /** 时钟闹钟重复日：原版 `alarm_repeat_switch_*` 位图，绿色轨道。 */
    Repeat,
}

/**
 * 一套开关位图。
 *
 * 六张图与 [SmartisanSwitchStyle] 一一对应；调用方可以用 [SmartisanSwitch] 的 `@DrawableRes`
 * 参数逐张替换，用于换皮或适配别的原版素材。
 */
private class SmartisanSwitchBitmaps(
    @DrawableRes val bottom: Int,
    @DrawableRes val mask: Int,
    @DrawableRes val frame: Int,
    @DrawableRes val framePressed: Int,
    @DrawableRes val knob: Int,
    @DrawableRes val knobPressed: Int,
)

/** 音乐开关的六张位图：轨道底色、遮罩、外框、按下外框、滑块、按下滑块。 */
private val MusicSwitchBitmaps =
    SmartisanSwitchBitmaps(
        bottom = SmartisanDrawables.SwitchBottom,
        mask = SmartisanDrawables.SwitchMask,
        frame = SmartisanDrawables.SwitchFrame,
        framePressed = SmartisanDrawables.SwitchFramePressed,
        knob = SmartisanDrawables.SwitchKnob,
        knobPressed = SmartisanDrawables.SwitchKnobPressed,
    )

/** 时钟闹钟重复日开关的六张位图。 */
private val RepeatSwitchBitmaps =
    SmartisanSwitchBitmaps(
        bottom = SmartisanTimerDrawables.RepeatSwitchBottomGreen,
        mask = SmartisanTimerDrawables.RepeatSwitchMask,
        frame = SmartisanTimerDrawables.RepeatSwitchFrame,
        framePressed = SmartisanTimerDrawables.RepeatSwitchFramePressed,
        knob = SmartisanTimerDrawables.RepeatSwitchKnob,
        knobPressed = SmartisanTimerDrawables.RepeatSwitchKnobPressed,
    )

/** 取某个风格对应的默认位图。 */
private fun SmartisanSwitchStyle.defaultBitmaps(): SmartisanSwitchBitmaps =
    when (this) {
        SmartisanSwitchStyle.Music -> MusicSwitchBitmaps
        SmartisanSwitchStyle.Repeat -> RepeatSwitchBitmaps
    }

/**
 * 开关的状态机。
 *
 * 开关本体与它所在的设置行共用同一个实例，因此「整行点击」与「点开关」只会产生一次回调
 * （与锤子音乐 `SmartisanSwitchState` 的做法一致）。
 */
@Stable
internal class SmartisanSwitchState(
    checked: Boolean,
    private val scope: CoroutineScope,
    private val currentChecked: () -> Boolean,
    private val currentEnabled: () -> Boolean,
    private val onCheckedChange: (Boolean) -> Unit,
    private val haptic: () -> Unit,
) {
    /** 滑块位置，0 为关、1 为开。 */
    var position by mutableFloatStateOf(if (checked) 1f else 0f)
        private set

    /** 按压投影强度：按下为 1，松开后按余弦缓动淡出到 0。 */
    var shadowAlpha by mutableFloatStateOf(0f)
        private set

    /** 是否处于按压态。 */
    var pressed by mutableStateOf(false)
        private set

    private var settling = false
    private var disposed = false
    private var pendingTarget: Boolean? = null
    private var generation = 0
    private var settleJob: Job? = null
    private var callbackJob: Job? = null
    private var shadowJob: Job? = null

    /** 开始一次按压：投影立刻出现。禁用或正在落位时返回 false，忽略这次手势。 */
    fun begin(): Boolean {
        if (disposed || settling || !currentEnabled()) return false
        shadowJob?.cancel()
        shadowAlpha = 1f
        pressed = true
        return true
    }

    /** 按下后拖动滑块，[fraction] 为 0..1 的位置。 */
    fun dragTo(fraction: Float) {
        position = fraction.coerceIn(0f, 1f)
    }

    /** 供无障碍点击使用：直接切换状态。 */
    fun toggle() {
        if (disposed || settling || pressed || !currentEnabled()) return
        finish(!(pendingTarget ?: currentChecked()), fadeShadow = false)
    }

    /**
     * 松手落位。
     *
     * @param target 落位后的开关状态。
     * @param fadeShadow 是否播放投影淡出；直接点击没有按下投影，因此不播放。
     */
    fun finish(target: Boolean, fadeShadow: Boolean = true) {
        pressed = false
        if (disposed || !currentEnabled()) return
        val current = ++generation
        pendingTarget = target
        if (fadeShadow) {
            shadowJob?.cancel()
            shadowAlpha = 1f
            shadowJob =
                scope.launch {
                    Animatable(1f).animateTo(
                        targetValue = 0f,
                        animationSpec =
                            tween(
                                durationMillis = SwitchShadowFadeMillis,
                                easing = SmartisanMotion.SwitchShadowEasing,
                            ),
                    ) {
                        shadowAlpha = value
                    }
                }
        }
        val targetPosition = if (target) 1f else 0f
        val duration = SmartisanMotion.switchSettleMillis(position, targetPosition)
        settleJob?.cancel()
        settling = true
        settleJob =
            scope.launch {
                try {
                    Animatable(position).animateTo(
                        targetValue = targetPosition,
                        animationSpec = tween(durationMillis = duration, easing = LinearEasing),
                    ) {
                        position = value
                    }
                } finally {
                    if (generation == current) {
                        settling = false
                        if (pendingTarget == null) synchronize(currentChecked())
                    }
                }
            }
        callbackJob?.cancel()
        callbackJob =
            scope.launch {
                try {
                    // 与原版一致：只有状态真的变化时才回调并触发触感反馈。
                    if (target != currentChecked()) {
                        onCheckedChange(target)
                        haptic()
                    }
                    // 等一帧，让使用方先发布新值，再决定是否收回临时视觉状态。
                    withFrameNanos { }
                } finally {
                    if (generation == current) {
                        pendingTarget = null
                        synchronize(currentChecked())
                    }
                }
            }
    }

    /** 使用方发布新值后同步滑块位置；按压或落位过程中不打断动画。 */
    fun synchronize(checked: Boolean) {
        if (!pressed && !settling && pendingTarget == null) position = if (checked) 1f else 0f
    }

    /** 组合离开时取消所有动画。 */
    fun dispose() {
        generation++
        disposed = true
        settleJob?.cancel()
        callbackJob?.cancel()
        shadowJob?.cancel()
    }
}

/**
 * 记住一个 [SmartisanSwitchState]。
 *
 * 开关与设置行共用同一个实例，保证一次意图只产生一次回调。
 */
@Composable
internal fun rememberSmartisanSwitchState(
    checked: Boolean,
    enabled: Boolean,
    hapticsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): SmartisanSwitchState {
    val scope = rememberCoroutineScope()
    val latestChecked = rememberUpdatedState(checked)
    val latestEnabled = rememberUpdatedState(enabled)
    val latestHaptics = rememberUpdatedState(hapticsEnabled)
    val callback = rememberUpdatedState(onCheckedChange)
    val haptic = smartisanHaptic()
    val state =
        remember(scope, haptic) {
            SmartisanSwitchState(
                checked = checked,
                scope = scope,
                currentChecked = { latestChecked.value },
                currentEnabled = { latestEnabled.value },
                onCheckedChange = { callback.value(it) },
                haptic = { if (latestHaptics.value) haptic() },
            )
        }
    LaunchedEffect(checked) { state.synchronize(checked) }
    DisposableEffect(state) { onDispose { state.dispose() } }
    return state
}

/**
 * 松手时的目标状态（纯函数，便于单元测试）。
 *
 * 还原锤子时钟 `SmartisanSwitchExView` 的判定：手指移动超过触摸阈值、或按住超过 300ms，
 * 都按当前滑块位置取整（越过一半即切换）；否则视为一次点击，直接取反。
 */
internal fun smartisanSwitchDragTarget(
    checked: Boolean,
    position: Float,
    delta: Offset,
    elapsedMillis: Long,
    touchSlop: Float,
): Boolean =
    if (abs(delta.x) >= touchSlop || abs(delta.y) >= touchSlop || elapsedMillis >= 300L) {
        position > 0.5f
    } else {
        !checked
    }

private const val SwitchDisabledAlpha = 0.75f

/** 投影淡出时长，与原版 `PRESSED_FADE_OUT_MS` 同量级。 */
private const val SwitchShadowFadeMillis = 200

/**
 * 开关本体。
 *
 * 绘制顺序与原版一致：遮罩 → 轨道底色（用 `SrcIn` 裁进遮罩形状）→ 外框 → 按下外框（投影层）→
 * 滑块。手势：按下时投影出现，随后可以左右拖动滑块，松手按 [smartisanSwitchDragTarget] 判定
 * 目标状态，并用 [SmartisanMotion.switchSettleMillis] 计算落位时长。
 *
 * @param bitmaps 这一颗开关使用的六张位图。
 * @param exposeSemantics 是否对外暴露开关语义。放进设置行时由行提供语义，这里传 false。
 */
@Composable
private fun SmartisanSwitchContent(
    checked: Boolean,
    enabled: Boolean,
    state: SmartisanSwitchState,
    modifier: Modifier,
    bitmaps: SmartisanSwitchBitmaps,
    exposeSemantics: Boolean = true,
) {
    val currentChecked by rememberUpdatedState(checked)
    val semanticsModifier =
        if (exposeSemantics) {
            Modifier.semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
                onClick {
                    if (enabled) state.toggle()
                    enabled
                }
            }
        } else {
            Modifier
        }
    val mask = ImageBitmap.imageResource(bitmaps.mask)
    val bottom = ImageBitmap.imageResource(bitmaps.bottom)
    val frame = ImageBitmap.imageResource(bitmaps.frame)
    val framePressed = ImageBitmap.imageResource(bitmaps.framePressed)
    val knob = ImageBitmap.imageResource(bitmaps.knob)
    val knobPressed = ImageBitmap.imageResource(bitmaps.knobPressed)
    Canvas(
        modifier =
            modifier
                .size(SwitchCanvasWidth, SwitchCanvasHeight)
                .clipToBounds()
                .graphicsLayer { alpha = if (enabled) 1f else SwitchDisabledAlpha }
                .then(semanticsModifier)
                .pointerInput(enabled, state) {
                    if (!enabled) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        if (!state.begin()) return@awaitEachGesture
                        val initial = if (currentChecked) 1f else 0f
                        val travel = SwitchKnobTravel.toPx()
                        var last = down
                        try {
                            do {
                                val event = awaitPointerEvent()
                                last = event.changes.firstOrNull { it.id == down.id } ?: break
                                state.dragTo(initial + (last.position.x - down.position.x) / travel)
                                last.consume()
                            } while (last.pressed)
                        } finally {
                            // 原版把 ACTION_CANCEL 也当作松手处理，包括越过一半的切换。
                            state.finish(
                                smartisanSwitchDragTarget(
                                    checked = currentChecked,
                                    position = state.position,
                                    delta = last.position - down.position,
                                    elapsedMillis = last.uptimeMillis - down.uptimeMillis,
                                    touchSlop = viewConfiguration.touchSlop,
                                ),
                            )
                        }
                    }
                },
    ) {
        // 位图按整数像素对齐，与原版一致。
        val bitmapWidth = SwitchBitmapWidth.roundToPx()
        val bitmapHeight = SwitchBitmapHeight.roundToPx()
        val movingWidth = SwitchMovingWidth.roundToPx()
        val top = SwitchCanvasInset.roundToPx()
        // 开启时滑动层贴左（滑块停在轨道右端），关闭时整体左移一个行程。
        val shift = (-SwitchKnobTravel.toPx() * (1f - state.position)).roundToInt()
        // 原版把这几张图叠在同一个离屏层里，轨道底色用 SrcIn 裁进遮罩形状。
        drawIntoCanvas { canvas -> canvas.saveLayer(Rect(Offset.Zero, size), Paint()) }
        drawImage(mask, dstOffset = IntOffset(0, top), dstSize = IntSize(bitmapWidth, bitmapHeight))
        drawImage(
            bottom,
            dstOffset = IntOffset(shift, top),
            dstSize = IntSize(movingWidth, bitmapHeight),
            blendMode = BlendMode.SrcIn,
        )
        drawImage(frame, dstOffset = IntOffset(0, top), dstSize = IntSize(bitmapWidth, bitmapHeight))
        // 按下时叠上投影层，松手后按余弦缓动淡出。
        if (state.shadowAlpha > 0f) {
            drawImage(
                framePressed,
                dstOffset = IntOffset(0, top),
                dstSize = IntSize(bitmapWidth, bitmapHeight),
                alpha = state.shadowAlpha,
            )
        }
        // 投影满值时换用按下滑块，随后随投影一起淡出（与原版判断一致）。
        drawImage(
            if (state.shadowAlpha >= 1f) knobPressed else knob,
            dstOffset = IntOffset(shift, top),
            dstSize = IntSize(movingWidth, bitmapHeight),
        )
        drawIntoCanvas { canvas -> canvas.restore() }
    }
}

/**
 * 锤子风格开关。
 *
 * ```kotlin
 * var on by remember { mutableStateOf(true) }
 * SmartisanSwitch(checked = on, onCheckedChange = { on = it })
 * ```
 *
 * 位图来自原版：默认 [SmartisanSwitchStyle.Music] 用音乐 `switch_ex_*`，
 * [SmartisanSwitchStyle.Repeat] 用时钟闹钟重复日的 `alarm_repeat_switch_*`。
 *
 * ```kotlin
 * SmartisanSwitch(
 *     checked = on,
 *     onCheckedChange = { on = it },
 *     style = SmartisanSwitchStyle.Repeat,
 * )
 * ```
 *
 * @param hapticsEnabled 松手后状态确实发生变化时，是否触发触感反馈。
 * @param style 位图风格；两种风格共用同一套外框 / 遮罩 / 滑块素材，只有轨道底色不同。
 * @param bottomRes 轨道底色位图，默认取 [style] 对应的原版素材。
 * @param maskRes 遮罩位图，决定轨道底色的形状。
 * @param frameRes 外框位图。
 * @param framePressedRes 按下时的外框位图，自带投影。
 * @param knobRes 滑块位图，自带一层淡投影。
 * @param knobPressedRes 按下时的滑块位图，投影更重。
 */
@Composable
fun SmartisanSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    style: SmartisanSwitchStyle = SmartisanSwitchStyle.Music,
    @DrawableRes bottomRes: Int = style.defaultBitmaps().bottom,
    @DrawableRes maskRes: Int = style.defaultBitmaps().mask,
    @DrawableRes frameRes: Int = style.defaultBitmaps().frame,
    @DrawableRes framePressedRes: Int = style.defaultBitmaps().framePressed,
    @DrawableRes knobRes: Int = style.defaultBitmaps().knob,
    @DrawableRes knobPressedRes: Int = style.defaultBitmaps().knobPressed,
) {
    val state = rememberSmartisanSwitchState(checked, enabled, hapticsEnabled, onCheckedChange)
    val bitmaps =
        remember(bottomRes, maskRes, frameRes, framePressedRes, knobRes, knobPressedRes) {
            SmartisanSwitchBitmaps(
                bottom = bottomRes,
                mask = maskRes,
                frame = frameRes,
                framePressed = framePressedRes,
                knob = knobRes,
                knobPressed = knobPressedRes,
            )
        }
    SmartisanSwitchContent(
        checked = checked,
        enabled = enabled,
        state = state,
        modifier = modifier,
        bitmaps = bitmaps,
    )
}

/**
 * 设置页里的开关行：整行可点。
 *
 * 行与开关共用同一个 [SmartisanSwitchState]，并且开关的指针手势会消费事件，
 * 所以「点行」和「点开关」只会触发一次 [onCheckedChange]。
 * 按压时整行切换为 `surfacePressed`，不使用涟漪。
 *
 * @param style 开关的位图风格，默认与 [SmartisanSwitch] 相同。
 */
@Composable
fun SmartisanSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
    style: SmartisanSwitchStyle = SmartisanSwitchStyle.Music,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val state = rememberSmartisanSwitchState(checked, enabled, true, onCheckedChange)
    val bitmaps = remember(style) { style.defaultBitmaps() }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SmartisanDimens.ListItemHeight)
                .background(if (pressed && enabled) colors.surfacePressed else Color.Transparent)
                .toggleable(
                    value = checked,
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = { state.toggle() },
                )
                .padding(
                    start = SmartisanDimens.RowContentStart,
                    end = SmartisanDimens.ListItemHorizontalMargin,
                    top = 6.dp,
                    bottom = 6.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            SmartisanText(
                text = text,
                style = typography.listItemPrimary,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
                maxLines = 2,
            )
            if (summary != null) {
                SmartisanText(
                    text = summary,
                    modifier = Modifier.padding(top = 2.dp),
                    style = typography.listItemSecondary,
                    color = if (enabled) colors.textTertiary else colors.textDisabled,
                    maxLines = 2,
                )
            }
        }
        SmartisanSwitchContent(
            checked = checked,
            enabled = enabled,
            state = state,
            modifier = Modifier,
            bitmaps = bitmaps,
            exposeSemantics = false,
        )
    }
}

