package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanColorSchemeMode
import cc.wuersan008.smartisanx.core.theme.SmartisanThemeController
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarTextAction

private val galleryTabs = listOf("组件", "整页示例", "设置")
private val galleryTabIcons = listOf(SamplePage.Control.icon, SamplePage.Layouts.icon, SamplePage.Theme.icon)
private val galleryExamples = listOf(SamplePage.Layouts, SamplePage.Messages, SamplePage.Clock, SamplePage.Calendar)

@Composable
internal fun GalleryHome(controller: SmartisanThemeController, onOpen: (SamplePage) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val pages = when (tab) {
        0 -> SamplePage.entries.filter { it !in galleryExamples && it != SamplePage.About }
        1 -> galleryExamples
        else -> emptyList()
    }
    SmartisanScaffold(
        topBar = {
            SmartisanTitleBar(
                title = "SmartisanX",
                trailingContent = if (tab == 2) {
                    { SmartisanTitleBarTextAction("关于", onClick = { onOpen(SamplePage.About) }) }
                } else null,
            )
        },
        bottomBar = {
            SmartisanBottomBar(
                items = galleryTabs.mapIndexed { index, label ->
                    SmartisanBottomBarItem(label = label, iconRes = galleryTabIcons[index])
                },
                selectedIndex = tab,
                onSelected = { tab = it },
                tintResourceIcons = true,
            )
        },
    ) { insets ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = insets.calculateTopPadding() + 24.dp,
                bottom = insets.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SmartisanText(
                    text = galleryTabs[tab],
                    style = typography.title.copy(fontSize = 32.sp),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            if (tab == 2) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SmartisanGroup {
                        SmartisanSwitchRow(
                            text = "深色模式",
                            checked = !colors.isLight,
                            onCheckedChange = { dark ->
                                controller.colorSchemeMode = if (dark) SmartisanColorSchemeMode.Dark else SmartisanColorSchemeMode.Light
                            },
                            position = SmartisanGroupRowPosition.Top,
                            leading = { SmartisanIcon(res = SamplePage.Theme.icon, contentDescription = null, size = 24.dp) },
                        )
                        SmartisanGroupItem(
                            position = SmartisanGroupRowPosition.Bottom,
                            title = "主题与设计变量",
                            leading = { SmartisanIcon(res = SamplePage.Icon.icon, contentDescription = null, size = 24.dp) },
                            onClick = { onOpen(SamplePage.Theme) },
                        )
                    }
                }
            }
            items(pages, key = { it.name }) { page ->
                Column(
                    Modifier.fillMaxWidth().heightIn(min = 124.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceRaised)
                        .smartisanClickable { onOpen(page) }
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SmartisanIcon(res = page.icon, contentDescription = null, size = 28.dp, tint = colors.accent)
                    SmartisanText(page.title, style = typography.body)
                    SmartisanText(page.subtitle, style = typography.caption, color = colors.textSecondary, maxLines = 2)
                }
            }
        }
    }
}
