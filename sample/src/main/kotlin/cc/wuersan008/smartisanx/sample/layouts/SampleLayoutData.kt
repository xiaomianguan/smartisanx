package cc.wuersan008.smartisanx.sample.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanColors
import cc.wuersan008.smartisanx.core.theme.LocalSmartisanTypography
import cc.wuersan008.smartisanx.ui.basic.SmartisanSurface
import cc.wuersan008.smartisanx.ui.basic.SmartisanText

/**
 * 整页布局示例用的假数据。
 *
 * 这些数据只为了让页面看起来像真的，不参与任何业务逻辑。
 * 字母分组直接按拼音首字母写死（原版是按歌曲名 / 联系人名的拼音首字母分组的），
 * 所以下面的分组字母是**内容的一部分**：改数据时记得让首字母和分组对得上。
 */

/** 一首歌。 */
internal data class SampleSong(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
)

/** 按拼音首字母分好组的歌曲（锤子音乐「我的音乐」页的分组方式）。 */
internal val SampleSongSections: List<Pair<Char, List<SampleSong>>> =
    listOf(
        'A' to listOf(SampleSong("安和桥", "宋冬野", "安和桥北", "4:21")),
        'B' to listOf(SampleSong("白桦林", "朴树", "我去2000年", "4:34")),
        'C' to listOf(SampleSong("成都", "赵雷", "无法长大", "5:28")),
        'D' to listOf(SampleSong("董小姐", "宋冬野", "安和桥北", "4:20")),
        'E' to listOf(SampleSong("儿时", "刘昊霖", "儿时", "4:30")),
        'F' to listOf(SampleSong("富士山下", "陈奕迅", "What's Going On…?", "4:10")),
        'G' to listOf(SampleSong("光辉岁月", "Beyond", "命运派对", "5:16")),
        'H' to listOf(SampleSong("海阔天空", "Beyond", "乐与怒", "5:25")),
        'J' to listOf(SampleSong("加州旅馆", "Eagles", "Hotel California", "6:30")),
        'L' to listOf(SampleSong("理想", "赵雷", "无法长大", "4:47")),
        'N' to listOf(SampleSong("南方姑娘", "赵雷", "赵小雷", "4:39")),
        'P' to listOf(SampleSong("平凡之路", "朴树", "猎户星座", "5:03")),
        'Q' to listOf(SampleSong("晴天", "周杰伦", "叶惠美", "4:29")),
        'R' to listOf(SampleSong("人生海海", "五月天", "人生海海", "4:28")),
        'S' to listOf(SampleSong("山丘", "李宗盛", "山丘", "5:31")),
        'T' to listOf(SampleSong("童年", "罗大佑", "之乎者也", "4:37")),
        'W' to listOf(SampleSong("我是一只小小鸟", "赵传", "我很丑，可是我很温柔", "4:41")),
        'X' to listOf(SampleSong("小幸运", "田馥甄", "我的少女时代", "4:25")),
        'Y' to listOf(SampleSong("野子", "苏运莹", "冥明", "4:36")),
        'Z' to listOf(SampleSong("再见", "张震岳", "再见", "4:14")),
    )

/** 一条闹钟。 */
internal data class SampleAlarm(
    val time: String,
    val repeat: String,
    val label: String,
    val enabled: Boolean,
)

/** 闹钟列表。 */
internal val SampleAlarms: List<SampleAlarm> =
    listOf(
        SampleAlarm("07:30", "周一至周五", "起床", true),
        SampleAlarm("09:00", "每天", "晨会提醒", true),
        SampleAlarm("13:30", "周一、周三、周五", "午休结束", false),
        SampleAlarm("22:30", "周六、周日", "睡前阅读", false),
    )

/** 世界时钟里的一座城市。 */
internal data class SampleCity(
    val city: String,
    val zone: String,
)

/** 世界时钟列表（原版按城市拼音首字母分组）。 */
internal val SampleCitySections: List<Pair<Char, List<SampleCity>>> =
    listOf(
        'B' to listOf(SampleCity("北京", "Asia/Shanghai")),
        'L' to listOf(SampleCity("伦敦", "Europe/London"), SampleCity("洛杉矶", "America/Los_Angeles")),
        'N' to listOf(SampleCity("纽约", "America/New_York")),
        'S' to listOf(SampleCity("首尔", "Asia/Seoul")),
        'X' to listOf(SampleCity("悉尼", "Australia/Sydney")),
        'Z' to listOf(SampleCity("东京", "Asia/Tokyo")),
    )

/** 一位联系人。 */
internal data class SampleContact(
    val name: String,
    val phone: String,
)

