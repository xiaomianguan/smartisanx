/**
 * 基础控件：锤子风格开关。
 *
 * 合并了两个复刻项目里的重复实现：
 * - 锤子音乐（Compose）`ui/components/SmartisanSwitch.kt`：`switch_ex_*` 位图开关的几何、
 *   按下后拖动滑块、松开后投影按余弦缓动淡出、以及按「每 350px 用 88/3 毫秒」换算的落位时长；
 * - 锤子时钟（XML + 自定义 View）`custom/SmartisanSwitchView.kt` 与 `custom/SmartisanSwitchExView.kt`：
 *   浅灰轨道 + 描边 + 白色滑块的几何、开启时轨道左端的绿色指示点、松手落位与触感反馈。
 *
 * 原实现依赖 `switch_ex_*.png` / `alarm_repeat_switch_*.png` 位图与 `ValueAnimator`，
 * 这里改为 Compose `Canvas` + `Animatable` 重画：
 * 颜色取自主题（`switchTrack` / `switchTrackStroke` / `switchKnob` / `switchIndicator`），
 * 尺寸取自 `SmartisanDimens`，落位时长取自 `SmartisanMotion.switchSettleMillis`。
 */
package cc.wuersan008.smartisanx.ui.control

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
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
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.abs

/** 开关画布尺寸（含投影），对应原版阴影图 66dp × 48dp。 */
private val SwitchCanvasWidth = SmartisanDimens.SwitchShadowWidth

/** 开关画布高度。 */
private val SwitchCanvasHeight = SmartisanDimens.SwitchShadowHeight

/** 轨道宽度。 */
private val SwitchTrackWidth = SmartisanDimens.SwitchWidth

/** 轨道高度。 */
private val SwitchTrackHeight = SmartisanDimens.SwitchHeight

/** 滑块直径。 */
private val SwitchKnobSize = SmartisanDimens.SwitchKnobSize

/** 滑块行程：轨道宽度减去滑块直径，对应原版的 88/3 dp。 */
private val SwitchKnobTravel = SwitchTrackWidth - SwitchKnobSize

/** 轨道描边宽度，取自锤子时钟 `SmartisanSwitchView` 的 0.6dp。 */
private val SwitchTrackStrokeWidth = 0.6.dp

/** 开启指示点半径，取自锤子时钟 `SmartisanSwitchView` 的 4.4dp。 */
private val SwitchIndicatorRadius = 4.4.dp

/** 开启指示点圆心距轨道左边缘的距离，取自锤子时钟 `SmartisanSwitchView` 的 9dp。 */
private val SwitchIndicatorInset = 9.dp

/** 滑块自带的一层淡投影强度。 */
private const val SwitchKnobShadowBase = 0.10f

/** 按压时叠加的投影强度，松开后淡出到 0。 */
private const val SwitchKnobShadowPressed = 0.26f

/** 禁用时整体透明度，原版为 191/255。 */

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

/** 投影颜色：原版投影是低透明度黑色。 */
private val SwitchKnobShadowColor = Color.Black

/**
 * 开关本体。
 *
 * 绘制顺序与原版一致：轨道填充 → 轨道描边 → 开启指示点 → 滑块投影 → 白色滑块。
 * 手势：按下时投影出现，随后可以左右拖动滑块，松手按
 * [smartisanSwitchDragTarget] 判定目标状态，并用
 * [SmartisanMotion.switchSettleMillis] 计算落位时长。
 *
 * @param exposeSemantics 是否对外暴露开关语义。放进设置行时由行提供语义，这里传 false。
 */
@Composable
private fun SmartisanSwitchContent(
    checked: Boolean,
    enabled: Boolean,
    state: SmartisanSwitchState,
    modifier: Modifier,
    exposeSemantics: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
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
        val trackWidth = SwitchTrackWidth.toPx()
        val trackHeight = SwitchTrackHeight.toPx()
        val trackLeft = (size.width - trackWidth) / 2f
        val trackTop = (size.height - trackHeight) / 2f
        val trackRadius = trackHeight / 2f
        val knobRadius = SwitchKnobSize.toPx() / 2f
        val knobCenter =
            Offset(
                x = trackLeft + knobRadius + SwitchKnobTravel.toPx() * state.position,
                y = size.height / 2f,
            )
        drawRoundRect(
            color = colors.switchTrack,
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackWidth, trackHeight),
            cornerRadius = CornerRadius(trackRadius, trackRadius),
        )
        val strokeWidth = SwitchTrackStrokeWidth.toPx()
        drawRoundRect(
            color = colors.switchTrackStroke,
            topLeft = Offset(trackLeft + strokeWidth / 2f, trackTop + strokeWidth / 2f),
            size = Size(trackWidth - strokeWidth, trackHeight - strokeWidth),
            cornerRadius =
                CornerRadius(trackRadius - strokeWidth / 2f, trackRadius - strokeWidth / 2f),
            style = Stroke(width = strokeWidth),
        )
        // 锤子时钟 SmartisanSwitchView：开启时在轨道左端画一个绿色指示点，
        // 滑块停在左侧时会把它盖住，因此只有开启状态才看得到。
        if (checked || state.position > 0.5f) {
            drawCircle(
                color = colors.switchIndicator,
                radius = SwitchIndicatorRadius.toPx(),
                center = Offset(trackLeft + SwitchIndicatorInset.toPx(), size.height / 2f),
            )
        }
        val shadowStrength = SwitchKnobShadowBase + SwitchKnobShadowPressed * state.shadowAlpha
        val shadowRadius = knobRadius * 1.7f
        val shadowCenter = Offset(knobCenter.x, knobCenter.y + 1.dp.toPx())
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            SwitchKnobShadowColor.copy(alpha = shadowStrength),
                            Color.Transparent,
                        ),
                    center = shadowCenter,
                    radius = shadowRadius,
                ),
            radius = shadowRadius,
            center = shadowCenter,
        )
        drawCircle(color = colors.switchKnob, radius = knobRadius, center = knobCenter)
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
 * @param hapticsEnabled 松手后状态确实发生变化时，是否触发触感反馈。
 */
@Composable
fun SmartisanSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true,
) {
    val state = rememberSmartisanSwitchState(checked, enabled, hapticsEnabled, onCheckedChange)
    SmartisanSwitchContent(
        checked = checked,
        enabled = enabled,
        state = state,
        modifier = modifier,
    )
}

/**
 * 设置页里的开关行：整行可点。
 *
 * 行与开关共用同一个 [SmartisanSwitchState]，并且开关的指针手势会消费事件，
 * 所以「点行」和「点开关」只会触发一次 [onCheckedChange]。
 * 按压时整行切换为 `surfacePressed`，不使用涟漪。
 */
@Composable
fun SmartisanSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val state = rememberSmartisanSwitchState(checked, enabled, true, onCheckedChange)
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
            exposeSemantics = false,
        )
    }
}

