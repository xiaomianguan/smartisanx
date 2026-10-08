package cc.wuersan008.smartisanx.ui.overlay

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 底部菜单弹层（贴底、全宽）。
 *
 * 复刻来源：锤子时钟 `widget/SmartisanMenuDialog.kt` + `res/layout/smartisan_menu_dialog.xml`
 * （贴底全宽窗口、48dp 标题栏、动作区留白、动作项 18dp 间距、可选红色通栏确认按钮）。
 *
 * 合并点：原版由 `Dialog` + `LinearLayout` 动态 addView 拼装，这里改成 Compose 组合：
 * 标题栏与动作项都是可复用的公开组件，调用方直接用 [SmartisanMenuItem] 描述动作即可。
 * 质感全部来自原版资源：标题栏底色 `bottom_sheet_title_bar_bg`，内容区底色
 * `menu_dialog_background`，标题栏关闭图标 `standard_icon_cancel_selector`，
 * 动作项按压底图 `menu_item_selector`。
 */

/**
 * 菜单项点击时自动关闭所属菜单的回调，由 [SmartisanMenuDialog] 提供。
 *
 * 未处于菜单弹层中时为 null，此时菜单项只执行自己的 [SmartisanMenuItem.onClick]。
 */
internal val LocalSmartisanMenuDismiss: ProvidableCompositionLocal<(() -> Unit)?> =
    staticCompositionLocalOf { null }

/**
 * 底部菜单弹层：贴底、全宽，结构与尺寸严格对齐原版 `res/layout/smartisan_menu_dialog.xml`
 * 与 `res/values/smartisan_menu_values.xml`：
 *
 * - 标题栏：48dp 高、`bottom_sheet_title_bar_bg` 底图、标题 13.5sp 加粗，下面一条 1px `smartisan_menu_divider`；
 * - 动作区：`menu_dialog_background` 底图，左右 18dp（`smartisan_menu_horizontal_margin`）、
 *   上下 24dp（`smartisan_menu_action_edge_margin`）留白，动作项间距 18dp（`smartisan_menu_action_gap`）；
 * - 确认区（[confirmText] 不为 null 时）：`menu_dialog_background` 底图，左右 18dp、上 18dp、下 24dp 留白，
 *   里面是 48dp 高、17sp 白字的红色通栏按钮（`smartisan_menu_confirm_background` + `smartisan_menu_confirm_shadow`）。
 *
 * 进出场动画对齐原版 `smartisan_menu_enter` / `smartisan_menu_exit`（整层贴底上下位移），
 * 关闭请求会等退场动画播完再回调 [onDismissRequest]。
 *
 * ```kotlin
 * if (menuVisible) {
 *     SmartisanMenuDialog(onDismissRequest = { menuVisible = false }, title = "计时器") {
 *         SmartisanMenuItem("重命名", onClick = { rename() })
 *         SmartisanMenuItem("删除", danger = true, onClick = { delete() })
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 关闭回调（点击遮罩、返回键、标题栏取消、或点击某个动作项）。
 * @param title 标题；为 null 时不显示标题栏。
 * @param modifier 作用于菜单面板。
 * @param backgroundRes 内容区底色；默认用原版 `menu_dialog_background`（含夜间变体），
 *   传 null 时退回色板的 `surfaceRaised`。
 * @param confirmText 通栏确认按钮文字；为 null 时不显示确认区。
 * @param confirmEnabled 确认按钮是否可用。
 * @param onConfirm 确认回调；与原版一致，先关闭菜单再执行动作。
 * @param content 菜单内容，纵向排列，[SmartisanMenuItem] 之间会自动留出 18dp 间距。
 */
