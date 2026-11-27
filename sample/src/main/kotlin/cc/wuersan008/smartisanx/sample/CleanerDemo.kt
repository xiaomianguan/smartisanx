package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.control.SmartisanCleanerDial
import cc.wuersan008.smartisanx.ui.control.SmartisanCleanerProgress
import cc.wuersan008.smartisanx.ui.control.SmartisanSmoothSeekBar
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap

@Composable
internal fun CleanerDemo(onBack: () -> Unit) {
    var scanning by rememberSaveable { mutableStateOf(false) }
    var progress by rememberSaveable { mutableFloatStateOf(0f) }
    var showProgress by rememberSaveable { mutableStateOf(false) }
    SamplePageScaffold(title = "垃圾清理", onBack = onBack) {
        SmartisanListVerticalGap(height = 9.dp)
        Box(
            Modifier.fillMaxWidth().height(183.7.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (showProgress) {
                SmartisanCleanerProgress(progress) { SmartisanText("${(progress * 100).toInt()}%") }
            } else {
                SmartisanCleanerDial(scanning)
            }
        }
        SmartisanGroup {
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Top,
                title = if (scanning) "停止扫描动画" else "播放扫描动画",
                onClick = {
                    scanning = !scanning
                    showProgress = false
                },
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Bottom,
                title = "显示清理进度",
                selected = showProgress,
                onClick = {
                    scanning = false
                    showProgress = true
                },
            )
        }
        if (showProgress) {
            SmartisanSmoothSeekBar(value = progress, onValueChange = { progress = it }, contentDescription = "清理进度")
        }
    }
}
