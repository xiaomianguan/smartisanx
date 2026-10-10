package cc.wuersan008.smartisanx.sample.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.sample.SampleFootnote
import cc.wuersan008.smartisanx.sample.SamplePageScaffold
import cc.wuersan008.smartisanx.sample.SampleSectionHeader
import cc.wuersan008.smartisanx.ui.asset.SmartisanDrawables
import cc.wuersan008.smartisanx.ui.basic.SmartisanRowDivider
import cc.wuersan008.smartisanx.ui.basic.SmartisanText
import cc.wuersan008.smartisanx.ui.control.SmartisanButton
import cc.wuersan008.smartisanx.ui.control.SmartisanButtonStyle
import cc.wuersan008.smartisanx.ui.input.SmartisanAutoFitText
import cc.wuersan008.smartisanx.ui.input.SmartisanClearableField
import cc.wuersan008.smartisanx.ui.input.SmartisanJustifyText
import cc.wuersan008.smartisanx.ui.control.SmartisanMarqueeText
import cc.wuersan008.smartisanx.ui.input.SmartisanMessageField
import cc.wuersan008.smartisanx.ui.input.SmartisanPasswordField
import cc.wuersan008.smartisanx.ui.input.SmartisanSearchBar
import cc.wuersan008.smartisanx.ui.layout.SmartisanGroup

/**
 * 文本与输入页：展示 5 个原版文本 / 输入类组件的移植结果。
 *
 * | 组件 | 对应原版类 | 原版出现的 App |
 * | --- | --- | --- |
 * | [SmartisanSearchBar] | `smartisanos.widget.SearchBar` | 短信、日历、时钟、图库、音乐、便签、录音机 |
 * | [SmartisanAutoFitText] | `smartisanos.widget.FontFitTextView` | 日历、短信、便签 |
 * | [SmartisanJustifyText] | `smartisanos.tablet.widget.SmartisanJustifyTextView` | 音乐 |
 * | [SmartisanPasswordField] | `smartisanos.widget.PasswordEditText` | 日历、邮件、音乐 |
 * | [SmartisanClearableField] | `smartisanos.widget.QuickDeleteEditText` | 日历、邮件 |
 */
@Composable
fun InputPage(onBack: () -> Unit) {
    SamplePageScaffold(title = "文本与输入", onBack = onBack) {
        SampleSectionHeader("搜索栏（SearchBar）")
        SearchBarSection()

        SampleSectionHeader("自动缩字文本（FontFitTextView）")
        AutoFitTextSection()

        SampleSectionHeader("两端对齐文本（SmartisanJustifyTextView）")
        JustifyTextSection()

        SampleSectionHeader("密码输入框（PasswordEditText）")
        PasswordFieldSection()

        SampleSectionHeader("可清空输入框（QuickDeleteEditText）")
        ClearableFieldSection()

        SampleSectionHeader("framework 编辑行（editor.*）")
        EditorRowSection()

        SampleSectionHeader("消息输入栏（MessageField）")
        MessageFieldSection()

        SampleSectionHeader("跑马灯标题（MarqueeView）")
        MarqueeSection()

        SampleFootnote(
            "这 5 个组件都直接用原版 APK 里的素材：搜索栏用 NinePatch `search_field`、" +
                "放大镜 `search_bar_left_icon`、清除按钮 `text_clear_btn`、筛选 `sorting_icon_selector`、" +
                "取消 `standard_icon_cancel_selector`、二级筛选 `search_bar_secondary_filter_btn`；" +
                "密码框用 16 帧帧动画 `pwd_eye_open_close_anim`；可清空输入框用 `quick_icon_delete`。" +
                "原版私有 framework 里缺失的取色（光标条、文字与提示色）改用 smartisanx 主题语义色。",
        )
    }
}

/** 搜索栏：展示展开 / 收起、清除、筛选、二级筛选与动画。 */
/** 消息输入栏（framework `smartisanos.widget.MessageField`）演示。 */
@Composable
private fun MessageFieldSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var draft by remember { mutableStateOf("") }
    var lastSent by remember { mutableStateOf("（还没有发送过）") }
    var emojiDraft by remember { mutableStateOf("带表情图标的输入框") }
    val messages =
        remember {
            listOf(
                "对方：明天上午十点开会",
                "我：收到，会议室我来订",
            )
        }

    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        // 模拟聊天窗口的下半部分：上面是消息，下面压着输入栏。
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(colors.surfaceRaised),
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                messages.forEach { line ->
                    SmartisanText(
                        text = line,
                        modifier = Modifier.padding(bottom = 8.dp),
                        style = typography.listItemPrimary,
                        color = colors.textPrimary,
                    )
                }
                SmartisanText(
                    text = "最近发送：$lastSent",
                    style = typography.caption,
                    color = colors.textTertiary,
                )
            }
            SmartisanMessageField(
                value = draft,
                onValueChange = { draft = it },
                onSend = {
                    lastSent = draft
                    draft = ""
                },
                hint = "输入消息",
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // 第二例：显示表情图标（输入框右内边距 6dp → 5dp）并隐藏左侧图标。
        Box(Modifier.fillMaxWidth().padding(top = 24.dp)) {
            SmartisanMessageField(
                value = emojiDraft,
                onValueChange = { emojiDraft = it },
                leftIconRes = null,
                emojiIconRes = SmartisanDrawables.MessageFieldEmojiIcon,
                hint = "无左侧图标 + 表情图标",
            )
        }
    }
}

