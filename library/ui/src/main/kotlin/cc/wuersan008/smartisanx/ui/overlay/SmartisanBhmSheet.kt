package cc.wuersan008.smartisanx.ui.overlay

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIconButton
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgressIndeterminate

/** 计数徽标配色，对应原版 `BHMItemView.COUNT_BLUE / COUNT_RED / COUNT_GREY`。 */
enum class SmartisanBhmCountColor {
    /** 原版 `COUNT_BLUE`（默认）。 */
    Blue,

    /** 原版 `COUNT_RED`。 */
    Red,

    /** 原版 `COUNT_GREY`。 */
    Grey,
}

/**
 * 弹层列表的一行，对应原版 `BHMDrawerItem` + `BHMItemView` 的组合。
 *
 * @param title 主标题（16sp 加粗，单行省略）
 * @param iconRes 左侧图标（18dp 盒、`centerInside`）；`null` 表示不显示
 * @param subtitle 右侧副标题（12sp，最多 [SmartisanBhmDefaults.ItemSubtitleMaxWidth]）
 * @param count 计数徽标文字；`null` 表示不显示（原版 `setCount`）
 * @param countColor 徽标配色（原版 `setCountColor`）
 * @param showProgress 是否显示行尾进度圈（原版 `showProgressBar`）
 * @param showAlert 是否显示行尾警示小图标（原版 `showAlert`）
 * @param titleColor 标题颜色；`Color.Unspecified` 用主题 `textSecondary`（原版 `setTitleColor`）
 */
data class SmartisanBhmItem(
    val title: String,
    @DrawableRes val iconRes: Int? = null,
    val subtitle: String? = null,
    val count: String? = null,
    val countColor: SmartisanBhmCountColor = SmartisanBhmCountColor.Blue,
    val showProgress: Boolean = false,
    val showAlert: Boolean = false,
    val enabled: Boolean = true,
    val titleColor: Color = Color.Unspecified,
    val onClick: (() -> Unit)? = null,
)

/** 带标题的列表弹层（BHM）的默认尺寸，全部照抄原版 `bhm_*.xml` 与 `values/styles.xml`。 */
object SmartisanBhmDefaults {
    /** 行最小高度，原版 `bhm_item_view_min_height`。 */
    val ItemMinHeight = 48.dp

    /** 行图标尺寸，原版 `bhm_item_view_icon_width`。 */
    val ItemIconSize = 18.dp

    /** 行左边距，原版 `bhm_item_view_left_margin`。 */
    val ItemLeftMargin = 15.dp

    /** 标题与图标之间的间距，原版 `bhm_item_view_title_left_margin`。 */
    val ItemTitleLeftMargin = 15.dp

    /** 标题右侧留白，原版 `bhm_item_view_title_right_margin`。 */
    val ItemTitleRightMargin = 6.dp

    /** 标题最大宽度，原版 `bhm_list_view_title_max_width`。 */
    val TitleMaxWidth = 225.dp

    /** 有副标题时的标题最大宽度，原版 `bhm_list_view_title_max_width_subtitle_exists`。 */
    val TitleMaxWidthWithSubtitle = 150.dp

    /** 行尾进度圈尺寸，原版 `bhm_item_view_progress_width`。 */
    val ItemProgressSize = 30.dp

    /** 进度圈右侧留白，原版 `bhm_item_view_progress_margin_right`。 */
    val ItemProgressRightMargin = 15.dp

    /** 副标题右侧留白，原版 `bhm_item_view_subtitle_margin_right`。 */
    val ItemSubtitleRightMargin = 6.dp

    /** 副标题最大宽度，原版 `bhm_item_view_subtitle_max_width`。 */
    val ItemSubtitleMaxWidth = 82.dp

    /** 计数徽标右侧留白，原版 `BHMCount` 样式的 `layout_marginRight`。 */
    val CountRightMargin = 18.dp

    /** 分组标题最小高度，原版 `bhm_header_view_min_height`。 */
    val HeaderMinHeight = 24.dp

    /** 分组标题左内边距，原版 `bhm_header_view_left_margin`。 */
    val HeaderLeftMargin = 24.dp

