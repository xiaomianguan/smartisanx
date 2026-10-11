package cc.wuersan008.smartisanx.core.scroll

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.OverscrollFactory
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 原版滚动越界的实测参数。
 *
 * 数字来自 Smartisan R2（`SMARTISAN/aries/aries:11/RKQ1.201217.002`，
 * SmartisanOS 8.5.3）上的 `com.android.settings`，以及它的 framework：
 *
 * * `AbsListView.java` 里原厂把三个量都改成按视图高度算：
 *   `mOverscrollDistance = b - t`（拖拽上限 = 整屏高）、
 *   `mOverflingDistance = (b - t) / 2`（甩动上限 = 半屏高）、
 *   `mOverscrollMax = (b - t) / 3`，并声明了常量 `OVERSCROLL_LIMIT_DIVISOR = 3`；
 * * 拖拽过程按 **1/3 阻尼**：手指走 400 / 800 / 1200px，内容分别被拉出
 *   125 / 302 / 398px（60fps 录像逐帧量像素，误差在触摸 slop 之内）；
 * * 松手后**从静止回弹**：把录像的位移曲线（峰值 503px）逐帧拟合，
 *   临界阻尼弹簧 ω ≈ 22（rms 6px）明显优于其它候选 —— 四元缓出 rms 21px、
 *   三次缓出 18px、指数衰减 32px；换算成弹簧就是 stiffness 500、dampingRatio 1.0。
 *
 * 也就是说原版不是 Material 的「拉伸」（内容被拉伸变形），而是**刚性平移**：
 * 内容整块被拉出、上面露出底色，然后弹回去。
 */
object SmartisanOverscrollDefaults {
    /** 越界拖拽的阻尼系数：位移 = 手指位移 / 3。 */
    const val DragDivisor: Float = 3f

    /** 拖拽越界上限：容器高度的 1/3（原版 `mOverscrollMax = (b - t) / 3`）。 */
    const val MaxDragFraction: Float = 1f / 3f

    /** 甩动越界上限：容器高度的 1/2（原版 `mOverflingDistance = (b - t) / 2`）。 */
    const val MaxFlingFraction: Float = 1f / 2f

    /** 回弹弹簧刚度，对应实测 ω ≈ 22.36。 */
    const val Stiffness: Float = 500f

    /** 回弹弹簧阻尼比：临界阻尼，弹到位不过冲。 */
    const val DampingRatio: Float = 1f
}

