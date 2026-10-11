package cc.wuersan008.smartisanx.ui.basic

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableState
import cc.wuersan008.smartisanx.core.utils.smartisanRepaintOnStateChange

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
    val stateKey =
        smartisanDrawableState(
                enabled = enabled,
                pressed = pressed,
                selected = selected,
                checked = checked,
            )
            .contentHashCode()
    // 这里多套一层 Box 是必须的：`Image` 会在传入的 modifier 后面追加自己的
    // `clipToBounds()`，于是挂在 `Image` 上的绘制块并不落在「带 layer 的区段」里，
    // drawable 状态变化时不会重绘（原因见 `smartisanRepaintOnStateChange`）。
    // 套一层 Box 之后，绘制块紧跟在 Box 的 `graphicsLayer()` 后面，
    // 而 Box 的 layer 正好包住里面这张图，`invalidateDraw()` 就能命中它。
    Box(
        modifier =
            (if (size != null) modifier.size(size) else modifier)
                .smartisanRepaintOnStateChange(stateKey),
        contentAlignment = alignment,
        // 与原版 `ImageView` 一致：父容器给的 min 约束继续传给图片本身，
        // 否则 `contentScale = FillBounds` 这类按控件尺寸缩放的用法会取到位图固有尺寸。
        propagateMinConstraints = true,
    ) {
        // `Image` 自己还会建一层 layer（`clipToBounds()`），而 `PainterNode` 的
        // `getShouldAutoInvalidate()` 是 false：换 painter / 改 drawable 状态都不会
        // 让它 invalidate，那层 layer 于是永远画缓存里的旧图。
        // 因此按状态指纹换一次 key：节点重建 → 新 layer 必然重录 → 新状态画得出来。
        // 视觉结果与原来完全一致（还是同一个 `Image` + 同一个 painter）。
        key(stateKey) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                alignment = alignment,
                contentScale = contentScale,
            )
        }
    }
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
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
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
    // 与 `SmartisanIcon` 同样多套一层 Box + 换 key：只有让绘制节点重建，
    // `Image` 自己那层缓存 layer 才会重录，drawable 的 selector 状态（按下 / 禁用）
    // 变化才画得出来。
    Box(
        modifier =
            modifier.smartisanRepaintOnStateChange(
                smartisanDrawableState(enabled = enabled, pressed = pressed).contentHashCode(),
            ),
    ) {
        key(smartisanDrawableState(enabled = enabled, pressed = pressed).contentHashCode()) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = ContentScale.None,
                alpha = alpha,
            )
        }
    }
}
