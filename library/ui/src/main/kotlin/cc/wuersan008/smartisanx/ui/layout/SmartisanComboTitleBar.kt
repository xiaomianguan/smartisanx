package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.core.utils.smartisanPainterBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 主标题栏投影的两档高度，对应原版 `SmartisanComboTitleBar.TitleShadowType`。
 *
 * | 枚举 | 原版素材 | 用在哪 |
 * | --- | --- | --- |
 * | [Normal] | `title_bar_shadow`（素材高 48px） | 没有次级栏时，投影露在主栏下方 |
 * | [Short] | `title_bar_shadow_short`（素材高 25px） | 有次级栏时，投影压在次级栏顶部，只要短短一截 |
 */
enum class SmartisanComboTitleShadow {
    Normal,
    Short,
}

/**
 * framework 的复合标题栏（`smartisanos.widget.SmartisanComboTitleBar` +
 * `combo_title_layout.xml` / `primary_title_layout.xml`）。
 *
 * 原版是三层结构，本组件照搬：
 *
 * 1. **主标题栏**（`primary_title`）：高 48dp（`@dimen/titlebar_height`）、底图 `titlebar_bg`；
 * 2. **主栏投影**：和次级栏放在**同一个** FrameLayout 里，所以有次级栏时投影是「压在次级栏顶部」
 *    的一小截（[SmartisanComboTitleShadow.Short]），没有次级栏时才是露在主栏下方的那道
 *    （[SmartisanComboTitleShadow.Normal]）；
 * 3. **次级栏 + 次级栏投影**：次级栏内容由调用方给（原版是 `secondaryBarLayout` 指定的布局），
 *    它下面再叠一层 `secondary_bar_shadow`。
 *
 * 最值得照抄的是原版的**中槽摆法**（`adjustContainerParams()`，本组件用 `SubcomposeLayout` 实现）：
 *
 * | 情况 | 中槽位置 | 中槽可用宽度 |
 * | --- | --- | --- |
 * | 装得下：`2 × max(左, 右) + 中 < 栏宽 - 2 × 12dp` | 在**整条栏里居中** | `栏宽 - 2 × max(左, 右) - 2 × 12dp` |
 * | 装不下 | 取消居中、**贴左按钮右侧**排布 | `栏宽 - 左 - 右 - 2 × 12dp` |
 *
 * 也就是说：标题短的时候按整条栏居中（左右按钮一宽一窄也不会把它挤歪），标题长了就退化成
 * 「左右按钮之间剩下的那段」。12dp 就是原版 `mid_container_margin`。
 *
 * 判断「装不装得下」用的中槽自然宽度来自 `maxIntrinsicWidth()`（同一个 `Measurable` 在 Compose 里
 * 只能量一次，不能先量自然宽度再重量）。所以中槽内容要能被**固有测量**问出宽度才走上面那张表；
 * 自定义 `Layout` 没实现固有测量时（本库的 `SmartisanButtonTabGroup` 就是一个），一律按
 * 「装不下」处理 —— 不是因为宽，而是因为量不出来。
 *
 * 原版中槽有 6 种 `style`（`STYLE_CONTENT_NORMAL` 到 `STYLE_CONTENT_SEPARATOR`，分别是
 * 普通文字 / 单选标签页 / 下拉 / 左右翻页 / 跑马灯 / 分隔条），本组件用一个 [center] 槽位代替，
 * 需要哪种就把对应的库内组件放进去；只写 [title] / [subtitle] 时就是普通文字那一档
 * （原版这档文字用的是 `title_bar_title_text_size` = 20sp，副标题 `item_sub_title_size` = 10sp）。
 *
 * ```kotlin
 * SmartisanComboTitleBar(
 *     title = "全部邮件",
 *     subtitle = "共 128 封",
 *     leading = { SmartisanIconButton(SmartisanDrawables.IconBack, "返回") {} },
 *     trailing = { SmartisanIconButton(SmartisanDrawables.IconMultiSelect, "多选") {} },
 * )
 * ```
 *
 * 与原版一致的地方：左右槽**竖直居中**、中槽随可用宽度收缩、投影随次级栏切换长短。
 * 有意不同的地方：原版把右侧按钮（最多 10 个）用 `-6dp` 右外边距互相压住，本组件不做这种
 * 「按钮容器」，[trailing] 想怎么排由调用方决定（要照抄那套压边距可以用
 * [SmartisanDimens.ComboTitleActionSpacing]）。
 *
 * @param title 中槽主标题；给了 [center] 就忽略 [title] / [subtitle]
 * @param subtitle 中槽副标题，显示在主标题下方
 * @param leading 左槽内容，原版是 `SmartisanButton`（返回 / 取消）
 * @param trailing 右槽内容，原版是一排 `SmartisanButton`（图标之间 `-6dp` 压住）
 * @param center 中槽自定义内容，替代原版的 6 种 style
 * @param secondaryBar 次级栏内容；为 `null` 时整层次级栏与它的投影都不显示
 * @param includeStatusBar 是否在标题栏上方留出状态栏高度
 * @param showTitleShadow 是否显示主栏投影
 * @param showSecondaryShadow 是否显示次级栏投影（只在有次级栏时有意义）
 * @param titleShadow 主栏投影用哪一档；`null` 表示按有没有次级栏自动选（原版行为）
 * @param titleColor 主标题颜色；默认主题 `textSecondary`（≈ 原版 `title_or_btn_text_color` `#99000000`）
 * @param subtitleColor 副标题颜色；默认主题 `textTertiary`
 * @param backgroundRes 主栏底图；传 `null` 用主题 `titleBarBackground` 纯色
 */
