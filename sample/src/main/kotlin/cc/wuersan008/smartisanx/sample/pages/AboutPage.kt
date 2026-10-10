package cc.wuersan008.smartisanx.sample.pages

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import cc.wuersan008.smartisanx.core.interaction.collectSmartisanPressedAsState
import cc.wuersan008.smartisanx.core.interaction.rememberSmartisanInteractionSource
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.core.utils.smartisanShadowBackground
import cc.wuersan008.smartisanx.sample.R
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanAboutStaticItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import java.io.File
import java.util.Date

/**
 * 「关于本机」页：照抄坚果 R2 设置页的 `about_settings_layout.xml`。
 *
 * 自上而下与原版一一对应：
 *
 * 1. **logo 卡片**（原版 `ListContentItemText` + `ListContentItemStyle.Single`）：
 *    原版这里放的是那张「smartisan os / based on Android™」锁图（`about_logo` 9-patch，
 *    336×180dp），本页换成项目的牛皮纸盒 logo（`R.drawable.about_logo`，88dp 高，
 *    位置与原版红色锁图相同：距卡片顶 40dp）；原版压在图上那行 8.6sp 小字
 *    （`about_logo_os_vertion`，距卡片顶 143dp）放的是系统版本，这里放示例应用版本。
 * 2. **五行可点设置行**（原版 `SettingItemText`，`SettingSubItemTop/Mid/BottomStyle`）：
 *    本机状态信息 / 本机名称 / 法律信息 / 保修服务 / 用户反馈。原版分别跳到状态页、
 *    设备名称页、法律信息页、保修应用与反馈应用，示例里只保留版式与箭头。
 * 3. **只读信息行**（原版 `AboutStaticItem`，即 [SmartisanAboutStaticItem]）：
 *    型号 / 存储容量 / Android 版本 / 基带版本 / 内核版本 / 处理器 / 内存 / 软件版本 /
 *    编译日期。取值方式照抄原版 `AboutFragment.onSupportVisible()`：型号取 `Build.MODEL`、
 *    容量按原版公式向上取到 2 的幂、内存读 `/proc/meminfo` 再向上取整、处理器读
 *    `/proc/cpuinfo` 的 `Hardware` 行、基带取 `Build.getRadioVersion()`（原版读
 *    `gsm.version.baseband`）、编译日期取 `Build.TIME`（原版读 `ro.build.date`）。
 *
 * 原版的小彩蛋也照搬：300ms 内的连点累加，连点 logo 15 次后「软件版本」显示完整版本号
 * （`showRightModVersion()`，原版显示的是 `ro.smartisan.version` 全量串）。
 */
