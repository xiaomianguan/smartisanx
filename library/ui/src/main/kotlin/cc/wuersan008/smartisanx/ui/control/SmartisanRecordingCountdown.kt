package cc.wuersan008.smartisanx.ui.control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import kotlin.math.cos

/** 录屏应用的三秒倒计时。进入组合即播放，移除组件即可取消。 */
@Composable
fun SmartisanRecordingCountdown(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elapsed = remember { Animatable(0f) }
    val currentFinished by rememberUpdatedState(onFinished)
    val digit by remember { derivedStateOf { (3 - (elapsed.value / 1000f).toInt()).coerceAtLeast(0) } }
    LaunchedEffect(Unit) {
        elapsed.animateTo(3000f, tween(3000, easing = LinearEasing))
        currentFinished()
    }
    Box(
        modifier
            .size(width = 220.dp, height = 296.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                if (digit > 0) contentDescription = digit.toString()
            },
        contentAlignment = Alignment.Center,
    ) {
        // 原版用两个 ImageView 交替绘制，第二个 View 始终盖在第一个之上。
        CountdownDigit(R.drawable.recording_countdown_3, start = 0f, elapsed = { elapsed.value })
        CountdownDigit(R.drawable.recording_countdown_1, start = 2000f, elapsed = { elapsed.value })
        CountdownDigit(R.drawable.recording_countdown_2, start = 1000f, elapsed = { elapsed.value })
    }
}

@Composable
private fun CountdownDigit(res: Int, start: Float, elapsed: () -> Float) {
    SmartisanIcon(
        res = res,
        contentDescription = null,
        modifier = Modifier.size(width = 122.dp, height = 164.dp).graphicsLayer {
            val time = elapsed() - start
            val entry = (time / 1000f).coerceIn(0f, 1f)
            val exit = ((time - 1000f) / 300f).coerceIn(0f, 1f)
            // AnimationSet 会把两段缩放相乘，并在开始和结束处钳制。
            val enterScale = 0.5f + 0.7f * countdownInterpolation(entry)
            val exitScale = 1.2f + 0.3f * (1f - (1f - exit) * (1f - exit))
            scaleX = enterScale * exitScale
            scaleY = scaleX
            alpha = if (time < 0f || time >= 1300f || elapsed() >= 3000f) 0f else {
                accelerateDecelerate((time / 300f).coerceIn(0f, 1f)) * (1f - accelerateDecelerate(exit))
            }
        },
    )
}

private fun countdownInterpolation(fraction: Float): Float =
    if (fraction <= 0.3f) 3.34686f * fraction - 10.741f * fraction * fraction * fraction
    else 0.5918f + fraction * 0.4082f

private fun accelerateDecelerate(fraction: Float): Float =
    (0.5 - 0.5 * cos(Math.PI * fraction)).toFloat()
