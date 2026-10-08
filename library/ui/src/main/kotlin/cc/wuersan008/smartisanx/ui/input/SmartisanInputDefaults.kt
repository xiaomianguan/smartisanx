package cc.wuersan008.smartisanx.ui.input

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import kotlin.math.pow

/**
 * 原版文本 / 输入类组件的公共尺寸与动效参数。
 *
 * 数值全部取自反编译产物（`dimen` 资源与位图固有尺寸），单位换算基准为 xxhdpi（3x）：
 * - 搜索栏：锤子短信 / 日历 / 时钟 / 图库 / 音乐 / 便签 / 录音机 共 7 个 APK 的
 *   `smartisanos.widget.SearchBar`；
 * - 输入框：锤子日历 / 邮件 / 音乐的 `smartisanos.widget.PasswordEditText`、
 *   `smartisanos.widget.QuickDeleteEditText`。
 */
object SmartisanInputDefaults {
    /** 输入框高度。原版 `search_field.9.png` 固有尺寸 146×96px @xxhdpi（≈48.7dp × 32dp）。 */
    val FieldHeight = 32.dp

    /** 输入框与标题栏边缘的留白。原版 `@dimen/bar_margin_edge`。 */
    val EdgeMargin = 6.dp

    /** 搜索框与右侧取消按钮之间的间距。原版 `@dimen/search_bar_margin_search_view`。 */
    val SearchViewGap = 6.dp

    /** 搜索框与右侧筛选按钮之间的间距。原版 `@dimen/search_bar_margin_each`。 */
    val IconGap = 12.dp

    /** 标题栏图标尺寸。原版 `@dimen/standard_icon_size`。 */
    val IconSize = 36.dp

    /** 展开 / 收起时取消按钮与筛选按钮的位移距离。原版 `@dimen/search_bar_anim_distance`。 */
    val AnimDistance = 10.dp

    /** 搜索框左图标宽度。原版 `search_bar_left_icon.png` 72×90px @xxhdpi。 */
    val SearchIconWidth = 24.dp

    /** 搜索框左图标高度。原版 `search_bar_left_icon.png` 72×90px @xxhdpi。 */
    val SearchIconHeight = 30.dp

    /** 搜索框清除按钮尺寸。原版 `text_clear_btn.png` 90×90px @xxhdpi。 */
    val ClearIconSize = 30.dp

    /** 可清空输入框的清除按钮尺寸。原版 `quick_icon_delete` 96×96px @xxhdpi。 */
    val QuickDeleteIconSize = 32.dp

    /** 可清空输入框清除按钮距右边框的距离。原版 `QuickDeleteEditText.mIconPaddingRight = 54px`。 */
    val QuickDeletePaddingEnd = 18.dp

    /** 密码框眼睛图标距右边框的距离。原版 `PasswordEditText.mEyePaddingRight = 48px`。 */
    val EyePaddingEnd = 16.dp

    /** 单项动画时长。原版 `SearchBar.ANIM_DURATION_ITEM`。 */
    const val DurationItem = 200

    /** 整体展开 / 收起时长。原版 `SearchBar.ANIM_DURATION_ALL`。 */
    const val DurationAll = 300

    /** 展开时取消按钮的启动延迟。原版 `SearchBar.ITEM_START_DELAY`。 */
    const val ItemStartDelay = 100
}

/**
 * 原版 `android.view.animation.DecelerateInterpolator(1.5f)`。
 *
 * 计算公式与平台实现一致：`1 - (1 - t) ^ (2 * factor)`，搜索栏的展开 / 收起、
 * 取消按钮淡入淡出都用它。
 */
internal fun smartisanDecelerateEasing(factor: Float = 1.5f): Easing =
    Easing { fraction -> 1f - (1f - fraction).pow(2f * factor) }

/** 原版搜索栏动画的补间规格：默认时长 200ms，可加启动延迟。 */
internal fun <T> smartisanItemTween(
    durationMillis: Int = SmartisanInputDefaults.DurationItem,
    delayMillis: Int = 0,
): TweenSpec<T> =
    tween(
        durationMillis = durationMillis,
        delayMillis = delayMillis,
        easing = smartisanDecelerateEasing(),
    )

/**
 * 输入框右侧的一键清空按钮。
 *
 * 原版把这段逻辑写了两遍：搜索栏的 `search_bar_clear_text`（`smartisanos.widget.SearchBar`）
 * 与可清空输入框的 `quick_icon_delete`（`smartisanos.widget.QuickDeleteEditText`）。
 * 两者的行为完全一致 —— 图标 selector 自带按下态、点击后清空文本 —— 因此在这里抽成唯一实现，
 * 由 [SmartisanSearchBar] 与 [SmartisanClearableField] 共用。
 *
 * 原版只是 `setVisibility(GONE/VISIBLE)` 瞬时切换、没有动画；这里按原版同类按钮
 * （搜索栏取消按钮）的 200ms + `DecelerateInterpolator(1.5f)` 做淡入淡出，
 * 传 `animateVisibility = false` 即可完全对齐原版。
 *
 * @param iconRes 原版图标 selector（[SmartisanSearchBar] 用 `selector_small_icon_btn_text_clear`，
 *   [SmartisanClearableField] 用 `quick_icon_delete`）。
 */
@Composable
internal fun SmartisanClearIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = SmartisanInputDefaults.ClearIconSize,
    visible: Boolean = true,
    enabled: Boolean = true,
    animateVisibility: Boolean = true,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val click =
        smartisanClick {
            haptic()
            onClick()
        }
    val content: @Composable () -> Unit = {
        Box(
            modifier =
                Modifier
                    .size(size)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = click,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            SmartisanIcon(
                res = iconRes,
                contentDescription = contentDescription,
                enabled = enabled,
                pressed = pressed,
                size = size,
                contentScale = ContentScale.Fit,
            )
        }
    }
    if (animateVisibility) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = smartisanItemTween()),
            exit = fadeOut(animationSpec = smartisanItemTween()),
            modifier = modifier,
        ) { content() }
    } else if (visible) {
        Box(modifier) { content() }
    }
}
