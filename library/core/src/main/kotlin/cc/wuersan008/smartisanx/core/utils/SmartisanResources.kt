package cc.wuersan008.smartisanx.core.utils

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanDarkOverride

/**
 * 按 **smartisanx 主题** 而不是 **系统 uiMode** 解析资源。
 *
 * 原版素材的夜间变体是放在 `drawable-night` / `values-night` 里的，
 * Android 只会根据系统的 uiMode 去挑。但应用允许用户在应用内切换深浅色时，
 * 系统可能仍是浅色，于是会出现「界面是深色、资源却是浅色」的错配
 * （白色标题栏、白色列表行、浅色开关等）。
 *
 * 这里在两者不一致时，用一个覆盖了 uiMode 的 [Configuration] 重新取 [Resources]，
 * 让原版资源跟随应用主题。两者一致时直接复用系统资源，不产生额外开销。
 *
 * 这些资源里没有用到 `?attr/`，因此不需要额外携带主题。
 */
@Composable
fun smartisanThemedResources(
    darkOverride: Boolean? = LocalSmartisanDarkOverride.current,
): Resources {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val systemDark = isSystemInDarkTheme()
    val wantDark = darkOverride ?: systemDark
    return remember(context, configuration, wantDark, systemDark) {
        if (wantDark == systemDark) {
            context.resources
        } else {
            val nightMode =
                if (wantDark) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                }
            val target =
                Configuration(configuration).apply {
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
                }
            context.createConfigurationContext(target).resources
        }
    }
}

/** 当前应当使用的夜间模式标志。 */
@Composable
fun smartisanWantsNightResources(
    darkOverride: Boolean? = LocalSmartisanDarkOverride.current,
): Boolean = darkOverride ?: isSystemInDarkTheme()
