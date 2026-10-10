package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.theme.SmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanShadowBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 行的文字版式。
 *
 * 对应 framework 里四张「中间文字区」的布局，一字不差地照搬了它们的字号：
 *
 * | 版式 | 原版 layout | 一级 | 二级 | 三级 |
 * | --- | --- | --- | --- | --- |
 * | [TwoLine] | `list_content_mid_primary_2line` | 17sp | — | 13.5sp |
 * | [TwoLineAlt] | `list_content_mid_primary_2line_alt` | 16sp | — | 12.5sp |
 * | [ThreeLine] | `list_content_mid_primary_3line` | 17sp | 15sp | 13.5sp |
 * | [ThreeLineAlt] | `list_content_mid_primary_3line_alt` | 16sp | 13.5sp | 12sp |
 *
 * 三行版的第二、三级分别落在 [SmartisanListRow] 的 `summary` 与 `tertiary` 参数上。
 * 字号来自 framework 的 `primary_text_size` / `_alt` / `secondary_text_size` /
 * `tertiary_text_size` / `_alt` / `quaternary_text_size`。
 */
enum class SmartisanListRowLines {
    /** 两行：17sp 标题 + 13.5sp 说明（`list_content_mid_primary_2line`）。 */
    TwoLine,

    /** 两行紧凑版：16sp 标题 + 12.5sp 说明（`list_content_mid_primary_2line_alt`）。 */
    TwoLineAlt,

    /** 三行：17sp 标题 + 15sp 说明 + 13.5sp 附注（`list_content_mid_primary_3line`）。 */
    ThreeLine,

    /** 三行紧凑版：16sp 标题 + 13.5sp 说明 + 12sp 附注（`list_content_mid_primary_3line_alt`）。 */
    ThreeLineAlt,
}

/** 取该版式的一级文字样式。 */
private fun SmartisanListRowLines.titleStyle(typography: SmartisanTypography): TextStyle =
    when (this) {
        SmartisanListRowLines.TwoLine, SmartisanListRowLines.ThreeLine -> typography.listRowPrimary
        SmartisanListRowLines.TwoLineAlt, SmartisanListRowLines.ThreeLineAlt -> typography.listRowPrimaryAlt
    }

/** 取该版式第二行文字的样式。 */
private fun SmartisanListRowLines.summaryStyle(typography: SmartisanTypography): TextStyle =
    when (this) {
        SmartisanListRowLines.TwoLine -> typography.listRowTertiary
        SmartisanListRowLines.TwoLineAlt -> typography.listRowTertiaryAlt
        SmartisanListRowLines.ThreeLine -> typography.listRowSecondary
        SmartisanListRowLines.ThreeLineAlt -> typography.listRowTertiary
    }

/** 取该版式第三行文字的样式（两行版不显示第三行）。 */
private fun SmartisanListRowLines.tertiaryStyle(typography: SmartisanTypography): TextStyle =
    when (this) {
        SmartisanListRowLines.TwoLine, SmartisanListRowLines.ThreeLine -> typography.listRowTertiary
        SmartisanListRowLines.TwoLineAlt -> typography.listRowTertiaryAlt
        SmartisanListRowLines.ThreeLineAlt -> typography.listRowQuaternary
    }

/** 该版式是否显示第三行。 */
private val SmartisanListRowLines.hasTertiary: Boolean
    get() = this == SmartisanListRowLines.ThreeLine || this == SmartisanListRowLines.ThreeLineAlt

