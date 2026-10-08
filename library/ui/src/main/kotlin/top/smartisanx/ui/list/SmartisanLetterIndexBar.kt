package top.smartisanx.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanText

/**
 * A–Z 字母索引栏。
 *
 * 合并了锤子音乐资料库的字母快捷栏（`quickbar`）与锤子时钟世界时钟的
 * `QuickBarEx`：竖向排列字母、按住拖动连续选择、按下时在旁边显示放大字母气泡。
 */
@Composable
fun SmartisanLetterIndexBar(
    letters: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    activeLetter: Char? = null,
    letterHeight: Dp = 13.dp,
    showOverlay: Boolean = true,
) {
    if (letters.isEmpty()) return
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val density = LocalDensity.current
    val letterHeightPx = with(density) { letterHeight.toPx() }
    var dragging by remember { mutableStateOf(false) }
    var dragIndex by remember { mutableIntStateOf(-1) }

    fun indexAt(y: Float): Int = (y / letterHeightPx).toInt().coerceIn(0, letters.lastIndex)

    fun select(index: Int) {
        if (index != dragIndex) {
            dragIndex = index
            onLetterSelected(letters[index])
        }
    }

    Box(
        modifier =
            modifier
                .width(SmartisanDimens.LetterIndexBarWidth)
                .pointerInput(letters, letterHeightPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        dragging = true
                        select(indexAt(down.position.y))
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            select(indexAt(change.position.y))
                            change.consume()
                        }
                        dragging = false
                        dragIndex = -1
                    }
                },
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            letters.forEach { letter ->
                val isActive = letter == activeLetter
                // 每个字母占用固定高度，索引换算才与手指位置严格对应。
                Box(
                    modifier =
                        Modifier
                            .width(SmartisanDimens.LetterIndexBarWidth)
                            .height(letterHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    SmartisanText(
                        text = letter.toString(),
                        style = typography.caption.copy(fontSize = 10.sp),
                        color = if (isActive) colors.accent else colors.textTertiary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
        if (showOverlay && dragging && dragIndex in letters.indices) {
            Box(
                modifier =
                    Modifier.align(Alignment.Center)
                        // 气泡贴在字母栏左侧：栏宽一半 + 4dp 间距 + 气泡半径 24dp。
                        .offset(x = -(SmartisanDimens.LetterIndexBarWidth / 2 + 28.dp))
                        .size(48.dp)
                        .background(
                            color = colors.textPrimary.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                SmartisanText(
                    text = letters[dragIndex].toString(),
                    style = typography.title,
                    color = colors.pageBackground,
                    maxLines = 1,
                )
            }
        }
    }
}

/** 生成资料库常用的字母索引表：`#` + A–Z。 */
fun smartisanDefaultLetterIndex(): List<Char> = listOf('#') + ('A'..'Z').toList()

/** 把首字母规整到索引表里的取值。 */
fun smartisanIndexLetter(name: String): Char {
    val first = name.firstOrNull()?.uppercaseChar() ?: '#'
    return if (first in 'A'..'Z') first else '#'
}

