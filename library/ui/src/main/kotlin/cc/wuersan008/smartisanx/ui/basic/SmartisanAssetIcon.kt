package cc.wuersan008.smartisanx.ui.basic

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.size
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter

/**
 * 使用**原始图形资源**绘制图标。
 *
 * 这是 [SmartisanIcon] 的位图重载：图标资源来自三个复刻项目还原的原版素材
 * （selector 自带按下 / 禁用态，NinePatch 保持拉伸），因此能还原原版的拟物质感。
 *
 * 与矢量重载的区别：
 * - 传 [res] 时由 drawable 自己决定尺寸与内边距，[contentScale] 默认 `Fit`；
 * - [pressed] / [checked] / [enabled] 会映射为 drawable 状态，selector 自动切换；
 * - 原版图标在不同倍率下有独立位图，交给系统按密度挑选即可。
 *
 * ```kotlin
 * SmartisanIcon(
 *     res = SmartisanDrawables.IconBack,
 *     contentDescription = "返回",
 * )
 * ```
 */
@Composable
fun SmartisanIcon(
    @DrawableRes res: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    checked: Boolean = false,
    size: Dp? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    val painter =
        rememberSmartisanDrawablePainter(
            drawableRes = res,
            enabled = enabled,
            pressed = pressed,
            selected = selected,
            checked = checked,
        )
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = if (size != null) modifier.size(size) else modifier,
        alignment = alignment,
        contentScale = contentScale,
    )
}

/**
 * 按位图**原始尺寸**绘制资源，不做任何缩放。
 *
 * 用于机械表盘、指针、刻度这类必须逐像素对齐的原版素材：
 * 原版把这些位图按固定坐标叠放在一块固定尺寸的画布上，
 * 一旦让 Compose 去缩放任一图层，指针轴心就会偏移。
 */
@Composable
fun SmartisanIntrinsicImage(
    @DrawableRes res: Int,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressed: Boolean = false,
    alpha: Float = 1f,
) {
    val painter =
        rememberSmartisanDrawablePainter(
            drawableRes = res,
            enabled = enabled,
            pressed = pressed,
        )
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.None,
        alpha = alpha,
    )
}
