package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.overlay.SmartisanBottomSheet
import cc.wuersan008.smartisanx.ui.overlay.SmartisanConfirmDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuItem
import cc.wuersan008.smartisanx.ui.overlay.SmartisanMenuDialog
import cc.wuersan008.smartisanx.ui.overlay.SmartisanProgressDialog

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
}
