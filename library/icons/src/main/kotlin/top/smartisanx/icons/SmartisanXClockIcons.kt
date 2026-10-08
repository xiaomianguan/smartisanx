package top.smartisanx.icons

import androidx.compose.ui.graphics.vector.ImageVector

/** 时钟、闹钟、计时类图标，对应锤子时钟的四个页面。 */
object SmartisanXClockIcons {
    /** 世界时钟 / 表盘。 */
    val Clock: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Clock",
            listOf(circlePath(12f, 12f, 8.5f), "M12 6.8 L12 12.2 L16 14.4"),
        )
    }

    /** 闹钟。 */
    val Alarm: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Alarm",
            listOf(
                circlePath(12f, 13.2f, 7.4f),
                "M12 9 L12 13.4 L15 15.2",
                "M4.2 5.4 L7 2.8",
                "M19.8 5.4 L17 2.8",
            ),
        )
    }

    /** 秒表。 */
    val Stopwatch: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Stopwatch",
            listOf(
                circlePath(12f, 13.6f, 7.2f),
                "M12 9.6 L12 13.8 L15 15.6",
                "M9.6 2.8 L14.4 2.8",
                "M12 2.8 L12 6.4",
            ),
        )
    }

    /** 计时器（沙漏）。 */
    val Hourglass: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Hourglass",
            "M6.5 3.5 L17.5 3.5 L12 11 L17.5 20.5 L6.5 20.5 L12 11 Z",
        )
    }

    /** 世界时钟（地球）。 */
    val Globe: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Globe",
            listOf(
                circlePath(12f, 12f, 8.5f),
                "M3.5 12 L20.5 12",
                "M12 3.5 A5.5 8.5 0 1 0 12 20.5 A5.5 8.5 0 1 0 12 3.5 Z",
            ),
        )
    }

    /** 闹铃。 */
    val Bell: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Bell",
            listOf(
                "M12 3.2 A5.8 5.8 0 0 1 17.8 9 L17.8 14 L20 17 L4 17 L6.2 14 L6.2 9 A5.8 5.8 0 0 1 12 3.2 Z",
                "M9.8 19.5 A2.2 2.2 0 0 0 14.2 19.5",
            ),
        )
    }

    /** 睡眠定时。 */
    val SleepTimer: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.SleepTimer",
            listOf(
                "M20 14.4 A8.4 8.4 0 0 1 9.6 4 A8.6 8.6 0 1 0 20 14.4 Z",
                "M14.5 3 L19.5 3 L14.5 7.5 L19.5 7.5",
            ),
        )
    }

    /** 屏幕常亮。 */
    val KeepScreenOn: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.KeepScreenOn",
            listOf("M7 3.5 L17 3.5 L17 20.5 L7 20.5 Z", "M10.5 17.8 L13.5 17.8"),
        )
    }
}
