package cc.wuersan008.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 全局搜索应用建议行，对应 QuickSearchBoxSmartisan 的 application_suggestion.xml。
 * 行高至少 60dp，图标放在 60dp 左槽的 30dp 画布内。
 */
@Composable
fun SmartisanSearchSuggestion(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    detail: String? = null,
    icon: @Composable () -> Unit = {},
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val decoration = if (colors.isLight) {
        Modifier.smartisanDrawableBackground(
            if (pressed) R.drawable.quick_search_result_pressed else R.drawable.quick_search_result_normal,
        )
    } else {
        Modifier.background(if (pressed) colors.surfacePressed else colors.surface)
    }
    Row(
        modifier.fillMaxWidth().heightIn(min = 60.dp)
            .then(decoration)
            .smartisanClickable(interactionSource = interaction, onClick = onClick)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(60.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) { icon() }
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmartisanText(
                    text = title,
                    modifier = Modifier.weight(2f),
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (detail != null) {
                    SmartisanText(
                        detail,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        color = colors.textSecondary,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
            if (subtitle != null) {
                SmartisanText(subtitle, color = colors.textSecondary, fontSize = 13.sp, maxLines = 1)
            }
        }
    }
}
