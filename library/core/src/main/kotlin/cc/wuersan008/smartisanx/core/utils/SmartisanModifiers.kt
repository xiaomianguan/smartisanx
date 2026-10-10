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
 */
fun Modifier.smartisanPainterBackground(painter: Painter): Modifier =
    drawBehind { with(painter) { draw(size) } }

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
    return smartisanPainterBackground(painter)
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

