package cc.wuersan008.smartisanx.icons

import androidx.compose.ui.graphics.vector.ImageVector

/** 播放控制类图标，对应锤子音乐播放页与全局播放条。 */
object SmartisanXMediaIcons {
    /** 播放。 */
    val Play: ImageVector by lazy {
        smartisanIcon("SmartisanX.Play", "M8 5 L19 12 L8 19 Z", filled = true)
    }

    /** 暂停。 */
    val Pause: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Pause",
            "M8 5 L11 5 L11 19 L8 19 Z M13 5 L16 5 L16 19 L13 19 Z",
            filled = true,
        )
    }

    /** 下一首。 */
    val Next: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Next",
            listOf("M6 5 L14 12 L6 19 Z", "M16 5 L18.5 5 L18.5 19 L16 19 Z"),
            filled = true,
        )
    }

    /** 上一首。 */
    val Previous: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Previous",
            listOf("M18 5 L10 12 L18 19 Z", "M5.5 5 L8 5 L8 19 L5.5 19 Z"),
            filled = true,
        )
    }

    /** 停止。 */
    val Stop: ImageVector by lazy {
        smartisanIcon("SmartisanX.Stop", "M6.5 6.5 L17.5 6.5 L17.5 17.5 L6.5 17.5 Z", filled = true)
    }

    /** 随机播放。 */
    val Shuffle: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Shuffle",
            listOf(
                "M4 6.5 L8 6.5 L15.5 17.5 L20 17.5",
                "M4 17.5 L8 17.5 L10.5 14",
                "M13.5 10 L15.5 6.5 L20 6.5",
                "M17 3.8 L20 6.5 L17 9.2",
                "M17 14.8 L20 17.5 L17 20.2",
            ),
        )
    }

    /** 循环播放。 */
    val Repeat: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Repeat",
            listOf(
                "M5 9.5 A5 5 0 0 1 10 4.5 L17.5 4.5",
                "M14.8 2 L17.5 4.5 L14.8 7",
                "M19 14.5 A5 5 0 0 1 14 19.5 L6.5 19.5",
                "M9.2 17 L6.5 19.5 L9.2 22",
            ),
        )
    }

    /** 播放队列。 */
    val Queue: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Queue",
            listOf(
                "M4 6.5 L20 6.5",
                "M4 11.5 L20 11.5",
                "M4 16.5 L13 16.5",
                "M17.5 14.5 L17.5 19.5",
                "M15 17 L20 17",
            ),
        )
    }

    /** 音量。 */
    val Volume: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Volume",
            listOf(
                "M4 9.5 L7.5 9.5 L11.5 6 L11.5 18 L7.5 14.5 L4 14.5 Z",
                "M14.5 9.5 A4 4 0 0 1 14.5 14.5",
                "M17 7 A7 7 0 0 1 17 17",
            ),
        )
    }

    /** 静音。 */
    val VolumeMute: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.VolumeMute",
            listOf(
                "M4 9.5 L7.5 9.5 L11.5 6 L11.5 18 L7.5 14.5 L4 14.5 Z",
                "M15 9.5 L20 14.5",
                "M20 9.5 L15 14.5",
            ),
        )
    }

    /** 快进。 */
    val FastForward: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.FastForward",
            listOf("M4 6 L11.5 12 L4 18 Z", "M12 6 L19.5 12 L12 18 Z"),
            filled = true,
        )
    }

    /** 快退。 */
    val Rewind: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Rewind",
            listOf("M20 6 L12.5 12 L20 18 Z", "M12 6 L4.5 12 L12 18 Z"),
            filled = true,
        )
    }

    /** 歌词。 */
    val Lyrics: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Lyrics",
            listOf("M4 6.5 L20 6.5", "M4 11.5 L16 11.5", "M4 16.5 L13 16.5", dotPath(19f, 16.5f, 1.5f)),
        )
    }
}
