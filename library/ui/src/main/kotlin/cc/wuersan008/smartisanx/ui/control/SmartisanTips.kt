/**
 * 控件：锤子轻量提示条。
 *
 * 对应原版 `smartisanos.widget.TipsView`（一个套了 `TipsViewAppearance` 样式的 `TextView`），
 * 出现在锤子日历（Calendar 8.1.2）与锤子邮件（Mail 7.1.0）的分组页脚提示里
 * （`calendar_edit_list_footer.xml`、`setting_preference_tips.xml` 等）。
 *
 * 还原要点（照抄原版 `TipsView.java` 与 `values/styles.xml`）：
 * - 字号 `dimen/semi_small_text_size = 13.5sp`；
 * - 文字色 `color/tips_or_section_title_text_color = #80000000`；
 * - 左右内边距 `dimen/group_list_section_title_padding = 30dp`；
 * - 文字阴影 `shadowColor = #2dffffff`、`shadowDy = 2`（物理像素）、`shadowRadius = 0.1`；
 * - `onLayout` 里按行数决定对齐：**单行居中、多行左对齐**
 *   （原版 `setGravity(getLineCount() > 1 ? LEFT : CENTER)`），这里用 `onTextLayout`
 *   读行数后切换 `textAlign`，效果一致。
 *
 * 邮件里的同款样式是 `sos_smartisanos_style_TipsViewAppearance`（值完全相同），
 * 两个 App 的取值都已作为原版资源导入本库。
 */
package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 文字阴影纵向偏移，原版 `android:shadowDy = 2`（TextView 的阴影单位是物理像素）。 */
private const val TipsShadowDy = 2f

/** 文字阴影模糊半径，原版 `android:shadowRadius = 0.1`。 */
private const val TipsShadowRadius = 0.1f

/** 文字阴影颜色，原版 `android:shadowColor = #2dffffff`。 */
private val TipsShadowColor = Color(0x2D_FFFFFF)

/**
 * 锤子轻量提示条。
 *
 * ```kotlin
 * SmartisanTips("提示：同步后会覆盖本地内容")
 * ```
 *
 * @param text 提示文字。
 * @param modifier 外部修饰符。
 * @param color 文字颜色；不指定时用原版 `color/tips_or_section_title_text_color`。
 * @param showShadow 是否绘制原版文字阴影（`#2dffffff`，向下 2px）。
 */
@Composable
fun SmartisanTips(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    showShadow: Boolean = true,
) {
    // 原版 onLayout：行数 > 1 时左对齐，否则居中。
    var lineCount by remember(text) { mutableIntStateOf(1) }
    val resolvedColor = if (color == Color.Unspecified) colorResource(R.color.tips_or_section_title_text_color) else color
    SmartisanText(
        text = text,
        modifier =
            modifier.padding(
                start = dimensionResource(R.dimen.group_list_section_title_padding),
                end = dimensionResource(R.dimen.group_list_section_title_padding),
            ),
        color = resolvedColor,
        textAlign = if (lineCount > 1) TextAlign.Start else TextAlign.Center,
        style =
            TextStyle(
                fontSize = 13.5.sp,
                shadow =
                    if (showShadow) {
                        Shadow(color = TipsShadowColor, offset = Offset(0f, TipsShadowDy), blurRadius = TipsShadowRadius)
                    } else {
                        null
                    },
            ),
        onTextLayout = { layoutResult -> lineCount = layoutResult.lineCount },
    )
}
