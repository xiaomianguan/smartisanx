package cc.wuersan008.smartisanx.core.anim

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

/**
 * smartisanx 的动画规格。
 *
 * 缓动曲线来自三个项目对原版 `ViewPropertyAnimator` / `AnimatorSet` 默认插值器的还原：
 * 余弦缓入缓出 `cos((t + 1) * π) / 2 + 0.5`。
 */
object SmartisanMotion {
    /** 原版默认缓动：余弦缓入缓出。 */
    val EaseInOut: Easing = Easing { fraction ->
        ((cos((fraction + 1) * PI) / 2.0) + 0.5).toFloat()
    }

    /** 原版标签、编辑态动画时长。 */
    const val DurationShort = 200

    /** 原版页面切换时长。 */
    const val DurationMedium = 300

    /** 原版整页展开收起时长。 */
    const val DurationLong = 400

    /** 按压缩放比例，取自原版标题栏图标 1.33 倍按压放大。 */
    const val PressedScale = 1.33f

    /** 按压回弹弹簧。 */
    val PressSpring: SpringSpec<Float> = spring(
        dampingRatio = 0.55f,
        stiffness = 800f,
        visibilityThreshold = 0.0015f,
    )

    /** 通用弹簧，用于列表让位、队列拖拽。 */
    val SettleSpring: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** 位移动画弹簧。 */
    val OffsetSpring: SpringSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

    /** 缓入缓出的补间动画。 */
    fun <T> easeInOut(durationMillis: Int = DurationMedium): TweenSpec<T> =
        tween(durationMillis = durationMillis, easing = EaseInOut)

    /**
     * 开关滑块落位时长。
     *
     * 原版开关每 350px 需要 88/3 毫秒，这里按比例换算为毫秒数。
     */
    fun switchSettleMillis(position: Float, target: Float): Int =
        (abs(target - position) * (88f / 3f) / 350f * 1000f).roundToInt().coerceAtLeast(1)

    /** 开关投影淡出的缓动。 */
    val SwitchShadowEasing: Easing = Easing { fraction ->
        ((cos((fraction + 1) * PI) / 2.0) + 0.5).toFloat()
    }

    /** 列表拖拽让位的缓动。 */
    val DragEasing: Easing = Easing { fraction ->
        ((cos((fraction + 1) * PI) / 2.0) + 0.5).toFloat()
    }
}

/** 允许使用方整体替换动画规格。 */
@Immutable
class SmartisanMotionSpec(
    /** 主题切换、按压反馈等短动画。 */
    val short: FiniteAnimationSpec<Float> = tween(SmartisanMotion.DurationShort, easing = SmartisanMotion.EaseInOut),
    /** 页面元素进出。 */
    val medium: FiniteAnimationSpec<Float> = tween(SmartisanMotion.DurationMedium, easing = SmartisanMotion.EaseInOut),
    /** 整页展开收起。 */
    val long: FiniteAnimationSpec<Float> = tween(SmartisanMotion.DurationLong, easing = SmartisanMotion.EaseInOut),
    /** 列表让位、拖拽落位。 */
    val spring: SpringSpec<Float> = SmartisanMotion.SettleSpring,
)

/** 当前 [SmartisanMotionSpec]。 */
val LocalSmartisanMotion: ProvidableCompositionLocal<SmartisanMotionSpec> =
    staticCompositionLocalOf { SmartisanMotionSpec() }
