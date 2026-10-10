package cc.wuersan008.smartisanx.ui.input

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 消息输入栏（framework `smartisanos.widget.MessageField` + `res/layout/message_field.xml`）。
 *
 * 原版布局是一个 `merge`，横向三段，左右留白都用 `bar_margin_edge`（6dp）：
 *
 * | 部件 | 原版规则 |
 * | --- | --- |
 * | 左图标 `left_iv` | `StandardIconStyle` = 36dp（`standard_icon_size`）、`centerInside`、左边距 6dp、竖直居中 |
 * | 输入区 `message_field_edit_layout` | 底图 `message_field.9.png`（固有 146×96px @xxhdpi ⇒ 32dp 高）、左边距 12dp（`search_bar_margin_each`）、上下外边距 8.33dp（`message_field_margin_top_bottom`）、夹在左右图标之间 |
 * | 输入框 `message_field_editor` | 透明底、13.5sp（`input_editor_text_size`）、左内边距 12dp（复用 `hidden_list_action_left_right_padding`）、右内边距 6dp（`mid_container_top_bottom_padding`；有表情图标时 5dp）、上下 6dp |
 * | 表情图标 `message_field_emoji_icon` | 默认 `gone`，背景 `message_field_emoji_selector`，贴输入区右侧 |
 * | 发送按钮 `send_button` | 36dp、底图 `selector_small_icon_send`（绿色箭头）、左边距 12dp、右边距 6dp，**输入为空时禁用** |
 *
 * 行为照抄原版 `MessageField`：
 *
 * - 发送按钮只有输入非空才可用（原版在 `afterTextChanged` 里 `setEnabled(s.length() > 0)`），
 *   禁用态走 selector 的 `icon_send_disabled`；
 * - 长度上限默认 2000（原版 `integer/message_field_editor_max_length`），超出直接不接收；
 * - 左边图标可隐藏（原版 `setLeftImageViewVisible`）：隐藏时输入区左边距从 12dp 变 6dp
 *   （`title_bar_margin_view` ↔ `bar_margin_edge`，即原版 `adjustMessageFiledLayoutParams`）；
 * - 表情图标由 [emojiIconRes] 决定是否显示，显示时输入框右内边距从 6dp 变 5dp
 *   （原版 `updateEditorPaddingRight`）；
 * - 底栏上边缘压着原版投影 `bottom_bar_shadow`（11dp）+ 0.67dp 分隔线
 *   （原版 `BarsHelper` 的 `SHADOW_BOTTOM_TYPE`，投影与分隔线各自向上平移自身高度）。
 *
 * 与原版的差异（都写在这里，代码里不再各自注释）：
 *
 * - 原版 `Listener` 把 `beforeTextChanged` / `onTextChanged` / `afterTextChanged` 分开回调，
 *   Compose 只有 [onValueChange] 一个口子，前两个语义没有对应物，因此合并；
 * - 原版不给背景时 `setBackgroundColor(-1)`（纯白），这里用主题 `surface`，跟随深色主题；
 * - 文字色原版 `editor_text_color`（#cc000000）→ 主题 `textPrimary`，提示色
 *   `editor_hint_text_color` → 主题 `textHint`，光标用主题 `accent`（原版是 NinePatch 光标条）；
 * - 原版不管导航栏 insets（交给各 App 的布局），这里同样不管：需要时自己给 [modifier] 加
 *   `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` / `Modifier.imePadding()`。
 *
 * ```kotlin
 * SmartisanMessageField(
 *     value = text,
 *     onValueChange = { text = it },
 *     onSend = { send(text); text = "" },
 *     hint = "输入消息",
 * )
 * ```
 *
 * @param value 当前文字
 * @param onValueChange 文字变化（超过 [maxLength] 时不会回调）
 * @param onSend 点发送；输入为空时按钮禁用、不会触发
 * @param hint 提示文字（原版 `hintText` 属性）
 * @param maxLength 最大长度，原版 `message_field_editor_max_length` = 2000
 * @param maxLines 输入框最大行数（原版 `editorMaxLine`），多行时输入区跟着长高
 * @param leftIconRes 左图标；`null` 表示不显示
 * @param emojiIconRes 表情图标；`null` 表示不显示
 * @param showShadow 是否画底栏投影 + 分隔线
 * @param backgroundRes 输入区底图（原版 `message_field.9.png`）
 * @param sendIconRes 发送按钮底图（原版 `selector_small_icon_send`）
 */

