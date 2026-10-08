/**
 * 基础控件：锤子风格复选框。
 *
 * 复刻自锤子音乐（Compose）`ui/components/SmartisanCheckboxHit.kt` 与列表多选行为：
 * - 选中态是原版 `red_check_box.png` 的「实心圆 + 白色对勾」，未选中态是 `check_box_off.png`
 *   的细圆环，禁用态与原版一致地整体降到 30% 透明度；
 * - `Modifier.smartisanCheckboxBounds` 把复选框的命中区域上报给列表行，
 *   配合 [smartisanCheckboxHit] 让「整行点击」与「点复选框」得到一致的结果。
 *
 * 原实现依赖位图资源，这里按位图量出的几何用 Canvas 重画（36dp 命中框、24dp 圆、
 * 2.33dp 圆环、2dp 对勾），颜色取自主题：选中用 `accent` / `accentPressed`，
 * 未选中圆环用 `divider`，对勾用 `onAccent`。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors

/** 复选框命中框尺寸，原版位图为 108px @3x = 36dp。 */
private val CheckboxHitSize = 36.dp

/** 圆形直径，原版位图量得 24dp。 */
private val CheckboxCircleSize = 24.dp

/** 未选中圆环宽度，原版位图量得 2.33dp。 */
private val CheckboxRingWidth = 2.33.dp

/** 对勾线宽，原版位图量得约 2dp。 */
private val CheckboxCheckWidth = 2.dp

/** 禁用时的整体透明度，原版禁用位图是同样素材的 77/255。 */
private const val CheckboxDisabledAlpha = 0.30f

/** 对勾三个折点相对圆心的位置（dp），取自原版位图 38/49/70px @3x。 */
private val CheckboxCheckPoints =
    listOf(
        Offset(-5.33f, -0.17f),
        Offset(-1.67f, 3.67f),
        Offset(5.33f, -3.33f),
    )

/**
 * 判断某个点是否落在复选框命中区域内。
 *
 * 列表行用 [smartisanCheckboxBounds] 记录复选框的位置，再在整行点击时用本函数判断
 * 这次点击是否应该只切换多选状态；[touchSlop] 用 `ViewConfiguration.touchSlop`
 * 把命中区域向外扩一点，与原版一致。
 */
fun smartisanCheckboxHit(point: Offset, bounds: Rect?, touchSlop: Float): Boolean {
    if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) return false
    val slop = touchSlop.coerceAtLeast(0f)
    return point.x >= bounds.left - slop &&
        point.x < bounds.right + slop &&
        point.y >= bounds.top - slop &&
        point.y < bounds.bottom + slop
}

/**
 * 上报复选框的命中区域。
 *
 * 只上报真正显示出来的复选框；复选框离开组合时用 `null` 通知使用方清除记录。
 */
@Composable
fun Modifier.smartisanCheckboxBounds(onBounds: (Rect?) -> Unit): Modifier {
    val latestBounds by rememberUpdatedState(onBounds)
    DisposableEffect(Unit) { onDispose { latestBounds(null) } }
    return onGloballyPositioned { latestBounds(it.boundsInRoot()) }
}

/**
 * 锤子风格复选框。
 *
 * ```kotlin
 * var checked by remember { mutableStateOf(false) }
 * SmartisanCheckbox(checked = checked, onCheckedChange = { checked = it })
 * ```
 *
 * @param onCheckedChange 传 `null` 时复选框只作为展示，不响应点击（多选列表里未进入编辑态时就是这样）。
 */
@Composable
fun SmartisanCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val interaction: MutableInteractionSource = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick {}
    val interactive = enabled && onCheckedChange != null
    Canvas(
        modifier =
            modifier
                .size(CheckboxHitSize)
                .graphicsLayer { alpha = if (enabled) 1f else CheckboxDisabledAlpha }
                .toggleable(
                    value = checked,
                    interactionSource = interaction,
                    indication = null,
                    enabled = interactive,
                    role = Role.Checkbox,
                ) { value ->
                    click()
                    onCheckedChange?.invoke(value)
                },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = CheckboxCircleSize.toPx() / 2f
        if (checked) {
            drawCircle(
                color = if (pressed) colors.accentPressed else colors.accent,
                radius = radius,
                center = center,
            )
            val path =
                Path().apply {
                    CheckboxCheckPoints.forEachIndexed { index, point ->
                        val x = center.x + point.x.dp.toPx()
                        val y = center.y + point.y.dp.toPx()
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
            drawPath(
                path = path,
                color = colors.onAccent,
                style =
                    Stroke(
                        width = CheckboxCheckWidth.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
        } else {
            drawCircle(
                color = colors.divider,
                radius = radius - CheckboxRingWidth.toPx() / 2f,
                center = center,
                style = Stroke(width = CheckboxRingWidth.toPx()),
            )
        }
    }
}

