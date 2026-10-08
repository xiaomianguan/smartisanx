package top.smartisanx.core.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

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
