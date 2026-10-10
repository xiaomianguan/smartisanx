package cc.wuersan008.smartisanx.ui.clock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.ui.asset.SmartisanFlipClockDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** 翻页时长（原版 `FlipNumber.ANIMATION_DURATION = 1000`）。 */
const val SmartisanFlipDurationMillis: Int = 1000

/** 原版 `FlipNumber` 把数值 clamp 到 0…60（60 是分钟十位能取到的最大值）。 */
private const val FlipMaxValue = 60

/** 单个象限素材的宽高比（原版 454×468px）。 */
private const val FlipHalfAspect = 468f / 454f

/** 停止时上半覆盖层的 alpha（原版 `cover_top` 的 `setImageAlpha(229)`）。 */
private const val CoverTopAlpha = 229f / 255f

/** 停止时下半覆盖层的 alpha（原版 `cover_bottom` 的 `setImageAlpha(127)`）。 */
private const val CoverBottomAlpha = 127f / 255f

/** 铰链素材相对一个象限的宽高（原版 `flip_axle` 24×120px vs 454×468px）。 */
private const val FlipAxleWidthRatio = 24f / 454f
private const val FlipAxleHeightRatio = 120f / 468f

/** 时卡与分卡的间距（原版 `WirelessChargingTime.onFinishInflate` 里写死的 `leftMargin = 18`px）。 */
private const val FlipCardGapRatio = 18f / 454f

/**
 * 卡片圆角相对一个半格宽度的比例（原版 `flip_*` 素材左上角：半径约 27px / 454px）。
 *
 * 每半格的素材在折线一侧留了 **6px 透明边**，两半叠起来就是 12px，原版这里露出的是
 * 充电画布的纯黑（`wireless_charging_display.xml` 的 `#ff000000`），所以底衬要补上，
 * 否则会透出宿主页面的底色（真机上折线处是一条白缝）。
 */
private const val FlipCornerRadiusRatio = 27f / 454f

/**
 * 翻转用的相机距离：原版 `UpperFlipAnimation` / `LowerFlipAnimation` 里是
 * `Camera.setLocation(0, 0, -40)`，即框架默认值（-8）的 5 倍；Compose 的 `cameraDistance`
 * 默认是 8dp，所以这里取 40dp，保持同一个「5 倍贴近」的透视强度。
 */
private val FlipCameraDistance = 40.dp

/** 12 小时制时 AM / PM 的颜色（原版 `flip_charging_clock.xml` 写死的 `#ff7f00`）。 */
val SmartisanFlipAmPmColor: Color = Color(0xFFFF7F00)

/** AM / PM 与时钟的间距（原版 `wireless_charging_battery` 的 `layout_marginLeft="20dp"`）。 */
private val FlipAmPmGap = 20.dp

/** AM / PM 底边比时钟底边高出的距离（原版 `am_pm_text` 的 `layout_marginBottom="2dp"`）。 */
private val FlipAmPmBottomPadding = 2.dp

/** AM / PM 字号（原版 `@dimen/wireless_charging_text_size`，`values-xxhdpi` 里是 16dp）。 */
private val FlipAmPmTextSize = 16.sp

/**
 * 原版 `FlipDownInterpolator(1.0f, 0.75f)` —— 就是标准的 ease-out-elastic（amplitude 1、period 0.75）。
 *
 * 曲线在 1 附近来回摆几下：翻页卡片先快速落下，再轻微回弹落位，就是靠这段尾巴。
 */
val SmartisanFlipEasing: Easing =
    Easing { input ->
        when {
            input <= 0f -> 0f
            input >= 1f -> 1f
            else -> {
                // a = 1 → s = period / (2π) · asin(1 / a) = period / 4
                val period = 0.75f
                val s = period / 4f
                1f + 2f.pow(-10f * input) * sin((input - s) * 2f * PI.toFloat() / period)
            }
        }
    }

/**
 * 翻页卡片（原版 `com.smartisanos.keyguard.widgets.flipnumber.FlipNumber`）。
 *
 * 一张卡片里放**两位数**（十位 + 个位），上下两半各 468px、中间是折线，两侧有铰链；
 * 数字变化时上半从折线处向下翻走、下半同时翻上来露出新数字，翻到位后用 elastic 曲线回弹收尾。
 *
 * 素材全部来自原版（[SmartisanFlipClockDrawables]），所以上半灰字、下半白字、卡片暗色渐变
 * 与铰链都是原样；翻页过程中三层覆盖层也按原版 `FlipNumber.startFlipAnimation` 的时序淡入淡出：
 *
 * | 层 | 原版 | 行为 |
 * | --- | --- | --- |
 * | 上半覆盖 | `UpperFlipAlphaAnimation(1 → 0)` | 翻页时淡出 |
 * | 下半覆盖 | `LowerAlphaAnimation(1 → 0)` | 翻页时淡出（基础 alpha 127） |
 * | 翻起的那半 | `LowerFlipAlphaAnimation(1 → 0.5)` | 翻页时压暗到一半 |
 * | 投影 | `LowerShadowAlphaAnimation(0 → 1)` | 翻页时浮现 |
 *
 * ```kotlin
 * SmartisanFlipCard(value = 7)                              // 一个「07」卡片
 * SmartisanFlipCard(value = 42, digitWidth = 76.dp)         // 等比缩小
 * ```
 *
 * @param value 0…60（原版 clamp 到 60，超过按 60 处理）
 * @param digitWidth 一位数的宽度；默认取原版素材固有尺寸（454px）。高度按素材比例
 *   468/454 自动推算，卡片实际尺寸是宽 ×2、高 ×2。
 * @param animate 数字变化时是否翻页（false 直接换图，对应原版 `setNumber`）
 * @param durationMillis 翻页时长，默认原版 1000ms
 * @param backingColor 卡片底衬颜色，默认纯黑（原版充电画布的颜色）
 */
