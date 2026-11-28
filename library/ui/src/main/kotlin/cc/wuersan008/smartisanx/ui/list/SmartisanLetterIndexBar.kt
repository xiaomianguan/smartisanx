package cc.wuersan008.smartisanx.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.ceil

/**
 * 字母索引栏里的图标项（原版 `LBLetter.fromSymbol` + `QuickBarEx.initSpecialIcons`）。
 *
 * 原版每个图标项要三张位图：常态、「有别的字母被按住」时的持有态、自己被按住时的按下态。
 * 例如联系人在字母表最上面放了一个「我」：常态 `quickbar_me_nor`（灰 `#B3B3B3`）、
 * 持有态 `quickbar_me_holder`，按下态是白色人形压在 `letters_bar_highlight_icon` 气泡上。
 *
 * @param key 回调时传回的键（[SmartisanLetterIndexBar] / [SmartisanQuickBar] 的 `onLetterSelected`）。
 * @param iconRes 常态图标。
 * @param holderIconRes 有别的字母被按住、或字母栏被拖出去时的图标，默认同 [iconRes]。
 * @param touchedIconRes 自己被按住时的图标；`null` 时把 [iconRes] 着成白色（联系人「我」就是这样）。
 */
@Immutable
data class SmartisanLetterSymbol(
    val key: Char,
    @DrawableRes val iconRes: Int,
    @DrawableRes val holderIconRes: Int = iconRes,
    @DrawableRes val touchedIconRes: Int? = null,
)

/**
 * A–Z 字母索引栏。
 *
 * 复刻 framework 的 `smartisanos.widget.letters.LettersBar`（锤子音乐资料库 / 联系人 /
 * 时钟世界时钟的 `QuickBarEx` 里那一条）。原版的几何全部照抄：
 *
 * | 行为 | 原版出处 |
 * | --- | --- |
 * | 高度占满列表 | `quickbar_ex.xml` 里 `LettersBar` 是 `layout_height="match_parent"` |
 * | 宽 30dp | 底色 nine-patch `letters_bar_background` 的固有宽（89px @xxhdpi = 29.7dp） |
 * | 字母 10sp 加粗、`#9A9A9A` | `letters_bar_letter_font_size` 10dp + `Typeface.DEFAULT_BOLD` + `no_chosen_letter_font_color` |
 * | 上下各留 3dp | `smartisan_quickbar_letterbar_margin` |
 * | 每个字母至少占 12dp | `letters_bar_single_letter_min_height` |
 * | 放不下就隔几个画一个 | `LettersBar.calculateVisibleLettersIndexes()`（首尾一定画） |
 * | 底色只在按住时出现 | `setShowBg(true)` 只在 `ACTION_DOWN` / `ACTION_MOVE` 里调用 |
 * | 按住的字母变白加粗，并套 20dp 放大气泡 | `letters_bar_highlight_icon`（60px @xxhdpi）画在字母栏**内部**、字母后面 |
 * | 按下只高亮，不回调 | `dispatchTouchEvent`：`ACTION_DOWN` 只 `setTouchedLetter`，`ACTION_MOVE` 换格时、`ACTION_UP` 时才回调 |
 * | 图标项（[SmartisanLetterSymbol]）排在字母前面 | `QuickBarEx.initLetters()`：先放 `mSymbols`，再放字母表 |
 *
 * 手指位置换算也照抄原版 `dispatchTouchEvent`：**整条栏的高度线性映射到整个字母表**
 * （`y / 栏高 * 字母数`），而不是按「每个字母一格」去算 —— 所以放不下时虽然只画了一部分字母，
 * 手指扫过整条栏仍然能选到全部字母。
 *
 * 需要原版那种「往左拖出 A–Z 九宫格」的完整索引时用 [SmartisanQuickBar]。
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     LazyColumn { ... }
 *     // 原版字母栏压在列表上：`smart_pinnedlist.xml` 里两者同级
 *     SmartisanLetterIndexBar(
 *         letters = smartisanDefaultLetterIndex(),
 *         onLetterSelected = { scope.launch { listState.animateScrollToItem(index[it] ?: 0) } },
 *         modifier = Modifier.align(Alignment.CenterEnd),
 *     )
 * }
 * ```
 *
 * @param letters 字母表；原版是 A–Z 后跟一个 `#`（`LettersConfig.ENGLISH_LETTERS`，
 *   `QuickBarEx.mPoundPosition` 默认 2 = 底部），见 [smartisanDefaultLetterIndex]。
 * @param onLetterSelected 手指移到新字母上、或松手时回调（同一格只回调一次）；图标项回调它的 `key`。
 * @param modifier 作用于整条字母栏，通常配合 `Modifier.align(Alignment.CenterEnd)` 使用。
 * @param activeLetter 需要常亮（白色 + 气泡）的字母；原版只有按住时才有这个状态，默认 null。
 * @param showOverlay 按住时是否绘制放大气泡；false 时只把字母改成白色加粗。
 * @param minLetterHeight 每个字母至少占的高度，原版 `letters_bar_single_letter_min_height` = 12dp。
 * @param backgroundRes 字母栏底色（原版 `letters_bar_background`，含夜间变体）；
 *   只在按住时显示，与原版一致。传 null 时任何状态都不画底。
 * @param symbols 排在字母前面的图标项，例如联系人的「我」。
 */
