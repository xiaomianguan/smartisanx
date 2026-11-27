package cc.wuersan008.smartisanx.ui.control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 清理应用的 RayLoopView。表盘与指针来自 CleanerSmartisan APK，原图均为 458×458 @xxhdpi。
 * 扫描时指针延迟 200ms 后开始旋转（每圈 3000ms），同时淡入 300ms。
 * [content] 对应原版独立居中的扫描状态层。
 */
@Composable
fun SmartisanCleanerDial(
    scanning: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier.size(458.dp / 3), contentAlignment = Alignment.Center) {
        SmartisanIcon(res = R.drawable.cleaner_scan_dial, contentDescription = null, modifier = Modifier.matchParentSize())
        if (scanning) {
            val rotation by rememberInfiniteTransition(label = "cleaner dial").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    initialStartOffset = StartOffset(200),
                ),
                label = "needle rotation",
            )
            val opacity = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                opacity.animateTo(1f, tween(300, delayMillis = 200, easing = CleanerDecelerate))
            }
            SmartisanIcon(
                res = R.drawable.cleaner_scan_needle,
                contentDescription = null,
                modifier = Modifier.matchParentSize().graphicsLayer {
                    rotationZ = rotation
                    alpha = opacity.value
                },
            )
        }
        content()
    }
}

internal val CleanerDecelerate = Easing { 1f - (1f - it) * (1f - it) }
