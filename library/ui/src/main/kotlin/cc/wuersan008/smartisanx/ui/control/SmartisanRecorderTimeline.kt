package cc.wuersan008.smartisanx.ui.control

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.R
import kotlin.math.ceil
import kotlin.math.floor

/** 录音机原版播放刻度，秒标记间距为 50dp，与同一时间的播放波形对齐。 */
@Composable
fun SmartisanRecorderTimeline(
    positionMillis: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSmartisanColors.current
    val textColor = if (colors.isLight) Color(0x99000000) else colors.textTertiary
    val artwork = rememberSmartisanDrawablePainter(R.drawable.recorder_wave_scale)
    val pixels = with(LocalDensity.current) { 10.5.dp.toPx() }
    val paint = remember(pixels, textColor) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor.toArgb()
            textSize = pixels
        }
    }
    Canvas(modifier.fillMaxWidth().height(18.dp).clipToBounds()) {
        val spacing = 50.dp.toPx()
        val center = size.width / 2
        val position = positionMillis.coerceAtLeast(0L) / 1000.0
        val first = floor(position - center / spacing).toLong().coerceAtLeast(0L)
        val last = ceil(position + center / spacing).toLong()
        for (second in first..last) {
            val x = center + ((second - position) * spacing).toFloat()
            val hours = (second / 3600).toString().padStart(2, '0')
            val minutes = (second / 60 % 60).toString().padStart(2, '0')
            val seconds = (second % 60).toString().padStart(2, '0')
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText("$hours:$minutes:$seconds", x + 5.dp.toPx(), pixels, paint)
            }
            translate(left = x) {
                with(artwork) { draw(intrinsicSize) }
            }
        }
    }
}
