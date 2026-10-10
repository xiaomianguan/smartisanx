package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 取该位置对应的编辑行底图（原版 `EditorStyle.Single` / `.Top` / `.Mid` / `.Bottom`，
 * 分别指向 `editor_bg_single` / `_top` / `_middle` / `_bottom`）。
 */
@DrawableRes
fun smartisanEditorRowBackground(position: SmartisanGroupRowPosition): Int =
    when (position) {
        SmartisanGroupRowPosition.Single -> SmartisanDrawables.EditorRowSingle
        SmartisanGroupRowPosition.Top -> SmartisanDrawables.EditorRowTop
        SmartisanGroupRowPosition.Middle -> SmartisanDrawables.EditorRowMiddle
        SmartisanGroupRowPosition.Bottom -> SmartisanDrawables.EditorRowBottom
    }

/**
 * framework 编辑行（`smartisanos.widget.editor.AbsEditor` + `res/layout/abs_editor_layout.xml`）。
 *
 * 这是设置页里「标签 + 输入框 + 右侧说明 / 图标」那一行的母版。原版整行是一张按位置取的
 * 9-patch（`editor_bg_*`，由 `EditorStyle.*` 四套样式给出），左右内边距 6dp
 * （`editor_horizontal_padding`），最小高度 44dp；三块内容分别是：
 *
 * - 左槽：默认 [SmartisanEditorLabel]（12sp、`editor_label_text_color`、左边距 12dp），
 *   也可以用 [leading] 换成别的（复选框、图标……）；
 * - 中槽：默认一个无底图的输入框（15sp、`editor_text_color` / `editor_hint_text_color`，
 *   左右外边距 12dp、上下 6dp，即 `EditorTextStyle`）；原版还有密码 / 可清空两个变体
 *   （`pwd_edit_text` / `quick_del_edit_text`），本库对应在 [content] 里放
 *   `SmartisanPasswordField` / `SmartisanClearableField`；
 * - 右槽：原版放 [SmartisanEditorRightIcon]（右对齐说明文字 + 图标），
 *   也可以用开关、箭头等任意内容。
 *
 * 与原版一致：当左右两槽都没给、只有输入框时，整行可点，点击会把焦点给输入框
 * （原版 `AbsEditor` 在这种情况下 `setOnClickListener(this)`，`onClick` 里 `requestFocus`）。
 *
 * @param position 行在分组里的位置，决定用哪张底图。
 * @param value 输入框内容，仅在 [content] 为 `null` 时使用。
 * @param onValueChange 输入回调，仅在 [content] 为 `null` 时使用。
 * @param placeholder 输入框提示文字（原版 `setHint`）。
 * @param content 自定义中槽；给了它就不再画内置输入框。
 */
