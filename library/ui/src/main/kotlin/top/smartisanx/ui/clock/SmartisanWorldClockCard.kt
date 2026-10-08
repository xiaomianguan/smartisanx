package top.smartisanx.ui.clock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.smartisanx.core.interaction.smartisanClickable
import top.smartisanx.core.theme.LocalSmartisanColors
import top.smartisanx.core.theme.LocalSmartisanTypography
import top.smartisanx.core.theme.SmartisanDimens
import top.smartisanx.ui.basic.SmartisanSurface
import top.smartisanx.ui.basic.SmartisanText
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * smartisanx 的世界时钟卡片：小表盘 + 城市名 + 当地日期 + 与本地时差。
 *
 * 复刻自锤子时钟的世界时钟列表行（`WorldClockListView` + `SmallWorldClockView`）：
 * 行左侧是小表盘（原版按当地时间在日间 / 夜间两套位图之间切换，这里改为切换表盘底色），
 * 右侧是城市名、当地日期（带「昨天 / 明天」提示）与相对本地的时间差，最右侧是当地时刻。
 *
 * 时差按 [ZoneId.getRules] 的实时偏移计算，因此夏令时（DST）与半小时时区都能正确显示。
 *
 * ```kotlin
 * val now by produceState(Instant.now()) {
 *     while (true) {
 *         value = Instant.now()
 *         withFrameNanos { }
 *     }
 * }
 * SmartisanWorldClockCard(city = "纽约", zone = ZoneId.of("America/New_York"), now = now)
 * ```
 *
 * @param city 城市名（或时区显示名）。
 * @param zone 该城市所在时区。
 * @param modifier 外部修饰符。
 * @param now 用于计算当地时间的时刻，默认当前时刻；组件不做计时，需要走时请自行驱动。
 * @param localZone 本地时区，默认系统时区。
 * @param showSecondHand 是否在小表盘上绘制秒针。
 * @param onClick 可选的点击回调；为 null 时该行不可点击。
 */
@Composable
fun SmartisanWorldClockCard(
    city: String,
    zone: ZoneId,
    modifier: Modifier = Modifier,
    now: Instant = Instant.now(),
    localZone: ZoneId = ZoneId.systemDefault(),
    showSecondHand: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    val cityTime = now.atZone(zone)
    val cityDate = cityTime.toLocalDate()
    val localDate = now.atZone(localZone).toLocalDate()
    val night = cityTime.hour >= NightStartHour || cityTime.hour < NightEndHour
    val dialColor = if (night) colors.surfaceRaised else colors.surface

    val dayDelta = ChronoUnit.DAYS.between(localDate, cityDate).toInt()
    val dateText = dayHint(dayDelta) +
        cityDate.monthValue.toString() + "月" +
        cityDate.dayOfMonth.toString() + "日 " +
        WeekdayLabels[cityDate.dayOfWeek.value - 1]

    val diffMinutes =
        (zone.rules.getOffset(now).totalSeconds - localZone.rules.getOffset(now).totalSeconds) / 60
    val offsetText = offsetText(diffMinutes)

    val timeText = buildString {
        append(cityTime.hour.toString().padStart(2, '0'))
        append(':')
        append(cityTime.minute.toString().padStart(2, '0'))
        if (showSecondHand) {
            append(':')
            append(cityTime.second.toString().padStart(2, '0'))
        }
    }

    val clickModifier = if (onClick != null) {
        Modifier.smartisanClickable(onClick = onClick)
    } else {
        Modifier
    }

    SmartisanSurface(modifier = modifier.fillMaxWidth(), color = colors.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SmartisanDimens.ListItemHeight)
                .then(clickModifier)
                .padding(
                    horizontal = SmartisanDimens.ListItemHorizontalMargin,
                    vertical = RowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmartisanCompactClock(
                hour = cityTime.hour,
                minute = cityTime.minute,
                second = cityTime.second,
                showSecondHand = showSecondHand,
                size = ClockSize,
                dialColor = dialColor,
            )
            Spacer(modifier = Modifier.width(SmartisanDimens.GroupRowPadding))
            Column(modifier = Modifier.weight(1f)) {
                SmartisanText(
                    text = city,
                    style = typography.listItemPrimary,
                    color = colors.textPrimary,
                    maxLines = 1,
                )
                SmartisanText(
                    text = dateText,
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
                SmartisanText(
                    text = offsetText,
                    style = typography.caption,
                    color = colors.textTertiary,
                    maxLines = 1,
                )
            }
            SmartisanText(
                text = timeText,
                style = typography.numeric.copy(
                    fontSize = TimeTextSize,
                    fontWeight = FontWeight.Light,
                    color = colors.textPrimary,
                ),
                maxLines = 1,
            )
        }
    }
}

/** 「昨天 / 今天 / 明天 / N 天前」提示，空字符串表示就是本地当天。 */
private fun dayHint(dayDelta: Int): String = when {
    dayDelta == 0 -> ""
    dayDelta == 1 -> "明天 "
    dayDelta == -1 -> "昨天 "
    dayDelta > 1 -> "${dayDelta} 天后 "
    else -> "${-dayDelta} 天前 "
}

/** 与本地时差的文案，按小时 / 分钟拆分，支持半小时时区。 */
private fun offsetText(diffMinutes: Int): String {
    if (diffMinutes == 0) return "与本地时间相同"
    val direction = if (diffMinutes > 0) "快" else "慢"
    val absolute = abs(diffMinutes)
    val hours = absolute / 60
    val minutes = absolute % 60
    return buildString {
        append("比本地").append(direction)
        if (hours > 0) append(' ').append(hours).append(" 小时")
        if (minutes > 0) append(' ').append(minutes).append(" 分钟")
    }
}

/** 周一到周日的短名，与 [SmartisanWeekdayPicker] 的默认标签一致。 */
private val WeekdayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

/** 小表盘直径，原版世界时钟行表盘为 60dp，列表行内使用 40dp 更紧凑。 */
private val ClockSize = 40.dp

/** 行内上下留白。 */
private val RowVerticalPadding = 8.dp

/** 右侧时刻字号。 */
private val TimeTextSize = 20.sp

/** 夜间表盘起始小时，对应原版 `NIGHT_START_HOUR`。 */
private const val NightStartHour = 18

/** 夜间表盘结束小时，对应原版 `NIGHT_END_HOUR`。 */
private const val NightEndHour = 6

