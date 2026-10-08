/**
 * 布局：锤子流式布局（自动换行）。
 *
 * 对应原版 `smartisanos.widget.letters.SurnameFlowLayout`（`ViewGroup` 子类），
 * 出现在锤子日历（Calendar 8.1.2）、时钟（Clock 7.1.1）、短信（Messages 40）、
 * 音乐（Music 8.1.0）的「姓氏选择」弹层 `surname_flow_popup.xml` 里
 * （弹层里的 `surname_content` 就是它）。
 *
 * 还原要点（照抄原版 `onMeasure` / `onLayout`）：
 * - 逐个子项累加宽度，`已用宽度 + 子项宽度 > 容器宽度` 时换行（原版 `i4 + measuredWidth > size`）；
 * - 行高取该行子项的最大高度（原版 `iMax2 = max(iMax2, measuredHeight)`）；
 * - 容器高度 = 各行高度之和；宽度在父级给定时取父级宽度，否则取最长行（原版
 *   `setMeasuredDimension(mode == EXACTLY ? size : iMax, ...)`）；
 * - 摆放时每个子项从 `行内已用宽度` 开始、纵向按行顶对齐（原版 `onLayout`）。
 *
 * 原版依赖 `MarginLayoutParams` 的四个外边距来留缝（默认 0），因此这里的
 * [itemSpacing] / [lineSpacing] 默认也是 0dp，保持与原版一致的「由子项自己控制间距」；
 * 需要额外间距时再显式传入。
 */
package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * 锤子流式布局（自动换行）。
 *
 * ```kotlin
 * SmartisanFlowLayout {
 *     surnames.forEach { SmartisanText(it, modifier = Modifier.padding(8.dp)) }
 * }
 * ```
 *
 * @param modifier 外部修饰符。
 * @param itemSpacing 同一行内相邻子项的额外间距（原版靠子项外边距，默认 0dp）。
 * @param lineSpacing 行与行之间的额外间距（原版靠子项外边距，默认 0dp）。
 * @param content 子项内容，按顺序摆放。
 */
@Composable
fun SmartisanFlowLayout(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    lineSpacing: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val itemSpacingPx = with(density) { itemSpacing.roundToPx() }
        val lineSpacingPx = with(density) { lineSpacing.roundToPx() }
        // 原版用父级给的宽度判断换行；父级宽度无限时不做换行（与 View 的 UNSPECIFIED 行为一致）。
        val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else Int.MAX_VALUE
        val childConstraints =
            Constraints(
                minWidth = 0,
                maxWidth = maxWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight,
            )
        val placeables = measurables.map { it.measure(childConstraints) }
        // 逐行摆放：先算出每行的子项与行高，再统一 layout。
        val lines = mutableListOf<MutableList<Pair<Int, Placeable>>>()
        val lineHeights = mutableListOf<Int>()
        var usedWidth = 0
        var lineHeight = 0
        var maxLineWidth = 0
        var totalHeight = 0
        var current = mutableListOf<Pair<Int, Placeable>>()
        placeables.forEach { placeable ->
            val spacing = if (current.isEmpty()) 0 else itemSpacingPx
            if (current.isNotEmpty() && usedWidth + spacing + placeable.width > maxWidth) {
                // 换行：结算上一行。
                lines += current
                lineHeights += lineHeight
                maxLineWidth = max(maxLineWidth, usedWidth)
                totalHeight += lineHeight + lineSpacingPx
                current = mutableListOf()
                usedWidth = 0
                lineHeight = 0
            }
            val offset = if (current.isEmpty()) 0 else usedWidth + itemSpacingPx
            current += offset to placeable
            usedWidth = offset + placeable.width
            lineHeight = max(lineHeight, placeable.height)
        }
        if (current.isNotEmpty()) {
            lines += current
            lineHeights += lineHeight
            maxLineWidth = max(maxLineWidth, usedWidth)
            totalHeight += lineHeight
        }
        val width = if (constraints.hasFixedWidth) constraints.maxWidth else maxLineWidth
        // 父级高度固定时撑满，否则取各行高度之和（与父级约束的收敛交给 Compose）。
        val height = if (constraints.hasFixedHeight) constraints.maxHeight else totalHeight
        layout(width = width, height = height) {
            var y = 0
            lines.forEachIndexed { lineIndex, line ->
                line.forEach { (x, placeable) -> placeable.placeRelative(x, y) }
                y += lineHeights[lineIndex] + lineSpacingPx
            }
        }
    }
}
