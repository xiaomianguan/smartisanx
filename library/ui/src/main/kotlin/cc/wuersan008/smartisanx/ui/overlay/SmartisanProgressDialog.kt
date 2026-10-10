package cc.wuersan008.smartisanx.ui.overlay

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePadding
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 进度弹窗的默认尺寸，全部照抄原版 `smartisan_progress_dialog.xml`。 */
object SmartisanProgressDialogDefaults {
    /** 卡片宽度，原版写死 246dp。 */
    val CardWidth: Dp = 246.dp

    /** 圆环尺寸，原版 `ProgressBarCircleStyle.Medium` 的 min/max 都是 48dp。 */
    val SpinnerSize: Dp = 48.dp

    /** 标题与文案的上下外边距，原版都是 8dp。 */
    val TextVerticalMargin: Dp = 8.dp

    /** 圆环上方的间距，原版 `layout_marginTop` 2dp。 */
    val SpinnerTopMargin: Dp = 2.dp

    /** 文案左右内边距，原版两行都是 30dp。 */
    val MessageHorizontalPadding: Dp = 30.dp

    /** 标题字号，原版 18sp。 */
    val TitleTextSize = 18.sp

    /** 文案字号，原版 13sp。 */
    val MessageTextSize = 13.sp

    /** 原版标题 / 文案的默认颜色（浅色 61% 黑）。 */
    val LightTextColor = Color(0x9C000000)

    /** 深色主题下的默认文字颜色（原版 `setDarkTheme(true)` 把颜色改成纯白）。 */
    val DarkTextColor = Color.White

    /**
     * 圆环转一圈的时长。
     *
     * 原版这里没有指定，用的是 ROM 平台默认的不确定 ProgressBar 动画；
     * Compose 里换成匀速旋转等效复刻，按常见的 1s/圈取值，可自行覆盖。
     */
    const val SpinDurationMillis = 1000
}

/**
 * 进度弹窗（framework `smartisanos.app.SmartisanProgressDialog`，布局 `smartisan_progress_dialog.xml`）。
 *
 * 原版就是一个居中的 246dp 卡片，内容竖直居中排列，三段都是可选的：
 * 标题（18sp，单行、居中、上下 8dp）、不确定圆环（48dp，上方 2dp）、
 * 文案（13sp，单行、居中、左右 30dp、上下 8dp）。卡片本身在布局里**没有写任何 padding** ——
 * 那些内边距来自底图 9-patch `smartisan_progress_dialog_bg` 自带的 padding，
 * Android 在把 9-patch 设成背景时会自动吃掉，这里用
 * [rememberSmartisanDrawablePadding] 读同一份数值。
 *
 * 原版可调项与这里的对应关系：
 *
 * | 原版 | 这里 |
 * | --- | --- |
 * | `setDarkTheme(boolean)` | [dark]，同时切底图 `smartisan_progress_dialog_bg_dark` 与文字色 |
 * | `setBackground(Drawable)` | [backgroundRes]（传了就优先用它） |
 * | `setHideProgressBar(boolean)` | [showProgress] |
 * | `setIndeterminateDrawable(Drawable)` | [spinnerRes] |
 * | `setTitleColor` / `setMessageColor` | [titleColor] / [messageColor] |
 * | `setProgress(int)` | 原版是空实现（这个弹窗只有不确定态），这里同样不提供确定进度 |
 *
 * 圆环用的是 ROM 里那张 48dp 的不确定圈素材（`spinner_48_outer_smartisanos_light`，
 * 96px @xhdpi / 144px @xxhdpi，正好 48dp，夜间变体是矢量图）；
 * 原版没给 `ProgressBarCircleStyle.Medium` 指定 `indeterminateDrawable`，
 * 吃的是平台默认的不确定动画，尺寸同样是 48dp。
 *
 * ```kotlin
 * if (loading) {
 *     SmartisanProgressDialog(
 *         onDismissRequest = { loading = false },
 *         message = "正在同步…",
 *     )
 * }
 * ```
 *
 * @param onDismissRequest 点弹窗外或返回键时的回调（原版是 Dialog，可取消）。
 * @param title 标题，`null` 表示不显示（原版 `setVisibility(GONE)`）。
 * @param message 文案，`null` 表示不显示。
 * @param showProgress 是否显示圆环（原版 `setHideProgressBar`）。
 * @param dark 深色主题：换深色底图 + 白色文字（原版 `setDarkTheme`）。
 * @param dimAmount 遮罩浓度，与原版同为窗口 `FLAG_DIM_BEHIND`。
 * @param spinnerRes 圆环素材。
 * @param backgroundRes 卡片底图；`null` 时按 [dark] 自动选。
 */
