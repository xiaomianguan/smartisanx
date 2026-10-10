package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanActionButton
import cc.wuersan008.smartisanx.ui.control.SmartisanActionButtonGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonTabGroupItem
import cc.wuersan008.smartisanx.ui.control.SmartisanChips
import cc.wuersan008.smartisanx.ui.control.SmartisanIconSlider
import cc.wuersan008.smartisanx.ui.control.SmartisanSliderIcon
import cc.wuersan008.smartisanx.ui.control.SmartisanSmoothSeekBar
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.roundToInt
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorButton
import cc.wuersan008.smartisanx.ui.control.SmartisanCalculatorButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanNumberPicker
import cc.wuersan008.smartisanx.ui.control.SmartisanPageIndicator
import cc.wuersan008.smartisanx.ui.control.SmartisanPreviewOption
import cc.wuersan008.smartisanx.ui.control.SmartisanPreviewOptionCell
import cc.wuersan008.smartisanx.ui.control.SmartisanPreviewOptions
import cc.wuersan008.smartisanx.ui.control.SmartisanPreviewOptionsInvalid
import cc.wuersan008.smartisanx.ui.control.SmartisanProgressIndicator
import cc.wuersan008.smartisanx.ui.control.SmartisanProgressState
import cc.wuersan008.smartisanx.ui.control.SmartisanTips
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/**
 * 基础控件页里「原版控件移植」这一段。
 *
 * 单独成文件是为了和 [ControlPage] 里自研控件的段落分开，方便逐个对照原版类。
 */
@Composable
fun OriginalControlSections() {
    ButtonTabGroupSection()
    CalculatorButtonSection()
    SmoothSeekBarSection()
    IconSliderSection()
    ChipsSection()
    NumberPickerSection()
    PageIndicatorSection()
    ProgressIndicatorSection()
    TipsSection()
    PreviewOptionsSection()
    ActionButtonGroupSection()
}

/** 底部操作按钮组：原版 `smartisanos.widget.ActionButtonGroup` / `ButtonGroup`。 */
@Composable
private fun ActionButtonGroupSection() {
    var lastAction by remember { mutableStateOf("（还没点）") }
    SampleSectionHeader("底部操作按钮组（ActionButtonGroup / ButtonGroup）")
    SmartisanText(
        text = "原版设置页 / 文件管理器多选时贴底的 48dp 操作条。最近一次点击：$lastAction",
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
        style = LocalSmartisanTypography.current.caption,
        color = LocalSmartisanColors.current.textTertiary,
    )
    // 三个文字按钮：多个按钮按位置取 filter_left / middle / right 分段底图。
    SmartisanActionButtonGroup(
        actions =
            listOf(
                SmartisanActionButton("复制") { lastAction = "复制" },
                SmartisanActionButton("移动") { lastAction = "移动" },
                SmartisanActionButton("删除") { lastAction = "删除" },
            ),
    )
    Box(Modifier.height(8.dp))
    // 单个文字按钮：走 SmallButton.Standard 的整块底图。
    SmartisanActionButtonGroup(
        actions = listOf(SmartisanActionButton("完成") { lastAction = "完成" }),
    )
    Box(Modifier.height(8.dp))
    // 左图标 + 四个文字按钮（原版 ACTION_MODE_LEFT，文字居中）。
    SmartisanActionButtonGroup(
        actions =
            listOf(
                SmartisanActionButton("分享") { lastAction = "分享" },
                SmartisanActionButton("收藏") { lastAction = "收藏" },
                SmartisanActionButton("压缩") { lastAction = "压缩" },
                SmartisanActionButton("删除", enabled = false) { lastAction = "删除（禁用）" },
            ),
        leftAction =
            SmartisanActionButton(
                iconRes = SmartisanDrawables.IconComplete,
                contentDescription = "全选",
            ) { lastAction = "全选" },
    )
    Box(Modifier.height(8.dp))
    // 两个文字按钮 + 右图标（原版 ACTION_MODE_BOTH，文字左对齐）。
    SmartisanActionButtonGroup(
        actions =
            listOf(
                SmartisanActionButton("上一页") { lastAction = "上一页" },
                SmartisanActionButton("下一页") { lastAction = "下一页" },
            ),
        rightAction =
            SmartisanActionButton(
                iconRes = SmartisanDrawables.IconMultiSelect,
                contentDescription = "设置",
            ) { lastAction = "设置" },
        showShadow = false,
    )
    SmartisanRowDivider()
}