@Composable
fun AboutPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val info = remember(context) { readAboutInfo(context) }
    var taps by remember { mutableIntStateOf(0) }
    var lastTapAt by remember { mutableLongStateOf(0L) }
    var showFullVersion by remember { mutableStateOf(false) }
    val version = if (showFullVersion) info.appVersionFull else info.appVersion

    SamplePageScaffold(title = "关于本机", onBack = onBack) {
        // 原版布局的第一件事就是插一条 14dp 的分组留白（group_list_item_vertical_gap_layout）。
        SmartisanListVerticalGap()
        AboutLogoCard(
            version = version,
            onLogoClick = {
                // 原版：300ms 内的连续点击才累加，数到 15 次就切到完整版本号。
                val now = System.currentTimeMillis()
                taps = if (now - lastTapAt < 300) taps + 1 else 1
                lastTapAt = now
                if (taps >= 15) showFullVersion = true
            },
        )
        // 五行设置行：位置决定取分组底图的哪一段（圆角只出现在首尾），
        // 对应原版四套 SettingSubItem*Style。
        SmartisanGroup {
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Top,
                title = "本机状态信息",
                trailing = { AboutRowArrow() },
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Middle,
                title = "本机名称",
                trailing = { AboutRowArrow() },
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Middle,
                title = "法律信息",
                trailing = { AboutRowArrow() },
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Middle,
                title = "保修服务",
                trailing = { AboutRowArrow() },
                onClick = {},
            )
            SmartisanGroupItem(
                position = SmartisanGroupRowPosition.Bottom,
                title = "用户反馈",
                trailing = { AboutRowArrow() },
                onClick = {},
            )
        }
        // 原版这里有一条卡片底部投影（item_bottom_shadow_layout）；库里的行投影画在行边界
        // 之外，所以留一段 14dp 留白让它落下来，别压到下面的信息行。
        SmartisanListVerticalGap()
        // 只读信息行：原版这些行不带卡片底图，直接落在页面底纹上，靠 1px 分隔线分节。
        SmartisanAboutStaticItem(title = "型号", summary = info.model)
        SmartisanAboutStaticItem(title = "存储容量", summary = info.storage)
        SmartisanAboutStaticItem(title = "Android 版本", summary = info.androidVersion)
        SmartisanAboutStaticItem(title = "基带版本", summary = info.baseband)
        SmartisanAboutStaticItem(title = "内核版本", summary = info.kernel)
        SmartisanAboutStaticItem(title = "处理器", summary = info.cpu)
        SmartisanAboutStaticItem(title = "内存", summary = info.memory)
        SmartisanAboutStaticItem(title = "软件版本", summary = version)
        SmartisanAboutStaticItem(title = "编译日期", summary = info.buildDate)

        SampleFootnote(
            "版式来自坚果 R2 设置页：about_settings_layout（logo 卡片 + 五行设置行 + " +
                "只读信息行）与 about_static_item_layout（SmartisanAboutStaticItem）。" +
                "原版卡片里那张「smartisan os / based on Android™」锁图换成了项目 logo，" +
                "图上那行小字从系统版本改成示例应用版本；只读信息行的取值方式照抄 " +
                "AboutFragment，所以数字随设备而变。",
        )
    }
}

/** 设置行右侧的小箭头，用原版设置项的 `setting_item_arrow`。 */
@Composable
private fun AboutRowArrow() {
    SmartisanIcon(
        res = SmartisanDrawables.SettingsItemArrow,
        contentDescription = null,
    )
}

/** logo 卡片高度，等于原版 `about_logo` 9-patch 的固有高度（540px @ xxhdpi）。 */
private val LogoCardHeight = 180.dp

/** logo 距卡片顶的距离，原版红色锁图占 40dp..127.7dp 这一段。 */
private val LogoTop = 40.dp

/** logo 的图形高度，与原版红色锁图那一段等高。 */
private val LogoHeight = 88.dp

/** 版本号距卡片顶的距离，原版 `about_logo_os_vertion` 的 `layout_marginTop`（143dp）。 */
private val LogoVersionTop = 143.dp

/** 底部商标说明距卡片顶的距离，原版锁图里那行灰字量出来在 165.7dp。 */
private val LogoTrademarkTop = 164.dp

/** 图上两行小字的字号，原版写死 `8.599976sp`。 */
private val LogoTextSize = 8.6.sp

/**
 * 原版那张 logo 卡片（`ListContentItemText` + `ListContentItemStyle.Single`）。
 *
 * 卡片 180dp 高（原版 9-patch 的固有高度），里面的三样东西都按原版量出来的位置摆：
 * logo 距顶 40dp、高 88dp；版本号距顶 143dp；底部那行 Google 商标说明距顶 164dp。
 * 卡片本身可点（原版的 logo 也是可点的，用来触发连点彩蛋），底图用设置页子项卡片的
 * selector，所以按下会换成原版的按压位图。
 *
 * 版本号与商标说明原版写死 `#a4a4a4`（约 36% 黑），这里用主题的 `textTertiary`
 * （浅色 40% 黑），深色模式能跟随。
 */
