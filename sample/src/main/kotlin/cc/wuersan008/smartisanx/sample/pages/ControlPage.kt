package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgress
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgressIndeterminate
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgressLarge
import cc.wuersan008.smartisanx.ui.control.SmartisanCircleProgressPopup
import cc.wuersan008.smartisanx.ui.control.SmartisanRadioButton
import cc.wuersan008.smartisanx.ui.control.SmartisanRatingBar
import cc.wuersan008.smartisanx.ui.control.SmartisanSelectionMark
import cc.wuersan008.smartisanx.ui.control.SmartisanSpinner
import cc.wuersan008.smartisanx.ui.control.SmartisanSpinnerStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitch
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider

/** 基础控件页：开关、复选框、按钮、评分条。 */
@Composable
fun ControlPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var switchOn by remember { mutableStateOf(true) }
    var switchOff by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(true) }
    var unchecked by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(3) }

    SamplePageScaffold(title = "基础控件", onBack = onBack) {
        SampleSectionHeader("开关")
        // 设置页的开关行都长在白色卡片里：行自己画卡片底图，所以要按位置传 Top / Middle / Bottom。
        SmartisanGroup {
            SmartisanSwitchRow(
                text = "智能音效",
                summary = "整行可点，行内点击只触发一次回调",
                checked = switchOn,
                onCheckedChange = { switchOn = it },
                position = SmartisanGroupRowPosition.Top,
            )
            SmartisanRowDivider()
            SmartisanSwitchRow(
                text = "睡眠定时",
                checked = switchOff,
                onCheckedChange = { switchOff = it },
                position = SmartisanGroupRowPosition.Middle,
            )
            SmartisanRowDivider()
            SmartisanSwitchRow(
                text = "禁用示例",
                checked = true,
                enabled = false,
                onCheckedChange = {},
                position = SmartisanGroupRowPosition.Bottom,
            )
        }
        // 两张卡片直接相邻：按原版插一条 14dp 的分组间距（group_list_item_vertical_gap_layout）。
        SmartisanListVerticalGap()
        SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                // 三个开关横排：开关是定宽的，说明文字必须另起一行 ——
                // 塞在同一行里会被挤成一列竖排的单字。
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SmartisanSwitch(checked = switchOn, onCheckedChange = { switchOn = it })
                    SmartisanSwitch(checked = switchOff, onCheckedChange = { switchOff = it })
                    SmartisanSwitch(checked = true, enabled = false, onCheckedChange = {})
                }
                SmartisanText(
                    text = "从左到右：选中 / 未选中 / 禁用（按住可拖动滑块）",
                    modifier = Modifier.padding(top = 8.dp),
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
        }

        SampleSectionHeader("复选框")
        SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SmartisanCheckbox(checked = checked, onCheckedChange = { checked = it })
                    SmartisanCheckbox(checked = unchecked, onCheckedChange = { unchecked = it })
                    SmartisanCheckbox(checked = true, enabled = false, onCheckedChange = {})
                }
                SmartisanText(
                    text = "从左到右：选中 / 未选中 / 禁用",
                    modifier = Modifier.padding(top = 8.dp),
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
        }

        SampleSectionHeader("评分条")
        SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                SmartisanRatingBar(rating = rating, onRatingChange = { rating = it })
                SmartisanText(
                    text = "当前评分：$rating 星（按住可以连续拖动选分）",
                    modifier = Modifier.padding(top = 8.dp),
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
        }

        SampleSectionHeader("下拉选择（SmartisanSpinner）")
        SpinnerSection()

        SampleSectionHeader("环形进度（CircleProgressView）")
        CircleProgressSection()

        SampleSectionHeader("单选对勾（SelectionMark）")
        SelectionMarkSection()

        // 原版 APK 里直接移植过来的控件，单独成文件，见 OriginalControlSections.kt。
        OriginalControlSections()

        SampleFootnote(
            "开关合并了锤子音乐的 Compose 开关与锤子时钟的两套自定义 View 开关；" +
                "复选框与评分条来自锤子音乐；按钮合并了锤子音乐的红色收缩按钮与锤子天气的操作按钮；" +
                "分段按钮组、计算器按键、数字滚轮、页面指示器、环形下载进度与提示条来自原厂 APK 的" +
                "自定义 View（ButtonTabGroup / SmartisanCalculatorButton / SmartisanNumberPicker / IndicatorView / " +
                "DownloadProgressView / TipsView），以及 framework 的两栏预览选项 PreviewOptionsCheckView。",
        )
    }
}


