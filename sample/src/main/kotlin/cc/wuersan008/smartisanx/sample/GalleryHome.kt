package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanColorSchemeMode
import cc.wuersan008.smartisanx.core.theme.SmartisanThemeController
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.sample.pages.ColorSwatchSection
import cc.wuersan008.smartisanx.sample.pages.IconGrid
import cc.wuersan008.smartisanx.sample.pages.TypographySection
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanIconSet
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.control.SmartisanProgressBar
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.input.SmartisanSearchBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.list.SmartisanSearchSuggestion
import kotlinx.coroutines.launch

private val galleryTabs = listOf(
    SmartisanBottomBarItem(label = "首页", iconRes = R.drawable.setting_item_icon_launcher),
    SmartisanBottomBarItem(label = "图标", iconRes = R.drawable.setting_item_icon_appmanage),
    SmartisanBottomBarItem(label = "色彩", iconRes = R.drawable.setting_item_icon_wallpaper),
    SmartisanBottomBarItem(label = "文字", iconRes = R.drawable.setting_item_icon_keyboard),
    SmartisanBottomBarItem(label = "设置", iconRes = R.drawable.setting_item_icon_advanced),
)

@Composable
internal fun GalleryHome(controller: SmartisanThemeController, onOpen: (SamplePage) -> Unit) {
    val pager = rememberPagerState { galleryTabs.size }
    val scope = rememberCoroutineScope()
    val home = rememberLazyListState()
    val icons = rememberLazyListState()
    val colors = rememberLazyListState()
    val text = rememberLazyListState()
    val settings = rememberLazyListState()
    val lists = listOf(home, icons, colors, text, settings)
    var swipeEnabled by rememberSaveable { mutableStateOf(true) }

    SmartisanScaffold(
        topBar = {
            SmartisanTitleBar(title = galleryTabs[pager.currentPage].label)
        },
        bottomBar = {
            SmartisanBottomBar(
                items = galleryTabs,
                selectedIndex = pager.currentPage,
                onSelected = { index -> scope.launch { pager.animateScrollToPage(index) } },
            )
        },
    ) { insets ->
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize().smartisanDrawableBackground(SmartisanDrawables.PageBackground),
            userScrollEnabled = swipeEnabled,
        ) { index ->
            LazyColumn(
                state = lists[index],
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = insets.calculateTopPadding() + SmartisanDimens.ListItemVerticalGap,
                    bottom = insets.calculateBottomPadding() + 12.dp,
                ),
            ) {
                when (index) {
                    0 -> homeSections(onOpen)
                    1 -> {
                        SmartisanIconSet.groups.forEach { group ->
                            item {
                                SampleSectionHeader(group.title)
                                IconGrid(group.icons.take(8))
                            }
                        }
                        galleryLinks("原版素材", listOf(SamplePage.Icon), onOpen)
                    }
                    2 -> {
                        item { ColorSwatchSection() }
                        galleryLinks("设计变量", listOf(SamplePage.Theme), onOpen)
                    }
                    3 -> {
                        item { TypographySection() }
                        galleryLinks("文字示例", listOf(SamplePage.Text), onOpen)
                    }
                    else -> settingsSections(controller, swipeEnabled, { swipeEnabled = it }, onOpen)
                }
            }
        }
    }
}

private fun LazyListScope.homeSections(onOpen: (SamplePage) -> Unit) {
    item { HomeControls(onOpen) }
    galleryLinks(
        "交互示例",
        listOf(SamplePage.Input, SamplePage.Control, SamplePage.Button, SamplePage.Overlay, SamplePage.ListInteraction),
        onOpen,
    )
    galleryLinks(
        "布局示例",
        listOf(SamplePage.Messages, SamplePage.Cleaner, SamplePage.NotesPhotos, SamplePage.RecorderWaveform, SamplePage.Layouts, SamplePage.Layout, SamplePage.Clock, SamplePage.Calendar),
        onOpen,
    )
}

