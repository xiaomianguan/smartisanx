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
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanShadowBackground
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
 * **默认是透明的**，这一点很关键：原版的分组本身不画底色，
 * 卡片的白底、圆角与投影全部由每一行自己的 nine-patch 提供，
 * 分组之间露出的正是页面的细条纹底纹。
 *
 * 如果给分组画上一层不透明底色，会出现两个问题：
 * 条纹底纹被盖住；行的圆角与白底混在一起，看上去就没有圆角了。
 *
 * 需要整块底色时显式传 [color]。
 */
@Composable
fun SmartisanGroup(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.none,
    color: Color = Color.Transparent,
    horizontalMargin: Dp = SmartisanDimens.ListItemHorizontalMargin,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                // 纵向留白是原版的 list_item_vertical_gap，投影要落在行边界之外。
                .padding(horizontal = horizontalMargin, vertical = SmartisanDimens.ListItemVerticalGap)
                .then(
                    if (color != Color.Transparent) {
                        Modifier.background(color = color, shape = shape)
                    } else {
                        Modifier
                    },
                ),
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
 *
 * 传 [backgroundRes] + [shadowRes] 时使用原版的「内容底图 + 向外投影」两层结构
 * （见 `Modifier.smartisanShadowBackground`），此时 [color] 与 [shape] 不再生效；
 * 投影会画在卡片边界之外，所以调用方要留出外边距。
 */
@Composable
fun SmartisanCard(
    modifier: Modifier = Modifier,
    shape: Shape = LocalSmartisanShapes.current.medium,
    color: Color = LocalSmartisanColors.current.surface,
    @DrawableRes backgroundRes: Int? = null,
    @DrawableRes shadowRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backgroundModifier =
        if (backgroundRes != null && shadowRes != null) {
            Modifier.smartisanShadowBackground(backgroundRes, shadowRes)
        } else if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(color = color, shape = shape)
        }
    Column(
        modifier = modifier.fillMaxWidth().then(backgroundModifier),
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

/**
 * 取该位置对应的原版卡片投影 9-patch。
 *
 * 与 [smartisanGroupRowBackground] 配对使用，还原原版「底图 + 向外投影」的卡片质感。
 */
@DrawableRes
fun smartisanGroupRowShadow(position: SmartisanGroupRowPosition): Int =
    when (position) {
        SmartisanGroupRowPosition.Single -> SmartisanDrawables.GroupRowSingleShadow
        SmartisanGroupRowPosition.Top -> SmartisanDrawables.GroupRowTopShadow
        SmartisanGroupRowPosition.Middle -> SmartisanDrawables.GroupRowMiddleShadow
        SmartisanGroupRowPosition.Bottom -> SmartisanDrawables.GroupRowBottomShadow
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
        rowShadowRes = smartisanGroupRowShadow(position),
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
