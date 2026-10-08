package top.smartisanx.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanIcon
import top.smartisanx.ui.basic.SmartisanText

/**
 * 空列表提示。
 *
 * 对应锤子音乐资料库与锤子天气城市列表的空态：居中、灰色文字、可选图标与操作按钮。
 */
@Composable
fun SmartisanEmptyHint(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            SmartisanIcon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textDisabled,
                size = 48.dp,
            )
        }
        SmartisanText(
            text = title,
            modifier = Modifier.padding(top = if (icon != null) 12.dp else 0.dp),
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
