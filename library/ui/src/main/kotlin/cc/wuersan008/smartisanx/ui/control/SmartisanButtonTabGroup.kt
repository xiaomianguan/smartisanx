/**
 * 控件：锤子分段按钮组。
 *
 * 对应原版 `smartisanos.widget.ButtonTabGroup`（首 / 中 / 尾三段各自圆角不同的分段按钮），
 * 原版出现在锤子日历（Calendar 8.1.2）、短信（Messages 40）、音乐（Music 8.1.0）、
 * 便签（Notes 7.3.1）、录音机（Recorder 8.1.0）五个 App 的标题栏。
 *
 * 还原要点（照抄原版 `ButtonTabGroup.java` 与 `values/styles.xml`）：
 * - 每个分段 `layout_weight = 1`（等宽），第 2 个起 `leftMargin = -6dp`
 *   （原版 `dimen/button_tab_group_each_gap`），让相邻分段的投影互相压住、不出现双边框；
 * - 连续分段（`hasGap = false`）用 `selector_small_btn_filter_left / _middle / _right`
 *   与 `SmallButton.Filter` 样式；有间距（`hasGap = true`）或只有一个分段时，
 *   改用 `selector_small_btn_standard` 与 `SmallButton.Standard` 样式；
 * - 文字 13.5sp 加粗、左右各 12dp 内边距、最小高度 36dp（standard 还带最小宽度 66dp），
 *   单行、超长省略；
 * - 图标走「原版 `InsetDrawable`」的左 9dp / 右 7dp 内缩
 *   （原版 `dimen/action_button_icon_inset_left / _right`）；
 * - Filter 样式的文字阴影取自 `color/filter_button_text_shadow_colors`：
 *   按下 / 选中时 `#26000000`、偏移 (0, -2px)、模糊 0.1px，其余状态透明；
 * - 选中态由 `state_activated` 驱动（原版 `setButtonActivated`）；点击已选中分段时，
 *   只有 `hasGap` 或 `alwaysKeepClickListen` 才回调（原版 `setButtonActivatedInner`）。
 *
 * 与本库自研的 `ui.layout.SmartisanTabRow` 的区别：那是文字标签条（原版没有对应实现），
 * 这里是原版的分段按钮组，底图与阴影都是原版素材。
 */
package cc.wuersan008.smartisanx.ui.control

import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableState
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.jvm.JvmName
import kotlin.math.roundToInt

/**
 * 分段按钮组里的一个分段。
 *
 * @param text 分段文字，对应原版 `setButtonGroupData(List<String>)`。
 * @param iconRes 分段左侧图标，对应原版 `setButtonDrawable(List<Integer>)`；为 null 时只有文字。
 */
@Immutable
data class SmartisanButtonTabGroupItem(
    val text: String,
    @DrawableRes val iconRes: Int? = null,
)

/** 分段左右内边距，原版 `SmallButton` 样式的 `paddingLeft/Right = 12dp`。 */
private val SegmentHorizontalPadding = 12.dp

/** 分段最小高度，原版 `SmallButton` 样式的 `minHeight = 36dp`。 */
private val SegmentMinHeight = 36.dp

/** standard 样式的最小宽度，原版 `SmallButton.Standard` 的 `minWidth = 66dp`。 */
private val StandardSegmentMinWidth = 66.dp

/** 分段文字字号，原版 `SmallButton` 样式的 `textSize = 13.5sp`。 */
private val SegmentTextSize = 13.5.sp

/** 相邻分段的重叠量，原版 `dimen/button_tab_group_each_gap = 6dp`（原版取负作为左边距）。 */
private val SegmentOverlap = 6.dp

/** 分段图标左侧内缩，原版 `dimen/action_button_icon_inset_left = 9dp`。 */
private val IconInsetStart = 9.dp

/** 分段图标与文字之间的内缩，原版 `dimen/action_button_icon_inset_right = 7dp`。 */
private val IconInsetEnd = 7.dp

/** 文字阴影纵向偏移，原版 `shadowDy = -2`（TextView 的阴影单位是物理像素）。 */
private const val TextShadowDy = -2f

/** 文字阴影模糊半径，原版 `shadowRadius = 0.1`。 */
private const val TextShadowRadius = 0.1f

/** 禁用分段的透明度，原版 `setEnabled(i, false)` 里的 `setAlpha(0.3f)`。 */
private const val DisabledAlpha = 0.3f


