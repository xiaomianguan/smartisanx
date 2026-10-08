package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格标签（芯片）。
 *
 * 对应 framework 里的 `smartisanos.widget.ChipsView` / `ShadowChipsView`
 * （`framework/smartisanos.jar` 的 `classes.dex`）。
 * 用于联系人标签、搜索历史、热门词、分类这类「一组可点的短标签」。
 *
 * 素材取自 framework 资源 `framework-smartisanos-res.apk`：
 *
 * | 资源 | 用途 |
 * | --- | --- |
 * | `chips_normal` / `chips_normal_pressed` | 常规标签底图（nine-patch） |
 * | `chips_remove` / `chips_remove_pressed` | 带删除叉的标签底图 |
 * | `chips_normal_colorlist` 等 | 文字颜色状态表（按用途分 7 套） |
 *
 * ```kotlin
 * SmartisanChips(chips = listOf("工作", "家人", "重要"), onChipClick = { /* ... */ })
 * ```
 *
 * @param removable 是否在标签右侧显示删除叉（原版 `chips_remove_*`）
 */
@Composable
fun SmartisanChips(
    chips: List<String>,
    modifier: Modifier = Modifier,
    onChipClick: (String) -> Unit = {},
    onChipRemove: ((String) -> Unit)? = null,
    selected: Set<String> = emptySet(),
    chipHeight: Dp = 28.dp,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        chips.forEach { chip ->
            SmartisanChip(
                text = chip,
                selected = chip in selected,
                onClick = { onChipClick(chip) },
                onRemove = onChipRemove?.let { { it(chip) } },
                height = chipHeight,
            )
        }
    }
}

/**
 * 单个标签。
 *
 * @param selected 选中时用强调色底图（原版 `chips_hot_*` 一类）
 * @param onRemove 非空时右侧出现删除叉
 */
@Composable
fun SmartisanChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onRemove: (() -> Unit)? = null,
    height: Dp = 28.dp,
    onClick: (() -> Unit)? = null,
    @DrawableRes backgroundRes: Int? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }
    val background =
        when {
            backgroundRes != null -> Modifier.smartisanDrawableBackground(backgroundRes, enabled = enabled, pressed = pressed)
            else ->
                Modifier.background(
                    color =
                        when {
                            !enabled -> colors.surfaceDisabled
                            selected || pressed -> colors.accent
                            else -> colors.surfaceRaised
                        },
                    shape = RoundedCornerShape(height / 2),
                )
        }
    Row(
        modifier =
            modifier
                .heightIn(min = height)
                .then(background)
                .then(if (onClick != null && enabled) Modifier.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = click) else Modifier)
                .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SmartisanText(
            text = text,
            style = typography.listItemSecondary,
            color = if (selected) colors.onAccent else colors.textSecondary,
            maxLines = 1,
        )
        if (onRemove != null) {
            Box(
                modifier = Modifier.size(14.dp).clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                SmartisanIcon(
                    res = R.drawable.chips_ic_close_24dp,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                )
            }
        }
    }
}
