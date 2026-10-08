package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 单个标题栏图标。
 *
 * 还原原版行为：按压时整体放大到 1.33 倍（弹簧回弹），同时把按下态传给 selector
 * 切换图标资源；点击播放系统音效并触发虚拟按键触感。
 */
@Composable
internal fun SmartisanTitleBarIcon(item: SmartisanTitleBarAction) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val click =
        smartisanClick {
            haptic()
            item.onClick()
        }
    val scale by animateFloatAsState(
        targetValue = if (pressed && item.enabled) SmartisanMotion.PressedScale else 1f,
        animationSpec = SmartisanMotion.PressSpring,
        label = "smartisan title icon press",
    )
    Box(
        modifier =
            Modifier
                .size(SmartisanDimens.IconSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = item.enabled,
                    role = Role.Button,
                    onClick = click,
                ),
        contentAlignment = Alignment.Center,
    ) {
        val res = item.iconRes
        if (res != null) {
            // 原版图标按位图固有尺寸绘制，不额外缩放。
            SmartisanIcon(
                res = res,
                contentDescription = item.contentDescription,
                enabled = item.enabled,
                pressed = pressed,
                modifier = Modifier.size(SmartisanDimens.IconSize),
                contentScale = ContentScale.None,
            )
        } else {
            SmartisanIcon(
                imageVector = requireNotNull(item.imageVector) { "SmartisanTitleBarAction 缺少图标" },
                contentDescription = item.contentDescription,
                tint = if (item.enabled) colors.textSecondary else colors.textDisabled,
                size = 24.dp,
            )
        }
    }
}