/**
 * 锤子分段按钮组。
 *
 * ```kotlin
 * SmartisanButtonTabGroup(
 *     items = listOf("全部", "今天"),
 *     selectedIndex = selected,
 *     onSelectedChange = { selected = it },
 * )
 * ```
 *
 * @param items 分段内容，按顺序对应原版 `setButtonGroupData`。
 * @param selectedIndex 当前选中分段（原版 `setButtonActivated`），越界会被收敛。
 * @param onSelectedChange 选中分段变化回调，对应原版
 *   `OnButtonGroupItemClickListener#onButtonGroupItemClick`。
 * @param hasGap 是否用 standard 样式（每个分段独立底图），对应原版
 *   `setButtonGroupData(list, hasGap)`；false 时用 left / middle / right 三段拼接底图。
 * @param alwaysKeepClickListen 点击已选中分段时是否依然回调，对应原版 `setAlwaysKeepClickListen`。
 * @param itemWidth 单个分段的固定宽度；为 null 时按原版 `layout_weight = 1` 等分可用宽度。
 * @param disabledIndices 禁用分段下标，对应原版 `setEnabled(index, false)`（透明度 0.3）。
 * @param contentColor 文字颜色覆盖；不指定时按原版 `res/color/filter_button_text_color`
 *   与 `res/color/title_or_btn_text_color` 取色。
 */
@Composable
fun SmartisanButtonTabGroup(
    items: List<SmartisanButtonTabGroupItem>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hasGap: Boolean = false,
    alwaysKeepClickListen: Boolean = false,
    itemWidth: Dp? = null,
    disabledIndices: Set<Int> = emptySet(),
    contentColor: Color = Color.Unspecified,
) {
    if (items.isEmpty()) return
    val count = items.size
    val safeIndex = selectedIndex.coerceIn(0, count - 1)
    // 原版 generateShadowButton：只有一个分段时也走 standard 样式（mBtnCount == 1 分支）。
    val standardStyle = hasGap || count == 1
    val density = LocalDensity.current
    val overlap = if (hasGap && count > 1) with(density) { SegmentOverlap.roundToPx() } else 0
    val fixedSegmentWidth = itemWidth?.let { with(density) { it.roundToPx() } }
    Layout(
        modifier = modifier,
        content = {
            items.forEachIndexed { index, item ->
                SmartisanButtonTabGroupSegment(
                    item = item,
                    index = index,
                    count = count,
                    standardStyle = standardStyle,
                    activated = index == safeIndex,
                    enabled = index !in disabledIndices,
                    contentColor = contentColor,
                    onClick = {
                        // 原版 setButtonActivatedInner：点到已选中项时按 alwaysKeepClickListen 决定是否回调。
                        if (index != safeIndex || hasGap || alwaysKeepClickListen) {
                            onSelectedChange(index)
                        }
                    },
                )
            }
        },
    ) { measurables, constraints ->
        val resolvedSegmentWidth =
            fixedSegmentWidth
                ?: if (constraints.hasBoundedWidth) {
                    ((constraints.maxWidth + overlap * (count - 1)).toFloat() / count).roundToInt()
                } else {
                    with(density) { StandardSegmentMinWidth.roundToPx() }
                }
        val minHeight = with(density) { SegmentMinHeight.roundToPx() }
        val childConstraints =
            Constraints(
                minWidth = resolvedSegmentWidth,
                maxWidth = resolvedSegmentWidth,
                minHeight = minHeight,
                maxHeight = constraints.maxHeight,
            )
        val placeables = measurables.map { it.measure(childConstraints) }
        val width =
            if (fixedSegmentWidth != null) {
                resolvedSegmentWidth * count - overlap * (count - 1)
            } else {
                constraints.maxWidth
            }
        val height = placeables.maxOfOrNull { it.height } ?: minHeight
        layout(width = width, height = height) {
            var x = 0
            placeables.forEachIndexed { index, placeable ->
                if (index > 0) x -= overlap
                placeable.placeRelative(x, 0)
                x += placeable.width
            }
        }
    }
}


/**
 * 分段按钮组的字符串重载（不带图标），等价于原版 `setButtonGroupData(List<String>)`。
 *
 * [List] 在 JVM 上会擦除成同一个签名，因此与
 * [SmartisanButtonTabGroupItem] 版重载用 `@JvmName` 区分；
 * Kotlin 调用方仍然写 `SmartisanButtonTabGroup(...)`，只是 Java 调用方会看到另一个名字。
 */
