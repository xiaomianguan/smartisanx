package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors

/**
 * 锤子风格页面骨架。
 *
 * 从上到下依次是：标题栏、内容区、底部栏。内容区自动占满剩余空间，
 * 底色使用色板的 `pageBackground`。
 */
@Composable
fun SmartisanScaffold(
    modifier: Modifier = Modifier,
    titleBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    containerColor: Color = LocalSmartisanColors.current.pageBackground,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxSize().background(containerColor)) {
        if (titleBar != null) {
            Box(Modifier.fillMaxWidth()) { titleBar() }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
        if (bottomBar != null) {
            Box(Modifier.fillMaxWidth()) { bottomBar() }
        }
    }
}
