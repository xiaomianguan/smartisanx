package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens

/**
 * 锤子风格竖向滚动条。
 *
 * **这是本库自有的组件，原版没有对应位图。**
 * 锤子音乐的 `SmartisanScrollbar` 读取的是 `android:scrollbarThumbVertical` 属性，
 * 而应用的样式并没有覆盖它，所以原版实际用的是系统默认滚动条。
 *
 * 这里按锤子的视觉语言重新实现：宽 3dp、贴右边缘、圆角、半透明，
 * 颜色取主题的 `scrollbarThumb`，内容不足一屏时不绘制。
 * 需要完全一致的观感时，可以直接换回平台的滚动条。
 */
@Composable
fun Modifier.smartisanVerticalScrollbar(
    state: ScrollState,
    width: Dp = SmartisanDimens.ScrollbarWidth,
    margin: Dp = SmartisanDimens.ScrollbarMargin,
    color: Color = LocalSmartisanColors.current.scrollbarThumb,
): Modifier =
    this.drawWithContent {
        drawContent()
        val maxValue = state.maxValue
        if (maxValue <= 0) return@drawWithContent
        val viewport = state.viewportSize.toFloat()
        val total = maxValue + viewport
        if (total <= 0f || viewport <= 0f) return@drawWithContent
        val thumbWidth = width.toPx()
        val thumbHeight = (size.height * (viewport / total)).coerceAtLeast(thumbWidth)
        val travel = size.height - thumbHeight
        val fraction = (state.value.toFloat() / maxValue).coerceIn(0f, 1f)
        val left = size.width - margin.toPx() - thumbWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(left, travel * fraction),
            size = Size(thumbWidth, thumbHeight),
            cornerRadius = CornerRadius(thumbWidth / 2f, thumbWidth / 2f),
        )
    }

/**
 * [LazyListState] 版本的竖向滚动条。
 *
 * 滚动条长度按「可见项数 / 总项数」估算，与原版列表的表现一致。
 */
@Composable
fun Modifier.smartisanVerticalScrollbar(
    state: LazyListState,
    width: Dp = SmartisanDimens.ScrollbarWidth,
    margin: Dp = SmartisanDimens.ScrollbarMargin,
    color: Color = LocalSmartisanColors.current.scrollbarThumb,
): Modifier =
    this.drawWithContent {
        drawContent()
        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo.size
        if (totalItems <= 0 || visibleItems <= 0 || totalItems <= visibleItems) return@drawWithContent
        val firstIndex = layoutInfo.visibleItemsInfo.first().index
        val scrollableItems = (totalItems - visibleItems).coerceAtLeast(1)
        val fraction = (firstIndex.toFloat() / scrollableItems).coerceIn(0f, 1f)
        val thumbWidth = width.toPx()
        val thumbHeight = (size.height * (visibleItems.toFloat() / totalItems)).coerceAtLeast(thumbWidth)
        val travel = size.height - thumbHeight
        val left = size.width - margin.toPx() - thumbWidth
        drawRoundRect(
            color = color,
            topLeft = Offset(left, travel * fraction),
            size = Size(thumbWidth, thumbHeight),
            cornerRadius = CornerRadius(thumbWidth / 2f, thumbWidth / 2f),
        )
    }
