/**
 * 控件：底部操作按钮组（原版 `smartisanos.widget.ActionButtonGroup` / `ButtonGroup`）。
 *
 * 这是设置页、文件管理器、便签这类「多选 / 批量操作」界面底部的操作条：
 * 一条 48dp 的次级栏（`secondary_bar`）+ 上方一条投影（`smartisan_secondary_bar_shadow`），
 * 栏里放 1~4 个文字按钮，两端可选一个图标按钮（60×48dp 的方形按钮）。
 *
 * | framework 类 | 行数 | 本库对应 |
 * | --- | --- | --- |
 * | `smartisanos.widget.ActionButtonGroup` | 256 | [SmartisanActionButtonGroup]：左图标 + 最多 4 个文字按钮 / 最多 2 个文字按钮 + 右图标 |
 * | `smartisanos.widget.ButtonGroup` | 209 | [SmartisanActionButtonGroup]：只放文字按钮，1~3 个 |
 * | `smartisanos.widget.ShadowButton` | 270 | 文字按钮外观（与库内 [SmartisanButtonTabGroup] 同一套 selector 与文字阴影） |
 * | `smartisanos.widget.ShadowComponent` | 143 | 栏上方的 9-patch 投影（`smartisan_secondary_bar_shadow`） |
 *
 * 原版的两种排布模式（`ActionButtonGroup` 的 `ActionButtonGroup_actionMode`）在这里合成一套 API：
 * 给 [leftAction] 就是「左图标 + 文字按钮」，给 [rightAction] 就是「文字按钮 + 右图标」，
 * 两端都给就是「左图标 + 文字按钮 + 右图标」；原版两个模式各自限制文字按钮的个数
 * （左图标模式 4 个、右图标模式 2 个），本组件不限制，超出的由调用方自己取舍。
 *
 * 尺寸与素材全部照抄原版：
 *
 * | 部位 | 原版 | 值 |
 * | --- | --- | --- |
 * | 栏高 / 底图 | `dimen/secondary_bar_height`、`drawable/secondary_bar` | 48dp |
 * | 栏上投影 | `drawable/smartisan_secondary_bar_shadow` | 9-patch |
 * | 文字按钮底图 | `selector_small_btn_filter_left` / `_middle` / `_right`（多个）/ `selector_small_btn_standard`（单个） | 与 [SmartisanButtonTabGroup] 同一套 |
 * | 文字按钮文字 | `style/SmallButton.Filter` / `SmallButton.Standard`、`dimen/semi_small_text_size` | 13.5sp 加粗、单行省略 |
 * | 文字阴影 | `color/filter_button_text_shadow_colors` | 偏移 (0, -2px)、模糊 0.1px（只在按下 / 激活态可见） |
 * | 图标按钮 | `dimen/smartisan_button_fixed_width` × `samrtisan_button_fixed_height`、`dimen/smartisan_button_limit_min_width` | 60×48dp、最小 66dp |
 * | 图标内缩 | `dimen/action_button_icon_inset_left` / `_right` | 左 9dp、右 7dp |
 * | 按钮间距 | `dimen/button_group_btn_gap` | 6dp |
 * | 栏左右内边距 | `dimen/button_group_left_right_padding` | 6dp |
 * | 图标按钮与文字按钮之间 | `dimen/action_button_left_margin` | 12dp |
 *
 * 与原版的已知差异：
 *
 * 1. 原版在系统字号放大时会把按钮文字钳到基础字号的 0.93 倍（`MAX_FONT_SCALE_WITH_TEXT`），
 *    本库不钳，字号跟随系统、超长按原版一样省略；
 * 2. 原版 `ShadowButton` 还支持图标 + 文字混排（`setButtonDrawable` 给文字按钮加左图标），
 *    本库的文字按钮只放文字、图标按钮只放图标（原版实际用法也是如此）；
 * 3. 原版禁用态是把整个按钮的透明度降到 0.3（`ShadowButton` 的 `_disabled` 位图），
 *    这里用同样的 0.3 alpha 叠在 selector 的禁用底图上。
 *
 * 用法：
 *
 * ```kotlin
 * SmartisanActionButtonGroup(
 *     actions = listOf(
 *         SmartisanActionButton("复制") { … },
 *         SmartisanActionButton("移动") { … },
 *         SmartisanActionButton("删除", enabled = false) { … },
 *     ),
 *     leftAction = SmartisanActionButton(iconRes = SmartisanDrawables.IconComplete, contentDescription = "全选") { … },
 * )
 * ```
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanStateListColor
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 底部操作按钮组的尺寸，全部对应原版 framework 的 dimens。 */
object SmartisanActionButtonGroupDefaults {
    /** 栏高（原版 `dimen/secondary_bar_height`）。 */
    val BarHeight = 48.dp

