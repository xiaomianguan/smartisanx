package top.smartisanx.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanShapes
import top.smartisanx.ui.basic.SmartisanDivider

/**
 * 底部弹层（带动画的 Sheet）。
 *
 * 复刻来源：锤子音乐 `ui/components/SmartisanAnimatedSheet.kt`（遮罩与面板各自做进出场动画、
 * 面板从底部滑入滑出）与锤子时钟 `anim/smartisan_menu_enter.xml` / `smartisan_menu_exit.xml`
 * 的时长与插值器（进场 300ms 减速、退场 250ms 加速）。
 *
 * 合并点：原版一处是「页面内的可见性切换」，一处是「独立 Dialog 窗口」；
 * 这里把两者拆成两个入口，共用同一套动画数值：
 * - [SmartisanBottomSheet]：独立窗口（`Dialog`）版，适合任意页面直接调用；
 * - [SmartisanSheetScaffold]：页面内嵌版，由调用方传入 `visible`，可以放在任意 `Box` 里，
 *   并且能真正播放退场动画（内容在动画期间仍留在组合中）。
 */

/**
 * 底部弹层窗口：贴底、全宽，圆角取自 `shapes.sheet`，底色为色板的 `surface`。
 *
 * 进入时从屏幕下方滑入，关闭时先滑出、动画播完后再回调 [onDismissRequest]，
 * 因此调用方只需要用状态控制组合即可：
 *
 * ```kotlin
 * if (sheetVisible) {
 *     SmartisanBottomSheet(
 *         onDismissRequest = { sheetVisible = false },
 *         title = "选择音质",
 *     ) {
 *         SmartisanMenuItem("标准", showDivider = false) { pick(0) }
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 关闭回调（点击遮罩、返回键、标题栏取消，均在退场动画之后触发）。
 * @param modifier 作用于弹层面板。
 * @param title 标题；为 null 时不显示标题栏。
 * @param content 弹层内容，纵向排列，留白由调用方控制（标题栏自带 48dp 高度与分隔线）。
 */
@Composable
fun SmartisanBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val shapes = LocalSmartisanShapes.current
    val controller = rememberSmartisanOverlayController(onDismissRequest)
    val progress = rememberSmartisanOverlayProgress(controller.visible)
    val dismiss = controller.requestDismiss
    SmartisanModal(
        onDismissRequest = dismiss,
        bottom = true,
        dimAmount = 0.54f,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 原版弹层进出场：整层从屏幕下方滑入滑出。
                        translationY = (1f - progress) * size.height
                    }
                    .clip(shapes.sheet)
                    .background(colors.surface),
        ) {
            if (title != null) {
                SmartisanDialogTitleBar(title = title, onDismiss = dismiss)
                SmartisanDivider(color = colors.divider)
            }
            content()
        }
    }
}


/**
 * 页面内嵌的弹层脚手架：遮罩 + 贴底面板，自己管理进场与退场动画。
 *
 * 与 [SmartisanBottomSheet] 的区别是不创建 `Dialog` 窗口，而是铺满调用方给的父容器，
 * 因此要放在一个「占满屏幕的容器」（例如页面根 `Box`）里，通常作为最后一个子节点：
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     SmartisanPage()
 *     SmartisanSheetScaffold(
 *         visible = sheetVisible,
 *         onDismissRequest = { sheetVisible = false },
 *         title = "选择音质",
 *     ) {
 *         SmartisanMenuItem("标准", showDivider = false) { pick(0) }
 *     }
 * }
 * ```
 *
 * 遮罩与面板分别做淡入淡出、滑入滑出；退场时内容仍留在组合中，因此能播完整的退场动画。
 *
 * @param visible 是否显示弹层。
 * @param onDismissRequest 点击遮罩时的回调，由调用方把 [visible] 置为 false。
 * @param modifier 作用于整个弹层容器。
 * @param title 标题；为 null 时不显示标题栏。
 * @param scrimColor 遮罩颜色，默认取色板的 `scrim`。
 * @param content 弹层内容，纵向排列。
 */
@Composable
fun SmartisanSheetScaffold(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    scrimColor: Color = LocalSmartisanColors.current.scrim,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val shapes = LocalSmartisanShapes.current
    // 首帧不可见，之后跟随 visible；退场时 AnimatedVisibility 会继续持有内容直到动画结束。
    val transition = remember { MutableTransitionState(false) }
    transition.targetState = visible
    AnimatedVisibility(
        visibleState = transition,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        Box(Modifier.fillMaxSize()) {
            // 遮罩单独做淡入淡出：若跟着父级一起淡，会把面板的透明度也乘一遍。
            Box(
                Modifier.matchParentSize()
                    .animateEnterExit(
                        enter =
                            fadeIn(
                                tween(
                                    SmartisanOverlayEnterDurationMillis,
                                    easing = FastOutLinearInEasing,
                                )
                            ),
                        exit =
                            fadeOut(
                                tween(
                                    SmartisanOverlayExitDurationMillis,
                                    easing = LinearOutSlowInEasing,
                                )
                            ),
                    )
                    .background(scrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    ),
            )
            Column(
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .animateEnterExit(
                            enter =
                                slideInVertically(
                                    tween(
                                        SmartisanOverlayEnterDurationMillis,
                                        easing = FastOutLinearInEasing,
                                    )
                                ) { height -> height } +
                                    fadeIn(tween(SmartisanOverlayEnterDurationMillis)),
                            exit =
                                slideOutVertically(
                                    tween(
                                        SmartisanOverlayExitDurationMillis,
                                        easing = LinearOutSlowInEasing,
                                    )
                                ) { height -> height } +
                                    fadeOut(tween(SmartisanOverlayExitDurationMillis)),
                        )
                        .clip(shapes.sheet)
                        .background(colors.surface)
                        // 吃掉面板上的点击，避免穿透到遮罩把弹层关掉。
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
            ) {
                if (title != null) {
                    SmartisanDialogTitleBar(title = title, onDismiss = onDismissRequest)
                    SmartisanDivider(color = colors.divider)
                }
                content()
            }
        }
    }
}