/** 分段按钮组：原版 `smartisanos.widget.ButtonTabGroup`。 */
@Composable
private fun ButtonTabGroupSection() {
    var selected by remember { mutableIntStateOf(0) }
    var gapped by remember { mutableIntStateOf(1) }
    SampleSectionHeader("分段按钮组（ButtonTabGroup）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            // 连续分段：首 / 中 / 尾三段圆角不同，相邻分段按原版负边距互相压住。
            SmartisanButtonTabGroup(
                items = listOf("全部", "今天", "已完成"),
                selectedIndex = selected,
                onSelectedChange = { selected = it },
            )
            SmartisanText(
                text = "连续三段（selector_small_btn_filter_left / _middle / _right），当前第 ${selected + 1} 段",
                modifier = Modifier.padding(top = 12.dp),
                style = LocalSmartisanTypography.current.caption,
                color = LocalSmartisanColors.current.textTertiary,
            )
            // 有间距：每个分段独立底图，走 selector_small_btn_standard。
            SmartisanButtonTabGroup(
                items = listOf("日", "周", "月"),
                selectedIndex = gapped,
                onSelectedChange = { gapped = it },
                hasGap = true,
                modifier = Modifier.padding(top = 16.dp),
            )
            // 带图标 + 禁用项。
            SmartisanButtonTabGroup(
                items =
                    listOf(
                        SmartisanButtonTabGroupItem("添加", SmartisanOriginalIcons.Add),
                        SmartisanButtonTabGroupItem("完成", SmartisanOriginalIcons.Complete),
                        SmartisanButtonTabGroupItem("删除", SmartisanOriginalIcons.Delete),
                    ),
                selectedIndex = 0,
                onSelectedChange = {},
                disabledIndices = setOf(2),
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

/** 计算器按键：原版 `com.smartisanos.calculator.HammerButton`。 */
@Composable
private fun CalculatorButtonSection() {
    var pressedTimes by remember { mutableIntStateOf(0) }
    SampleSectionHeader("计算器按键（原版 com.smartisanos.calculator.HammerButton）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartisanCalculatorButton(
                    iconRes = SmartisanOriginalIcons.Add,
                    onClick = { pressedTimes++ },
                    style = SmartisanCalculatorButtonStyle.White,
                    contentDescription = "加",
                    modifier = Modifier.size(64.dp),
                )
                SmartisanCalculatorButton(
                    iconRes = SmartisanOriginalIcons.Delete,
                    onClick = { pressedTimes++ },
                    style = SmartisanCalculatorButtonStyle.Grey,
                    contentDescription = "删除（长按连发）",
                    // 原版删除键：按下 500ms 后开始连发，之后每 150ms 一次。
                    onRepeat = { pressedTimes++ },
                    modifier = Modifier.size(64.dp),
                )
                SmartisanCalculatorButton(
                    iconRes = SmartisanOriginalIcons.Settings,
                    onClick = { pressedTimes++ },
                    style = SmartisanCalculatorButtonStyle.Black,
                    highlighted = true,
                    contentDescription = "设置（带高亮角标）",
                    modifier = Modifier.size(64.dp),
                )
                SmartisanCalculatorButton(
                    iconRes = SmartisanOriginalIcons.Confirm,
                    onClick = { pressedTimes++ },
                    style = SmartisanCalculatorButtonStyle.Equal,
                    contentDescription = "等号",
                    modifier = Modifier.size(64.dp),
                )
            }
            SmartisanText(
                text = "按键触发 $pressedTimes 次；按住删除键会按原版节奏连发",
                modifier = Modifier.padding(top = 12.dp),
                style = LocalSmartisanTypography.current.caption,
                color = LocalSmartisanColors.current.textTertiary,
            )
        }
    }
}

/** 标签（芯片）：framework 的 `smartisanos.widget.ChipsView` / `ShadowChipsView`。 */
@Composable
private fun ChipsSection() {
    val tags = listOf("工作", "家人", "重要", "待办")
    var selected by remember { mutableStateOf(emptySet<String>()) }
    var removable by remember { mutableStateOf(tags) }
    SampleSectionHeader("标签（framework ChipsView）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
            SmartisanChips(
                chips = tags,
                selected = selected,
                onChipClick = { t ->
                    selected = if (t in selected) selected - t else selected + t
                },
            )
            SmartisanText(
                text = "已选：${if (selected.isEmpty()) "无" else selected.joinToString("、")}",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textTertiary,
            )
            SmartisanChips(
                chips = removable,
                modifier = Modifier.padding(top = 12.dp),
                onChipRemove = { t -> removable = removable - t },
            )
            SmartisanText(
                text = if (removable.isEmpty()) "全部删掉了" else "点右侧叉号可删除",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textTertiary,
            )
        }
    }
}

