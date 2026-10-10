package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables

/**
 * 标题栏容器。
 *
 * 当标题栏里需要放搜索框、标签页等自定义内容时使用，
 * 它保证与 [SmartisanTitleBar] 完全一致的原版底色、高度与投影。
 */
@Composable
fun SmartisanTitleBarSurface(
    modifier: Modifier = Modifier,
    includeStatusBar: Boolean = true,
    showShadow: Boolean = true,
    @DrawableRes shadowRes: Int = SmartisanDrawables.TitleBarShadow,
    contentHeight: Dp = SmartisanDimens.TitleBarHeight,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.TitleBarBackground,
    content: @Composable () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier.background(colors.titleBarBackground)
        }
    Box(modifier.fillMaxWidth().then(backgroundModifier)) {
        Column(Modifier.fillMaxWidth()) {
            if (includeStatusBar) {
                Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars))
            }
            Box(Modifier.fillMaxWidth().height(contentHeight)) { content() }
        }
        if (showShadow) {
            // 与 [SmartisanTitleBar] 同一套做法：投影画在栏外、盖在内容上，不占布局空间。
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .offset(y = SmartisanDimens.TitleBarShadowHeight)
                    .height(SmartisanDimens.TitleBarShadowHeight)
                    .smartisanDrawableBackground(shadowRes),
            )
        }
    }
}