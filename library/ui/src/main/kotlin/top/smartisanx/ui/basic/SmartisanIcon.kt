package top.smartisanx.ui.basic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.smartisanx.core.anim.SmartisanMotion
import top.smartisanx.core.interaction.collectSmartisanPressedAsState
import top.smartisanx.core.interaction.rememberSmartisanInteractionSource
import top.smartisanx.core.interaction.smartisanClick
import top.smartisanx.core.interaction.smartisanHaptic
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanContentColor
import top.smartisanx.core.theme.SmartisanDimens

/**
 * smartisanx 的图标。
 *
 * 图标默认跟随 `LocalSmartisanContentColor`，禁用时由调用方传入 `textDisabled`。
 */
@Composable
fun SmartisanIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp,
) {
    val colors = LocalSmartisanColors.current
    val contentColor = LocalSmartisanContentColor.current
    val resolvedTint =
        when {
            tint != Color.Unspecified -> tint
            contentColor != Color.Unspecified -> contentColor
            else -> colors.textPrimary
        }
    Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(resolvedTint),
        contentScale = ContentScale.Fit,
    )
}

/**
 * smartisanx 的图标按钮。
 *
 * 原版标题栏图标的按压反馈是「放大到 1.33 倍 + 切换按压态资源」，
 * 这里保留同样的弹簧放大与触感反馈，并且不使用涟漪。
 */
@Composable
fun SmartisanIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = SmartisanDimens.IconSize,
    contentDescription: String? = null,
    pressedScale: Float = SmartisanMotion.PressedScale,
    content: @Composable () -> Unit,
) {
    val interaction: MutableInteractionSource = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val click = smartisanClick {
        haptic()
        onClick()
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = SmartisanMotion.PressSpring,
        label = "smartisan icon press",
    )
    val colors = LocalSmartisanColors.current
    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = click,
                ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalSmartisanContentColor provides
                if (enabled) LocalSmartisanContentColor.current else colors.textDisabled,
            content = content,
        )
    }
}