@Composable
private fun AboutLogoCard(version: String, onLogoClick: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    val interaction = rememberSmartisanInteractionSource()
    val pressed by interaction.collectSmartisanPressedAsState()
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SmartisanDimens.ListItemHorizontalMargin)
                .smartisanShadowBackground(
                    backgroundRes = SmartisanDrawables.SettingSubItemSingle,
                    shadowRes = SmartisanDrawables.GroupRowSingleShadow,
                    pressed = pressed,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onLogoClick,
                )
                .height(LogoCardHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        SmartisanIcon(
            res = R.drawable.about_logo,
            contentDescription = null,
            modifier = Modifier.padding(top = LogoTop),
            size = LogoHeight,
        )
        SmartisanText(
            text = version,
            modifier = Modifier.fillMaxWidth().padding(top = LogoVersionTop),
            style = typography.caption.copy(fontSize = LogoTextSize),
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
        SmartisanText(
            text = "- Android is a trademark of Google LLC. -",
            modifier = Modifier.fillMaxWidth().padding(top = LogoTrademarkTop),
            style = typography.caption.copy(fontSize = LogoTextSize),
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 「关于本机」页要显示的取值，逐项对应原版 `AboutFragment.onSupportVisible()` 里的赋值。
 *
 * 原版这些值都从系统属性与 `/proc` 里现取，所以数字随设备而变；这里同样现取，
 * 取不到的项显示原版的兜底文案「未知」（`device_info_default`）。
 */
private data class AboutInfo(
    val model: String,
    val storage: String,
    val androidVersion: String,
    val baseband: String,
    val kernel: String,
    val cpu: String,
    val memory: String,
    val appVersion: String,
    val appVersionFull: String,
    val buildDate: String,
)

/** 原版 `device_info_default`：取不到值时显示「未知」。 */
private const val AboutUnknown = "未知"

private fun readAboutInfo(context: Context): AboutInfo {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: AboutUnknown
    return AboutInfo(
        // 原版是 `Build.MODEL` + `getMsvSuffix()`（只有工程机才会多一个后缀）。
        model = Build.MODEL,
        storage = nominalStorage(Environment.getDataDirectory().path),
        androidVersion = Build.VERSION.RELEASE,
        // 原版读 `gsm.version.baseband`；公开 API 里对应的是 `Build.getRadioVersion()`。
        baseband = Build.getRadioVersion()?.takeIf { it.isNotBlank() } ?: AboutUnknown,
        // 原版读 `/proc/version` 再格式化成三行；这里只取内核版本号。
        kernel = System.getProperty("os.version") ?: AboutUnknown,
        cpu = cpuInfo(),
        memory = totalMemoryGb(),
        appVersion = versionName,
        appVersionFull = "$versionName (build ${PackageInfoCompat.getLongVersionCode(packageInfo)})",
        // 原版读 `ro.build.date`（形如 "Tue Jun 27 07:32:14 CST 2017"），
        // `Date.toString()` 正好是同一个格式。
        buildDate = Date(Build.TIME).toString(),
    )
}

/**
 * 原版 `Utils.getStorageVersion()`：把总容量向上取到 2 的整数次幂，再换算成 GB / TB。
 *
 * 128GB 的机器 `StatFs` 读到约 118GB，向上取整后正好是原版显示的「128 GB」。
 */
private fun nominalStorage(path: String): String =
    runCatching {
        val totalBytes = StatFs(path).totalBytes
        if (totalBytes <= 0L) return@runCatching AboutUnknown
        val nominal = 1L shl (64 - java.lang.Long.numberOfLeadingZeros(totalBytes))
        val tb = nominal shr 40
        if (tb > 0L) "$tb TB" else "${nominal shr 30} GB"
    }.getOrDefault(AboutUnknown)

/** 原版读 `/proc/cpuinfo` 的 `Hardware` 行；读不到就退回 `Build.HARDWARE`。 */
private fun cpuInfo(): String {
    val fromProc =
        runCatching {
            File("/proc/cpuinfo").readLines()
                .firstOrNull { it.startsWith("Hardware") }
                ?.substringAfter(':')
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }.getOrNull()
    return fromProc ?: Build.HARDWARE.takeIf { it.isNotBlank() } ?: AboutUnknown
}

/** 原版把 `/proc/meminfo` 的 `MemTotal` 向上取整到 GB（`(kb / 1024 / 1024) + 1`）。 */
private fun totalMemoryGb(): String =
    runCatching {
        val kb =
            File("/proc/meminfo").readLines()
                .firstOrNull { it.startsWith("MemTotal") }
                ?.filter { it.isDigit() }
                ?.toLongOrNull()
                ?: return@runCatching AboutUnknown
        if (kb <= 0L) AboutUnknown else "${(kb / 1024 / 1024) + 1} GB"
    }.getOrDefault(AboutUnknown)