    /** 分组标题右内边距，原版 `bhm_header_view_right_margin`。 */
    val HeaderRightMargin = 16.dp

    /** 标题栏最小高度，原版 `menu_dialog_title_bar.xml` 的 `minHeight`。 */
    val TitleBarMinHeight = 48.dp

    /** 列表顶部的那条 0.67dp 细线高度，原版 `BHMListShadowStyle` 的 `layout_height`。 */
    val ListTopHairlineHeight = 0.67.dp

    /** 列表顶部细线的透明度，原版 `BHMListShadowStyle` 的 `alpha`。 */
    const val ListTopHairlineAlpha = 0.08f

    /** 弹层最大高度占屏高的比例，原版 `setMaxHeight(screenHeight * 2 / 3)`。 */
    const val MaxHeightFraction = 2f / 3f

    /** 单列 / 合并两列之间滑动切换的时长，原版 `bhm_slide_*` 动画都是 400ms。 */
    const val SlideDurationMillis = SmartisanMotion.DurationLong
}

/** 原版 `bhm_slide_*` 动画用的 `decelerate_interpolator`（二次减速）。 */
private val BhmSlideEasing: Easing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

/** 标题栏图标尺寸，原版 `StandardIconStyle` 的 `standard_icon_size`。 */
private val BhmTitleBarIconSize = 36.dp

/** 返回按钮的无障碍描述（库内惯例用中文文案，与 `SmartisanAssetIcon` 示例一致）。 */
private const val BhmBackLabel = "返回"

/**
 * 弹层列表的一行（原版 `BHMItemView`）。
 *
 * 几何照抄 `bhm_item_view.xml`：整行最小高 48dp、底图 `bhm_item_view_bg`（按下 / 聚焦换色）；
 * 图标 18dp、左边距 15dp、竖直居中；标题 16sp 加粗、跟图标相距 15dp、右侧留 6dp、
 * 最多一行省略；行尾按优先级放「进度圈（30dp，右留 15dp）→ 计数徽标（12sp 白字，右留 18dp）
 * → 警示小图标」；副标题 12sp、贴在行尾元素左侧、最宽 82dp。
 */
@Composable
fun SmartisanBhmRow(
    item: SmartisanBhmItem,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val click = item.onClick
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SmartisanBhmDefaults.ItemMinHeight)
                .smartisanDrawableBackground(
                    drawableRes = SmartisanDrawables.BhmItemBackground,
                    enabled = item.enabled,
                    pressed = selected,
                    selected = selected,
                )
                .then(
                    if (click != null) {
                        Modifier.smartisanClickable(enabled = item.enabled, onClick = click)
                    } else {
                        Modifier
                    },
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = SmartisanBhmDefaults.ItemMinHeight)
                    .padding(start = SmartisanBhmDefaults.ItemLeftMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.iconRes != null) {
                SmartisanIcon(
                    res = item.iconRes,
                    contentDescription = null,
                    modifier = Modifier.size(SmartisanBhmDefaults.ItemIconSize),
                    enabled = item.enabled,
                )
            }
            SmartisanText(
                text = item.title,
                modifier =
                    Modifier
                        .padding(
                            start =
                                if (item.iconRes != null) {
                                    SmartisanBhmDefaults.ItemTitleLeftMargin
                                } else {
                                    0.dp
                                },
                        )
                        .widthIn(
                            max =
                                if (item.subtitle == null) {
                                    SmartisanBhmDefaults.TitleMaxWidth
                                } else {
                                    SmartisanBhmDefaults.TitleMaxWidthWithSubtitle
                                },
                        ),
                // 原版 16sp 加粗、includeFontPadding = false、单行省略。
                style = typography.listItemPrimary.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = if (item.titleColor == Color.Unspecified) colors.textSecondary else item.titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            if (item.subtitle != null) {
                SmartisanText(
                    text = item.subtitle,
                    modifier =
                        Modifier
                            .widthIn(max = SmartisanBhmDefaults.ItemSubtitleMaxWidth)
                            .padding(end = SmartisanBhmDefaults.ItemSubtitleRightMargin),
                    style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = colors.textHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BhmRowTrailing(item)
        }
    }
}


