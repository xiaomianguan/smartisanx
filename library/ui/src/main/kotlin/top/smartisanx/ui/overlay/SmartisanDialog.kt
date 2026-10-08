package top.smartisanx.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.rememberSmartisanInteractionSource
import top.smartisanx.core.interaction.smartisanClickable
import top.smartisanx.core.interaction.smartisanHaptic
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens

/**
 * 居中弹窗的标题栏与按钮，以及由它们拼出的弹窗。
 *
 * 复刻来源：
 * - 锤子时钟 `widget/SmartisanModalDialog.kt`：48dp 标题栏、居中加粗标题、48dp 按钮、308dp 弹窗宽度；
 * - 锤子音乐 `ui/components/SmartisanModal.kt` 的 `SmartisanMenuTitleBar` 与 `SmartisanDialogButton`：
 *   标题栏左右文字按钮的排布，以及「红色长按钮 / 通栏确认按钮」的按压反馈。
 *
 * 合并点：两个项目各自实现了一份「标题栏 + 底部按钮」的弹窗骨架（一份用 XML + Dialog，
 * 一份用 Compose + drawable），这里合并成一套纯 Compose 实现，颜色与尺寸全部走主题与
 * [SmartisanDimens]，不再依赖 drawable 资源。
 */

/** 标题栏左右文字按钮的宽度，保证至少 48dp 的可点区域。 */
private val DialogTitleBarSideWidth = 56.dp

/** 标题左右留白：让出两侧按钮的宽度再留一点余量。 */
private val DialogTitleHorizontalPadding = DialogTitleBarSideWidth + 8.dp

/** 弹窗内容的默认左右留白，对齐原版弹窗内容区的内缩。 */
private val DialogContentHorizontalPadding = 18.dp

/** 弹窗内容的默认上下留白。 */
private val DialogContentVerticalPadding = 16.dp

/**
 * 弹窗标题栏：高 48dp，标题居中、13.5sp 加粗，左右是「取消 / 确定」文字按钮。
 *
 * 按钮的显隐对齐锤子音乐标题栏的图标逻辑：
 * - [onConfirm] 为 null（例如只需要一个关闭入口的弹层）：右侧只显示「取消」，点击 [onDismiss]；
 * - [onConfirm] 不为 null：左侧「取消」、右侧「确定」，确定按钮由 [confirmEnabled] 控制可用性。
 *
 * @param title 标题文字。
 * @param onDismiss 取消 / 关闭回调。
 * @param onConfirm 确认回调；为 null 时不显示左侧「取消」，右侧改为「取消」。
 * @param confirmEnabled 「确定」按钮是否可用，禁用时用 `textDisabled` 绘制。
 * @param modifier 作用于标题栏容器。
 */
