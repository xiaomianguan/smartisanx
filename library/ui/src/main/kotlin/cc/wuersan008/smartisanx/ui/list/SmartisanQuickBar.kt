package cc.wuersan008.smartisanx.ui.list

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanColor
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * [SmartisanQuickBar] 的状态：九宫格拖出的程度，以及给宿主用的收起 / 展开。
 *
 * 原版 `QuickBarEx` 有三个状态：`STATE_START`（收起）、`STATE_MOVE`（手指拖着）、`STATE_END`（展开）。
 * 这里用 [fraction] 表示位置（0 = 收起，1 = 展开），拖动时跟手，松手后用 200ms 动画归位。
 */
@Stable
class SmartisanQuickBarState {
    /** 拖出程度：0 = 只露字母栏（收起），1 = 九宫格完全展开。 */
    var fraction: Float by mutableFloatStateOf(0f)
        internal set

    /** 手指是否正拖着面板（原版 `STATE_MOVE`）。 */
    var isDragging: Boolean by mutableStateOf(false)
        internal set

    /** 九宫格是否完整收起（原版 `isLetterGridHidden()`）；字母栏只在这时响应选字母。 */
    val isGridHidden: Boolean get() = !isDragging && fraction == 0f

    /** 九宫格是否完整展开（原版 `isLetterGridShown()`）。 */
    val isGridShown: Boolean get() = !isDragging && fraction == 1f

    private val mutex = MutatorMutex()

    /** 带动画展开九宫格（原版 `showLetterGridWithAnim()`：200ms）。 */
    suspend fun showGrid() = animateTo(1f)

    /** 带动画收起九宫格（原版 `hideLetterGrid(true)`：200ms）。宿主按返回键时调用它。 */
    suspend fun hideGrid() = animateTo(0f)

    internal suspend fun animateTo(target: Float) {
        mutex.mutate {
            isDragging = false
            // 原版是 ObjectAnimator 默认插值（AccelerateDecelerate），时长 200ms。
            animate(fraction, target, animationSpec = tween(SmartisanMotion.DurationShort, easing = SmartisanMotion.EaseInOut)) { value, _ ->
                fraction = value
            }
        }
    }
}

/** 创建并记住一个 [SmartisanQuickBarState]。 */
@Composable
fun rememberSmartisanQuickBarState(): SmartisanQuickBarState = remember { SmartisanQuickBarState() }

