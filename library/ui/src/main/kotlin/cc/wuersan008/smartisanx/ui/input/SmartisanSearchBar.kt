package cc.wuersan008.smartisanx.ui.input

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import androidx.compose.ui.res.stringResource
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarShadow
import kotlinx.coroutines.delay

/**
 * 锤子风格搜索栏。
 *
 * 对应原版 `smartisanos.widget.SearchBar`（布局 `res/layout/search_bar.xml`）。
 * 原版有 9 个 APK 在用：锤子短信、日历、邮件、时钟、图库、音乐、便签、录音机、天气，
 * 其中短信 / 日历 / 时钟 / 图库 / 音乐 / 便签 / 录音机 这 7 个用的是同一份实现。
 *
 * 结构与几何完全照抄原版布局：
 * - 整条高度 `title_bar_height`（48dp），编辑区高 32dp —— 取自原版 NinePatch
 *   `search_field.9.png` 的固有尺寸（146×96px @xxhdpi）；
 * - 编辑区左右留白 `bar_margin_edge`（6dp），底色用原版 `search_bar_edit_bg_selector`；
 * - 左侧放大镜 `search_bar_left_icon_selector`（24×30dp，来自原版位图）；
 * - 输入文字 15sp，原版 `@dimen/search_bar_input_editor_text_size`；
 * - 右侧可挂筛选按钮（原版 `sorting_icon_selector` / `standard_icon_filter_selector`，
 *   36dp，间距 `search_bar_margin_each` 12dp）；
 * - 展开后右侧换成取消按钮 `standard_icon_cancel_selector`（36dp）；
 * - 输入非空时显示清除按钮 `selector_small_icon_btn_text_clear`（30dp，贴右边框）；
 * - 收起态可挂「二级筛选」入口（`search_bar_secondary_filter_divider` +
 *   `search_bar_secondary_filter_btn`，文字 13.5sp / `#a3a3a3`）。
 *
 * 动画按原版 `SearchBar.startAnimation(boolean)` 复刻，时长与插值器一致
 * （`DecelerateInterpolator(1.5f)`）：
 * - 编辑区让位：展开 300ms；收起先延迟 100ms 再 200ms（`ANIM_DURATION_ITEM`）；
 * - 取消按钮：位移 `search_bar_anim_distance`（10dp）→ 0、透明度 0 → 1，
 *   展开时延迟 100ms，收起时无延迟，时长都是 200ms；
 * - 筛选按钮容器：与取消按钮相反（展开时位移 0 → 10dp 并淡出、无延迟；收起时延迟 100ms 淡入）。
 *
 * 说明：
 * - 原版用 `View.GONE/VISIBLE` 在动画首尾切换可见性，这里用透明度门控达到同样效果；
 * - 原版收起搜索态时会 `TextKeyListener.clear` 清空输入，这里同样在收起时回调 `onQueryChange("")`；
 * - 输入框光标原版用 `edittext_cursor_bbackground`（14×15px 的 NinePatch 光标条，已随资源一起搬进库），
 *   但 Compose 的 `cursorBrush` 只接受 `Brush`，因此这里用主题强调色 `accent` 代替；
 * - 文字色原版取 `editor_text_color`（#cc000000）、提示色 `editor_hint_text_color`（#26000000），
 *   这里改用主题语义色 `textPrimary` / `textHint`，以便跟随 smartisanx 深色主题。
 *
 * @param expanded 是否处于搜索态（原版内部维护 `mIsSearchMode`，Compose 里提升给调用方）。
 * @param filterIconRes 收起态右侧的筛选按钮图标；传 `null` 表示没有筛选按钮
 *   （此时展开会让编辑区让位，与原版 `getOffsetX` 一致）。
 * @param secondaryFilterText 收起态编辑区内的二级筛选文案；`null` 表示不显示。
 * @param showShadow 是否在搜索栏下方画原版标题栏投影 `title_bar_shadow`。
 */
