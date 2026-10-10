#!/usr/bin/env bash
#
# 由 .github/assets/smartisanx.png 生成示例应用（sample 模块）的图标资源。
#
# 规格：
#   自适应图标（Android 8.0+）：108dp 画布，白色底图 #ffffff + 透明前景，图形高 56dp。
#     只有自适应图标才要自带底图，所以白底只出现在这里。
#     注意「图形高」不等于「外接圆直径」：源图是等轴测的六边形纸箱，剪影最宽处是左右两条
#     竖边、且竖边顶端离中心最远（实测 0.4995 × 图形高，见 LOGO_RADIUS_RATIO），
#     所以 56dp 的图形外接圆半径只有约 28dp：
#       - 安全圆的半径是 33dp（66dp 直径，Google 保证任意遮罩下都可见），留 5dp 余量；
#       - 启动器圆形遮罩的半径最多 36dp（72dp 可见区），留 8dp 余量。
#     圆形 / 方圆形 / 泪滴形遮罩都不会切到纸箱的角，白色环宽也匀称。
#   自适应图标只能放在 `mipmap-anydpi-v26/`，`-v26` 不能省：一旦改成 `mipmap-anydpi/`，
#     AGP 会把这个资源整个从 APK 里丢掉（单独 `aapt2 compile` 那张 XML 却是能过的），
#     `@mipmap/ic_launcher` 于是落到 library/ui 里那张原版 `mipmap-400dpi/ic_launcher.png`，
#     启动器上就显示成原版的指南针图标。原因见 sample/lint.xml 里 ObsoleteSdkInt 那条注释。
#   不再生成传统图标（`mipmap-*/ic_launcher.png`）：minSdk 26 的设备都走自适应图标那条加载
#     路径，非自适应图标只有 Android 8.0 以下和少数第三方启动器才会用，留着是死资源。
#     同理也没有 `ic_launcher_round`：自适应图标由启动器按自己的遮罩形状裁切，
#     manifest 里也没有 `roundIcon`。
#   不写 `<monochrome>`：主题图标（Android 13+）要的是「实心剪影」，而这里的源图是彩色位图，
#     得另外出一份剪影素材才能支持，现在先不做（见 sample/lint.xml 的 MonochromeLauncherIcon）。
#   另外还生成「关于本机」页（AboutPage）用的 logo：`drawable-*/about_logo.png`，
#     透明底、图形高 88dp，尺寸照原版设置页那张 `about_logo` 9-patch 里红色锁图占的高度。
#
# 依赖：ImageMagick（magick）；有 oxipng 就顺便无损压一遍。
# 用法：tools/gen_sample_icon.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/.github/assets/smartisanx.png"
RES="$ROOT/sample/src/main/res"

CANVAS_DP=108        # 自适应图标画布
LOGO_DP=56           # 自适应前景里的图形高度（外接圆半径 ≈ 28dp）
ABOUT_LOGO_DP=88     # 「关于本机」页 logo 的图形高度

# 源图剪影的外接圆半径 / 图形高度，由 .github/assets/smartisanx.png 的 alpha 通道实测得到：
# 剪影是六边形，离中心最远的是左右两条竖边的上端，距离为 0.49946 × 图形高度。
LOGO_RADIUS_RATIO=0.4995
SAFE_RADIUS_DP=33    # 自适应图标安全圆半径，前景图形的外接圆半径不能超过它

[ -f "$SRC" ] || { echo "找不到源图：$SRC" >&2; exit 1; }
command -v magick >/dev/null || { echo "需要 ImageMagick（magick）" >&2; exit 1; }

# 前景图形的外接圆必须留在安全圆以内，这是「图形不被任何遮罩裁到」的硬性条件
awk "BEGIN{exit !($LOGO_DP*$LOGO_RADIUS_RATIO <= $SAFE_RADIUS_DP)}" || {
    echo "警告：图形高 ${LOGO_DP}dp 的外接圆半径已超出安全圆 ${SAFE_RADIUS_DP}dp" >&2
}

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# 输出目录与文件
mkdir -p "$RES/mipmap-anydpi-v26"
cat > "$RES/mipmap-anydpi-v26/ic_launcher.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<!-- 自适应图标：白色底图 + 原版图形作前景。 -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
XML
# 没有圆形图标要清：manifest 里只有 android:icon，没有 roundIcon

for spec in "mdpi 1" "hdpi 1.5" "xhdpi 2" "xxhdpi 3" "xxxhdpi 4"; do
    dpi="${spec% *}"; f="${spec#* }"
    fg=$(awk "BEGIN{printf \"%d\", $CANVAS_DP*$f}")
    fgh=$(awk "BEGIN{printf \"%d\", $LOGO_DP*$f}")

    mkdir -p "$RES/drawable-$dpi"

    # 图形：按目标高度缩小（Lanczos 缩小时质量最好）
    magick "$SRC" -filter Lanczos -resize "x$fgh" "$TMP/fg_logo.png"

    # 自适应前景：透明画布 + 居中图形（保留透明度，白色底图从空隙里透出来）
    magick -size "${fg}x${fg}" xc:none "$TMP/fg_logo.png" -gravity center -composite \
        -define png:compression-level=9 -strip "$RES/drawable-$dpi/ic_launcher_foreground.png"

    # 「关于本机」页的 logo：透明底、只有图形本身（卡片的白底由页面自己画）。
    # 高度 88dp 是照原版那张 `about_logo` 9-patch 里红色锁图占的高度定的：
    # 原版锁图整体是 336×180dp，红色部分（圆标 + 字标 + based on Android）占 40dp..127.7dp。
    al=$(awk "BEGIN{printf \"%d\", $ABOUT_LOGO_DP*$f}")
    magick "$SRC" -filter Lanczos -resize "x$al" \
        -define png:compression-level=9 -strip "$RES/drawable-$dpi/about_logo.png"

    echo "  ${dpi}: foreground ${fg}px (logo ${fgh}px), about ${al}px"
done

if command -v oxipng >/dev/null; then
    oxipng -q -o4 --strip safe "$RES"/drawable-*/ic_launcher_foreground.png \
        "$RES"/drawable-*/about_logo.png
    echo "  oxipng 已无损压缩"
fi

echo "完成：样例图标已生成到 sample/src/main/res"
