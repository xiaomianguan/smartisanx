package cc.wuersan008.smartisanx.core.utils

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/**
 * 把 Android [Drawable]（包括 selector 与 nine-patch）直接画到 Compose 画布上。
 *
 * 三个复刻项目各自维护了一份实现（锤子音乐的 `SmartisanDrawablePainter`、
 * 锤子天气的 `WeatherDrawablePainter`），逻辑完全一致，这里合并为唯一实现。
 *
 * 与 androidx 的 `DrawablePainter` 不同，这里的实现会：
 * 1. 把 enabled / pressed / selected / focused / checked 映射为 drawable 状态；
 * 2. 跟随布局方向设置 drawable 的 layoutDirection；
 * 3. 把 drawable 回调挂到主线程 Handler，保证帧动画与 `invalidateSelf` 正常工作。
 */
@Composable
fun rememberSmartisanDrawablePainter(
    @DrawableRes drawableRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    focused: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): Painter {
    // 关键：按 smartisanx 主题（而不是系统 uiMode）解析 drawable，
    // 否则应用内切到深色时，drawable-night 里的夜间素材不会被选中。
    val resources = smartisanThemedResources()
    val painter =
        remember(resources, drawableRes) {
            val drawable =
                requireNotNull(resources.getDrawable(drawableRes, null)) {
                    "找不到 drawable 资源：$drawableRes"
                }
            DrawablePainterOwner(drawable.mutate()).painter
        }
    UpdateDrawableState(painter, enabled, pressed, selected, focused, checked, activated)
    return painter
}

/** 把 drawable 状态同步到 painter，避免在组合阶段写入。 */
@Composable
private fun UpdateDrawableState(
    painter: SmartisanDrawablePainter,
    enabled: Boolean,
    pressed: Boolean,
    selected: Boolean,
    focused: Boolean,
    checked: Boolean,
    activated: Boolean,
) {
    val direction = LocalLayoutDirection.current
    SideEffect {
        painter.drawable.state = smartisanDrawableState(enabled, pressed, selected, focused, checked, activated)
        painter.drawable.layoutDirection =
            if (direction == LayoutDirection.Rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }
}

/** 把 Compose 状态映射为 Android drawable 状态数组。 */
fun smartisanDrawableState(
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    focused: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): IntArray =
    intArrayOf(
        if (enabled) android.R.attr.state_enabled else -android.R.attr.state_enabled,
        if (pressed) android.R.attr.state_pressed else -android.R.attr.state_pressed,
        if (selected) android.R.attr.state_selected else -android.R.attr.state_selected,
        if (focused) android.R.attr.state_focused else -android.R.attr.state_focused,
        if (checked) android.R.attr.state_checked else -android.R.attr.state_checked,
        if (activated) android.R.attr.state_activated else -android.R.attr.state_activated,
    )

// 观察者保持在原本的 remember 作用域内：调用方若再次 remember 返回的 Painter，
// 不应该得到第二个生命周期更短的 drawable 回调。
private class DrawablePainterOwner(drawable: Drawable) : RememberObserver {
    val painter = SmartisanDrawablePainter(drawable)

    override fun onRemembered() = painter.attach()

    override fun onForgotten() = painter.detach()

    override fun onAbandoned() = painter.detach()
}

/**
 * 只负责绘制的 Painter，读取 [invalidation] 以便 drawable 自身的
 * `invalidateSelf` 能精确重绘对应的绘制作用域。
 */
internal class SmartisanDrawablePainter(val drawable: Drawable) : Painter(), Drawable.Callback {
    private var invalidation by mutableIntStateOf(0)
    private val handler = Handler(Looper.getMainLooper())

    override val intrinsicSize: Size
        get() =
            if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                Size(drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            } else {
                Size.Unspecified
            }

    override fun DrawScope.onDraw() {
        @Suppress("UNUSED_VARIABLE") val generation = invalidation
        drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }

    fun attach() {
        drawable.callback = this
        drawable.setVisible(true, true)
    }

    fun detach() {
        drawable.setVisible(false, false)
        drawable.callback = null
        handler.removeCallbacksAndMessages(drawable)
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidation++
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        handler.postAtTime(what, drawable, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        handler.removeCallbacks(what, drawable)
    }
}
