/**
 * 基础控件：锤子风格按钮。
 *
 * 合并了两处重复实现：
 * - 锤子音乐（Compose）`ui/components/SmartisanModal.kt` 的 `SmartisanDialogButton`：
 *   `shrink_long_btn_red_selector` 红色收缩按钮，高 48dp、加粗文字，
 *   按下时按钮整体收缩、投影消失（`shadow_button_shrink_shadow_selector` 的按压态没有投影）；
 * - 锤子天气（Compose）`ui/components/WeatherComponents.kt` 的 `WeatherButton`：
 *   48dp 高、左右 12dp 内边距、加粗文字、按下切换按压态颜色。
 *
 * 三种样式都直接用原版 nine-patch selector 当底图，常态 / 按下 / 禁用三态由 `enabled`、
 * `pressed` 交给 drawable 自动切换，不再用 `accentPressed` 这类手绘换色：
 * - [SmartisanButtonStyle.Accent]：`shrink_long_btn_red_selector` + 配套的
 *   `shadow_button_shrink_shadow_selector`，这是**长按钮**（高 48dp），只有它才配红色底图；
 * - [SmartisanButtonStyle.Neutral]：`revone_dialog_button_bg_selector`；
 * - [SmartisanButtonStyle.Text]：不加底图，文字取 `link` / `linkPressed`。
 *
 * 圆角、按下收缩与投影全部由底图负责，组件**不再叠加任何自绘圆角**，
 * 也不会用 `shapes.extraSmall` 这类主题圆角去盖原版 nine-patch 的圆角。
 */
package cc.wuersan008.smartisanx.ui.control

import android.graphics.Rect
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 按钮的三种样式。 */
enum class SmartisanButtonStyle {
    /** 锤子红实心按钮，对应原版 `shrink_long_btn_red_selector`。 */
    Accent,

    /** 中性按钮，对应原版 `revone_dialog_button_bg_selector`，用于次要操作。 */
    Neutral,

    /** 纯文字按钮，用于弹窗、设置行里的轻量操作。 */
    Text,
}

/** 按钮左右内边距，取自锤子天气 `WeatherButton`。 */
private val ButtonHorizontalPadding = 12.dp

/**
 * 原版按钮的「底图 + 投影」两层背景。
 *
 * 对应锤子音乐 `ui/components/ShadowDrawable.kt`：投影 nine-patch 自带内边距，
 * 原版把投影的绘制范围按内边距向四周外扩（溢出控件本身的范围，不影响布局），
 * 底图再覆盖在控件自身的范围上。
 *
 * 两层都会接收 [enabled] / [pressed]，所以原版 selector 的常态 / 按下态 / 禁用态
 * 会同时作用于底图与投影（按下态的原版投影比常态更收敛）。
 *
 * @param backgroundRes 底图 selector，例如 `shrink_long_btn_red_selector`。
 * @param shadowRes 投影 selector，例如 `shadow_button_shrink_shadow_selector`。
 */
@Composable
internal fun Modifier.smartisanShadowedDrawableBackground(
    @DrawableRes backgroundRes: Int,
    @DrawableRes shadowRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
): Modifier {
    val context = LocalContext.current
    val shadowPainter =
        rememberSmartisanDrawablePainter(shadowRes, enabled = enabled, pressed = pressed)
    val backgroundPainter =
        rememberSmartisanDrawablePainter(backgroundRes, enabled = enabled, pressed = pressed)
    // 投影的厚度就是 nine-patch 的内边距，直接读 drawable 的真实像素值。
    val insets = remember(context, shadowRes) {
        Rect().also { ContextCompat.getDrawable(context, shadowRes)?.getPadding(it) }
    }
    return drawBehind {
        translate(-insets.left.toFloat(), -insets.top.toFloat()) {
            with(shadowPainter) {
                draw(
                    Size(
                        width = size.width + insets.left + insets.right,
                        height = size.height + insets.top + insets.bottom,
                    )
                )
            }
        }
        with(backgroundPainter) { draw(size) }
    }
}

/**
 * 解析原版按钮的「底图 + 投影」背景。
 *
 * 底图为 null 时返回 null，调用方回退到主题色；只给底图时会自动补上配套的投影
 * （`shrink_long_btn_red_selector` → `shadow_button_shrink_shadow_selector`，
 * `smartisan_menu_confirm_background` → `smartisan_menu_confirm_shadow`）。
 *
 * @param showShadow 是否绘制投影，设为 false 时只画底图。
 */
@Composable
internal fun Modifier.smartisanDrawableButtonBackground(
    @DrawableRes backgroundRes: Int?,
    @DrawableRes shadowRes: Int? = null,
    showShadow: Boolean = true,
    enabled: Boolean = true,
    pressed: Boolean = false,
): Modifier {
    if (backgroundRes == null) return this
    val resolvedShadowRes =
        if (showShadow) shadowRes ?: smartisanPairedShadowRes(backgroundRes) else null
    return if (resolvedShadowRes != null) {
        smartisanShadowedDrawableBackground(
            backgroundRes = backgroundRes,
            shadowRes = resolvedShadowRes,
            enabled = enabled,
            pressed = pressed,
        )
    } else {
        smartisanDrawableBackground(backgroundRes, enabled = enabled, pressed = pressed)
    }
}

/** 原版成对的「底图 → 投影」资源；没有配套投影时返回 null。 */
private fun smartisanPairedShadowRes(@DrawableRes backgroundRes: Int): Int? =
    when (backgroundRes) {
        SmartisanDrawables.DialogButtonAccent -> SmartisanDrawables.DialogButtonAccentShadow
        SmartisanDrawables.DialogConfirmBackground -> SmartisanDrawables.DialogConfirmShadow
        else -> null
    }

