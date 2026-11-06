package cc.wuersan008.smartisanx.core.scroll

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.invalidatePlacement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.exp
import kotlin.math.round
import kotlin.math.sign

/**
 * 滚动越界的取值。
 *
 * 原版（坚果 R2 `SMARTISAN/aries/aries:11/RKQ1.201217.002`，SmartisanOS 8.5.3）的越界是
 * **刚性平移**：内容整块被拉出、露出底色，松手后弹回；`AbsListView` 里原厂把拖拽上限
 * 改成了 `mOverscrollMax = (b - t) / 3`。
 *
 * 旧实现照原版 framework 的数字做：拖拽按固定 1/3 阻尼、到上限硬卡住，松手后从静止起
 * 四元缓出。量出来的位移曲线对得上，但手上的感觉是「硬」：一拉就只走三分之一、拉到头
 * 突然拉不动，甩到边界时速度先被丢掉再起一段滑行，衔接处会顿一下。
 *
 * 现在的手感照 miuix（compose-miuix-ui，HyperOS 的做法，比 Material 更接近原版坚果）：
 *
 * * 拖拽走一条**渐硬的橡皮筋曲线**：设手指累计越界 `d`、范围 `R`（容器尺寸），
 *   `x = |d| / R`，位移 `= R·(x - x² + x³/3)`。起步斜率是 1（刚拉时跟手），越拉越沉，
 *   手指走满一个 `R` 时正好停在 `R/3` —— 也就是原版的 `mOverscrollMax`，只是平滑地到达；
 * * 反向拖拽沿同一条曲线收回（按手指累计量倒推），不会跳；
 * * 松手、甩到边界都交给**临界阻尼弹簧**，并把当时的速度带进去：甩到头时内容顺势冲出去
 *   再落回，没有「停一下再滑」的接缝；弹簧周期 0.4 s，速度很大时放慢到 0.55 s；
 * * 弹回途中手指按下，从当前位置接着拖（曲线反解出对应的手指累计量）。
 */
object SmartisanOverscrollDefaults {
    /** 橡皮筋曲线的范围 `R` 占容器尺寸的比例；拖拽越界最多到 `R/3`（原版 `(b - t) / 3`）。 */
    const val RangeFraction: Float = 1f

    /** 回弹弹簧的周期（秒），临界阻尼。 */
    const val SpringPeriod: Float = 0.4f

    /** 速度超过 [FastVelocity] 时用的周期（秒）：冲得远的回得慢一点，免得一闪而过。 */
    const val FastSpringPeriod: Float = 0.55f

    /** 「速度很大」的界限（dp/s）。 */
    const val FastVelocity: Float = 2000f

    /** 甩到边界时剩下的速度带进弹簧前除以这个数（miuix 同值），冲出去的距离才和原版相当。 */
    const val EdgeVelocityDivisor: Float = 1.53333f

    /** 越界中反向甩动：内容的甩动速度除以这个数，先把越界收回来再滚。 */
    const val InwardFlingDivisor: Float = 2.13333f
}

