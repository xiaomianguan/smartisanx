package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.utils.smartisanTopMargin
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 原版布局里两个 `TextView` 都是 `includeFontPadding = false`，这里用同一份基础样式。 */
private val MarqueeTextStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

/**
 * 锤子风格跑马灯标题（主标题 + 副标题）。
 *
 * 对应 framework 里的 `smartisanos.widget.SmartisanMarqueeView`
 * （`framework/smartisanos.jar` 的 `classes.dex`），布局是
 * `res/layout/marquee_title_center_layout.xml`。用在标题栏、播放页这类
 * 「一行主标题 + 一行小副标题」且标题过长时横向滚动的位置。
 *
 * 还原要点（照抄原版布局与 `initView` / `initTextSize`）：
 * - 主标题：`20sp`（`title_bar_title_text_size`）、颜色
 *   `smartisan_button_normal_text_color` = `#666666`、`singleLine`、居中、
 *   `ellipsize = marquee` + `marqueeRepeatLimit = marquee_forever`、
 *   上边距 `8.8dp`（`marquee_title_margin_top`）、`includeFontPadding = false`；
 * - 原版 `mTitle.getPaint().setFlags(33)`（`ANTI_ALIAS_FLAG | FAKE_BOLD_TEXT_FLAG`），
 *   所以主标题是**加粗**的；
 * - 副标题：`10sp`（`item_sub_title_size`）、颜色 `sub_title_text_color` = `#4c000000`、
 *   上边距 `-0.4dp`（`marquee_subtitle_margin_top`）、`includeFontPadding = false`，
 *   文案为空时整行隐藏（原版 `checkSubContext`）；
 * - 字号上限：原版 `initTextSize` 会把字号收敛到 `(原尺寸 / fontScale) × 1.1`
 *   （`MAX_FONT_SCALE_WITH_SUBTITLE`），避免大字号设置下标题溢出。
 *
 * ```kotlin
 * SmartisanMarqueeText(
 *     title = "这是一个很长很长很长很长很长很长很长的标题",
 *     subTitle = "副标题",
 * )
 * ```
 *
 * @param title 主标题文案。
 * @param modifier 外部修饰符。
 * @param subTitle 副标题文案；`null` 或空串时不显示。
 * @param titleColor 主标题颜色，默认取原版 `smartisan_button_normal_text_color`。
 * @param subTitleColor 副标题颜色，默认取原版 `sub_title_text_color`。
 * @param titleTextSize 主标题字号，默认 20sp。
 * @param subTitleTextSize 副标题字号，默认 10sp。
 * @param titleFontWeight 主标题字重，默认加粗（对齐原版 `FAKE_BOLD_TEXT_FLAG`）。
 * @param marquee 是否开启跑马灯滚动，默认开启。
 * @param marqueeIterations 滚动次数，默认 `Int.MAX_VALUE`（对应原版 `marquee_forever`）。
 * @param titleTextAlign 主标题对齐方式。
 * @param subTitleTextAlign 副标题对齐方式。
 */
@Composable
fun SmartisanMarqueeText(
    title: String,
    modifier: Modifier = Modifier,
    subTitle: String? = null,
    titleColor: Color = colorResource(R.color.smartisan_button_normal_text_color),
    subTitleColor: Color = colorResource(R.color.sub_title_text_color),
    titleTextSize: TextUnit = SmartisanMarqueeDefaults.TitleTextSize,
    subTitleTextSize: TextUnit = SmartisanMarqueeDefaults.SubTitleTextSize,
    titleFontWeight: FontWeight? = FontWeight.Bold,
    marquee: Boolean = true,
    marqueeIterations: Int = Int.MAX_VALUE,
    titleTextAlign: TextAlign = TextAlign.Center,
    subTitleTextAlign: TextAlign = TextAlign.Center,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SmartisanText(
            text = title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SmartisanMarqueeDefaults.TitleMarginTop)
                    .then(if (marquee) Modifier.basicMarquee(iterations = marqueeIterations) else Modifier),
            style = MarqueeTextStyle,
            color = titleColor,
            fontWeight = titleFontWeight,
            fontSize = titleTextSize,
            textAlign = titleTextAlign,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
        if (!subTitle.isNullOrEmpty()) {
            SmartisanText(
                text = subTitle,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        // 原版是负的 layout_marginTop，padding 收不了负值。
                        .smartisanTopMargin(SmartisanMarqueeDefaults.SubTitleMarginTop),
                style = MarqueeTextStyle,
                color = subTitleColor,
                fontSize = subTitleTextSize,
                textAlign = subTitleTextAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 跑马灯标题默认值，取自 framework 的 `res/values/dimens.xml`
 * 与 `res/values/styles.xml`（`TitleTextStyle` / `SubTitleTextStyle`）。
 */
object SmartisanMarqueeDefaults {
    /** 主标题字号，原版 `dimen/title_bar_title_text_size = 20sp`。 */
    val TitleTextSize: TextUnit = 20.sp

    /** 副标题字号，原版 `dimen/item_sub_title_size = 10sp`。 */
    val SubTitleTextSize: TextUnit = 10.sp

    /** 主标题上边距，原版 `dimen/marquee_title_margin_top = 8.8dp`。 */
    val TitleMarginTop: Dp
        @Composable get() = dimensionResource(R.dimen.marquee_title_margin_top)

    /** 副标题上边距，原版 `dimen/marquee_subtitle_margin_top = -0.4dp`。 */
    val SubTitleMarginTop: Dp
        @Composable get() = dimensionResource(R.dimen.marquee_subtitle_margin_top)
}
