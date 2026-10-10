package cc.wuersan008.smartisanx.ui.overlay

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import android.content.res.Configuration

/** 竖屏时提示面板的尺寸（原版 `values-port-xxhdpi/dimens.xml` 的 `twist_guide_width/height`）。 */
private val GuideWidthPortrait = 303.dp
private val GuideHeightPortrait = 453.dp

/** 横屏时提示面板的尺寸（原版 `values-land-xxhdpi/dimens.xml`）。 */
private val GuideWidthLandscape = 453.dp
private val GuideHeightLandscape = 297.dp

/** 面板四边的外边距，横竖屏都是 16dp。 */
private val GuideBodyMargin = 16.dp

/** 竖屏面板的上下内边距。 */
private val GuideBodyPaddingTop = 23.dp
private val GuideBodyPaddingBottom = 12.dp

/** 横屏面板的左右内边距。 */
private val GuideBodyPaddingHorizontal = 16.dp

/** 竖屏：标题与正文的间距；横屏：7dp。 */
private val GuideTitleMarginBottomPortrait = 8.dp
private val GuideTitleMarginBottomLandscape = 7.dp

/** 竖屏：正文与插画之间的空高；横屏同理是空宽（`twist_guide_space_width`）。 */
private val GuideSpaceHeight = 38.dp
private val GuideSpaceWidth = 38.dp

/** 竖屏正文宽度（`twist_guide_text_body_width`）。 */
private val GuideBodyTextWidth = 190.dp

/** 横屏正文区上下留白（`twist_guide_text_margtinTop/Bottom`，原版就这么拼写）。 */
private val GuideTextMarginTopLandscape = 26.dp
private val GuideTextMarginBottomLandscape = 24.dp

/** 标题 16sp、正文 13sp、颜色都是原版布局里写死的 `#484848`。 */
private val GuideTextColor = Color(0xFF484848)

/** 原版遮罩色 `-1744830464` = `0x98000000`（约 60% 黑）。 */
private val GuideDimColor = Color(0x98_000000)

/** 原版 `anim/shrink_to_right_top`：300ms，以右上角为锚点缩到 0 并淡出。 */
private const val BodyExitDuration = 300

/** 原版 `anim/rotate`：400ms 转 320°，之后再缩没。 */
private const val CloseBtnExitDuration = 400

/** 原版 `anim/fade_out`：400ms 淡出遮罩。 */
private const val DimExitDuration = 400

/**
 * 浮层：手势切换横竖屏的提示（原版 framework `smartisanos.widget.TwistGuideView`）。
 *
 * 原版是个**窗口级**浮层：`WindowManager.addView` 盖一整屏，底下一层 `0x98000000` 遮罩，
 * 中间一块按屏幕方向定尺寸的面板（竖屏 303×453dp、横屏 453×297dp），右上角一个关闭按钮。
 * 本组件保留同样的观感与退出动画，只是用 Compose 叠在调用方页面里：
 *
 * | 原版 | 本组件 |
 * | --- | --- |
 * | `layout-port/twist_guide_view.xml` | 竖屏排版：标题 → 正文（可滚动）→ 38dp 空高 → 插画 |
 * | `layout-land/twist_guide_view.xml` | 横屏排版：插画 → 38dp 空宽 → 标题 + 正文 |
 * | `anim/shrink_to_right_top` | 点关闭时面板以**右上角**为锚点 300ms 缩没并淡出 |
 * | `anim/rotate` | 关闭按钮 400ms 转 320°（随后 300ms 缩没，对应原版 `startOffset = 100`） |
 * | `anim/fade_out` | 遮罩 400ms 淡出，播完才回调 [onDismiss]（原版移除窗口的时机） |
 *
 * 素材取自 framework 资源 `framework-smartisanos-res.apk`：`twist_guide_bg_port` /
 * `twist_guide_bg_land`（9-patch 面板底图）、`twist_guide_diagram_port` / `_land`（插画，
 * **竖屏用的是 land 那张**、横屏用 port，原版布局就是这么摆的）、
 * `twist_guide_close_btn`（selector，注意原版把按下态画成了 `_norm`）。
 *
 * 文案默认取原版中文（`提示` + 那段说明），可以整体替换。
 *
 * @param visible 是否显示。
 * @param onDismiss 关闭回调：等退出动画播完才触发。
 * @param modifier 外部修饰符。
 * @param title 标题文案。
 * @param body 正文文案。
 * @param titleTextSize 标题字号，默认原版 16sp。
 * @param bodyTextSize 正文字号，默认原版 13sp。
 * @param dimColor 遮罩颜色，默认原版 `0x98000000`。
 * @param portraitBackgroundRes 竖屏面板底图；默认原版 `twist_guide_bg_port`。
 * @param landscapeBackgroundRes 横屏面板底图；默认原版 `twist_guide_bg_land`。
 * @param closeIconRes 关闭按钮素材；默认原版 `twist_guide_close_btn`。
 */
