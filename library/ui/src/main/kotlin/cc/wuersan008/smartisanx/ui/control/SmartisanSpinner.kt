package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 下拉选择的版式，对应 framework `SmartisanSpinnerView` 的三个常量
 * （`SPINNER_STYLE_NORMAL` / `SPINNER_STYLE_DROP` / `SPINNER_STYLE_RANGE`）。
 */
enum class SmartisanSpinnerStyle {
    /** 纯文字（原版 `SPINNER_STYLE_NORMAL = 0`）：只有居中的文字选择器。 */
    Normal,

    /** 下拉（原版 `SPINNER_STYLE_DROP = 1`）：文字 + 可选左侧图标 + 右侧下拉箭头。 */
    Drop,

    /** 区间（原版 `SPINNER_STYLE_RANGE = 2`）：左右箭头 + 定宽文字选择器。 */
    Range,
}

/**
 * 锤子风格下拉选择。
 *
 * 对应 framework `smartisanos.widget.SmartisanSpinnerView`
 * （`framework/smartisanos.jar` 的 `classes.dex`）。原版没有对应的 layout 文件，
 * 整个视图是在 `setTitleStyle` / `setLeftIconStyle` / `setDropIconStyle` /
 * `setRangeIconStyle` 里**用代码拼出来的**，这里按同样的规则还原：
 *
 * | 版式 | 原版拼装 | 本组件 |
 * | --- | --- | --- |
 * | [Normal] | `addRule(13)`，只有文字 | 居中文字 |
 * | [Drop] | 文字 + 左侧图标（`rightMargin = 6dp`）+ 右侧 `selector_dropdown_arrow` | 同左 |
 * | [Range] | 文字 + 左侧 `selector_previous_arrow` + 右侧 `selector_next_arrow` | 同左 |
 *
 * 还原要点（照抄原版 `SmartisanSpinnerView` 与 `res/values/dimens.xml`）：
 * - 图标尺寸 `smartisan_spinner_small_icon_width = 22dp`；
 * - 图标与文字间距 `smartisan_small_blank_spacing_width = 6dp`；
 * - 区间版式里文字区最小宽度 `smartisan_spinner_view_text_width = 152dp`
 *   （原版 `setRangeIconStyle` 里 `mTextPicker.setMinimumWidth(minWidth)`）；
 * - 下拉箭头 / 左右箭头分别是 `selector_dropdown_arrow`、`selector_previous_arrow`、
 *   `selector_next_arrow`（都自带按下态）；
 * - 文字左侧图标用编辑类那两套 selector（`selector_editor_spinner_icon` /
 *   `selector_editor_spinner_lite_icon`），对应原版 `centerLeftIcon` 属性；
 * - 原版 `setOnClickListener` 直接转发给文字选择器，所以点击区域就是文字；
 *   箭头各自有独立点击（原版 `mDropDownListener` / `mRangeClickListener`）。
 *
 * ```kotlin
 * // 下拉（最常用）：点文字或箭头都回调
 * SmartisanSpinner(
 *     text = "按名称排序",
 *     leftIconRes = SmartisanSpinnerDefaults.EditorIcon,
 *     onClick = { open() },
 *     onDropDownClick = { open() },
 * )
 *
 * // 区间：左右箭头切换
 * SmartisanSpinner(
 *     text = "2024 年 6 月",
 *     style = SmartisanSpinnerStyle.Range,
 *     onPreviousClick = { prev() },
 *     onNextClick = { next() },
 * )
 * ```
 *
 * @param text 当前选中项文案。
 * @param modifier 外部修饰符。
 * @param style 版式，默认 [SmartisanSpinnerStyle.Drop]（原版属性默认值是 `Normal`）。
 * @param subText 副标题文案，对应原版 `SmartisanWheelTextView.setSubContentText`；`null` 或空串时不显示。
 * @param leftIconRes 文字左侧图标素材（原版 `centerLeftIcon` 属性）；`null` 时不显示。
 * @param enabled 是否可用；禁用时按下态与点击都不生效。
 * @param textColor 文字颜色；不指定时沿用 [SmartisanText] 的默认色。
 * @param textSize 文字字号；不指定时沿用 [SmartisanText] 的默认字号。
 * @param iconSize 图标尺寸，默认 22dp（`smartisan_spinner_small_icon_width`）。
 * @param onClick 点击文字的回调（原版 `setOnClickListener`）。
 * @param onDropDownClick 点击下拉箭头的回调（原版 `mDropDownListener`）。
 * @param onPreviousClick 点击左箭头的回调（原版 `SpinnerRangeClickListener.onRangeLeftClick`）。
 * @param onNextClick 点击右箭头的回调（原版 `SpinnerRangeClickListener.onRangeRightClick`）。
 */
