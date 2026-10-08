package top.smartisanx.core.interaction

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.ViewConfiguration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 收集按压状态，并保证「同一帧内按下又抬起」的快速点击依然可见。
 *
 * 三个复刻项目各自实现了一份完全相同的逻辑（`collectSmartisanPressedAsState` 与
 * `collectWeatherPressedAsState`），这里合并为唯一实现。
 *
 * 平台按压反馈时长取自 [ViewConfiguration.getPressedStateDuration]，与原版 View 一致；
 * 手势取消时立即清除，不做延时。
 */
@Composable
fun InteractionSource.collectSmartisanPressedAsState(): State<Boolean> {
    val pressed = remember(this) { mutableStateOf(false) }
    LaunchedEffect(this) {
        val active = mutableSetOf<PressInteraction.Press>()
        var release: Job? = null
        interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    release?.cancel()
                    active.add(interaction)
                    pressed.value = true
                }

                is PressInteraction.Release -> {
                    if (active.remove(interaction.press) && active.isEmpty()) {
                        release = launch {
                            // 先等一帧，避免同帧内的按下与抬起完全跳过按压态绘制。
                            val start = withFrameNanos { it }
                            val duration = ViewConfiguration.getPressedStateDuration() * 1_000_000L
                            while (withFrameNanos { it } - start < duration) {
                                // 保持按压态可见，直到平台按压时长结束。
                            }
                            pressed.value = false
                        }
                    }
                }

                is PressInteraction.Cancel -> {
                    if (active.remove(interaction.press) && active.isEmpty()) {
                        release?.cancel()
                        pressed.value = false
                    }
                }

                else -> Unit
            }
        }
    }
    return pressed
}

/**
 * 保留 `View.performClick` 的点击音效，并遵循系统「触摸提示音」开关。
 */
@Composable
fun smartisanClick(onClick: () -> Unit): () -> Unit {
    val host = LocalView.current
    return remember(host, onClick) {
        {
            host.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        }
    }
}

/** 触发一次与原版一致的虚拟按键触感反馈。 */
/** 触发一次与原版一致的虚拟按键触感反馈（`HapticFeedbackConstants.VIRTUAL_KEY`）。 */
@Composable
fun smartisanHaptic(): () -> Unit {
    val host = LocalView.current
    return remember(host) {
        { host.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    }
}


/**
 * 原版列表、按钮不使用涟漪，只切换按压态资源。
 *
 * 该修饰符只负责点击与语义，按压态由调用方通过
 * [collectSmartisanPressedAsState] 自行绘制。
 */
@Composable
fun Modifier.smartisanClickable(
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val click = smartisanClick(onClick)
    return this.clickable(
        interactionSource = source,
        indication = null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = click,
    )
}

/** 记住一个 [MutableInteractionSource]，用于配合按压态绘制。 */
@Composable
fun rememberSmartisanInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }

/** 与原版一致：按压时切换文字颜色。 */
@Composable
fun smartisanPressedTextColor(
    normal: androidx.compose.ui.graphics.Color,
    highlight: androidx.compose.ui.graphics.Color,
    pressed: Boolean,
): androidx.compose.ui.graphics.Color = if (pressed) highlight else normal