@Composable
fun SmartisanTwistGuide(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.smartisan_twist_guide_title),
    body: String = stringResource(R.string.smartisan_twist_guide_body),
    titleTextSize: TextUnit = 16.sp,
    bodyTextSize: TextUnit = 13.sp,
    dimColor: Color = GuideDimColor,
    @DrawableRes portraitBackgroundRes: Int = SmartisanDrawables.TwistGuideBackgroundPortrait,
    @DrawableRes landscapeBackgroundRes: Int = SmartisanDrawables.TwistGuideBackgroundLandscape,
    @DrawableRes closeIconRes: Int = SmartisanDrawables.TwistGuideCloseButton,
) {
    if (!visible) return
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bodyProgress = remember { Animatable(1f) }
    val closeProgress = remember { Animatable(1f) }
    val dimProgress = remember { Animatable(1f) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(closing) {
        if (closing) bodyProgress.animateTo(0f, tween(BodyExitDuration, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(closing) {
        if (closing) closeProgress.animateTo(0f, tween(CloseBtnExitDuration, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(closing) {
        if (closing) {
            dimProgress.animateTo(0f, tween(DimExitDuration, easing = LinearEasing))
            onDismiss()
        }
    }
    val close: () -> Unit = {
        if (!closing) closing = true
    }
    Box(
        modifier = modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { close() } },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer { alpha = dimProgress.value }
                .background(dimColor),
        )
        Box(
            modifier =
                Modifier.width(if (landscape) GuideWidthLandscape else GuideWidthPortrait)
                    .height(if (landscape) GuideHeightLandscape else GuideHeightPortrait)
                    .graphicsLayer {
                        val p = bodyProgress.value
                        // 原版 pivot 在右上角（pivotX = 100% / pivotY = 0）。
                        transformOrigin = TransformOrigin(1f, 0f)
                        alpha = p
                        scaleX = p
                        scaleY = p
                    },
        ) {
            // 面板内再留 16dp：原版底图挂在带 margin 的 body 上，关闭按钮则贴面板自身右上角。
            Box(
                Modifier.fillMaxSize()
                    .padding(GuideBodyMargin)
                    .smartisanDrawableBackground(
                        if (landscape) landscapeBackgroundRes else portraitBackgroundRes,
                    ),
            ) {
                if (landscape) {
                    TwistGuideLandscapeContent(title, body, titleTextSize, bodyTextSize)
                } else {
                    TwistGuidePortraitContent(title, body, titleTextSize, bodyTextSize)
                }
            }
            val closePainter = rememberSmartisanDrawablePainter(closeIconRes)
            val closeInteraction = rememberSmartisanInteractionSource()
            Box(
                modifier =
                    Modifier.align(Alignment.TopEnd)
                        .graphicsLayer {
                            val p = closeProgress.value
                            // 原版 rotate：0 → 320°，同时最后 300ms 才缩没淡出。
                            val shrink = (((1f - p) * CloseBtnExitDuration) / 300f).coerceIn(0f, 1f)
                            rotationZ = (1f - p) * 320f
                            alpha = 1f - shrink
                            scaleX = 1f - shrink
                            scaleY = 1f - shrink
                        }
                        .smartisanClickable(
                            interactionSource = closeInteraction,
                            enabled = !closing,
                        ) {
                            close()
                        },
            ) {
                Image(
                    painter = closePainter,
                    contentDescription = stringResource(R.string.smartisan_cancel),
                )
            }
        }
    }
}


/** 竖屏排版：标题 → 正文（可滚动）→ 38dp 空高 → 插画（原版竖屏用的是 land 那张图）。 */
@Composable
private fun TwistGuidePortraitContent(
    title: String,
    body: String,
    titleTextSize: TextUnit,
    bodyTextSize: TextUnit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize().padding(
                top = GuideBodyPaddingTop,
                bottom = GuideBodyPaddingBottom,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TwistGuideTitle(title, titleTextSize, GuideTitleMarginBottomPortrait)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SmartisanText(
                text = body,
                modifier = Modifier.width(GuideBodyTextWidth),
                style = TextStyle(fontSize = bodyTextSize, lineHeight = bodyTextSize * 1.25f),
                color = GuideTextColor,
            )
        }
        Spacer(Modifier.height(GuideSpaceHeight))
        Image(
            painter = rememberSmartisanDrawablePainter(SmartisanDrawables.TwistGuideDiagramLandscape),
            contentDescription = null,
        )
    }
}

/** 横屏排版：插画（原版横屏用的是 port 那张图）→ 38dp 空宽 → 右侧标题 + 正文。 */
@Composable
private fun TwistGuideLandscapeContent(
    title: String,
    body: String,
    titleTextSize: TextUnit,
    bodyTextSize: TextUnit,
) {
    Row(
        modifier =
            Modifier.fillMaxSize().padding(
                start = GuideBodyPaddingHorizontal,
                end = GuideBodyPaddingHorizontal,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberSmartisanDrawablePainter(SmartisanDrawables.TwistGuideDiagramPortrait),
            contentDescription = null,
        )
        Spacer(Modifier.width(GuideSpaceWidth))
        Column(
            modifier =
                Modifier.weight(1f).heightIn(
                    max = GuideHeightLandscape - GuideTextMarginTopLandscape * 2
                ).padding(
                    top = GuideTextMarginTopLandscape,
                    bottom = GuideTextMarginBottomLandscape,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TwistGuideTitle(title, titleTextSize, GuideTitleMarginBottomLandscape)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                SmartisanText(
                    text = body,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(fontSize = bodyTextSize, lineHeight = bodyTextSize * 1.2f),
                    color = GuideTextColor,
                )
            }
        }
    }
}

/** 标题：16sp、`#484848`、居中。 */
@Composable
private fun TwistGuideTitle(title: String, textSize: TextUnit, marginBottom: Dp) {
    SmartisanText(
        text = title,
        modifier = Modifier.padding(bottom = marginBottom),
        style = TextStyle(fontSize = textSize),
        color = GuideTextColor,
        textAlign = TextAlign.Center,
    )
}