/** 滑杆：framework 的 `smartisanos.widget.SmoothSeekBar`。 */
@Composable
private fun SmoothSeekBarSection() {
    var brightness by remember { mutableFloatStateOf(0.6f) }
    var volume by remember { mutableFloatStateOf(0.3f) }
    var stepped by remember { mutableFloatStateOf(0.5f) }
    SampleSectionHeader("滑杆（framework SmoothSeekBar）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
            SmartisanText(
                text = "亮度  ${(brightness * 100).roundToInt()}%",
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanSmoothSeekBar(value = brightness, onValueChange = { brightness = it })
            SmartisanText(
                text = "音量  ${(volume * 100).roundToInt()}%",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanSmoothSeekBar(value = volume, onValueChange = { volume = it })
            SmartisanText(
                text = "按 10 档吸附  ${(stepped * 10).roundToInt()}",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanSmoothSeekBar(value = stepped, onValueChange = { stepped = it }, steps = 10)
            SmartisanText(
                text = "禁用态",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanSmoothSeekBar(value = 0.4f, onValueChange = {}, enabled = false)
        }
    }
}

/** 带图标的滑杆：framework 的 `smartisanos.widget.SliderWithIcons`。 */
@Composable
private fun IconSliderSection() {
    var volume by remember { mutableFloatStateOf(0.6f) }
    var alone by remember { mutableFloatStateOf(0.35f) }
    SampleSectionHeader("带图标的滑杆（framework SliderWithIcons）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
            SmartisanText(
                text = "音量  ${(volume * 100).roundToInt()}%",
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            // 原版布局：左图标 + 滑杆 + 右图标；两端按素材固有尺寸摆放，这里显式给 26dp。
            SmartisanIconSlider(
                value = volume,
                onValueChange = { volume = it },
                leading = {
                    SmartisanSliderIcon(
                        res = SmartisanDrawables.VolumeSmall,
                        contentDescription = "小音量",
                        size = 26.dp,
                    )
                },
                trailing = {
                    SmartisanSliderIcon(
                        res = SmartisanDrawables.VolumeHigh,
                        contentDescription = "大音量",
                        size = 26.dp,
                    )
                },
            )
            SmartisanText(
                text = "只有一端有图标  ${(alone * 100).roundToInt()}%",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanIconSlider(
                value = alone,
                onValueChange = { alone = it },
                leading = {
                    SmartisanSliderIcon(
                        res = SmartisanDrawables.VolumeMiddle,
                        contentDescription = "中等音量",
                        size = 26.dp,
                    )
                },
            )
            SmartisanText(
                text = "禁用态（滑块换成禁用素材，图标是原图、不跟着变灰）",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.listItemSecondary,
                color = LocalSmartisanColors.current.textSecondary,
            )
            SmartisanIconSlider(
                value = 0.2f,
                onValueChange = {},
                enabled = false,
                leading = {
                    SmartisanSliderIcon(
                        res = SmartisanDrawables.VolumeSmall,
                        contentDescription = "小音量",
                        size = 26.dp,
                    )
                },
                trailing = {
                    SmartisanSliderIcon(
                        res = SmartisanDrawables.VolumeHigh,
                        contentDescription = "大音量",
                        size = 26.dp,
                    )
                },
            )
        }
    }
}

/** 数字滚轮：原版 `SmartisanNumberPicker` / `SmartisanNumberPickerEx`。 */
@Composable
private fun NumberPickerSection() {
    var minute by remember { mutableIntStateOf(30) }
    SampleSectionHeader("数字滚轮（NumberPicker）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmartisanNumberPicker(
                    value = minute,
                    onValueChange = { minute = it },
                    minValue = 0,
                    maxValue = 59,
                    formatter = { "%02d".format(it) },
                    unit = "分",
                    modifier = Modifier.size(width = 120.dp, height = 200.dp),
                )
                SmartisanText(
                    text = "循环滚动（原版 setWrapSelectorWheel），当前值 $minute",
                    modifier = Modifier.padding(start = 16.dp),
                    style = LocalSmartisanTypography.current.listItemSecondary,
                    color = LocalSmartisanColors.current.textTertiary,
                )
            }
        }
    }
}

/** 页面指示器：原版 `smartisanos.app.IndicatorView`。 */
@Composable
private fun PageIndicatorSection() {
    var page by remember { mutableIntStateOf(0) }
    val pageCount = 5
    SampleSectionHeader("页面指示器（IndicatorView）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SmartisanPageIndicator(pageCount = pageCount, currentPage = page)
            }
            SmartisanText(
                text = "第 ${page + 1} 页 / 共 $pageCount 页",
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                style = LocalSmartisanTypography.current.caption,
                color = LocalSmartisanColors.current.textTertiary,
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartisanButton(
                    text = "上一页",
                    onClick = { page = (page - 1 + pageCount) % pageCount },
                    style = SmartisanButtonStyle.Neutral,
                )
                SmartisanButton(
                    text = "下一页",
                    onClick = { page = (page + 1) % pageCount },
                    style = SmartisanButtonStyle.Neutral,
                )
            }
        }
    }
}

