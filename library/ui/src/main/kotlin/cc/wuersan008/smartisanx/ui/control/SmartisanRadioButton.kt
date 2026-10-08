/**
 * 基础控件：锤子风格单选按钮与单选行。
 *
 * 几何取自锤子时钟（XML + 自定义 View）铃声选择里的 `ringtone_picker_radio_*.png`
 * 与锤子音乐（Compose）的 `selector_radio_choice`：细圆环 + 选中时环内的实心圆点，
 * 选中与未选中的比例与原版位图一致（圆点直径约为外环的 0.31）。
 *
 * 原实现依赖位图，这里用 Canvas 重画；颜色取自主题：选中用 `accent` / `accentPressed`，
 * 未选中圆环用 `divider`，禁用时整体降到 30% 透明度（与原版禁用位图一致）。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 单选按钮命中框尺寸，与复选框保持一致。 */
private val RadioHitSize = 36.dp

/** 圆环外径。 */
private val RadioCircleSize = 24.dp

/** 圆环线宽，取自原版位图 2.67dp 按比例换算。 */
private val RadioRingWidth = 2.33.dp

/** 选中时的实心圆点直径，原版比例约为外径的 0.31。 */
private val RadioDotSize = 7.5.dp

/** 禁用时的整体透明度，原版禁用位图同样是 30%。 */
private const val RadioDisabledAlpha = 0.30f

/**
 * 单选按钮的圆环与圆点。
 *
 * @param pressed 按压时圆环与圆点切换到 `accentPressed`。
 */
@Composable
private fun SmartisanRadioIndicator(
    selected: Boolean,
    enabled: Boolean,
    pressed: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val accent = if (pressed) colors.accentPressed else colors.accent
    Canvas(
        modifier =
            modifier
                .size(RadioHitSize)
                .graphicsLayer { alpha = if (enabled) 1f else RadioDisabledAlpha },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = RadioCircleSize.toPx() / 2f
        val strokeWidth = RadioRingWidth.toPx()
        drawCircle(
            color = if (selected) accent else colors.divider,
            radius = radius - strokeWidth / 2f,
            center = center,
            style = Stroke(width = strokeWidth),
        )
        if (selected) {
            drawCircle(color = accent, radius = RadioDotSize.toPx() / 2f, center = center)
        }
    }
}

/**
 * 锤子风格单选按钮。
 *
 * ```kotlin
 * var selected by remember { mutableStateOf(false) }
 * SmartisanRadioButton(selected = selected, onClick = { selected = true })
 * ```
 *
 * @param onClick 传 `null` 时只作为展示；点击已选中的按钮同样会回调，由使用方决定是否忽略。
 */
@Composable
fun SmartisanRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }
    val interactive = enabled && onClick != null
    SmartisanRadioIndicator(
        selected = selected,
        enabled = enabled,
        pressed = pressed && interactive,
        modifier =
            modifier.then(
                if (interactive) {
                    Modifier.selectable(
                        selected = selected,
                        interactionSource = interaction,
                        indication = null,
                        enabled = true,
                        role = Role.RadioButton,
                        onClick = click,
                    )
                } else {
                    Modifier
                },
            ),
    )
}

/**
 * 设置页里的单选行：整行可点，右侧是单选按钮。
 *
 * 按压时整行切换为 `surfacePressed`，不使用涟漪。
 */
@Composable
fun SmartisanRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick(onClick)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SmartisanDimens.ListItemHeight)
                .background(if (pressed && enabled) colors.surfacePressed else Color.Transparent)
                .selectable(
                    selected = selected,
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = click,
                )
                .padding(
                    start = SmartisanDimens.RowContentStart,
                    end = SmartisanDimens.ListItemHorizontalMargin,
                    top = 6.dp,
                    bottom = 6.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            SmartisanText(
                text = text,
                style = typography.listItemPrimary,
                color = if (enabled) colors.textPrimary else colors.textDisabled,
                maxLines = 2,
            )
        }
        SmartisanRadioIndicator(
            selected = selected,
            enabled = enabled,
            pressed = pressed && enabled,
        )
    }
}

