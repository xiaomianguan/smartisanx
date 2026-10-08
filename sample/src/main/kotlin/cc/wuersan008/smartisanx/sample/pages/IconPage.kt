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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/**
 * 图标页。
 *
 * 页面主体是**原版图标素材**（`library/ui/src/main/res/drawable*` 里从原版 APK 还原的位图与 selector），
 * 每一项显示图标、常量名与对应的资源文件名；页面最后单独列出库自绘的补充矢量图标。
 */
@Composable
fun IconPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "图标", onBack = onBack) {
        smartisanOriginalIconCatalog.forEach { category ->
            SampleSectionHeader(category.title)
            OriginalIconList(category.icons)
        }
        SampleFootnote(
            "以上全部是原版图标素材，直接取自原版 APK（`library/ui/src/main/res/drawable*`）：" +
                "多数是 selector，按下 / 禁用 / 选中态会自动切换，并带夜间变体；" +
                "组件内部也优先使用这些资源。",
        )

        SampleSectionHeader("库自绘补充图标（原版没有对应素材时使用）")
        SampleFootnote(
            "下面这些 24×24 矢量图标是库自绘的补充，不是原版图标。" +
                "只有当原版没有对应素材时才使用它们；有原版素材的场合请用上面的原版图标。",
        )
        smartisanIconCatalog.forEach { category ->
            SampleSectionHeader(category.title)
            VectorIconGrid(category.icons)
        }

        SampleFootnote(
            "图标体系以原版位图素材为主：SmartisanOriginalIcons 暴露了 109 个原版图标资源，" +
                "矢量图标集（library/icons）仅作补充。页面里的图标尺寸只是示意，" +
                "实际使用时由组件或调用方决定。",
        )
    }
}

/** 原版图标列表：每行一个图标，右侧显示常量名与资源文件名。 */
@Composable
private fun OriginalIconList(icons: List<OriginalIconEntry>) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanGroup {
        icons.forEach { entry ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(colors.surfaceRaised, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    // 位图重载：selector 会按 enabled / pressed / selected 自动切图。
                    SmartisanIcon(
                        res = entry.res,
                        contentDescription = entry.name,
                        size = 30.dp,
                    )
                }
                Column(Modifier.padding(start = 12.dp)) {
                    SmartisanText(
                        text = entry.name,
                        style = typography.caption,
                        color = colors.textPrimary,
                        maxLines = 1,
                    )
                    SmartisanText(
                        text = entry.resourceName,
                        style = typography.caption.copy(fontSize = 9.sp),
                        color = colors.textTertiary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** 自绘矢量图标网格：每行四个，只显示图标名。 */
@Composable
private fun VectorIconGrid(icons: List<Pair<String, ImageVector>>) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanGroup {
        icons.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { (name, icon) ->
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
                                imageVector = icon,
                                contentDescription = name,
                                tint = colors.textPrimary,
                                size = 24.dp,
                            )
                        }
                        SmartisanText(
                            text = name,
                            modifier = Modifier.padding(top = 4.dp),
                            style = typography.caption,
                            color = colors.textTertiary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
                repeat(4 - row.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}
