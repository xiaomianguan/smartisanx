package cc.wuersan008.smartisanx.ui.overlay

import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlinx.coroutines.delay
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R

/**
 * 浮层组件组的窗口外壳。
 *
 * 复刻来源：
 * - 锤子音乐 `ui/components/SmartisanModal.kt`：`Dialog` + `usePlatformDefaultWidth = false` 的透明窗口外壳；
 * - 锤子时钟 `widget/SmartisanModalDialog.kt`：0.54 遮罩浓度、`FLAG_DIM_BEHIND`、软键盘不顶起窗口；
 * - 锤子时钟 `widget/SmartisanMenuDialog.kt`：贴底全宽窗口。
 *
 * 合并点：三个原始实现各自维护一份「透明窗口 + 遮罩 + 对齐 + 软键盘模式」的样板代码，
 * 这里统一为 [SmartisanModal]；[SmartisanModalWindow] 在它之上提供居中弹窗外壳，
 * 底部弹层（[SmartisanMenuDialog]、[SmartisanBottomSheet]）也直接复用它。
 */

/** 浮层进场动画时长，对齐原版 `smartisan_menu_enter` 的 300ms。 */
internal const val SmartisanOverlayEnterDurationMillis = 300

/** 浮层退场动画时长，对齐原版 `smartisan_menu_exit` 的 250ms。 */
internal const val SmartisanOverlayExitDurationMillis = 250

/**
 * 弹窗 / 弹层的窗口外壳。
 *
 * 窗口本身透明、无标题栏、宽度铺满，位置由 [bottom] 决定：
 * - `bottom = false`（默认）：窗口居中，适合 [SmartisanModalWindow] 这类居中弹窗；
 * - `bottom = true`：窗口贴底，适合菜单、底部弹层。
 *
 * 遮罩由窗口的 `FLAG_DIM_BEHIND` 实现（[dimAmount] 为 0 时不加该标记），
 * 因此窗口内容自身不需要再画一层半透明底。
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanModal(onDismissRequest = { visible = false }, bottom = true) {
 *         SmartisanText("贴底弹层", Modifier.padding(24.dp))
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 点击外部或返回键时的回调，由调用方决定是否关闭。
 * @param modifier 作用于外壳内部的根 [Column]。
 * @param bottom 是否贴底对齐。
 * @param dimAmount 窗口背后的遮罩浓度，原版为 0.54。
 * @param content 外壳内容，纵向排列；底部弹层通常用 `fillMaxWidth()` 铺满。
 */
@Composable
fun SmartisanModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    bottom: Boolean = false,
    dimAmount: Float = 0.54f,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Dialog 的内容视图是 DialogLayout，它实现了 DialogWindowProvider，可以直接拿到窗口。
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                if (dimAmount > 0f) {
                    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    setDimAmount(dimAmount)
                } else {
                    clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                }
                setGravity(if (bottom) Gravity.BOTTOM else Gravity.CENTER)
                // 原版弹窗由内容自己处理键盘，窗口不跟着顶起。
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                // 高度交给内容决定：贴底时窗口正好贴住屏幕底边。
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                )
            }
        }
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/**
 * 居中弹窗外壳：宽度固定为 308dp（原版 `smartisan_modal_width`），
 * 底色是原版 `smartisan_modal_background`（10dp 圆角 + 1px `smartisan_modal_border` 描边的 shape），
 * 并按原版 `android:clipToOutline="true"` 的做法整体裁剪 10dp 圆角，
 * 因此弹窗内部的按钮、内容区**不需要也不应该**再自己画圆角。
 *
 * 进出场动画是原版 `smartisan_modal_enter` / `smartisan_modal_exit` 的「0.9 → 1.0 缩放 + 淡入淡出」。
 *
 * 关闭请求会先播放退场动画，动画结束后才回调 [onDismissRequest]，调用方直接用它控制组合即可：
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanModalWindow(onDismissRequest = { visible = false }) {
 *         SmartisanDialogTitleBar(title = "标题", onDismiss = { visible = false })
 *         SmartisanText("内容", Modifier.padding(18.dp))
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 退场动画播放完毕后的关闭回调。
 * @param modifier 作用于弹窗面板。
 * @param dimAmount 窗口背后的遮罩浓度，原版为 0.54。
 * @param backgroundRes 弹窗底色；默认用原版 `smartisan_modal_background`，传 null 时退回色板的 `surface`。
 * @param content 弹窗内容，纵向排列；组件已裁剪圆角，底部按钮可以直接铺满。
 */
