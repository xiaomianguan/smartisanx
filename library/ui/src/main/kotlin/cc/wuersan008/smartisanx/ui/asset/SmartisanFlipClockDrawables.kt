package cc.wuersan008.smartisanx.ui.asset

import androidx.annotation.DrawableRes
import cc.wuersan008.smartisanx.ui.R

/**
 * 锁屏无线充电「翻页时钟」的素材索引。
 *
 * 来源：`KeyguardSmartisan`（Android 11 darwin 版，
 * `com.smartisanos.keyguard.widgets.flipnumber.FlipNumber` + `res/drawable-xxhdpi/flip_*.png`）。
 *
 * 原版一套素材是**「一个两位数卡片」的四个象限**：一张 908×936px 的卡片，上下各 468px、
 * 左右各 454px，四个象限各有一张 454×468px 的图；名字里的
 * `left` / `right` 是十位 / 个位，`top` / `bottom` 是上半 / 下半：
 *
 * | 象限 | 十位（左） | 个位（右） |
 * | --- | --- | --- |
 * | 上半 | `flip_{d}_left_top` | `flip_{d}_right_top` |
 * | 下半 | `flip_{d}_left_bottom` | `flip_{d}_right_bottom` |
 *
 * 上半的数字是**灰色**、下半是**白色**（翻页时钟的分界观感直接画在素材里），
 * 卡片本身还带一层 [CoverTop] / [CoverBottom] 覆盖层（原版 `cover_top` / `cover_bottom`，alpha 229 / 127），
 * 翻页时再加一层 [CoverBottomShadow]（原版 `cover_bottom_shadow`，alpha 0 → 1）。
 *
 * 原版只有 0–6 的**左**半格：十位最大只到 6（`FlipNumber.setNumber` 把数值 clamp 到 60，
 * 时 / 分的十位不可能出现 7 / 8 / 9）。[digit] 遇到缺的左半格会退回同一象限的右半格，
 * 不会崩，但那是原版不会出现的状态。
 */
object SmartisanFlipClockDrawables {
    /** 卡片左 / 右两侧的铰链（原版 `flip_axle`，24×120px，竖直渐变圆柱）。 */
    @DrawableRes val Axle = R.drawable.flip_axle

    /** 上半静止覆盖层（原版 `cover_top`，908×468px，alpha 229）。 */
    @DrawableRes val CoverTop = R.drawable.cover_top

    /** 下半静止覆盖层（原版 `cover_bottom`，908×468px，alpha 127）。 */
    @DrawableRes val CoverBottom = R.drawable.cover_bottom

    /** 翻页时落在下半的投影（原版 `cover_bottom_shadow`，908×468px，alpha 0 → 1）。 */
    @DrawableRes val CoverBottomShadow = R.drawable.cover_bottom_shadow

    private val LeftTop =
        intArrayOf(
            R.drawable.flip_0_left_top,
            R.drawable.flip_1_left_top,
            R.drawable.flip_2_left_top,
            R.drawable.flip_3_left_top,
            R.drawable.flip_4_left_top,
            R.drawable.flip_5_left_top,
            R.drawable.flip_6_left_top,
        )
    private val LeftBottom =
        intArrayOf(
            R.drawable.flip_0_left_bottom,
            R.drawable.flip_1_left_bottom,
            R.drawable.flip_2_left_bottom,
            R.drawable.flip_3_left_bottom,
            R.drawable.flip_4_left_bottom,
            R.drawable.flip_5_left_bottom,
            R.drawable.flip_6_left_bottom,
        )
    private val RightTop =
        intArrayOf(
            R.drawable.flip_0_right_top,
            R.drawable.flip_1_right_top,
            R.drawable.flip_2_right_top,
            R.drawable.flip_3_right_top,
            R.drawable.flip_4_right_top,
            R.drawable.flip_5_right_top,
            R.drawable.flip_6_right_top,
            R.drawable.flip_7_right_top,
            R.drawable.flip_8_right_top,
            R.drawable.flip_9_right_top,
        )
    private val RightBottom =
        intArrayOf(
            R.drawable.flip_0_right_bottom,
            R.drawable.flip_1_right_bottom,
            R.drawable.flip_2_right_bottom,
            R.drawable.flip_3_right_bottom,
            R.drawable.flip_4_right_bottom,
            R.drawable.flip_5_right_bottom,
            R.drawable.flip_6_right_bottom,
            R.drawable.flip_7_right_bottom,
            R.drawable.flip_8_right_bottom,
            R.drawable.flip_9_right_bottom,
        )

    /**
     * 取某个数字在某一个象限里的素材。
     *
     * @param digit 0–9
     * @param right true 取个位（卡片右半格），false 取十位（左半格）
     * @param top true 取上半，false 取下半
     */
    @DrawableRes
    fun digit(digit: Int, right: Boolean, top: Boolean): Int {
        val d = digit.coerceIn(0, 9)
        val table =
            when {
                right && top -> RightTop
                right -> RightBottom
                top -> LeftTop
                else -> LeftBottom
            }
        if (d < table.size) return table[d]
        // 十位 7–9 原版没有素材，退回同一象限的个位图（只为了避免越界，原版不会走到这里）。
        return if (top) RightTop[d] else RightBottom[d]
    }
}
