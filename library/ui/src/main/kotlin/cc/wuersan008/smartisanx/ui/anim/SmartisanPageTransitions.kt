package cc.wuersan008.smartisanx.ui.anim

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import kotlin.math.PI
import kotlin.math.cos

/** 原版页面转场的时长，锤子音乐 `SmartisanNavigationDuration`。 */
const val SmartisanNavigationDuration = 300

/** 原版「打开」的缓动，锤子音乐 `Smooth`：`(1 - cos(t * π)) / 2`。 */
val SmartisanNavigationOpenEasing: Easing = Easing { fraction ->
    ((1.0 - cos(fraction * PI)) / 2.0).toFloat()
}

/** 原版「关闭」的缓动，锤子音乐 `Decelerate`：`1 - (1 - t)²`。 */
val SmartisanNavigationCloseEasing: Easing = Easing { fraction ->
    1f - (1f - fraction) * (1f - fraction)
}

/**
 * 原版 XML 转场里的 `decelerate_cubic` 插值器。
 *
 * 用于页面级「上推进入 / 下滑退出」与底部菜单弹窗。
 */
val SmartisanDecelerateCubic: Easing = Easing { fraction ->
    1f - (1f - fraction) * (1f - fraction) * (1f - fraction)
}

/** 原版 `@android:anim/decelerate_interpolator`，用于弹窗的缩放与淡入。 */
val SmartisanDecelerate: Easing = Easing { fraction ->
    1f - (1f - fraction) * (1f - fraction)
}

/** 原版 `@android:anim/accelerate_interpolator`，用于底部菜单的退出。 */
val SmartisanAccelerate: Easing = Easing { fraction ->
    fraction * fraction
}

/**
 * 常规页面转场：新页面从**右侧滑入**，旧页面向左让位。
 *
 * 还原锤子音乐的 `PageStackTransition`：
 *
 * - 旧页面 `translationX = -宽度 * 进度`；
 * - 新页面 `translationX = 宽度 * (1 - 进度)`；
 * - 时长 300ms，打开用 `Smooth`、关闭用 `Decelerate`。
 *
 * 对应「用返回箭头退出」的常规页面。
 *
 * ```kotlin
 * SmartisanPageTransition(
 *     secondary = page != null,
 *     primary = { Home() },
 * ) {
 *     DetailPage()
 * }
 * ```
 */
@Composable
fun SmartisanPageTransition(
    secondary: Boolean,
    modifier: Modifier = Modifier,
    primary: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
) {
    // 关闭动画期间还要继续画旧页面，所以这里自己留一份。
    var retained by remember { mutableStateOf(secondary) }
    val progress = remember { Animatable(if (secondary) 1f else 0f) }
    LaunchedEffect(secondary) {
        if (secondary) {
            retained = true
            progress.animateTo(
                1f,
                tween(SmartisanNavigationDuration, easing = SmartisanNavigationOpenEasing),
            )
        } else {
            progress.animateTo(
                0f,
                tween(SmartisanNavigationDuration, easing = SmartisanNavigationCloseEasing),
            )
            retained = false
        }
    }
    BoxWithConstraints(modifier.clipToBounds()) {
        val width = with(LocalDensity.current) { maxWidth.toPx() }
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationX = -width * progress.value
            },
        ) {
            primary()
        }
        if (retained) {
            Box(
                Modifier.fillMaxSize()
                    .graphicsLayer {
                        translationX = width * (1f - progress.value)
                    }
                    .zIndex(1f),
            ) {
                secondaryContent()
            }
        }
    }
}

/**
 * 弹层页面转场：**从底部滑入**，退出时向下滑走。
 *
 * 还原锤子天气的 `pop_up_in` / `slide_down_out`：
 * 进入 `translateY` 100% → 0、退出 0 → 109%，都是 300ms、`decelerate_cubic`；
 * 底下的页面不动（原版用 `fake_anim`）。
 *
 * 对应「用 × 关闭」的模态页面。
 */
@Composable
fun SmartisanModalPageTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(if (visible) 1f else 0f) }
    LaunchedEffect(visible) {
        progress.animateTo(
            if (visible) 1f else 0f,
            tween(SmartisanNavigationDuration, easing = SmartisanDecelerateCubic),
        )
    }
    BoxWithConstraints(modifier) {
        val height = with(LocalDensity.current) { maxHeight.toPx() }
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                // 退出滑到 109%，与原版 slide_down_out 一致。
                translationY = height * (1f - progress.value) * 1.09f
            },
        ) {
            content()
        }
    }
}
