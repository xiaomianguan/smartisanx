package cc.wuersan008.smartisanx.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 行内隐藏操作的一项（原版 `HiddenListActionLayout` 里的一个 `ImageView`）。
 *
 * @param iconRes 图标资源，通常是 selector（按下 / 禁用态自动切换）
 * @param contentDescription 无障碍描述
 * @param enabled 传 `false` 会切到 selector 的禁用态（原版 `setActionEnabled`）
 */
data class SmartisanHiddenRowAction(
    @DrawableRes val iconRes: Int,
    val contentDescription: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {},
)

/**
 * 列表行内隐藏的一排操作（framework `smartisanos.widget.HiddenListActionLayout`）。
 *
 * 原版就是一个横向 `LinearLayout`：左右各 12dp 内边距
 * （`hidden_list_action_left_right_padding`）、相邻图标之间 6dp
 * （`hidden_list_action_icon_gap`）、图标本身 `wrap_content` 且竖直居中；
 * 个数由 `actionCount` 决定，图标 / 点击 / 可用性按索引设置。
 *
 * 本库的差别只有一个：原版按索引传参（越界会 `IllegalArgumentException`），
 * 这里换成 `List<SmartisanHiddenRowAction>`，越界问题从 API 上消失；
 * 图标仍默认取素材固有尺寸（即原版的 `wrap_content`），要统一大小再传 [iconSize]。
 *
 * 用法上它一般是**被侧滑露出来**的那一层 —— 框架里没有任何布局 / 代码引用这个类
 * （dump 里只有它自己），露出容器是各个 App 自己写的，所以本库只提供这一排操作本身，
 * 要滑动露出可以自己套一层手势，或者配合 [SmartisanSwipeToDelete] 的物理参数
 * （原版时钟的 65dp 直接位移 + 1/5 阻尼 + 50dp 阈值）。
 *
 * ```kotlin
 * SmartisanHiddenRowActions(
 *     actions = listOf(
 *         SmartisanHiddenRowAction(SmartisanOriginalIcons.Delete, "删除") { remove() },
 *         SmartisanHiddenRowAction(SmartisanOriginalIcons.More, "更多") { openMenu() },
 *     ),
 * )
 * ```
 *
 * @param actions 从左到右排列的操作
 * @param iconSize 图标尺寸；`null` 表示按素材固有尺寸（原版行为）
 * @param spacing 相邻图标间距，原版 `hidden_list_action_icon_gap` = 6dp
 * @param sidePadding 左右内边距，原版 `hidden_list_action_left_right_padding` = 12dp
 */
@Composable
fun SmartisanHiddenRowActions(
    actions: List<SmartisanHiddenRowAction>,
    modifier: Modifier = Modifier,
    iconSize: Dp? = null,
    spacing: Dp = SmartisanDimens.HiddenActionIconGap,
    sidePadding: Dp = SmartisanDimens.HiddenActionSidePadding,
) {
    Row(
        modifier = modifier.padding(horizontal = sidePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEachIndexed { index, action ->
            Box(
                modifier =
                    Modifier
                        // 原版每个 ImageView 只挂了 OnClickListener，没有涟漪、也没有额外按压动画。
                        .smartisanClickable(enabled = action.enabled) { action.onClick() }
                        .then(if (index == 0) Modifier else Modifier.padding(start = spacing)),
            ) {
                SmartisanIcon(
                    res = action.iconRes,
                    contentDescription = action.contentDescription,
                    enabled = action.enabled,
                    modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
                )
            }
        }
    }
}