@Composable
fun SmartisanFlipCard(
    value: Int,
    modifier: Modifier = Modifier,
    digitWidth: Dp? = null,
    animate: Boolean = true,
    durationMillis: Int = SmartisanFlipDurationMillis,
    backingColor: Color = Color.Black,
) {
    val safe = value.coerceIn(0, FlipMaxValue)
    val density = LocalDensity.current
    val reference = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.digit(0, right = true, top = true))
    // 尺寸吸附到整像素：原版是直接按位图原生 px 摆的，而上下两半要严丝合缝地拼在一起，
    // 半高不是整数像素时中间会露出 1px 背景缝（真机上能看到折线处一条白线，原版没有）。
    val digitW = with(density) { (digitWidth ?: reference.intrinsicSize.width.dp).toPx().roundToInt().toDp() }
    val digitH = with(density) { (digitW * FlipHalfAspect).toPx().roundToInt().toDp() }
    val axle = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.Axle)
    val axleW = with(density) { (digitW * FlipAxleWidthRatio).toPx().roundToInt().toDp() }
    val axleH = with(density) { (digitH * FlipAxleHeightRatio).toPx().roundToInt().toDp() }
    val cameraDistance = with(density) { FlipCameraDistance.toPx() }

    var shown by remember { mutableIntStateOf(safe) }
    var previous by remember { mutableIntStateOf(safe) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(safe) {
        if (safe != shown) {
            val from = shown
            shown = safe
            previous = from
            if (animate) {
                progress.snapTo(0f)
                progress.animateTo(1f, tween(durationMillis, easing = SmartisanFlipEasing))
            } else {
                progress.snapTo(1f)
            }
        }
    }

    // 原版是拿「插值后的时间」直接当旋转角度用的：
    // 上半 0 → -180（只在 ≤ 0.5 时可见，之后 alpha 归零），下半 180 → 0（只在 > 0.5 时可见）。
    val t = progress.value
    val flipping = t < 1f

    Box(modifier.size(digitW * 2, digitH * 2)) {
        // 底衬：原版画布是纯黑，两半之间 12px 透明边露出的就是它（圆角与素材一致）。
        Box(
            Modifier.matchParentSize()
                .clip(RoundedCornerShape(digitW * FlipCornerRadiusRatio))
                .background(backingColor),
        )
        // 静止层·上半：新数字。
        FlipRow(value = shown, top = true, coverAlpha = CoverTopAlpha, digitW = digitW, digitH = digitH)
        // 静止层·下半：翻页过程中还是旧数字，翻完才换成新数字。
        FlipRow(
            value = if (flipping) previous else shown,
            top = false,
            coverAlpha = CoverBottomAlpha * if (flipping) (1f - t) else 1f,
            shadowAlpha = if (flipping) t else 0f,
            digitW = digitW,
            digitH = digitH,
            modifier = Modifier.offset(y = digitH),
        )
        // 铰链：原版左右各一个，贴卡片外侧、竖直居中在折线上。
        Image(
            painter = axle,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterStart).size(axleW, axleH),
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = axle,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterEnd).size(axleW, axleH),
            contentScale = ContentScale.FillBounds,
        )
        // 翻页层·上半（旧数字）：绕折线（上半的下边缘）向下翻。
        if (flipping && t <= 0.5f) {
            FlipRow(
                value = previous,
                top = true,
                coverAlpha = CoverTopAlpha * (1f - t),
                digitW = digitW,
                digitH = digitH,
                modifier =
                    Modifier.graphicsLayer {
                        rotationX = -180f * t
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        this.cameraDistance = cameraDistance
                    },
            )
        }
        // 翻页层·下半（新数字）：绕折线（下半的上边缘）翻上来。
        if (flipping && t > 0.5f) {
            FlipRow(
                value = shown,
                top = false,
                coverAlpha = 1f - 0.5f * t,
                digitW = digitW,
                digitH = digitH,
                modifier =
                    Modifier.offset(y = digitH).graphicsLayer {
                        rotationX = 180f * (1f - t)
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        this.cameraDistance = cameraDistance
                    },
            )
        }
    }
}

