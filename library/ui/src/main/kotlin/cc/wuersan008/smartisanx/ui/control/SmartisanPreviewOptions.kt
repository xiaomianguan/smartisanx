package cc.wuersan008.smartisanx.ui.control

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.interaction.smartisanClickable
import cc.wuersan008.smartisanx.core.interaction.smartisanHaptic
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanDrawablePainter
import cc.wuersan008.smartisanx.core.utils.rememberSmartisanStateListColor
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.R
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/** 原版 `dimen/item_block_gap`：根容器与下一块之间的间距。 */
private val ItemBlockGap = 5.dp

/** 原版 `dimen/settings_item_title_left_margin`：顶部标题左右边距。 */
private val HeadTitleHorizontalMargin = 30.dp

/** 原版 `dimen/settings_item_preview_image_margin_top`：预览图与单元格顶部的距离。 */
private val PreviewImageTopMargin = 11.5.dp

/** 原版布局里 `preview_image` 的左右内边距。 */
private val PreviewImageHorizontalPadding = 10.dp

/** 原版布局里 `preview_title` 与预览图的距离。 */
private val PreviewTitleTopMargin = 9.dp

/** 原版 `SettingPreviewStyle` 的左右内边距。 */
private val PreviewContainerPadding = 12.dp

/** 原版 `SettingItemTitleStyle` 的 `maxWidth`。 */
private val HeadTitleMaxWidth = 307.dp

/** 原版文字阴影颜色 `#2dffffff`（与提示条同一份）。 */
private val HeadTitleShadowColor = Color(0x2D_FFFFFF)

/** 原版文字阴影下移量 `android:shadowDy = 2`（物理像素）。 */
private const val HeadTitleShadowDy = 2f

/** 原版文字阴影模糊半径 `android:shadowRadius = 0.1`。 */
private const val HeadTitleShadowRadius = 0.1f

/** 原版 `PreviewOptionsCheckView.INVALID`：两栏都没有被选中。 */
const val SmartisanPreviewOptionsInvalid = -1

/** 一栏预览选项的内容（对应原版 `bindPreviewOptionView(index, drawableRes, title)`）。 */
data class SmartisanPreviewOption(
    /** 预览图素材。 */
    @DrawableRes val previewRes: Int,
    /** 预览图下方的标题；为空时原版把标题设为 `gone`，这里不占位。 */
    val title: String? = null,
)

/**
 * 控件：锤子两栏预览选项（设置页里选「预览样式」那种一左一右的图）。
 *
 * 对应 framework 的两个类（`framework/smartisanos.jar` 的 `classes.dex`）：
 *
 * | framework 类 | 原版布局 | 本组件 |
 * | --- | --- | --- |
 * | `smartisanos.widget.PreviewOptionsCheckView` | `preview_options_view_layout.xml` | [SmartisanPreviewOptions] |
 * | `smartisanos.widget.PreviewOptionView` | `preview_single_option_layout.xml` | [SmartisanPreviewOptionCell]（单元格，可单独用） |
 *
 * 素材取自 framework 资源 `framework-smartisanos-res.apk`：
 *
 * | 资源 | 用途 |
 * | --- | --- |
 * | `preview_options_two`（9-patch，118×560 px） | 两栏容器底图，中间那条分隔线来自它本身（横向拉伸时贴在栏间） |
 * | `preview_picture_selected`（76×76 px） | 选中角标 |
 * | `preview_picture_selected_disable_solid`（76×76 px） | 禁用态角标（走 `preview_picture_check_selector`） |
 * | `preview_title_text_colorlist` | 标题文字色状态表（`setting_item_text_color` #353539 / 禁用 #bababa） |
 *
 * 还原要点（照抄原版布局、样式与 `onClick`）：
 *
 * - 根容器垂直排列、底部留 `item_block_gap = 5dp`；
 * - 顶部标题走 `SettingItemTitleStyle`：13.5sp、`setting_item_summary_text_color`（#80000000）、
 *   左右 `settings_item_title_left_margin = 30dp`、上 7dp / 下 1dp、`maxWidth = 307dp`、
 *   文字阴影 `#2dffffff` 下移 2px；标题为空时不占位；
 * - 两栏容器：`preview_options_two` 底图 + 左右各 12dp 内边距（`SettingPreviewStyle`）；
 * - 单元格（`preview_single_option_layout.xml`）：预览图水平居中、左右各 10dp 内边距、
 *   上边距 `settings_item_preview_image_margin_top = 11.5dp`；标题在预览图下方 9dp、15sp、
 *   居中、单行；选中角标按原版固有尺寸（25.3dp）绘制，**右边缘对齐预览图右边缘、
 *   上边缘对齐单元格顶部**（原版 `layout_alignRight` + `layout_alignParentTop`）；
 * - 点击照抄原版 `onClick`：**点已经选中的那一栏不会再触发回调**（原版 `if (changed)`）；
 * - 禁用时图、标题、角标一起走禁用态（原版 `setEnabled` 逐个下发给子 View）。
 *
 * 与原版 `setAutoCheck(false)` 的关系：Compose 的选中态由调用方持有，回调里自己决定
 * 要不要把 `checkedIndex` 换过去即可，所以这里不再需要单独的开关。
 *
 * ```kotlin
 * var checked by remember { mutableIntStateOf(0) }
 * SmartisanPreviewOptions(
 *     left = SmartisanPreviewOption(previewRes = iconLeft, title = "列表"),
 *     right = SmartisanPreviewOption(previewRes = iconRight, title = "网格"),
 *     checkedIndex = checked,
 *     onCheckedChange = { checked = it },
 *     headTitle = "预览样式",
 * )
 * ```
 *
 * @param left 左栏内容。
 * @param right 右栏内容。
 * @param checkedIndex 当前选中的栏位：`0` 左、`1` 右，[SmartisanPreviewOptionsInvalid]（-1）表示都不选。
 * @param onCheckedChange 选中栏位变化的回调；点已选中的栏不会触发。
 * @param modifier 外部修饰符。
 * @param headTitle 顶部标题；为空时不占位。
 * @param enabled 是否可用；禁用时两栏都走禁用态。
 * @param backgroundRes 两栏容器底图；默认原版 `preview_options_two`，传 `null` 退回透明。
 * @param checkRes 选中角标素材；默认原版 `preview_picture_check_selector`。
 */