/**
 * 锤子联系人的完整字母索引：字母栏 + 往左拖出的 A–Z 九宫格。
 *
 * 复刻 framework 的 `smartisanos.widget.letters.QuickBarEx`（`quickbar_ex.xml`），
 * 联系人、音乐资料库、世界时钟用的都是它。整块面板是一行：
 * **字母栏左侧投影 `letters_bar_background_shadow` → 字母栏 → 九宫格**，作为一个整体横向平移：
 *
 * | 行为 | 原版 |
 * | --- | --- |
 * | 收起时只露字母栏（投影位置留空） | `mStartX = 宿主宽 - 字母栏宽 - 投影宽` |
 * | 手指在字母栏上往左拖过它的左沿，面板左沿跟着手指走 | `dispatchTouchEvent` `ACTION_MOVE`，`STATE_START → STATE_MOVE` |
 * | 松手：最后一下在往左（或没动）且面板左沿离屏幕右边超过 150px 就展开，否则收起 | `ACTION_UP` 里 `mCurrentRawX - mLastRawX <= 0 && getX() < 屏宽 - 150` |
 * | 展开 / 收起动画 200ms，AccelerateDecelerate | `showLetterGridWithAnim()` / `hideLetterGridWithAnim()` |
 * | 展开后往右拖超过 4dp 再次跟手 | `smartisan_quickbar_min_distance` |
 * | 拖出 / 展开时字母栏不再选字母，底色与左侧投影一直显示 | `LettersBar.setSettled(false)`、`setLettersBarBg(true)` |
 * | 九宫格：3 列 × 60dp，列 / 行间距 3px，底色 `#EAEAEA` | `quickbar_ex_grid_column_width`、`smartisan_quickbar_grid_item_space`（**px**） |
 * | 9 行等分高度，余数补给最后一行 | `AlphabetsAdapter.getView`：`(高 - 行间距) / 9` |
 * | 行底色隔行 `#F5F5F5` / `#F0F0F0`，按下 `#E5E5E5`（含夜间色） | `quickbar_ex_alphabet_text_{light,dark}_colorlist` |
 * | 字母 16sp 粗体 `#80000000` | `AlphabetView`、`smartisan_quickbarex_gridview_font_size` |
 * | 第 27 格是收起按钮 | `ENGLISH_ALPHABETS` 末尾的 `.>` → `letter_bar_unfold_button` |
 * | 点九宫格里的字母：回调后收起 | `onItemClick` → `onLetterChanged(…, LETTER_CHANGED_CLICK)` → `hideLetterGridWithAnim()` |
 *
 * R2 联系人实测（400dpi）：收起时整块宽 92px（投影 18 + 字母栏 74），展开后格子 150 × 202px、间距 3px，
 * 收起动画 200ms。
 *
 * **没有移植**：长按九宫格字母弹出的中文姓氏网格（`SurnameGridView` / `SurnameFlowLayout`，
 * 需要宿主提供「该字母下有哪些姓」的数据）。
 *
 * 用法与 [SmartisanLetterIndexBar] 相同，放在列表上层、贴右：
 *
 * ```kotlin
 * val quickBar = rememberSmartisanQuickBarState()
 * BackHandler(enabled = !quickBar.isGridHidden) { scope.launch { quickBar.hideGrid() } }
 * Box(Modifier.fillMaxSize()) {
 *     LazyColumn(state = listState) { ... }
 *     SmartisanQuickBar(
 *         letters = smartisanDefaultLetterIndex(),
 *         onLetterSelected = { letter -> scope.launch { listState.scrollToItem(index[letter] ?: 0) } },
 *         modifier = Modifier.align(Alignment.CenterEnd),
 *         state = quickBar,
 *     )
 * }
 * ```
 *
 * @param letters 字母栏上的字母（原版 A–Z + 底部 `#`），见 [smartisanDefaultLetterIndex]。
 * @param onLetterSelected 字母栏选中字母、或点了九宫格里的字母时回调。
 * @param modifier 作用于整块面板（高度占满父容器），通常 `Modifier.align(Alignment.CenterEnd)`。
 * @param symbols 排在字母栏最前面的图标项，例如联系人的「我」。
 * @param gridLetters 九宫格里的 26 个字母，第 27 格固定是收起按钮。
 * @param state 面板状态，宿主可以用它在返回键时收起九宫格。
 * @param onGridVisibleChange 九宫格开始露出（`onLetterGridShow`）/ 完整收起（`onLetterGridHidden`）时回调。
 */
