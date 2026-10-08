/**
 * 基础控件：锤子风格按钮。
 *
 * 合并了两处重复实现：
 * - 锤子音乐（Compose）`ui/components/SmartisanModal.kt` 的 `SmartisanDialogButton`：
 *   `shrink_long_btn_red_selector` 红色收缩按钮，高 48dp、加粗文字，
 *   按下时按钮整体收缩、投影消失（`shadow_button_shrink_shadow_selector` 的按压态没有投影）；
 * - 锤子天气（Compose）`ui/components/WeatherComponents.kt` 的 `WeatherButton`：
 *   48dp 高、左右 12dp 内边距、加粗文字、按下切换按压态颜色。
 *
 * 原实现依赖 9-patch 与 selector，这里用 Compose 重画：
 * 强调色按钮取 `accent` / `accentPressed` / `accentDisabled`，
 * 中性按钮取 `surface` / `surfacePressed` + `divider` 描边，文字按钮取 `link` / `linkPressed`。
 * 按压反馈是「缩放 + 投影变化」，不使用涟漪。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanShapes
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanProjectedShadow
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 按钮的三种样式。 */
enum class SmartisanButtonStyle {
    /** 锤子红实心按钮，对应原版 `shrink_long_btn_red_selector`。 */
    Accent,

    /** 描边中性按钮，用于次要操作。 */
    Neutral,

    /** 纯文字按钮，用于弹窗、设置行里的轻量操作。 */
    Text,
}

/** 按下时的收缩比例：原版按钮按下后 9-patch 内边距变大，视觉上整体收缩。 */
private const val ButtonPressedScale = 0.96f

/** 按钮常态投影高度，对应原版按钮下方约 4dp 的柔和投影。 */
private val ButtonElevation = 2.dp

/** 按钮左右内边距，取自锤子天气 `WeatherButton`。 */
private val ButtonHorizontalPadding = 12.dp

/** 加载指示器直径。 */
private val ButtonSpinnerSize = 16.dp

/** 加载指示器线宽。 */
private val ButtonSpinnerStroke = 2.dp

/** 加载指示器一圈的时长。 */
private const val ButtonSpinnerDurationMillis = 900

/**
 * 自绘加载指示器（不使用 Material 的进度条）。
 *
 * 原版按钮没有加载态，这里沿用锤子风格：一段 270° 的圆弧匀速旋转。
 */
@Composable
private fun SmartisanButtonSpinner(
    color: Color,
    diameter: Dp = ButtonSpinnerSize,
    strokeWidth: Dp = ButtonSpinnerStroke,
) {
    val transition = rememberInfiniteTransition(label = "smartisan button loading")
    val angle by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(ButtonSpinnerDurationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "smartisan button loading angle",
        )
    Canvas(modifier = Modifier.size(diameter)) {
        val stroke = strokeWidth.toPx()
        val radius = (size.minDimension - stroke) / 2f
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

/**
 * 按钮的公共实现：颜色、缩放、投影与加载态都在这里统一处理。
 *
 * @param contentColorOverride 仅文字按钮使用；为 `Color.Unspecified` 时按样式取默认色。
 */
@Composable
private fun SmartisanButtonSurface(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    style: SmartisanButtonStyle,
    contentColorOverride: Color,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val shapes = LocalSmartisanShapes.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val active = enabled && !loading
    val pressActive = pressed && active
    val shape: Shape = shapes.extraSmall
    val scale by
        animateFloatAsState(
            targetValue = if (pressActive) ButtonPressedScale else 1f,
            animationSpec = SmartisanMotion.easeInOut(SmartisanMotion.DurationShort),
            label = "smartisan button press scale",
        )
    val elevation by
        animateDpAsState(
            targetValue =
                if (style == SmartisanButtonStyle.Accent && !pressActive) ButtonElevation else 0.dp,
            animationSpec = SmartisanMotion.easeInOut(SmartisanMotion.DurationShort),
            label = "smartisan button press elevation",
        )
    val background =
        when (style) {
            SmartisanButtonStyle.Accent ->
                when {
                    !enabled -> colors.accentDisabled
                    pressActive -> colors.accentPressed
                    else -> colors.accent
                }

            SmartisanButtonStyle.Neutral ->
                when {
                    !enabled -> colors.surfaceDisabled
                    pressActive -> colors.surfacePressed
                    else -> colors.surface
                }

            SmartisanButtonStyle.Text -> Color.Transparent
        }
    val contentColor =
        when {
            contentColorOverride != Color.Unspecified ->
                if (enabled) contentColorOverride else colors.textDisabled

            style == SmartisanButtonStyle.Accent -> colors.onAccent

            style == SmartisanButtonStyle.Neutral ->
                if (enabled) colors.textPrimary else colors.textDisabled

            !enabled -> colors.textDisabled
            pressActive -> colors.linkPressed
            else -> colors.link
        }
    val textStyle =
        if (style == SmartisanButtonStyle.Accent) typography.dialogButton else typography.button
    Box(
        modifier =
            modifier
                .heightIn(min = SmartisanDimens.DialogButtonHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .smartisanProjectedShadow(elevation = elevation, shape = shape)
                .background(color = background, shape = shape)
                .then(
                    if (style == SmartisanButtonStyle.Neutral) {
                        Modifier.border(
                            border = BorderStroke(SmartisanDimens.DividerThickness, colors.divider),
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = active,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = ButtonHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (loading) {
                SmartisanButtonSpinner(color = contentColor)
            }
            SmartisanText(
                text = text,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/**
 * 锤子风格按钮。
 *
 * ```kotlin
 * SmartisanButton("确定", onClick = { /* ... */ })
 * SmartisanButton("取消", onClick = { /* ... */ }, style = SmartisanButtonStyle.Neutral)
 * ```
 *
 * @param loading 加载中：显示自绘旋转指示器，并暂时屏蔽点击。
 */
@Composable
fun SmartisanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: SmartisanButtonStyle = SmartisanButtonStyle.Accent,
    loading: Boolean = false,
) {
    SmartisanButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        style = style,
        contentColorOverride = Color.Unspecified,
    )
}

/**
 * 纯文字按钮。
 *
 * @param color 文字颜色，默认取主题的 `link`；禁用时统一为 `textDisabled`。
 */
@Composable
fun SmartisanTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color.Unspecified,
) {
    SmartisanButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = false,
        style = SmartisanButtonStyle.Text,
        contentColorOverride = color,
    )
}

