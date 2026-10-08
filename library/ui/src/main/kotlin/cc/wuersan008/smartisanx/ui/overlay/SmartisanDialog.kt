package cc.wuersan008.smartisanx.ui.overlay

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import androidx.compose.ui.res.stringResource
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.rememberSmartisanDrawableButtonBackground

/**
 * 居中弹窗的标题栏、底部按钮，以及由它们拼出的弹窗。
 *
 * 复刻来源（严格照抄原版布局 `res/layout/smartisan_modal_dialog.xml`）：
 * ```
 * LinearLayout smartisan_modal_root   宽 308dp，背景 smartisan_modal_background，clipToOutline
 *   TextView     title    高 48dp  18sp 加粗  smartisan_modal_title_text
 *   FrameLayout  content  背景 smartisan_modal_content_background
 *   LinearLayout buttons  高 48dp
 *     TextView cancel   weight=1  背景 smartisan_modal_cancel_background  12.5sp 加粗
 *     View     divider  宽 1px    背景 smartisan_modal_border
 *     TextView confirm  weight=1  背景 smartisan_modal_confirm_background 12.5sp 加粗
 * ```
 *
 * 两个按钮的 selector 默认项都是 `@android:color/transparent`：
 * 弹窗里的确认按钮是**蓝色文字**，不是常驻的红色矩形，只有按下 / 聚焦 / 禁用时才画一层浅色底图。
 * 圆角只来自外壳的 `smartisan_modal_background`（10dp 圆角 + 1px 描边）与整体裁剪，
 * 按钮自己不再叠加任何圆角——这是此前「圆角不统一」的根因。
 */

/** 标题栏图标按钮的可点区域，对应原版 `smartisan_menu_dialog.xml` 里 48dp 的 ImageView。 */
private val DialogTitleBarIconTouchSize = 48.dp

/**
 * 标题左右留白。
 *
 * 对应原版标题的 `layout_marginHorizontal="48dp"`，正好让开两侧 48dp 的图标按钮。
 */
private val DialogTitleHorizontalPadding = 48.dp

/** 弹窗内容的默认左右留白，对齐原版弹窗内容区的内缩。 */
private val DialogContentHorizontalPadding = 18.dp

/** 弹窗内容的默认上下留白。 */
private val DialogContentVerticalPadding = 16.dp

/** 弹窗标题文字：原版 `smartisan_modal_dialog.xml` 的 18sp 加粗。 */
private val DialogModalTitleStyle =
    TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)

/** 弹窗底部按钮文字：原版 `smartisan_modal_dialog.xml` 的 12.5sp 加粗。 */
private val DialogModalButtonStyle =
    TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Bold, lineHeight = 17.sp)

/**
 * 原版弹窗里 1px 的物理像素分隔线换算成 dp。
 *
 * 原版 XML 写的是 `1px`（物理像素），而 Compose 的 dp 会随屏幕密度放大：
 * 直接写 `1.dp` 在 xxhdpi 上会比原版粗两倍，所以这里按当前密度换算回物理像素。
 */
@Composable
internal fun smartisanOnePixel(): Dp = Dp(1f / LocalDensity.current.density)

/**
 * 读取原版 `res/color/` 下的 state list，并按 Compose 的按下 / 禁用状态取色。
 *
 * 弹窗按钮文字色在原版是 `@color/smartisan_modal_confirm_text` 这样的 selector，
 * `colorResource` 只能拿到默认色，所以这里用 [ContextCompat.getColorStateList] 取完整状态色。
 *
 * @param colorRes `res/color/` 下的 selector 资源，例如 `R.color.smartisan_modal_confirm_text`。
 * @param enabled 是否可用，禁用时取 `state_enabled="false"` 那一项。
 * @param pressed 是否按下，按下时取 `state_pressed="true"` 那一项。
 */
@Composable
internal fun rememberSmartisanStateColor(
    @ColorRes colorRes: Int,
    enabled: Boolean = true,
    pressed: Boolean = false,
): Color {
    val context = LocalContext.current
    val stateList =
        remember(context, colorRes) { ContextCompat.getColorStateList(context, colorRes) }
    return remember(context, colorRes, stateList, enabled, pressed) {
        val state =
            when {
                !enabled -> intArrayOf(-android.R.attr.state_enabled)
                pressed -> intArrayOf(android.R.attr.state_pressed)
                else -> intArrayOf()
            }
        val argb =
            stateList?.getColorForState(state, stateList.defaultColor)
                ?: ContextCompat.getColor(context, colorRes)
        Color(argb)
    }
}

