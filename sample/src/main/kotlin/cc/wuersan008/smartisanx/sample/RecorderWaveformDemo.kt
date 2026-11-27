package cc.wuersan008.smartisanx.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanRecorderWaveform
import cc.wuersan008.smartisanx.ui.control.SmartisanRecorderTimeline
import cc.wuersan008.smartisanx.ui.control.SmartisanSmoothSeekBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import kotlin.math.abs
import kotlin.math.sin

@Composable
internal fun RecorderWaveformDemo(onBack: () -> Unit) {
    var progress by rememberSaveable { mutableFloatStateOf(0.5f) }
    val peaks = remember {
        ByteArray(1100) { index ->
            val sound = abs(sin(index * 0.37) * sin(index * 0.013))
            (18 + sound * 210).toInt().toByte()
        }
    }
    val millis = (progress * (peaks.size - 1) * 20).toLong()
    SamplePageScaffold(title = "录音波形", onBack = onBack) {
        SampleSectionHeader("播放示例")
        SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                SmartisanText("${millis / 1000} 秒", Modifier.padding(start = 16.dp))
                SmartisanRecorderTimeline(millis)
                SmartisanRecorderWaveform(peaks, millis)
                SmartisanSmoothSeekBar(value = progress, onValueChange = { progress = it }, contentDescription = "播放进度")
            }
        }
    }
}