@Composable
fun SmartisanPreviewOptions(
    left: SmartisanPreviewOption,
    right: SmartisanPreviewOption,
    checkedIndex: Int,
    onCheckedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    headTitle: String? = null,
    enabled: Boolean = true,
    @DrawableRes backgroundRes: Int? = SmartisanDrawables.PreviewOptionsTwo,
    @DrawableRes checkRes: Int = SmartisanDrawables.PreviewCheckSelector,
) {
    Column(modifier.fillMaxWidth().padding(bottom = ItemBlockGap)) {
        if (!headTitle.isNullOrEmpty()) {
            SmartisanText(
                text = headTitle,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            start = HeadTitleHorizontalMargin,
                            end = HeadTitleHorizontalMargin,
                            top = 7.dp,
                            bottom = 1.dp,
                        )
                        .widthIn(max = HeadTitleMaxWidth),
                style =
                    TextStyle(
                        fontSize = 13.5.sp,
                        shadow =
                            Shadow(
                                color = HeadTitleShadowColor,
                                offset = Offset(0f, HeadTitleShadowDy),
                                blurRadius = HeadTitleShadowRadius,
                            ),
                    ),
                color = colorResource(R.color.setting_item_summary_text_color),
            )
        }
        val backgroundModifier =
            if (backgroundRes != null) {
                Modifier.smartisanDrawableBackground(backgroundRes)
            } else {
                Modifier
            }
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .then(backgroundModifier)
                    .padding(horizontal = PreviewContainerPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanPreviewOptionCell(
                option = left,
                checked = checkedIndex == 0,
                enabled = enabled,
                checkRes = checkRes,
                modifier = Modifier.weight(1f),
                onClick = { if (checkedIndex != 0) onCheckedChange(0) },
            )
            SmartisanPreviewOptionCell(
                option = right,
                checked = checkedIndex == 1,
                enabled = enabled,
                checkRes = checkRes,
                modifier = Modifier.weight(1f),
                onClick = { if (checkedIndex != 1) onCheckedChange(1) },
            )
        }
    }
}


/**
 * 单个预览选项单元格（原版 `PreviewOptionView` + `preview_single_option_layout.xml`）。
 *
 * 通常不直接用，交给 [SmartisanPreviewOptions] 摆两栏；单独用时行为一致：
 * 预览图居中、标题在图片下方、选中角标贴在预览图右上角。
 *
 * @param option 预览图与标题。
 * @param checked 是否选中；原版用 `visibility` 控制角标，这里不选中就不画角标。
 * @param modifier 外部修饰符。
 * @param enabled 是否可用。
 * @param checkRes 选中角标素材；默认原版 `preview_picture_check_selector`。
 * @param onClick 点击回调；原版按「整块」接收点击。
 */
@Composable
fun SmartisanPreviewOptionCell(
    option: SmartisanPreviewOption,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes checkRes: Int = SmartisanDrawables.PreviewCheckSelector,
    onClick: (() -> Unit)? = null,
) {
    val interaction = rememberSmartisanInteractionSource()
    val haptic = smartisanHaptic()
    val previewPainter = rememberSmartisanDrawablePainter(option.previewRes, enabled = enabled)
    val checkPainter = rememberSmartisanDrawablePainter(checkRes, enabled = enabled)
    val clickModifier =
        if (onClick != null) {
            Modifier.smartisanClickable(
                interactionSource = interaction,
                enabled = enabled,
            ) {
                haptic()
                onClick()
            }
        } else {
            Modifier
        }
    Column(
        modifier = modifier.then(clickModifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.padding(horizontal = PreviewImageHorizontalPadding)) {
            Image(
                painter = previewPainter,
                contentDescription = null,
                modifier = Modifier.padding(top = PreviewImageTopMargin),
            )
            if (checked) {
                // 原版角标上边缘对齐「父容器顶部」（不是图片顶部），所以贴在图片槽的顶边。
                Image(
                    painter = checkPainter,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
        if (!option.title.isNullOrEmpty()) {
            SmartisanText(
                text = option.title,
                modifier = Modifier.padding(top = PreviewTitleTopMargin),
                style = TextStyle(fontSize = 15.sp),
                color =
                    rememberSmartisanStateListColor(
                        colorRes = R.color.preview_title_text_colorlist,
                        enabled = enabled,
                    ),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

