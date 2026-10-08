package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanShapes
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 分组标题。
 *
 * 对应原版设置页、资料库里的灰色小标题（13.5sp，`textTertiary`）。
 */
@Composable
fun SmartisanSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.RowContentStart,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanText(
        text = text,
        modifier = modifier.fillMaxWidth().padding(start = startIndent, end = startIndent, top = 16.dp, bottom = 6.dp),
        style = typography.sectionTitle,
        color = colors.textTertiary,
        maxLines = 1,
    )
}

/**
 * 分组容器。
 *
 * 原版把一组设置项放在同一张白底卡片里，卡片之间留出间距；
 * 深色模式下卡片是 `surface`（炭灰 `#34373C`）。
 */
@Composable
fun SmartisanGroup(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.none,
    color: Color = LocalSmartisanColors.current.surface,
    horizontalMargin: Dp = SmartisanDimens.ListItemHorizontalMargin,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalMargin, vertical = 6.dp)
                .background(color = color, shape = shape),
        content = content,
    )
}

/** 分组内的分隔线，左右缩进与内容对齐。 */
@Composable
fun SmartisanGroupDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.RowContentStart,
) {
    SmartisanRowDivider(modifier = modifier, startIndent = startIndent)
}

/**
 * 卡片容器。
 *
 * 用于天气卡片、专辑卡片这类有描边和圆角的块。
 */
@Composable
fun SmartisanCard(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.medium,
    color: Color = LocalSmartisanColors.current.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().background(color = color, shape = shape),
        content = content,
    )
}

/**
 * 分组行在分组里的位置。
 *
 * 原版把分组卡片拆成 top / middle / bottom / single 四张底图，
 * 每张都带自己的圆角、描边与按压态，所以行必须知道自己的位置。
 */
enum class SmartisanGroupRowPosition {
    /** 分组里唯一的一行（带完整描边）。 */
    Single,

    /** 分组第一行。 */
    Top,

    /** 分组中间行。 */
    Middle,

    /** 分组最后一行。 */
    Bottom,
}

/** 取该位置对应的原版分组底图。 */
@DrawableRes
fun smartisanGroupRowBackground(position: SmartisanGroupRowPosition): Int =
    when (position) {
        SmartisanGroupRowPosition.Single -> SmartisanDrawables.GroupRowSingle
        SmartisanGroupRowPosition.Top -> SmartisanDrawables.GroupRowTop
        SmartisanGroupRowPosition.Middle -> SmartisanDrawables.GroupRowMiddle
        SmartisanGroupRowPosition.Bottom -> SmartisanDrawables.GroupRowBottom
    }

/**
 * 分组里的列表行：自动使用对应位置的原版分组底图。
 *
 * 这是 [SmartisanListItem] 的便捷包装，保留了后者的全部参数。
 */
@Composable
fun SmartisanGroupItem(
    position: SmartisanGroupRowPosition,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    SmartisanListItem(
        title = title,
        modifier = modifier,
        summary = summary,
        leading = leading,
        trailing = trailing,
        enabled = enabled,
        selected = selected,
        showDivider = showDivider,
        rowBackgroundRes = smartisanGroupRowBackground(position),
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
