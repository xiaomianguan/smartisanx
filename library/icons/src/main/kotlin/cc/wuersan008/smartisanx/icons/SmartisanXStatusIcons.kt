package cc.wuersan008.smartisanx.icons

import androidx.compose.ui.graphics.vector.ImageVector

/** 状态与内容类图标：评分、收藏、定位、主题、提示等。 */
object SmartisanXStatusIcons {
    /** 实心星。 */
    val Star: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Star",
            "M12 2.8 L14.7 8.6 L21 9.5 L16.4 13.9 L17.5 20.2 L12 17.2 L6.5 20.2 L7.6 13.9 L3 9.5 L9.3 8.6 Z",
            filled = true,
        )
    }

    /** 空心星。 */
    val StarOutline: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.StarOutline",
            "M12 2.8 L14.7 8.6 L21 9.5 L16.4 13.9 L17.5 20.2 L12 17.2 L6.5 20.2 L7.6 13.9 L3 9.5 L9.3 8.6 Z",
        )
    }

    /** 实心爱心。 */
    val Heart: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Heart",
            "M12 20.5 C12 20.5 3.5 14.8 3.5 9.2 C3.5 6.4 5.7 4.2 8.4 4.2 " +
                "C10.1 4.2 11.4 5.1 12 6.4 C12.6 5.1 13.9 4.2 15.6 4.2 " +
                "C18.3 4.2 20.5 6.4 20.5 9.2 C20.5 14.8 12 20.5 12 20.5 Z",
            filled = true,
        )
    }

    /** 定位。 */
    val Location: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Location",
            listOf(
                "M12 21 C12 21 18.8 14.6 18.8 9.7 A6.8 6.8 0 1 0 5.2 9.7 C5.2 14.6 12 21 12 21 Z",
                circlePath(12f, 9.7f, 2.4f),
            ),
        )
    }

    /** 浅色模式。 */
    val Sun: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Sun",
            listOf(
                circlePath(12f, 12f, 4.2f),
                "M12 2.5 L12 4.6",
                "M12 19.4 L12 21.5",
                "M2.5 12 L4.6 12",
                "M19.4 12 L21.5 12",
                "M5.3 5.3 L6.8 6.8",
                "M17.2 17.2 L18.7 18.7",
                "M18.7 5.3 L17.2 6.8",
                "M6.8 17.2 L5.3 18.7",
            ),
        )
    }

    /** 深色模式。 */
    val Moon: ImageVector by lazy {
        smartisanIcon("SmartisanX.Moon", "M20 14.4 A8.4 8.4 0 0 1 9.6 4 A8.6 8.6 0 1 0 20 14.4 Z")
    }

    /** 信息。 */
    val Info: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Info",
            listOf(circlePath(12f, 12f, 8.5f), "M12 11 L12 16.5", dotPath(12f, 7.8f, 1f)),
        )
    }

    /** 警告。 */
    val Warning: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Warning",
            listOf("M12 3.6 L21.2 19.8 L2.8 19.8 Z", "M12 9.5 L12 14.5", dotPath(12f, 17f, 1f)),
        )
    }

    /** 上移。 */
    val ArrowUp: ImageVector by lazy {
        smartisanIcon("SmartisanX.ArrowUp", listOf("M12 20 L12 4.5", "M5.5 11 L12 4.5 L18.5 11"))
    }

    /** 下移。 */
    val ArrowDown: ImageVector by lazy {
        smartisanIcon("SmartisanX.ArrowDown", listOf("M12 4 L12 19.5", "M5.5 13 L12 19.5 L18.5 13"))
    }

    /** 复制。 */
    val Copy: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Copy",
            listOf("M9.5 9.5 L9.5 20 L20 20 L20 9.5 Z", "M4.5 15 L4.5 4 L15 4 L15 5.5"),
        )
    }

    /** 日历。 */
    val Calendar: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Calendar",
            listOf(
                "M4.5 6.5 L19.5 6.5 L19.5 20 L4.5 20 Z",
                "M4.5 10.5 L19.5 10.5",
                "M8.5 4 L8.5 7",
                "M15.5 4 L15.5 7",
            ),
        )
    }

    /** 打开链接。 */
    val ExternalLink: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.ExternalLink",
            listOf(
                "M14 4.5 L19.5 4.5 L19.5 10",
                "M19.5 4.5 L11 13",
                "M17 14 L17 19.5 L4.5 19.5 L4.5 7 L10 7",
            ),
        )
    }

    /** 文件夹。 */
    val Folder: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Folder",
            "M3.5 6.5 L9.5 6.5 L11.5 9 L20.5 9 L20.5 19 L3.5 19 Z",
        )
    }
}
