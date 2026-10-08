package top.smartisanx.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.SmartisanDimens

/**
 * 标题栏容器。
 *
 * 当标题栏里需要放搜索框、标签页等自定义内容时使用，
 * 它保证与 [SmartisanTitleBar] 完全一致的高度、底色与投影。
 */
@Composable
fun SmartisanTitleBarSurface(
    modifier: Modifier = Modifier,
    includeStatusBar: Boolean = true,
    showShadow: Boolean = true,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    Column(modifier.fillMaxWidth().background(colors.titleBarBackground)) {
        if (includeStatusBar) {
            Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
        }
        Box(Modifier.fillMaxWidth().height(contentHeight)) { content() }
        if (showShadow) {
            SmartisanTitleBarShadow()
        }
    }
}