/**
 * framework 版列表行（`smartisanos.widget.ListContentItem` + `res/layout/list_content_item_layout.xml`）。
 *
 * 这是锤子所有应用列表行的**母版**：原版把行拆成「左容器 + 中容器 + 右容器」三块，
 * 每块再配一张布局 —— 左边的 `list_content_left_checkbox` / `list_content_left_image_view`、
 * 中间四张 `list_content_mid_primary_*`、右边的 `list_content_right_checkbox` /
 * `_image_view` / `_subtitle_arrow` / `_switch`。本组件把三块合并成一个带槽位的行：
 *
 * - 左槽 [leading]：原版是 60dp × 60dp 的方形区（`left_icon_area_width`），
 *   放复选框或图标，内容居中且最大 36dp；
 * - 中间：按 [lines] 输出 2 / 3 行文字，字号见 [SmartisanListRowLines]；
 * - 右槽 [trailing]：原版按 `right_container_margin`(6dp) 留边，
 *   副标题 + 箭头可以直接用 [SmartisanListRowArrow]。
 *
 * 与 [SmartisanListItem] 的区别：[SmartisanListItem] 合并的是三个复刻项目里的资料库行 /
 * 城市行（一级 15sp），本组件对应 framework 的原始矩阵（一级 17sp / 16sp）。
 * 两者都保留，按需要选用。
 *
 * @param tertiary 第三行文字，仅在 [SmartisanListRowLines.ThreeLine] / [ThreeLineAlt] 下显示。
 * @param leading 左槽内容，自动放进 60dp 的方形图标区；传 `null` 时改用 [contentPadding] 缩进。
 * @param contentPadding 没有 [leading] 时的行内容左缩进，对应原版 `flexible_space`(18dp)。
 * @param rowBackgroundRes 行底色 selector，默认 [SmartisanDrawables.ListRowSelector]。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartisanListRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    tertiary: String? = null,
    lines: SmartisanListRowLines = SmartisanListRowLines.TwoLine,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    selected: Boolean = false,
    showDivider: Boolean = false,
    dividerStartIndent: Dp = SmartisanDimens.RowContentStart,
    minHeight: Dp = SmartisanDimens.ListRowMinHeight,
    contentPadding: Dp = SmartisanDimens.ListRowFlexibleSpace,
    @DrawableRes rowBackgroundRes: Int? = SmartisanDrawables.ListRowSelector,
    @DrawableRes rowShadowRes: Int? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }

    val titleColor = if (enabled) colors.textPrimary else colors.textDisabled
    val lineColor = if (enabled) colors.textTertiary else colors.textDisabled

    // 与原版一致：底色由 selector 决定（按下换按压位图、activated 换多选底色、默认卡片白）。
    val backgroundModifier =
        if (rowBackgroundRes != null && rowShadowRes != null) {
            Modifier.smartisanShadowBackground(
                backgroundRes = rowBackgroundRes,
                shadowRes = rowShadowRes,
                enabled = enabled,
                pressed = pressed && enabled,
                activated = selected,
            )
        } else if (rowBackgroundRes != null) {
            Modifier.smartisanDrawableBackground(
                drawableRes = rowBackgroundRes,
                enabled = enabled,
                pressed = pressed && enabled,
                activated = selected,
            )
        } else {
            Modifier.background(
                when {
                    selected -> colors.selectionBackground
                    pressed && enabled -> colors.surfacePressed
                    else -> Color.Transparent
                },
            )
        }

    Column(modifier.fillMaxWidth().then(backgroundModifier)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .then(
                        if (enabled && (onClick != null || onLongClick != null)) {
                            Modifier.combinedClickable(
                                interactionSource = interaction,
                                indication = null,
                                enabled = true,
                                role = Role.Button,
                                onLongClick = onLongClick,
                                onClick = click,
                            )
                        } else {
                            Modifier
                        },
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                // 原版左容器：60dp 见方，内容居中且不超过 36dp。
                Box(
                    modifier = Modifier.size(SmartisanDimens.ListRowLeftIconArea),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier.sizeIn(
                                maxWidth = SmartisanDimens.ListRowLeftIconMax,
                                maxHeight = SmartisanDimens.ListRowLeftIconMax,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        leading()
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(
                            // 原版 `ListContentItem.setLeftContainerVisible()`：有左图标时中容器紧贴
                            // 左容器（layout_toRightOf），无左图标时补 `flexible_space`(18dp) 左内边距。
                            start = if (leading != null) 0.dp else contentPadding,
                            end = 0.dp,
                        )
                        // 原版 `mid_container_top_bottom_padding`。
                        .padding(vertical = SmartisanDimens.ListRowTextVerticalPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                SmartisanText(
                    text = title,
                    style = lines.titleStyle(typography),
                    color = titleColor,
                    maxLines = 1,
                )
                if (summary != null) {
                    RowTextLine(text = summary, style = lines.summaryStyle(typography), color = lineColor)
                }
                if (tertiary != null && lines.hasTertiary) {
                    RowTextLine(text = tertiary, style = lines.tertiaryStyle(typography), color = lineColor)
                }
            }
            if (trailing != null) {
                Row(
                    modifier = Modifier.padding(end = SmartisanDimens.ListRowRightContainerMargin),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trailing()
                }
            }
        }
        if (showDivider) {
            SmartisanRowDivider(startIndent = dividerStartIndent)
        }
    }
}

/** 行内的第二 / 第三行文字：原版行间距是 `mid_container_summary_margin`(2dp)，最多一行。 */
@Composable
private fun RowTextLine(text: String, style: TextStyle, color: Color) {
    SmartisanText(
        text = text,
        modifier = Modifier.padding(top = SmartisanDimens.ListRowTextLineGap),
        style = style,
        color = color,
        maxLines = 1,
    )
}