/**
 * 翻页时钟（原版锁屏无线充电画布上的时钟：`FlipClock` + `WirelessChargingTime`）。
 *
 * 两张翻页卡片（时、分）并排，中间 18px；12 小时制时右侧还有 AM / PM
 * （原版 `am_pm_text`：与时钟底边对齐、向右 20dp、橙色加粗 16dp）。
 *
 * 原版这张时钟画在**横屏**的充电画布上（`wireless_charging_display.xml` 是 2242×1080px、
 * `rotation="270"`），素材也是按 1080p 固定的 px 尺寸（400dpi 与 xxhdpi 桶里放的是同一张图），
 * 所以竖屏里用请显式传 [digitWidth] 缩放，例如 `digitWidth = 76.dp`。
 *
 * ```kotlin
 * SmartisanFlipClock(hour = 7, minute = 30, use24Hour = false, digitWidth = 76.dp)
 * ```
 *
 * @param hour 0…23（[use24Hour] 为 false 时按 12 小时制取模，0 点显示 12）
 * @param minute 0…59
 * @param use24Hour 是否 24 小时制；false 时显示 AM / PM 且小时按 12 小时制
 * @param digitWidth 一位数的宽度，见 [SmartisanFlipCard]；默认原版素材固有尺寸
 * @param animate 时间变化时是否翻页
 * @param durationMillis 翻页时长，默认原版 1000ms
 * @param amPmColor AM / PM 颜色，默认原版 `#ff7f00`
 * @param backingColor 卡片底衬颜色，默认纯黑（原版充电画布的颜色）
 */
@Composable
fun SmartisanFlipClock(
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    use24Hour: Boolean = true,
    digitWidth: Dp? = null,
    animate: Boolean = true,
    durationMillis: Int = SmartisanFlipDurationMillis,
    amPmColor: Color = SmartisanFlipAmPmColor,
    backingColor: Color = Color.Black,
) {
    val reference = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.digit(0, right = true, top = true))
    // 同 [SmartisanFlipCard]：整像素吸附，卡片与间距都不落在半个像素上。
    val digitW =
        with(LocalDensity.current) {
            (digitWidth ?: reference.intrinsicSize.width.dp).toPx().roundToInt().toDp()
        }
    val hourOfDay = ((hour % 24) + 24) % 24
    val isAm = hourOfDay < 12
    val hour12 = hourOfDay % 12
    val cardHour = if (use24Hour) hourOfDay else if (hour12 == 0) 12 else hour12

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        SmartisanFlipCard(
            value = cardHour,
            digitWidth = digitW,
            animate = animate,
            durationMillis = durationMillis,
            backingColor = backingColor,
        )
        Spacer(Modifier.width(digitW * FlipCardGapRatio))
        SmartisanFlipCard(
            value = minute.coerceIn(0, FlipMaxValue),
            digitWidth = digitW,
            animate = animate,
            durationMillis = durationMillis,
            backingColor = backingColor,
        )
        if (!use24Hour) {
            Spacer(Modifier.width(FlipAmPmGap))
            SmartisanText(
                text = if (isAm) "AM" else "PM",
                modifier = Modifier.padding(bottom = FlipAmPmBottomPadding),
                color = amPmColor,
                fontWeight = FontWeight.Bold,
                fontSize = FlipAmPmTextSize,
            )
        }
    }
}

/**
 * 卡片的一半：左右两个象限（十位 / 个位）加覆盖层。
 *
 * [shadowAlpha] > 0 时再叠一层 `cover_bottom_shadow`：原版只在下半静止层上用，
 * 用来接住从上面翻下来的那半卡片的投影。
 */
@Composable
private fun FlipRow(
    value: Int,
    top: Boolean,
    coverAlpha: Float,
    digitW: Dp,
    digitH: Dp,
    modifier: Modifier = Modifier,
    shadowAlpha: Float = 0f,
) {
    val tens = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.digit(value / 10, right = false, top = top))
    val units = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.digit(value % 10, right = true, top = top))
    val cover =
        rememberSmartisanDrawablePainter(
            if (top) SmartisanFlipClockDrawables.CoverTop else SmartisanFlipClockDrawables.CoverBottom,
        )
    val shadow = rememberSmartisanDrawablePainter(SmartisanFlipClockDrawables.CoverBottomShadow)

    Box(modifier.size(digitW * 2, digitH)) {
        Image(
            painter = tens,
            contentDescription = null,
            modifier = Modifier.size(digitW, digitH),
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = units,
            contentDescription = null,
            modifier = Modifier.offset(x = digitW).size(digitW, digitH),
            contentScale = ContentScale.FillBounds,
        )
        Image(
            painter = cover,
            contentDescription = null,
            modifier = Modifier.size(digitW * 2, digitH),
            contentScale = ContentScale.FillBounds,
            alpha = coverAlpha,
        )
        if (shadowAlpha > 0f) {
            Image(
                painter = shadow,
                contentDescription = null,
                modifier = Modifier.size(digitW * 2, digitH),
                contentScale = ContentScale.FillBounds,
                alpha = shadowAlpha,
            )
        }
    }
}