/** 按拼音首字母分好组的联系人。 */
internal val SampleContactSections: List<Pair<Char, List<SampleContact>>> =
    listOf(
        'A' to listOf(SampleContact("安琪", "138 0013 8000")),
        'B' to listOf(SampleContact("白雪", "139 0013 9000")),
        'C' to listOf(SampleContact("陈晨", "137 0013 7000")),
        'D' to listOf(SampleContact("邓超", "136 0013 6000")),
        'F' to listOf(SampleContact("方舟", "135 0013 5000")),
        'G' to listOf(SampleContact("高山", "134 0013 4000")),
        'H' to listOf(SampleContact("韩梅", "133 0013 3000")),
        'J' to listOf(SampleContact("姜涛", "132 0013 2000")),
        'L' to listOf(SampleContact("李雷", "131 0013 1000")),
        'M' to listOf(SampleContact("马丽", "130 0013 0000")),
        'N' to listOf(SampleContact("倪妮", "188 0013 8000")),
        'O' to listOf(SampleContact("欧阳", "187 0013 7000")),
        'P' to listOf(SampleContact("潘石", "186 0013 6000")),
        'Q' to listOf(SampleContact("钱进", "185 0013 5000")),
        'R' to listOf(SampleContact("任翔", "184 0013 4000")),
        'S' to listOf(SampleContact("孙燕", "183 0013 3000")),
        'T' to listOf(SampleContact("唐唐", "182 0013 2000")),
        'W' to listOf(SampleContact("王芳", "181 0013 1000")),
        'X' to listOf(SampleContact("徐静", "180 0013 0000")),
        'Y' to listOf(SampleContact("杨帆", "189 0013 9000")),
        'Z' to listOf(SampleContact("张伟", "177 0013 7000")),
    )

/** 一条笔记。 */
internal data class SampleNote(
    val title: String,
    val time: String,
    val summary: String,
    val starred: Boolean,
)

/** 笔记列表（原版是「时间在上、正文在下」，这里用列表行的「标题 + 摘要」两行呈现）。 */
internal val SampleNotes: List<SampleNote> =
    listOf(
        SampleNote("周会纪要", "今天 09:12", "1. 组件库 v1.2 冻结；2. 真机核对补齐设置页；3. 下周发版", true),
        SampleNote("读书笔记 · 设计中的设计", "昨天 21:40", "「设计不是把东西做得漂亮，而是把问题解决得漂亮。」", false),
        SampleNote("购物清单", "周三 19:05", "咖啡豆、牛奶、洗衣液、灯泡（E27 暖光）", false),
        SampleNote("出差报销", "周一 14:22", "机票 1,280 · 酒店 1,050 · 市内交通 96", false),
        SampleNote("灵感", "上周五", "把「标题栏 + 列表 + 底部标签栏」做成可复用的页面骨架。", true),
    )

/** 收件箱里的一封消息（给「列表 → 详情」用的数据）。 */
internal data class SampleMessage(
    val sender: String,
    val subject: String,
    val time: String,
    val body: String,
)

/** 收件箱列表。 */
internal val SampleMessages: List<SampleMessage> =
    listOf(
        SampleMessage(
            sender = "设计评审",
            subject = "本周设计评审安排",
            time = "10:24",
            body = "本周评审改到周四下午三点，会议室 B。\n\n" +
                "需要提前准备的：\n" +
                "· 组件库本周改动清单\n" +
                "· 真机核对截图\n" +
                "· 遗留问题与下周计划\n\n" +
                "如果时间冲突请提前告诉我。",
        ),
        SampleMessage(
            sender = "王芳",
            subject = "关于列表行高的疑问",
            time = "09:02",
            body = "原版列表行是 60dp，两行文字的行会到 90dp，这个我理解。\n\n" +
                "但带副标题的那一行，副标题和标题之间的间距在原版里是 3dp 还是 4dp？" +
                "我量了几次都在 3.5dp 左右。",
        ),
        SampleMessage(
            sender = "构建机器人",
            subject = "构建成功：main",
            time = "昨天",
            body = "分支 main 构建成功。\n\n耗时 4 分 12 秒，lint 报告结尾为 No issues found.。",
        ),
        SampleMessage(
            sender = "李雷",
            subject = "周末爬山",
            time = "昨天",
            body = "周六早上六点半在老地方集合，记得带水。",
        ),
    )

/**
 * 列表里的头像。
 *
 * 原版联系人列表的头像是 `AvatarImageView`：显示联系人照片，没有照片时用默认头像。
 * 示例里没有照片素材，所以画一个「主题底色 + 首字」的圆形占位 ——
 * 这样既不引入新素材，也不会用错原版的图片资源。
 */
@Composable
internal fun SampleAvatar(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val colors = LocalSmartisanColors.current
    val typography = LocalSmartisanTypography.current
    SmartisanSurface(modifier = modifier.size(size), shape = CircleShape, color = colors.surfaceRaised) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            SmartisanText(
                text = text,
                style = typography.listItemSecondary,
                color = colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}