    /** 文字按钮最小宽度（原版 `dimen/smartisan_button_limit_min_width`）。 */
    val ActionMinWidth = 66.dp

    /** 文字按钮高度（`SmallButton` 底图 `filter_btn_*` 的固有高度：92×146px = 30.7×48.7dp，与栏同高）。 */
    val ActionHeight = 48.dp

    /** 图标按钮的固定尺寸（原版 `dimen/smartisan_button_fixed_width` / `samrtisan_button_fixed_height`）。 */
    val IconButtonWidth = 60.dp
    val IconButtonHeight = 48.dp

    /** 栏左右内边距（原版 `dimen/button_group_left_right_padding`）。 */
    val BarHorizontalPadding = 6.dp

    /** 按钮之间的间距（原版 `dimen/button_group_btn_gap`）。 */
    val ButtonGap = 6.dp

    /** 图标按钮与文字按钮之间的间距（原版 `dimen/action_button_left_margin`）。 */
    val IconButtonGap = 12.dp

    /** 图标在图标按钮里的内缩（原版 `dimen/action_button_icon_inset_left` / `_right`）。 */
    val IconInsetStart = 9.dp
    val IconInsetEnd = 7.dp

    /** 文字按钮的字号（原版 `dimen/semi_small_text_size`）。 */
    val TextSize = 13.5.sp

    /** 原版 `ShadowButton` 禁用时的整体透明度。 */
    const val DisabledAlpha = 0.3f
}

/** 按钮组里的一个按钮：文字按钮给 [text]，图标按钮给 [iconRes]。 */
data class SmartisanActionButton(
    /** 文字按钮的文案。 */
    val text: String? = null,
    /** 图标按钮的图标。 */
    @DrawableRes val iconRes: Int? = null,
    /** 是否可用。 */
    val enabled: Boolean = true,
    /** 图标按钮的无障碍描述。 */
    val contentDescription: String? = null,
    /** 点击回调。 */
    val onClick: () -> Unit,
)

/**
 * 底部操作按钮组。
 *
 * @param actions 文字按钮，按顺序从左到右；多个按钮时按位置取原版分段的
 *   `filter_left` / `filter_middle` / `filter_right` 底图，只有一个时取 `standard`。
 * @param modifier 外部修饰符。
 * @param leftAction 左端的图标按钮（原版 `mLeftActionButton`）。
 * @param rightAction 右端的图标按钮（原版 `mRightActionButton`）。
 * @param showShadow 是否画栏上方那条投影（原版 `setActionButtonGroupShadowVisibility`）。
 * @param barRes 栏底图；默认原版 `secondary_bar`。
 * @param shadowRes 栏上投影；默认原版 `smartisan_secondary_bar_shadow`。
 */
