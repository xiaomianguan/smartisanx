package cc.wuersan008.smartisanx.ui.layout

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.anim.SmartisanMotion
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import kotlinx.coroutines.delay

/**
 * 提示条（轻量 snackbar）。
 *
 * 对应 framework 里的两个类（`framework/smartisanos.jar` 的 `classes.dex`）：
 *
 * | framework 类 | 对应布局 | 本组件形态 |
 * | --- | --- | --- |
 * | `smartisanos.widget.SnackbarWithButton` | `res/layout/snackbar_with_btn_layout.xml` | 文案 + 右侧金色操作按钮 |
 * | `smartisanos.widget.SnackbarWithDrawable` | `res/layout/snackbar_with_drawable_layout.xml` | 文案 + 右侧竖分隔线 + 40dp 图标按钮 |
 *
 * 两者都由 framework 的 `smartisanos.widget.CustomToast.makeButtonSnackbar()` /
 * `makeDrawableSnackbar()` 弹出来，底图统一是 `drawable/toast_frame_smartisanos`，
 * 默认时长 `CustomToast.SNACKBAR_DURATION = 2000ms`（点击操作按钮会先取消提示条再回调，
 * 这里保持一致）。
 *
 * 还原要点：
 * - 文案样式照抄 `values/styles.xml` 的 `SnackbarMessageStyle`：加粗、`#996b3d`、
 *   `maxLines = 1`、`ellipsize = end`、`gravity = center_vertical`、外边距 18dp / 10dp；
 * - 操作按钮底图 `toast_action_btn_selector`（`snack_button_normal` / `snack_button_pressed`），
 *   左右内边距取该 nine-patch 的 content padding（51px @3x ≈ 17dp），按钮高度 40dp；
 * - 图标变体的竖分隔线是 `2.0px` 宽、颜色 `#1e996b3d`（布局里写死），图标 40dp 居中；
 * - 进入 / 退出用淡入淡出（原版弹窗动画是窗口级 fade），时长取本库短动画 200ms。
 *
 * ```kotlin
 * // 1) 最简单：放进页面底部的 Box 里，2 秒后自动淡出
 * SmartisanSnackbar(message = "已删除", actionText = "撤销", onAction = { restore() })
 *
 * // 2) 需要多次触发：用 state + host
 * val snackbar = rememberSmartisanSnackbarState()
 * Box(Modifier.fillMaxSize()) {
 *     Content()
 *     SmartisanSnackbarHost(state = snackbar)
 * }
 * // 任意位置：snackbar.show("已删除", actionText = "撤销") { restore() }
 * ```
 *
 * @param message 提示文案。
 * @param modifier 外部修饰符。
 * @param actionText 右侧操作按钮文案；`null` 且 [iconRes] 也为 `null` 时只显示文案。
 * @param onAction 点击操作按钮的回调（原版会先取消提示条再回调）。
 * @param iconRes 右侧图标按钮素材，原版默认是关闭图标 `toast_action_dismiss`；
 *   传该参数即切换到 `SnackbarWithDrawable` 那套「分隔线 + 图标」形态。
 * @param durationMillis 展示时长；`0` 表示常驻不自动消失。
 * @param onDismiss 淡出结束后回调，配合 [SmartisanSnackbarHost] 清空 state 用。
 */
@Composable
fun SmartisanSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    @DrawableRes iconRes: Int? = null,
    durationMillis: Int = SmartisanSnackbarDefaults.DurationShort,
    onDismiss: () -> Unit = {},
) {
    // 文案 / 按钮变化时视为一条新提示条，重新开始计时。
    var visible by remember(message, actionText, iconRes) { mutableStateOf(true) }
    LaunchedEffect(message, actionText, iconRes, durationMillis) {
        if (durationMillis > 0) {
            delay(durationMillis.toLong())
            visible = false
            // 等淡出动画放完再通知调用方，避免动画被提前打断。
            delay(SmartisanMotion.DurationShort.toLong())
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(SmartisanMotion.DurationShort, easing = SmartisanMotion.EaseInOut)),
        exit = fadeOut(tween(SmartisanMotion.DurationShort, easing = SmartisanMotion.EaseInOut)),
    ) {
        SmartisanSnackbarBar(
            message = message,
            actionText = actionText,
            iconRes = iconRes,
            onAction =
                onAction?.let { callback ->
                    {
                        // 原版 CustomToast：点操作按钮先 cancel() 再回调。
                        visible = false
                        callback()
                    }
                },
        )
    }
}