/**
 * 弹窗标题栏：高 48dp，标题居中、13.5sp 加粗，左右是原版图标按钮。
 *
 * 结构与原版 `smartisan_menu_dialog.xml` 的标题栏一致：`bottom_sheet_title_bar_bg` 底图 +
 * 居中标题 + 右侧 `standard_icon_cancel_selector` 关闭图标；需要「确定」时（[onConfirm] 不为 null）
 * 左侧再加一个 `standard_icon_cancel_selector`、右侧换成 `standard_icon_complete_selector`，
 * 对齐锤子音乐 `SmartisanMenuTitleBar` 的排布。
 *
 * 标题文字取色板的 `textSecondary`（浅色下即原版 `smartisan_menu_title_text`），
 * 因为 `bottom_sheet_title_bar_bg` 自带夜间变体，文字必须跟着主题走。
 *
 * @param title 标题文字。
 * @param onDismiss 取消 / 关闭回调。
 * @param onConfirm 确认回调；为 null 时只在右侧显示一个关闭图标。
 * @param confirmEnabled 「确定」图标是否可用，禁用时交给原版 selector 切换禁用态图标。
 * @param modifier 作用于标题栏容器。
 * @param backgroundRes 标题栏底色；默认用原版 `bottom_sheet_title_bar_bg`（NinePatch，自带夜间变体），
 *   传 null 时退回透明底。
 */
