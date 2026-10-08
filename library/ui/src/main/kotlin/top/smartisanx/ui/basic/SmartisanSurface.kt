package top.smartisanx.ui.basic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanContentColor

/**
 * smartisanx 的基础容器。
 *
 * 负责底色、描边与裁剪形状，并把内容默认色写入 `LocalSmartisanContentColor`，
 * 让其中的 [SmartisanText] 自动获得正确的前景色。
 */
@Composable
fun SmartisanSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = LocalSmartisanColors.current.surface,
    contentColor: Color = smartisanContentColorFor(color),
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .background(color = color, shape = shape),
    ) {
        CompositionLocalProvider(LocalSmartisanContentColor provides contentColor) {
            content()
        }
    }
}

/** 根据底色推导合适的内容色：强调色上用白字，其余用一级文字色。 */
@Composable
fun smartisanContentColorFor(background: Color): Color {
    val colors = LocalSmartisanColors.current
    val onAccent =
        background == colors.accent ||
            background == colors.accentPressed ||
            background == colors.pressedHighlight ||
            background == colors.link
    return if (onAccent) colors.onAccent else colors.textPrimary
}
