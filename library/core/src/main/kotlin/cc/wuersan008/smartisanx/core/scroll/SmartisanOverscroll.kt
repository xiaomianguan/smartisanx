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
import kotlin.math.max
import kotlin.math.min
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
 * * 松手后是**四元缓出** `1-(1-t/T)^4`，时长 `T = sqrt(2·Δ/GRAVITY)`：
 *   真机录像逐帧量位移曲线，四元缓出 rms 5.8–7.0px 优于临界阻尼弹簧 7.5–8.3px、
 *   三次缓出 6.7–8.5px、指数衰减 11.2–11.8px；`framework.jar` 里
 *   `OverScroller.SplineOverScroller.startSpringback` 写的正是
 *   `mDuration = sqrt(-2·delta/deceleration)·1000 + |velocity|/10`，
 *   减速度是 `GRAVITY = 8000`，两条独立证据对上（实测 T 与公式相差 10% 以内）；
 * * 甩到边界时**先滑行再回弹**：`onEdgeReached` 用 `v²/(2·GRAVITY)` 算滑行距离，
 *   超过允许值就把减速度放大到刚好停在上限；允许值又被
 *   `continueWhenFinished` 里那句 `mOver = mOver · |v| / mMaximumVelocity` 按速度缩放
 *   （真机 9000px/s 的甩动 → 上限 472px，实测滑到 523px、滑行 87ms）；
 * * 回弹**从静止开始**（速度序列先涨后落，释放速度不参与），
 *   也就是弹簧的初速度项在真机上观察不到，公式里的 `|velocity|/10` 不参与。
 *
 * 也就是说原版不是 Material 的「拉伸」（内容被拉伸变形），而是**刚性平移**：
 * 内容整块被拉出、上面露出底色，然后滑行 + 回弹。
 */
object SmartisanOverscrollDefaults {
    /** 越界拖拽的阻尼系数：位移 = 手指位移 / 3。 */
    const val DragDivisor: Float = 3f

    /** 拖拽越界上限：容器高度的 1/3（原版 `mOverscrollMax = (b - t) / 3`）。 */
    const val MaxDragFraction: Float = 1f / 3f

    /** 甩动越界上限：容器高度的 1/2（原版 `mOverflingDistance = (b - t) / 2`）。 */
    const val MaxFlingFraction: Float = 1f / 2f

    /**
     * 原版 framework 的回弹加速度 `GRAVITY`（px/s²）。
     *
     * 这是 R2 上的**裸 px 常数**（400dpi），所以按 [GravityReferenceDensity] 折算成密度无关的
     * 加速度再乘当前密度 —— 这样同一次手势在不同密度的机器上滑行距离一样。
     */
    const val Gravity: Float = 8000f

    /** [Gravity] 对应的基准密度（坚果 R2 是 400dpi）。 */
    const val GravityReferenceDensity: Float = 2.5f

    /**
     * 甩动速度上限（dp/s）：原版 `ViewConfiguration.getScaledMaximumFlingVelocity()`，
     * 用来按速度缩放甩动越界的上限（原版 `mOver · |v| / mMaximumVelocity`）。
     */
    const val MaxFlingVelocity: Float = 8000f
}

/**
 * 把原版越界滑行 + 回弹装进 Compose 的滚动体系。
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
 * @param dragDivisor 越界拖拽阻尼，见 [SmartisanOverscrollDefaults.DragDivisor]。
 * @param maxDragFraction 拖拽上限占容器尺寸的比例。
 * @param maxFlingFraction 甩动上限占容器尺寸的比例。
 * @param gravity 回弹加速度，见 [SmartisanOverscrollDefaults.Gravity]。
 * @param maxFlingVelocity 甩动速度上限（dp/s），见 [SmartisanOverscrollDefaults.MaxFlingVelocity]。
 */
