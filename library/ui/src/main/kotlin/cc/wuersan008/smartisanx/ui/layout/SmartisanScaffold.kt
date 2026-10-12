package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

/**
 * 内容区底部**还需要自己补**的系统导航栏留白。
 *
 * 由 [SmartisanScaffold] 提供，含义是「这条留白还没人补」：
 *
 * - 内容不滚动（`contentScrollable = false`，默认）时是 `0.dp` —— 骨架已经把它补在
 *   内容区外面了，别再补一遍；
 * - 内容滚动（`contentScrollable = true`）时是导航栏的高度 —— 内容可以一直画到屏幕
 *   最底下（透明导航栏下能看到列表），这条留白要加在**滚动内容的末尾**，滚到底时
 *   最后一行才不会被导航栏挡住：
 *
 * ```kotlin
 * val bottom = LocalSmartisanContentBottomInset.current
 * Column(Modifier.fillMaxSize().verticalScroll(state).padding(bottom = bottom)) { /* ... */ }
 * LazyColumn(contentPadding = PaddingValues(bottom = bottom)) { /* ... */ }
 * ```
 *
 * - 有底部栏（`bottomBar`）时是 `0.dp`：底部栏自己吃这条留白（[SmartisanBottomBar]
 *   的 `includeNavigationBar` 默认就是 `true`），内容区在它上面，不需要再让。
 * - 页面自己处理 Insets（`applyNavigationBarInset = false`）时也是 `0.dp`。
 */
val LocalSmartisanContentBottomInset = staticCompositionLocalOf { 0.dp }

/**
 * 锤子风格页面骨架。
 *
 * 从上到下依次是：标题栏、内容区、底部栏。内容区自动占满剩余空间，
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
 * 四个要点：
 *
 * 1. **内容区自己不滚动**。需要滚动就在里面放 `Modifier.verticalScroll(...)` 或
 *    `LazyColumn`（滚动组件默认已经是原版的越界回弹，见 `SmartisanTheme`），
 *    同时把 [contentScrollable] 传 `true`。
 * 2. **底部栏自己吃系统导航栏留白**：`setDecorFitsSystemWindows(false)` 之后内容会
 *    延伸到系统栏底下，[SmartisanBottomBar] 默认（`includeNavigationBar = true`）
 *    把导航栏高度作为自己的内边距，所以它的底色一直画到屏幕最底下，骨架不会再补一条。
 *    自己写的底部栏请照做（`Modifier.windowInsetsPadding(WindowInsets.navigationBars)`）。
 * 3. **没有底部栏时**，导航栏留白由 [applyNavigationBarInset] 与 [contentScrollable] 决定：
 *    内容不滚动时骨架把它补在内容区外面（内容整体在导航栏之上）；内容滚动时内容一直
 *    画到屏幕最底下，留白改由内容从 [LocalSmartisanContentBottomInset] 取、补在滚动末尾。
 * 4. **标题栏的投影画在栏外、盖在内容上**（原版做法），所以内容区不用额外留投影的高度。
 *
 * @param contentScrollable 内容区是否会纵向滚动。传 `true` 时骨架不再在内容区外面留
 *   导航栏的高度，而是把它交给内容补在滚动末尾（见 [LocalSmartisanContentBottomInset]）。
 * @param applyNavigationBarInset 骨架是否负责导航栏留白。自己处理 Insets 的页面
 *   （例如已经用了 `Modifier.windowInsetsPadding`）传 `false`。
 */
@Composable
fun SmartisanScaffold(
    modifier: Modifier = Modifier,
    titleBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    containerColor: Color = LocalSmartisanColors.current.pageBackground,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.PageBackground,
    contentScrollable: Boolean = false,
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
    // 底部栏（SmartisanBottomBar）默认自己吃导航栏留白，所以只有「没有底部栏」时
    // 这条留白才轮到骨架或内容来处理。
    val contentBottomInset: Dp =
        if (applyNavigationBarInset && bottomBar == null) {
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        } else {
            0.dp
        }
    CompositionLocalProvider(
        LocalSmartisanContentBottomInset provides
            if (contentScrollable) contentBottomInset else 0.dp,
    ) {
        Column(modifier.fillMaxSize().then(backgroundModifier)) {
            if (titleBar != null) {
                // 标题栏（连同它画在栏外的那条投影）要压在内容上面：原版是给标题栏
                // `setElevation(0.1f)`，这里用 zIndex 得到同样的绘制顺序。
                Box(Modifier.fillMaxWidth().zIndex(1f)) { titleBar() }
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // 内容不滚动：留白加在内容区外面，内容整体在导航栏之上。
                    // 内容滚动：不加，内容一直画到屏幕最底下，留白由内容补在滚动末尾。
                    .padding(bottom = if (contentScrollable) 0.dp else contentBottomInset),
            ) {
                content()
            }
            if (bottomBar != null) {
                Box(Modifier.fillMaxWidth()) { bottomBar() }
            }
        }
    }
}
