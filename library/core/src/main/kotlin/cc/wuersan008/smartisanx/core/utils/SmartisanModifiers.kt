package cc.wuersan008.smartisanx.core.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import android.graphics.Rect

/**
 * 用 [Painter] 作为背景绘制。
 *
 * 对应原版把 nine-patch / selector 直接设为 `View.background` 的做法，但不创建 View 宿主。
 *
 * @param stateKey 当前 drawable 状态的指纹，见 [smartisanRepaintOnStateChange]。
 *   默认 0 表示「这块底图不会随状态变」。
 */
fun Modifier.smartisanPainterBackground(painter: Painter, stateKey: Int = 0): Modifier =
    // layer 必须建在绘制块**前面**（原因见 smartisanRepaintOnStateChange）：
    // 这样绘制块才落在「拥有该 layer 的那个 coordinator」的区段里。
    graphicsLayer().drawBehind {
        stateKey
        with(painter) { draw(size) }
    }

/**
 * 让「用 drawable / painter 画出来的东西」跟随 selector 状态真正刷新。
 *
 * 原版把这些素材设成 `View.background` / `ImageView`，状态一变系统自己就会重绘；
 * Compose 里没有这个机制，实测下来有两层坑：
 *
 * 1. [androidx.compose.foundation.Image]`(painter)` 内部是 `PainterNode`，
 *    而 `PainterNode.getShouldAutoInvalidate()` 是 **false**（已对 1.12.1 的
 *    字节码核对过），所以只换 painter 实例并不会触发 `invalidateDraw()`；
 * 2. 绘制阶段读 snapshot 状态（[SmartisanDrawablePainter] 在 `onDraw` 里读
 *    `invalidation` 就是为了这个）确实会被记录，但观察者的回调是
 *    `coordinator.layer?.invalidate()`——coordinator 自己没有 layer 时**什么都不发生**；
 *    同理元素更新走的 `invalidateDraw()` → `NodeCoordinator.invalidateLayer()`
 *    也只会沿着 `wrappedBy` 往外找 layer，绘制节点不在带 layer 的区段里就白跑。
 *
 * 于是现象就是「语义里 `checked` 已经是 true、圆圈还是空心的」：
 * 多选列表的勾选框、列表行按压 / 多选高亮底图都踩这个坑。
 *
 * 修法是先给节点建一层 layer（`graphicsLayer()`，实测 `placeWithLayer` 一定会
 * 创建 layer），再把绘制块挂在它**紧后面**；同时让绘制块闭包捕获 [stateKey]，
 * 每次状态变化都会生成新的 `DrawBehindElement`（`equals` 比的是闭包引用）→
 * `autoInvalidateUpdatedNode` → `invalidateDraw()` → 命中 layer → 重绘。
 *
 * @param stateKey 当前 drawable 状态的指纹，直接传
 *   `smartisanDrawableState(...).contentHashCode()`。
 */
fun Modifier.smartisanRepaintOnStateChange(stateKey: Int): Modifier =
    graphicsLayer().drawBehind {
        // 这里不画任何东西：只要闭包捕获 stateKey（见上面的说明）。
        stateKey
    }

/** 用 drawable 资源作为背景绘制，并按状态切换。 */
@Composable
fun Modifier.smartisanDrawableBackground(
    @DrawableRes drawableRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    focused: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): Modifier {
    val painter =
        rememberSmartisanDrawablePainter(
            drawableRes = drawableRes,
            enabled = enabled,
            pressed = pressed,
            selected = selected,
            focused = focused,
            checked = checked,
            activated = activated,
        )
    val stateKey =
        smartisanDrawableState(
                enabled = enabled,
                pressed = pressed,
                selected = selected,
                focused = focused,
                checked = checked,
                activated = activated,
            )
            .contentHashCode()
    return smartisanPainterBackground(painter, stateKey)
}

/**
 * 原版列表行背景是「内容 drawable + 向外投影的 nine-patch 阴影」两层。
 *
 * 这里用 Compose 的 elevation 阴影还原同样的投影感：
 * 阴影不裁剪内容（`clip = false`），因此可以和原版一样压在相邻行之上。
 */
fun Modifier.smartisanProjectedShadow(
    elevation: Dp = 1.dp,
    shape: Shape = RectangleShape,
): Modifier = this.shadow(elevation = elevation, shape = shape, clip = false)

/**
 * 顶外边距，**允许负值**。
 *
 * 原版布局里偶有 `android:layout_marginTop="-0.4dp"` 这类负外边距（例如跑马灯标题的
 * 副标题 `marquee_subtitle_margin_top = -0.4dp`），而 Compose 的 `Modifier.padding`
 * 会直接抛 `Padding must be non-negative`，所以这里用 `layout` 自己实现一个真正的负外边距：
 * 自身高度按「内容高度 + margin」上报（负值即收紧父容器），内容整体向上偏移，
 * 后续兄弟节点的位置与父容器高度都与原版一致。
 *
 * ```kotlin
 * Text("副标题", modifier = Modifier.smartisanTopMargin((-0.4).dp))
 * ```
 *
 * @param margin 顶外边距，正值相当于 `padding(top = margin)`，负值把内容往上提。
 */
fun Modifier.smartisanTopMargin(margin: Dp): Modifier =
    layout { measurable, constraints ->
        val marginPx = margin.roundToPx()
        val placeable = measurable.measure(constraints)
        layout(placeable.width, (placeable.height + marginPx).coerceAtLeast(0)) {
            placeable.placeRelative(0, marginPx)
        }
    }

/**
 * 读 9-patch 自己声明的 padding，转成 Compose 的 [PaddingValues]。
 *
 * 对应原版「把一个 9-patch 设成 `View.background` 时，View 自动吃掉它的 padding」这个行为：
 * framework 的不少布局（例如进度弹窗的 246dp 卡片）就靠这个 padding 撑出内边距，
 * 布局里一个 `padding*` 都没写。
 *
 * ```kotlin
 * Box(Modifier.width(246.dp).smartisanDrawableBackground(res).padding(rememberSmartisanDrawablePadding(res))) { ... }
 * ```
 */
@Composable
fun rememberSmartisanDrawablePadding(@DrawableRes drawableRes: Int): PaddingValues {
    val resources = smartisanThemedResources()
    val density = LocalDensity.current
    return remember(resources, drawableRes, density) {
        val padding = Rect()
        resources.getDrawable(drawableRes, null)?.getPadding(padding)
        PaddingValues(
            start = with(density) { padding.left.toDp() },
            top = with(density) { padding.top.toDp() },
            end = with(density) { padding.right.toDp() },
            bottom = with(density) { padding.bottom.toDp() },
        )
    }
}