@Composable
fun SmartisanMenuDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.MenuDialogBackground,
    confirmText: String? = null,
    confirmEnabled: Boolean = true,
    onConfirm: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val controller = rememberSmartisanOverlayController(onDismissRequest)
    val progress = rememberSmartisanOverlayProgress(controller.visible)
    val dismiss = controller::requestDismiss
    val confirm: () -> Unit = {
        // 与原版一致：先关闭菜单，再执行确认动作。
        dismiss()
        onConfirm?.invoke()
    }
    // 动作区与确认区共用原版 menu_dialog_background 底色。
    val contentBackgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.surfaceRaised)
        }
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
                        },
            ) {
                if (title != null) {
                    SmartisanDialogTitleBar(title = title, onDismiss = dismiss)
                    // 原版标题栏下面是 1px 的 smartisan_menu_divider。
                    SmartisanDivider(
                        color = colorResource(R.color.smartisan_menu_divider),
                        thickness = smartisanOnePixel(),
                    )
                }
                // 动作区：原版 smartisan_menu_actions，左右 18dp、上下 24dp 留白，动作间距 18dp。
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .then(contentBackgroundModifier)
                            .padding(
                                horizontal = SmartisanDimens.MenuHorizontalMargin,
                                vertical = SmartisanDimens.MenuActionEdgeMargin,
                            ),
                    verticalArrangement = Arrangement.spacedBy(SmartisanDimens.MenuActionGap),
                ) {
                    content()
                }
                if (confirmText != null) {
                    // 确认区：原版 smartisan_menu_confirm_panel，左右 18dp、上 18dp、下 24dp 留白。
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .then(contentBackgroundModifier)
                                .padding(
                                    start = SmartisanDimens.MenuHorizontalMargin,
                                    end = SmartisanDimens.MenuHorizontalMargin,
                                    top = SmartisanDimens.MenuButtonTopMargin,
                                    bottom = SmartisanDimens.MenuButtonBottomMargin,
                                ),
                    ) {
                        // 原版红色通栏按钮：底图 + 配套投影 selector，17sp 白字。
                        SmartisanDialogButton(
                            text = confirmText,
                            onClick = confirm,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = confirmEnabled,
                            accent = true,
                            backgroundRes = SmartisanDrawables.DialogConfirmBackground,
                            shadowRes = SmartisanDrawables.DialogConfirmShadow,
                            contentColor = Color.White,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 菜单动作项：高 48dp、17sp 加粗、文字居中，对应原版 `item_smartisan_menu_action`。
 *
 * 不使用涟漪：按压时由原版 `menu_item_selector`（`action_menu_grid_bg` +
 * `action_menu_grid_bg_pressed` 与 1px 描边）切换底色，并附带系统点击音与触感反馈。
 * 与原版一致，点击后会先关闭所属的 [SmartisanMenuDialog]，再执行 [onClick]。
 *
 * @param text 动作文字。
 * @param onClick 点击回调。
 * @param modifier 作用于整行容器。
 * @param enabled 是否可用；禁用时按原版做法整体降低透明度，文字取 `textDisabled`。
 * @param showDivider 是否在动作项下方绘制分隔线。
 * @param danger 是否使用强调色文字（用于删除等危险动作）。
 * @param backgroundRes 动作项底图；默认用原版 `menu_item_selector`，传 null 时退回主题色。
 */
@Composable
fun SmartisanMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showDivider: Boolean = true,
    danger: Boolean = false,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.MenuItemSelector,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val dismiss = LocalSmartisanMenuDismiss.current
    // 原版按压底图：常态 / 按下态由 selector 自己切换。
    val itemBackground =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(
                backgroundRes,
                enabled = enabled,
                pressed = pressed,
            )
        } else {
            Modifier.background(
                when {
                    !enabled -> colors.surfaceDisabled
                    pressed -> colors.surfacePressed
                    else -> Color.Transparent
                }
            )
        }
    // 原版 selector 没有禁用态，禁用时按原版做法整体降低透明度。
    val itemAlpha = if (backgroundRes != null && !enabled) 0.35f else 1f
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
                    .alpha(itemAlpha)
                    .then(itemBackground)
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