@Composable
fun SmartisanModalWindow(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dimAmount: Float = 0.54f,
    @DrawableRes backgroundRes: Int? = R.drawable.smartisan_modal_background,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val controller = rememberSmartisanOverlayController(onDismissRequest)
    val progress = rememberSmartisanOverlayProgress(controller.visible)
    val dismiss = controller::requestDismiss
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.surface)
        }
    SmartisanModal(
        onDismissRequest = dismiss,
        bottom = false,
        dimAmount = dimAmount,
    ) {
        CompositionLocalProvider(LocalSmartisanModalDismiss provides dismiss) {
            Column(
                modifier =
                    modifier
                        .width(SmartisanDimens.DialogWidth)
                        .graphicsLayer {
                            // 原版弹窗进出场：0.9 倍起，缩放到 1.0 的同时淡入。
                            val scale = 0.9f + 0.1f * progress
                            scaleX = scale
                            scaleY = scale
                            alpha = progress
                        }
                        // 原版靠父容器的 clipToOutline 裁掉圆角，这里等价于 clip(10dp)。
                        .clip(RoundedCornerShape(SmartisanDimens.DialogCornerRadius))
                        .then(backgroundModifier),
                content = content,
            )
        }
    }
}

/**
 * 弹层 / 弹窗的关闭控制器。
 *
 * 它把「关闭」拆成两步：先让动画把弹层移出去（[visible] 变为 false），
 * 动画播完后再回调调用方的关闭请求，避免原版的窗口动画被立即移除打断。
 */
@Stable
internal class SmartisanOverlayController internal constructor() {
    /** 是否已经进入过组合，用于驱动进场动画。 */
    internal var entered by mutableStateOf(false)

    /** 是否已请求关闭。 */
    internal var dismissing by mutableStateOf(false)

    private var notified = false

    /** 弹层当前是否应该绘制：进场动画期间与退场动画期间都为 true。 */
    val visible: Boolean
        get() = entered && !dismissing

    /** 请求关闭，重复调用只生效一次。 */
    fun requestDismiss() {
        if (!dismissing) dismissing = true
    }

    /** 把关闭事件交回调用方，保证只回调一次。 */
    internal fun notifyDismissed(onDismissRequest: () -> Unit) {
        if (dismissing && !notified) {
            notified = true
            onDismissRequest()
        }
    }
}

/**
 * 创建 [SmartisanOverlayController]，并在退场动画结束后回调 [onDismissRequest]。
 *
 * 如果调用方在退场动画播放期间就把弹层移出了组合（例如在确认动作里顺手关掉了状态），
 * 会在离开组合时补一次回调，保证调用方状态不会停留在「界面已关闭但状态仍是打开」。
 */
@Composable
internal fun rememberSmartisanOverlayController(
    onDismissRequest: () -> Unit,
    exitDurationMillis: Int = SmartisanOverlayExitDurationMillis,
): SmartisanOverlayController {
    val controller = remember { SmartisanOverlayController() }
    LaunchedEffect(controller) { controller.entered = true }
    LaunchedEffect(controller.dismissing, exitDurationMillis) {
        if (controller.dismissing) {
            delay(exitDurationMillis.toLong())
            controller.notifyDismissed(onDismissRequest)
        }
    }
    DisposableEffect(controller) {
        onDispose { controller.notifyDismissed(onDismissRequest) }
    }
    return controller
}

/**
 * 把 [visible] 转成 0f → 1f 的动画进度。
 *
 * 进场用减速曲线、退场用加速曲线，对齐原版 `decelerate_interpolator` /
 * `accelerate_interpolator` 的观感；首帧进度为 0，因此调用方一开始就写入
 * `translationY`、`alpha`、`scale` 也不会看到闪烁。
 */
@Composable
internal fun rememberSmartisanOverlayProgress(visible: Boolean): Float {
    val progress by
        animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec =
                tween(
                    durationMillis =
                        if (visible) SmartisanOverlayEnterDurationMillis
                        else SmartisanOverlayExitDurationMillis,
                    easing = if (visible) FastOutLinearInEasing else LinearOutSlowInEasing,
                ),
            label = "smartisan overlay progress",
        )
    return progress
}

/**
 * 由 [SmartisanModalWindow] 提供的「带动画的关闭」回调。
 *
 * 弹窗内部的按钮通过它关闭窗口，这样退场动画不会被调用方的状态更新提前打断；
 * 未处于 [SmartisanModalWindow] 中时该值为 null，调用方应回退到自己的关闭回调。
 */
internal val LocalSmartisanModalDismiss: ProvidableCompositionLocal<(() -> Unit)?> =
    staticCompositionLocalOf { null }
