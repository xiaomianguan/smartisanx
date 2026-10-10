/**
 * 控件：原版弹窗内容版式（framework `smartisanos.widget.DialogPattern*`）。
 *
 * framework 把「应用信息」「分组说明」「两行单选」这三种弹窗内容做成了独立的自定义 View +
 * 布局 + 样式，任何应用要弹这类内容都直接用它，所以版式在各应用里完全一致。本组件把这三套
 * 版式搬过来：
 *
 * | framework 类 / 布局 | 行数 | 本库对应 |
 * | --- | --- | --- |
 * | `DialogPatternAppInfoLayout` + `dlg_pattern_app_info_layout.xml` | 71 | [SmartisanDialogAppInfo]（36dp 图标 + 标题 + 副标题两行） |
 * | `DialogPatternSectionGroup` + `dlg_pattern_section_group_layout.xml` | 71 | [SmartisanDialogSectionGroup]（主标题 / 副标题 / 正文三段） |
 * | `DialogPatternTwoLineSingleChoice` + `dlg_pattern_two_line_single_choice_item.xml` | 55 | [SmartisanDialogSingleChoiceRow]（两行单选行 + 右侧单选图标） |
 *
 * 文字规格照抄原版样式：
 *
 * | 样式 | 字号 / 颜色 |
 * | --- | --- |
 * | `DialogPatternPrimaryText` | 16sp（`textAppearanceMedium`）加粗、`#9a000000` |
 * | `DialogPatternSecondaryText` | 12.5sp、`dlg_content_secondary_text_color`（`#66000000`） |
 * | `DialogPatternSectionPrimaryTitle` | 15sp 加粗、`#9a000000`，左右内边距 20dp / 18dp |
 * | `DialogPatternSectionSubtitle` | 12.5sp、`#66000000` |
 * | `DialogPatternSectionMessage` | 16sp、`#9a000000` |
 *
 * 尺寸：应用信息图标 36dp、与文字间距 12dp；单选行高 60dp（`dlg_single_choice_height_has_summary`）、
 * 左内边距 20dp、右内边距 6dp；三段式之间的竖向间距 18dp（`dlg_section_vertical_space`）。
 *
 * 与原版的差异：
 *
 * 1. 原版单选行的文字颜色取系统主题的 `?android:textColorAlertDialogListItem`，
 *    本库统一用主题语义色（`textPrimary`），这样深浅色都能跟随；
 * 2. 原版右侧单选图标是 `selector_radio_choice`（指向 Android 内置的 `btn_star_*` 素材），
 *    本库默认用库内同一张 selector，也可以换成 [cc.wuersan008.smartisanx.ui.control.SmartisanSelectionMark]。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 弹窗内容版式的尺寸与颜色，全部对应原版 framework 的样式与 dimens。 */
object SmartisanDialogPatternDefaults {
    /** 应用信息图标尺寸（`dlg_pattern_app_info_layout.xml` 里写死 36dp）。 */
    val AppInfoIconSize = 36.dp

    /** 图标与文字之间的间距（同上，12dp）。 */
    val AppInfoIconGap = 12.dp

    /** 两行单选行的高度（原版 `dimen/dlg_single_choice_height_has_summary`）。 */
    val SingleChoiceRowHeight = 60.dp

    /** 单选行左右内边距（布局里的 20dp / 6dp）。 */
    val SingleChoicePaddingStart = 20.dp
    val SingleChoicePaddingEnd = 6.dp

    /** 三段式之间的竖向间距（原版 `dimen/dlg_section_vertical_space`）。 */
    val SectionSpacing = 18.dp

    /** 三段式的左右内边距（原版 `dlg_text_view_padding_left` / `_right`）。 */
    val SectionPaddingStart = 20.dp
    val SectionPaddingEnd = 18.dp

    /** 主标题字号（`DialogPatternSectionPrimaryTitle`，15sp 加粗）。 */
    val SectionTitleSize = 15.sp

    /** 副标题字号（`DialogPatternSectionSubtitle` / `DialogPatternSecondaryText`，12.5sp）。 */
    val SecondaryTextSize = 12.5.sp

    /** 正文 / 主文字字号（`DialogPatternSectionMessage` / `DialogPatternPrimaryText`，16sp）。 */
    val PrimaryTextSize = 16.sp
}

/**
 * 弹窗里的「应用信息」两行版式（原版 `DialogPatternAppInfoLayout`）。
 *
 * @param title 标题（16sp 加粗、`#9a000000`、单行省略）。
 * @param summary 副标题（12.5sp、`#66000000`、单行省略）。
 * @param modifier 外部修饰符。
 * @param iconRes 左侧图标；为空时不占位。
 */
