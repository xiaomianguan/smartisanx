package cc.wuersan008.smartisanx.ui.control

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon

/**
 * 清理应用的 RayProgressBar。以原版的左右半盘和旋转遮罩显示 0–1 进度。
 * 进度由调用方提供，动画沿用原版每次更新的 600ms 减速曲线。
 */
@Composable
fun SmartisanCleanerProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val boundedProgress = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
    val angle by animateFloatAsState(
        targetValue = -360f * boundedProgress,
        animationSpec = tween(600, easing = CleanerDecelerate),
        label = "cleaner progress mask",
    )
    Box(
        modifier
            .size(458.dp / 3)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(boundedProgress, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        // 过半后把左表盘提到最上层。旋出的遮罩被对应半盘盖住。
        if (angle > -180f) {
            SmartisanIcon(res = R.drawable.cleaner_left_dial, contentDescription = null, modifier = Modifier.matchParentSize())
            CleanerMask(angle)
            SmartisanIcon(res = R.drawable.cleaner_right_dial, contentDescription = null, modifier = Modifier.matchParentSize())
            CleanerMask(-180f)
        } else {
            CleanerMask(0f)
            SmartisanIcon(res = R.drawable.cleaner_right_dial, contentDescription = null, modifier = Modifier.matchParentSize())
            CleanerMask(angle)
            SmartisanIcon(res = R.drawable.cleaner_left_dial, contentDescription = null, modifier = Modifier.matchParentSize())
        }
        content()
    }
}

@Composable
private fun BoxScope.CleanerMask(angle: Float) {
    SmartisanIcon(
        res = R.drawable.cleaner_half_mask,
        contentDescription = null,
        modifier = Modifier.matchParentSize().graphicsLayer { rotationZ = angle },
    )
}
