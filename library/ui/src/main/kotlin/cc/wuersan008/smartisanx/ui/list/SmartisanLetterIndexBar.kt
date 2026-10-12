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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.ceil

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
 *
 * 手指位置换算也照抄原版 `dispatchTouchEvent`：**整条栏的高度线性映射到整个字母表**
 * （`y / 栏高 * 字母数`），而不是按「每个字母一格」去算 —— 所以放不下时虽然只画了一部分字母，
 * 手指扫过整条栏仍然能选到全部字母。
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
 * @param onLetterSelected 手指落到某个字母上时回调（按下与滑动都会回调，同一格只回调一次）。
 * @param modifier 作用于整条字母栏，通常配合 `Modifier.align(Alignment.CenterEnd)` 使用。
 * @param activeLetter 需要常亮（白色 + 气泡）的字母；原版只有按住时才有这个状态，默认 null。
 * @param showOverlay 按住时是否绘制放大气泡；false 时只把字母改成白色加粗。
 * @param minLetterHeight 每个字母至少占的高度，原版 `letters_bar_single_letter_min_height` = 12dp。
 * @param backgroundRes 字母栏底色（原版 `letters_bar_background`，含夜间变体）；
 *   只在按住时显示，与原版一致。传 null 时任何状态都不画底。
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
) {
    if (letters.isEmpty()) return
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val highlightSize =
        with(density) {
            rememberSmartisanDrawablePainter(SmartisanDrawables.LetterBarHighlight)
                .intrinsicSize
                .toDpSize()
        }
    var dragging by remember { mutableStateOf(false) }
    var dragIndex by remember { mutableIntStateOf(-1) }

    BoxWithConstraints(
        modifier =
            modifier
                .width(SmartisanDimens.LetterIndexBarWidth)
                // 原版是 match_parent：字母栏占满列表高度，字母按可用高度均分。
                .fillMaxHeight()
                .then(
                    if (backgroundRes != null && dragging) {
                        Modifier.smartisanDrawableBackground(backgroundRes)
                    } else {
                        Modifier
                    },
                ),
    ) {
        // 父容器高度无限时（例如塞进可滚动的 Column）退回「每个字母一格」的兜底高度。
        val barHeight =
            if (maxHeight == Dp.Infinity) minLetterHeight * letters.size else maxHeight
        val innerHeight = (barHeight - SmartisanDimens.LetterBarMargin * 2).coerceAtLeast(0.dp)
        val minLetterHeightPx = with(density) { minLetterHeight.toPx() }
        val innerHeightPx = with(density) { innerHeight.toPx() }
        val visibleIndexes =
            remember(letters.size, innerHeightPx, minLetterHeightPx) {
                visibleLetterIndexes(letters.size, innerHeightPx, minLetterHeightPx)
            }
        val slotHeight =
            if (visibleIndexes.isEmpty()) {
                innerHeight
            } else {
                maxOf(innerHeight / visibleIndexes.size, minLetterHeight)
            }
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .pointerInput(letters) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            dragging = true
                            selectLetter(down.position.y, size.height, letters, dragIndex, onLetterSelected)
                                ?.let { dragIndex = it }
                            down.consume()
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) break
                                selectLetter(change.position.y, size.height, letters, dragIndex, onLetterSelected)
                                    ?.let { dragIndex = it }
                                change.consume()
                            }
                            dragging = false
                            dragIndex = -1
                        }
                    },
        ) {
            visibleIndexes.forEachIndexed { slotIndex, letterIndex ->
                val letter = letters[letterIndex]
                val highlighted = letterIndex == dragIndex || letter == activeLetter
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
                    if (showOverlay && highlighted) {
                        // 原版 drawHighlightIcon()：气泡画在字母栏内部、字母后面。
                        Box(
                            Modifier.fillMaxSize()
                                .smartisanDrawableBackground(SmartisanDrawables.LetterBarHighlight),
                        )
                    }
                    SmartisanText(
                        text = letter.toString(),
                        style =
                            typography.caption.copy(
                                fontSize = 10.sp,
                                // 原版 Typeface.DEFAULT_BOLD，按住的字母再 setFakeBoldText(true)。
                                fontWeight = if (highlighted) FontWeight.Black else FontWeight.Bold,
                            ),
                        // 原版按住的字母是纯白，其余是 no_chosen_letter_font_color（#9A9A9A）。
                        color = if (highlighted) Color.White else colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
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

/**
 * 手指 y → 字母下标，照抄原版 `dispatchTouchEvent`：整条栏的高度线性映射到整个字母表。
 *
 * 下标没变时返回 null（原版也只在换格时才回调 listener）。
 */
private fun selectLetter(
    y: Float,
    barHeightPx: Int,
    letters: List<Char>,
    currentIndex: Int,
    onLetterSelected: (Char) -> Unit,
): Int? {
    if (barHeightPx <= 0) return null
    val index = ((y / barHeightPx) * letters.size).toInt().coerceIn(0, letters.lastIndex)
    if (index == currentIndex) return null
    onLetterSelected(letters[index])
    return index
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

