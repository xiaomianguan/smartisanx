package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import cc.wuersan008.smartisanx.core.utils.smartisanDrawableBackground
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanIconButton
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.control.SmartisanCheckbox
import cc.wuersan008.smartisanx.ui.layout.SmartisanBlankDefaults
import cc.wuersan008.smartisanx.ui.layout.SmartisanEmptyActionButton
import cc.wuersan008.smartisanx.ui.layout.SmartisanEmptyHint
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction
import cc.wuersan008.smartisanx.ui.layout.smartisanGroupRowBackground
import cc.wuersan008.smartisanx.ui.layout.smartisanGroupRowShadow
import cc.wuersan008.smartisanx.ui.list.SmartisanHiddenRowAction
import cc.wuersan008.smartisanx.ui.list.SmartisanHiddenRowActions

/**
 * 通用骨架：个人资料。
 *
 * 抬头（头像 + 昵称 + 账号）用 [SampleAvatar] 画，下面按分组排信息行，
 * 最后一条是危险操作 —— 用强调色按钮（锤子红，原版 `shrink_long_btn_red_selector`）。
 */
@Composable
fun ProfileLayout(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "个人资料",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Edit,
                            contentDescription = "编辑资料",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SampleAvatar(text = "P", size = 72.dp)
                SmartisanText(
                    text = "puffercat",
                    modifier = Modifier.padding(top = 12.dp),
                    style = typography.listRowPrimary,
                    color = colors.textPrimary,
                )
                SmartisanText(
                    text = "puffercat@example.com",
                    modifier = Modifier.padding(top = 4.dp),
                    style = typography.listRowTertiary,
                    color = colors.textTertiary,
                )
            }
            SmartisanSectionTitle("账号")
            SmartisanGroup {
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Top,
                    title = "昵称",
                    summary = "puffercat",
                    trailing = { Arrow() },
                    onClick = {},
                )
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Middle,
                    title = "手机号",
                    summary = "181 0013 1000",
                    trailing = { Arrow() },
                    onClick = {},
                )
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Bottom,
                    title = "邮箱",
                    summary = "puffercat@example.com",
                    trailing = { Arrow() },
                    onClick = {},
                )
            }
            SmartisanListVerticalGap()
            SmartisanSectionTitle("安全")
            SmartisanGroup {
                SmartisanGroupItem(
                    position = SmartisanGroupRowPosition.Single,
                    title = "修改密码",
                    summary = "上次修改：3 个月前",
                    trailing = { Arrow() },
                    onClick = {},
                )
            }
            SmartisanListVerticalGap()
            SmartisanButton(
                text = "退出登录",
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                style = SmartisanButtonStyle.Accent,
            )
            SmartisanListVerticalGap()
        }
    }
}

/** 信息行右侧的小箭头（`SmartisanListItem` 右槽的母版箭头，与首页一致）。 */
@Composable
private fun Arrow() {
    SmartisanIcon(res = SmartisanDrawables.ListItemArrow, contentDescription = null)
}

/**
 * 通用骨架：空态页。
 *
 * [SmartisanEmptyHint] 默认用 framework 的通用空态插图（`blank_icon_large`，120dp），
 * 版式是：插图 → 主文案（`empty_primary_hint`）→ 说明（`empty_secondary_hint`）→
 * 60dp 圆形操作按钮（`empty_action_btn`，底图 `blank_option_btn_selector` 自带齿轮图标，
 * 所以是**图标按钮**，不要再往上压文字）。
 * 这里换成锤子音乐的「没有播放列表」插图（`blank_playlist`，同样是 120dp），
 * 想让示例看起来更像一个真页面。
 */
@Composable
fun EmptyStateLayout(onBack: () -> Unit) {
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "收藏",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Edit,
                            contentDescription = "编辑",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SmartisanEmptyHint(
                title = "还没有收藏的内容",
                description = "在任意内容上长按，就能把它加入收藏",
                iconRes = SmartisanOriginalIcons.EmptyPlaylist,
                // 原版 60dp 圆形操作按钮的底图 `blank_option_btn_selector` 里**自带齿轮图标**：
                // framework 的 `empty_action_btn` 是个没有文案的 TextView，应用要文字按钮时会先用
                // `emptyActionBackground` 换掉底图 —— 所以这里按原版用法只画图标，不往上压文字。
                action = {
                    SmartisanEmptyActionButton(
                        modifier =
                            Modifier.padding(top = SmartisanBlankDefaults.ActionButtonMarginTop),
                        onClick = {},
                    )
                },
            )
        }
    }
}

