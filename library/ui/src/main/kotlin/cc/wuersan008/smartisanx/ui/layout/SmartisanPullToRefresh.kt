package cc.wuersan008.smartisanx.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgress

/** 滚动内容到达顶部后继续下拉并松手时刷新，由调用方管理刷新状态。 */
@Composable
fun SmartisanPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorTopPadding: Dp = 8.dp,
    threshold: Dp = 64.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx().coerceAtLeast(1f) }
    val currentRefresh by rememberUpdatedState(onRefresh)
    val refreshing by rememberUpdatedState(isRefreshing)
    var drag by remember(isRefreshing) { mutableFloatStateOf(0f) }
    val connection = remember(thresholdPx, isRefreshing) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (refreshing || source != NestedScrollSource.UserInput || available.y >= 0f || drag == 0f) {
                    return Offset.Zero
                }
                val consumed = available.y.coerceAtLeast(-drag)
                drag += consumed
                return Offset(0f, consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (refreshing || source != NestedScrollSource.UserInput || available.y <= 0f) {
                    return Offset.Zero
                }
                drag = (drag + available.y).coerceAtMost(2 * thresholdPx)
                return Offset(0f, available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val wasPulling = drag > 0f
                val shouldRefresh = !refreshing && drag >= thresholdPx
                drag = 0f
                if (shouldRefresh) currentRefresh()
                return if (wasPulling && available.y > 0f) Velocity(0f, available.y) else Velocity.Zero
            }
        }
    }
    Box(modifier.nestedScroll(connection)) {
        content()
        if (drag > 0f || isRefreshing) {
            SmartisanCircleProgress(
                progress = if (isRefreshing) null else (drag / thresholdPx).coerceAtMost(1f),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = indicatorTopPadding),
                size = 28.dp,
                arcColor = LocalSmartisanColors.current.accent,
            )
        }
    }
}
