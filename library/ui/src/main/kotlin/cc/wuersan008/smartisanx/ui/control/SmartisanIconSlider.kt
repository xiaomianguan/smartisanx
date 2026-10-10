package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter

/**
 * framework 的带图标滑杆（`smartisanos.widget.SliderWithIcons` +
 * `res/layout/slider_with_icons_layout.xml`）。
 *
 * 原版布局只有三条布局规则，**没有任何尺寸常量**（所以本库也没有为它加 `SmartisanDimens`）：
 *
 * | 子视图 | 原版规则 |
 * | --- | --- |
 * | `left_icon` | `alignParentLeft` + `centerVertical` + `wrap_content` |
 * | `right_icon` | `alignParentRight` + `centerVertical` + `wrap_content` |
 * | `seek_bar` | `match_parent`，`toRightOf` 左图标、`toLeftOf` 右图标、`centerVertical` |
 *
 * 滑杆本体用的是 `SmoothSeekBar` 的 `SeekBarStyle.Thin.LargeThumb.Actived` 样式
 * （`SeekBarStyle` 家族里最粗的一档滑块）：滑块取自 `seekbar_scrubber_control_selector`，
 * 即 `progress_control` / 禁用态 `progress_control_disabled` —— 和
 * [SmartisanSmoothSeekBar] 用的是同一套素材；该样式的 `progressDrawable` 是 `@null`，
 * 轨道由控件自己画，所以这里直接复用 [SmartisanSmoothSeekBar]。
 *
 * 原版有个小脾气值得注意：左右图标资源「要么两个都给、要么两个都不显示」——
 * 构造函数里只有 `leftIconRes > 0 && rightIconRes > 0` 才 `setImageResource`，
 * 否则两个都 `setVisibility(GONE)`。Compose 版改成两个普通槽位，给哪个画哪个。
 *
 * ```kotlin
 * var volume by remember { mutableFloatStateOf(0.6f) }
 * SmartisanIconSlider(
 *     value = volume,
 *     onValueChange = { volume = it },
 *     leading = { SmartisanSliderIcon(SmartisanDrawables.VolumeSmall, "小音量", size = 26.dp) },
 *     trailing = { SmartisanSliderIcon(SmartisanDrawables.VolumeHigh, "大音量", size = 26.dp) },
 * )
 * ```
 *
 * @param value 当前值，0f..1f
 * @param leading 左端内容，通常是 [SmartisanSliderIcon]；原版按素材固有尺寸（`wrap_content`）摆放
 * @param trailing 右端内容，通常是 [SmartisanSliderIcon]
 * @param steps 大于 0 时按等分吸附，透传给 [SmartisanSmoothSeekBar]
 */
@Composable
fun SmartisanIconSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    // 原版：RelativeLayout + setGravity(CENTER_VERTICAL)，三个子视图都竖直居中；
    // 滑杆占满两端图标之间的剩余宽度（match_parent + toLeftOf / toRightOf）。
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        SmartisanSmoothSeekBar(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            steps = steps,
        )
        trailing?.invoke()
    }
}

/**
 * 带图标滑杆两端的图标。
 *
 * 原版这两个 `ImageView` 是 `wrap_content`，也就是**按素材自身尺寸画**，
 * 所以 [size] 默认 `null`（用固有尺寸）；想统一成某个尺寸时再显式传值。
 *
 * @param res 原版位图资源（例如 `volume_small_n`）
 * @param contentDescription 无障碍描述
 * @param size 显式尺寸；`null` 表示按素材固有尺寸（原版行为）
 * @param tint 着色；默认不改变原图颜色
 */
@Composable
fun SmartisanSliderIcon(
    @DrawableRes res: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    tint: Color = Color.Unspecified,
) {
    Image(
        painter = rememberSmartisanDrawablePainter(res),
        contentDescription = contentDescription,
        modifier = if (size != null) modifier.size(size) else modifier,
        colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint),
    )
}
