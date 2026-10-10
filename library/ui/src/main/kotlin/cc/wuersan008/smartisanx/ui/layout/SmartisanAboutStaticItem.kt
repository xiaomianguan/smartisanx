package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 本机信息静态行（原版设置页 `com.android.settings.AboutStaticItem` +
 * `res/layout/about_static_item_layout.xml`）。
 *
 * 「关于本机」页下半部分那串只读信息行：一行小标题 + 一行取值 + 一条 1px 分隔线，
 * 例如「型号 / DT2002」「Android 版本 / 11」「内核版本 / 4.19.81」。
 *
 * **整行不画卡片底色**：原版就是这样 —— 这些行直接落在页面底纹上，靠 1px 分隔线分节，
 * 所以调用方不用套 [SmartisanGroup]，左 30dp / 右 15dp 的缩进由行自己带。
 *
 * | 部分 | 原版 | 说明 |
 * | --- | --- | --- |
 * | 标题 | 13.5sp、`#80000000`（50% 黑） | 上留白 7dp |
 * | 摘要 | 12sp、`lineSpacingMultiplier` 1.125 | 与标题间距 3dp，颜色与标题**相同** |
 * | 分隔线 | 1px、`#14000000`（8% 黑） | 在摘要下方 7dp 处，左右缩进与文字一致 |
 * | 行间 | `layout_marginBottom` 5dp | 原版在 `about_settings_layout` 里逐个 item 设，这里由本组件画在下方 |
 *
 * 库内两处换算与 [SmartisanListSectionTitle] 同一套做法：原版两行文字同色（50% 黑），
 * 这里统一到主题的 `textSecondary`（浅色 60%），深色模式能跟随；分隔线用主题的
 * `divider`（浅色 `#E9E9E9`，正是 8% 黑落在白底上的效果）。
 *
 * ```kotlin
 * SmartisanAboutStaticItem(title = "型号", summary = "DT2002")
 * ```
 *
 * @param title 信息项名称。
 * @param summary 取值；为 `null` 时只画标题（原版初始化前就是这个状态）。
 * @param showArrow 是否在右侧画箭头（原版 `setArrowVisible`，默认不显示；
 *   「关于本机」页所有行都没开，留着是为了跟原版 API 对齐）。
 */
@Composable
fun SmartisanAboutStaticItem(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    showArrow: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = SmartisanDimens.AboutStaticItemBottomMargin),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                SmartisanText(
                    text = title,
                    modifier =
                        Modifier.padding(
                            start = SmartisanDimens.AboutStaticItemContentStart,
                            end = SmartisanDimens.AboutStaticItemContentEnd,
                            top = SmartisanDimens.AboutStaticItemPaddingVertical,
                        ),
                    style = typography.sectionTitle,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
                if (summary != null) {
                    SmartisanText(
                        text = summary,
                        modifier =
                            Modifier.padding(
                                start = SmartisanDimens.AboutStaticItemContentStart,
                                end = SmartisanDimens.AboutStaticItemContentEnd,
                                top = SmartisanDimens.AboutStaticItemTitleSummaryGap,
                            ),
                        style = typography.caption,
                        color = colors.textSecondary,
                    )
                }
                // 1px 分隔线：原版写死 `1.0px`，左右缩进与文字相同。
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SmartisanDimens.AboutStaticItemContentStart,
                            end = SmartisanDimens.AboutStaticItemContentEnd,
                            top = SmartisanDimens.AboutStaticItemPaddingVertical,
                        )
                        .height(SmartisanDimens.AboutStaticItemDividerHeight)
                        .background(colors.divider),
                )
            }
            if (showArrow) {
                SmartisanIcon(
                    res = SmartisanDrawables.SettingsItemArrow,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = SmartisanDimens.AboutStaticItemArrowEnd),
                )
            }
        }
    }
}