/** 加载指示器直径。 */
private val ButtonSpinnerSize = 16.dp

/** 加载指示器线宽。 */
private val ButtonSpinnerStroke = 2.dp

/** 加载指示器一圈的时长。 */
private const val ButtonSpinnerDurationMillis = 900

/**
 * 自绘加载指示器（不使用 Material 的进度条）。
 *
 * 原版按钮没有加载态，这里沿用锤子风格：一段 270° 的圆弧匀速旋转。
 */
@Composable
private fun SmartisanButtonSpinner(
    color: Color,
    diameter: Dp = ButtonSpinnerSize,
    strokeWidth: Dp = ButtonSpinnerStroke,
) {
    val transition = rememberInfiniteTransition(label = "smartisan button loading")
    val angle by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(ButtonSpinnerDurationMillis, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "smartisan button loading angle",
        )
    Canvas(modifier = Modifier.size(diameter)) {
        val stroke = strokeWidth.toPx()
        val radius = (size.minDimension - stroke) / 2f
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

/**
 * 按钮的公共实现：底图、文字色与加载态都在这里统一处理。
 *
 * 底图全部来自原版 selector，组件**不再叠加任何自绘圆角**：
 * 圆角、按下收缩、投影都由原版 nine-patch / selector 自己完成。
 *
 * @param contentColorOverride 仅文字按钮使用；为 `Color.Unspecified` 时按样式取默认色。
 * @param backgroundRes 原版底图；为 null 时强调色按钮取 `shrink_long_btn_red_selector`，
 *   中性按钮取 `revone_dialog_button_bg_selector`，文字按钮不加底图。
 * @param shadowRes 原版投影；为 null 时按底图自动配对。
 * @param showShadow 是否绘制原版投影。
 */
@Composable
private fun SmartisanButtonSurface(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    style: SmartisanButtonStyle,
    contentColorOverride: Color,
    @DrawableRes backgroundRes: Int?,
    @DrawableRes shadowRes: Int?,
    showShadow: Boolean,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val active = enabled && !loading
    val pressActive = pressed && active
    // 底图全部来自原版 selector：强调色是红色长按钮（shrink_long_btn_red_selector +
    // shadow_button_shrink_shadow_selector），中性是原版弹窗中性按钮
    // （revone_dialog_button_bg_selector），文字按钮不加底图。
    // 圆角、按下收缩与投影都由底图自己完成，这里不再叠加任何自绘圆角。
    val resolvedBackgroundRes =
        backgroundRes
            ?: when (style) {
                SmartisanButtonStyle.Accent -> SmartisanDrawables.DialogButtonAccent
                SmartisanButtonStyle.Neutral -> SmartisanDrawables.DialogButtonNeutral
                SmartisanButtonStyle.Text -> null
            }
    val backgroundModifier =
        Modifier.smartisanDrawableButtonBackground(
            backgroundRes = resolvedBackgroundRes,
            shadowRes = shadowRes,
            showShadow = showShadow,
            enabled = enabled,
            pressed = pressed,
        )
    val contentColor =
        when {
            contentColorOverride != Color.Unspecified ->
                if (enabled) contentColorOverride else colors.textDisabled

            style == SmartisanButtonStyle.Accent -> colors.onAccent

            style == SmartisanButtonStyle.Neutral ->
                if (enabled) colors.textPrimary else colors.textDisabled

            !enabled -> colors.textDisabled
            pressActive -> colors.linkPressed
            else -> colors.link
        }
    val textStyle =
        if (style == SmartisanButtonStyle.Accent) typography.dialogButton else typography.button
    Box(
        modifier =
            modifier
                // 原版长按钮固定 48dp 高，底图按 48dp 的 nine-patch 内边距拉伸。
                .heightIn(min = SmartisanDimens.DialogButtonHeight)
                .then(backgroundModifier)
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = active,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = ButtonHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (loading) {
                SmartisanButtonSpinner(color = contentColor)
            }
            SmartisanText(
                text = text,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/**
 * 锤子风格按钮。
 *
 * ```kotlin
 * SmartisanButton("确定", onClick = { /* ... */ })
 * SmartisanButton("取消", onClick = { /* ... */ }, style = SmartisanButtonStyle.Neutral)
 * ```
 *
 * @param loading 加载中：显示自绘旋转指示器，并暂时屏蔽点击。
 * @param backgroundRes 原版底图；为 null 时强调色样式使用 `shrink_long_btn_red_selector`，
 *   中性样式使用 `revone_dialog_button_bg_selector`，文字样式不加底图。
 * @param shadowRes 原版投影；为 null 时按底图自动配对。
 * @param showShadow 是否绘制原版投影。
 */
@Composable
fun SmartisanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: SmartisanButtonStyle = SmartisanButtonStyle.Accent,
    loading: Boolean = false,
    @DrawableRes backgroundRes: Int? = null,
    @DrawableRes shadowRes: Int? = null,
    showShadow: Boolean = true,
) {
    SmartisanButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        style = style,
        contentColorOverride = Color.Unspecified,
        backgroundRes = backgroundRes,
        shadowRes = shadowRes,
        showShadow = showShadow,
    )
}

/**
 * 纯文字按钮。
 *
 * @param color 文字颜色，默认取主题的 `link`；禁用时统一为 `textDisabled`。
 */
@Composable
fun SmartisanTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Color.Unspecified,
) {
    SmartisanButtonSurface(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = false,
        style = SmartisanButtonStyle.Text,
        contentColorOverride = color,
        backgroundRes = null,
        shadowRes = null,
        showShadow = false,
    )
}

