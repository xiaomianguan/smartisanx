package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cc.wuersan008.smartisanx.core.theme.SmartisanDimens
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.input.SmartisanClearableField
import cc.wuersan008.smartisanx.ui.input.SmartisanPasswordField
import cc.wuersan008.smartisanx.ui.layout.SmartisanEditorLabel
import cc.wuersan008.smartisanx.ui.layout.SmartisanEditorRightIcon
import cc.wuersan008.smartisanx.ui.layout.SmartisanEditorRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap

/**
 * framework 编辑行（`smartisanos.widget.editor.*`）演示。
 *
 * 逐张对应 `abs_editor_layout`（行）、`editor_left_label_layout`（左标签）、
 * `editor_right_icon_widget_layout`（右说明 + 图标）。
 */
@Composable
fun EditorRowSection() {
    var name by remember { mutableStateOf("锤子科技") }
    var mail by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("四行一组") }
    var password by remember { mutableStateOf("123456") }
    var reveal by remember { mutableStateOf(false) }
    var tag by remember { mutableStateOf("可清空输入框") }
    // 左图标 / 右说明那一行也要能输入、能点，否则点上去像是坏掉了。
    var iconLabel by remember { mutableStateOf("") }
    var synced by remember { mutableStateOf(true) }

    // 四行一组：底图按 Single / Top / Middle / Bottom 取（原版 EditorStyle.*）。
    Column(modifier = Modifier.padding(horizontal = SmartisanDimens.ListItemHorizontalMargin)) {
        SmartisanEditorRow(
            label = "名称",
            value = name,
            onValueChange = { name = it },
            position = SmartisanGroupRowPosition.Top,
        )
        SmartisanEditorRow(
            label = "邮箱",
            value = mail,
            onValueChange = { mail = it },
            placeholder = "editor_hint_text_color",
            position = SmartisanGroupRowPosition.Middle,
        )
        SmartisanEditorRow(
            label = "备注",
            value = note,
            onValueChange = { note = it },
            position = SmartisanGroupRowPosition.Middle,
            trailing = { SmartisanEditorRightIcon(text = "${note.length}/20") },
        )
        SmartisanEditorRow(
            label = "禁用",
            value = "enabled = false",
            enabled = false,
            position = SmartisanGroupRowPosition.Bottom,
        )
    }

    SmartisanListVerticalGap()

    Column(modifier = Modifier.padding(horizontal = SmartisanDimens.ListItemHorizontalMargin)) {
        // 左槽换成「图标 + 标签 + 箭头」，右槽换成「说明 + 图标（带 2px 分隔线）」。
        SmartisanEditorRow(
            value = iconLabel,
            onValueChange = { iconLabel = it },
            placeholder = "左图标 + 右说明 / 图标",
            leading = {
                SmartisanEditorLabel(
                    text = "带图标的标签",
                    iconRes = SmartisanOriginalIcons.TabFolder,
                    showDivider = true,
                    showArrow = true,
                    onClick = { synced = !synced },
                )
            },
            trailing = {
                SmartisanEditorRightIcon(
                    text = if (synced) "已同步" else "未同步",
                    iconRes = SmartisanOriginalIcons.Refresh,
                    showDivider = true,
                    onClick = { synced = !synced },
                )
            },
        )
        // 中槽换成原版另外两个编辑框变体（pwd_edit_text / quick_del_edit_text）。
        SmartisanEditorRow(
            label = "密码",
            position = SmartisanGroupRowPosition.Single,
            content = {
                SmartisanPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    revealPassword = reveal,
                    onRevealPasswordChange = { reveal = it },
                )
            },
        )
        SmartisanEditorRow(
            label = "可清空",
            position = SmartisanGroupRowPosition.Single,
            content = {
                SmartisanClearableField(value = tag, onValueChange = { tag = it })
            },
        )
    }

    SampleFootnote(
        "编辑行来自 framework 的 AbsEditor 家族：整行底图是按位置取的 9-patch（editor_bg_single / " +
            "_top / _middle / _bottom，即 EditorStyle 的四套样式），左右内边距 6dp、最小高度 44dp；" +
            "左标签 12sp + 左边距 12dp，图标容器 40dp × 44dp 且图标 26dp 居中；" +
            "右说明最宽 150dp、图标左边距 6dp（有分隔线时变 0）；" +
            "内部 2px 分隔线取主题 divider（浅色 #E9E9E9，即原版 list_divider_color 的 8% 黑）。",
    )
}