@Composable
fun SmartisanLetterIndexBar(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    activeLetter: Char? = null,
    showOverlay: Boolean = true,
    minLetterHeight: Dp = SmartisanDimens.LetterIndexMinLetterHeight,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.LetterBarBackground,
    symbols: List<SmartisanLetterSymbol> = emptyList(),
) {
    val keys = remember(symbols, letters) { symbols.map { it.key } + letters }
    if (keys.isEmpty()) return
    val currentOnSelected by rememberUpdatedState(onLetterSelected)
    var touched by remember { mutableIntStateOf(-1) }
    SmartisanLetterBarContent(
        letters = letters,
        symbols = symbols,
        touchedIndex = touched,
        settled = true,
        showBackground = touched >= 0,
        activeKey = activeLetter,
        showOverlay = showOverlay,
        minLetterHeight = minLetterHeight,
        backgroundRes = backgroundRes,
        modifier =
            modifier
                .width(SmartisanDimens.LetterIndexBarWidth)
                .pointerInput(keys) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        // 原版 ACTION_DOWN：只高亮，不回调（列表不会因为一碰就跳）。
                        touched = smartisanLetterIndexAt(down.position.y, size.height, keys.size)
                        var reported = -1
                        down.consume()
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                            val index = smartisanLetterIndexAt(change.position.y, size.height, keys.size)
                            if (!change.pressed) {
                                // 原版 ACTION_UP：再按松手位置回调一次（点一下就跳到那个字母）。
                                if (index != reported) currentOnSelected(keys[index])
                                break
                            }
                            if (index != touched) {
                                touched = index
                                reported = index
                                currentOnSelected(keys[index])
                            }
                            change.consume()
                        }
                        touched = -1
                    }
                },
    )
}

/**
 * 字母栏的绘制部分（[SmartisanLetterIndexBar] 与 [SmartisanQuickBar] 共用），不处理手势。
 *
 * @param touchedIndex 按住的下标（在「图标项 + 字母」合起来的列表里），-1 表示没有。
 * @param settled 原版 `LettersBar.mSettled`：九宫格收起时为 true；拖出或展开时为 false，
 *   此时不画按下气泡、图标项改用持有态。
 */