@Composable
@JvmName("SmartisanButtonTabGroupOfStrings")
fun SmartisanButtonTabGroup(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hasGap: Boolean = false,
    alwaysKeepClickListen: Boolean = false,
    itemWidth: Dp? = null,
    disabledIndices: Set<Int> = emptySet(),
    contentColor: Color = Color.Unspecified,
) {
    SmartisanButtonTabGroup(
        items = items.map { SmartisanButtonTabGroupItem(it) },
        selectedIndex = selectedIndex,
        onSelectedChange = onSelectedChange,
        modifier = modifier,
        hasGap = hasGap,
        alwaysKeepClickListen = alwaysKeepClickListen,
        itemWidth = itemWidth,
        disabledIndices = disabledIndices,
        contentColor = contentColor,
    )
}

@Composable
private fun SmartisanButtonTabGroupSegment(
    item: SmartisanButtonTabGroupItem,
    index: Int,
    count: Int,
    standardStyle: Boolean,
    activated: Boolean,
    enabled: Boolean,
    contentColor: Color,
    onClick: () -> Unit,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val backgroundRes = segmentBackground(standardStyle, index, count)
    val textColor =
        when {
            contentColor != Color.Unspecified -> contentColor
            standardStyle -> colorResource(R.color.title_or_btn_text_color)
            else ->
                smartisanStateListColor(
                    colorRes = R.color.filter_button_text_color,
                    enabled = enabled,
                    pressed = pressed,
                    activated = activated,
                )
        }
    val shadowColor =
        if (standardStyle) {
            Color.Transparent
        } else {
            smartisanStateListColor(
                colorRes = R.color.filter_button_text_shadow_colors,
                enabled = enabled,
                pressed = pressed,
                activated = activated,
            )
        }
    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .smartisanDrawableBackground(
                    drawableRes = backgroundRes,
                    enabled = enabled,
                    pressed = pressed,
                    activated = activated,
                )
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(horizontal = SegmentHorizontalPadding)
                // 原版 setEnabled(i, false) 把整个按钮的透明度降到 0.3。
                .graphicsLayer { alpha = if (enabled) 1f else DisabledAlpha },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconRes = item.iconRes
            if (iconRes != null) {
                SmartisanIcon(
                    res = iconRes,
                    contentDescription = null,
                    modifier = Modifier.padding(start = IconInsetStart, end = IconInsetEnd),
                )
            }
            SmartisanText(
                text = item.text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = SegmentTextSize,
                // 原版 SmallButton.Filter：阴影偏移 (0, -2px)、模糊 0.1px，其余状态透明。
                style =
                    TextStyle(
                        shadow =
                            Shadow(
                                color = shadowColor,
                                offset = Offset(0f, TextShadowDy),
                                blurRadius = TextShadowRadius,
                            ),
                    ),
                maxLines = 1,
            )
        }
    }
}



/** 按原版 `generateShadowButton` 选择分段底图。 */
@DrawableRes
private fun segmentBackground(standardStyle: Boolean, index: Int, count: Int): Int =
    when {
        standardStyle -> SmartisanDrawables.ButtonTabGroupStandard
        index == 0 -> SmartisanDrawables.ButtonTabGroupFilterLeft
        index == count - 1 -> SmartisanDrawables.ButtonTabGroupFilterRight
        else -> SmartisanDrawables.ButtonTabGroupFilterMiddle
    }

/**
 * 读取原版 `res/color/` 下的状态色（支持按下 / 选中 / 禁用）。
 *
 * 与弹窗组件里的 `rememberSmartisanStateColor` 同一套做法，这里额外支持 `state_activated`
 * —— 分段按钮组的选中态正是用它（原版 `ShadowButton#setActivated`）。
 */
@Composable
private fun smartisanStateListColor(
    @ColorRes colorRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    activated: Boolean = false,
): Color {
    val context = LocalContext.current
    val stateList: ColorStateList? =
        remember(context, colorRes) { ContextCompat.getColorStateList(context, colorRes) }
    return remember(context, colorRes, stateList, enabled, pressed, activated) {
        // 与弹窗按钮同理：空状态集会让 `state_enabled="false"` 之类的否定项命中，
        // 必须交给 core 的构造器生成完整状态集。
        val state =
            smartisanDrawableState(
                enabled = enabled,
                pressed = pressed,
                activated = activated,
            )
        val argb =
            stateList?.getColorForState(state, stateList.defaultColor)
                ?: ContextCompat.getColor(context, colorRes)
        Color(argb)
    }
}