/** 下拉选择：原版 `SmartisanSpinnerView` 的三种版式。 */
@Composable
private fun SpinnerSection() {
    var dropIndex by remember { mutableIntStateOf(0) }
    var rangeIndex by remember { mutableIntStateOf(2) }
    val options = listOf("每天", "工作日", "仅周末")
    val ranges = listOf("10 分钟", "20 分钟", "30 分钟")
    SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanText("重复", style = LocalSmartisanTypography.current.listItemPrimary)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                SmartisanSpinner(
                    text = options[dropIndex],
                    style = SmartisanSpinnerStyle.Drop,
                    subText = "下拉版式",
                    onClick = { dropIndex = (dropIndex + 1) % options.size },
                )
            }
        }
        SmartisanRowDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanText("提醒间隔", style = LocalSmartisanTypography.current.listItemPrimary)
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
                SmartisanSpinner(
                    text = ranges[rangeIndex],
                    style = SmartisanSpinnerStyle.Range,
                    onPreviousClick = { rangeIndex = (rangeIndex + ranges.size - 1) % ranges.size },
                    onNextClick = { rangeIndex = (rangeIndex + 1) % ranges.size },
                )
            }
        }
    }
}

/** 环形进度：确定进度、不确定进度、大号不确定圈与贴底弹层四种形态。 */
@Composable
private fun CircleProgressSection() {
    var progress by remember { mutableStateOf(0.35f) }
    var popupVisible by remember { mutableStateOf(false) }
    SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanCircleProgress(progress = progress)
            SmartisanCircleProgressIndeterminate()
            SmartisanCircleProgressLarge()
            SmartisanCircleProgressPopup(visible = popupVisible)
        }
        SmartisanText(
            text = "确定进度 ${(progress * 100).toInt()}%（进度 / 不确定 / 大号 / 弹层）",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            style = LocalSmartisanTypography.current.caption,
            color = LocalSmartisanColors.current.textTertiary,
        )
        SmartisanRowDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SmartisanButton(
                text = "推进",
                style = SmartisanButtonStyle.Neutral,
                onClick = { progress = if (progress >= 1f) 0f else progress + 0.15f },
            )
            SmartisanButton(
                text = "弹层进度",
                style = SmartisanButtonStyle.Neutral,
                onClick = { popupVisible = !popupVisible },
            )
        }
    }
}

/** 单选对勾：原版的单选标记是一枚蓝色对勾（选中才画，未选中只占位）。 */
@Composable
private fun SelectionMarkSection() {
    var selected by remember { mutableIntStateOf(0) }
    SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanSelectionMark(selected = true)
            SmartisanSelectionMark(selected = false)
            SmartisanSelectionMark(selected = true, pressed = true)
            SmartisanSelectionMark(selected = true, enabled = false)
        }
        SmartisanText(
            text = "选中 / 未选中（不画）/ 按下 / 禁用",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            style = LocalSmartisanTypography.current.caption,
            color = LocalSmartisanColors.current.textTertiary,
        )
        SmartisanRowDivider()
        listOf("每天 07:30", "工作日 07:30", "仅一次").forEachIndexed { index, label ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { selected = index }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmartisanText(label, Modifier.weight(1f), style = LocalSmartisanTypography.current.listItemPrimary)
                SmartisanRadioButton(selected = selected == index, onClick = { selected = index })
            }
            if (index != 2) SmartisanRowDivider()
        }
    }
}