@Stable
data class SmartisanOverscrollFactory(
    private val dragDivisor: Float = SmartisanOverscrollDefaults.DragDivisor,
    private val maxDragFraction: Float = SmartisanOverscrollDefaults.MaxDragFraction,
    private val maxFlingFraction: Float = SmartisanOverscrollDefaults.MaxFlingFraction,
    private val gravity: Float = SmartisanOverscrollDefaults.Gravity,
    private val maxFlingVelocity: Float = SmartisanOverscrollDefaults.MaxFlingVelocity,
) : OverscrollFactory {
    override fun createOverscrollEffect(): OverscrollEffect =
        SmartisanOverscrollEffect(
            dragDivisor = dragDivisor,
            maxDragFraction = maxDragFraction,
            maxFlingFraction = maxFlingFraction,
            gravity = gravity,
            maxFlingVelocity = maxFlingVelocity,
        )
}

/**
 * 原版 Smartisan 的滚动越界（刚性平移 + 匀减速滑行 + 四元缓出回弹）。
 *
 * 与 Material 的 `StretchOverscrollNode`（把内容拉伸变形）不同，这里做的是原版的行为：
 *
 * 1. 越界拖拽按 1/3 阻尼，位移上限是容器尺寸的 1/3（原版 `mOverscrollMax`）；
 * 2. 内容整块平移，不做任何形变（实测行距、卡片高度全程不变）；
 * 3. 松手 / 甩到边界时，速度朝外就先**匀减速滑行**到原版算出的顶点
 *    （`onEdgeReached`），再**四元缓出**回弹（`startSpringback`）；
 * 4. 回弹时长是 `sqrt(2·Δ/GRAVITY)` —— 与越界距离是平方根关系，
 *    所以拉得再远也是「越远越快」，不会越拉越慢。
 *
 * 实例由 [SmartisanOverscrollFactory] 创建，也可以在 `CompositionLocal` 里
 * 单独提供；[androidx.compose.foundation.rememberOverscrollEffect] 会从
 * 当前 factory 取一个。
 */