@Composable
fun SmartisanDialogTitleBar(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Box(
        modifier = modifier.fillMaxWidth().height(SmartisanDimens.DialogTitleHeight),
    ) {
        SmartisanText(
            text = title,
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(horizontal = DialogTitleHorizontalPadding),
            style = typography.dialogTitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (onConfirm != null) {
            SmartisanDialogTextButton(
                text = "取消",
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
        SmartisanDialogTextButton(
            text = if (onConfirm == null) "取消" else "确定",
            onClick = onConfirm ?: onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd),
            enabled = onConfirm == null || confirmEnabled,
            accent = onConfirm != null,
        )
    }
}

/**
 * 标题栏里的文字按钮：不使用涟漪，按压时只切换文字颜色，并带系统点击音与触感反馈。
 *
 * @param text 按钮文字。
 * @param onClick 点击回调。
 * @param modifier 作用于按钮容器。
 * @param enabled 是否可用，禁用时用 `textDisabled` 绘制。
 * @param accent 是否使用强调色文字（用于「确定」）。
 */
@Composable
private fun SmartisanDialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val textColor =
        when {
            !enabled -> colors.textDisabled
            pressed -> if (accent) colors.accentPressed else colors.textPrimary
            accent -> colors.accent
            else -> colors.textSecondary
        }
    Box(
        modifier =
            modifier
                .width(DialogTitleBarSideWidth)
                .fillMaxHeight()
                .smartisanClickable(interactionSource = interaction, enabled = enabled) {
                    haptic()
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            style = typography.dialogTitle,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}


/**
 * 弹窗底部按钮：高 48dp、17sp 加粗、单行居中。
 *
 * [accent] 为 true 时是原版的红色长按钮（强调色底 + `onAccent` 文字），
 * 否则是表面色底 + `textPrimary` 文字。按压时切换底色（`accentPressed` / `surfacePressed`），
 * 不使用涟漪，并附带系统点击音与触感反馈。
 *
 * @param text 按钮文字。
 * @param onClick 点击回调。
 * @param modifier 作用于按钮容器，通常配合 `fillMaxWidth()` 或 `weight(1f)` 使用。
 * @param enabled 是否可用，禁用时使用 `accentDisabled` / `surfaceDisabled` 底色。
 * @param accent 是否使用强调色实底样式。
 */
@Composable
fun SmartisanDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val background =
        when {
            !enabled -> if (accent) colors.accentDisabled else colors.surfaceDisabled
            pressed -> if (accent) colors.accentPressed else colors.surfacePressed
            accent -> colors.accent
            else -> colors.surface
        }
    val textColor =
        when {
            !enabled -> if (accent) colors.onAccent else colors.textDisabled
            accent -> colors.onAccent
            else -> colors.textPrimary
        }
    Box(
        modifier =
            modifier
                .height(SmartisanDimens.DialogButtonHeight)
                .background(background)
                .smartisanClickable(interactionSource = interaction, enabled = enabled) {
                    haptic()
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
}


/**
 * 居中弹窗：标题栏 + 内容 + 底部按钮，结构与尺寸对齐锤子时钟的 `SmartisanModalDialog`。
 *
 * 底部按钮有两种排布：
 * - [dismissText] 为 null（默认）：只显示一个通栏确认按钮，对齐锤子音乐的删除确认弹层；
 * - [dismissText] 非 null：显示「取消 | 确定」两个等宽按钮，中间一条 1px 分隔线，对齐锤子时钟。
 *
 * 确认时先关闭弹窗再执行 [onConfirm]，与原版 `dismiss()` 之后再执行动作的顺序一致。
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanDialog(
 *         onDismissRequest = { visible = false },
 *         title = "删除录音",
 *         confirmText = "删除",
 *         dismissText = "取消",
 *         onConfirm = { delete() },
 *     ) {
 *         SmartisanText("删除后无法恢复。", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 关闭回调（点击遮罩、返回键、取消或确认）。
 * @param title 标题。
 * @param modifier 作用于弹窗面板。
 * @param confirmText 确认按钮文字。
 * @param dismissText 取消按钮文字；为 null 时不显示取消按钮。
 * @param confirmEnabled 确认按钮是否可用。
 * @param onConfirm 确认回调。
 * @param content 弹窗内容，纵向排列，默认带 18dp 左右、16dp 上下留白。
 */
@Composable
fun SmartisanDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    confirmText: String = "确定",
    dismissText: String? = null,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val dismiss = LocalSmartisanModalDismiss.current ?: onDismissRequest
    val confirm = {
        // 与原版一致：先关闭弹窗，再执行确认动作。
        dismiss()
        onConfirm()
    }
    SmartisanModalWindow(onDismissRequest = onDismissRequest, modifier = modifier) {
        SmartisanDialogTitleBar(
            title = title,
            onDismiss = dismiss,
            onConfirm = null,
            confirmEnabled = confirmEnabled,
        )
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = DialogContentHorizontalPadding,
                        vertical = DialogContentVerticalPadding,
                    ),
        ) {
            content()
        }
        if (dismissText == null) {
            SmartisanDialogButton(
                text = confirmText,
                onClick = confirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = confirmEnabled,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(SmartisanDimens.DialogButtonHeight),
            ) {
                SmartisanDialogButton(
                    text = dismissText,
                    onClick = dismiss,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    accent = false,
                )
                Box(
                    modifier =
                        Modifier.fillMaxHeight()
                            .width(SmartisanDimens.DividerThickness)
                            .background(colors.divider),
                )
                SmartisanDialogButton(
                    text = confirmText,
                    onClick = confirm,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    enabled = confirmEnabled,
                )
            }
        }
    }
}

/**
 * 确认弹窗：在 [SmartisanDialog] 的基础上加一段居中说明文字。
 *
 * ```kotlin
 * SmartisanConfirmDialog(
 *     onDismissRequest = { visible = false },
 *     title = "清空数据",
 *     message = "所有记录都会被删除，且无法恢复。",
 *     onConfirm = { clear() },
 * )
 * ```
 *
 * @param onDismissRequest 关闭回调。
 * @param title 标题。
 * @param message 说明文字。
 * @param confirmText 确认按钮文字。
 * @param dismissText 取消按钮文字。
 * @param onConfirm 确认回调。
 */
@Composable
fun SmartisanConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确定",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
    ) {
        SmartisanText(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            style = typography.body,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}