@Composable
fun SmartisanQuickBar(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    symbols: List<SmartisanLetterSymbol> = emptyList(),
    gridLetters: List<Char> = ('A'..'Z').toList(),
    state: SmartisanQuickBarState = rememberSmartisanQuickBarState(),
    onGridVisibleChange: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val currentOnSelected by rememberUpdatedState(onLetterSelected)
    val currentOnGridVisible by rememberUpdatedState(onGridVisibleChange)
    val keys = remember(symbols, letters) { symbols.map { it.key } + letters }
    // 原版用 Utils.getDrawableWidth() 取字母栏底图与投影的固有宽度，这里同样按素材算。
    val barWidth = with(density) { rememberSmartisanDrawablePainter(SmartisanDrawables.LetterBarBackground).intrinsicSize.width.toDp() }
    val shadowWidth = with(density) { rememberSmartisanDrawablePainter(SmartisanDrawables.LetterBarShadow).intrinsicSize.width.toDp() }
    // 列间距是原版 dimen 里写死的 3px（不是 dp）。
    val gridSpacing = with(density) { QuickBarGridSpacingPx.toDp() }
    val gridWidth = QuickBarGridColumnWidth * QuickBarGridColumns + gridSpacing * (QuickBarGridColumns - 1)
    val panelWidth = shadowWidth + barWidth + gridWidth
    // 收起时面板右移一个九宫格的宽度，只露出「投影位 + 字母栏」（原版 mStartX）。
    val closedOffsetPx = with(density) { gridWidth.toPx() }
    val minDistancePx = with(density) { QuickBarMinDistance.toPx() }
    val windowWidthPx = LocalWindowInfo.current.containerSize.width
    var panelWindowX by remember { mutableFloatStateOf(0f) }
    var touched by remember { mutableIntStateOf(-1) }
    var barTouched by remember { mutableStateOf(false) }
    val out = state.isDragging || state.fraction > 0f

    fun leftPx(): Float = closedOffsetPx * (1f - state.fraction)

    fun hide() = scope.launch {
        state.hideGrid()
        currentOnGridVisible(false)
    }

    Box(
        modifier
            .fillMaxHeight()
            .width(panelWidth)
            .onGloballyPositioned { panelWindowX = it.positionInWindow().x },
    ) {
        Layout(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .offset { IntOffset(leftPx().roundToInt(), 0) }
                    .pointerInput(keys, closedOffsetPx) {
                        val shadowPx = shadowWidth.toPx()
                        val barPx = barWidth.toPx()
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            // 这里的坐标都换算成「面板容器」坐标：左沿 0 = 展开位置，closedOffsetPx = 收起位置。
                            fun containerX(localX: Float) = localX + leftPx()
                            val startedClosed = state.isGridHidden
                            val missX = if (state.isGridShown) containerX(down.position.x) else 0f
                            var current = containerX(down.position.x)
                            var last = current
                            val onBar = down.position.x in shadowPx..(shadowPx + barPx)
                            val letterMode = startedClosed && onBar
                            var reported = -1
                            if (letterMode) {
                                barTouched = true
                                touched = smartisanLetterIndexAt(down.position.y, size.height, keys.size)
                            }
                            while (true) {
                                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                                val x = containerX(change.position.x)
                                if (!change.pressed) {
                                    if (letterMode && state.isGridHidden) {
                                        val index = smartisanLetterIndexAt(change.position.y, size.height, keys.size)
                                        if (index != reported) currentOnSelected(keys[index])
                                    }
                                    break
                                }
                                last = current
                                current = x
                                val wasHidden = state.isGridHidden
                                when {
                                    // STATE_START：手指拖过字母栏左沿（收起时面板左沿）才开始拖出。
                                    wasHidden && x < closedOffsetPx -> {
                                        state.isDragging = true
                                        currentOnGridVisible(true)
                                        touched = -1
                                    }
                                    // STATE_END：往右拖超过 4dp 才重新跟手。
                                    state.isGridShown && x - last > minDistancePx && last > 0f -> state.isDragging = true
                                }
                                if (state.isDragging) {
                                    val left = (x - missX).coerceIn(0f, closedOffsetPx)
                                    state.fraction = 1f - left / closedOffsetPx
                                    if (left >= closedOffsetPx) {
                                        // 拖回原位 = 收起（原版 moveQuickBar → STATE_START + onLetterGridHidden）。
                                        state.isDragging = false
                                        currentOnGridVisible(false)
                                    }
                                    change.consume()
                                } else if (letterMode && state.isGridHidden) {
                                    val index = smartisanLetterIndexAt(change.position.y, size.height, keys.size)
                                    if (index != touched) {
                                        touched = index
                                        reported = index
                                        currentOnSelected(keys[index])
                                    }
                                    change.consume()
                                }
                            }
                            touched = -1
                            barTouched = false
                            if (state.isDragging) {
                                // 原版 ACTION_UP：最后一下往左（或没动）且面板左沿离屏幕右边超过 150px → 展开。
                                val leftInWindow = panelWindowX + leftPx()
                                if (current - last <= 0f && leftInWindow < windowWidthPx - QuickBarSettleThresholdPx) {
                                    scope.launch { state.showGrid() }
                                } else {
                                    hide()
                                }
                            }
                        }
                    },
            content = {
                // 字母栏左侧投影：只在底色显示时可见（原版 setShadowVisible 跟着 setShowBg 走）。
                Box(
                    if (out || barTouched) {
                        Modifier.smartisanDrawableBackground(SmartisanDrawables.LetterBarShadow)
                    } else {
                        Modifier
                    },
                )
                SmartisanLetterBarContent(
                    letters = letters,
                    symbols = symbols,
                    touchedIndex = touched,
                    settled = !out,
                    showBackground = out || barTouched,
                )
                QuickBarGrid(
                    letters = gridLetters,
                    spacing = gridSpacing,
                    onLetter = { letter ->
                        currentOnSelected(letter)
                        hide()
                    },
                    onFold = { hide() },
                )
            },
        ) { measurables, constraints ->
            val height = constraints.maxHeight
            val shadowPx = shadowWidth.roundToPx()
            val barPx = barWidth.roundToPx()
            val gridPx = gridWidth.roundToPx()
            val fixed = { w: Int -> Constraints.fixed(w, height) }
            val shadow = measurables[0].measure(fixed(shadowPx))
            val bar = measurables[1].measure(fixed(barPx))
            val grid = measurables[2].measure(fixed(gridPx))
            layout(shadowPx + barPx + gridPx, height) {
                shadow.place(0, 0)
                bar.place(shadowPx, 0)
                grid.place(shadowPx + barPx, 0)
            }
        }
    }
}