@Composable
fun SmartisanComboTitleBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    center: (@Composable () -> Unit)? = null,
    secondaryBar: (@Composable () -> Unit)? = null,
    includeStatusBar: Boolean = true,
    showTitleShadow: Boolean = true,
    showSecondaryShadow: Boolean = true,
    titleShadow: SmartisanComboTitleShadow? = null,
    titleColor: Color = Color.Unspecified,
    subtitleColor: Color = Color.Unspecified,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    centerMargin: Dp = SmartisanDimens.ComboTitleCenterMargin,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.TitleBarBackground,
) {
    val colors = LocalSmartisanColors.current
    val density = LocalDensity.current
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.titleBarBackground)
        }
    val shadow =
        titleShadow
            ?: if (secondaryBar != null) {
                SmartisanComboTitleShadow.Short
            } else {
                SmartisanComboTitleShadow.Normal
            }
    val shadowRes =
        when (shadow) {
            SmartisanComboTitleShadow.Normal -> SmartisanDrawables.TitleBarShadow
            SmartisanComboTitleShadow.Short -> SmartisanDrawables.TitleBarShadowShort
        }
    val titleShadowPainter = rememberSmartisanDrawablePainter(shadowRes)
    val titleShadowHeight = with(density) { titleShadowPainter.intrinsicSize.height.toDp() }
    val secondaryShadowPainter =
        rememberSmartisanDrawablePainter(SmartisanDrawables.SecondaryBarShadow)
    val secondaryShadowHeight =
        with(density) { secondaryShadowPainter.intrinsicSize.height.toDp() }

    Column(modifier.fillMaxWidth()) {
        if (includeStatusBar) {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
        }
        PrimaryTitleBar(
            title = title,
            subtitle = subtitle,
            leading = leading,
            trailing = trailing,
            center = center,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            height = contentHeight,
            centerMargin = centerMargin,
            modifier = backgroundModifier,
        )
        // 原版：投影与次级栏同处一个 FrameLayout，且投影画在它顶部 —— 有次级栏时就是
        // 「压在次级栏顶部的一小截」，没有次级栏时这一格只剩投影，看起来就在主栏下方。
        Box(Modifier.fillMaxWidth()) {
            secondaryBar?.invoke()
            if (showTitleShadow) {
                // 9-patch 必须按「底图」画（拉伸交给 nine-patch 自己），不能用 Image + ContentScale，
                // 否则被拉伸的是整张位图、连透明的那一半一起拉进来，投影会几乎看不见。
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(titleShadowHeight)
                        .smartisanPainterBackground(titleShadowPainter),
                )
            }
        }
        if (secondaryBar != null && showSecondaryShadow) {
            Box(
                Modifier.fillMaxWidth()
                    .height(secondaryShadowHeight)
                    .smartisanPainterBackground(secondaryShadowPainter),
            )
        }
    }
}

