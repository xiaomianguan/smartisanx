package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar

/**
 * 示例应用首页：按分组列出所有组件页面。
 *
 * 版式照抄原版设置页的主菜单：
 *
 * 1. 页面底色是原版的**细条纹亚麻底纹**（`list_bg` → 平铺的 `common_bg`，
 *    270×270px 的三档灰 `#F0F0F0` / `#F1F1F1` / `#F2F2F2`，肉眼几乎看不出，但确实有纹路）；
 * 2. 列表整体是**一张卡片**：每一行用原版分组卡片的分段底图（上 / 中 / 下各一张，
 *    自带圆角、1px 描边与向外投影），分组容器本身透明，所以卡片圆角之外露出的正是条纹底纹；
 * 3. 每行是「**黑色 glyph 图标 + 标题 + 灰色副标题 + 右侧小箭头**」——
 *    箭头用原版设置项的 `secletor_setting_item_arrow`（6.3×10dp、30% 黑的小箭头），
 *    不是三点；图标一律取自原版素材，同一档粗细（见 [SamplePage] 的注释）。
 */
@Composable
fun SampleHome(onOpen: (SamplePage) -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(
        Modifier
            .fillMaxSize()
            .smartisanDrawableBackground(SmartisanDrawables.PageBackground),
    ) {
        SmartisanTitleBar(title = "smartisanx 组件示例")
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
        ) {
            SmartisanText(
                text = "锤子风格 Compose 组件库。组件来自锤子音乐、锤子天气、锤子时钟三个复刻项目，" +
                    "已去重并统一为同一套 API。",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            SmartisanSectionTitle("组件目录")
            val pages = SamplePage.entries
            SmartisanGroup {
                pages.forEachIndexed { index, page ->
                    SmartisanGroupItem(
                        // 原版分组卡片按位置取上 / 中 / 下三段底图，圆角只出现在首尾。
                        position =
                            when (index) {
                                0 -> SmartisanGroupRowPosition.Top
                                pages.lastIndex -> SmartisanGroupRowPosition.Bottom
                                else -> SmartisanGroupRowPosition.Middle
                            },
                        title = page.title,
                        summary = page.subtitle,
                        leading = {
                            SmartisanIcon(
                                res = page.icon,
                                contentDescription = null,
                                size = page.iconSize,
                            )
                        },
                        trailing = {
                            SmartisanIcon(
                                res = SmartisanDrawables.SettingsItemArrow,
                                contentDescription = null,
                            )
                        },
                        onClick = { onOpen(page) },
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