/** 行尾元素：进度圈 / 计数徽标 / 警示图标，三者按原版优先级只显示一个。 */
@Composable
private fun BhmRowTrailing(item: SmartisanBhmItem) {
    val typography = LocalSmartisanTypography.current
    when {
        item.showProgress -> {
            SmartisanCircleProgressIndeterminate(
                modifier = Modifier.size(SmartisanBhmDefaults.ItemProgressSize),
            )
            Spacer(Modifier.width(SmartisanBhmDefaults.ItemProgressRightMargin))
        }

        item.count != null -> {
            SmartisanText(
                text = item.count,
                modifier =
                    Modifier
                        .padding(end = SmartisanBhmDefaults.CountRightMargin)
                        .smartisanDrawableBackground(
                            when (item.countColor) {
                                SmartisanBhmCountColor.Blue -> SmartisanDrawables.BhmCountBlue
                                SmartisanBhmCountColor.Red -> SmartisanDrawables.BhmCountRed
                                SmartisanBhmCountColor.Grey -> SmartisanDrawables.BhmCountGrey
                            },
                        )
                        .padding(horizontal = 6.dp),
                // 原版 BHMCount：12sp 加粗白字，底图 bhm_num_*（徽标自带圆角与内边距）。
                style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
            )
        }

        item.showAlert -> {
            SmartisanIcon(
                res = SmartisanDrawables.BhmItemAlert,
                contentDescription = null,
                modifier = Modifier.padding(end = SmartisanBhmDefaults.ItemProgressRightMargin),
                enabled = item.enabled,
            )
        }
    }
}

/**
 * 弹层列表的分组标题（原版 `bhm_header_view.xml`）。
 *
 * 13.5sp 加粗、左内边距 24dp、右内边距 16dp、最小高度 24dp、单行省略，底图
 * `bhm_header_view_bg`。原版文字色 `bhm_header_view_color` 是 30% 黑，这里用主题
 * `textTertiary`（40% 黑）以便跟随深色主题。
 */
@Composable
fun SmartisanBhmHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanText(
        text = text,
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SmartisanBhmDefaults.HeaderMinHeight)
                .smartisanDrawableBackground(SmartisanDrawables.BhmHeaderBackground)
                .padding(
                    start = SmartisanBhmDefaults.HeaderLeftMargin,
                    end = SmartisanBhmDefaults.HeaderRightMargin,
                ),
        style = typography.sectionTitle.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
        color = colors.textTertiary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 带标题的列表弹层（framework `smartisanos.widget.BHM`）。
 *
 * 原版是文件管理器的「下半屏菜单」：贴底弹出（弹窗主题 `BHMDialog` + `bhm_dialog_in/out` 动画，
 * 300ms 从下往上），内容 = 标题栏（`menu_dialog_title_bar.xml`，`bottom_sheet_title_bar_bg`、
 * 最小高 48dp、左侧返回 / 右侧取消图标 36dp、标题 13.5sp 加粗单行）+ 白色列表区
 * （`bhm_content_layout.xml`，列表顶部插一条 0.67dp、8% 黑的细线），
 * 弹层最高不超过屏高的 2/3（原版 `setMaxHeight(screenHeight * 2 / 3)`）。
 *
 * 原版有「合并 / 单列」两套列表，切换时两个 `ListView` 各跑 400ms 的
 * `decelerate_interpolator` 平移动画（`bhm_slide_in_from_right` / `out_to_left` /
 * `in_from_left` / `out_to_right`）。Compose 里把这个语义做成 [pageKey] + `content`：
 * 只要 [pageKey] 变了，[content] 就会按方向滑入 / 滑出，时长与缓动照原版 400ms 二次减速。
 *
 * ```kotlin
 * var single by remember { mutableStateOf(false) }
 * SmartisanBhmSheet(
 *     onDismissRequest = { visible = false },
 *     title = if (single) "最近文件" else "添加",
 *     onBack = if (single) { { single = false } } else null,
 *     pageKey = single,
 * ) {
 *     // 列表内容
 * }
 * ```
 *
 * @param onDismissRequest 点遮罩、返回键或取消图标时回调。
 * @param title 标题栏文字。
 * @param onBack 非 `null` 时标题栏左侧显示返回图标（原版进入单列时 `setLeftButtonVisibility(VISIBLE)`）。
 * @param pageKey 当前页面标识；变化时内容做左右平移切换。
 * @param maxHeightFraction 弹层最大高度占屏高的比例，原版 2/3。
 * @param dimAmount 遮罩浓度，与原版同为窗口 `FLAG_DIM_BEHIND`。
 * @param content 弹层内容（通常是若干 [SmartisanBhmRow] 与 [SmartisanBhmHeader]）。
 */
