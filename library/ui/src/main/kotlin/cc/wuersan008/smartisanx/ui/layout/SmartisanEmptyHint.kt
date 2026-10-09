package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 空态版式，对应 framework `smartisanos.widget.SmartisanBlankView` 的三种 style
 * （`res/values/attrs.xml` 的 `SmartisanBlankView_style`）。
 *
 * 三种版式在 `res/layout/blank_view.xml` 里是同一套结构，区别只有
 * 图标尺寸 / 图标素材，以及是否显示 60dp 圆形操作按钮：
 *
 * | 版式 | 原版 style | 图标 | 操作按钮 |
 * | --- | --- | --- | --- |
 * | [Normal] | `0` | `blank_icon_large`，120dp | 无 |
 * | [Small] | `1` | `blank_icon_small`，60dp（`small_blank_image_size`） | 无 |
 * | [WithAction] | `2` | `blank_icon_small`，60dp，上边距归零 | `blank_option_btn_selector`，60dp |
 */
enum class SmartisanBlankStyle {
    /** 大图标空态（原版 style = 0），图标取 `blank_icon_large`，默认 120dp。 */
    Normal,

    /** 小图标空态（原版 style = 1），图标取 `blank_icon_small`，60dp。 */
    Small,

    /** 带操作按钮的空态（原版 style = 2），小图标 + 60dp 圆形按钮。 */
    WithAction,
}

/**
 * 空列表提示。
 *
 * 对应锤子音乐资料库与锤子天气城市列表的空态：居中、灰色文字、可选图标与操作按钮。
 *
 * 图标有两种来源，推荐用原版空态插图 [SmartisanOriginalIcons]
 * （`blank_song` / `blank_folder` / `blank_playlist` / `blank_search` / `blank_style`）：
 *
 * ```kotlin
 * // 原版空态插图（blank_song / blank_folder / blank_playlist …），默认 120dp
 * SmartisanEmptyHint(title = "还没有歌曲", iconRes = SmartisanOriginalIcons.EmptySong)
 *
 * // 自定义矢量图标（原版没有对应素材时才用）
 * SmartisanEmptyHint(title = "还没有内容", icon = SmartisanOriginalIcons.EmptyFolder)
 *
 * // framework SmartisanBlankView 那套：小图标 + 说明 + 60dp 圆形操作按钮
 * SmartisanEmptyHint(
 *     title = "没有网络",
 *     description = "请检查网络连接后重试",
 *     blankStyle = SmartisanBlankStyle.WithAction,
 *     actionText = "重试",
 *     onActionClick = { reload() },
 * )
 * ```
 *
 * 与 framework `SmartisanBlankView` 的对照（`res/layout/blank_view.xml`）：
 * - 图标：原版 `empty_image` 是 120dp 的 `ImageView`，`style = 1 / 2` 时缩到
 *   `small_blank_image_size`（60dp）、并改用 `blank_icon_small`；
 * - 主标题：原版 `empty_primary_hint`，20sp 加粗、`#26000000`、上边距 18dp、单行；
 * - 说明：原版 `empty_secondary_hint`，13.5sp、`#26000000`、上边距 5dp；
 * - 操作按钮：原版 `empty_action_btn`，60dp × 60dp、底图 `blank_option_btn_selector`
 *   （含按下 / 禁用态）、上边距 `blankview_actionbtn_margintop`（84dp）。
 *
 * @param title 主标题文案（原版 `empty_primary_hint`）。
 * @param modifier 外部修饰符。
 * @param description 说明文案（原版 `empty_secondary_hint`）；`null` 时不显示。
 * @param icon 自定义矢量图标；原版没有对应素材时才用。
 * @param action 自定义操作区插槽；需要任意内容时用，与 [actionText] 二选一即可。
 * @param iconRes 位图图标素材，推荐 [SmartisanOriginalIcons] 里的原版空态插图。
 * @param iconResSize 位图图标尺寸；[SmartisanBlankStyle.Normal] 下默认 120dp。
 * @param blankStyle 空态版式，默认 [SmartisanBlankStyle.Normal]（大图标）。
 *   未显式传 [iconRes] / [icon] 时按版式取原版素材（`blank_icon_large` / `blank_icon_small`）。
 * @param actionText 原版 60dp 圆形操作按钮的文案（`empty_action_btn`）；`null` 时不显示该按钮。
 * @param onActionClick 点击圆形操作按钮的回调。
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
    blankStyle: SmartisanBlankStyle = SmartisanBlankStyle.Normal,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    // 原版 style = 0 用 blank_icon_large（120dp），style = 1 / 2 用 blank_icon_small（60dp）。
    val frameworkIconRes =
        if (blankStyle == SmartisanBlankStyle.Normal) {
            R.drawable.blank_icon_large
        } else {
            R.drawable.blank_icon_small
        }
    // 调用方显式传了 iconRes / icon 就优先用，否则回落到原版空态素材。
    val resolvedIconRes = iconRes ?: if (icon == null) frameworkIconRes else null
    val resolvedIconSize =
        if (blankStyle == SmartisanBlankStyle.Normal) {
            iconResSize
        } else {
            SmartisanBlankDefaults.SmallImageSize
        }
    val hasIcon = resolvedIconRes != null || icon != null
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (resolvedIconRes != null) {
            // 原版空态插图：与锤子音乐空态一致，位图缩放到 resolvedIconSize。
            SmartisanIcon(
                res = resolvedIconRes,
                contentDescription = null,
                size = resolvedIconSize,
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
            modifier =
                Modifier.padding(
                    top = if (hasIcon) SmartisanBlankDefaults.PrimaryHintTopMargin else 0.dp,
                ),
            style = typography.body,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        if (description != null) {
            SmartisanText(
                text = description,
                modifier = Modifier.padding(top = SmartisanBlankDefaults.SecondaryHintTopMargin),
                style = typography.listItemSecondary,
                color = colors.textDisabled,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null) {
            Column(modifier = Modifier.padding(top = 20.dp)) { action() }
        }
        if (actionText != null) {
            SmartisanBlankActionButton(text = actionText, onClick = onActionClick)
        }
    }
}

/**
 * 原版 `blank_view.xml` 的 `empty_action_btn`：60dp 圆形按钮，底图
 * `blank_option_btn_selector`（`blank_option_btn` / `_pressed` / `_disabled`）。
 */