@Stable
class SmartisanOverscrollEffect internal constructor(
    private val dragDivisor: Float = SmartisanOverscrollDefaults.DragDivisor,
    private val maxDragFraction: Float = SmartisanOverscrollDefaults.MaxDragFraction,
    private val maxFlingFraction: Float = SmartisanOverscrollDefaults.MaxFlingFraction,
    private val gravity: Float = SmartisanOverscrollDefaults.Gravity,
    private val maxFlingVelocity: Float = SmartisanOverscrollDefaults.MaxFlingVelocity,
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

    /** 当前密度，用来把原版的裸 px 常数折算成密度无关的加速度；由 [node] 在绘制时回报。 */
    private var density: Float = 1f

    /** 越界动画是否在跑。 */
    private var animating = false

    /** 新一轮触摸是否打断了动画。 */
    private var interrupted = false

    override val isInProgress: Boolean
        get() = offset != Offset.Zero || animating

    override val node: DelegatableNode = BounceNode()

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        if (containerSize == IntSize.Zero) {
            return performScroll(delta)
        }
        // 手指回来了：先打断还在跑的动画（原版也是「按下即停在当前位置」）。
        interrupted = true

        // 1. 反向拖拽先把已经拉出的位移 1:1 收回去（原版 overScrollBy 的还原段）。
        val released = releaseOffset(delta)
        val left = delta - released

        // 2. 剩下的交给真正的滚动。
        val consumed = performScroll(left)
        val remaining = left - consumed

        // 3. 只有手指拖动才把滚不动的那部分按 1/3 阻尼变成越界位移（原版拖拽路径）。
        //    甩动动画自己的 delta 不吃掉、也不平移 —— 让它停在这里，剩余速度由
        //    applyToFling 走原版的滑行（AbsListView 也是靠 overScroller 停下再 bounce）。
        if (source != NestedScrollSource.UserInput) {
            return released + consumed
        }
        return released + consumed + pullOffset(remaining)
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        if (offset != Offset.Zero) {
            // 松手时已经在越界：原版 OverScroller.startAfterEdge —— 速度还朝外就继续
            // 滑行（startBounceAfterEdge），否则直接回弹（startSpringback）。
            runOverscrollAnimation(Offset(velocity.x, velocity.y))
            return
        }
        val consumed = performFling(velocity)
        val leftover = velocity - consumed
        if (leftover.x != 0f || leftover.y != 0f) {
            // 甩到边界：没被吃掉的速度变成滑行 + 回弹（原版 notifyEdgeReached）。
            runOverscrollAnimation(Offset(leftover.x, leftover.y))
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
     * 滑行 + 回弹：逐帧按 [OverscrollAxisAnimation] 的闭式曲线推进。
     *
     * 两段的时长都是算出来的、到点精确落回 0 —— 没有「慢慢收尾」的尾巴，也不用
     * 阈值判断，所以同一段越界的时长每次完全一致（这正是原版的手感）。
     *
     * @param velocity 与 [offset] 同号的越界速度（px/s）：同号 = 继续往外拉，
     *   先滑行再回弹；反号或为 0 = 直接回弹（原版 startSpringback）。
     */
    private suspend fun runOverscrollAnimation(velocity: Offset) {
        if (animating) return
        val gravityPx = gravity * density / SmartisanOverscrollDefaults.GravityReferenceDensity
        val maxVelocityPx = maxFlingVelocity * density
        val axisX = OverscrollAxisAnimation(
            start = offset.x,
            velocity = velocity.x,
            size = containerSize.width.toFloat(),
            gravityPx = gravityPx,
            maxFlingVelocityPx = maxVelocityPx,
            maxFlingFraction = maxFlingFraction,
        )
        val axisY = OverscrollAxisAnimation(
            start = offset.y,
            velocity = velocity.y,
            size = containerSize.height.toFloat(),
            gravityPx = gravityPx,
            maxFlingVelocityPx = maxVelocityPx,
            maxFlingFraction = maxFlingFraction,
        )
        if (axisX.isIdle && axisY.isIdle) {
            offset = Offset.Zero
            return
        }
        animating = true
        interrupted = false
        try {
            val total = max(axisX.duration, axisY.duration)
            var elapsed = 0f
            var lastFrame = 0L
            while (true) {
                withFrameNanos { now ->
                    if (lastFrame != 0L) {
                        // 掉帧时最多按 50ms 推进，免得一步跨过整段曲线。
                        elapsed += ((now - lastFrame) / 1_000_000_000f).coerceAtMost(0.05f)
                    }
                    lastFrame = now
                }
                // 手指按下来了：停在当前位置，交给拖拽。
                if (interrupted) return
                if (elapsed >= total) break
                offset = Offset(axisX.positionAt(elapsed), axisY.positionAt(elapsed))
            }
            offset = Offset.Zero
        } finally {
            animating = false
        }
    }



    /** 越界位移的绘制节点 + 手势结束兜底。 */
    private inner class BounceNode : DelegatingNode() {
        init {
            delegate(BounceDrawNode())
            delegate(
                SuspendingPointerInputModifierNode {
                    // 每次手势：等它结束（抬起 / 取消），再兜底回弹。
                    // 兜底要放在 awaitEachGesture 外面 —— 受限协程作用域里
                    // 不能调用带 withFrameNanos 的普通挂起函数。
                    while (true) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            waitForUpOrCancellation()
                        }
                        if (offset != Offset.Zero && !animating) {
                            // 先让同一次抬手里的 applyToFling 跑（它会带上速度做滑行），
                            // 一帧后还没人接手才兜底：按「松手速度不参与」的实测行为回弹。
                            withFrameNanos { }
                            if (offset != Offset.Zero && !animating) {
                                runOverscrollAnimation(Offset.Zero)
                            }
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
            this@SmartisanOverscrollEffect.density = this.density
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
}

/**
 * 单轴越界动画：匀减速滑行 + 四元缓出回弹，两段都是闭式解。
 *
 * 1. **滑行**（原版 `SplineOverScroller.onEdgeReached` 的 BALLISTIC 状态）：
 *    从边缘起以速度 v 匀减速滑出 `min(v²/(2·GRAVITY), 上限)`；超过上限时原版把
 *    减速度放大到刚好停在上限，这里等价地取 `a = v²/(2d)`；已经拉出去的位置按
 *    `fitOnBounceCurve` 的做法对齐到曲线上（求出当前位置在曲线上对应的时间），
 *    所以只会滑「剩下的那一段」。
 * 2. **回弹**（原版 `startSpringback` 的 CUBIC 状态）：`Δ·(1-t/T)^4`，
 *    时长 `T = sqrt(2·Δ/GRAVITY)`。
 *
 * @param start 当前越界位移（带符号）。
 * @param velocity 越界速度（px/s），与 [start] 同号 = 继续往外拉。
 */
private class OverscrollAxisAnimation(
    private val start: Float,
    velocity: Float,
    size: Float,
    gravityPx: Float,
    maxFlingVelocityPx: Float,
    maxFlingFraction: Float,
) {
    /** 越界方向：+1 = 内容往下 / 往右，-1 = 反之。 */
    private val sign: Float = when {
        velocity > 0f -> 1f
        velocity < 0f -> -1f
        start >= 0f -> 1f
        else -> -1f
    }

    private val coastSpeed: Float
    private val coastDeceleration: Float

    /** 当前位置在滑行曲线上已经走过的时间（原版 `fitOnBounceCurve` 的对齐量）。 */
    private val coastElapsed: Float
    private val coastSeconds: Float

    /** 滑行终点 = 回弹起点。 */
    private val apex: Float
    private val springSeconds: Float

    /** 整段动画的时长（秒）。 */
    val duration: Float

    /** 这根轴有没有事可做（位移与速度都是 0）。 */
    val isIdle: Boolean

    init {
        val speed = abs(velocity)
        // 原版 mOverflingDistance · |v| / mMaximumVelocity：甩得越慢，允许的越界越小。
        val ratio = if (maxFlingVelocityPx > 0f) speed / maxFlingVelocityPx else 1f
        val allowed = maxFlingFraction * size * min(1f, ratio)
        var distance = if (gravityPx > 0f) speed * speed / (2f * gravityPx) else 0f
        if (distance > allowed) distance = allowed
        val outward = speed > 0f && (start == 0f || (start > 0f) == (velocity > 0f))
        if (outward && distance > abs(start)) {
            coastSpeed = speed
            coastDeceleration = speed * speed / (2f * distance)
            val travelled = abs(start)
            coastElapsed = (
                speed -
                    sqrt((speed * speed - 2f * coastDeceleration * travelled).coerceAtLeast(0f))
                ) / coastDeceleration
            coastSeconds = speed / coastDeceleration - coastElapsed
            apex = sign * distance
        } else {
            coastSpeed = 0f
            coastDeceleration = 0f
            coastElapsed = 0f
            coastSeconds = 0f
            apex = start
        }
        springSeconds = if (apex == 0f || gravityPx <= 0f) 0f else sqrt(2f * abs(apex) / gravityPx)
        duration = coastSeconds + springSeconds
        isIdle = coastSeconds <= 0f && springSeconds <= 0f
    }

    /** 动画开始后 [elapsed] 秒（闭式）的越界位移。 */
    fun positionAt(elapsed: Float): Float {
        if (coastSeconds > 0f && elapsed < coastSeconds) {
            val t = coastElapsed + elapsed
            return sign * (coastSpeed * t - coastDeceleration * t * t / 2f)
        }
        if (springSeconds <= 0f) return 0f
        val progress = ((elapsed - coastSeconds) / springSeconds).coerceIn(0f, 1f)
        val remaining = 1f - progress
        return apex * remaining * remaining * remaining * remaining
    }
}
