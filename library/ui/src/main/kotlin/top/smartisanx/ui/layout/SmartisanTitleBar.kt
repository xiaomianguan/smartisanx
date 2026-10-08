package top.smartisanx.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanIcon
import top.smartisanx.ui.basic.SmartisanIconButton
import top.smartisanx.ui.basic.SmartisanText

/**
 * 标题栏动作项。
 *
 * 合并了锤子音乐的 `SmartisanTitleBarAction` 与锤子天气的 `WeatherIconButton`：
 * 两者都是「36dp 图标 + 1.33 倍按压放大 + 按压态颜色」。
 */
@Immutable
data class SmartisanTitleBarAction(
    /** 图标。 */
    val icon: ImageVector,
    /** 无障碍描述。 */
    val contentDescription: String,
    /** 点击回调。 */
    val onClick: () -> Unit,
    /** 是否可用，禁用时使用 `textDisabled` 颜色。 */
    val enabled: Boolean = true,
)

/**
 * 锤子风格标题栏。
 *
 * 视觉来自锤子音乐 `SmartisanTitleBar` 与锤子天气 `WeatherTitleBar`（两者布局一致，
 * 这里合并为一个实现）：左右 6dp 留白、36dp 图标、20sp 加粗居中标题、
 * 下方 14dp 渐变投影、可选状态栏占位。
 */
@Composable
fun SmartisanTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: SmartisanTitleBarAction? = null,
    action: SmartisanTitleBarAction? = null,
    navigationActions: List<SmartisanTitleBarAction> = emptyList(),
    actions: List<SmartisanTitleBarAction> = emptyList(),
    includeStatusBar: Boolean = true,
    showShadow: Boolean = true,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    centerContent: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val leftActions = listOfNotNull(navigationIcon) + navigationActions
    val rightActions = listOfNotNull(action) + actions
    val hasIcons = leftActions.isNotEmpty() || rightActions.isNotEmpty()

    Column(modifier.fillMaxWidth().background(colors.titleBarBackground)) {
        if (includeStatusBar) {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
        }
        Box(Modifier.fillMaxWidth().height(contentHeight)) {
            if (centerContent != null) {
                Box(Modifier.align(Alignment.Center)) { centerContent() }
            } else {
                SmartisanText(
                    text = title,
                    modifier =
                        Modifier.align(Alignment.Center)
                            .padding(horizontal = if (hasIcons) 60.dp else 0.dp),
                    style = typography.titleBar,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
            Row(
                modifier =
                    Modifier.align(Alignment.CenterStart)
                        .padding(start = SmartisanDimens.TitleBarHorizontalMargin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leftActions.forEach { item -> SmartisanTitleBarIcon(item) }
            }
            Row(
                modifier =
                    Modifier.align(Alignment.CenterEnd)
                        .padding(end = SmartisanDimens.TitleBarHorizontalMargin),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rightActions.forEach { item -> SmartisanTitleBarIcon(item) }
            }
        }
        if (showShadow) {
            SmartisanTitleBarShadow()
        }
    }
}

/** 标题栏下方的渐变投影。 */
@Composable
fun SmartisanTitleBarShadow(
    modifier: Modifier = Modifier,
    height: Dp = SmartisanDimens.TitleBarShadowHeight,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0x14000000), Color.Transparent),
                ),
            ),
    )
}

/** 单个标题栏图标按钮。 */
@Composable
private fun SmartisanTitleBarIcon(item: SmartisanTitleBarAction) {
    val colors = LocalSmartisanColors.current
    SmartisanIconButton(
        onClick = item.onClick,
        enabled = item.enabled,
        size = SmartisanDimens.IconSize,
        contentDescription = item.contentDescription,
    ) {
        SmartisanIcon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = if (item.enabled) colors.textSecondary else colors.textDisabled,
            size = 24.dp,
        )
    }
}
