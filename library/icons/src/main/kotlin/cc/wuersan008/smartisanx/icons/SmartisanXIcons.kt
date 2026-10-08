package cc.wuersan008.smartisanx.icons

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * smartisanx 通用图标。
 *
 * 图标风格取自三个复刻项目共用的锤子原版资源：细线、圆角端点、24×24 网格。
 * 颜色由使用处的 `tint` 决定，不打包任何位图。
 */
object SmartisanXIcons {
    /** 返回。 */
    val Back: ImageVector by lazy { smartisanIcon("SmartisanX.Back", "M15.5 4.5 L8 12 L15.5 19.5") }

    /** 向右箭头。 */
    val ChevronRight: ImageVector by lazy { smartisanIcon("SmartisanX.ChevronRight", "M8.5 4.5 L16 12 L8.5 19.5") }

    /** 向下箭头。 */
    val ChevronDown: ImageVector by lazy { smartisanIcon("SmartisanX.ChevronDown", "M4.5 8.5 L12 16 L19.5 8.5") }

    /** 向上箭头。 */
    val ChevronUp: ImageVector by lazy { smartisanIcon("SmartisanX.ChevronUp", "M4.5 15.5 L12 8 L19.5 15.5") }

    /** 关闭。 */
    val Close: ImageVector by lazy {
        smartisanIcon("SmartisanX.Close", listOf("M6 6 L18 18", "M18 6 L6 18"))
    }

    /** 勾选。 */
    val Check: ImageVector by lazy { smartisanIcon("SmartisanX.Check", "M5 12.5 L9.5 17 L19 7") }

    /** 新增。 */
    val Add: ImageVector by lazy {
        smartisanIcon("SmartisanX.Add", listOf("M12 5 L12 19", "M5 12 L19 12"))
    }

    /** 减少。 */
    val Remove: ImageVector by lazy { smartisanIcon("SmartisanX.Remove", "M5 12 L19 12") }

    /** 搜索。 */
    val Search: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Search",
            listOf(
                "M10.5 3.5 A7 7 0 1 0 10.5 17.5 A7 7 0 1 0 10.5 3.5 Z",
                "M15.6 15.6 L20.5 20.5",
            ),
        )
    }

    /** 更多（竖排三点）。 */
    val More: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.More",
            listOf(dotPath(12f, 5.6f, 1.5f), dotPath(12f, 12f, 1.5f), dotPath(12f, 18.4f, 1.5f)),
            filled = true,
        )
    }

    /** 菜单（三条横线）。 */
    val Menu: ImageVector by lazy {
        smartisanIcon("SmartisanX.Menu", listOf("M4 7 L20 7", "M4 12 L20 12", "M4 17 L20 17"))
    }

    /** 删除。 */
    val Delete: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Delete",
            listOf(
                "M4.5 7 L19.5 7",
                "M9.5 7 L9.5 4.6 L14.5 4.6 L14.5 7",
                "M6.8 7 L7.7 19.6 L16.3 19.6 L17.2 7",
                "M10.5 10.5 L10.5 16.5",
                "M13.5 10.5 L13.5 16.5",
            ),
        )
    }

    /** 编辑。 */
    val Edit: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Edit",
            listOf("M4.5 19.5 L8.8 18.4 L19.2 8 L16 4.8 L5.6 15.2 Z", "M14.2 6.6 L17.4 9.8"),
        )
    }

    /** 分享。 */
    val Share: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Share",
            listOf("M12 3.5 L12 15", "M8 7.5 L12 3.5 L16 7.5", "M5.5 12.5 L5.5 20.5 L18.5 20.5 L18.5 12.5"),
        )
    }

    /** 刷新。 */
    val Refresh: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Refresh",
            listOf("M19 12 A7 7 0 1 1 12 5", "M9.4 2.6 L12.4 5 L9.4 7.4"),
        )
    }

    /** 设置（推子样式）。 */
    val Settings: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.Settings",
            listOf(
                "M4 7.5 L20 7.5",
                "M4 16.5 L20 16.5",
                circlePath(9f, 7.5f, 2.2f),
                circlePath(15f, 16.5f, 2.2f),
            ),
        )
    }

    /** 排序 / 拖拽手柄。 */
    val DragHandle: ImageVector by lazy {
        smartisanIcon(
            "SmartisanX.DragHandle",
            listOf("M4 8 L20 8", "M4 16 L20 16"),
            strokeWidth = 2f,
        )
    }
}
