package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction

/** 组件示例页的统一骨架：标题栏 + 可滚动内容 + 底部系统栏留白。 */
@Composable
fun SamplePageScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<SmartisanTitleBarAction> = emptyList(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        SmartisanTitleBar(
            title = title,
            // 原版标题栏图标资源：selector 自带按下 / 禁用态，按压还会放大 1.33 倍。
            navigationIcon =
                SmartisanTitleBarAction(
                    iconRes = SmartisanDrawables.IconBack,
                    contentDescription = "返回",
                    onClick = onBack,
                ),
            actions = actions,
        )
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
            content = content,
        )
        Box(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

/** 页面内的分组标题。 */
@Composable
fun SampleSectionHeader(text: String) {
    SmartisanSectionTitle(text = text)
}

/** 页面底部的说明文字，用于标注组件来源与去重情况。 */
@Composable
fun SampleFootnote(text: String) {
    SmartisanText(
        text = text,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp),
        style = LocalSmartisanTypography.current.caption,
        color = LocalSmartisanColors.current.textDisabled,
    )
}

/**
 * 实验性功能提示。
 *
 * 用来标注「原版 Smartisan OS 没有、由三个复刻项目或本库新增」的特性。
 * 目前主要用于深色模式：原版只有浅色一套设计，深色是复刻项目自行补的，
 * 而且原版图形资源里带夜间变体的只是一小部分。
 */
@Composable
fun ExperimentalNote(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        SmartisanText(
            text = "实验性 · $title",
            style = typography.sectionTitle,
            color = colors.warning,
        )
        SmartisanText(
            text = body,
            modifier = Modifier.padding(top = 4.dp),
            style = typography.caption,
            color = colors.textTertiary,
        )
    }
}
