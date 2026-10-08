package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanShapes
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanFlowLayout
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/** 示例用的姓氏，对应原版姓氏选择弹层里的数据。 */
private val SampleSurnames =
    listOf(
        "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈",
        "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许",
        "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏",
    )

/**
 * 布局页里「原版布局移植」这一段。
 *
 * 目前只有流式布局 [SmartisanFlowLayout]（原版 `smartisanos.widget.letters.SurnameFlowLayout`）。
 */
@Composable
fun OriginalLayoutSections() {
    val colors = LocalSmartisanColors.current
    val shapes = LocalSmartisanShapes.current
    val typography = LocalSmartisanTypography.current
    SampleSectionHeader("流式布局（SurnameFlowLayout）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            // 原版按行自动换行；这里额外给了 8dp 行列间距（原版靠子项外边距留缝，默认 0dp）。
            SmartisanFlowLayout(
                modifier = Modifier.fillMaxWidth(),
                itemSpacing = 8.dp,
                lineSpacing = 8.dp,
            ) {
                SampleSurnames.forEach { surname ->
                    SmartisanText(
                        text = surname,
                        modifier =
                            Modifier
                                .background(color = colors.surfacePressed, shape = shapes.small)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = typography.body,
                    )
                }
            }
            SmartisanText(
                text = "共 ${SampleSurnames.size} 个姓氏，超出容器宽度时自动换行，行高取该行最大高度。",
                modifier = Modifier.padding(top = 12.dp),
                style = typography.caption,
                color = colors.textTertiary,
            )
        }
    }
}
