package cc.wuersan008.smartisanx.ui.input

import android.graphics.drawable.AnimationDrawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClick
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.utils.smartisanThemedResources
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 带「显示 / 隐藏密码」切换的密码输入框。
 *
 * 对应原版 `smartisanos.widget.PasswordEditText`，在锤子日历、邮件、音乐三个 APK 里使用
 * （布局 `pwd_edit_text.xml` / `sos_smartisanos_layout_pwd_edit_text.xml`，样式 `EditorTextStyle`）。
 *
 * 原版把眼睛图标画在输入框右侧：位置是 `宽 - 图标宽 - (mEyePaddingRight - 父容器右边距)`，
 * 垂直居中（`mEyePaddingRight = 48px`，@xxhdpi 约 16dp）。眼睛是资源里的一张一次性帧动画
 * `pwd_eye_open_close_anim`（16 帧 × 16ms），点击时从当前帧朝目标方向逐帧播放，
 * **在动画进行到一半时**才真正切换明文 / 密文（`getAnimationDuration() / 2`），
 * 并保持原来的光标位置。
 *
 * 这里完整保留这套节奏：
 * - 帧动画用 `Animatable` 驱动，帧时长取自原版 `AnimationDrawable.getDuration(frame)`（16ms），
 *   缓动为线性（原版是 `Handler` 定时逐帧，没有插值器）；
 * - 动画总时长按原版公式计算：正向 `(总帧数 - 当前帧) × 帧时长`，反向 `当前帧 × 帧时长`；
 * - 明文 / 密文在总时长的一半处切换。
 *
 * 说明：原版通过切换 `InputType` 的 `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD` 位来实现，
 * Compose 里等价地用 `VisualTransformation` 切换。
 *
 * @param revealPassword 是否已切换为明文（动画过半后才会生效）。既可以由调用方受控，
 *   也可以不传回调、只当初始值用。
 * @param onRevealPasswordChange 明文 / 密文真正切换时的回调。
 * @param eyePaddingEnd 眼睛图标距输入框右边框的距离，原版 `mEyePaddingRight = 48px`。
 */
@Composable
fun SmartisanPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    revealPassword: Boolean = false,
    onRevealPasswordChange: (Boolean) -> Unit = {},
    singleLine: Boolean = true,
    textStyle: TextStyle = LocalSmartisanTypography.current.body.copy(fontSize = 15.sp),
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
    keyboardActions: KeyboardActions? = null,
    eyePaddingEnd: Dp = SmartisanInputDefaults.EyePaddingEnd,
) {
    val colors = LocalSmartisanColors.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val resources = smartisanThemedResources()

    // 原版 `R.drawable.pwd_eye_open_close_anim`：16 帧、每帧 16ms 的一次性帧动画。
    val eyeAnimation =
        remember(resources) {
            resources.getDrawable(R.drawable.pwd_eye_open_close_anim, null) as? AnimationDrawable
        }
    val frameCount = eyeAnimation?.numberOfFrames ?: 0

    // 眼睛图标固有尺寸，取自原版位图（126×102px @xxhdpi ≈ 42dp × 34dp）。
    val eyeFrameWidthPx =
        remember(eyeAnimation) {
            val frame = eyeAnimation?.getFrame(0)
            frame?.intrinsicWidth?.takeIf { it > 0 } ?: 0
        }
    val eyeFrameHeightPx =
        remember(eyeAnimation) {
            val frame = eyeAnimation?.getFrame(0)
            frame?.intrinsicHeight?.takeIf { it > 0 } ?: 0
        }
    val eyeWidth = with(density) { eyeFrameWidthPx.toDp() }
    val eyeHeight = with(density) { eyeFrameHeightPx.toDp() }

    // 原版 `EyeAnimator.mCurrent`：当前播放到的帧。
    val frameIndex =
        remember(eyeAnimation) {
            Animatable(if (revealPassword) 0f else (frameCount - 1).coerceAtLeast(0).toFloat())
        }
    // 眼睛的开关状态：点击时立刻翻转（动画先跑），明文 / 密文等动画过半再跟上。
    val eyeOpen = remember { mutableStateOf(revealPassword) }
    // 当前实际生效的明文状态（原版 `mVisible`）。
    val revealed = remember { mutableStateOf(revealPassword) }

    LaunchedEffect(revealPassword) { eyeOpen.value = revealPassword }

    LaunchedEffect(eyeOpen.value, eyeAnimation) {
        val animation = eyeAnimation ?: return@LaunchedEffect
        if (animation.numberOfFrames <= 1) return@LaunchedEffect
        val target = if (eyeOpen.value) 0f else (animation.numberOfFrames - 1).toFloat()
        val from = frameIndex.value
        if (from == target) return@LaunchedEffect
        val frameDuration =
            animation.getDuration(from.roundToInt().coerceIn(0, animation.numberOfFrames - 1))
        val duration = (abs(target - from) * frameDuration).roundToInt().coerceAtLeast(1)
        launch {
            // 原版：`postDelayed(mPendingSetInputType, getAnimationDuration() / 2)`。
            delay((duration / 2).toLong())
            revealed.value = eyeOpen.value
            onRevealPasswordChange(eyeOpen.value)
        }
        frameIndex.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = duration, easing = LinearEasing),
        )
    }

    val eyeInteraction = rememberSmartisanInteractionSource()
    val eyeHaptic = smartisanHaptic()
    val eyeClick =
        smartisanClick {
            eyeHaptic()
            eyeOpen.value = !eyeOpen.value
        }

    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            // 原版 `getCompoundPaddingRight`：文字区为眼睛图标让出「图标宽 + 右边距」。
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = eyeWidth + eyePaddingEnd),
            enabled = enabled,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = colors.textPrimary),
            visualTransformation =
                if (revealed.value) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = keyboardOptions,
            keyboardActions =
                keyboardActions
                    ?: KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        SmartisanText(
                            text = placeholder,
                            style = textStyle,
                            color = colors.textHint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (eyeAnimation != null && eyeWidth > 0.dp && eyeHeight > 0.dp) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = eyePaddingEnd)
                        .size(width = eyeWidth, height = eyeHeight)
                        .clickable(
                            interactionSource = eyeInteraction,
                            indication = null,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = eyeClick,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(width = eyeWidth, height = eyeHeight)) {
                    // 原版 `onDraw`：只画当前帧，不参与布局测量。
                    val index =
                        frameIndex.value.roundToInt().coerceIn(0, eyeAnimation.numberOfFrames - 1)
                    val frame = eyeAnimation.getFrame(index)
                    frame.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
                    drawIntoCanvas { canvas -> frame.draw(canvas.nativeCanvas) }
                }
            }
        }
    }
}
