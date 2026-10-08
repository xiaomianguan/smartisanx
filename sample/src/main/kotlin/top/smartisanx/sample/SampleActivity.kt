package top.smartisanx.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

/** 示例应用入口：单 Activity + Compose 内部路由。 */
class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 内容延伸到系统栏，具体 Insets 由各组件自行处理。
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { SampleApp() }
    }
}
