package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

/**
 * 标题栏动作项。
 *
 * 支持两种图标来源，构造方式一样：
 *
 * ```kotlin
 * // 原版图形资源（推荐，selector 自带按下 / 禁用态）
 * SmartisanTitleBarAction(SmartisanDrawables.IconBack, "返回") { back() }
 *
 * // 自定义矢量图标
 * SmartisanTitleBarAction(SmartisanXIcons.Back, "返回") { back() }
 * ```
 */
@Immutable
class SmartisanTitleBarAction private constructor(
    /** 无障碍描述。 */
    val contentDescription: String,
    /** 点击回调。 */
    val onClick: () -> Unit,
    /** 是否可用。 */
    val enabled: Boolean,
    /** 原版位图资源，与 [imageVector] 二选一。 */
    @DrawableRes val iconRes: Int?,
    /** 自定义矢量图标，与 [iconRes] 二选一。 */
    val imageVector: ImageVector?,
) {
    companion object {
        /** 使用自定义矢量图标。 */
        operator fun invoke(
            imageVector: ImageVector,
            contentDescription: String,
            onClick: () -> Unit,
            enabled: Boolean = true,
        ): SmartisanTitleBarAction =
            SmartisanTitleBarAction(contentDescription, onClick, enabled, null, imageVector)

        /** 使用原版位图资源（见 [SmartisanDrawables]）。 */
        operator fun invoke(
            @DrawableRes iconRes: Int,
            contentDescription: String,
            onClick: () -> Unit,
            enabled: Boolean = true,
        ): SmartisanTitleBarAction =
            SmartisanTitleBarAction(contentDescription, onClick, enabled, iconRes, null)
    }
}
