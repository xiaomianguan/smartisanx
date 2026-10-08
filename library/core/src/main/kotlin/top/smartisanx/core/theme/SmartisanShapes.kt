package top.smartisanx.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * smartisanx 的形状。
 *
 * 锤子风格整体偏方正：列表、标题栏都是直角，只有弹窗与悬浮元素带小圆角。
 */
@Immutable
class SmartisanShapes(
    /** 完全直角，列表、标题栏、分组卡片默认使用。 */
    val none: Shape = RoundedCornerShape(0.dp),
    /** 极小圆角，用于标签、徽标。 */
    val extraSmall: Shape = RoundedCornerShape(2.dp),
    /** 小圆角，用于按钮、输入框。 */
    val small: Shape = RoundedCornerShape(4.dp),
    /** 中圆角，用于卡片。 */
    val medium: Shape = RoundedCornerShape(8.dp),
    /** 弹窗圆角，原版 `smartisan_modal_corner_radius`。 */
    val dialog: Shape = RoundedCornerShape(10.dp),
    /** 底部弹层圆角。 */
    val sheet: Shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
    /** 大圆角，用于悬浮按钮。 */
    val large: Shape = RoundedCornerShape(16.dp),
)

/** 当前 [SmartisanShapes]。 */
val LocalSmartisanShapes: ProvidableCompositionLocal<SmartisanShapes> =
    staticCompositionLocalOf { SmartisanShapes() }
