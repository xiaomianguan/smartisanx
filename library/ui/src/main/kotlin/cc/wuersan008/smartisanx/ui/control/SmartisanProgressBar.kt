/**
 * 控件：锤子横向进度条（原版 `progress_horizontal_drawable_*` 这套 layer-list）。
 *
 * 原版没写自定义 View，而是用系统 `ProgressBar` + 一整套 9-patch layer-list：
 * 轨道（`progress_track_smartisanos`）+ 主进度（`progress_smartisanos`）+ 次进度
 * （`secondary_progress_smartisanos`），并给主进度层左右各留 1dp；另外还有失败 / 失焦 / 禁用
 * 三种状态的主进度图，以及一套更细的 thin 变体。这些素材来自文件管理器
 * （`FileManagerSmartisan.apk`，系统 dump 里 `system/system/app/FileManagerSmartisan`），
 * 库内直接按同样的三层结构绘制。
 *
 * | 层 | 素材 | 说明 |
 * | --- | --- | --- |
 * | 轨道 | `progress_track_smartisanos` / `thin_progress_track_smartisanos` | 固有高 26px（8.67dp）/ 20px（6.67dp） |
 * | 主进度 | `progress_smartisanos`（常态）/ `progress_error_smartisanos`（失败）/ `progress_unfocused_smartisanos`（失焦）/ `progress_disabled_smartisanos`（禁用） | 左右各内缩 1dp（原版 layer-list 的 `android:left/right="1dp"`） |
 * | 次进度 | `secondary_progress_smartisanos` | 与轨道同宽，画在主进度之下 |
 *
 * 用法：
 *
 * ```kotlin
 * SmartisanProgressBar(progress = 0.42f, secondaryProgress = 0.6f)
 * SmartisanProgressBar(progress = 0.42f, state = SmartisanProgressBarState.Failed, thin = true)
 * ```
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R

/** 横向进度条的尺寸与素材，对应原版文件管理器的 `progress_*_smartisanos` 素材。 */
object SmartisanProgressBarDefaults {
    /** 常态进度条高度（原版 `progress_*.9.png` 固有高 26px = 8.67dp）。 */
    val Height = 8.67.dp

    /** thin 变体高度（原版 `thin_progress_*.9.png` 固有高 20px = 6.67dp）。 */
    val ThinHeight = 6.67.dp

    /** 主进度层左右的内缩（原版 layer-list 里写死的 1dp）。 */
    val ProgressInset = 1.dp
}

/** 主进度条的四种状态，对应原版四张主进度素材。 */
enum class SmartisanProgressBarState {
    /** 常态（`progress_smartisanos`）。 */
    Normal,

    /** 失败 / 出错（`progress_error_smartisanos`）。 */
    Failed,

    /** 失焦（`progress_unfocused_smartisanos`）。 */
    Unfocused,

    /** 禁用（`progress_disabled_smartisanos`）。 */
    Disabled,
}

/**
 * 横向进度条。
 *
 * @param progress 主进度，0f..1f（超出会被截断）。
 * @param modifier 外部修饰符。
 * @param secondaryProgress 次进度（缓冲 / 第二段），0f..1f。
 * @param state 主进度状态。
 * @param thin 是否用细一档的 thin 变体。
 */
@Composable
fun SmartisanProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    secondaryProgress: Float = 0f,
    state: SmartisanProgressBarState = SmartisanProgressBarState.Normal,
    thin: Boolean = false,
) {
    val barHeight = if (thin) SmartisanProgressBarDefaults.ThinHeight else SmartisanProgressBarDefaults.Height
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(barHeight)
                .smartisanDrawableBackground(smartisanProgressTrackRes(thin)),
    ) {
        if (secondaryProgress > 0f) {
            Box(
                Modifier
                    .fillMaxWidth(secondaryProgress.coerceIn(0f, 1f))
                    .height(barHeight)
                    .smartisanDrawableBackground(smartisanProgressSecondaryRes(thin)),
            )
        }
        if (progress > 0f) {
            // 原版给主进度层左右各留 1dp，让圆角不贴到轨道两端。
            Box(
                Modifier
                    .padding(horizontal = SmartisanProgressBarDefaults.ProgressInset)
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(barHeight)
                    .smartisanDrawableBackground(smartisanProgressFillRes(state, thin)),
            )
        }
    }
}

/** 按状态取主进度素材。 */
@DrawableRes
private fun smartisanProgressFillRes(state: SmartisanProgressBarState, thin: Boolean): Int =
    if (thin) {
        when (state) {
            SmartisanProgressBarState.Normal -> R.drawable.thin_progress_smartisanos
            SmartisanProgressBarState.Failed -> R.drawable.thin_progress_error_smartisanos
            SmartisanProgressBarState.Unfocused -> R.drawable.thin_progress_unfocused_smartisanos
            SmartisanProgressBarState.Disabled -> R.drawable.thin_progress_disabled_smartisanos
        }
    } else {
        when (state) {
            SmartisanProgressBarState.Normal -> R.drawable.progress_smartisanos
            SmartisanProgressBarState.Failed -> R.drawable.progress_error_smartisanos
            SmartisanProgressBarState.Unfocused -> R.drawable.progress_unfocused_smartisanos
            SmartisanProgressBarState.Disabled -> R.drawable.progress_disabled_smartisanos
        }
    }

/** 取轨道素材。 */
@DrawableRes
private fun smartisanProgressTrackRes(thin: Boolean): Int =
    if (thin) R.drawable.thin_progress_track_smartisanos else R.drawable.progress_track_smartisanos

/** 取次进度素材。 */
@DrawableRes
private fun smartisanProgressSecondaryRes(thin: Boolean): Int =
    if (thin) R.drawable.thin_secondary_progress_smartisanos else R.drawable.secondary_progress_smartisanos
