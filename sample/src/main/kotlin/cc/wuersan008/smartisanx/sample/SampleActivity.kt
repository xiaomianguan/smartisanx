package cc.wuersan008.smartisanx.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

/** 示例应用入口：单 Activity + Compose 内部路由。 */
class SampleActivity : ComponentActivity() {
    /**
     * 示例应用固定使用简体中文。
     *
     * 组件库自带 80 多种语言的文案，会跟随系统语言；
     * 这里把示例应用自己的资源上下文锁到 zh-CN，
     * 这样无论设备是什么语言，示例都显示简体中文。
     * 你自己的应用不需要这样做 —— 不覆盖就会自动跟随系统。
     */
    override fun attachBaseContext(newBase: android.content.Context) {
        val locale = java.util.Locale.SIMPLIFIED_CHINESE
        java.util.Locale.setDefault(locale)
        val configuration = android.content.res.Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 内容延伸到系统栏，具体 Insets 由各组件自行处理。
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { SampleApp() }
    }
}
