package cc.wuersan008.smartisanx.sample.pages

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
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanShapes
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.LocalSampleFeedback
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem

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
    // 样例行没有业务动作，点一下弹条提示，避免「点了没反应」。
    val feedback = LocalSampleFeedback.current
    SmartisanGroup {
        SmartisanListItem(
            title = "标题栏高度",
            summary = "SmartisanDimens.TitleBarHeight = 48dp",
            onClick = { feedback("标题栏高度 · 纯展示行") },
        )
        SmartisanListItem(
            title = "图标尺寸",
            summary = "SmartisanDimens.IconSize = 36dp",
            onClick = { feedback("图标尺寸 · 纯展示行") },
        )
        SmartisanListItem(
            title = "列表行最小高度",
            summary = "SmartisanDimens.ListItemMinHeight = 48dp",
            onClick = { feedback("列表行最小高度 · 纯展示行") },
        )
        SmartisanListItem(
            title = "弹窗宽度",
            summary = "SmartisanDimens.DialogWidth = 308dp",
            onClick = { feedback("弹窗宽度 · 纯展示行") },
        )
        SmartisanListItem(
            title = "底部栏高度",
            summary = "SmartisanDimens.BottomBarHeight = 50dp",
            onClick = { feedback("底部栏高度 · 纯展示行") },
        )
    }
}
