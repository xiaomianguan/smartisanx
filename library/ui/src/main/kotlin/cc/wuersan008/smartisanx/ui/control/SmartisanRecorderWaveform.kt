package cc.wuersan008.smartisanx.ui.control

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.R
import kotlin.math.ceil
import kotlin.math.floor

/**
 * 录音机的播放波形。每个无符号振幅样本代表 20ms，绘制为每 1dp 一根居中的竖线。
 * [positionMillis] 始终对齐到固定的原版播放指示线。由调用方负责读取和生成样本。
 */
@Composable
fun SmartisanRecorderWaveform(
    samples: ByteArray,
    positionMillis: Long,
    modifier: Modifier = Modifier,
    waveformColor: Color = if (LocalSmartisanColors.current.isLight) Color(0xFFAFB0B0) else LocalSmartisanColors.current.textTertiary,
) {
    val marker = rememberSmartisanDrawablePainter(R.drawable.recorder_wave_playhead)
    Canvas(modifier.fillMaxWidth().height(120.dp).clipToBounds()) {
        val step = 1.dp.toPx()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val centerFrame = positionMillis.coerceAtLeast(0L) / 20.0
        val framesToEdge = centerX / step
        val first = floor(centerFrame - framesToEdge).toInt().coerceAtLeast(0)
        val last = ceil(centerFrame + framesToEdge).toInt().coerceAtMost(samples.lastIndex)
        for (index in first..last) {
            val amplitude = (samples[index].toInt() and 255) / 255f
            val halfHeight = centerY * amplitude
            val x = centerX + ((index - centerFrame) * step).toFloat()
            drawLine(waveformColor, Offset(x, centerY - halfHeight), Offset(x, centerY + halfHeight), strokeWidth = step)
        }
        translate(left = centerX - marker.intrinsicSize.width / 2f) {
            with(marker) { draw(Size(intrinsicSize.width, size.height)) }
        }
    }
}
