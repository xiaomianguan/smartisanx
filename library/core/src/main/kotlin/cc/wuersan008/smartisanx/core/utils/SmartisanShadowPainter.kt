package cc.wuersan008.smartisanx.core.utils

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import android.view.View
import kotlin.math.roundToInt

/**
 * 把「内容底图 + 向外投影的阴影 9-patch」两层合成一个 [Painter]。
 *
 * 这是锤子列表卡片的核心机制，对应原版锤子音乐的 `ShadowDrawable`：
 *
 * - 内容底图（`group_list_item_bg_top` 之类）画在控件自身的边界内；
 * - 阴影 9-patch（`list_content_item_top_shadow` 之类）按自己的 **padding** 向四周
 *   扩张后绘制，因此投影会落在控件边界**之外**；
 * - 绘制顺序是先阴影、后内容。
 *
 * 因此使用方需要给控件留出外边距，投影才有地方显示 —— 原版也是靠父容器的 padding 实现的。
 */
internal class SmartisanShadowPainter(
    val shadow: Drawable,
    val target: Drawable,
    private val insetLeftRight: Int,
    private val insetTopBottom: Int,
) : Painter(), Drawable.Callback {
    private var invalidation by mutableIntStateOf(0)
    private val handler = Handler(Looper.getMainLooper())

    override val intrinsicSize: Size
        get() =
            if (target.intrinsicWidth > 0 && target.intrinsicHeight > 0) {
                Size(target.intrinsicWidth.toFloat(), target.intrinsicHeight.toFloat())
            } else {
                Size.Unspecified
            }

    override fun DrawScope.onDraw() {
        @Suppress("UNUSED_VARIABLE") val generation = invalidation
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        target.setBounds(0, 0, width, height)
        // 阴影按 padding 向外扩张，和原版 ShadowDrawable.onBoundsChange 一致。
        shadow.setBounds(
            -insetLeftRight,
            -insetTopBottom,
            width + insetLeftRight,
            height + insetTopBottom,
        )
        drawIntoCanvas { canvas ->
            shadow.draw(canvas.nativeCanvas)
            target.draw(canvas.nativeCanvas)
        }
    }

    fun attach() {
        shadow.callback = this
        target.callback = this
        shadow.setVisible(true, true)
        target.setVisible(true, true)
    }

    fun detach() {
        shadow.callback = null
        target.callback = null
        shadow.setVisible(false, false)
        target.setVisible(false, false)
        handler.removeCallbacksAndMessages(shadow)
        handler.removeCallbacksAndMessages(target)
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidation++
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        handler.postAtTime(what, who, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        handler.removeCallbacks(what, who)
    }
}

private class ShadowPainterOwner(shadow: Drawable, target: Drawable, left: Int, top: Int) : RememberObserver {
    val painter = SmartisanShadowPainter(shadow, target, left, top)

    override fun onRemembered() = painter.attach()

    override fun onForgotten() = painter.detach()

    override fun onAbandoned() = painter.detach()
}

/**
 * 生成「内容底图 + 原版阴影 9-patch」的合成 Painter。
 *
 * 阴影的扩张量取自阴影 9-patch 自身的 padding，不需要调用方传数值。
 */
@Composable
fun rememberSmartisanShadowPainter(
    @DrawableRes backgroundRes: Int,
    @DrawableRes shadowRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    focused: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): Painter {
    val resources = smartisanThemedResources()
    val owner =
        remember(resources, backgroundRes, shadowRes) {
            val target = requireNotNull(resources.getDrawable(backgroundRes, null)) { "找不到底图：$backgroundRes" }.mutate()
            val shadow = requireNotNull(resources.getDrawable(shadowRes, null)) { "找不到阴影：$shadowRes" }.mutate()
            val padding = Rect()
            shadow.getPadding(padding)
            ShadowPainterOwner(shadow, target, padding.left, padding.top)
        }
    val direction = LocalLayoutDirection.current
    SideEffect {
        val state = smartisanDrawableState(enabled, pressed, selected, focused, checked, activated)
        owner.painter.target.state = state
        owner.painter.shadow.state = state
        val layoutDirection =
            if (direction == LayoutDirection.Rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        owner.painter.target.layoutDirection = layoutDirection
        owner.painter.shadow.layoutDirection = layoutDirection
    }
    return owner.painter
}

/**
 * 用「内容底图 + 原版阴影」作为背景。
 *
 * 投影会画在控件边界之外，所以调用方需要留出外边距。
 */
@Composable
fun Modifier.smartisanShadowBackground(
    @DrawableRes backgroundRes: Int,
    @DrawableRes shadowRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    focused: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): Modifier {
    val painter =
        rememberSmartisanShadowPainter(
            backgroundRes = backgroundRes,
            shadowRes = shadowRes,
            enabled = enabled,
            pressed = pressed,
            selected = selected,
            focused = focused,
            checked = checked,
            activated = activated,
        )
    return smartisanPainterBackground(painter)
}
