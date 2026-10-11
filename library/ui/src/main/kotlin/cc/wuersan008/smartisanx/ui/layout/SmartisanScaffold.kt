package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
 * 从上到下依次是：标题栏、内容区、底部栏、系统导航栏留白。内容区自动占满剩余空间，
 * 底色使用色板的 `pageBackground`（或原版页面底纹）。
 *
 * ```kotlin
 * SmartisanScaffold(
 *     titleBar = { SmartisanTitleBar(title = "设置") },
 * ) {
 *     SmartisanListVerticalGap()
 *     SmartisanGroup {
 *         SmartisanGroupItem(SmartisanGroupRowPosition.Single, "第一行")
 *     }
 * }
 * ```
 *
 * 三个要点：
 *
 * 1. **内容区自己不滚动**。需要滚动就在里面放 `Modifier.verticalScroll(...)` 或
 *    `LazyColumn`（滚动组件默认已经是原版的越界回弹，见 `SmartisanTheme`）。
 * 2. **底部自动留出系统导航栏**（[applyNavigationBarInset]）：`setDecorFitsSystemWindows(false)`
 *    之后内容会延伸到系统栏底下，这里补一条和导航栏等高的留白，卡片不会贴着导航栏。
 *    自己处理 Insets 的页面（例如用 `Modifier.windowInsetsPadding`）传 `false`。
 * 3. **标题栏的投影画在栏外、盖在内容上**（原版做法），所以内容区不用额外留投影的高度。
 */
@Composable
fun SmartisanScaffold(
    modifier: Modifier = Modifier,
    titleBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    containerColor: Color = LocalSmartisanColors.current.pageBackground,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.PageBackground,
    applyNavigationBarInset: Boolean = true,
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
        if (applyNavigationBarInset) {
            Box(Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
