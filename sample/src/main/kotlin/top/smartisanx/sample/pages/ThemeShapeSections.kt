package top.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanShapes
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.layout.SmartisanGroup
import top.smartisanx.ui.layout.SmartisanListItem

/** 形状一览。 */
@Composable
fun ShapeSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val shapes = LocalSmartisanShapes.current
    val shapeList =
        listOf(
            "none" to shapes.none,
            "extraSmall" to shapes.extraSmall,
            "small" to shapes.small,
            "medium" to shapes.medium,
            "dialog" to shapes.dialog,
            "sheet" to shapes.sheet,
            "large" to shapes.large,
        )
    SmartisanGroup {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            shapeList.forEach { (name, shape) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(36.dp).background(colors.surfaceRaised, shape))
                    SmartisanText(
                        text = name,
                        modifier = Modifier.padding(top = 4.dp),
                        style = typography.caption,
                        color = colors.textTertiary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** 尺寸常量一览。 */
@Composable
fun DimensSection() {
    SmartisanGroup {
        SmartisanListItem(title = "标题栏高度", summary = "SmartisanDimens.TitleBarHeight = 48dp")
        SmartisanListItem(title = "图标尺寸", summary = "SmartisanDimens.IconSize = 36dp")
        SmartisanListItem(title = "列表行最小高度", summary = "SmartisanDimens.ListItemMinHeight = 48dp")
        SmartisanListItem(title = "弹窗宽度", summary = "SmartisanDimens.DialogWidth = 308dp")
        SmartisanListItem(title = "底部栏高度", summary = "SmartisanDimens.BottomBarHeight = 50dp")
    }
}
