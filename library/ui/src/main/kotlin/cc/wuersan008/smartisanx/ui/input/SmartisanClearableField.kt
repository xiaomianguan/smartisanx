package cc.wuersan008.smartisanx.ui.input

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import androidx.compose.ui.res.stringResource
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 输入框内右侧带「一键清空」按钮的文本输入框。
 *
 * 对应原版 `smartisanos.widget.QuickDeleteEditText`，在锤子日历、邮件两个 APK 里使用
 * （布局 `quick_del_edit_text.xml` / `sos_smartisanos_layout_quick_del_edit_text.xml`，
 * 样式 `EditorTextStyle`，图标 `quick_icon_delete`）。
 *
 * 原版行为：
 * - 清除图标画在输入框内右侧，位置是 `宽 - 图标宽 - (mIconPaddingRight - 父容器右边距)`，
 *   `mIconPaddingRight = 54px`（@xxhdpi 约 18dp）；
 * - 只有「文本非空 **且** 获得焦点」时才画（`updateDrawableVisibility`），
 *   同时 `getCompoundPaddingRight` 才为图标让出宽度；
 * - 按下时把 selector 切到 `quick_icon_delete_pressed`（`state_pressed`），
 *   抬起时清空文本并回调 `OnDeleteIconClickListener`。
 *
 * 清除按钮的绘制与按压态由 [SmartisanClearIcon] 提供 —— 它与搜索栏的清除按钮
 * （原版 `SearchBar` 的 `search_bar_clear_text`）共用同一份实现，不重复写两遍。
 *
 * 说明：原版是 `setVisibility` 瞬时切换、没有动画，这里默认给清除按钮加了原版同类按钮
 * 200ms + `DecelerateInterpolator(1.5f)` 的淡入淡出，传 `animateClearIcon = false` 可完全对齐原版。
 *
 * @param showClearOnlyWhenFocused 是否只在获得焦点时显示清除按钮（原版为 `true`）。
 * @param onClear 清空文本后的额外回调，对应原版 `OnDeleteIconClickListener`。
 */
@Composable
fun SmartisanClearableField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    showClearOnlyWhenFocused: Boolean = true,
    onClear: (() -> Unit)? = null,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 15.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions? = null,
    @DrawableRes clearIconRes: Int = R.drawable.quick_icon_delete,
    clearIconSize: Dp = SmartisanInputDefaults.QuickDeleteIconSize,
    clearPaddingEnd: Dp = SmartisanInputDefaults.QuickDeletePaddingEnd,
    animateClearIcon: Boolean = true,
) {
    val colors = LocalSmartisanColors.current
    val focusManager = LocalFocusManager.current
    val interaction = rememberSmartisanInteractionSource()
    val focused by interaction.collectIsFocusedAsState()
    // 原版 `shouldDrawIcon()`：文本非空且获得焦点才画清除按钮。
    val showClear = value.isNotEmpty() && (!showClearOnlyWhenFocused || focused)
    // 原版 `getCompoundPaddingRight`：图标可见时才为它让出宽度。
    val reservedEnd = if (showClear) clearIconSize + clearPaddingEnd else 0.dp

    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = reservedEnd),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = keyboardOptions,
            keyboardActions =
                keyboardActions
                    ?: KeyboardActions(onDone = { focusManager.clearFocus() }),
            interactionSource = interaction,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        SmartisanText(
                            text = placeholder,
                            style = textStyle,
                            color = colors.textHint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )

        SmartisanClearIcon(
            iconRes = clearIconRes,
            contentDescription = stringResource(R.string.smartisan_clear),
            onClick = {
                onValueChange("")
                onClear?.invoke()
            },
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = clearPaddingEnd),
            size = clearIconSize,
            visible = showClear,
            enabled = enabled,
            animateVisibility = animateClearIcon,
        )
    }
}