/**
 * 行右槽的「副标题 + 箭头」（`list_content_right_subtitle_arrow`）。
 *
 * 原版结构是 `[右扩展区] [副标题] [箭头]`：副标题 13.5sp、最多一行、超出省略，
 * 左边留 `flexible_space`(18dp) 作为弹性间隔，右边留 `right_container_margin`(6dp)。
 * 传 `subtitle = null` 时只画箭头。
 */
@Composable
fun SmartisanListRowArrow(
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    subtitleMaxWidth: Dp = SmartisanDimens.ListRowSubtitleMaxWidth,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Row(
        modifier = modifier.padding(start = SmartisanDimens.ListRowFlexibleSpace),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (subtitle != null) {
            SmartisanText(
                text = subtitle,
                modifier = Modifier.widthIn(max = subtitleMaxWidth),
                style = typography.listRowTertiary,
                color = if (enabled) colors.textSecondary else colors.textDisabled,
                maxLines = 1,
            )
        }
        // 原版箭头用 selector 的固有尺寸（selector_list_content_item_arrow）。
        SmartisanIcon(
            res = SmartisanDrawables.ListItemArrow,
            contentDescription = null,
            enabled = enabled,
        )
    }
}

/**
 * 分组标题（`list_section_title_layout`）。
 *
 * 原版是一块 30dp 高（`list_section_title_height`）、底色 `#f5f5f5`、左边距 12dp 的
 * 13.5sp 加粗标题。这里底色改用主题的 `surfaceRaised`（浅色 `#F7F8F9`，与原版基本一致），
 * 这样深色模式下也能自动跟随；文字色原版是 `#4c000000`（30% 黑），这里用主题的
 * `textTertiary`（浅色 `#66000000`，40%），与 [SmartisanGroup] 的分组标题保持同一档。
 */
@Composable
fun SmartisanListSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.ListSectionHeaderPaddingStart,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SmartisanDimens.ListSectionTitleHeight)
                .background(colors.surfaceRaised)
                .padding(start = startIndent),
        contentAlignment = Alignment.CenterStart,
    ) {
        SmartisanText(
            text = text,
            style = typography.sectionTitle.copy(fontWeight = FontWeight.Bold),
            color = colors.textTertiary,
            maxLines = 1,
        )
    }
}

