package cc.wuersan008.smartisanx.ui.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens

/**
 * 列表分隔线。
 *
 * 原版分隔线是 0.67dp 的极细线，并且可以按左右缩进对齐内容。
 */
@Composable
fun SmartisanDivider(
    modifier: Modifier = Modifier,
    color: Color = LocalSmartisanColors.current.divider,
    thickness: Dp = SmartisanDimens.DividerThickness,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = startIndent, end = endIndent)
                .height(thickness)
                .background(color),
    )
}

/** 分组内部使用的淡分隔线。 */
@Composable
fun SmartisanRowDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.RowContentStart,
    endIndent: Dp = 0.dp,
) {
    SmartisanDivider(
        modifier = modifier,
        color = LocalSmartisanColors.current.rowDivider,
        startIndent = startIndent,
        endIndent = endIndent,
    )
}
