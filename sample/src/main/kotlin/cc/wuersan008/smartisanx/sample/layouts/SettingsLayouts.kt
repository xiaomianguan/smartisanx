package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.control.SmartisanIconSlider
import cc.wuersan008.smartisanx.ui.control.SmartisanRadioRow
import cc.wuersan008.smartisanx.ui.control.SmartisanSliderIcon
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.LocalSmartisanContentBottomInset
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction

/**
 * 锤子设置「声音与振动」页。
 *
 * 对应原版 `SettingsSmartisan.apk` 的设置详情页版式（`SettingSubItem` 系列布局）：
 * 分组标题 + 一张分组卡片（卡片底图由每行自己的 9-patch 提供，行间插 [SmartisanRowDivider]），
 * 分组之间用 [SmartisanListVerticalGap] 留出 14dp —— 卡片投影正好落在这段留白里。
 *
 * 本页把设置页最常用的四种行都摆了一遍：
 *
 * | 本页 | 组件 | 原版出处 |
 * | --- | --- | --- |
 * | 开关行 | [SmartisanSwitchRow] | `switch_ex_*` 位图开关，行高 60dp |
 * | 图标滑块 | [SmartisanIconSlider] + [SmartisanSliderIcon] | 音量行：左侧音量图标 + 右侧滑条 |
 * | 单选行 | [SmartisanRadioRow] | `selector_check_box_red` 的红色实心圆 |
 * | 跳转行 | [SmartisanGroupItem] + 箭头 | `secletor_setting_item_arrow` 小箭头 |
 */
@Composable
fun SettingsSoundLayout(onBack: () -> Unit) {
    var vibrateOnSilent by remember { mutableStateOf(true) }
    var vibrateOnTap by remember { mutableStateOf(false) }
    var keyTone by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.6f) }
    var ringtone by remember { mutableIntStateOf(0) }
    val ringtones = remember { listOf("清澈", "铃音", "静音") }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        // 内容会滚动：内容区一直画到屏幕最底下，导航栏留白补在滚动内容的末尾。
        contentScrollable = true,
        titleBar = {
            SmartisanTitleBar(
                title = "声音与振动",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    // 留白加在滚动修饰符之后：它属于滚动内容（列表能滚到导航栏底下，
                    // 滚到底时最后一行还在导航栏之上）。
                    .padding(bottom = LocalSmartisanContentBottomInset.current),
        ) {
            SmartisanSectionTitle("铃声与振动")
            SmartisanGroup {
                SmartisanSwitchRow(
                    text = "静音时振动",
                    checked = vibrateOnSilent,
                    onCheckedChange = { vibrateOnSilent = it },
                    position = SmartisanGroupRowPosition.Top,
                )
                SmartisanSwitchRow(
                    text = "触摸时振动",
                    checked = vibrateOnTap,
                    onCheckedChange = { vibrateOnTap = it },
                    position = SmartisanGroupRowPosition.Middle,
                )
                SmartisanSwitchRow(
                    text = "拨号按键音",
                    checked = keyTone,
                    onCheckedChange = { keyTone = it },
                    position = SmartisanGroupRowPosition.Bottom,
                )
            }
            SmartisanListVerticalGap()
            SmartisanSectionTitle("音量")
            SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
                SmartisanIconSlider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                    leading = {
                        // 原版音量行的图标：`VolumeHigh` / `VolumeMiddle` / `VolumeMute` 三档。
                        SmartisanSliderIcon(
                            res =
                                when {
                                    volume <= 0.02f -> SmartisanDrawables.VolumeMute
                                    volume < 0.5f -> SmartisanDrawables.VolumeMiddle
                                    else -> SmartisanDrawables.VolumeHigh
                                },
                            contentDescription = "音量",
                        )
                    },
                )
            }
            SmartisanListVerticalGap()
            SmartisanSectionTitle("默认铃声")
            SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
                ringtones.forEachIndexed { index, name ->
                    if (index > 0) {
                        SmartisanRowDivider()
                    }
                    SmartisanRadioRow(
                        text = name,
                        selected = ringtone == index,
                        onClick = { ringtone = index },
                    )
                }
            }
            SmartisanListVerticalGap()
            SmartisanSectionTitle("更多设置")
            SmartisanGroup {
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Single,
                    title = "免打扰",
                    summary = "定时静音、允许的联系人",
                    trailing = {
                        SmartisanIcon(
                            // 母版 `list_content_right_subtitle_arrow` 里的箭头。
                            res = SmartisanDrawables.ListItemArrow,
                            contentDescription = null,
                        )
                    },
                    onClick = {},
                )
            }
            SmartisanListVerticalGap()
        }
    }
}