@Composable
fun SmartisanEditorRow(
    modifier: Modifier = Modifier,
    label: String? = null,
    position: SmartisanGroupRowPosition = SmartisanGroupRowPosition.Single,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    content: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val focusRequester = remember { FocusRequester() }
    val click = smartisanClick { focusRequester.requestFocus() }
    // 原版只在「没有左右小部件、只有编辑框」时把整行做成可点。
    val clickableRow = content == null && leading == null && trailing == null && enabled
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .smartisanDrawableBackground(
                    drawableRes = smartisanEditorRowBackground(position),
                    enabled = enabled,
                    pressed = pressed && enabled,
                )
                .heightIn(min = SmartisanDimens.EditorRowMinHeight)
                .then(
                    if (clickableRow) {
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = true,
                            onClick = click,
                        )
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = SmartisanDimens.EditorHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> leading()
            label != null -> SmartisanEditorLabel(text = label, enabled = enabled)
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (content != null) {
                content()
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange?.invoke(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(
                                horizontal = SmartisanDimens.EditorFieldHorizontalMargin,
                                vertical = SmartisanDimens.EditorFieldVerticalMargin,
                            ),
                    enabled = enabled,
                    singleLine = singleLine,
                    textStyle =
                        typography.editorField.copy(
                            color = if (enabled) colors.textPrimary else colors.textDisabled,
                        ),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = { inner ->
                        if (value.isEmpty() && placeholder != null) {
                            SmartisanText(
                                text = placeholder,
                                style = typography.editorField,
                                color = colors.textHint,
                                maxLines = 1,
                            )
                        }
                        inner()
                    },
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * 编辑行左侧的标签（原版 `EditorLeftLabelWidget` + `editor_left_label_layout.xml`）。
 *
 * 结构照抄原版：横向一行、垂直居中、最小高度 40dp（`editor_left_right_widget_min_height`），
 * 从左到右是「图标容器（40dp × 44dp，`editor_left_icon_container_*`）+ 标签 + 箭头」：
 *
 * - 图标 26dp 居中放在容器里，容器可以给一张底图（原版 `setIconContainerBackground`）；
 * - 容器右侧可选一条 2px 分隔线（原版 `setShowDivider`，颜色取主题 `divider`；
 * - 标签 12sp、`editor_label_text_color`、左边距固定 12dp（`editor_element_margin_left_right`）；
 * - 箭头默认不显示；显示时标签右边距变 0、箭头自己留 12dp（原版 `setArrowVisible` 就是这么切的）。
 *
 * @param iconRes 左侧图标；为 `null` 时不占位（原版把图标容器设为 `gone`）。
 * @param iconContainerBackground 图标容器底图（原版 `editor_left_icon_bg_*`）。
 * @param showDivider 是否画图标容器右侧的 2px 分隔线。
 * @param showArrow 是否画标签右侧的箭头（原版 `arrow_icon`）。
 */
@Composable
fun SmartisanEditorLabel(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    @DrawableRes iconContainerBackground: Int? = null,
    showDivider: Boolean = false,
    showArrow: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }
    Row(
        modifier =
            modifier
                .heightIn(min = SmartisanDimens.EditorWidgetMinHeight)
                .then(
                    if (onClick != null && enabled) {
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = true,
                            onClick = click,
                        )
                    } else {
                        Modifier
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Box(
                modifier =
                    Modifier
                        .size(
                            width = SmartisanDimens.EditorLeftIconContainerWidth,
                            height = SmartisanDimens.EditorLeftIconContainerHeight,
                        )
                        .then(
                            if (iconContainerBackground != null) {
                                Modifier.smartisanDrawableBackground(
                                    drawableRes = iconContainerBackground,
                                    enabled = enabled,
                                    pressed = pressed && enabled,
                                )
                            } else {
                                Modifier
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                SmartisanIcon(
                    res = iconRes,
                    contentDescription = null,
                    enabled = enabled,
                    size = SmartisanDimens.EditorLeftIconSize,
                )
                if (showDivider) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(SmartisanDimens.EditorInnerDividerWidth)
                                .background(colors.divider),
                    )
                }
            }
        }
        SmartisanText(
            text = text,
            modifier =
                Modifier.padding(
                    start = SmartisanDimens.EditorElementMargin,
                    end = if (showArrow) 0.dp else SmartisanDimens.EditorElementMargin,
                ),
            style = typography.editorLabel,
            color = if (enabled) colors.textTertiary else colors.textDisabled,
            maxLines = 1,
        )
        if (showArrow) {
            SmartisanIcon(
                res = SmartisanDrawables.ListItemArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = SmartisanDimens.EditorElementMargin),
                enabled = enabled,
            )
        }
    }
}

/**
 * 编辑行右侧的「说明文字 + 图标」（原版 `EditorRightIconWidget` +
 * `editor_right_icon_widget_layout.xml`）。
 *
 * 横向一行、垂直居中、最小高度 40dp；从左到右是「标签 + 可选 2px 分隔线 + 图标」：
 *
 * - 标签 12sp、`editor_label_text_color`、左边距 12dp、最宽 150dp、超出省略（原版写死的值）；
 * - 分隔线 2px（取主题 `divider`，浅色 `#E9E9E9` = 原版 `list_divider_color` 8% 黑压在白底上的合成值），默认不显示；
 * - 图标左边距 6dp（`editor_horizontal_padding`），**分隔线显示时左边距变 0**
 *   （原版 `setDevideVisible` 就是这么切的），尺寸取素材固有大小。
 */
@Composable
fun SmartisanEditorRightIcon(
    modifier: Modifier = Modifier,
    text: String? = null,
    @DrawableRes iconRes: Int? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val click = smartisanClick { onClick?.invoke() }
    Row(
        modifier =
            modifier
                .heightIn(min = SmartisanDimens.EditorWidgetMinHeight)
                .then(
                    if (onClick != null && enabled) {
                        Modifier.clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = true,
                            onClick = click,
                        )
                    } else {
                        Modifier
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text != null) {
            SmartisanText(
                text = text,
                modifier =
                    Modifier
                        .widthIn(max = SmartisanDimens.EditorRightLabelMaxWidth)
                        .padding(start = SmartisanDimens.EditorElementMargin),
                style = typography.editorLabel,
                color = if (enabled) colors.textTertiary else colors.textDisabled,
                maxLines = 1,
            )
        }
        if (showDivider) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(SmartisanDimens.EditorInnerDividerWidth)
                        .background(colors.divider),
            )
        }
        if (iconRes != null) {
            SmartisanIcon(
                res = iconRes,
                contentDescription = null,
                modifier =
                    Modifier.padding(
                        start = if (showDivider) 0.dp else SmartisanDimens.EditorHorizontalPadding,
                    ),
                enabled = enabled,
            )
        }
    }
}