/** 环形下载进度：原版 `smartisanos.widget.DownloadProgressView`。 */
@Composable
private fun ProgressIndicatorSection() {
    SampleSectionHeader("环形下载进度（DownloadProgressView）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 原版四种状态：1 下载中、2 已暂停、3 失败 / 重试、4 处理中（每帧转 5°）。
                SmartisanProgressIndicator(progress = 42, state = SmartisanProgressState.Download)
                SmartisanProgressIndicator(progress = 42, state = SmartisanProgressState.Pause)
                SmartisanProgressIndicator(progress = 42, state = SmartisanProgressState.Retry)
                SmartisanProgressIndicator(progress = 42, state = SmartisanProgressState.Processing)
            }
            SmartisanText(
                text = "下载中 / 已暂停 / 失败 / 处理中（状态切换时图标 300ms 淡入淡出）",
                modifier = Modifier.padding(top = 12.dp),
                style = LocalSmartisanTypography.current.caption,
                color = LocalSmartisanColors.current.textTertiary,
            )
        }
    }
}

/** 轻量提示条：原版 `smartisanos.widget.TipsView`。 */
@Composable
private fun TipsSection() {
    SampleSectionHeader("轻量提示条（TipsView）")
    SmartisanGroup {
        Column(Modifier.fillMaxWidth()) {
            SmartisanTips("同步后会覆盖本地内容，请先确认。")
            SmartisanTips(
                "这是一条比较长的提示，用来演示原版 onLayout 的行为：文字超过一行时自动从居中改为左对齐，" +
                    "与原版 setGravity(getLineCount() > 1 ? LEFT : CENTER) 完全一致。",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * 两栏预览选项：原版 `PreviewOptionsCheckView`（我们的示例用锤子音乐的列表 / 网格图标当预览图）。
 */
@Composable
private fun PreviewOptionsSection() {
    var checked by remember { mutableIntStateOf(0) }
    var enabled by remember { mutableStateOf(true) }
    SampleSectionHeader("两栏预览选项（PreviewOptionsCheckView）")
    SmartisanPreviewOptions(
        left =
            SmartisanPreviewOption(
                previewRes = SmartisanDrawables.IconAlbumSwitchList,
                title = "列表",
            ),
        right =
            SmartisanPreviewOption(
                previewRes = SmartisanDrawables.IconAlbumSwitchGrid,
                title = "网格",
            ),
        checkedIndex = checked,
        onCheckedChange = { checked = it },
        headTitle = "预览样式",
        enabled = enabled,
    )
    SmartisanPreviewOptions(
        left = SmartisanPreviewOption(previewRes = SmartisanDrawables.IconAlbumSwitchList),
        right = SmartisanPreviewOption(previewRes = SmartisanDrawables.IconAlbumSwitchGrid),
        checkedIndex = SmartisanPreviewOptionsInvalid,
        onCheckedChange = { checked = it },
        headTitle = "没有标题、两栏都不选（INVALID = -1）",
        enabled = enabled,
    )
    SmartisanRowDivider()
    SmartisanText(
        text = "单独用单元格（原版 PreviewOptionView 可独立使用）：",
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
        style = LocalSmartisanTypography.current.caption,
        color = LocalSmartisanColors.current.textTertiary,
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SmartisanPreviewOptionCell(
            option = SmartisanPreviewOption(SmartisanDrawables.IconAlbumSwitchList, "未选中"),
            checked = false,
        )
        SmartisanPreviewOptionCell(
            option = SmartisanPreviewOption(SmartisanDrawables.IconAlbumSwitchGrid, "选中"),
            checked = true,
        )
    }
    SmartisanRowDivider()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmartisanText(
            text = "当前选中：${if (checked == 0) "列表" else "网格"}",
            modifier = Modifier.weight(1f),
            style = LocalSmartisanTypography.current.listItemSecondary,
            color = LocalSmartisanColors.current.textTertiary,
        )
        SmartisanText(
            text = "禁用",
            style = LocalSmartisanTypography.current.listItemSecondary,
        )
        SmartisanSwitch(checked = !enabled, onCheckedChange = { enabled = !it })
    }
    SmartisanText(
        text = "点已经选中的那一栏不会触发回调（原版 if (changed)）；禁用时预览图、标题、角标一起走禁用态。",
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        style = LocalSmartisanTypography.current.caption,
        color = LocalSmartisanColors.current.textTertiary,
    )
}