@Composable
fun SmartisanProgressDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    showProgress: Boolean = true,
    dark: Boolean = false,
    dimAmount: Float = 0.54f,
    @DrawableRes spinnerRes: Int = R.drawable.spinner_48_outer_smartisanos_light,
    @DrawableRes backgroundRes: Int? = null,
    titleColor: Color = Color.Unspecified,
    messageColor: Color = Color.Unspecified,
    spinDurationMillis: Int = SmartisanProgressDialogDefaults.SpinDurationMillis,
) {
    SmartisanModal(onDismissRequest = onDismissRequest, dimAmount = dimAmount) {
        SmartisanProgressDialogCard(
            modifier = modifier,
            title = title,
            message = message,
            showProgress = showProgress,
            dark = dark,
            spinnerRes = spinnerRes,
            backgroundRes = backgroundRes,
            titleColor = titleColor,
            messageColor = messageColor,
            spinDurationMillis = spinDurationMillis,
        )
    }
}


/**
 * 进度弹窗的卡片本体（不含遮罩与窗口），方便塞进别的弹层里复用。
 *
 * 尺寸与间距全部来自原版布局，详见 [SmartisanProgressDialog]。
 */
@Composable
fun SmartisanProgressDialogCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    showProgress: Boolean = true,
    dark: Boolean = false,
    @DrawableRes spinnerRes: Int = R.drawable.spinner_48_outer_smartisanos_light,
    @DrawableRes backgroundRes: Int? = null,
    titleColor: Color = Color.Unspecified,
    messageColor: Color = Color.Unspecified,
    spinDurationMillis: Int = SmartisanProgressDialogDefaults.SpinDurationMillis,
) {
    val typography = LocalSmartisanTypography.current
    val resolvedBackground =
        backgroundRes
            ?: if (dark) {
                R.drawable.smartisan_progress_dialog_bg_dark
            } else {
                R.drawable.smartisan_progress_dialog_bg
            }
    val defaultTextColor =
        if (dark) {
            SmartisanProgressDialogDefaults.DarkTextColor
        } else {
            SmartisanProgressDialogDefaults.LightTextColor
        }
    Column(
        modifier =
            modifier
                .width(SmartisanProgressDialogDefaults.CardWidth)
                .smartisanDrawableBackground(resolvedBackground)
                // 原版布局里没有 padding：这几段内边距是 9-patch 背景自带的，这里读同一份数值。
                .padding(rememberSmartisanDrawablePadding(resolvedBackground)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (title != null) {
            Box(
                modifier = Modifier.padding(vertical = SmartisanProgressDialogDefaults.TextVerticalMargin),
                contentAlignment = Alignment.Center,
            ) {
                SmartisanText(
                    text = title,
                    // 原版 18sp、单行居中、超长省略；没有加粗。
                    style = typography.body.copy(fontSize = SmartisanProgressDialogDefaults.TitleTextSize),
                    color = if (titleColor == Color.Unspecified) defaultTextColor else titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (showProgress) {
            SmartisanProgressSpinner(
                res = spinnerRes,
                modifier = Modifier.padding(top = SmartisanProgressDialogDefaults.SpinnerTopMargin),
                durationMillis = spinDurationMillis,
            )
        }
        if (message != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SmartisanProgressDialogDefaults.MessageHorizontalPadding)
                        .padding(vertical = SmartisanProgressDialogDefaults.TextVerticalMargin),
                contentAlignment = Alignment.Center,
            ) {
                SmartisanText(
                    text = message,
                    style = typography.body.copy(fontSize = SmartisanProgressDialogDefaults.MessageTextSize),
                    color = if (messageColor == Color.Unspecified) defaultTextColor else messageColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * 不确定圆环：把 48dp 的圈素材匀速转起来。
 *
 * 原版是平台 ProgressBar 的不确定动画，Compose 里没有对应物，这里用无限旋转等效复刻。
 */
@Composable
private fun SmartisanProgressSpinner(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    durationMillis: Int = SmartisanProgressDialogDefaults.SpinDurationMillis,
) {
    val transition = rememberInfiniteTransition(label = "smartisan progress dialog spinner")
    val angle by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(durationMillis, easing = LinearEasing)),
            label = "angle",
        )
    SmartisanIcon(
        res = res,
        contentDescription = null,
        modifier =
            modifier
                .size(SmartisanProgressDialogDefaults.SpinnerSize)
                .graphicsLayer { rotationZ = angle },
    )
}
