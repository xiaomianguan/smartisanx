package top.smartisanx.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanShapes
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanRowDivider
import top.smartisanx.ui.basic.SmartisanText

/**
 * 分组标题。
 *
 * 对应原版设置页、资料库里的灰色小标题（13.5sp，`textTertiary`）。
 */
@Composable
fun SmartisanSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.RowContentStart,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanText(
        text = text,
        modifier = modifier.fillMaxWidth().padding(start = startIndent, end = startIndent, top = 16.dp, bottom = 6.dp),
        style = typography.sectionTitle,
        color = colors.textTertiary,
        maxLines = 1,
    )
}

/**
 * 分组容器。
 *
 * 原版把一组设置项放在同一张白底卡片里，卡片之间留出间距；
 * 深色模式下卡片是 `surface`（炭灰 `#34373C`）。
 */
@Composable
fun SmartisanGroup(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.none,
    color: Color = LocalSmartisanColors.current.surface,
    horizontalMargin: Dp = SmartisanDimens.ListItemHorizontalMargin,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalMargin, vertical = 6.dp)
                .background(color = color, shape = shape),
        content = content,
    )
}

/** 分组内的分隔线，左右缩进与内容对齐。 */
@Composable
fun SmartisanGroupDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.RowContentStart,
) {
    SmartisanRowDivider(modifier = modifier, startIndent = startIndent)
}

/**
 * 卡片容器。
 *
 * 用于天气卡片、专辑卡片这类有描边和圆角的块。
 */
@Composable
fun SmartisanCard(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.medium,
    color: Color = LocalSmartisanColors.current.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().background(color = color, shape = shape),
        content = content,
    )
}
