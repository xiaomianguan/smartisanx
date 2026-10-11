package cc.wuersan008.smartisanx.sample.layouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.anim.SmartisanPageTransition
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap

/**
 * 整页布局目录。
 *
 * 打开任意一条都会进到一张**完整的页面**（有自己的标题栏、内容和底部栏），
 * 页面的返回按钮回到这里，系统返回键同理 —— 用 [SmartisanPageTransition] 做转场，
 * 和原版锤子音乐的 `PageStackTransition` 一致。
 *
 * 页面里没有一行是示例自己画的「假组件」：全部是 smartisanx 的公开组件，
 * 你可以把任意一张布局整个复制到自己项目里当起点。
 */
@Composable
fun LayoutsPage(onBack: () -> Unit) {
    var open by remember { mutableStateOf<SampleLayout?>(null) }
    // 关闭动画期间被关掉的页面还要继续画，所以留一份「最后打开过的布局」。
    var retained by remember { mutableStateOf<SampleLayout?>(null) }
    LaunchedEffect(open) { if (open != null) retained = open }
    // 子页面在栈顶时先消费返回键，与标题栏的返回按钮行为一致。
    BackHandler(enabled = open != null) { open = null }
    Box(Modifier.fillMaxSize()) {
        SmartisanPageTransition(
            secondary = open != null,
            primary = { LayoutsList(onOpen = { open = it }, onBack = onBack) },
        ) {
            val layout = retained
            if (layout != null) {
                LayoutContent(layout = layout, onBack = { open = null })
            }
        }
    }
}

/** 按分类列出所有整页布局。 */
@Composable
private fun LayoutsList(
    onOpen: (SampleLayout) -> Unit,
    onBack: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SamplePageScaffold(title = "整页布局", onBack = onBack) {
        SmartisanText(
            text = "一屏完整页面怎么拼。上半页是照原版应用拼的，下半页是通用骨架，" +
                "每一张都用同一套组件与设计变量，可以直接复制当起点。",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            style = typography.listItemSecondary,
            color = colors.textTertiary,
        )
        SampleLayoutCategory.entries.forEachIndexed { categoryIndex, category ->
            if (categoryIndex > 0) {
                SmartisanListVerticalGap()
            }
            SampleSectionHeader(category.title)
            val layouts = SampleLayout.entries.filter { it.category == category }
            SmartisanGroup {
                layouts.forEachIndexed { index, layout ->
                    SmartisanGroupItem(
                        position =
                            when (index) {
                                0 -> SmartisanGroupRowPosition.Top
                                layouts.lastIndex -> SmartisanGroupRowPosition.Bottom
                                else -> SmartisanGroupRowPosition.Middle
                            },
                        title = layout.title,
                        summary = layout.subtitle,
                        leading = {
                            SmartisanIcon(
                                res = layout.icon,
                                contentDescription = null,
                                size = layout.iconSize,
                            )
                        },
                        trailing = {
                            SmartisanIcon(
                                res = SmartisanDrawables.SettingsItemArrow,
                                contentDescription = null,
                            )
                        },
                        onClick = { onOpen(layout) },
                    )
                }
            }
        }
        SampleFootnote(
            "这些布局只是版式示例：数据是写死的假数据，业务逻辑（真的播放、真的保存）由你的应用实现。",
        )
    }
}

/** 打开某一张整页布局。 */
@Composable
private fun LayoutContent(
    layout: SampleLayout,
    onBack: () -> Unit,
) {
    when (layout) {
        SampleLayout.MusicLibrary -> MusicLibraryLayout(onBack = onBack)
        SampleLayout.MusicNowPlaying -> MusicNowPlayingLayout(onBack = onBack)
        SampleLayout.ClockAlarm -> ClockAlarmLayout(onBack = onBack)
        SampleLayout.ClockWorld -> ClockWorldLayout(onBack = onBack)
        SampleLayout.Contacts -> ContactsLayout(onBack = onBack)
        SampleLayout.Notes -> NotesLayout(onBack = onBack)
        SampleLayout.Calculator -> CalculatorLayout(onBack = onBack)
        SampleLayout.SettingsSound -> SettingsSoundLayout(onBack = onBack)
        SampleLayout.ListDetail -> ListDetailLayout(onBack = onBack)
        SampleLayout.FormEditor -> FormEditorLayout(onBack = onBack)
        SampleLayout.Profile -> ProfileLayout(onBack = onBack)
        SampleLayout.EmptyState -> EmptyStateLayout(onBack = onBack)
        SampleLayout.MultiSelect -> MultiSelectLayout(onBack = onBack)
    }
}
