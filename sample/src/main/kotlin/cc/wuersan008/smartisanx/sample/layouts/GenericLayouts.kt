package cc.wuersan008.smartisanx.sample.layouts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.anim.SmartisanPageTransition
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.asset.SmartisanOriginalIcons
import cc.wuersan008.smartisanx.ui.basic.SmartisanIcon
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanActionButton
import cc.wuersan008.smartisanx.ui.control.SmartisanActionButtonGroup
import cc.wuersan008.smartisanx.ui.control.SmartisanSwitchRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanEditorRow
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupItem
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroupRowPosition
import cc.wuersan008.smartisanx.ui.layout.SmartisanListVerticalGap
import cc.wuersan008.smartisanx.ui.layout.SmartisanScaffold
import cc.wuersan008.smartisanx.ui.layout.SmartisanSectionTitle
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanTitleBarAction

/**
 * 通用骨架：列表 → 详情。
 *
 * 最常见的一级 / 二级结构。两级页面各自是一个 [SmartisanScaffold]，
 * 中间用 [SmartisanPageTransition] 做转场（新页面从右侧滑入，和原版一致）；
 * 返回键先关详情、再退出列表，两级都能正确消费。
 *
 * 复制到自己项目时，把 [SampleMessages] 换成自己的数据源、把详情页换成自己的内容即可。
 */
@Composable
fun ListDetailLayout(onBack: () -> Unit) {
    var open by remember { mutableStateOf<SampleMessage?>(null) }
    // 关闭动画期间被关掉的页面还要继续画。
    var retained by remember { mutableStateOf<SampleMessage?>(null) }
    LaunchedEffect(open) { if (open != null) retained = open }
    BackHandler(enabled = open != null) { open = null }
    SmartisanPageTransition(
        secondary = open != null,
        modifier = Modifier.fillMaxSize(),
        primary = { MessageList(onOpen = { open = it }, onBack = onBack) },
    ) {
        val message = retained
        if (message != null) {
            MessageDetail(message = message, onBack = { open = null })
        }
    }
}

/**
 * 通用骨架：表单填写。
 *
 * 用 [SmartisanEditorRow] 排一组输入行（原版 `editor_row_*` 底图，行高 `editor_row_min_height`），
 * 加一个开关行，底部是原版的操作按钮组。输入值都存在 `remember` 里，能直接打字验证焦点与键盘行为。
 */
@Composable
fun FormEditorLayout(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "新建地址",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Complete,
                            contentDescription = "完成",
                            onClick = {},
                        ),
                    ),
            )
        },
        bottomBar = {
            // 原版底部按钮组：中间是文字按钮（左 / 右两个槽位是图标按钮，这里不用）。
            SmartisanActionButtonGroup(
                actions =
                    listOf(
                        SmartisanActionButton(text = "取消", onClick = onBack),
                        SmartisanActionButton(text = "保存", onClick = {}),
                    ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SmartisanGroup {
                SmartisanEditorRow(
                    label = "姓名",
                    position = SmartisanGroupRowPosition.Top,
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "收货人姓名",
                )
                SmartisanEditorRow(
                    label = "手机",
                    position = SmartisanGroupRowPosition.Middle,
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "11 位手机号",
                )
                SmartisanEditorRow(
                    label = "地址",
                    position = SmartisanGroupRowPosition.Bottom,
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "街道、门牌号",
                )
            }
            SmartisanListVerticalGap()
            SmartisanSectionTitle("其它")
            SmartisanGroup {
                SmartisanSwitchRow(
                    text = "设为默认地址",
                    summary = "下单时优先使用这个地址",
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    position = SmartisanGroupRowPosition.Single,
                )
            }
            SmartisanListVerticalGap()
        }
    }
}

/** 一级页面：收件箱列表。 */
@Composable
private fun MessageList(
    onOpen: (SampleMessage) -> Unit,
    onBack: () -> Unit,
) {
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "收件箱",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.Search,
                            contentDescription = "搜索",
                            onClick = {},
                        ),
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.AddTitleBar,
                            contentDescription = "写邮件",
                            onClick = {},
                        ),
                    ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SmartisanGroup {
                SampleMessages.forEachIndexed { index, message ->
                    SmartisanGroupItem(
                        position =
                            when (index) {
                                0 -> SmartisanGroupRowPosition.Top
                                SampleMessages.lastIndex -> SmartisanGroupRowPosition.Bottom
                                else -> SmartisanGroupRowPosition.Middle
                            },
                        title = message.subject,
                        summary = "${message.sender} · ${message.time}",
                        trailing = {
                            SmartisanIcon(
                                res = SmartisanDrawables.SettingsItemArrow,
                                contentDescription = null,
                            )
                        },
                        onClick = { onOpen(message) },
                    )
                }
            }
            SmartisanListVerticalGap()
        }
    }
}

/** 二级页面：消息详情，底部是原版的操作按钮组。 */
@Composable
private fun MessageDetail(
    message: SampleMessage,
    onBack: () -> Unit,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanScaffold(
        modifier = Modifier.fillMaxSize(),
        titleBar = {
            SmartisanTitleBar(
                title = "详情",
                navigationIcon =
                    SmartisanTitleBarAction(
                        iconRes = SmartisanOriginalIcons.Back,
                        contentDescription = "返回",
                        onClick = onBack,
                    ),
                actions =
                    listOf(
                        SmartisanTitleBarAction(
                            iconRes = SmartisanOriginalIcons.More,
                            contentDescription = "更多",
                            onClick = {},
                        ),
                    ),
            )
        },
        bottomBar = {
            // 原版 framework 的底部操作按钮组（`smartisan_button_group_layout.xml`）。
            SmartisanActionButtonGroup(
                actions =
                    listOf(
                        SmartisanActionButton(text = "回复", onClick = {}),
                        SmartisanActionButton(text = "转发", onClick = {}),
                    ),
            )
        },
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    SmartisanText(
                        text = message.subject,
                        style = typography.listRowPrimary,
                        color = colors.textPrimary,
                    )
                    SmartisanText(
                        text = "${message.sender} · ${message.time}",
                        modifier = Modifier.padding(top = 6.dp),
                        style = typography.listRowTertiary,
                        color = colors.textTertiary,
                    )
                }
            }
            SmartisanListVerticalGap()
            SmartisanGroup(position = SmartisanGroupRowPosition.Single) {
                SmartisanText(
                    text = message.body,
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    style = typography.body,
                    color = colors.textSecondary,
                )
            }
            SmartisanListVerticalGap()
        }
    }
}
