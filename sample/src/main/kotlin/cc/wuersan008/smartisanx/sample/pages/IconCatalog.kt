package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.ui.graphics.vector.ImageVector
import cc.wuersan008.smartisanx.icons.SmartisanXClockIcons
import cc.wuersan008.smartisanx.icons.SmartisanXMediaIcons
import cc.wuersan008.smartisanx.icons.SmartisanXIcons
import cc.wuersan008.smartisanx.icons.SmartisanXStatusIcons

/** 图标分组：分组名 + （图标名, 图标）列表。 */
data class IconCategory(
    val title: String,
    val icons: List<Pair<String, ImageVector>>,
)

/** smartisanx 自带的全部矢量图标。 */
val smartisanIconCatalog: List<IconCategory> =
    listOf(
        IconCategory(
            title = "通用图标 SmartisanXIcons",
            icons =
                listOf(
                    "Back" to SmartisanXIcons.Back,
                    "ChevronRight" to SmartisanXIcons.ChevronRight,
                    "ChevronDown" to SmartisanXIcons.ChevronDown,
                    "ChevronUp" to SmartisanXIcons.ChevronUp,
                    "Close" to SmartisanXIcons.Close,
                    "Check" to SmartisanXIcons.Check,
                    "Add" to SmartisanXIcons.Add,
                    "Remove" to SmartisanXIcons.Remove,
                    "Search" to SmartisanXIcons.Search,
                    "More" to SmartisanXIcons.More,
                    "Menu" to SmartisanXIcons.Menu,
                    "Delete" to SmartisanXIcons.Delete,
                    "Edit" to SmartisanXIcons.Edit,
                    "Share" to SmartisanXIcons.Share,
                    "Refresh" to SmartisanXIcons.Refresh,
                    "Settings" to SmartisanXIcons.Settings,
                    "DragHandle" to SmartisanXIcons.DragHandle,
                ),
        ),
        IconCategory(
            title = "状态图标 SmartisanXStatusIcons",
            icons =
                listOf(
                    "Star" to SmartisanXStatusIcons.Star,
                    "StarOutline" to SmartisanXStatusIcons.StarOutline,
                    "Heart" to SmartisanXStatusIcons.Heart,
                    "Location" to SmartisanXStatusIcons.Location,
                    "Sun" to SmartisanXStatusIcons.Sun,
                    "Moon" to SmartisanXStatusIcons.Moon,
                    "Info" to SmartisanXStatusIcons.Info,
                    "Warning" to SmartisanXStatusIcons.Warning,
                    "ArrowUp" to SmartisanXStatusIcons.ArrowUp,
                    "ArrowDown" to SmartisanXStatusIcons.ArrowDown,
                    "Copy" to SmartisanXStatusIcons.Copy,
                    "Calendar" to SmartisanXStatusIcons.Calendar,
                    "ExternalLink" to SmartisanXStatusIcons.ExternalLink,
                    "Folder" to SmartisanXStatusIcons.Folder,
                ),
        ),
        IconCategory(
            title = "媒体图标 SmartisanXMediaIcons",
            icons =
                listOf(
                    "Play" to SmartisanXMediaIcons.Play,
                    "Pause" to SmartisanXMediaIcons.Pause,
                    "Next" to SmartisanXMediaIcons.Next,
                    "Previous" to SmartisanXMediaIcons.Previous,
                    "Stop" to SmartisanXMediaIcons.Stop,
                    "Shuffle" to SmartisanXMediaIcons.Shuffle,
                    "Repeat" to SmartisanXMediaIcons.Repeat,
                    "Queue" to SmartisanXMediaIcons.Queue,
                    "Volume" to SmartisanXMediaIcons.Volume,
                    "VolumeMute" to SmartisanXMediaIcons.VolumeMute,
                    "FastForward" to SmartisanXMediaIcons.FastForward,
                    "Rewind" to SmartisanXMediaIcons.Rewind,
                    "Lyrics" to SmartisanXMediaIcons.Lyrics,
                ),
        ),
        IconCategory(
            title = "时钟图标 SmartisanXClockIcons",
            icons =
                listOf(
                    "Clock" to SmartisanXClockIcons.Clock,
                    "Alarm" to SmartisanXClockIcons.Alarm,
                    "Stopwatch" to SmartisanXClockIcons.Stopwatch,
                    "Hourglass" to SmartisanXClockIcons.Hourglass,
                    "Globe" to SmartisanXClockIcons.Globe,
                    "Bell" to SmartisanXClockIcons.Bell,
                    "SleepTimer" to SmartisanXClockIcons.SleepTimer,
                    "KeepScreenOn" to SmartisanXClockIcons.KeepScreenOn,
                ),
        ),
    )
