package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 空列表提示。
 *
 * 对应锤子音乐资料库与锤子天气城市列表的空态：居中、灰色文字、可选图标与操作按钮。
 *
 * 图标有两种来源，推荐用原版空态插图 [SmartisanOriginalIcons]
 * （`blank_song` / `blank_folder` / `blank_playlist` / `blank_search` / `blank_style`）：
 *
 *
 * ```kotlin
 * // 原版空态插图（blank_song / blank_folder / blank_playlist …），默认 120dp
 * SmartisanEmptyHint(title = "还没有歌曲", iconRes = SmartisanOriginalIcons.EmptySong)
 *
 * // 自定义矢量图标（原版没有对应素材时才用）
 * SmartisanEmptyHint(title = "还没有内容", icon = SmartisanXIcons.Menu)
 * ```
 */
@Composable
fun SmartisanEmptyHint(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    @DrawableRes iconRes: Int? = null,
    iconResSize: Dp = 120.dp,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val hasIcon = iconRes != null || icon != null
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (iconRes != null) {
            // 原版空态插图：与锤子音乐空态一致，位图缩放到 iconResSize。
            SmartisanIcon(
                res = iconRes,
                contentDescription = null,
                size = iconResSize,
            )
        } else if (icon != null) {
            SmartisanIcon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textDisabled,
                size = 48.dp,
            )
        }
        SmartisanText(
            text = title,
            modifier = Modifier.padding(top = if (hasIcon) 12.dp else 0.dp),
            style = typography.body,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            SmartisanText(
                text = description,
                modifier = Modifier.padding(top = 6.dp),
                style = typography.listItemSecondary,
                color = colors.textDisabled,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Column(modifier = Modifier.padding(top = 20.dp)) { action() }
        }
    }
}
