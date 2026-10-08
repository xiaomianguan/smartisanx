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
 * 原版 Smartisan OS 的界面字体是系统字体 **smartisan-compact-cns**，
 * 它位于 ROM 的 `/system/fonts/`，并不在任何一个 APK 里 ——
 * 本项目能拿到的原厂资源（12 个 APK + `framework-res.apk` + `smartisanos_11.apk`）
 * 都不包含它，所以本库改用同一套系统里能拿到的锤子字体作为默认。
 *
 * 想换成真正的 `smartisan-compact-cns`（或任何自己的字体）见下文的用法。
 */
enum class SmartisanFontMode {
    /**
     * 锤子原厂字体（**默认**）。
     *
     * 文字用 `FZCCHK`（方正粗黑宋简体，`smartisanos_11.apk` 的 `assets/FZCCHK.TTF`），
     * 机械数字用 `SmartisanClock` 三档字重（原版时钟表盘与计时器用的就是它）。
     */
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
         * 锤子原厂字体：正文 `FZCCHK`，机械数字 `SmartisanClock`。
         *
         * 这几个字体文件都取自原厂资源，直接放在 `library/core/src/main/res/font/`。
         */
        val Original: SmartisanFonts =
            SmartisanFonts(
                text =
                    FontFamily(
                        Font(R.font.fzcchk, FontWeight.Normal),
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
