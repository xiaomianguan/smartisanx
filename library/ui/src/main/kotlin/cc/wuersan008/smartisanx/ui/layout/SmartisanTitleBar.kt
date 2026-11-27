package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 锤子风格标题栏。
 *
 * 视觉来自锤子音乐的 `SmartisanTitleBar` 与锤子天气的 `WeatherTitleBar`（两者布局一致，已合并）：
 * - 底色使用原版 NinePatch `titlebar_bg`（自带夜间变体）；
 * - 下方 14dp 使用原版 `title_bar_shadow`，**画在标题栏下边界之外、盖在内容上**：
 *   原版 `BarsHelper` 把投影作为标题栏的子 View，用 `translationY = ±shadowHeight` 平移出
 *   自己的边界，再关掉父 View 的 `clipChildren`、给标题栏 `setElevation(0.1f)`。所以标题栏
 *   本身只有状态栏 + 48dp 高，投影**不占布局空间** —— 内容紧贴标题栏下沿，投影落在内容顶上。
 *   需要一条**占位**的投影带（内容不是同一张底纹时）请自己插 [SmartisanTitleBarShadow]；
 * - 左右 6dp 留白、36dp 图标位、20sp 加粗居中标题；
 * - 图标按压放大 1.33 倍并切换 selector 的按下态。
 *
 * 如果要做纯色标题栏（不想要原版质感），把 [backgroundRes] 设为 `null`，
 * 组件会退回主题的 `titleBarBackground` 颜色。
 */
@Composable
fun SmartisanTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: SmartisanTitleBarAction? = null,
    action: SmartisanTitleBarAction? = null,
    navigationActions: List<SmartisanTitleBarAction> = emptyList(),
    actions: List<SmartisanTitleBarAction> = emptyList(),
    includeStatusBar: Boolean = true,
    showShadow: Boolean = true,
    @DrawableRes shadowRes: Int = SmartisanDrawables.TitleBarShadow,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.TitleBarBackground,
    centerContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val leftActions = listOfNotNull(navigationIcon) + navigationActions
    val rightActions = listOfNotNull(action) + actions
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.titleBarBackground)
        }

    Box(modifier.fillMaxWidth().then(backgroundModifier)) {
        Column(Modifier.fillMaxWidth()) {
            if (includeStatusBar) {
                Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
            }
            if (centerContent != null) {
                Box(Modifier.fillMaxWidth().height(contentHeight)) {
                    Box(Modifier.align(Alignment.Center)) { centerContent() }
                    TitleBarActions(leftActions, rightActions, trailingContent)
                }
            } else {
                // The title is centred on the bar and kept clear of the
                // buttons beside it, by at least [TitleMinGap]; it is cut short
                // only where a button really is (a bar with no right button lets
                // a long title run toward the right edge rather than cut it off
                // as far from the edge as from the left button).
                Layout(
                    modifier = Modifier.fillMaxWidth().height(contentHeight),
                    content = {
                        SmartisanText(
                            text = title,
                            style = typography.titleBar,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                        Row(
                            Modifier.padding(start = SmartisanDimens.TitleBarHorizontalMargin),
                            verticalAlignment = Alignment.CenterVertically,
                        ) { leftActions.forEach { item -> SmartisanTitleBarIcon(item) } }
                        Row(
                            Modifier.padding(end = SmartisanDimens.TitleBarHorizontalMargin),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            rightActions.forEach { item -> SmartisanTitleBarIcon(item) }
                            trailingContent?.invoke()
                        }
                    },
                ) { ms, c ->
                    val w = c.maxWidth
                    val h = c.maxHeight
                    val loose = c.copy(minWidth = 0, minHeight = 0)
                    val left = ms[1].measure(loose)
                    val right = ms[2].measure(loose)
                    val gap = TitleMinGap.roundToPx()
                    val edge = SmartisanDimens.TitleBarHorizontalMargin.roundToPx()
                    val from = if (leftActions.isNotEmpty()) left.width + gap else edge
                    val to = if (rightActions.isNotEmpty() || trailingContent != null) w - right.width - gap else w - edge
                    val room = (to - from).coerceAtLeast(0)
                    val titleP = ms[0].measure(loose.copy(maxWidth = room))
                    // Centred if it fits there, else pushed just clear of the buttons.
                    val x = ((w - titleP.width) / 2).coerceIn(from, (to - titleP.width).coerceAtLeast(from))
                    layout(w, h) {
                        titleP.placeRelative(x, (h - titleP.height) / 2)
                        left.placeRelative(0, (h - left.height) / 2)
                        right.placeRelative(w - right.width, (h - right.height) / 2)
                    }
                }
            }
        }
        if (showShadow) {
            // 投影**不吃布局空间**，而是画在标题栏下边界之外、盖在内容上 —— 原版就是这么做的：
            // `BarsHelper` 把投影作为标题栏的一个子 View，用 `translationY = ±shadowHeight`
            // 把它平移出自己的边界，再把父 View 的 `clipChildren` 关掉、给标题栏
            // `setElevation(0.1f)`，于是标题栏本身只有 48dp 高，投影落在内容顶上。
            // （早先的写法是把投影当普通子 View 排下来，结果标题栏和内容之间多出 14dp 空隙。）
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .offset(y = SmartisanDimens.TitleBarShadowHeight)
                    .height(SmartisanDimens.TitleBarShadowHeight)
                    .smartisanDrawableBackground(shadowRes),
            )
        }
    }
}

/** The least space between a title bar button and the title. */
private val TitleMinGap = 8.dp

/** The buttons at the bar's two ends (with [SmartisanTitleBar]'s centre content). */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.TitleBarActions(
    leftActions: List<SmartisanTitleBarAction>,
    rightActions: List<SmartisanTitleBarAction>,
    trailingContent: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier.align(Alignment.CenterStart).padding(start = SmartisanDimens.TitleBarHorizontalMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) { leftActions.forEach { item -> SmartisanTitleBarIcon(item) } }
    Row(
        modifier = Modifier.align(Alignment.CenterEnd).padding(end = SmartisanDimens.TitleBarHorizontalMargin),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rightActions.forEach { item -> SmartisanTitleBarIcon(item) }
        trailingContent?.invoke()
    }
}

/** 标题栏下方的原版投影（NinePatch，含夜间变体）。 */
@Composable
fun SmartisanTitleBarShadow(
    modifier: Modifier = Modifier,
    height: Dp = SmartisanDimens.TitleBarShadowHeight,
    @DrawableRes shadowRes: Int = SmartisanDrawables.TitleBarShadow,
) {
    // 注意：[SmartisanTitleBar] 里的投影是**画在栏外、盖在内容上**的（原版做法），不吃布局
    // 空间；这个组件是给「内容不是同一张底纹、需要一条占位投影带」的场景用的。
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .smartisanDrawableBackground(shadowRes),
    )
}
