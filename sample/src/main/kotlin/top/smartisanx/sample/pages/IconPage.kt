package top.smartisanx.sample.pages

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.sample.SampleFootnote
import top.smartisanx.sample.SamplePageScaffold
import top.smartisanx.sample.SampleSectionHeader
import top.smartisanx.ui.basic.SmartisanIcon
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.layout.SmartisanGroup

/** 图标页：列出 smartisanx 自带的全部矢量图标。 */
@Composable
fun IconPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "图标", onBack = onBack) {
        smartisanIconCatalog.forEach { category ->
            SampleSectionHeader(category.title)
            IconGrid(category.icons)
        }
        SampleFootnote(
            "图标全部是 24×24 的矢量路径，颜色由 SmartisanIcon 的 tint 决定，库内不包含任何位图资源。",
        )
    }
}

@Composable
private fun IconGrid(icons: List<Pair<String, ImageVector>>) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanGroup {
        icons.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { (name, icon) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .background(colors.surfaceRaised, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            SmartisanIcon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = colors.textPrimary,
                                size = 24.dp,
                            )
                        }
                        SmartisanText(
                            text = name,
                            modifier = Modifier.padding(top = 4.dp),
                            style = typography.caption,
                            color = colors.textTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
                repeat(4 - row.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}