/**
 * 通用骨架：多选编辑。
 *
 * 演示多选态的完整套路：
 *
 * 1. 未进入多选时，标题栏右侧是「多选」图标（原版 `standard_icon_multi_select_selector`）；
 * 2. 进入多选后标题栏变成「取消 / 已选 n 项 / 完成」；
 * 3. 行左侧出现复选框（原版 `selector_check_box_red`），选中行的底色由行自己的 selector 切成多选高亮；
 * 4. 行尾挂一排「隐藏行操作」（framework `HiddenListActionLayout`，图标间距 6dp、左右内边距 12dp）；
 * 5. 底部换成原版的多选删除条：红色纹理底图 `list_edit_remove_background`，高 48dp。
 */
@Composable
fun MultiSelectLayout(onBack: () -> Unit) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var selected by remember { mutableStateOf(emptySet<Int>()) }
    val editing = selected.isNotEmpty()
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            if (editing) {
                SmartisanTitleBar(
                    title = "已选择 ${selected.size} 项",
                    navigationIcon =
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Close,
                            contentDescription = "取消选择",
                            onClick = { selected = emptySet() },
                        ),
                    actions =
                        listOf(
                            SmartisanTitleBarAction(
                                iconRes = SmartisanOriginalIcons.Confirm,
                                contentDescription = "完成",
                                onClick = { selected = emptySet() },
                            ),
                        ),
                )
            } else {
                SmartisanTitleBar(
                    title = "全部内容",
                    navigationIcon =
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Back,
                            contentDescription = "返回",
                            onClick = onBack,
                        ),
                    actions =
                        listOf(
                            SmartisanTitleBarAction(
                                iconRes = SmartisanOriginalIcons.MultiSelect,
                                contentDescription = "进入多选",
                                onClick = { selected = setOf(0) },
                            ),
                        ),
                )
            }
        },
        bottomBar = {
            if (editing) {
                // 原版多选条：红色纹理底图 list_edit_remove_background，高 48dp。
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .smartisanDrawableBackground(SmartisanOriginalIcons.RemoveBarBackground),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SmartisanIconButton(
                            onClick = { selected = emptySet() },
                            size = 40.dp,
                            contentDescription = "删除所选",
                        ) {
                            SmartisanIcon(
                                res = SmartisanOriginalIcons.DeleteTitleBar,
                                contentDescription = null,
                            )
                        }
                        SmartisanText(
                            text = "删除所选（${selected.size}）",
                            modifier = Modifier.padding(start = 8.dp),
                            style = typography.listItemPrimary,
                            color = colors.onAccent,
                        )
                    }
                }
            }
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SmartisanGroup {
                SampleNotes.forEachIndexed { index, note ->
                    val position =
                        when (index) {
                            0 -> SmartisanGroupRowPosition.Top
                            SampleNotes.lastIndex -> SmartisanGroupRowPosition.Bottom
                            else -> SmartisanGroupRowPosition.Middle
                        }
                    SmartisanListItem(
                        title = note.title,
                        summary = note.summary,
                        selected = index in selected,
                        leading = {
                            SmartisanCheckbox(
                                checked = index in selected,
                                onCheckedChange = { checked ->
                                    selected = if (checked) selected + index else selected - index
                                },
                            )
                        },
                        trailing = {
                            SmartisanHiddenRowActions(
                                actions =
                                    listOf(
                                        SmartisanHiddenRowAction(
                                            iconRes = SmartisanOriginalIcons.Move,
                                            contentDescription = "移动",
                                        ),
                                        SmartisanHiddenRowAction(
                                            iconRes = SmartisanOriginalIcons.DeleteQuick,
                                            contentDescription = "删除",
                                        ),
                                    ),
                            )
                        },
                        rowBackgroundRes = smartisanGroupRowBackground(position),
                        rowShadowRes = smartisanGroupRowShadow(position),
                        onClick = {
                            selected = if (index in selected) selected - index else selected + index
                        },
                    )
                }
            }
            SmartisanListVerticalGap()
        }
    }
}
