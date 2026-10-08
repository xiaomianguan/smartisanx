package cc.wuersan008.smartisanx.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import cc.wuersan008.smartisanx.core.R

/**
 * 字体方案。
 *
 * 默认使用 Smartisan OS 的**系统字体 `Smartisan Compact CNS`**
 * （方正出品，字体内嵌名称为 `Smartisan Compact CNS`，文件名为 `Smartisan_Compact-*.otf`）。
 * 字体取自坚果 R2 的官方 ROM 转储：
 * `dumps.tadiphone.dev/dumps/smartisan/darwin` → `system/system/fonts/`。
 */
enum class SmartisanFontMode {
    /** 锤子原厂字体（**默认**）。 */
    Original,

    /** 跟随系统默认字体，不加载任何字体文件。 */
    SystemDefault,

    /** 使用调用方自己提供的字体。 */
    Custom,
}

/**
 * smartisanx 的字体。
 *
 * @param text 正文字体族，`null` 表示跟随系统
 * @param numerals 机械数字字体族（时钟、计时器），`null` 表示与 [text] 相同
 */
@Immutable
class SmartisanFonts(
    val text: FontFamily?,
    val numerals: FontFamily?,
) {
    companion object {
        /**
         * 锤子原厂字体。
         *
         * 正文用系统字体 `Smartisan Compact CNS`，按 ROM 里的字重逐个接入：
         *
         * | Compose 字重 | ROM 里的文件 |
         * | --- | --- |
         * | `Light` (300) | `Smartisan_Compact-Light.otf` |
         * | `Normal` (400) | `Smartisan_Compact-Regular.otf` |
         * | `Medium` (500) | `Smartisan_Compact-Medium.otf` |
         * | `Bold` (700) | `Smartisan_Compact-Bold.otf` |
         *
         * ROM 里还有 `Thin`(100) 与 `Heavy`(900) 两档，本库没有引用到，
         * 需要时把它们放进 `library/core/src/main/res/font/` 再补进这里即可。
         *
         * 机械数字仍用时钟那套 `SmartisanClock`（表盘与计时器原本就是它）。
         */
        val Original: SmartisanFonts =
            SmartisanFonts(
                text =
                    FontFamily(
                        Font(R.font.smartisan_compact_light, FontWeight.Light),
                        Font(R.font.smartisan_compact_regular, FontWeight.Normal),
                        Font(R.font.smartisan_compact_medium, FontWeight.Medium),
                        Font(R.font.smartisan_compact_bold, FontWeight.Bold),
                    ),
                numerals =
                    FontFamily(
                        Font(R.font.smartisan_clock_light, FontWeight.Light),
                        Font(R.font.smartisan_clock, FontWeight.Normal),
                        Font(R.font.smartisan_clock_bold, FontWeight.Bold),
                    ),
            )

        /** 系统默认字体。 */
        val SystemDefault: SmartisanFonts = SmartisanFonts(text = null, numerals = null)

        /** 用调用方自己的字体族构造。 */
        fun custom(text: FontFamily?, numerals: FontFamily? = null): SmartisanFonts =
            SmartisanFonts(text = text, numerals = numerals ?: text)
    }
}

/**
 * 当前字体方案。
 *
 * 由 [SmartisanTheme] 提供，默认 [SmartisanFonts.Original]。
 */
val LocalSmartisanFonts: ProvidableCompositionLocal<SmartisanFonts> =
    staticCompositionLocalOf { SmartisanFonts.Original }
