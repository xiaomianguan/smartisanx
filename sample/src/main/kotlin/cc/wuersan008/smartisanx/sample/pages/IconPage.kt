package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanIconEntry
import cc.wuersan008.smartisanx.ui.asset.SmartisanIconSet
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/**
 * 图标页：展示从原厂 APK 里提取的**原版 UI 图标**。
 *
 * 这里没有任何自绘矢量图标 —— 全部是 `People-11/SmartisanOS_APP_Port` 的 12 个 APK 里
 * 用 apktool 解出的真实素材，保留原始文件名与夜间 / 多密度变体。
 */
@Composable
fun IconPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "原版图标", onBack = onBack) {
        SampleSectionHeader("总览")
        SmartisanGroup {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                SmartisanText(
                    text = "共 ${SmartisanIconSet.size} 个原版图标，分为 ${SmartisanIconSet.groups.size} 组。",
                    style = LocalSmartisanTypography.current.listItemPrimary,
                    color = LocalSmartisanColors.current.textPrimary,
                )
                SmartisanText(
                    text = "全部来自原厂 APK 的 res/drawable*，按内容去重后原样入库；" +
                        "selector 自带按下 / 禁用态，位图带夜间与多密度变体。",
                    modifier = Modifier.padding(top = 4.dp),
                    style = LocalSmartisanTypography.current.listItemSecondary,
                    color = LocalSmartisanColors.current.textTertiary,
                )
            }
        }

        SmartisanIconSet.groups.forEach { group ->
            SampleSectionHeader("${group.title}（${group.icons.size}）")
            IconGrid(group.icons)
        }

        SampleFootnote(
            "本库不再自绘矢量图标：原来的 SmartisanXIcons / SmartisanXStatusIcons / " +
                "SmartisanXMediaIcons / SmartisanXClockIcons 已删除，全部改用这里的原版素材。",
        )
    }
}

@Composable
private fun IconGrid(icons: List<SmartisanIconEntry>) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanGroup {
        icons.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { entry ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .background(colors.surfaceRaised, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            SmartisanIcon(
                                res = entry.res,
                                contentDescription = entry.name,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        SmartisanText(
                            text = entry.name,
                            modifier = Modifier.padding(top = 4.dp),
                            style = typography.caption,
                            color = colors.textTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
                repeat(4 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