@Composable
fun SmartisanDialogTitleBar(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    confirmEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.BottomSheetTitleBarBackground,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val backgroundModifier =
        if (backgroundRes != null) {
            Modifier.smartisanDrawableBackground(backgroundRes)
        } else {
            Modifier
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SmartisanDimens.DialogTitleHeight)
                .then(backgroundModifier),
    ) {
        SmartisanText(
            text = title,
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(horizontal = DialogTitleHorizontalPadding),
            style = typography.dialogTitle,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (onConfirm != null) {
            SmartisanDialogTitleBarIcon(
                res = SmartisanDrawables.IconCancel,
                contentDescription = stringResource(R.string.smartisan_cancel),
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
        SmartisanDialogTitleBarIcon(
            res =
                if (onConfirm == null) SmartisanDrawables.IconCancel
                else SmartisanDrawables.IconComplete,
            contentDescription =
                if (onConfirm == null) stringResource(R.string.smartisan_cancel) else stringResource(R.string.smartisan_confirm),
            onClick = onConfirm ?: onDismiss,
            modifier = Modifier.align(Alignment.CenterEnd),
            enabled = onConfirm == null || confirmEnabled,
        )
    }
}

/**
 * 标题栏里的图标按钮：48dp 可点区域、36dp 图标（对应原版 48dp ImageView 的 6dp 内边距）。
 *
 * 不使用涟漪：按下 / 禁用态由原版 selector（`standard_icon_cancel_selector` /
 * `standard_icon_complete_selector`）自己切换，并附带系统点击音与触感反馈。
 *
 * @param res 原版图标 selector。
 * @param contentDescription 无障碍描述。
 * @param onClick 点击回调。
 * @param modifier 作用于按钮容器。
 * @param enabled 是否可用。
 */
@Composable
private fun SmartisanDialogTitleBarIcon(
    @DrawableRes res: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    Box(
        modifier =
            modifier
                .size(DialogTitleBarIconTouchSize)
                .smartisanClickable(interactionSource = interaction, enabled = enabled) {
                    haptic()
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        SmartisanIcon(
            res = res,
            contentDescription = contentDescription,
            modifier = Modifier.size(SmartisanDimens.IconSize),
            enabled = enabled,
            pressed = pressed,
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * 弹窗底部按钮：高 48dp、12.5sp 加粗、单行居中，**默认透明底**。
 *
 * 原版弹窗按钮就是「透明底 + 彩色文字」：`smartisan_modal_cancel_background` /
 * `smartisan_modal_confirm_background` 两个 selector 的默认项都是 `@android:color/transparent`，
 * 只有按下 / 聚焦 / 禁用时才画一层浅色底图。所以这里：
 * - 不给按钮加任何圆角（圆角只由 [SmartisanModalWindow] 的外壳提供）；
 * - 不铺常驻底色，按下态交给原版 selector；
 * - 文字色用原版 `res/color/` state list：确认蓝色、取消灰色，按下 / 禁用自动切换。
 *
 * ```kotlin
 * Row(Modifier.height(48.dp)) {
 *     SmartisanDialogButton("取消", onClick = ::cancel, modifier = Modifier.weight(1f), accent = false)
 *     SmartisanDialogButton("确定", onClick = ::confirm, modifier = Modifier.weight(1f))
 * }
 * ```
 *
 * @param text 按钮文字。
 * @param onClick 点击回调。
 * @param modifier 作用于按钮容器，通常配合 `fillMaxWidth()` 或 `weight(1f)` 使用。
 * @param enabled 是否可用，禁用态交给原版 selector。
 * @param accent 是否使用确认按钮的蓝色文字；false 时用取消按钮的灰色文字。
 * @param backgroundRes 原版底图 selector；为 null 时按 [accent] 取原版弹窗按钮 selector。
 * @param shadowRes 原版投影 selector；只有传入带投影的底图（例如 `smartisan_menu_confirm_background`）时才需要。
 * @param showShadow 是否绘制投影；原版弹窗按钮没有投影，默认底图也就不会带投影。
 * @param contentColor 文字色覆盖；默认取原版 state list，传具体颜色时按传入值绘制
 *   （例如锤子时钟菜单弹窗的红色通栏按钮用白字）。
 */
@Composable
fun SmartisanDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = true,
    @DrawableRes backgroundRes: Int? = null,
    @DrawableRes shadowRes: Int? = null,
    showShadow: Boolean = true,
    contentColor: Color = Color.Unspecified,
) {
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    val haptic = smartisanHaptic()
    val resolvedBackgroundRes =
        backgroundRes
            ?: if (accent) R.drawable.smartisan_modal_confirm_background
            else R.drawable.smartisan_modal_cancel_background
    val backgroundModifier =
        rememberSmartisanDrawableButtonBackground(
            backgroundRes = resolvedBackgroundRes,
            shadowRes = shadowRes,
            showShadow = showShadow,
            enabled = enabled,
            pressed = pressed,
        ) ?: Modifier
    val resolvedContentColor =
        if (contentColor != Color.Unspecified) {
            contentColor
        } else {
            rememberSmartisanStateColor(
                colorRes =
                    if (accent) R.color.smartisan_modal_confirm_text
                    else R.color.smartisan_modal_cancel_text,
                enabled = enabled,
                pressed = pressed,
            )
        }
    Box(
        modifier =
            modifier
                .height(SmartisanDimens.DialogButtonHeight)
                .then(backgroundModifier)
                .smartisanClickable(interactionSource = interaction, enabled = enabled) {
                    haptic()
                    onClick()
                },
        contentAlignment = Alignment.Center,
    ) {
        SmartisanText(
            text = text,
            style = DialogModalButtonStyle,
            color = resolvedContentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * 居中弹窗：标题 + 内容 + 「取消 | 确定」按钮行，结构与尺寸严格对齐原版
 * `res/layout/smartisan_modal_dialog.xml`。
 *
 * - 标题：48dp 高、18sp 加粗、居中，颜色 `smartisan_modal_title_text`；
 * - 内容：`smartisan_modal_content_background`（白底 + 2px 描边），默认 18dp 左右、16dp 上下留白；
 * - 按钮行：48dp 高，两个 `weight(1f)` 的透明按钮，中间一条 1px 的 `smartisan_modal_border` 竖线。
 *
 * 确认时先关闭弹窗再执行 [onConfirm]，与原版 `dismiss()` 之后再执行动作的顺序一致。
 *
 * ```kotlin
 * if (visible) {
 *     SmartisanDialog(
 *         onDismissRequest = { visible = false },
 *         title = "删除录音",
 *         confirmText = "删除",
 *         dismissText = "取消",
 *         onConfirm = { delete() },
 *     ) {
 *         SmartisanText("删除后无法恢复。", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
 *     }
 * }
 * ```
 *
 * @param onDismissRequest 关闭回调（点击遮罩、返回键、取消或确认）。
 * @param title 标题。
 * @param modifier 作用于弹窗面板。
 * @param confirmText 确认按钮文字。
 * @param dismissText 取消按钮文字；原版布局固定是「取消 | 确定」两个等宽按钮，
 *   因此为 null 时按原版默认文案「取消」绘制。
 * @param confirmEnabled 确认按钮是否可用。
 * @param onConfirm 确认回调。
 * @param content 弹窗内容，纵向排列，默认带 18dp 左右、16dp 上下留白。
 */
@Composable
fun SmartisanDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    confirmText: String? = null,
    dismissText: String? = null,
    confirmEnabled: Boolean = true,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dismiss = LocalSmartisanModalDismiss.current ?: onDismissRequest
    val confirm = {
        // 与原版一致：先关闭弹窗，再执行确认动作。
        dismiss()
        onConfirm()
    }
    val cancelText = dismissText ?: stringResource(R.string.smartisan_cancel)
    val confirmLabel = confirmText ?: stringResource(R.string.smartisan_confirm)
    SmartisanModalWindow(onDismissRequest = onDismissRequest, modifier = modifier) {
        // 标题：48dp 高、18sp 加粗、居中（原版 smartisan_modal_title）。
        Box(
            modifier = Modifier.fillMaxWidth().height(SmartisanDimens.DialogTitleHeight),
            contentAlignment = Alignment.Center,
        ) {
            SmartisanText(
                text = title,
                modifier = Modifier.padding(horizontal = DialogTitleHorizontalPadding),
                style = DialogModalTitleStyle,
                // 原版弹窗底图没有夜间变体（始终浅色），所以标题固定用原版深色文字。
                color = colorResource(R.color.smartisan_modal_title_text),
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
        // 内容：原版 smartisan_modal_content_background（白底 + 2px 描边）。
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .smartisanDrawableBackground(R.drawable.smartisan_modal_content_background)
                    .padding(
                        horizontal = DialogContentHorizontalPadding,
                        vertical = DialogContentVerticalPadding,
                    ),
        ) {
            content()
        }
        // 按钮行：48dp + 1px 分隔线 + 两个 weight=1 的透明按钮。
        Row(modifier = Modifier.fillMaxWidth().height(SmartisanDimens.DialogButtonHeight)) {
            SmartisanDialogButton(
                text = cancelText,
                onClick = dismiss,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                accent = false,
            )
            Box(
                modifier =
                    Modifier.fillMaxHeight()
                        .width(smartisanOnePixel())
                        .background(colorResource(R.color.smartisan_modal_border)),
            )
            SmartisanDialogButton(
                text = confirmLabel,
                onClick = confirm,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                enabled = confirmEnabled,
                accent = true,
            )
        }
    }
}

/**
 * 确认弹窗：在 [SmartisanDialog] 的基础上加一段居中说明文字。
 *
 * ```kotlin
 * SmartisanConfirmDialog(
 *     onDismissRequest = { visible = false },
 *     title = "清空数据",
 *     message = "所有记录都会被删除，且无法恢复。",
 *     onConfirm = { clear() },
 * )
 * ```
 *
 * @param onDismissRequest 关闭回调。
 * @param title 标题。
 * @param message 说明文字。
 * @param confirmText 确认按钮文字。
 * @param dismissText 取消按钮文字。
 * @param onConfirm 确认回调。
 */
@Composable
fun SmartisanConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String? = null,
    dismissText: String? = null,
    onConfirm: () -> Unit,
) {
    SmartisanDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        confirmText = confirmText,
        dismissText = dismissText,
        onConfirm = onConfirm,
    ) {
        SmartisanText(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            style = LocalSmartisanTypography.current.body,
            // 弹窗内容区始终是白底（原版底图没有夜间变体），所以说明文字固定用原版深色文字。
            color = colorResource(R.color.smartisan_text_primary),
            textAlign = TextAlign.Center,
        )
    }
}
