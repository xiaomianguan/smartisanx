package cc.wuersan008.smartisanx.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * smartisanx 的文字样式。
 *
 * 字号取自三个项目的实际资源：标题栏 20sp、弹窗标题 13.5sp、弹窗按钮 17sp、
 * 列表一级 15sp、列表二级 12.5sp、分组标题 13.5sp。
 */
@Immutable
class SmartisanTypography(
    /** 标题栏标题。 */
    val titleBar: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp,
    ),
    /** 页面大标题。 */
    val title: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 26.sp,
    ),
    /** 正文。 */
    val body: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
    ),
    /** 列表一级文字。 */
    val listItemPrimary: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    /** 列表二级文字。 */
    val listItemSecondary: TextStyle = TextStyle(
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 17.sp,
    ),
    /** 分组标题。 */
    val sectionTitle: TextStyle = TextStyle(
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    ),
    /**
     * framework 列表行一级文字（17sp，原版 `primary_text_size`）。
     *
     * ⚠️ 与 [listItemPrimary]（15sp）**不是一个来源**：15sp 来自三个复刻项目里的应用版本，
     * 17sp 是坚果 R2 framework `list_content_mid_primary_*` 的取值。
     * framework 列表行（[cc.wuersan008.smartisanx.ui.layout.SmartisanListRow]）用这一档。
     */
    val listRowPrimary: TextStyle = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 23.sp,
    ),
    /** framework 列表行一级文字的紧凑版（16sp，原版 `primary_text_size_alt`）。 */
    val listRowPrimaryAlt: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
    ),
    /** framework 列表行二级文字（15sp，原版 `secondary_text_size`，三行版的第二行）。 */
    val listRowSecondary: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    /** framework 列表行三级文字（13.5sp，原版 `tertiary_text_size`）。 */
    val listRowTertiary: TextStyle = TextStyle(
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    ),
    /** framework 列表行三级文字的紧凑版（12.5sp，原版 `tertiary_text_size_alt`）。 */
    val listRowTertiaryAlt: TextStyle = TextStyle(
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 17.sp,
    ),
    /** framework 列表行第四级文字（12sp，原版 `quaternary_text_size`）。 */
    val listRowQuaternary: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    /** 开关行标题（18sp，原版 `switch_title_size`）。 */
    val listRowSwitchTitle: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
    ),
    /** 行内小号说明（10sp，原版 `item_sub_title_size`）。 */
    val listItemCaptionSmall: TextStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 13.sp,
    ),
    /** framework 编辑行的标签（12sp，原版 `EditorLabelTextStyle`）。 */
    val editorLabel: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    /** framework 编辑行的输入框文字（15sp，原版 `EditorTextStyle`）。 */
    val editorField: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
    ),
    /** 按钮文字。 */
    val button: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 19.sp,
    ),
    /** 弹窗按钮文字。 */
    val dialogButton: TextStyle = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 23.sp,
    ),
    /** 弹窗标题文字。 */
    val dialogTitle: TextStyle = TextStyle(
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp,
    ),
    /** 说明、页脚文字。 */
    val caption: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    ),
    /** 数字（等宽数字，避免时间跳动）。 */
    val numeric: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum",
    ),
    /** 机械感大号数字，用于时钟、计时器。 */
    val displayNumeric: TextStyle = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Light,
        lineHeight = 56.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
)

/** 当前 [SmartisanTypography]。 */
val LocalSmartisanTypography: ProvidableCompositionLocal<SmartisanTypography> =
    staticCompositionLocalOf { SmartisanTypography() }

/**
 * 把字体族套用到全部文字样式上。
 *
 * [text] 用于正文，[numerals] 用于等宽数字与大号数字（时钟、计时器）。
 * 传 `null` 表示该处跟随系统默认字体。
 */
fun SmartisanTypography.withFonts(
    text: androidx.compose.ui.text.font.FontFamily?,
    numerals: androidx.compose.ui.text.font.FontFamily? = text,
): SmartisanTypography =
    SmartisanTypography(
        titleBar = titleBar.copy(fontFamily = text),
        title = title.copy(fontFamily = text),
        body = body.copy(fontFamily = text),
        listItemPrimary = listItemPrimary.copy(fontFamily = text),
        listItemSecondary = listItemSecondary.copy(fontFamily = text),
        sectionTitle = sectionTitle.copy(fontFamily = text),
        listRowPrimary = listRowPrimary.copy(fontFamily = text),
        listRowPrimaryAlt = listRowPrimaryAlt.copy(fontFamily = text),
        listRowSecondary = listRowSecondary.copy(fontFamily = text),
        listRowTertiary = listRowTertiary.copy(fontFamily = text),
        listRowTertiaryAlt = listRowTertiaryAlt.copy(fontFamily = text),
        listRowQuaternary = listRowQuaternary.copy(fontFamily = text),
        listRowSwitchTitle = listRowSwitchTitle.copy(fontFamily = text),
        listItemCaptionSmall = listItemCaptionSmall.copy(fontFamily = text),
        editorLabel = editorLabel.copy(fontFamily = text),
        editorField = editorField.copy(fontFamily = text),
        button = button.copy(fontFamily = text),
        dialogButton = dialogButton.copy(fontFamily = text),
        dialogTitle = dialogTitle.copy(fontFamily = text),
        caption = caption.copy(fontFamily = text),
        numeric = numeric.copy(fontFamily = numerals ?: text),
        displayNumeric = displayNumeric.copy(fontFamily = numerals ?: text),
    )
