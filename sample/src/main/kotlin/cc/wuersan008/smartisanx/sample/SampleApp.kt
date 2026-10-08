package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.SmartisanColorSchemeMode
import cc.wuersan008.smartisanx.core.theme.SmartisanTheme
import cc.wuersan008.smartisanx.core.theme.ThemeController
import cc.wuersan008.smartisanx.core.theme.rememberSmartisanThemeController
import cc.wuersan008.smartisanx.sample.pages.ButtonPage
import cc.wuersan008.smartisanx.sample.pages.ClockPage
import cc.wuersan008.smartisanx.sample.pages.ControlPage
import cc.wuersan008.smartisanx.sample.pages.IconPage
import cc.wuersan008.smartisanx.sample.pages.InputPage
import cc.wuersan008.smartisanx.sample.pages.LayoutPage
import cc.wuersan008.smartisanx.sample.pages.ListInteractionPage
import cc.wuersan008.smartisanx.sample.pages.OverlayPage
import cc.wuersan008.smartisanx.sample.pages.TextPage
import cc.wuersan008.smartisanx.sample.pages.ThemePage

/**
 * 示例应用根组件。
 *
 * 用 [ThemeController] 管理深浅色，用简单的状态路由在首页与各组件页之间切换，
 * 不引入任何导航库，方便直接阅读。
 */
@Composable
fun SampleApp() {
    // 库默认就是浅色（与原版一致）；这里显式写出，方便读者知道默认值是什么。
    // 想体验实验性的深色方案，可在主题页切换到「跟随系统」或「深色」。
    val controller = rememberSmartisanThemeController(SmartisanColorSchemeMode.Light)
    SmartisanTheme(controller) {
        SystemBarAppearance()
        val colors = LocalSmartisanColors.current
        var page by remember { mutableStateOf<SamplePage?>(null) }
        // 系统返回键：在子页面时回到首页，与标题栏的返回按钮行为一致；
        // 已经在首页时 enabled = false，把返回键交还给系统（退出应用）。
        // 弹窗、底部弹层由各自的 Dialog / 弹层自行消费返回键，不受这里影响。
        BackHandler(enabled = page != null) { page = null }
        Box(Modifier.fillMaxSize().background(colors.pageBackground)) {
            val current = page
            if (current == null) {
                SampleHome(onOpen = { page = it })
            } else {
                val back = { page = null }
                when (current) {
                    SamplePage.Theme -> ThemePage(controller = controller, onBack = back)
                    SamplePage.Text -> TextPage(onBack = back)
                    SamplePage.Icon -> IconPage(onBack = back)
                    SamplePage.Button -> ButtonPage(onBack = back)
                    SamplePage.Control -> ControlPage(onBack = back)
                    SamplePage.Input -> InputPage(onBack = back)
                    SamplePage.Layout -> LayoutPage(onBack = back)
                    SamplePage.ListInteraction -> ListInteractionPage(onBack = back)
                    SamplePage.Overlay -> OverlayPage(onBack = back)
                    SamplePage.Clock -> ClockPage(onBack = back)
                }
            }
        }
    }
}

/** 让状态栏、导航栏图标跟随当前主题深浅色。 */
@Composable
private fun SystemBarAppearance() {
    val view = LocalView.current
    val isLight = LocalSmartisanColors.current.isLight
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = isLight
            controller.isAppearanceLightNavigationBars = isLight
        }
    }
}