@Composable
private fun HomeControls(onOpen: (SamplePage) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var switched by rememberSaveable { mutableStateOf(true) }
    var checked by rememberSaveable { mutableStateOf(false) }
    SmartisanSearchBar(
        query = query,
        onQueryChange = { query = it },
        expanded = searchExpanded,
        onExpandedChange = { searchExpanded = it },
        placeholder = "搜索组件",
        onCancel = {
            query = ""
            searchExpanded = false
        },
        showShadow = false,
    )
    if (query.isNotBlank()) {
        val term = query.trim()
        val matches = SamplePage.entries.filter {
            it.title.contains(term, ignoreCase = true) || it.name.contains(term, ignoreCase = true)
        }
        if (matches.isEmpty()) {
            SampleSectionHeader("没有匹配的组件")
        } else {
            matches.forEach { page ->
                SmartisanSearchSuggestion(
                    title = page.title,
                    subtitle = page.subtitle,
                    icon = { GalleryIcon(page) },
                    onClick = { onOpen(page) },
                )
            }
        }
    }
    SampleSectionHeader("基础控件")
    SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SmartisanText("开关", Modifier.weight(1f))
                SmartisanSwitch(switched, { switched = it })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SmartisanText("复选框", Modifier.weight(1f))
                SmartisanCheckbox(checked, { checked = it })
            }
            SmartisanText("进度")
            SmartisanProgressBar(if (checked) 1f else if (switched) 0.55f else 0f)
            SmartisanButton("查看按钮示例", onClick = { onOpen(SamplePage.Button) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun LazyListScope.settingsSections(
    controller: SmartisanThemeController,
    swipeEnabled: Boolean,
    onSwipeChange: (Boolean) -> Unit,
    onOpen: (SamplePage) -> Unit,
) {
    item {
        SampleSectionHeader("外观")
        SmartisanGroup {
            SmartisanSwitchRow(
                text = "深色模式",
                checked = !LocalSmartisanColors.current.isLight,
                onCheckedChange = {
                    controller.colorSchemeMode = if (it) SmartisanColorSchemeMode.Dark else SmartisanColorSchemeMode.Light
                },
                position = SmartisanGroupRowPosition.Top,
                leading = { GalleryIcon(SamplePage.Theme) },
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Bottom,
                title = SamplePage.Theme.title,
                leading = { GalleryIcon(SamplePage.Icon) },
                onClick = { onOpen(SamplePage.Theme) },
            )
        }
    }
    item {
        SampleSectionHeader("导航")
        SmartisanGroup {
            SmartisanSwitchRow(
                text = "左右滑动切换页面",
                checked = swipeEnabled,
                onCheckedChange = onSwipeChange,
                position = SmartisanGroupRowPosition.Single,
                leading = { GalleryIcon(SamplePage.Layout) },
            )
        }
    }
    galleryLinks("应用", listOf(SamplePage.About), onOpen)
}

private fun LazyListScope.galleryLinks(title: String, pages: List<SamplePage>, onOpen: (SamplePage) -> Unit) {
    item {
        SampleSectionHeader(title)
        GalleryLinks(pages, onOpen)
    }
}

@Composable
private fun GalleryLinks(pages: List<SamplePage>, onOpen: (SamplePage) -> Unit) {
    SmartisanGroup {
        pages.forEachIndexed { index, page ->
            val position = when {
                pages.size == 1 -> SmartisanGroupRowPosition.Single
                index == 0 -> SmartisanGroupRowPosition.Top
                index == pages.lastIndex -> SmartisanGroupRowPosition.Bottom
                else -> SmartisanGroupRowPosition.Middle
            }
            SmartisanGroupItem(
                position = position,
                title = page.title,
                summary = page.subtitle,
                leading = { GalleryIcon(page) },
                trailing = { SmartisanIcon(res = SmartisanDrawables.ListItemArrow, contentDescription = null) },
                onClick = { onOpen(page) },
            )
        }
    }
}

@Composable
private fun GalleryIcon(page: SamplePage) {
    SmartisanIcon(res = page.icon, contentDescription = null, size = page.iconSize)
}