/** 九宫格：3 列 × 9 行，第 27 格是收起按钮（原版 `GridView` + `AlphabetsAdapter`）。 */
@Composable
private fun QuickBarGrid(
    letters: List<Char>,
    spacing: Dp,
    onLetter: (Char) -> Unit,
    onFold: () -> Unit,
) {
    val cells = letters.take(QuickBarGridCells - 1).map { it as Char? } + null
    val lightColor = smartisanColor(R.color.quickbar_ex_alphabet_text_light_color)
    val darkColor = smartisanColor(R.color.quickbar_ex_alphabet_text_dark_color)
    val pressedColor = smartisanColor(R.color.quickbar_ex_alphabet_text_pressed_color)
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    // 格子间距处露出的底色：浅色照抄 quickbar_ex.xml 的 #EAEAEA；夜间比最暗的按下色再暗一档。
    val gapColor = if (colors.isLight) Color(0xFFEAEAEA) else Color(0xFF26292E)
    Box(Modifier.fillMaxHeight().background(gapColor)) {
        Layout(
            content = {
                cells.forEachIndexed { index, letter ->
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    // 原版 (position / 3) % 2：偶数行用 light，奇数行用 dark。
                    val rowColor = if ((index / QuickBarGridColumns) % 2 == 0) lightColor else darkColor
                    Box(
                        modifier =
                            Modifier
                                .background(if (pressed) pressedColor else rowColor)
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { if (letter != null) onLetter(letter) else onFold() },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (letter != null) {
                            SmartisanText(
                                text = letter.toString(),
                                style = typography.caption.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                // 原版 #80000000，与设置页分组标题同色。
                                color = colors.textSectionTitle,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        } else {
                            SmartisanIcon(
                                res = if (pressed) SmartisanDrawables.LetterBarUnfoldPressed else SmartisanDrawables.LetterBarUnfold,
                                contentDescription = "收起",
                            )
                        }
                    }
                }
            },
        ) { measurables, constraints ->
            val spacingPx = spacing.roundToPx()
            val columnPx = QuickBarGridColumnWidth.roundToPx()
            val rows = QuickBarGridCells / QuickBarGridColumns
            val usable = constraints.maxHeight - spacingPx * (rows - 1)
            val cellHeight = usable / rows
            // 原版：除不尽的余数补给最后一行的三个格子。
            val lastRowHeight = cellHeight + (usable - cellHeight * rows)
            val placeables =
                measurables.mapIndexed { index, measurable ->
                    val row = index / QuickBarGridColumns
                    measurable.measure(Constraints.fixed(columnPx, if (row == rows - 1) lastRowHeight else cellHeight))
                }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val row = index / QuickBarGridColumns
                    val column = index % QuickBarGridColumns
                    placeable.place(column * (columnPx + spacingPx), row * (cellHeight + spacingPx))
                }
            }
        }
    }
}

private const val QuickBarGridColumns = 3
private const val QuickBarGridCells = 27
private val QuickBarGridColumnWidth = 60.dp
private val QuickBarMinDistance = 4.dp
private const val QuickBarGridSpacingPx = 3
private const val QuickBarSettleThresholdPx = 150