@Composable
fun SmartisanBhmSheet(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    pageKey: Any = Unit,
    maxHeightFraction: Float = SmartisanBhmDefaults.MaxHeightFraction,
    dimAmount: Float = 0.54f,
    @DrawableRes titleBarBackgroundRes: Int? = SmartisanDrawables.BottomSheetTitleBarBackground,
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val configuration = LocalConfiguration.current
    val maxHeight = configuration.screenHeightDp.dp * maxHeightFraction
    // 与 SmartisanBottomSheet 共用同一套进出场控制：原版 BHMDialog 的窗口动画就是从屏幕下方滑入滑出。
    val controller = rememberSmartisanOverlayController(onDismissRequest)
    val progress = rememberSmartisanOverlayProgress(controller.visible)
    val dismiss = controller::requestDismiss
    SmartisanModal(onDismissRequest = dismiss, bottom = true, dimAmount = dimAmount) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        // 原版 bhm_dialog_in：整层从屏幕下方 100% 高度滑入，300ms 减速。
                        translationY = (1f - progress) * size.height
                    },
        ) {
            BhmTitleBar(
                title = title,
                onBack = onBack,
                onDismiss = dismiss,
                backgroundRes = titleBarBackgroundRes,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                        // 原版 bhm_content_layout 的列表区底色写死 @android:color/white，这里跟主题。
                        .background(colors.surface),
            ) {
                AnimatedContent(
                    targetState = pageKey,
                    transitionSpec = {
                        val spec = tween<IntOffset>(
                            durationMillis = SmartisanBhmDefaults.SlideDurationMillis,
                            easing = BhmSlideEasing,
                        )
                        (slideInHorizontally(animationSpec = spec) { width -> width } +
                            fadeIn(tween(durationMillis = SmartisanBhmDefaults.SlideDurationMillis))) togetherWith
                            (slideOutHorizontally(animationSpec = spec) { width -> -width } +
                                fadeOut(tween(durationMillis = SmartisanBhmDefaults.SlideDurationMillis)))
                    },
                    label = "smartisan bhm page",
                ) {
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        // 原版把 bhm_list_header_separator 当列表头插入：一条 0.67dp、8% 黑的细线，
                        // 并且会跟着列表一起滚走。
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(SmartisanBhmDefaults.ListTopHairlineHeight)
                                .background(Color.Black.copy(alpha = SmartisanBhmDefaults.ListTopHairlineAlpha)),
                        )
                        content()
                    }
                }
            }
        }
    }
}

/** 弹层的标题栏（原版 `menu_dialog_title_bar.xml`）。 */
@Composable
private fun BhmTitleBar(
    title: String,
    onBack: (() -> Unit)?,
    onDismiss: () -> Unit,
    @DrawableRes backgroundRes: Int?,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.surface)
        }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SmartisanBhmDefaults.TitleBarMinHeight)
                .then(backgroundModifier),
    ) {
        if (onBack != null) {
            SmartisanIconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
                contentDescription = BhmBackLabel,
            ) {
                SmartisanIcon(
                    res = SmartisanDrawables.IconBack,
                    contentDescription = null,
                    size = BhmTitleBarIconSize,
                )
            }
        }
        SmartisanText(
            text = title,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 6.dp),
            // 原版标题 13.5sp 加粗、单行省略（BHM 自己调 setTitleSingleLine(true)）。
            style = typography.dialogTitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SmartisanIconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd),
            contentDescription = stringResource(R.string.smartisan_cancel),
        ) {
            SmartisanIcon(
                res = SmartisanDrawables.IconCancel,
                contentDescription = null,
                size = BhmTitleBarIconSize,
            )
        }
    }
}