private enum class PrimarySlot {
    Leading,
    Center,
    Trailing,
}

/**
 * 主栏：左槽 / 中槽 / 右槽三块，中槽位置按原版 `adjustContainerParams()` 的规则算：
 * 装得下就整条栏居中，装不下就贴左按钮右侧、用「左右按钮之间剩下的那段」。
 */
@Composable
private fun PrimaryTitleBar(
    title: String?,
    subtitle: String?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    center: (@Composable () -> Unit)?,
    titleColor: Color,
    subtitleColor: Color,
    height: Dp,
    centerMargin: Dp,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val heightPx = with(density) { height.roundToPx() }
    val marginPx = with(density) { centerMargin.roundToPx() }

    SubcomposeLayout(Modifier.fillMaxWidth().height(height).then(modifier)) { constraints ->
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val sideConstraints = Constraints(maxWidth = width, maxHeight = heightPx)
        val leadingPlaceable =
            subcompose(PrimarySlot.Leading) { leading?.invoke() }
                .firstOrNull()
                ?.measure(sideConstraints)
        val trailingPlaceable =
            subcompose(PrimarySlot.Trailing) { trailing?.invoke() }
                .firstOrNull()
                ?.measure(sideConstraints)
        val centerMeasurables =
            subcompose(PrimarySlot.Center) {
                if (center != null) {
                    center()
                } else {
                    CenterText(
                        title = title,
                        subtitle = subtitle,
                        titleColor = titleColor,
                        subtitleColor = subtitleColor,
                    )
                }
            }
        val leftWidth = leadingPlaceable?.width ?: 0
        val rightWidth = trailingPlaceable?.width ?: 0
        val sideMax = maxOf(leftWidth, rightWidth)
        // 先看中槽「自然宽度」是多少，再决定要不要居中（原版就是先量 midWidth 再比的）。
        // 同一个 Measurable 只能 measure 一次，所以自然宽度只能走 maxIntrinsicWidth()；
        // 自定义 Layout 没实现固有测量时会抛异常（例如把 ButtonTabGroup 塞进 center），
        // 这种情况一律按「装不下」处理：贴左排布，至少不会把它量崩、也不会压到右侧按钮上。
        val centerMeasurable = centerMeasurables.firstOrNull()
        val naturalWidth =
            runCatching { centerMeasurable?.maxIntrinsicWidth(heightPx) }.getOrNull()
        val centered = naturalWidth != null && sideMax * 2 + naturalWidth < width - marginPx * 2
        val available =
            if (centered) {
                // 装得下：居中，两侧按较宽的那边各留一份，再各留 centerMargin。
                width - sideMax * 2 - marginPx * 2
            } else {
                // 装不下：取消居中、贴左按钮右侧，可用宽度 = 左右按钮之间的剩余空间。
                width - leftWidth - rightWidth - marginPx * 2
            }
        val centerPlaceable =
            centerMeasurable?.measure(
                Constraints(maxWidth = available.coerceAtLeast(0), maxHeight = heightPx),
            )
        val centerX =
            if (centered) {
                ((width - (centerPlaceable?.width ?: 0)) / 2).coerceAtLeast(0)
            } else {
                leftWidth + marginPx
            }
        layout(width, heightPx) {
            centerPlaceable?.place(centerX, (heightPx - centerPlaceable.height) / 2)
            leadingPlaceable?.place(0, (heightPx - leadingPlaceable.height) / 2)
            trailingPlaceable?.place(
                width - trailingPlaceable.width,
                (heightPx - trailingPlaceable.height) / 2,
            )
        }
    }
}

/** 中槽的默认内容：主标题 + 副标题（原版普通文字那一档）。 */
@Composable
private fun CenterText(
    title: String?,
    subtitle: String?,
    titleColor: Color,
    subtitleColor: Color,
) {
    if (title == null && subtitle == null) return
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (title != null) {
            SmartisanText(
                text = title,
                style = typography.titleBar,
                color = if (titleColor == Color.Unspecified) colors.textSecondary else titleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (subtitle != null) {
            SmartisanText(
                text = subtitle,
                style = typography.listItemCaptionSmall,
                color = if (subtitleColor == Color.Unspecified) colors.textTertiary else subtitleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
