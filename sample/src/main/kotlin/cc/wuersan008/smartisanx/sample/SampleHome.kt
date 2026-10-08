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
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar

/** 示例应用首页：按分组列出所有组件页面。 */
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
            SmartisanText(
                text = "组件目录",
                modifier = Modifier.padding(start = 18.dp, top = 8.dp, bottom = 4.dp),
                style = typography.sectionTitle,
                color = colors.textTertiary,
            )
            val pages = SamplePage.entries
            pages.forEachIndexed { index, page ->
                SmartisanListItem(
                    title = page.title,
                    summary = page.subtitle,
                    leading = {
                        SmartisanIcon(
                            res = page.icon,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            size = 24.dp,
                        )
                    },
                    trailing = {
                        SmartisanIcon(
                            res = SmartisanOriginalIcons.More,
                            contentDescription = null,
                            tint = colors.textDisabled,
                            size = 18.dp,
                        )
                    },
                    onClick = { onOpen(page) },
                )
                if (index != pages.lastIndex) {
                    SmartisanRowDivider(startIndent = 54.dp)
                }
            }
        }
        Box(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
