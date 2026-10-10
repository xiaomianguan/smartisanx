package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

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
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.PageBackground,
    content: @Composable () -> Unit,
) {
    // 默认就是原版页面底纹：common_bg 是一张 270×270 的细竖条纹布纹，
    // 原版通过 list_bg / account_background 以 tileMode=repeat 平铺满屏。
    // 传 null 则退回主题的 pageBackground 纯色。
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(containerColor)
        }
    Column(modifier.fillMaxSize().then(backgroundModifier)) {
        if (titleBar != null) {
            // 标题栏（连同它画在栏外的那条投影）要压在内容上面：原版是给标题栏
            // `setElevation(0.1f)`，这里用 zIndex 得到同样的绘制顺序。
            Box(Modifier.fillMaxWidth().zIndex(1f)) { titleBar() }
        }
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
        if (bottomBar != null) {
            Box(Modifier.fillMaxWidth()) { bottomBar() }
        }
    }
}
