package cc.wuersan008.smartisanx.sample.pages

import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.core.net.toUri
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 「关于」页：smartisanx 组件库与示例应用自己的说明页。
 *
 * 借的是**版式** —— 自上而下照抄坚果 R2 设置页的 `about_settings_layout.xml`，
 * 但内容跟「本机」无关：
 *
 * 1. **logo 卡片**（原版 `ListContentItemText` + `ListContentItemStyle.Single`）：
 *    原版这里放的是那张「smartisan os / based on Android™」锁图（`about_logo` 9-patch，
 *    336×180dp），本页换成项目的牛皮纸盒 logo（`R.drawable.about_logo`，88dp 高，
 *    位置与原版红色锁图相同：距卡片顶 40dp）；原版压在图上那行 8.6sp 小字
 *    （`about_logo_os_vertion`，距卡片顶 143dp）放的是系统版本，这里放示例应用版本。
 * 2. **五行可点设置行**（原版 `SettingItemText`，`SettingSubItemTop/Mid/BottomStyle`）：
 *    源码仓库 / 组件文档 / 问题反馈 / 更新日志 / 开源许可，点一下用浏览器打开对应网址。
 * 3. **只读信息行**（原版 `AboutStaticItem`，即 [SmartisanAboutStaticItem]）：
 *    组件库 / 示例应用版本 / 界面语言 / 最低 API / 目标 API / 界面字体 / 素材来源 /
 *    安装日期 / 运行环境。能现取的就现取 —— 版本号取 `PackageInfo`、两个 API 级别取
 *    `ApplicationInfo`、安装日期取 `lastUpdateTime`、运行环境取 `Build.MODEL` 与
 *    `Build.VERSION.RELEASE`；字体与素材来源这类运行时拿不到的项目事实直接写在示例里。
 *
 * 原版的小彩蛋也照搬：300ms 内的连点累加，连点 logo 15 次后「示例应用版本」显示完整版本号
 * （原版 `showRightModVersion()`）。
 */
@Composable
fun AboutPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val info = remember(context) { readAboutInfo(context) }
    var taps by remember { mutableIntStateOf(0) }
    var lastTapAt by remember { mutableLongStateOf(0L) }
    var showFullVersion by remember { mutableStateOf(false) }
    val version = if (showFullVersion) info.appVersionFull else info.appVersion

    SamplePageScaffold(title = "关于", onBack = onBack) {
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
        // 对应原版四套 SettingSubItem*Style；内容换成仓库 / 文档 / 反馈等链接。
        SmartisanGroup {
            AboutLinkRow(position = SmartisanGroupRowPosition.Top, title = "源码仓库", url = RepoUrl)
            AboutLinkRow(position = SmartisanGroupRowPosition.Middle, title = "组件文档", url = "$RepoUrl/tree/main/docs")
            AboutLinkRow(position = SmartisanGroupRowPosition.Middle, title = "问题反馈", url = "$RepoUrl/issues")
            AboutLinkRow(position = SmartisanGroupRowPosition.Middle, title = "更新日志", url = "$RepoUrl/commits/main")
            AboutLinkRow(position = SmartisanGroupRowPosition.Bottom, title = "开源许可", url = "$RepoUrl/blob/main/LICENSE")
        }
        // 原版这里有一条卡片底部投影（item_bottom_shadow_layout）；库里的行投影画在行边界
        // 之外，所以留一段 14dp 留白让它落下来，别压到下面的信息行。
        SmartisanListVerticalGap()
        // 只读信息行：原版这些行不带卡片底图，直接落在页面底纹上，靠 1px 分隔线分节。
        SmartisanAboutStaticItem(title = "组件库", summary = info.library)
        SmartisanAboutStaticItem(title = "示例应用版本", summary = version)
        SmartisanAboutStaticItem(title = "界面语言", summary = info.languages)
        SmartisanAboutStaticItem(title = "最低 API", summary = info.minApi)
        SmartisanAboutStaticItem(title = "目标 API", summary = info.targetApi)
        SmartisanAboutStaticItem(title = "界面字体", summary = info.font)
        SmartisanAboutStaticItem(title = "素材来源", summary = info.assets)
        SmartisanAboutStaticItem(title = "安装日期", summary = info.installDate)
        SmartisanAboutStaticItem(title = "运行环境", summary = info.runtime)

        SampleFootnote(
            "版式借自坚果 R2 设置页：about_settings_layout（logo 卡片 + 五行设置行 + " +
                "只读信息行）与 about_static_item_layout（SmartisanAboutStaticItem）；" +
                "内容换成 smartisanx 自己的 —— logo 卡片里原版那张「smartisan os / based on " +
                "Android™」锁图换成项目 logo，设置行换成仓库 / 文档 / 反馈等链接，" +
                "只读信息行换成组件库与示例应用的事实。",
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
 * 「关于」页只读信息行的取值。
 *
 * 能现取的就现取：版本号取 `PackageInfo`、两个 API 级别取 `ApplicationInfo`、
 * 安装日期取 `lastUpdateTime`、运行环境取 `Build`；字体与素材来源这类**运行时拿不到**的
 * 项目事实直接写在示例里（来源是 README 与固件清单，那边改了要一起改）。
 */
private data class AboutInfo(
    val library: String,
    val appVersion: String,
    val appVersionFull: String,
    val languages: String,
    val minApi: String,
    val targetApi: String,
    val font: String,
    val assets: String,
    val installDate: String,
    val runtime: String,
)

private fun readAboutInfo(context: Context): AboutInfo {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val applicationInfo = packageInfo.applicationInfo
    val versionName = packageInfo.versionName ?: "未知"
    return AboutInfo(
        library = "smartisanx（core + ui）",
        appVersion = versionName,
        appVersionFull = "$versionName (build ${PackageInfoCompat.getLongVersionCode(packageInfo)})",
        languages = "80 多种，跟随系统",
        // 两个 API 级别直接读 APK 清单（原版这几行读的是设备信息，本页改成读应用自己）。
        minApi = (applicationInfo?.minSdkVersion ?: 0).toString(),
        targetApi = (applicationInfo?.targetSdkVersion ?: 0).toString(),
        font = "Smartisan Compact CNS（坚果 R2 固件）",
        assets = "12 个官方 APK + 三个复刻项目",
        installDate =
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(packageInfo.lastUpdateTime)),
        runtime = "${Build.MODEL} · Android ${Build.VERSION.RELEASE}",
    )
}

/**
 * 一行可点设置行：右侧是原版设置项的小箭头，点一下用系统浏览器打开 [url]。
 *
 * 原版这五行是「本机状态信息 / 本机名称 / 法律信息 / 保修服务 / 用户反馈」，
 * 本页换成项目自己的入口。
 */
@Composable
private fun AboutLinkRow(position: SmartisanGroupRowPosition, title: String, url: String) {
    val context = LocalContext.current
    SmartisanGroupItem(
        position = position,
        title = title,
        trailing = { AboutRowArrow() },
        onClick = { openUrl(context, url) },
    )
}

/** 用系统浏览器打开链接；设备上没有能处理它的应用时静默失败（示例不弹错误提示）。 */
private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

/** 项目仓库地址；上面几行链接都由它拼出来。 */
private const val RepoUrl = "https://github.com/xiaomianguan/smartisanx"
