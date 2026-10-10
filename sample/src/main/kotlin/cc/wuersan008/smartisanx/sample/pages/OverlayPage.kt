package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanSnackbarHost
import cc.wuersan008.smartisanx.ui.layout.rememberSmartisanSnackbarState
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBhmCountColor
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBhmHeader
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBhmItem
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBhmRow
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBhmSheet
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBottomSheet
import cc.wuersan008.smartisanx.ui.overlay.SmartisanConfirmDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialogButton
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialogTitleBar
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuItem
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanModalWindow
import cc.wuersan008.smartisanx.ui.overlay.SmartisanProgressDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanTwistGuide
import cc.wuersan008.smartisanx.ui.overlay.SmartisanSheetScaffold

/** 浮层页：弹窗、确认框、底部菜单、底部弹层。 */
@Composable
fun OverlayPage(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var showDialog by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var progressDark by remember { mutableStateOf(false) }
    var showBhm by remember { mutableStateOf(false) }
    var bhmDrawer by remember { mutableStateOf<String?>(null) }
    var showModalWindow by remember { mutableStateOf(false) }
    var showSheetScaffold by remember { mutableStateOf(false) }
    var showTwistGuide by remember { mutableStateOf(false) }
    val snackbar = rememberSmartisanSnackbarState()
    var lastAction by remember { mutableStateOf("暂无操作") }

    SamplePageScaffold(title = "浮层", onBack = onBack) {
        SampleSectionHeader("弹窗")
        SmartisanGroup {
            SmartisanListItem(
                title = "SmartisanDialog",
                summary = "带标题栏与按钮的居中弹窗",
                onClick = { showDialog = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanConfirmDialog",
                summary = "标题 + 说明 + 确定/取消",
                onClick = { showConfirm = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanMenuDialog",
                summary = "贴底的全宽动作菜单",
                onClick = { showMenu = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanProgressDialog",
                summary = "246dp 进度卡片：标题 + 48dp 圆环 + 文案",
                onClick = { showProgress = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanProgressDialog（深色）",
                summary = "setDarkTheme(true)：深色底图 + 白字",
                onClick = {
                    progressDark = true
                    showProgress = true
                },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanModalWindow",
                summary = "自定义内容弹窗（标题栏 + 按钮自己排）",
                onClick = { showModalWindow = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanSheetScaffold",
                summary = "挂在页面里的弹层（自带遮罩与进出动画）",
                onClick = { showSheetScaffold = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanTwistGuide",
                summary = "手势切横竖屏提示（全屏遮罩 + 303×453dp 面板 + 右上角关闭）",
                onClick = { showTwistGuide = true },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanSnackbar（带操作）",
                summary = "金色操作按钮，2 秒后自动收起",
                onClick = { snackbar.show("已加入收藏", actionText = "撤销") { lastAction = "撤销" } },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanSnackbar（带图标）",
                summary = "右侧 2px 分隔线 + 40dp 图标按钮",
                onClick = {
                    snackbar.show(
                        message = "设置已同步",
                        iconRes = SmartisanOriginalIcons.Complete,
                    )
                },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanBhmSheet",
                summary = "带标题的列表弹层：分组标题 + 计数徽标 + 两列切换",
                onClick = {
                    bhmDrawer = null
                    showBhm = true
                },
            )
            SmartisanRowDivider()
            SmartisanListItem(
                title = "SmartisanBottomSheet",
                summary = "带进出动画的底部弹层",
                onClick = { showSheet = true },
            )
        }

        SampleSectionHeader("最近一次操作")
        SmartisanGroup {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                SmartisanText(
                    text = lastAction,
                    style = typography.listItemPrimary,
                    color = colors.textPrimary,
                )
            }
        }

        SampleFootnote(
            "弹窗外壳合并了锤子音乐的 SmartisanModal（透明窗口 + 0.54 遮罩）与锤子时钟的 " +
                "SmartisanModalDialog / SmartisanMenuDialog（308dp 宽、10dp 圆角、48dp 标题栏与按钮）。",
        )
    }

    if (showDialog) {
        SmartisanDialog(
            onDismissRequest = { showDialog = false },
            title = "锤子风格弹窗",
            confirmText = "知道了",
            dismissText = "取消",
            onConfirm = { lastAction = "弹窗已确认" },
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                SmartisanText(
                    text = "内容区由调用方提供，标题栏与按钮由组件负责，窗口遮罩为 0.54。",
                    style = typography.listItemSecondary,
                    color = colors.textSecondary,
                )
            }
        }
    }

    if (showConfirm) {
        SmartisanConfirmDialog(
            onDismissRequest = { showConfirm = false },
            title = "删除该条目？",
            message = "删除后无法恢复，确认框沿用原版锤子时钟的弹窗比例。",
            onConfirm = { lastAction = "已确认删除" },
        )
    }

    if (showMenu) {
        SmartisanMenuDialog(
            onDismissRequest = { showMenu = false },
            title = "选择操作",
        ) {
            SmartisanMenuItem(text = "分享", onClick = { lastAction = "分享" })
            SmartisanMenuItem(text = "重命名", onClick = { lastAction = "重命名" })
            SmartisanMenuItem(text = "删除", danger = true, onClick = { lastAction = "删除" })
            SmartisanMenuItem(text = "禁用项示例", enabled = false, showDivider = false, onClick = {})
        }
    }

    if (showProgress) {
        SmartisanProgressDialog(
            onDismissRequest = {
                showProgress = false
                progressDark = false
            },
            title = if (progressDark) "深色进度弹窗" else "正在同步",
            message = "标题与文案都是可选的，圆环是 ROM 里那张 48dp 不确定圈。",
            dark = progressDark,
        )
    }

    if (showBhm) {
        val drawer = bhmDrawer
        SmartisanBhmSheet(
            onDismissRequest = { showBhm = false },
            title = drawer ?: "添加",
            onBack = if (drawer != null) { { bhmDrawer = null } } else null,
            pageKey = drawer ?: "root",
        ) {
            if (drawer == null) {
                SmartisanBhmHeader("快速访问")
                listOf(
                    SmartisanBhmItem(
                        title = "最近文件",
                        iconRes = SmartisanOriginalIcons.SortByTime,
                        count = "12",
                        onClick = { bhmDrawer = "最近文件" },
                    ),
                    SmartisanBhmItem(
                        title = "图片",
                        iconRes = SmartisanOriginalIcons.Share,
                        count = "48",
                        onClick = { bhmDrawer = "图片" },
                    ),
                    SmartisanBhmItem(
                        title = "音乐",
                        iconRes = SmartisanOriginalIcons.Play,
                        count = "6",
                        countColor = SmartisanBhmCountColor.Grey,
                        onClick = { bhmDrawer = "音乐" },
                    ),
                ).forEach { SmartisanBhmRow(it) }
                SmartisanBhmHeader("更多")
                SmartisanBhmRow(
                    SmartisanBhmItem(
                        title = "收藏夹",
                        iconRes = SmartisanOriginalIcons.Settings,
                        subtitle = "3 个",
                    ),
                )
                SmartisanBhmRow(
                    SmartisanBhmItem(
                        title = "正在扫描…",
                        showProgress = true,
                    ),
                )
                SmartisanBhmRow(
                    SmartisanBhmItem(
                        title = "回收站",
                        iconRes = SmartisanOriginalIcons.DeletePlain,
                        count = "2",
                        countColor = SmartisanBhmCountColor.Red,
                        showAlert = true,
                    ),
                )
            } else {
                listOf(
                    SmartisanBhmItem(
                        title = "项目周报.pdf",
                        iconRes = SmartisanOriginalIcons.SortByName,
                        subtitle = "2.4 MB",
                    ),
                    SmartisanBhmItem(
                        title = "设计稿评审.png",
                        iconRes = SmartisanOriginalIcons.Share,
                        subtitle = "1.1 MB",
                    ),
                    SmartisanBhmItem(
                        title = "会议纪要.docx",
                        iconRes = SmartisanOriginalIcons.SortByName,
                        subtitle = "86 KB",
                    ),
                    SmartisanBhmItem(
                        title = "已归档的条目",
                        enabled = false,
                    ),
                ).forEach { SmartisanBhmRow(it) }
            }
        }
    }

    if (showSheet) {
        SmartisanBottomSheet(
            onDismissRequest = { showSheet = false },
            title = "底部弹层",
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                SmartisanText(
                    text = "SmartisanBottomSheet 使用 Compose 的进出动画，关闭时会先播完动画再回调。",
                    style = typography.listItemSecondary,
                    color = colors.textSecondary,
                )
            }
        }
    }

    // 提示条挂在页面底部（原版是 CustomToast 的窗口，这里用宿主叠在页面里）。
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        SmartisanSnackbarHost(state = snackbar, modifier = Modifier.padding(bottom = 24.dp))
    }

    if (showModalWindow) {
        SmartisanModalWindow(onDismissRequest = { showModalWindow = false }) {
            SmartisanDialogTitleBar(
                title = "自定义弹窗",
                onDismiss = { showModalWindow = false },
            )
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                SmartisanText(
                    text = "SmartisanModalWindow 只提供窗口与遮罩，标题栏与按钮由调用方用 " +
                        "SmartisanDialogTitleBar / SmartisanDialogButton 自己排。",
                    style = typography.listItemSecondary,
                    color = colors.textSecondary,
                )
            }
            Row(Modifier.fillMaxWidth().height(48.dp)) {
                SmartisanDialogButton(
                    text = "取消",
                    onClick = { showModalWindow = false },
                    modifier = Modifier.weight(1f),
                    accent = false,
                )
                SmartisanDialogButton(
                    text = "知道了",
                    onClick = {
                        lastAction = "自定义弹窗已关闭"
                        showModalWindow = false
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showSheetScaffold) {
        SmartisanSheetScaffold(
            visible = true,
            onDismissRequest = { showSheetScaffold = false },
            title = "页面内弹层",
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                SmartisanText(
                    text = "SmartisanSheetScaffold 不新建窗口，直接叠在当前页面里，适合侧边栏、内嵌面板。",
                    style = typography.listItemSecondary,
                    color = colors.textSecondary,
                )
            }
        }
    }

    // 手势提示最后画，盖在整页之上（原版是单独的窗口）。
    SmartisanTwistGuide(
        visible = showTwistGuide,
        onDismiss = {
            showTwistGuide = false
            lastAction = "手势提示已关闭"
        },
    )
}
