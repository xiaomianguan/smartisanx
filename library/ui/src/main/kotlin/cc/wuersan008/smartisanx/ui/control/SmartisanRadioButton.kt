package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格选择指示器（单选）。
 *
 * **原版的「单选」不是圆环，而是一枚蓝色对勾。**
 * 锤子音乐的主题设置页（`ThemeSettingsPage.kt`）与锤子时钟的铃声选择页
 * （`item_ringtone_picker.xml`）用的是同一张位图：
 * `btn_selected_on_smartisanos_light` / `ringtone_picker_radio_normal` 等，
 * 这几张图在本库里是**同一个文件**（80×80、蓝色对勾）。
 *
 * 原版的行为是：**选中时显示对勾，未选中时什么都不画**（但保留占位）。
 * 因此这里不再手绘圆环 + 圆点，改为直接使用原版位图。
 */
@Composable
fun SmartisanSelectionMark(
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressed: Boolean = false,
    size: androidx.compose.ui.unit.Dp = SelectionMarkSize,
) {
    // 未选中时保留同样大小的占位，避免文字跳动（原版也是这么做的）。
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (selected) {
            SmartisanIcon(
                res = SmartisanDrawables.RadioSelector,
                contentDescription = null,
                enabled = enabled,
                pressed = pressed,
                modifier = Modifier.size(size),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            )
        }
    }
}

/**
 * 单选按钮（仅指示器，不带文字）。
 *
 * 选中显示原版蓝色对勾，未选中留空 —— 与原版一致。
 */
@Composable
fun SmartisanRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    markSize: androidx.compose.ui.unit.Dp = SelectionMarkSize,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }
    val clickable =
        if (onClick != null && enabled) {
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
        }
    SmartisanSelectionMark(
        selected = selected,
        modifier = modifier.then(clickable),
        enabled = enabled,
        pressed = pressed,
        size = markSize,
    )
}

/**
 * 设置页里的单选行：整行可点，右侧显示原版蓝色对勾。
 *
 * 文本与指示器的间距、行高都按原版 `ThemeSettingsPage` 的取值：
 * 文字左 18dp、右 10dp，指示器距右 14dp、尺寸 28dp。
 */
@Composable
fun SmartisanRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    summary: String? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick(onClick)
    val textColor =
        when {
            !enabled -> colors.textDisabled
            pressed -> colors.textSecondary
            else -> colors.textPrimary
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SmartisanDimens.ListItemHeight)
                .selectable(
                    selected = selected,
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.RadioButton,
                    onClick = click,
                )
                .padding(start = RadioTextStart, end = RadioTextEnd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            if (summary == null) {
                SmartisanText(text = text, style = typography.listItemPrimary, color = textColor, maxLines = 1)
            } else {
                androidx.compose.foundation.layout.Column {
                    SmartisanText(text = text, style = typography.listItemPrimary, color = textColor, maxLines = 1)
                    SmartisanText(
                        text = summary,
                        style = typography.listItemSecondary,
                        color = colors.textTertiary,
                        maxLines = 1,
                    )
                }
            }
        }
        SmartisanSelectionMark(
            selected = selected,
            modifier = Modifier.padding(end = RadioMarkEnd),
            enabled = enabled,
            pressed = pressed,
        )
    }
}

/** 指示器尺寸，原版 `ThemeChoiceMarkSize`。 */
private val SelectionMarkSize = 28.dp

/** 行内文字左侧留白，原版 `ThemeChoiceTextStart`。 */
private val RadioTextStart = 18.dp

/** 行内文字右侧留白，原版 `ThemeChoiceTextEnd`。 */
private val RadioTextEnd = 10.dp

/** 指示器距行尾留白，原版 `ThemeChoiceMarkEnd`。 */
private val RadioMarkEnd = 14.dp
