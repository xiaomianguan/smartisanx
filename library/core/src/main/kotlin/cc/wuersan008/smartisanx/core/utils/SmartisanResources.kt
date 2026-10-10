package cc.wuersan008.smartisanx.core.utils

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.content.ContextCompat
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
    // 用 LocalResources 而不是 context.resources：前者随 Configuration 变化失效，
    // 后者是「读一次就固定」的，配置改变后可能拿到旧值。
    val systemResources = LocalResources.current
    val systemDark = isSystemInDarkTheme()
    val wantDark = darkOverride ?: systemDark
    return remember(context, configuration, systemResources, wantDark, systemDark) {
        if (wantDark == systemDark) {
            systemResources
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

/**
 * 取原版「颜色状态表」（`res/color` 下的 `_colorlist.xml`，例如 `preview_title_text_colorlist`）
 * 里当前状态的颜色。
 *
 * 为什么不用 `colorResource`：它只能拿到默认色，遇到状态表会取到第一条能匹配的 item，
 * 于是常态可能取成禁用色（原版这类状态表的第一条往往是 `state_enabled="false"`）。
 *
 * 状态集必须**写全**（同 [smartisanDrawableState]）：像 `state_enabled="false"` 这种否定项，
 * 在「空状态集」下同样会被判定为命中，所以只传 `state_pressed` 之类的部分状态是不够的。
 *
 * @param colorRes 颜色状态表资源。
 * @param enabled 当前是否可用。
 * @param pressed 当前是否按下。
 * @param selected 当前是否 selected。
 * @param checked 当前是否 checked。
 * @param activated 当前是否 activated。
 */
@Composable
fun rememberSmartisanStateListColor(
    @ColorRes colorRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
    selected: Boolean = false,
    checked: Boolean = false,
    activated: Boolean = false,
): Color {
    val context = LocalContext.current
    val stateList = remember(context, colorRes) { ContextCompat.getColorStateList(context, colorRes) }
    return remember(context, colorRes, stateList, enabled, pressed, selected, checked, activated) {
        val state =
            smartisanDrawableState(
                enabled = enabled,
                pressed = pressed,
                selected = selected,
                checked = checked,
                activated = activated,
            )
        val argb =
            stateList?.getColorForState(state, stateList.defaultColor)
                ?: ContextCompat.getColor(context, colorRes)
        Color(argb)
    }
}