@Composable
fun SmartisanMessageField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSend: () -> Unit = {},
    enabled: Boolean = true,
    hint: String? = null,
    maxLength: Int = 2000,
    maxLines: Int = 1,
    @DrawableRes leftIconRes: Int? = SmartisanDrawables.MessageFieldAddIcon,
    onLeftIconClick: (() -> Unit)? = null,
    @DrawableRes emojiIconRes: Int? = null,
    onEmojiClick: (() -> Unit)? = null,
    showShadow: Boolean = true,
    @DrawableRes backgroundRes: Int = SmartisanDrawables.MessageFieldBackground,
    @DrawableRes sendIconRes: Int = SmartisanDrawables.MessageFieldSendIcon,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 13.5.sp),
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send,
        ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = LocalSmartisanColors.current
    val density = LocalDensity.current
    // 输入区底图固有高度：原版是 wrap_content，量到的就是 NinePatch 的固有尺寸（约 32dp）。
    val fieldHeight =
        with(density) { rememberSmartisanDrawablePainter(backgroundRes).intrinsicSize.height.toDp() }
    val shadowHeight =
        with(density) {
            rememberSmartisanDrawablePainter(SmartisanDrawables.BottomBarShadowPlain)
                .intrinsicSize.height.toDp()
        }
    val canSend = enabled && value.isNotEmpty()
    // 原版 adjustMessageFiledLayoutParams：有左图标时输入区左边距 title_bar_margin_view（12dp），
    // 没有时退回 bar_margin_edge（6dp）。
    val fieldStartMargin =
        if (leftIconRes != null) {
            SmartisanDimens.ComboTitleCenterMargin
        } else {
            SmartisanDimens.TitleBarHorizontalMargin
        }
    // 原版 updateEditorPaddingRight：有表情图标时右内边距用 message_field_right_emoji_padding（5dp）。
    val fieldEndPadding =
        if (emojiIconRes != null) {
            SmartisanDimens.MessageFieldEmojiPadding
        } else {
            SmartisanDimens.FieldInnerPadding
        }


    Box(modifier.fillMaxWidth().background(colors.surface)) {
        if (showShadow) {
            // 原版 BarsHelper：底栏投影与分隔线都贴上边缘，投影向上平移自身高度（画在栏外）。
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .heightIn(min = shadowHeight)
                    .offset(y = -shadowHeight)
                    .smartisanDrawableBackground(SmartisanDrawables.BottomBarShadowPlain),
            )
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .heightIn(min = SmartisanDimens.DividerThickness)
                    .background(colors.divider),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leftIconRes != null) {
                val leftClick = onLeftIconClick
                SmartisanIcon(
                    res = leftIconRes,
                    contentDescription = "添加",
                    modifier =
                        Modifier
                            .padding(start = SmartisanDimens.TitleBarHorizontalMargin)
                            .then(
                                if (leftClick != null) {
                                    Modifier.smartisanClickable(enabled = enabled, onClick = leftClick)
                                } else {
                                    Modifier
                                },
                            ),
                    enabled = enabled,
                    size = SmartisanDimens.IconSize,
                )
            }
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = fieldStartMargin, end = SmartisanDimens.ComboTitleCenterMargin)
                        .padding(vertical = SmartisanDimens.MessageFieldFieldGap)
                        .heightIn(min = fieldHeight)
                        .smartisanDrawableBackground(backgroundRes),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    textStyle = textStyle.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    singleLine = maxLines <= 1,
                    maxLines = maxLines,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier =
                                Modifier.fillMaxWidth().padding(
                                    start = SmartisanDimens.HiddenActionSidePadding,
                                    end = fieldEndPadding,
                                    top = SmartisanDimens.FieldInnerPadding,
                                    bottom = SmartisanDimens.FieldInnerPadding,
                                ),
                        ) {
                            if (value.isEmpty() && hint != null) {
                                SmartisanText(
                                    text = hint,
                                    style = textStyle,
                                    color = colors.textHint,
                                    maxLines = 1,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (emojiIconRes != null) {
                    val emojiClick = onEmojiClick
                    SmartisanIcon(
                        res = emojiIconRes,
                        contentDescription = "表情",
                        modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .then(
                                    if (emojiClick != null) {
                                        Modifier.smartisanClickable(enabled = enabled, onClick = emojiClick)
                                    } else {
                                        Modifier
                                    },
                                ),
                        enabled = enabled,
                    )
                }
            }
            SmartisanIcon(
                res = sendIconRes,
                contentDescription = "发送",
                modifier =
                    Modifier
                        .padding(
                            start = SmartisanDimens.ComboTitleCenterMargin,
                            end = SmartisanDimens.TitleBarHorizontalMargin,
                        )
                        .smartisanClickable(enabled = canSend) { onSend() },
                enabled = canSend,
                size = SmartisanDimens.IconSize,
            )
        }
    }
}