@Composable
fun SmartisanActionButtonGroup(
    actions: List<SmartisanActionButton>,
    modifier: Modifier = Modifier,
    leftAction: SmartisanActionButton? = null,
    rightAction: SmartisanActionButton? = null,
    showShadow: Boolean = true,
    @DrawableRes barRes: Int = SmartisanDrawables.SecondaryBarBackground,
    @DrawableRes shadowRes: Int = SmartisanDrawables.ActionButtonGroupShadow,
) {
    Column(modifier.fillMaxWidth()) {
        if (showShadow) {
            Box(Modifier.fillMaxWidth().smartisanDrawableBackground(shadowRes))
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SmartisanActionButtonGroupDefaults.BarHeight)
                    .smartisanDrawableBackground(barRes)
                    .padding(
                        horizontal = SmartisanActionButtonGroupDefaults.BarHorizontalPadding,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leftAction?.let {
                SmartisanActionButtonGroupIconButton(
                    action = it,
                    modifier =
                        Modifier.padding(end = SmartisanActionButtonGroupDefaults.IconButtonGap),
                )
            }
            actions.forEachIndexed { index, action ->
                if (index > 0) {
                    Box(Modifier.size(SmartisanActionButtonGroupDefaults.ButtonGap))
                }
                SmartisanActionButtonGroupTextButton(
                    action = action,
                    index = index,
                    count = actions.size,
                    // 原版右图标模式（ACTION_MODE_BOTH）把文字按钮的 gravity 设成 start|center。
                    textAlign = if (rightAction != null) TextAlign.Start else TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
            rightAction?.let {
                SmartisanActionButtonGroupIconButton(
                    action = it,
                    modifier =
                        Modifier.padding(start = SmartisanActionButtonGroupDefaults.IconButtonGap),
                )
            }
        }
    }
}



/** 文字按钮（原版 `ShadowButton` + `SmallButton.Filter` / `SmallButton.Standard`）。 */
@Composable
private fun SmartisanActionButtonGroupTextButton(
    action: SmartisanActionButton,
    index: Int,
    count: Int,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val enabled = action.enabled
    val backgroundRes = smartisanActionButtonGroupBackground(index = index, count = count)
    val textColor =
        if (count > 1) {
            rememberSmartisanStateListColor(
                colorRes = R.color.filter_button_text_color,
                enabled = enabled,
                pressed = pressed,
            )
        } else {
            colorResource(R.color.title_or_btn_text_color)
        }
    val shadowColor =
        if (count > 1) {
            rememberSmartisanStateListColor(
                colorRes = R.color.filter_button_text_shadow_colors,
                enabled = enabled,
                pressed = pressed,
            )
        } else {
            Color.Transparent
        }
    Box(
        modifier =
            modifier
                .height(SmartisanActionButtonGroupDefaults.ActionHeight)
                .widthIn(min = SmartisanActionButtonGroupDefaults.ActionMinWidth)
                .smartisanDrawableBackground(
                    drawableRes = backgroundRes,
                    enabled = enabled,
                    pressed = pressed,
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = action.onClick,
                )
                .graphicsLayer {
                    alpha = if (enabled) 1f else SmartisanActionButtonGroupDefaults.DisabledAlpha
                },
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = action.text.orEmpty(),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = SmartisanActionButtonGroupDefaults.TextSize,
            // 原版 SmallButton.Filter：阴影偏移 (0, -2px)、模糊 0.1px。
            style =
                TextStyle(
                    shadow =
                        Shadow(
                            color = shadowColor,
                            offset = Offset(0f, -2f),
                            blurRadius = 0.1f,
                        ),
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
        )
    }
}

/** 图标按钮（原版 `ImageButton` + `selector_small_btn_standard`，图标内缩 9dp / 7dp）。 */
@Composable
private fun SmartisanActionButtonGroupIconButton(
    action: SmartisanActionButton,
    modifier: Modifier = Modifier,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val enabled = action.enabled
    Box(
        modifier =
            modifier
                .size(
                    SmartisanActionButtonGroupDefaults.IconButtonWidth,
                    SmartisanActionButtonGroupDefaults.IconButtonHeight,
                )
                .smartisanDrawableBackground(
                    drawableRes = SmartisanDrawables.ButtonTabGroupStandard,
                    enabled = enabled,
                    pressed = pressed,
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = action.onClick,
                )
                .padding(
                    start = SmartisanActionButtonGroupDefaults.IconInsetStart,
                    end = SmartisanActionButtonGroupDefaults.IconInsetEnd,
                )
                .graphicsLayer {
                    alpha = if (enabled) 1f else SmartisanActionButtonGroupDefaults.DisabledAlpha
                },
        contentAlignment = Alignment.Center,
    ) {
        val iconRes = action.iconRes
        if (iconRes != null) {
            SmartisanIcon(
                res = iconRes,
                contentDescription = action.contentDescription,
                enabled = enabled,
            )
        }
    }
}

/**
 * 按原版 `setButtonsAppearance` 选文字按钮的底图：
 * 多个按钮时按位置取分段的左 / 中 / 右，只有一个时取整块的 standard。
 */
@DrawableRes
private fun smartisanActionButtonGroupBackground(index: Int, count: Int): Int =
    when {
        count <= 1 -> SmartisanDrawables.ButtonTabGroupStandard
        index == 0 -> SmartisanDrawables.ButtonTabGroupFilterLeft
        index == count - 1 -> SmartisanDrawables.ButtonTabGroupFilterRight
        else -> SmartisanDrawables.ButtonTabGroupFilterMiddle
    }