@Composable
fun SmartisanSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    placeholder: String = "",
    onCancel: () -> Unit = {},
    onSearchIconClick: () -> Unit = {},
    onSearch: () -> Unit = {},
    @DrawableRes filterIconRes: Int? = null,
    onFilterClick: () -> Unit = {},
    secondaryFilterText: String? = null,
    onSecondaryFilterClick: () -> Unit = {},
    showLeftIcon: Boolean = true,
    showShadow: Boolean = true,
    enabled: Boolean = true,
    autoFocus: Boolean = true,
    withAnimation: Boolean = true,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null,
    fieldHeight: Dp = SmartisanInputDefaults.FieldHeight,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions? = null,
) {

    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 原版 `@dimen/search_bar_anim_distance`：取消 / 筛选按钮的位移动画距离。
    val animDistance = dimensionResource(R.dimen.search_bar_anim_distance)
    // 原版 `@dimen/search_bar_input_editor_text_size`。
    val editorFontSize: TextUnit =
        with(density) { dimensionResource(R.dimen.search_bar_input_editor_text_size).toSp() }
    val editorStyle = typography.body.copy(fontSize = editorFontSize)

    val hasFilter = filterIconRes != null
    // 原版 `getOffsetX`：有筛选按钮时取消按钮不会同时出现，编辑区让位距离恒为 0。
    val reservedWidth =
        if (expanded || hasFilter) {
            SmartisanInputDefaults.IconSize + SmartisanInputDefaults.SearchViewGap
        } else {
            0.dp
        }
    // 展开 300ms；收起延迟 100ms 再 200ms（原版 `startAnimation` 的两个分支）。
    val reservedSpec =
        if (expanded) {
            smartisanItemTween<Dp>(durationMillis = SmartisanInputDefaults.DurationAll)
        } else {
            smartisanItemTween<Dp>(
                durationMillis = SmartisanInputDefaults.DurationItem,
                delayMillis = SmartisanInputDefaults.ItemStartDelay,
            )
        }
    val reserved by animateDpAsState(
        targetValue = reservedWidth,
        animationSpec = if (withAnimation) reservedSpec else snap(),
        label = "smartisan search bar reserved width",
    )

    // 取消按钮：展开时「延迟 100ms 后位移 10dp→0 并淡入」，收起时「位移 0→10dp 并淡出」。
    val cancelDelay = if (expanded) SmartisanInputDefaults.ItemStartDelay else 0
    val cancelAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = if (withAnimation) smartisanItemTween(delayMillis = cancelDelay) else snap(),
        label = "smartisan search bar cancel alpha",
    )
    val cancelOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else animDistance,
        animationSpec = if (withAnimation) smartisanItemTween(delayMillis = cancelDelay) else snap(),
        label = "smartisan search bar cancel offset",
    )

    // 筛选按钮容器：与取消按钮相反（展开时先动、收起时后动）。
    val filterDelay = if (expanded) 0 else SmartisanInputDefaults.ItemStartDelay
    val filterAlpha by animateFloatAsState(
        targetValue = if (expanded) 0f else 1f,
        animationSpec = if (withAnimation) smartisanItemTween(delayMillis = filterDelay) else snap(),
        label = "smartisan search bar filter alpha",
    )
    val filterOffset by animateDpAsState(
        targetValue = if (expanded) animDistance else 0.dp,
        animationSpec = if (withAnimation) smartisanItemTween(delayMillis = filterDelay) else snap(),
        label = "smartisan search bar filter offset",
    )

    // 原版 `AnimationListenr`：动画开始 / 结束时回调。
    LaunchedEffect(expanded) {
        onAnimationStart?.invoke()
        val total =
            if (!withAnimation) {
                0
            } else if (expanded) {
                SmartisanInputDefaults.DurationAll
            } else {
                SmartisanInputDefaults.ItemStartDelay + SmartisanInputDefaults.DurationItem
            }
        if (total > 0) delay(total.toLong())
        onAnimationEnd?.invoke()
    }

    // 原版 `updateEditorStatus`：进入搜索态时聚焦并弹出键盘，退出时收起键盘并清空输入。
    LaunchedEffect(expanded) {
        if (expanded) {
            if (autoFocus) focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
            if (query.isNotEmpty()) onQueryChange("")
        }
    }


    val fieldInteraction = rememberSmartisanInteractionSource()

    Column(modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SmartisanDimens.TitleBarHeight),
        ) {
            // ---- 右侧筛选按钮（原版 search_bar_right_view_container，收起态可见）----
            if (filterIconRes != null && (!expanded || filterAlpha > 0.001f)) {
                val filterInteraction = rememberSmartisanInteractionSource()
                val filterPressed by filterInteraction.collectSmartisanPressedAsState()
                val filterHaptic = smartisanHaptic()
                val filterClick =
                    smartisanClick {
                        filterHaptic()
                        onFilterClick()
                    }
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(
                                start = SmartisanInputDefaults.IconGap,
                                end = SmartisanInputDefaults.EdgeMargin,
                            )
                            .graphicsLayer {
                                translationX = filterOffset.toPx()
                                alpha = filterAlpha
                            },
                ) {
                    SmartisanIcon(
                        res = filterIconRes,
                        contentDescription = stringResource(R.string.smartisan_filter),
                        enabled = enabled,
                        pressed = filterPressed,
                        size = SmartisanInputDefaults.IconSize,
                        modifier =
                            Modifier.clickable(
                                interactionSource = filterInteraction,
                                indication = null,
                                enabled = enabled,
                                role = Role.Button,
                                onClick = filterClick,
                            ),
                    )
                }
            }

            // ---- 编辑区（原版 search_bar_edit_layout）----
            Row(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .padding(
                            start = SmartisanInputDefaults.EdgeMargin,
                            end = SmartisanInputDefaults.EdgeMargin + reserved,
                        )
                        .height(fieldHeight)
                        .smartisanDrawableBackground(
                            drawableRes = R.drawable.search_bar_edit_bg_selector,
                            enabled = enabled,
                        )
                        .clickable(
                            interactionSource = fieldInteraction,
                            indication = null,
                            enabled = enabled,
                            onClick = {
                                if (!expanded) onExpandedChange(true)
                                focusRequester.requestFocus()
                            },
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showLeftIcon) {
                    val iconInteraction = rememberSmartisanInteractionSource()
                    val iconPressed by iconInteraction.collectSmartisanPressedAsState()
                    val iconHaptic = smartisanHaptic()
                    val iconClick =
                        smartisanClick {
                            iconHaptic()
                            onSearchIconClick()
                        }
                    SmartisanIcon(
                        res = R.drawable.search_bar_left_icon_selector,
                        contentDescription = null,
                        enabled = enabled,
                        pressed = iconPressed,
                        modifier =
                            Modifier
                                .padding(start = SmartisanInputDefaults.EdgeMargin)
                                .size(
                                    width = SmartisanInputDefaults.SearchIconWidth,
                                    height = SmartisanInputDefaults.SearchIconHeight,
                                )
                                .clickable(
                                    interactionSource = iconInteraction,
                                    indication = null,
                                    enabled = enabled,
                                    role = Role.Button,
                                    onClick = iconClick,
                                ),
                        contentScale = ContentScale.None,
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(start = SmartisanInputDefaults.EdgeMargin)
                            .focusRequester(focusRequester)
                            // 点输入框本身也要进入搜索态：BasicTextField 会消费掉
                            // 外层 Row 的点击，所以这里用焦点变化来触发展开。
                            .onFocusChanged { state ->
                                if (state.isFocused && !expanded) onExpandedChange(true)
                            },
                    enabled = enabled,
                    readOnly = !expanded,
                    singleLine = true,
                    textStyle = editorStyle.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = keyboardOptions,
                    keyboardActions =
                        keyboardActions
                            ?: KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onSearch()
                                },
                            ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (query.isEmpty()) {
                                SmartisanText(
                                    text = placeholder,
                                    style = editorStyle,
                                    color = colors.textHint,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                // ---- 二级筛选入口（原版 search_bar_secondary_filter，仅收起态显示）----
                if (!expanded) {
                    secondaryFilterText?.let { text ->
                        val secondaryInteraction = rememberSmartisanInteractionSource()
                        val secondaryHaptic = smartisanHaptic()
                        val secondaryClick =
                            smartisanClick {
                                secondaryHaptic()
                                onSecondaryFilterClick()
                            }
                        Row(
                            modifier =
                                Modifier
                                    .padding(end = SmartisanInputDefaults.EdgeMargin)
                                    .clickable(
                                        interactionSource = secondaryInteraction,
                                        indication = null,
                                        enabled = enabled,
                                        role = Role.Button,
                                        onClick = secondaryClick,
                                    ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SmartisanIcon(
                                res = R.drawable.search_bar_secondary_filter_divider,
                                contentDescription = null,
                                enabled = enabled,
                                modifier = Modifier.padding(end = 11.dp),
                                contentScale = ContentScale.None,
                            )
                            SmartisanText(
                                text = text,
                                style = typography.sectionTitle,
                                color = colors.textTertiary,
                                maxLines = 1,
                            )
                            Spacer(Modifier.width(7.dp))
                            SmartisanIcon(
                                res = R.drawable.search_bar_secondary_filter_btn,
                                contentDescription = null,
                                enabled = enabled,
                                contentScale = ContentScale.None,
                            )
                        }
                    }
                }

                // ---- 清除按钮（原版 search_bar_clear_text）----
                if (query.isNotEmpty()) {
                    SmartisanClearIcon(
                        iconRes = R.drawable.selector_small_icon_btn_text_clear,
                        contentDescription = stringResource(R.string.smartisan_clear),
                        onClick = { onQueryChange("") },
                        enabled = enabled,
                    )
                }
            }

            // ---- 取消按钮（原版 search_bar_cancel_button，展开态可见）----
            if (expanded || cancelAlpha > 0.001f) {
                val cancelInteraction = rememberSmartisanInteractionSource()
                val cancelPressed by cancelInteraction.collectSmartisanPressedAsState()
                val cancelHaptic = smartisanHaptic()
                val cancelClick =
                    smartisanClick {
                        cancelHaptic()
                        onCancel()
                        if (expanded) onExpandedChange(false)
                    }
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .padding(
                                start = SmartisanInputDefaults.SearchViewGap,
                                end = SmartisanInputDefaults.EdgeMargin,
                            )
                            .graphicsLayer {
                                translationX = cancelOffset.toPx()
                                alpha = cancelAlpha
                            },
                ) {
                    SmartisanIcon(
                        res = R.drawable.standard_icon_cancel_selector,
                        contentDescription = stringResource(R.string.smartisan_cancel),
                        enabled = enabled,
                        pressed = cancelPressed,
                        size = SmartisanInputDefaults.IconSize,
                        modifier =
                            Modifier.clickable(
                                interactionSource = cancelInteraction,
                                indication = null,
                                enabled = enabled,
                                role = Role.Button,
                                onClick = cancelClick,
                            ),
                    )
                }
            }
        }

        // 原版 `hasShadow` 为 true 时在搜索栏下方挂一层标题栏投影。
        if (showShadow) {
            SmartisanTitleBarShadow()
        }
    }
}