/**
 * 提示条数据。
 *
 * 由 [SmartisanSnackbarState.show] 生成；每次 show 都会换一个新的 [id]，
 * 提示条据此重新计时并重播进入动画。
 */
@Immutable
class SmartisanSnackbarData internal constructor(
    /** 本次提示条的序号，用于强制重播动画与重新计时。 */
    internal val id: Long,
    /** 提示文案。 */
    val message: String,
    /** 右侧操作按钮文案。 */
    val actionText: String? = null,
    /** 右侧图标按钮素材。 */
    @DrawableRes val iconRes: Int? = null,
    /** 展示时长（毫秒）。 */
    val durationMillis: Int = SmartisanSnackbarDefaults.DurationShort,
    /** 点击操作按钮的回调。 */
    internal val onAction: (() -> Unit)? = null,
)

/**
 * 提示条状态。
 *
 * 用法见 [SmartisanSnackbarHost]；由 [rememberSmartisanSnackbarState] 创建。
 */
@Stable
class SmartisanSnackbarState internal constructor() {
    private var idSeed = 0L

    /** 当前要显示的提示条；`null` 表示没有。 */
    var current: SmartisanSnackbarData? by mutableStateOf(null)
        private set

    /** 是否正在显示提示条。 */
    val isVisible: Boolean
        get() = current != null

    /**
     * 弹出一条提示条（会顶掉上一条）。
     *
     * @param message 提示文案。
     * @param actionText 右侧操作按钮文案。
     * @param iconRes 右侧图标按钮素材（传了就切到「分隔线 + 图标」形态）。
     * @param durationMillis 展示时长，`0` 表示常驻。
     * @param onAction 点击操作按钮的回调。
     */
    fun show(
        message: String,
        actionText: String? = null,
        @DrawableRes iconRes: Int? = null,
        durationMillis: Int = SmartisanSnackbarDefaults.DurationShort,
        onAction: (() -> Unit)? = null,
    ) {
        idSeed += 1
        current =
            SmartisanSnackbarData(
                id = idSeed,
                message = message,
                actionText = actionText,
                iconRes = iconRes,
                durationMillis = durationMillis,
                onAction = onAction,
            )
    }

    /** 立刻收起提示条。 */
    fun dismiss() {
        current = null
    }
}

/** 记住一个 [SmartisanSnackbarState]。 */
@Composable
fun rememberSmartisanSnackbarState(): SmartisanSnackbarState = remember { SmartisanSnackbarState() }

/**
 * 提示条宿主：显示 [state] 里当前的提示条，淡出结束后自动清空 state。
 *
 * 通常叠在页面底部：
 *
 * ```kotlin
 * val snackbar = rememberSmartisanSnackbarState()
 * Box(Modifier.fillMaxSize()) {
 *     Content()
 *     SmartisanSnackbarHost(state = snackbar)
 * }
 * ```
 *
 * @param state 提示条状态。
 * @param modifier 外部修饰符。
 * @param contentAlignment 提示条在宿主里的对齐方式，默认底部居中（原版 toast 的 `Gravity.BOTTOM`）。
 */
@Composable
fun SmartisanSnackbarHost(
    state: SmartisanSnackbarState,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.BottomCenter,
) {
    val data = state.current
    Box(modifier = modifier, contentAlignment = contentAlignment) {
        if (data != null) {
            // 按 id 分组：连续 show 时重新计时、重播动画。
            key(data.id) {
                SmartisanSnackbar(
                    message = data.message,
                    actionText = data.actionText,
                    iconRes = data.iconRes,
                    onAction = data.onAction,
                    durationMillis = data.durationMillis,
                    onDismiss = state::dismiss,
                )
            }
        }
    }
}


/** 提示条默认值，取自 framework 的 `CustomToast` 与 `res/values`。 */
object SmartisanSnackbarDefaults {
    /** 短时长，原版 `CustomToast.SNACKBAR_DURATION = 2000`。 */
    const val DurationShort = 2000