@Composable
internal fun SmartisanLetterBarContent(
    letters: List<Char>,
    symbols: List<SmartisanLetterSymbol>,
    touchedIndex: Int,
    settled: Boolean,
    showBackground: Boolean,
    modifier: Modifier = Modifier,
    activeKey: Char? = null,
    showOverlay: Boolean = true,
    minLetterHeight: Dp = SmartisanDimens.LetterIndexMinLetterHeight,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.LetterBarBackground,
) {
    val count = symbols.size + letters.size
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val highlightSize =
        with(density) {
            rememberSmartisanDrawablePainter(SmartisanDrawables.LetterBarHighlight)
                .intrinsicSize
                .toDpSize()
        }
    // 原版 isHolderState() || !mSettled：有字母被按住，或者九宫格不在收起位置。
    val holder = touchedIndex >= 0 || !settled
    BoxWithConstraints(
        modifier =
            modifier
                // 原版是 match_parent：字母栏占满列表高度，字母按可用高度均分。
                .fillMaxHeight()
                .then(
                    if (backgroundRes != null && showBackground) {
                        Modifier.smartisanDrawableBackground(backgroundRes)
                    } else {
                        Modifier
                    },
                ),
    ) {
        // 父容器高度无限时（例如塞进可滚动的 Column）退回「每个字母一格」的兜底高度。
        val barHeight = if (maxHeight == Dp.Infinity) minLetterHeight * count else maxHeight
        val innerHeight = (barHeight - SmartisanDimens.LetterBarMargin * 2).coerceAtLeast(0.dp)
        val minLetterHeightPx = with(density) { minLetterHeight.toPx() }
        val innerHeightPx = with(density) { innerHeight.toPx() }
        val visibleIndexes =
            remember(count, innerHeightPx, minLetterHeightPx) {
                visibleLetterIndexes(count, innerHeightPx, minLetterHeightPx)
            }
        val slotHeight =
            if (visibleIndexes.isEmpty()) {
                innerHeight
            } else {
                maxOf(innerHeight / visibleIndexes.size, minLetterHeight)
            }
        Box(Modifier.fillMaxSize()) {
            visibleIndexes.forEachIndexed { slotIndex, index ->
                val symbol = symbols.getOrNull(index)
                val key = symbol?.key ?: letters[index - symbols.size]
                // 原版 drawHighlightIcon / setLetterPaint：只有收起状态下按住的那一格才高亮。
                val touched = settled && (index == touchedIndex || key == activeKey)
                Box(
                    modifier =
                        Modifier.align(Alignment.TopCenter)
                            .offset(
                                y =
                                    SmartisanDimens.LetterBarMargin + slotHeight * slotIndex +
                                        (slotHeight - highlightSize.height) / 2,
                            )
                            .size(highlightSize),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showOverlay && touched) {
                        // 原版 drawHighlightIcon()：气泡画在字母栏内部、字母后面。
                        Box(
                            Modifier.fillMaxSize()
                                .smartisanDrawableBackground(SmartisanDrawables.LetterBarHighlight),
                        )
                    }
                    if (symbol != null) {
                        // 原版 drawSymbol()：按下 / 持有 / 常态三张图，按固有尺寸居中。
                        when {
                            touched && symbol.touchedIconRes != null ->
                                SmartisanIcon(res = symbol.touchedIconRes, contentDescription = null)
                            touched -> SmartisanIcon(res = symbol.iconRes, contentDescription = null, tint = Color.White)
                            holder -> SmartisanIcon(res = symbol.holderIconRes, contentDescription = null)
                            else -> SmartisanIcon(res = symbol.iconRes, contentDescription = null)
                        }
                    } else {
                        SmartisanText(
                            text = key.toString(),
                            style =
                                typography.caption.copy(
                                    fontSize = 10.sp,
                                    // 原版 Typeface.DEFAULT_BOLD，按住的字母再 setFakeBoldText(true)。
                                    fontWeight = if (touched) FontWeight.Black else FontWeight.Bold,
                                ),
                            // 原版按住的字母是纯白；其余是 has / no_chosen_letter_font_color（R2 上同为不透明 #9A9A9A，
                            // 白底上灰度 154 = textTertiary 的 #66000000；textSecondary 会深成 101）。
                            color = if (touched) Color.White else colors.textTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 原版 `LettersBar.calculateVisibleLettersIndexes()`：放不下时每隔 `iMax` 个画一个，首尾一定画。
 *
 * `iMax` 是**偶数**（原版乘了 2），这样跳着画时整条栏不会看起来偏向一侧。
 */
private fun visibleLetterIndexes(count: Int, innerHeightPx: Float, minLetterHeightPx: Float): List<Int> {
    if (count <= 0) return emptyList()
    val fits = minLetterHeightPx <= 0f || innerHeightPx / count >= minLetterHeightPx
    val step =
        if (fits) {
            1
        } else {
            maxOf(2, (ceil(count / (innerHeightPx / minLetterHeightPx - 2.0)) * 2).toInt())
        }
    val indexes = mutableListOf(0)
    if (step < count / 2) {
        var i = step
        while (i < count - 1) {
            indexes += i
            i += step
        }
    }
    if (step < count) indexes += count - 1
    return indexes
}

/** 手指 y → 下标，照抄原版 `dispatchTouchEvent`：整条栏的高度线性映射到整个字母表。 */
internal fun smartisanLetterIndexAt(y: Float, barHeightPx: Int, count: Int): Int {
    if (barHeightPx <= 0 || count <= 0) return 0
    return ((y / barHeightPx) * count).toInt().coerceIn(0, count - 1)
}

/**
 * 生成资料库常用的字母索引表：A–Z 后跟 `#`。
 *
 * 原版 `LettersConfig.ENGLISH_LETTERS` 就是 A–Z 再补一个 `#`，`QuickBarEx.mPoundPosition`
 * 默认 2（`POUND_POSITION_BOTTOM`），所以 `#` 在**最下面**。
 */
fun smartisanDefaultLetterIndex(): List<Char> = ('A'..'Z').toList() + '#'

/** 把首字母规整到索引表里的取值。 */
fun smartisanIndexLetter(name: String): Char {
    val first = name.firstOrNull()?.uppercaseChar() ?: '#'
    return if (first in 'A'..'Z') first else '#'
}
