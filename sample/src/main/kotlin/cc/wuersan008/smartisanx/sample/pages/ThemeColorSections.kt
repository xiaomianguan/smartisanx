package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/** 色板一览。 */
@Composable
fun ColorSwatchSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val swatches =
        listOf(
            "pageBackground" to colors.pageBackground,
            "surface" to colors.surface,
            "surfaceRaised" to colors.surfaceRaised,
            "surfacePressed" to colors.surfacePressed,
            "titleBarBackground" to colors.titleBarBackground,
            "divider" to colors.divider,
            "textPrimary" to colors.textPrimary,
            "textSecondary" to colors.textSecondary,
            "textTertiary" to colors.textTertiary,
            "textDisabled" to colors.textDisabled,
            "accent" to colors.accent,
            "accentPressed" to colors.accentPressed,
            "link" to colors.link,
            "success" to colors.success,
            "selectionBackground" to colors.selectionBackground,
            "pressedHighlight" to colors.pressedHighlight,
        )
    SmartisanGroup {
        swatches.forEach { (name, color) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .background(color, RoundedCornerShape(4.dp))
                        .border(0.67.dp, colors.divider, RoundedCornerShape(4.dp)),
                )
                SmartisanText(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = typography.listItemSecondary,
                    color = colors.textPrimary,
                    maxLines = 1,
                )
                SmartisanText(
                    text = color.toHexLabel(),
                    style = typography.caption,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
        }
    }
}

/** 文字样式一览。 */
@Composable
fun TypographySection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanGroup {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            SmartisanText("标题栏标题 titleBar 20sp", style = typography.titleBar, color = colors.textPrimary)
            SmartisanText("页面标题 title 20sp", style = typography.title, color = colors.textPrimary)
            SmartisanText("正文 body 15sp", style = typography.body, color = colors.textPrimary)
            SmartisanText("列表一级 listItemPrimary 15sp", style = typography.listItemPrimary, color = colors.textPrimary)
            SmartisanText("列表二级 listItemSecondary 12.5sp", style = typography.listItemSecondary, color = colors.textSecondary)
            SmartisanText("分组标题 sectionTitle 13.5sp", style = typography.sectionTitle, color = colors.textTertiary)
            SmartisanText("按钮 button 14sp", style = typography.button, color = colors.textPrimary)
            SmartisanText("说明 caption 12sp", style = typography.caption, color = colors.textTertiary)
            SmartisanText("等宽数字 numeric 0123456789", style = typography.numeric, color = colors.textPrimary)
            SmartisanText(
                "大号数字 displayNumeric 0123456789",
                style = typography.displayNumeric,
                color = colors.textPrimary,
            )
        }
    }
}

internal fun Color.toHexLabel(): String {
    val argb =
        (alpha * 255).toInt() shl 24 or
            (red * 255).toInt() shl 16 or
            (green * 255).toInt() shl 8 or
            (blue * 255).toInt()
    return "#" + argb.toUInt().toString(16).padStart(8, '0').uppercase()
}