/**
 * 把越界回弹装进 Compose 的滚动体系。
 *
 * 提供到 [androidx.compose.foundation.LocalOverscrollFactory] 之后，
 * 层级里所有滚动组件（`LazyColumn`、`Modifier.verticalScroll`、
 * `Modifier.scrollableArea` ……）都会自动换成这套行为；
 * [SmartisanTheme] 已经默认提供，一般不需要自己传。
 *
 * ```kotlin
 * // 想单独换掉某个滚动区域的越界效果：
 * val effect = rememberOverscrollEffect()
 * LazyColumn(modifier = Modifier.overscroll(effect!!)) { /* ... */ }
 * ```
 *
 * @param rangeFraction 橡皮筋范围占容器尺寸的比例，见 [SmartisanOverscrollDefaults.RangeFraction]。
 * @param springPeriod 回弹弹簧周期（秒），见 [SmartisanOverscrollDefaults.SpringPeriod]。
 * @param fastSpringPeriod 高速时的弹簧周期（秒）。
 * @param fastVelocity 「高速」的界限（dp/s）。
 */
@Stable
data class SmartisanOverscrollFactory(
    private val rangeFraction: Float = SmartisanOverscrollDefaults.RangeFraction,
    private val springPeriod: Float = SmartisanOverscrollDefaults.SpringPeriod,
    private val fastSpringPeriod: Float = SmartisanOverscrollDefaults.FastSpringPeriod,
    private val fastVelocity: Float = SmartisanOverscrollDefaults.FastVelocity,
) : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect =
        SmartisanOverscrollEffect(rangeFraction, springPeriod, fastSpringPeriod, fastVelocity)
}

/**
 * 坚果的滚动越界：刚性平移 + 橡皮筋拖拽 + 带速度的临界阻尼回弹（见 [SmartisanOverscrollDefaults]）。
 *
 * 内容在布局阶段整块平移（不形变，点击区域跟着走）。
 * 实例由 [SmartisanOverscrollFactory] 创建；[androidx.compose.foundation.rememberOverscrollEffect]
 * 会从当前 factory 取一个。
 */
@Stable
class SmartisanOverscrollEffect internal constructor(
    private val rangeFraction: Float = SmartisanOverscrollDefaults.RangeFraction,
    private val springPeriod: Float = SmartisanOverscrollDefaults.SpringPeriod,
    private val fastSpringPeriod: Float = SmartisanOverscrollDefaults.FastSpringPeriod,
    private val fastVelocity: Float = SmartisanOverscrollDefaults.FastVelocity,
) : OverscrollEffect {
    private val x = Axis()
    private val y = Axis()

    /**
     * 当前内容被拉出的位移（px）。
     *
     * 正 y = 内容整体向下平移 = 列表顶部越界（手指往下拖、列表已经在顶部）；
     * 负 y = 底部越界。x 轴同理，供横向列表使用。
     */
    val offset: Offset get() = Offset(x.offset, y.offset)

    /** 每次 [applyToScroll] / [applyToFling] 加一；手势兜底回弹用它判断滚动体系是否还在处理这次手势。 */
    private var scrollGeneration = 0

    /** 挂上的节点：弹簧的协程作用域、密度、重新摆放。 */
    private var attached: OverscrollNode? = null

    override val isInProgress: Boolean
        get() = x.active || y.active

    override val node: DelegatableNode = OverscrollNode()

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        scrollGeneration++
        if (source != NestedScrollSource.UserInput) {
            // 甩动 / 程序滚动：不越界，交给 applyToFling 收尾；弹簧被打断过、内容又能滚了，
            // 就顺着把残留的越界收掉。
            val consumed = performScroll(delta)
            x.unwind(consumed.x)
            y.unwind(consumed.y)
            return consumed
        }
        // 手指接管：停掉弹簧，从当前位置接着拖。
        x.grab()
        y.grab()
        // 反向拖拽先沿曲线把越界收回去。
        val released = Offset(x.release(delta.x), y.release(delta.y))
        val left = delta - released
        val consumed = performScroll(left)
        x.unwind(consumed.x)
        y.unwind(consumed.y)
        // 滚不动的部分全部变成越界（曲线自己负责越拉越沉），返回值吃掉它。
        val remaining = left - consumed
        x.pull(remaining.x)
        y.pull(remaining.y)
        return released + consumed + remaining
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        scrollGeneration++
        // 越界中：朝外的速度全给弹簧（内容不滚）；朝内的分一部分给内容。
        val vx = x.flingFromOverscroll(velocity.x)
        val vy = y.flingFromOverscroll(velocity.y)
        val consumed = performFling(Velocity(vx, vy))
        // 甩到边界：剩下的速度带进弹簧，内容顺势冲出去再落回。
        val left = Velocity(vx, vy) - consumed
        x.settleAfterFling(left.x / SmartisanOverscrollDefaults.EdgeVelocityDivisor)
        y.settleAfterFling(left.y / SmartisanOverscrollDefaults.EdgeVelocityDivisor)
    }

    /** 一根轴的越界状态：手指累计越界量、位移、回弹弹簧。 */
    private inner class Axis {
        /** 当前位移（px），由 [raw] 经曲线算出，或由弹簧推动。 */
        var offset = 0f
            private set(value) {
                if (field == value) return
                field = value
                if (round(value) != placed) {
                    placed = round(value)
                    attached?.invalidatePlacement()
                }
            }

        /** 上次布局用的整数位移：只有变了才重新摆放。 */
        var placed = 0f

        /** 手指累计越界量（px，带符号），夹在 ±range。 */
        private var raw = 0f

        /** 橡皮筋范围 R（px），由节点在测量时按容器尺寸设定。 */
        var range = 0f

        private var spring: Job? = null

        val active get() = abs(offset) >= 0.5f || spring?.isActive == true

        /** `R·(t - t² + t³/3)`，`t = |d|/R`：起步跟手，越拉越沉，`|d| = R` 时到 `R/3`。 */
        private fun curve(d: Float): Float {
            if (range <= 0f) return 0f
            val t = (abs(d) / range).coerceAtMost(1f)
            return sign(d) * range * (t - t * t + t * t * t / 3f)
        }

        /** [curve] 的反函数：位移 → 手指累计量（`1 - t = cbrt(1 - 3·o/R)`）。 */
        private fun inverse(o: Float): Float {
            if (range <= 0f) return 0f
            val u = (abs(o) / range).coerceAtMost(1f / 3f)
            return sign(o) * range * (1f - cbrt(1f - 3f * u))
        }

        fun grab() {
            if (spring?.isActive == true) {
                spring?.cancel()
                raw = inverse(offset)
            }
        }

        /** 与越界反向的 delta 先沿曲线收回；返回吃掉的部分。 */
        fun release(delta: Float): Float {
            if (offset == 0f || delta == 0f || sign(delta) == sign(raw)) return 0f
            if (abs(delta) >= abs(raw)) {
                val eaten = -raw
                reset()
                return eaten
            }
            raw += delta
            offset = curve(raw)
            return delta
        }

        /** 内容滚回来了（甩动穿过边缘、或被打断的残留），越界跟着收。 */
        fun unwind(consumed: Float) {
            if (offset == 0f || consumed == 0f || spring?.isActive == true) return
            if (raw == 0f) raw = inverse(offset)
            if (sign(consumed) != sign(raw)) return
            if (abs(raw) <= abs(consumed)) reset() else { raw -= consumed; offset = curve(raw) }
        }

        fun pull(delta: Float) {
            if (delta == 0f || range <= 0f) return
            raw = (raw + delta).coerceIn(-range, range)
            offset = curve(raw)
        }

        /** 甩动开始时还在越界：返回交给内容的速度。 */
        fun flingFromOverscroll(v: Float): Float {
            if (!active) return v
            settle(v)
            return if (v != 0f && sign(v) == sign(offset)) 0f else v / SmartisanOverscrollDefaults.InwardFlingDivisor
        }

        /** 甩动结束：撞到边界的速度起弹簧；没撞到而越界还在（弹簧已经在跑就不打扰）也收回去。 */
        fun settleAfterFling(v: Float) {
            if (v != 0f || spring?.isActive != true) settle(v)
        }

        /** 从当前位移带着速度 [v] 起一个回到 0 的临界阻尼弹簧。 */
        fun settle(v: Float) {
            if (abs(offset) < 0.5f && v == 0f) {
                if (spring?.isActive != true) reset()
                return
            }
            val n = attached ?: run { reset(); return }
            spring?.cancel()
            val fast = abs(v) > fastVelocity * n.density
            val omega = 2f * PI.toFloat() / (if (fast) fastSpringPeriod else springPeriod)
            val x0 = offset
            spring = n.coroutineScope.launch {
                // 临界阻尼的闭式解 x(t) = (x0 + (v0 + ω·x0)·t)·e^(-ωt)：与帧率无关，按时间取值。
                var start = 0L
                while (true) {
                    val t = withFrameNanos { now ->
                        if (start == 0L) start = now
                        (now - start) / 1_000_000_000f
                    }
                    val e = exp(-omega * t)
                    val pos = (x0 + (v + omega * x0) * t) * e
                    val vel = (v - omega * (v + omega * x0) * t) * e
                    if (t > 0f && abs(pos) < 0.5f && abs(vel) < 60f) break
                    offset = pos
                }
                reset()
            }
        }

        fun reset() {
            raw = 0f
            offset = 0f
        }

        fun detach() {
            spring?.cancel()
            spring = null
            reset()
        }
    }

    /** 布局阶段平移内容 + 手势结束兜底回弹。 */
    private inner class OverscrollNode : DelegatingNode(), LayoutModifierNode {
        var density = 1f

        override fun onAttach() {
            attached = this
        }

        override fun onDetach() {
            attached = null
            x.detach()
            y.detach()
        }

        init {
            delegate(
                SuspendingPointerInputModifierNode {
                    // 正常情况下抬手后滚动体系会调 applyToFling（速度为 0 也调）；
                    // 父级抢走手势等少数情况不会，这里兜底：等两帧没有任何滚动再回弹。
                    while (true) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            waitForUpOrCancellation()
                        }
                        var quiet = 0
                        while (quiet < 2 && isInProgress) {
                            val generation = scrollGeneration
                            withFrameNanos { }
                            quiet = if (generation == scrollGeneration) quiet + 1 else 0
                        }
                        x.settle(0f)
                        y.settle(0f)
                    }
                },
            )
        }

        override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
            this@OverscrollNode.density = density
            val placeable = measurable.measure(constraints)
            x.range = placeable.width * rangeFraction
            y.range = placeable.height * rangeFraction
            return layout(placeable.width, placeable.height) {
                // 原版是刚性平移：整块内容挪出去，不做任何形变。
                placeable.placeWithLayer(0, 0) {
                    translationX = x.placed
                    translationY = y.placed
                }
            }
        }
    }
}