/** 跑马灯：原版 `SmartisanMarqueeView`，主标题超长时横向滚动。 */
@Composable
private fun MarqueeSection() {
    val colors = LocalSmartisanColors.current
    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            SmartisanMarqueeText(
                title = "锤子音乐 · 世界经典钢琴曲精选集（1998 重制版）",
                subTitle = "原版标题过长时横向滚动，副标题一行小字",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SmartisanRowDivider()
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            SmartisanMarqueeText(
                title = "短标题不滚动",
                subTitle = "没有超出宽度时保持静止",
                marquee = false,
                modifier = Modifier.fillMaxWidth(),
            )
            SmartisanText(
                text = "第二行用 marquee = false 关闭滚动",
                modifier = Modifier.padding(top = 8.dp),
                style = LocalSmartisanTypography.current.caption,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun SearchBarSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        // 原版搜索栏本身就是标题栏高度的控件（48dp），这里直接放在页面内容里演示。
        SmartisanSearchBar(
            query = query,
            onQueryChange = { query = it },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            placeholder = "搜索短信",
            filterIconRes = SmartisanDrawables.SearchBarSorting,
            onFilterClick = {},
            secondaryFilterText = "全部",
            onSecondaryFilterClick = {},
            // 页面自己已经有标题栏，避免再叠一层原版标题栏投影。
            showShadow = false,
        )
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmartisanButton(
                    text = if (expanded) "收起" else "展开",
                    onClick = { expanded = !expanded },
                    style = SmartisanButtonStyle.Neutral,
                )
                SmartisanText(
                    text = "当前关键字：${query.ifEmpty { "（空）" }}",
                    style = typography.listItemSecondary,
                    color = colors.textTertiary,
                )
            }
            SmartisanText(
                text = "点输入框展开：编辑区让位 300ms；展开后右侧出现取消按钮（延迟 100ms 位移 10dp 并淡入，" +
                    "单项 200ms，插值器为原版 DecelerateInterpolator(1.5f)）；输入非空时右侧出现清除按钮；" +
                    "收起会清空关键字。",
                modifier = Modifier.padding(top = 8.dp),
                style = typography.caption,
                color = colors.textTertiary,
            )
        }
    }
}

/** 自动缩字文本：限宽 240dp，观察长文本被二分缩小到刚好放得下。 */
@Composable
private fun AutoFitTextSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    SmartisanGroup {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SmartisanText(
                text = "两行文字都限宽 240dp、起始字号 18sp（原版菜单项字号）：",
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            Box(Modifier.width(240.dp)) {
                SmartisanAutoFitText(
                    text = "短文本保持 18sp",
                    style = typography.body.copy(fontSize = 18.sp),
                    color = colors.textPrimary,
                )
            }
            Box(Modifier.width(240.dp)) {
                SmartisanAutoFitText(
                    text = "这段文字比较长，18sp 放不下，会被自动缩到刚好放进 240dp",
                    style = typography.body.copy(fontSize = 18.sp),
                    color = colors.textPrimary,
                )
            }
        }
    }
}

/** 两端对齐文本：中文长段落逐行拉满宽度，段首两空格缩进不被拉伸。 */
@Composable
private fun JustifyTextSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current

    SmartisanGroup {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            SmartisanJustifyText(
                text = "  锤子音乐的弹窗正文使用两端对齐：除最后一行、空行与以换行符结尾的行之外，" +
                    "每一行都会按「行宽减去本行自然宽度，再除以字符数减一」摊开字距，把整行拉满容器宽度。" +
                    "段首的两个空格是原版的段落缩进，不会被拉伸，所以中文长段落看起来整齐划一。",
                style = typography.body,
                color = colors.textPrimary,
            )
        }
    }
}

/** 密码输入框：点击眼睛图标播放原版帧动画，动画过半才切换明文 / 密文。 */
@Composable
private fun PasswordFieldSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var password by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }

    SmartisanGroup {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SmartisanPasswordField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                placeholder = "请输入密码",
                revealPassword = revealed,
                onRevealPasswordChange = { revealed = it },
            )
            SmartisanText(
                text = "当前状态：${if (revealed) "明文" else "密文"}（点击右侧眼睛切换）",
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
            SmartisanText(
                text = "眼睛是原版 16 帧帧动画（每帧 16ms）：总时长按当前帧到目标帧的距离计算，" +
                    "明文 / 密文在动画过半时才切换，与原版 EyeAnimator 一致。",
                style = typography.caption,
                color = colors.textTertiary,
            )
        }
    }
}

/** 可清空输入框：文本非空且获得焦点时，右侧出现一键清空按钮。 */
@Composable
private fun ClearableFieldSection() {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    var text by remember { mutableStateOf("") }
    var cleared by remember { mutableIntStateOf(0) }

    SmartisanGroup {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SmartisanClearableField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                placeholder = "点一下输入框，输入内容后右侧出现清除按钮",
                onClear = { cleared++ },
            )
            SmartisanText(
                text = "已清空 $cleared 次；清除按钮只在「文本非空且获得焦点」时出现" +
                    "（原版 updateDrawableVisibility）。",
                style = typography.listItemSecondary,
                color = colors.textTertiary,
            )
        }
    }
}