@Composable
fun SmartisanDialogAppInfo(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
) {
    val colors = LocalSmartisanColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            SmartisanIcon(
                res = iconRes,
                contentDescription = null,
                modifier = Modifier.size(SmartisanDialogPatternDefaults.AppInfoIconSize),
            )
            Box(Modifier.size(SmartisanDialogPatternDefaults.AppInfoIconGap))
        }
        Column(Modifier.weight(1f)) {
            SmartisanText(
                text = title,
                color = SmartisanDialogPatternTitleColor,
                fontSize = SmartisanDialogPatternDefaults.PrimaryTextSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!summary.isNullOrEmpty()) {
                SmartisanText(
                    text = summary,
                    color = colors.textTertiary,
                    fontSize = SmartisanDialogPatternDefaults.SecondaryTextSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 弹窗里的三段式说明（原版 `DialogPatternSectionGroup`）。
 *
 * 主标题 15sp 加粗、副标题 12.5sp、正文 16sp，左右内边距 20dp / 18dp，段间 18dp。
 *
 * @param primaryTitle 主标题。
 * @param modifier 外部修饰符。
 * @param subtitle 副标题。
 * @param message 正文。
 */
@Composable
fun SmartisanDialogSectionGroup(
    primaryTitle: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    message: String? = null,
) {
    val colors = LocalSmartisanColors.current
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = SmartisanDialogPatternDefaults.SectionPaddingStart,
                    end = SmartisanDialogPatternDefaults.SectionPaddingEnd,
                ),
        verticalArrangement = Arrangement.spacedBy(SmartisanDialogPatternDefaults.SectionSpacing),
    ) {
        SmartisanText(
            text = primaryTitle,
            color = SmartisanDialogPatternTitleColor,
            fontSize = SmartisanDialogPatternDefaults.SectionTitleSize,
            fontWeight = FontWeight.Bold,
        )
        if (!subtitle.isNullOrEmpty()) {
            SmartisanText(
                text = subtitle,
                color = colors.textTertiary,
                fontSize = SmartisanDialogPatternDefaults.SecondaryTextSize,
            )
        }
        if (!message.isNullOrEmpty()) {
            SmartisanText(
                text = message,
                color = SmartisanDialogPatternTitleColor,
                fontSize = SmartisanDialogPatternDefaults.PrimaryTextSize,
            )
        }
    }
}

/** 标题色：原版样式里写死的 `#9a000000`（60% 黑）。 */
private val SmartisanDialogPatternTitleColor = Color(0x9A000000)


/**
 * 弹窗里的两行单选行（原版 `DialogPatternTwoLineSingleChoice` + `dlg_pattern_two_line_single_choice_item.xml`）。
 *
 * 行高 60dp、左内边距 20dp、右内边距 6dp；标题 16sp 加粗、副标题 12.5sp（按下时变白，
 * 原版 `dlg_single_choice_summary_colorlist`）；右侧是选中图标，未选中时不占位（原版 `invisible`，
 * 这里用 `alpha = 0` 保持宽度一致）。
 *
 * @param title 标题。
 * @param selected 是否选中。
 * @param modifier 外部修饰符。
 * @param summary 副标题。
 * @param onClick 点击回调。
 * @param checkRes 右侧选中图标；默认原版 `selector_radio_choice`。
 */
@Composable
fun SmartisanDialogSingleChoiceRow(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    @DrawableRes checkRes: Int = R.drawable.selector_radio_choice,
) {
    val colors = LocalSmartisanColors.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val clickModifier =
        if (onClick != null) {
            Modifier.smartisanClickable(interactionSource = interaction, onClick = onClick)
        } else {
            Modifier
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SmartisanDialogPatternDefaults.SingleChoiceRowHeight)
                .then(clickModifier)
                .padding(
                    start = SmartisanDialogPatternDefaults.SingleChoicePaddingStart,
                    end = SmartisanDialogPatternDefaults.SingleChoicePaddingEnd,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            SmartisanText(
                text = title,
                color = SmartisanDialogPatternTitleColor,
                fontSize = SmartisanDialogPatternDefaults.PrimaryTextSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!summary.isNullOrEmpty()) {
                SmartisanText(
                    text = summary,
                    // 原版 colorlist：按下时白字（配深色按下底图），常态 40% 黑。
                    color = if (pressed) Color(0x4C_FFFFFF) else colors.textTertiary,
                    fontSize = SmartisanDialogPatternDefaults.SecondaryTextSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        SmartisanIcon(
            res = checkRes,
            contentDescription = null,
            modifier =
                Modifier.graphicsLayer { alpha = if (selected) 1f else 0f },
        )
    }
}