@Composable
fun SmartisanSpinner(
    text: String,
    modifier: Modifier = Modifier,
    style: SmartisanSpinnerStyle = SmartisanSpinnerStyle.Drop,
    subText: String? = null,
    @DrawableRes leftIconRes: Int? = null,
    enabled: Boolean = true,
    textColor: Color = Color.Unspecified,
    textSize: TextUnit = TextUnit.Unspecified,
    iconSize: Dp = SmartisanSpinnerDefaults.IconWidth,
    onClick: (() -> Unit)? = null,
    onDropDownClick: (() -> Unit)? = null,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null,
) {
    val interaction = rememberSmartisanInteractionSource()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        when (style) {
            SmartisanSpinnerStyle.Range -> {
                // 原版 setRangeIconStyle：文字左侧的上一项箭头。
                SmartisanSpinnerIconButton(
                    res = SmartisanSpinnerDefaults.PreviousArrow,
                    size = iconSize,
                    enabled = enabled,
                    onClick = onPreviousClick,
                )
                Spacer(modifier = Modifier.width(SmartisanSpinnerDefaults.BlankSpacing))
            }

            SmartisanSpinnerStyle.Drop -> {
                if (leftIconRes != null) {
                    // 原版 setLeftIconStyle：文字左侧图标，与文字间距 6dp。
                    SmartisanIcon(
                        res = leftIconRes,
                        contentDescription = null,
                        enabled = enabled,
                        size = iconSize,
                    )
                    Spacer(modifier = Modifier.width(SmartisanSpinnerDefaults.BlankSpacing))
                }
            }

            SmartisanSpinnerStyle.Normal -> Unit
        }
        Column(
            modifier =
                Modifier
                    .then(
                        if (style == SmartisanSpinnerStyle.Range) {
                            // 原版 setRangeIconStyle：文字区最小宽度 152dp。
                            Modifier.defaultMinSize(minWidth = SmartisanSpinnerDefaults.TextMinWidth)
                        } else {
                            Modifier
                        },
                    )
                    .smartisanClickable(
                        interactionSource = interaction,
                        enabled = enabled && onClick != null,
                        onClick = { onClick?.invoke() },
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SmartisanText(
                text = text,
                color = textColor,
                fontSize = textSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subText.isNullOrEmpty()) {
                SmartisanText(
                    text = subText,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when (style) {
            SmartisanSpinnerStyle.Drop -> {
                Spacer(modifier = Modifier.width(SmartisanSpinnerDefaults.BlankSpacing))
                // 原版 setDropIconStyle：文字右侧的下拉箭头。
                SmartisanSpinnerIconButton(
                    res = SmartisanSpinnerDefaults.DropArrow,
                    size = iconSize,
                    enabled = enabled,
                    onClick = onDropDownClick,
                )
            }

            SmartisanSpinnerStyle.Range -> {
                Spacer(modifier = Modifier.width(SmartisanSpinnerDefaults.BlankSpacing))
                // 原版 setRangeIconStyle：文字右侧的下一项箭头。
                SmartisanSpinnerIconButton(
                    res = SmartisanSpinnerDefaults.NextArrow,
                    size = iconSize,
                    enabled = enabled,
                    onClick = onNextClick,
                )
            }

            SmartisanSpinnerStyle.Normal -> Unit
        }
    }
}

/**
 * 原版用 `ImageButton` 承载箭头（`setBackgroundColor(0)` 去掉系统底），
 * 这里用 [SmartisanIcon] + 按压态 selector 还原，并挂上各自独立的点击。
 */
@Composable
private fun SmartisanSpinnerIconButton(
    @DrawableRes res: Int,
    size: Dp,
    enabled: Boolean,
    onClick: (() -> Unit)?,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    SmartisanIcon(
        res = res,
        contentDescription = null,
        enabled = enabled,
        pressed = pressed,
        size = size,
        modifier =
            Modifier.smartisanClickable(
                interactionSource = interaction,
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
            ),
    )
}

/**
 * 下拉选择默认值，取自 framework `SmartisanSpinnerView` 用到的
 * `res/values/dimens.xml` 尺寸与 `res/drawable` 里的箭头 / 图标 selector。
 */
object SmartisanSpinnerDefaults {
    /** 图标尺寸，原版 `dimen/smartisan_spinner_small_icon_width` = 22dp。 */
    val IconWidth: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.smartisan_spinner_small_icon_width)

    /** 区间版式里文字区最小宽度，原版 `dimen/smartisan_spinner_view_text_width` = 152dp。 */
    val TextMinWidth: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.smartisan_spinner_view_text_width)

    /** 图标与文字之间的间距，原版 `dimen/smartisan_small_blank_spacing_width` = 6dp。 */
    val BlankSpacing: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.smartisan_small_blank_spacing_width)

    /** 下拉箭头 selector（含按下态），原版 `selector_dropdown_arrow`。 */
    @DrawableRes val DropArrow: Int = R.drawable.selector_dropdown_arrow

    /** 上一项箭头 selector（含按下态），原版 `selector_previous_arrow`。 */
    @DrawableRes val PreviousArrow: Int = R.drawable.selector_previous_arrow

    /** 下一项箭头 selector（含按下态），原版 `selector_next_arrow`。 */
    @DrawableRes val NextArrow: Int = R.drawable.selector_next_arrow

    /** 编辑类下拉的左侧图标 selector，原版 `selector_editor_spinner_icon`（素材 `spinner_icon`）。 */
    @DrawableRes val EditorIcon: Int = R.drawable.selector_editor_spinner_icon

    /** 轻量编辑类下拉的左侧图标 selector，原版 `selector_editor_spinner_lite_icon`（素材 `spinner_lite_icon`）。 */
    @DrawableRes val EditorLiteIcon: Int = R.drawable.selector_editor_spinner_lite_icon
}
