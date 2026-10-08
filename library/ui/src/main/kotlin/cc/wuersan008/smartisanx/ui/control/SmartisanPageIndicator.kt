/**
 * 控件：锤子页面指示器（小圆点 / 图标）。
 *
 * 对应原版 `smartisanos.app.IndicatorView`，出现在锤子日历（Calendar 8.1.2）与
 * 便签（Notes 7.3.1）里；便签的 `indicator_view_smartisanos.xml` 直接引用了这个类。
 *
 * 还原要点（照抄原版 `IndicatorView.java`）：
 * - 最多 25 个点（原版 `MAX_NUM_DOT = 25`），只有 1 页时不画；
 * - 每个点占 `4 × radius` 的横向空间（点直径 `2r`、间隔 `2r`），
 *   整块宽度 = `paddingLeft + paddingRight + 4r × count − 2r + 1px`，
 *   高度 = `paddingTop + paddingBottom + 4r + 1px`（原版 `measureLong` / `measureShort`）；
 * - 点从 `paddingLeft + r + (内容宽 − 4r×count + 2r)/2` 开始摆放，
 *   圆心纵向固定在 `paddingTop + 2r`；
 * - 未选中的点用 `mPaintPageFill`、选中的点用 `mPaintFill` 并最后绘制（压在其它点之上）；
 * - 支持「前若干个用图标、其余用圆点」（原版 `addIconIndicator`），
 *   图标按原版尺寸居中摆放、宽度依次累加；
 * - 无障碍描述为「第 N 页，共 M 页」（原版用 `R.string.find` 的 `(cur + 1, all)` 两个参数）。
 *
 * 原版默认色取自 **Smartisan 私有 framework**（`android.R.color.car_card_ripple_background_inverse`
 * 为选中色 `#99000000`、`android.R.color.car_card_ripple_background_dark` 为未选中色 `#1a000000`，
 * 便签里同值的资源是 `zcoj` / `zcok`），半径取自 framework 的
 * `android.R.dimen.config_qsTileStrokeWidthActive`（AOSP 为 2dp，便签复刻版为 `zdi1b = 7px`）。
 * 这些资源在本库拿不到，因此默认值改用主题语义色与 2dp 半径代替，可用参数覆盖。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import kotlin.math.roundToInt


/** 原版 `IndicatorView.IconIndicator`：某一页用「常态 / 选中」两张图标表示。 */
@Immutable
data class SmartisanPageIndicatorIcon(
    @DrawableRes val normalRes: Int,
    @DrawableRes val selectedRes: Int,
)

/** 最多绘制的页数，原版 `MAX_NUM_DOT = 25`。 */
private const val MaxDotCount = 25

/** 默认圆点半径，原版取自 framework `config_qsTileStrokeWidthActive`（AOSP 2dp）。 */
private val DefaultDotRadius = 2.dp

/**
 * 锤子页面指示器。
 *
 * ```kotlin
 * SmartisanPageIndicator(pageCount = 5, currentPage = page)
 * ```
 *
 * @param pageCount 总页数，对应原版 `setState(all)`；超过 25 会被截断（原版 `MAX_NUM_DOT`）。
 * @param currentPage 当前页下标，对应原版 `setState(all, cur)`。
 * @param modifier 外部修饰符（本组件自己按原版公式算出宽高，不需要外部指定尺寸）。
 * @param radius 圆点半径，对应原版 `setRadius`。
 * @param pageColor 未选中圆点颜色，对应原版 `setPageColor`。
 * @param selectedColor 选中圆点颜色，对应原版 `setFillColor`。
 * @param icons 前若干页的图标指示（原版 `addIconIndicator`），为空时全部用圆点。
 * @param contentDescription 无障碍描述；为 null 时用「第 N 页，共 M 页」。
 */
@Composable
fun SmartisanPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    radius: Dp = DefaultDotRadius,
    pageColor: Color = Color.Unspecified,
    selectedColor: Color = Color.Unspecified,
    icons: List<SmartisanPageIndicatorIcon> = emptyList(),
    contentDescription: String? = null,
) {
    val colors = LocalSmartisanColors.current
    val count = pageCount.coerceIn(0, MaxDotCount)
    // 原版 onDraw：不足 2 页直接不画。
    if (count <= 1) return
    val page = currentPage.coerceIn(0, count - 1)
    val dotColor = if (pageColor == Color.Unspecified) colors.textPrimary.copy(alpha = 0.1f) else pageColor
    val currentColor = if (selectedColor == Color.Unspecified) colors.textSecondary else selectedColor
    val painters = icons.map { rememberSmartisanDrawablePainter(it.normalRes) to rememberSmartisanDrawablePainter(it.selectedRes) }
    val density = LocalDensity.current
    val radiusPx = with(density) { radius.toPx() }
    // 原版 measureLong / measureShort：整块宽高都由半径与页数算出（末尾 +1 是原版的取整补偿）。
    val widthPx = (radiusPx * 4f * count - radiusPx * 2f + 1f).roundToInt()
    val heightPx = (radiusPx * 4f + 1f).roundToInt()
    Canvas(
        modifier =
            modifier
                .smartisanIndicatorSize(widthPx, heightPx)
                .semantics {
                    this.contentDescription = contentDescription ?: "第 ${page + 1} 页，共 $count 页"
                },
    ) {
        val spacing = radiusPx * 4f
        val centerY = size.height / 2f
        // 原版：f5 = (4r / 2) * (2 * all - 1)，f6 = r + (width - f5) / 2。
        val span = (spacing / 2f) * (count * 2 - 1)
        val startX = radiusPx + (size.width - span) / 2f
        var iconCursor = 0f
        painters.forEachIndexed { index, pair ->
            val painter = if (index == page) pair.second else pair.first
            val iconWidth = painter.smartisanIntrinsicWidth()
            val iconHeight = painter.smartisanIntrinsicHeight()
            translate(
                left = startX - iconWidth / 2f + iconCursor,
                top = centerY - iconHeight / 2f,
            ) {
                with(painter) { draw(Size(iconWidth, iconHeight)) }
            }
            iconCursor += iconWidth
        }
        for (index in painters.size until count) {
            drawCircle(
                color = dotColor,
                radius = radiusPx,
                center = Offset(startX + index * spacing, centerY),
            )
        }
        // 原版最后单独画当前页，保证压在其它点之上。
        if (page >= painters.size) {
            drawCircle(
                color = currentColor,
                radius = radiusPx,
                center = Offset(startX + page * spacing, centerY),
            )
        }
    }
}

/** 把画布强制成原版公式算出的像素尺寸（原版是 `setMeasuredDimension`）。 */
private fun Modifier.smartisanIndicatorSize(widthPx: Int, heightPx: Int): Modifier =
    layout { measurable, _ ->
        val placeable =
            measurable.measure(
                Constraints.fixed(
                    width = widthPx.coerceAtLeast(0),
                    height = heightPx.coerceAtLeast(0),
                ),
            )
        layout(widthPx, heightPx) { placeable.place(0, 0) }
    }

/** Painter 的固有宽度（像素）；未知时按 0 处理。 */
private fun Painter.smartisanIntrinsicWidth(): Float =
    intrinsicSize.width.takeIf { it.isFinite() && it > 0f } ?: 0f

/** Painter 的固有高度（像素）；未知时按 0 处理。 */
private fun Painter.smartisanIntrinsicHeight(): Float =
    intrinsicSize.height.takeIf { it.isFinite() && it > 0f } ?: 0f
