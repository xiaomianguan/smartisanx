package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 锤子时钟的表盘与指针资源索引。
 *
 * 来源：SmartisanClock-Revived（坚果 R2 原厂 Clock 7.1.1 的资源还原）。
 * 大表盘在原版里是一块固定 360×400dp 的画布，这些位图按原尺寸叠放即可还原。
 */
object SmartisanClockDrawables {
    // ---- 大表盘 ----
    /** 日间表盘底。 */
    @DrawableRes val Face = R.drawable.blank_clock

    /** 夜间表盘底（原版用另一张图，不是简单反色）。 */
    @DrawableRes val FaceNight = R.drawable.blank_circle

    /** 刻度层。 */
    @DrawableRes val Degree = R.drawable.degree

    /** 顶部「12」。 */
    @DrawableRes val NumeralTopTwelve = R.drawable.d12

    /** 顶部「12」夜间变体。 */
    @DrawableRes val NumeralTopTwelveNight = R.drawable.d12_white

    /** 右侧「3」。 */
    @DrawableRes val NumeralRightThree = R.drawable.d3

    /** 右侧「3」夜间变体。 */
    @DrawableRes val NumeralRightThreeNight = R.drawable.d3_white

    /** 顶部「60」（24 小时制）。 */
    @DrawableRes val NumeralTopSixty = R.drawable.d60

    /** 右侧「15」（24 小时制）。 */
    @DrawableRes val NumeralRightFifteen = R.drawable.d15

    /** 闹钟「耳朵」左。 */
    @DrawableRes val EarLeft = R.drawable.big_left

    /** 闹钟「耳朵」右。 */
    @DrawableRes val EarRight = R.drawable.big_right

    // ---- 指针 ----
    @DrawableRes val HourHand = R.drawable.hour_hand
    @DrawableRes val MinuteHand = R.drawable.minute_hand
    @DrawableRes val SecondHand = R.drawable.sec_hand
    @DrawableRes val HourHandShadow = R.drawable.hour_hand_shadow
    @DrawableRes val MinuteHandShadow = R.drawable.minute_hand_shadow
    @DrawableRes val SecondHandShadow = R.drawable.sec_hand_shadow
    @DrawableRes val HandCenter = R.drawable.hand_center
    @DrawableRes val HandCenterMiddle = R.drawable.hand_center_middle

    @DrawableRes val HourHandBlack = R.drawable.hour_hand_black
    @DrawableRes val MinuteHandBlack = R.drawable.minute_hand_black
    @DrawableRes val HandCenterBlack = R.drawable.hand_center_black

    @DrawableRes val AlarmPointer = R.drawable.alarmpointer
    @DrawableRes val AlarmPointerShadow = R.drawable.alarmpointer_shadow
    @DrawableRes val Dot = R.drawable.icon_dot

    // ---- 小表盘（世界时钟 / 列表行） ----
    @DrawableRes val SmallFace = R.drawable.small_blank_clock

    /** 小表盘夜间底图。 */
    @DrawableRes val SmallFaceBlack = R.drawable.small_blank_clock_black
    @DrawableRes val SmallHourHand = R.drawable.small_hour_hand
    @DrawableRes val SmallMinuteHand = R.drawable.small_minute_hand
    @DrawableRes val SmallSecondHand = R.drawable.small_sec_hand
    @DrawableRes val SmallHourHandShadow = R.drawable.small_hour_hand_shadow
    @DrawableRes val SmallMinuteHandShadow = R.drawable.small_minute_hand_shadow
    @DrawableRes val SmallSecondHandShadow = R.drawable.small_sec_hand_shadow
    @DrawableRes val SmallHandCenter = R.drawable.small_hand_center
    @DrawableRes val SmallHandCenterMiddle = R.drawable.small_hand_center_middle
    @DrawableRes val SmallHourHandBlack = R.drawable.small_hour_hand_black
    @DrawableRes val SmallMinuteHandBlack = R.drawable.small_minute_hand_black
    @DrawableRes val SmallHandCenterBlack = R.drawable.small_hand_center_black

    // ---- 响铃卡片时钟（CompactAlarmClockView） ----
    @DrawableRes val RingingFace = R.drawable.alarm_ringing_clock_face
    @DrawableRes val RingingHourHand = R.drawable.alarm_ringing_hour_hand
    @DrawableRes val RingingMinuteHand = R.drawable.alarm_ringing_minute_hand
    @DrawableRes val RingingSecondHand = R.drawable.alarm_ringing_second_hand
    @DrawableRes val RingingHandCenter = R.drawable.alarm_ringing_hand_center
    @DrawableRes val RingingHandCenterMiddle = R.drawable.alarm_ringing_hand_center_middle
    @DrawableRes val RingingAlarmHand = R.drawable.alarm_ringing_alarm_hand
    @DrawableRes val RingingHourShadow = R.drawable.alarm_ringing_hour_shadow
    @DrawableRes val RingingMinuteShadow = R.drawable.alarm_ringing_minute_shadow
    @DrawableRes val RingingSecondShadow = R.drawable.alarm_ringing_second_shadow
    @DrawableRes val RingingAlarmShadow = R.drawable.alarm_ringing_alarm_shadow
    @DrawableRes val RingingNumeralTwelve = R.drawable.alarm_ringing_clock_12
    @DrawableRes val RingingNumeralThree = R.drawable.alarm_ringing_clock_3

    /** 响铃「耳朵」的四帧循环动画。 */
    val RingingEarFrames: List<Int> =
        listOf(
            R.drawable.alarm_ringing_ear_0001,
            R.drawable.alarm_ringing_ear_0002,
            R.drawable.alarm_ringing_ear_0003,
            R.drawable.alarm_ringing_ear_0004,
        )

    // ---- 时间滚轮 ----
    /** 滚轮列背景。 */
    @DrawableRes val TimePickerColumn = R.drawable.alarm_editor_timepicker_2

    /** 滚轮选中列背景。 */
    @DrawableRes val TimePickerColumnSelected = R.drawable.alarm_editor_timepicker_3
}
