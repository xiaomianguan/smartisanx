package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 锤子时钟的计时器、秒表与列表资源索引。
 *
 * 来源：SmartisanClock-Revived（Clock 7.1.1 与 6.8.0 的资源还原）。
 */
object SmartisanTimerDrawables {
    // ---- 7.1.1 横向卡尺计时器 ----
    /** 卡尺外框。 */
    @DrawableRes val CaliperFrame = R.drawable.timer_caliper_bg1

    /** 可用区间底色。 */
    @DrawableRes val CaliperFieldEnabled = R.drawable.timer_caliper_bg3

    /** 禁用区间底色。 */
    @DrawableRes val CaliperFieldDisabled = R.drawable.timer_caliper_bg2_disable

    /** 刻度图（一分钟宽度 = 一张图宽度）。 */
    @DrawableRes val CaliperScale = R.drawable.timer_scale_normal

    /** 禁用态刻度图。 */
    @DrawableRes val CaliperScaleDisabled = R.drawable.timer_scale_disable

    /** 刻度前导空白。 */
    @DrawableRes val CaliperBlank = R.drawable.timer_blank

    // ---- 6.8.0 竖向拉环计时器 ----
    /** 拉环标尺底图（按 400dpi 参考图等比缩放）。 */
    @DrawableRes val PullRingRuler = R.drawable.timer_680_ruler

    /** 拉环的 30 帧序列，索引 0 为最底、29 为最顶。 */
    val PullRingFrames: List<Int> =
        listOf(
            R.drawable.timer_680_loop_0001,
            R.drawable.timer_680_loop_0002,
            R.drawable.timer_680_loop_0003,
            R.drawable.timer_680_loop_0004,
            R.drawable.timer_680_loop_0005,
            R.drawable.timer_680_loop_0006,
            R.drawable.timer_680_loop_0007,
            R.drawable.timer_680_loop_0008,
            R.drawable.timer_680_loop_0009,
            R.drawable.timer_680_loop_0010,
            R.drawable.timer_680_loop_0011,
            R.drawable.timer_680_loop_0012,
            R.drawable.timer_680_loop_0013,
            R.drawable.timer_680_loop_0014,
            R.drawable.timer_680_loop_0015,
            R.drawable.timer_680_loop_0016,
            R.drawable.timer_680_loop_0017,
            R.drawable.timer_680_loop_0018,
            R.drawable.timer_680_loop_0019,
            R.drawable.timer_680_loop_0020,
            R.drawable.timer_680_loop_0021,
            R.drawable.timer_680_loop_0022,
            R.drawable.timer_680_loop_0023,
            R.drawable.timer_680_loop_0024,
            R.drawable.timer_680_loop_0025,
            R.drawable.timer_680_loop_0026,
            R.drawable.timer_680_loop_0027,
            R.drawable.timer_680_loop_0028,
            R.drawable.timer_680_loop_0029,
            R.drawable.timer_680_loop_0030,
        )

    @DrawableRes val PullRingPlay = R.drawable.timer_680_button_selector_play
    @DrawableRes val PullRingStop = R.drawable.timer_680_button_selector_stop
    @DrawableRes val PullRingReset = R.drawable.timer_680_button_selector_reset

    // ---- 秒表 ----
    @DrawableRes val StopwatchPlay = R.drawable.selector_stopwatch_play
    @DrawableRes val StopwatchStop = R.drawable.selector_stopwatch_stop
    @DrawableRes val StopwatchReset = R.drawable.selector_stopwatch_reset
    @DrawableRes val StopwatchLightOn = R.drawable.selector_stopwatch_light_on
    @DrawableRes val StopwatchLightOff = R.drawable.selector_stopwatch_light_off
    @DrawableRes val StopwatchDot = R.drawable.selector_stopwatch_dot

    // ---- 重复日开关（SwitchEx） ----
    @DrawableRes val RepeatSwitchFrame = R.drawable.alarm_repeat_switch_frame
    @DrawableRes val RepeatSwitchFramePressed = R.drawable.alarm_repeat_switch_frame_pressed
    @DrawableRes val RepeatSwitchKnob = R.drawable.alarm_repeat_switch_unpressed
    @DrawableRes val RepeatSwitchKnobPressed = R.drawable.alarm_repeat_switch_pressed
    @DrawableRes val RepeatSwitchMask = R.drawable.alarm_repeat_switch_mask
    @DrawableRes val RepeatSwitchBottomGreen = R.drawable.alarm_repeat_switch_bottom_green

    // ---- 列表行 ----
    @DrawableRes val AlarmRowBackground = R.drawable.alarm_list_item_bg
    @DrawableRes val AlarmRowPressedBackground = R.drawable.alarm_bg_down

    // ---- 时钟底部标签栏 ----
    @DrawableRes val TabAlarm = R.drawable.selector_tab_alarm
    @DrawableRes val TabStopwatch = R.drawable.selector_tab_stopwatch
    @DrawableRes val TabTimer = R.drawable.selector_tab_timer
    @DrawableRes val TabWorldClock = R.drawable.selector_tab_worldclock
    @DrawableRes val Divider = R.drawable.clock_divider
}
