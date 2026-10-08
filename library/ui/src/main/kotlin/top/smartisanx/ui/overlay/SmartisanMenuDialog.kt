package top.smartisanx.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.rememberSmartisanInteractionSource
import top.smartisanx.core.interaction.smartisanClickable
import top.smartisanx.core.interaction.smartisanHaptic
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanDivider
import top.smartisanx.ui.basic.SmartisanText

/**
 * 底部菜单弹层（贴底、全宽）。
 *
 * 复刻来源：锤子时钟 `widget/SmartisanMenuDialog.kt`（贴底全宽窗口、48dp 标题栏、
 * 动作区留白、动作项 18dp 间距），并结合锤子音乐底部弹层的「取消」标题栏按钮。
 *
 * 合并点：原版由 `Dialog` + `LinearLayout` 动态 addView 拼装，这里改成 Compose 组合：
 * 标题栏与动作项都是可复用的公开组件，调用方直接用 [SmartisanMenuItem] 描述动作即可。
 */

/**
 * 菜单项点击时自动关闭所属菜单的回调，由 [SmartisanMenuDialog] 提供。
 *
 * 未处于菜单弹层中时为 null，此时菜单项只执行自己的 [SmartisanMenuItem.onClick]。
 */
internal val LocalSmartisanMenuDismiss: ProvidableCompositionLocal<(() -> Unit)?> =
    staticCompositionLocalOf { null }

/**
 * 底部菜单弹层：贴底、全宽，标题栏 48dp（可选），动作区左右 24dp 留白、动作间距 18dp。
 *
 * 进出场动画对齐原版 `smartisan_menu_enter` / `smartisan_menu_exit`（整层贴底上下位移），
 * 关闭请求会等退场动画播完再回调 [onDismissRequest]。
 *
 * ```kotlin
 * if (menuVisible) {
 *     SmartisanMenuDialog(onDismissRequest = { menuVisible = false }, title = "计时器") {
 *         SmartisanMenuItem("重命名") { rename() }
 *         SmartisanMenuItem("删除", danger = true) { delete() }
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 关闭回调（点击遮罩、返回键、标题栏取消、或点击某个动作项）。
 * @param title 标题；为 null 时不显示标题栏。
 * @param modifier 作用于菜单面板。
 * @param content 菜单内容，纵向排列，[SmartisanMenuItem] 之间会自动留出 18dp 间距。
 */
@Composable
fun SmartisanMenuDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val controller = rememberSmartisanOverlayController(onDismissRequest)
    val progress = rememberSmartisanOverlayProgress(controller.visible)
    val dismiss = controller::requestDismiss
    CompositionLocalProvider(LocalSmartisanMenuDismiss provides dismiss) {
        SmartisanModal(
            onDismissRequest = dismiss,
            modifier = modifier,
            bottom = true,
            dimAmount = 0.54f,
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .graphicsLayer {
                            // 原版菜单进场：整层从屏幕下方滑入。
                            translationY = (1f - progress) * size.height
                        }
                        .background(colors.surface),
            ) {
                if (title != null) {
                    SmartisanDialogTitleBar(title = title, onDismiss = dismiss)
                    SmartisanDivider(color = colors.divider)
                }
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .background(colors.surfaceRaised)
                            .padding(
                                horizontal = SmartisanDimens.MenuActionEdgeMargin,
                                vertical = SmartisanDimens.MenuActionEdgeMargin,
                            ),
                    verticalArrangement = Arrangement.spacedBy(SmartisanDimens.MenuActionGap),
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 菜单动作项：高 48dp、17sp 加粗、文字居中，对应原版 `item_smartisan_menu_action`。
 *
 * 不使用涟漪：按压时整行底色切换成 `surfacePressed`，并附带系统点击音与触感反馈。
 * 与原版一致，点击后会先关闭所属的 [SmartisanMenuDialog]，再执行 [onClick]。
 *
 * @param text 动作文字。
 * @param onClick 点击回调。
 * @param modifier 作用于整行容器。
 * @param enabled 是否可用，禁用时使用 `surfaceDisabled` 底色与 `textDisabled` 文字。
 * @param showDivider 是否在动作项下方绘制分隔线。
 * @param danger 是否使用强调色文字（用于删除等危险动作）。
 */
@Composable
fun SmartisanMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showDivider: Boolean = true,
    danger: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val dismiss = LocalSmartisanMenuDismiss.current
    val background =
        when {
            !enabled -> colors.surfaceDisabled
            pressed -> colors.surfacePressed
            else -> Color.Transparent
        }
    val textColor =
        when {
            !enabled -> colors.textDisabled
            danger -> colors.accent
            else -> colors.textSecondary
        }
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(SmartisanDimens.DialogButtonHeight)
                    .background(background)
                    .smartisanClickable(interactionSource = interaction, enabled = enabled) {
                        haptic()
                        dismiss?.invoke()
                        onClick()
                    },
            contentAlignment = Alignment.Center,
        ) {
            SmartisanText(
                text = text,
                style = typography.dialogButton,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (showDivider) {
            SmartisanDivider(color = colors.rowDivider)
        }
    }
}