/**
 * 把原版越界回弹装进 Compose 的滚动体系。
 *
 * 提供到 [androidx.compose.foundation.LocalOverscrollFactory] 之后，
 * 层级里所有滚动组件（`LazyColumn`、`Modifier.verticalScroll`、
 * `Modifier.scrollableArea` ……）都会自动换成原版行为；
 * [SmartisanTheme] 已经默认提供，一般不需要自己传。
 *
 * ```kotlin
 * // 想单独换掉某个滚动区域的越界效果：
 * val effect = rememberOverscrollEffect()
 * LazyColumn(modifier = Modifier.overscroll(effect!!)) { /* ... */ }
 * ```
 *
 * @param stiffness 弹簧刚度，见 [SmartisanOverscrollDefaults.Stiffness]。
 * @param dampingRatio 弹簧阻尼比，见 [SmartisanOverscrollDefaults.DampingRatio]。
 * @param dragDivisor 越界拖拽阻尼，见 [SmartisanOverscrollDefaults.DragDivisor]。
 * @param maxDragFraction 拖拽上限占容器尺寸的比例。
 * @param maxFlingFraction 甩动上限占容器尺寸的比例。
 */
@Stable
data class SmartisanOverscrollFactory(
    private val stiffness: Float = SmartisanOverscrollDefaults.Stiffness,
    private val dampingRatio: Float = SmartisanOverscrollDefaults.DampingRatio,
    private val dragDivisor: Float = SmartisanOverscrollDefaults.DragDivisor,
    private val maxDragFraction: Float = SmartisanOverscrollDefaults.MaxDragFraction,
    private val maxFlingFraction: Float = SmartisanOverscrollDefaults.MaxFlingFraction,
) : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect =
        SmartisanOverscrollEffect(
            stiffness = stiffness,
            dampingRatio = dampingRatio,
            dragDivisor = dragDivisor,
            maxDragFraction = maxDragFraction,
            maxFlingFraction = maxFlingFraction,
        )
}

/**
 * 原版 Smartisan 的滚动越界回弹（刚性平移 + 弹簧回弹）。
 *
 * 与 Material 的 `StretchOverscrollNode`（把内容拉伸变形）不同，这里做的是原版的行为：
 *
 * 1. 越界拖拽按 1/3 阻尼，位移上限是容器尺寸的 1/3；
 * 2. 内容整块平移，不做任何形变（实测行距、卡片高度全程不变）；
 * 3. 松手后从静止开始做临界阻尼弹簧回弹，弹到位不过冲；
 * 4. 甩到边界时，剩余速度变成越界位移（上限 1/2 容器尺寸）再弹回去。
 *
 * 实例由 [SmartisanOverscrollFactory] 创建，也可以在 `CompositionLocal` 里
 * 单独提供；[androidx.compose.foundation.rememberOverscrollEffect] 会从
 * 当前 factory 取一个。
 */
@Stable
class SmartisanOverscrollEffect internal constructor(
    private val stiffness: Float = SmartisanOverscrollDefaults.Stiffness,
    private val dampingRatio: Float = SmartisanOverscrollDefaults.DampingRatio,
    private val dragDivisor: Float = SmartisanOverscrollDefaults.DragDivisor,
    private val maxDragFraction: Float = SmartisanOverscrollDefaults.MaxDragFraction,
    private val maxFlingFraction: Float = SmartisanOverscrollDefaults.MaxFlingFraction,
) : OverscrollEffect {
    /**
     * 当前内容被拉出的位移（px）。
     *
     * 正 y = 内容整体向下平移 = 列表顶部越界（手指往下拖、列表已经在顶部）；
     * 负 y = 底部越界。x 轴同理，供横向列表使用。
     */
    var offset: Offset by mutableStateOf(Offset.Zero)
        private set

    /** 滚动容器的尺寸，用来算越界上限；由 [node] 在测量时回报。 */
    private var containerSize: IntSize = IntSize.Zero

    /** 回弹动画是否在跑。 */
    private var springRunning = false

    /** 新一轮触摸是否打断了回弹。 */
    private var springInterrupted = false

    override val isInProgress: Boolean
        get() = offset != Offset.Zero || springRunning

    override val node: DelegatableNode = BounceNode()

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        if (containerSize == IntSize.Zero) {
            return performScroll(delta)
        }
        // 手指回来了：先打断还在跑的回弹（原版也是「按下即停在当前位置」）。
        springInterrupted = true

        // 1. 反向拖拽先把已经拉出的位移 1:1 收回去（原版 overScrollBy 的还原段）。
        val released = releaseOffset(delta)
        val left = delta - released

        // 2. 剩下的交给真正的滚动。
        val consumed = performScroll(left)
        val remaining = left - consumed

        // 3. 滚不动的那部分按 1/3 阻尼变成越界位移，并卡在容器尺寸的 1/3 上。
        val applied = pullOffset(remaining)
        return released + consumed + applied
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        if (offset != Offset.Zero) {
            // 松手：原版实测回弹是从静止开始的（释放速度不参与），这里照做，
            // 也不再把这个速度交给甩动（原版 AbsListView 松手走 startSpringback）。
            runSpringBack(Offset.Zero)
            return
        }
        val consumed = performFling(velocity)
        val leftover = velocity - consumed
        if (leftover.x != 0f || leftover.y != 0f) {
            // 甩到边界：没被吃掉的速度变成越界位移，再弹回去。
            // 位移与滚动 delta 同向（见 pullOffset 的注释），所以这里不取反。
            runSpringBack(clampFlingVelocity(Offset(leftover.x, leftover.y)))
        }
    }

    /** 把已经拉出的位移按 1:1 收回去，返回被吃掉的 delta（与 [delta] 同号）。 */
    private fun releaseOffset(delta: Offset): Offset {
        val current = offset
        if (current == Offset.Zero) return Offset.Zero
        val releasedX = releaseAxis(current.x, delta.x)
        val releasedY = releaseAxis(current.y, delta.y)
        if (releasedX != 0f || releasedY != 0f) {
            offset = Offset(current.x - releasedX, current.y - releasedY)
        }
        return Offset(releasedX, releasedY)
    }

    /**
     * 单轴还原：位移与 delta 反号时，delta 就是「把内容推回边缘」的方向，
     * 按 1:1 抵消，最多抵消到位移归零；返回被吃掉的 delta。
     */
    private fun releaseAxis(offsetValue: Float, deltaValue: Float): Float {
        if (offsetValue == 0f || deltaValue == 0f) return 0f
        if ((offsetValue > 0f) == (deltaValue > 0f)) return 0f
        return if (abs(deltaValue) <= abs(offsetValue)) deltaValue else offsetValue
    }

    /**
     * 把滚不动的 delta 变成越界位移：先按 [dragDivisor] 阻尼，再卡在
     * [maxDragFraction] 倍容器尺寸上；返回被吃掉的 delta（未吃掉的还给上层）。
     *
     * 符号实测自真机：`Modifier.verticalScroll` 在列表顶部往下拖 300px，
     * 传进来的 delta.y 为正、内容要往下走，所以位移 = +delta / 3。
     */
    private fun pullOffset(remaining: Offset): Offset {
        val damped = Offset(remaining.x / dragDivisor, remaining.y / dragDivisor)
        if (damped == Offset.Zero) return Offset.Zero
        val maxX = containerSize.width * maxDragFraction
        val maxY = containerSize.height * maxDragFraction
        val current = offset
        val target = Offset(
            (current.x + damped.x).coerceIn(-maxX, maxX),
            (current.y + damped.y).coerceIn(-maxY, maxY),
        )
        val moved = target - current
        if (moved == Offset.Zero) return Offset.Zero
        offset = target
        // 换算回「滚动 delta」：位移是按 1/3 阻尼来的，还回去要乘 3。
        return Offset(moved.x * dragDivisor, moved.y * dragDivisor)
    }

    /**
     * 甩动越界速度上限。
     *
     * 临界阻尼弹簧被一个初速度推出去时，位移峰值是 `v / (ω · e)`；
     * 想让峰值不超过 [maxFlingFraction] 倍容器尺寸，速度就不能超过
     * `maxFlingFraction · size · ω · e`。
     */
    private fun clampFlingVelocity(velocity: Offset): Offset {
        val omega = sqrt(stiffness)
        val maxX = containerSize.width * maxFlingFraction * omega * SpringPeakFactor
        val maxY = containerSize.height * maxFlingFraction * omega * SpringPeakFactor
        return Offset(velocity.x.coerceIn(-maxX, maxX), velocity.y.coerceIn(-maxY, maxY))
    }

    /**
     * 回弹：临界阻尼弹簧逐帧积分。
     *
     * 和原版 framework 里的 `SpringForce` 用同一套半隐式欧拉积分
     * （`a = -ω²x - 2ζωv`），因此曲线形状与真机一致：从静止出发、
     * 速度先涨后落、单调回到边缘、不过冲。
     *
     * @param initialVelocity 越界位移的初速度（px/s，正 = 继续往外拉）。
     *   松手回弹传 [Offset.Zero]（原版实测释放速度不参与回弹）；
     *   甩到边界时传剩余甩动速度（与滚动 delta 同向，见 [pullOffset]）。
     */
    private suspend fun runSpringBack(initialVelocity: Offset) {
        if (springRunning) return
        if (offset == Offset.Zero && initialVelocity == Offset.Zero) return
        springRunning = true
        springInterrupted = false
        try {
            val omega = sqrt(stiffness)
            var x = offset.x
            var y = offset.y
            var velocityX = initialVelocity.x
            var velocityY = initialVelocity.y
            var lastFrame = 0L
            var elapsed = 0f
            while (true) {
                withFrameNanos { now ->
                    if (lastFrame != 0L) {
                        // 掉帧时最多按 50ms 积分，免得一步跨过整段曲线。
                        val dt = ((now - lastFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                        elapsed += dt
                        velocityX += (-omega * omega * x - 2f * dampingRatio * omega * velocityX) * dt
                        velocityY += (-omega * omega * y - 2f * dampingRatio * omega * velocityY) * dt
                        x += velocityX * dt
                        y += velocityY * dt
                    }
                    lastFrame = now
                }
                // 手指按下来了：停在当前位置，交给拖拽。
                if (springInterrupted) return
                if (
                    abs(x) <= StopThreshold &&
                    abs(y) <= StopThreshold &&
                    abs(velocityX) <= StopVelocity &&
                    abs(velocityY) <= StopVelocity
                ) {
                    break
                }
                if (elapsed >= MaxSpringSeconds) break
                offset = Offset(x, y)
            }
            offset = Offset.Zero
        } finally {
            springRunning = false
        }
    }

    /** 越界位移的绘制节点 + 手势结束检测。 */
    private inner class BounceNode : DelegatingNode() {
        init {
            delegate(BounceDrawNode())
            delegate(
                SuspendingPointerInputModifierNode {
                    // 每次手势：等它结束（抬起 / 取消），再跑回弹。
                    // 回弹要放在 awaitEachGesture 外面 —— 受限协程作用域里
                    // 不能调用带 withFrameNanos 的普通挂起函数。
                    while (true) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            waitForUpOrCancellation()
                        }
                        if (offset != Offset.Zero) {
                            runSpringBack(Offset.Zero)
                        }
                    }
                },
            )
        }
    }

    private inner class BounceDrawNode : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode {
        override fun onRemeasured(size: IntSize) {
            containerSize = size
        }

        override fun ContentDrawScope.draw() {
            val current = offset
            if (current == Offset.Zero) {
                drawContent()
                return
            }
            // 原版是刚性平移：整块内容挪出去，不做任何形变。
            val content = this
            translate(current.x, current.y) { content.drawContent() }
        }
    }

    private companion object {
        /** 位移小于这个值（px）且速度也够小时收尾。 */
        const val StopThreshold = 0.5f

        /** 收尾速度阈值（px/s）。 */
        const val StopVelocity = 8f

        /** 回弹最长时长（秒），兜底防止极端参数下不收敛。 */
        const val MaxSpringSeconds = 2f

        /**
         * 临界阻尼弹簧被初速度推出去时，峰值位移 = 初速度 / (ω · e)，
         * 所以「初速度 = 峰值位移 × ω × e」，这里的 1/e 就是换算系数。
         */
        const val SpringPeakFactor = 0.36787945f
    }
}