@Composable
private fun SmartisanBlankActionButton(
    text: String,
    onClick: (() -> Unit)?,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Box(
        modifier =
            Modifier
                .padding(top = SmartisanBlankDefaults.ActionButtonMarginTop)
                .size(SmartisanBlankDefaults.ActionButtonSize)
                .smartisanDrawableBackground(R.drawable.blank_option_btn_selector, pressed = pressed)
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = onClick != null,
                    onClick = { onClick?.invoke() },
                ),
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            maxLines = 1,
        )
    }
}

/**
 * 空态默认尺寸，取自 framework 的 `res/values/dimens.xml` 与 `res/layout/blank_view.xml`。
 */
object SmartisanBlankDefaults {
    /** 大图标尺寸，原版 `blank_view.xml` 的 `empty_image` = 120dp。 */
    val LargeImageSize: Dp = 120.dp

    /** 小图标尺寸，原版 `dimen/small_blank_image_size` = 60dp。 */
    val SmallImageSize: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.small_blank_image_size)

    /** 主标题上边距，原版 `blank_view.xml` 的 `empty_primary_hint` = 18dp。 */
    val PrimaryHintTopMargin: Dp = 18.dp

    /** 说明上边距，原版 `blank_view.xml` 的 `empty_secondary_hint` = 5dp。 */
    val SecondaryHintTopMargin: Dp = 5.dp

    /** 圆形操作按钮尺寸，原版 `blank_view.xml` 的 `empty_action_btn` = 60dp × 60dp。 */
    val ActionButtonSize: Dp = 60.dp

    /** 圆形操作按钮上边距，原版 `dimen/blankview_actionbtn_margintop` = 84dp。 */
    val ActionButtonMarginTop: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.blankview_actionbtn_margintop)

    /** 图标上方留白，原版 `dimen/blank_view_vertical_margin` = 40dp。 */
    val VerticalMargin: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(R.dimen.blank_view_vertical_margin)
}