/**
 * 字母分组标题（原版联系人 `layout/list_section.xml`）。
 *
 * 这是联系人 A–Z 索引列表里压在每个字母分组上方的一条窄标题，和
 * [SmartisanListSectionTitle]（framework 的 30dp 灰带）**不是同一套版式**：
 *
 * | 部分 | 原版 | 说明 |
 * | --- | --- | --- |
 * | 标题条 | 18dp 高、`letter_seperater` 底图 | 浅灰 `#f5f5f5`，最下沿 2px 略深 `#ebebeb` |
 * | 阴影 | 1dp 高、`letter_seperater_shadow` 底图 | 黑色渐变（约 10% → 3%），压在标题条下方 |
 * | 文字 | 10sp 加粗、左内边距 8dp | 原版 `#4d000000`（30% 黑） |
 *
 * 整块合计 19dp 高（18 + 1），两个背景都是原版素材，浅灰不需要夜间变体。
 * 文字色沿用 [SmartisanListSectionTitle] 的做法：原版 30% 黑，库内统一成主题的
 * `textTertiary`（浅色 40%），深色模式下也能自动跟随。
 *
 * ```kotlin
 * SmartisanLetterSectionTitle("A")
 * ```
 *
 * @param text 字母（或任意分组名）。
 * @param modifier 外部修饰符。
 * @param startIndent 左内边距，原版 `list_section.xml` 的 `paddingLeft` 8dp。
 */
@Composable
fun SmartisanLetterSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    startIndent: Dp = SmartisanDimens.LetterSectionHeaderPaddingStart,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(modifier.fillMaxWidth()) {
        // 18dp 标题条：原版 `letter_seperater` 底图 + 10sp 加粗文字。
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SmartisanDimens.LetterSectionTitleHeight)
                    .smartisanDrawableBackground(SmartisanDrawables.LetterSeparator)
                    .padding(start = startIndent),
            contentAlignment = Alignment.CenterStart,
        ) {
            SmartisanText(
                text = text,
                style = typography.listItemCaptionSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textTertiary,
                maxLines = 1,
            )
        }
        // 标题条下方的 1dp 阴影：原版 `letter_seperater_shadow`。
        Box(
            Modifier
                .fillMaxWidth()
                .height(SmartisanDimens.LetterSectionShadowHeight)
                .smartisanDrawableBackground(SmartisanDrawables.LetterSeparatorShadow),
        )
    }
}

/**
 * 板块分组标题（`list_board_section_title_layout`）。
 *
 * 原版结构：6dp 留白 + 40dp 高的白底（按下变 `#f2f2f2`）标题条 + 1px 分隔线。
 * 标题 15sp 加粗、左边距 12dp，右侧固定一个箭头；整条可点（展开 / 收起板块用）。
 * 这三层都用原版素材：`list_board_section_bg`（selector）、
 * `list_board_section_title_divider`（9-patch）。
 */
@Composable
fun SmartisanListBoardSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val click = smartisanClick { onClick?.invoke() }
    Column(modifier.fillMaxWidth()) {
        // 原版 `top_space`：6dp。
        Spacer(modifier = Modifier.height(SmartisanDimens.ListBoardSectionTitleTopSpace))
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SmartisanDimens.ListBoardSectionTitleHeight)
                    .smartisanDrawableBackground(
                        drawableRes = SmartisanDrawables.ListBoardSectionBackground,
                        enabled = enabled,
                        pressed = pressed && enabled,
                    )
                    .then(
                        if (enabled && onClick != null) {
                            Modifier.clickable(
                                interactionSource = interaction,
                                indication = null,
                                enabled = true,
                                role = Role.Button,
                                onClick = click,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .padding(start = SmartisanDimens.ListSectionHeaderPaddingStart, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanText(
                text = text,
                modifier = Modifier.weight(1f).padding(end = 20.dp),
                style = typography.listRowSecondary.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) colors.textSecondary else colors.textDisabled,
                maxLines = 1,
            )
            SmartisanIcon(
                res = SmartisanDrawables.ListItemArrow,
                contentDescription = null,
                enabled = enabled,
            )
        }
        // 原版标题条下方 1px 分隔线。
        SmartisanIcon(
            res = SmartisanDrawables.ListBoardSectionTitleDivider,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 分组之间的纵向留白（`group_list_item_vertical_gap_layout`）。
 *
 * 原版把它当成一个 14dp 高的空白 View 插在分组之间；卡片投影就落在这段留白里。
 */
@Composable
fun SmartisanListVerticalGap(
    modifier: Modifier = Modifier,
    height: Dp = SmartisanDimens.ListItemVerticalGap,
) {
    Spacer(modifier = modifier.fillMaxWidth().height(height))
}




