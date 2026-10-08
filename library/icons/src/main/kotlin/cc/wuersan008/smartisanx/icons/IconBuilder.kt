package cc.wuersan008.smartisanx.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * 图标构造助手。
 *
 * smartisanx 不打包任何位图资源，全部图标用 24×24 的矢量路径描述，
 * 与锤子原版「细线 + 圆角端点」的图标风格一致。
 *
 * 图标本身使用纯黑填充/描边，颜色由 `SmartisanIcon` 的 `tint` 决定。
 */
internal fun smartisanIcon(
    name: String,
    pathData: String,
    filled: Boolean = false,
    strokeWidth: Float = 1.8f,
): ImageVector =
    smartisanIcon(name, listOf(pathData), filled, strokeWidth)

internal fun smartisanIcon(
    name: String,
    paths: List<String>,
    filled: Boolean = false,
    strokeWidth: Float = 1.8f,
): ImageVector {
    val builder =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
    paths.forEach { data ->
        builder.addPath(
            pathData = PathParser().parsePathString(data).toNodes(),
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = if (filled) null else SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}

/** 圆形路径，中心 (cx, cy)，半径 r。 */
internal fun circlePath(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} $cy " +
        "A$r $r 0 1 0 ${cx + r} $cy " +
        "A$r $r 0 1 0 ${cx - r} $cy Z"

/** 实心圆点路径。 */
internal fun dotPath(cx: Float, cy: Float, r: Float): String = circlePath(cx, cy, r)
