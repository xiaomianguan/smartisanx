package top.smartisanx.sample

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
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.icons.SmartisanXIcons
import top.smartisanx.ui.basic.SmartisanText
import top.smartisanx.ui.layout.SmartisanSectionTitle
import top.smartisanx.ui.layout.SmartisanTitleBar
import top.smartisanx.ui.layout.SmartisanTitleBarAction

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
            navigationIcon =
                SmartisanTitleBarAction(
                    icon = SmartisanXIcons.Back,
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
