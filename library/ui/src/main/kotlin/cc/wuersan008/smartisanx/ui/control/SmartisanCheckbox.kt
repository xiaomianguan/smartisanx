/**
 * 基础控件：锤子风格复选框。
 *
 * 复刻自锤子音乐（Compose）`ui/components/SmartisanCheckboxHit.kt` 与列表多选行为：
 * 选中态是原版 `red_check_box.png` 的「红色实心圆 + 白色对勾」，未选中态是 `check_box_off.png`
 * 的细圆环，按下与禁用态由原版 selector 自带（禁用态就是素材本身的低透明度）。
 *
 * 位图取自原版 selector `selector_check_box_red`（108px @3x = 36dp，与命中框同尺寸）：
 * `red_check_box` / `red_check_box_pressed` / `red_check_box_disabled` / `check_box_off` /
 * `check_box_off_disabled`。
 *
 * `Modifier.smartisanCheckboxBounds` 把复选框的命中区域上报给列表行，
 * 配合 [smartisanCheckboxHit] 让「整行点击」与「点复选框」得到一致的结果。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/** 复选框命中框尺寸，原版位图为 108px @3x = 36dp。 */
private val CheckboxHitSize = 36.dp

/** 默认复选框 selector：原版 `selector_check_box_red`，自带按下与禁用态。 */
@DrawableRes private val CheckboxSelector = R.drawable.selector_check_box_red

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
 * @param selectorRes 复选框位图 selector，默认是原版 `selector_check_box_red`：
 *   选中态是红色实心圆 + 白色对勾，未选中态是细圆环，按下与禁用态由 selector 自带。
 */
@Composable
fun SmartisanCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes selectorRes: Int = CheckboxSelector,
) {
    val interaction: MutableInteractionSource = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick {}
    val interactive = enabled && onCheckedChange != null
    SmartisanIcon(
        res = selectorRes,
        contentDescription = null,
        modifier =
            modifier
                .size(CheckboxHitSize)
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
        enabled = enabled,
        pressed = pressed,
        checked = checked,
    )
}
