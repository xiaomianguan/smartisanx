package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.clock.SmartisanAnalogClock
import cc.wuersan008.smartisanx.ui.clock.SmartisanWorldClockCard
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanBottomBarItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanLetterSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.list.SmartisanLetterIndexBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/** 时钟四个标签页的底部栏（闹钟 / 秒表 / 计时器 / 世界时钟）。 */
@Composable
private fun ClockBottomBar(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val items =
        remember {
            listOf(
                SmartisanBottomBarItem(label = "闹钟", iconRes = SmartisanOriginalIcons.TabAlarm),
                SmartisanBottomBarItem(label = "秒表", iconRes = SmartisanOriginalIcons.TabStopwatch),
                SmartisanBottomBarItem(label = "计时器", iconRes = SmartisanOriginalIcons.TabTimer),
                SmartisanBottomBarItem(label = "世界时钟", iconRes = SmartisanOriginalIcons.TabWorldClock),
            )
        }
    SmartisanBottomBar(items = items, selectedIndex = selectedIndex, onSelected = onSelected)
}

/**
 * 锤子时钟「闹钟」页。
 *
 * 对应原版 `ClockSmartisan.apk` 的 `fragment_alarm.xml`：
 *
 * | 本页 | 原版出处 |
 * | --- | --- |
 * | 顶部表盘 | `top_clock_holder` → `layout_for_null_clock.xml` → `layout_for_middle_clock.xml`，是一块带铃铛装饰的机械表；库里的 [SmartisanAnalogClock] 用 `showEars = true` 就是这个形态 |
 * | 表盘下方说明 | `alarm_clock_text`（18sp 加粗、`#b3000000`） |
 * | 列表行 | 时间 + 重复日 + 标签 + 开关，用 [SmartisanSwitchRow]（60dp 行高，开关是原版 `switch_ex_*`） |
 * | 底部标签栏 | `selector_tab_alarm` 等四张 `selector_tab_*`，栏高 54dp |
 *
 * 表盘每秒刷新一次时间，能看出组件是活的（原版秒针是连续走的）。
 */
@Composable
fun ClockAlarmLayout(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var alarms by remember { mutableStateOf(SampleAlarms) }
    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            delay(1000)
        }
    }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "闹钟",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.AddTitleBar,
                            contentDescription = "新建闹钟",
                            onClick = {},
                        ),
                    ),
            )
        },
        bottomBar = { ClockBottomBar(selectedIndex = tab, onSelected = { tab = it }) },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                // 原版这块表就是「带耳朵的机械表」，所以 showEars = true。
                SmartisanAnalogClock(time = time, showEars = true, size = 220.dp)
            }
            SmartisanGroup {
                alarms.forEachIndexed { index, alarm ->
                    SmartisanSwitchRow(
                        text = alarm.time,
                        summary = "${alarm.repeat} · ${alarm.label}",
                        checked = alarm.enabled,
                        onCheckedChange = { checked ->
                            alarms =
                                alarms.mapIndexed { i, item ->
                                    if (i == index) item.copy(enabled = checked) else item
                                }
                        },
                        position =
                            when (index) {
                                0 -> SmartisanGroupRowPosition.Top
                                alarms.lastIndex -> SmartisanGroupRowPosition.Bottom
                                else -> SmartisanGroupRowPosition.Middle
                            },
                    )
                }
            }
            SmartisanListVerticalGap()
        }
    }
}

/** 世界时钟列表里的两种条目（组标题 / 城市）。 */
private sealed interface WorldClockRow {
    data class Section(val letter: Char) : WorldClockRow

    data class City(val city: String, val zone: ZoneId) : WorldClockRow
}

/**
 * 锤子时钟「世界时钟」页。
 *
 * 对应原版 `layout_worldclock_listview.xml` + `layout_for_small_world_clock.xml`：
 * 每一行 60dp 高，左边是城市名（18sp 加粗、左缩进 24dp），中间一块 60dp 的小表盘，
 * 右边是时间（20sp 加粗、原版红 `#d5544b`）+ 上下午（12sp），下面一行是日期与相对时间（12sp）。
 * 这一整套正好就是库里的 [SmartisanWorldClockCard]，直接用它即可。
 *
 * 右侧的 A–Z 索引条与「我的音乐」页同源（原版世界时钟用的是 `QuickBarEx`）。
 */
@Composable
fun ClockWorldLayout(onBack: () -> Unit) {
    var tab by remember { mutableIntStateOf(3) }
    var now by remember { mutableStateOf(Instant.now()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()
            delay(1000)
        }
    }
    // 时区只在构造列表时解析一次，避免每秒重算。
    val rows =
        remember {
            buildList {
                SampleCitySections.forEach { (letter, cities) ->
                    add(WorldClockRow.Section(letter))
                    cities.forEach { add(WorldClockRow.City(it.city, ZoneId.of(it.zone))) }
                }
            }
        }
    val letterIndex =
        remember {
            rows.withIndex()
                .filter { it.value is WorldClockRow.Section }
                .associate { (index, row) -> (row as WorldClockRow.Section).letter to index }
        }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "世界时钟",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.AddTitleBar,
                            contentDescription = "添加城市",
                            onClick = {},
                        ),
                    ),
            )
        },
        bottomBar = { ClockBottomBar(selectedIndex = tab, onSelected = { tab = it }) },
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(rows) { row ->
                    when (row) {
                        is WorldClockRow.Section ->
                            SmartisanLetterSectionTitle(text = row.letter.toString())
                        is WorldClockRow.City ->
                            SmartisanWorldClockCard(
                                city = row.city,
                                zone = row.zone,
                                now = now,
                                onClick = {},
                                // 右侧时刻给压在列表上的 A–Z 索引条让位。
                                endInset = SmartisanDimens.ListIndexContentInset,
                            )
                    }
                }
            }
            SmartisanLetterIndexBar(
                letters = letterIndex.keys.toList(),
                onLetterSelected = { letter ->
                    letterIndex[letter]?.let { index ->
                        scope.launch { listState.animateScrollToItem(index) }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