    /** 长时长，原版 `CustomToast.DURATION_LONG = 3500`。 */
    const val DurationLong = 3500

    /** 提示条底图，原版 `CustomToast` 里的 `drawable/toast_frame_smartisanos`。 */
    @DrawableRes val Background = R.drawable.sos_android_drawable_toast_frame_smartisanos

    /** 操作按钮底图（selector），原版 `drawable/toast_action_btn_selector`。 */
    @DrawableRes val ActionBackground = R.drawable.toast_action_btn_selector

    /** 图标变体的默认图标，原版 `drawable/toast_action_dismiss`。 */
    @DrawableRes val DismissIcon = R.drawable.toast_action_dismiss
}

/** 提示条文案颜色，原版 `SnackbarMessageStyle` 里写死的 `#996b3d`。 */
private val SnackbarTextColor = Color(0xFF996B3D)

/** 图标变体竖分隔线颜色，原版 `snackbar_with_drawable_layout.xml` 里写死的 `#1e996b3d`。 */
private val SnackbarDividerColor = Color(0x1E996B3D)

/** 竖分隔线宽度，原版布局里的 `android:layout_width = 2.0px`。 */
private val SnackbarDividerWidth = 2.dp

/** 文案左右外边距，原版 `SnackbarMessageStyle` 的 `layout_marginLeft/Right = 18dp`。 */
private val SnackbarMessageHorizontalMargin = 18.dp

/** 文案上下外边距，原版 `SnackbarMessageStyle` 的 `layout_marginTop/Bottom = 10dp`。 */
private val SnackbarMessageVerticalMargin = 10.dp

/** 图标尺寸，原版 `snackbar_with_drawable_layout.xml` 里的 `40dp`。 */
private val SnackbarIconSize = 40.dp

/** 操作按钮高度，取自 nine-patch `snack_button_normal` 的 122px @3x。 */
private val SnackbarActionHeight = 40.dp

/** 操作按钮左右内边距，取自 nine-patch `snack_button_normal` 的 content padding（51px @3x）。 */
private val SnackbarActionHorizontalPadding = 17.dp

/** 提示条本体：左边文案，右边操作按钮或「分隔线 + 图标」。 */
@Composable
private fun SmartisanSnackbarBar(
    message: String,
    actionText: String?,
    @DrawableRes iconRes: Int?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .smartisanDrawableBackground(SmartisanSnackbarDefaults.Background)
                .fillMaxWidth()
                // 竖分隔线要撑满提示条高度，用最小固有高度确定行高。
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmartisanText(
            text = message,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = SnackbarMessageHorizontalMargin,
                        end = SnackbarMessageHorizontalMargin,
                        top = SnackbarMessageVerticalMargin,
                        bottom = SnackbarMessageVerticalMargin,
                    ),
            color = SnackbarTextColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        when {
            actionText != null -> SnackbarActionButton(text = actionText, onClick = onAction)
            iconRes != null -> SnackbarIconButton(iconRes = iconRes, onClick = onAction)
        }
    }
}

/** 操作按钮：原版是带 `toast_action_btn_selector` 底图的 `TextView`。 */
@Composable
private fun SnackbarActionButton(
    text: String,
    onClick: (() -> Unit)?,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Box(
        modifier =
            Modifier
                .height(SnackbarActionHeight)
                .smartisanDrawableBackground(SmartisanSnackbarDefaults.ActionBackground, pressed = pressed)
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = onClick != null,
                    onClick = { onClick?.invoke() },
                )
                .padding(horizontal = SnackbarActionHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            color = SnackbarTextColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 图标按钮：原版是「2px 竖分隔线 + 40dp ImageView」。 */
@Composable
private fun SnackbarIconButton(
    @DrawableRes iconRes: Int,
    onClick: (() -> Unit)?,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Box(
        modifier =
            Modifier
                .width(SnackbarDividerWidth)
                .fillMaxHeight()
                .background(SnackbarDividerColor),
    )
    SmartisanIcon(
        res = iconRes,
        contentDescription = null,
        modifier =
            Modifier
                .size(SnackbarIconSize)
                .smartisanClickable(
                    interactionSource = interaction,
                    enabled = onClick != null,
                    onClick = { onClick?.invoke() },
                ),
        pressed = pressed,
    )
}

