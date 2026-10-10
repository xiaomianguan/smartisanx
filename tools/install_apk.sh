#!/bin/bash
# 装机助手：某些 ROM（如 nubia / ZTE）的 `adb install` 会弹「是否允许通过 USB 安装」
# 对话框并**一直阻塞**，直到有人点「允许」。这里把 install 放到后台跑，同时轮询是否有这个
# 对话框，有就自动点掉它，最后再读 install 的结果。
#
# 用法：
#   tools/install_apk.sh <apk 路径> [设备序列号]
#
# 没弹对话框（普通 ROM、模拟器）时行为与直接 `adb install -r` 一样。
set -uo pipefail
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"

APK="${1:?用法: tools/install_apk.sh <apk> [serial]}"
DEVICE="${2:-}"
ADB=(adb)
[ -n "$DEVICE" ] && ADB=(adb -s "$DEVICE")

LOG="$(mktemp -t install_apk.XXXXXX.log)"
: > "$LOG"
("${ADB[@]}" install -r "$APK" >> "$LOG" 2>&1) &
INSTALL_PID=$!

# 弹窗里「允许 / 安装」是唯一的蓝色文字，取蓝色像素质心当点击坐标。
tap_blue_text() {
    "${ADB[@]}" exec-out screencap > /tmp/_install_probe.raw 2>/dev/null || return
    python3 - <<'PY'
import struct

blob = open('/tmp/_install_probe.raw', 'rb').read()
width, height, fmt, _ = struct.unpack('<IIII', blob[:16])
data = blob[16:]
bpp = 4 if fmt in (1, 2) else (2 if fmt == 4 else 3)
xs, ys = [], []
for y in range(int(height * 0.75), height):   # 弹窗按钮在下半屏
    base = y * width * bpp
    for x in range(width):
        off = base + x * bpp
        r, g, b = data[off], data[off + 1], data[off + 2]
        if b > 140 and b > r + 40 and g < b and g < 200:
            xs.append(x)
            ys.append(y)
if xs:
    print(f'{sum(xs) // len(xs)} {sum(ys) // len(ys)}')
PY
}

for _ in $(seq 1 120); do
    kill -0 "$INSTALL_PID" 2>/dev/null || break
    if "${ADB[@]}" shell dumpsys window 2>/dev/null | grep -qiE 'mCurrentFocus.*install'; then
        COORD="$(tap_blue_text)"
        if [ -n "$COORD" ]; then
            echo "点掉 USB 安装确认弹窗: $COORD"
            # shellcheck disable=SC2086
            "${ADB[@]}" shell input tap $COORD
        fi
    fi
    sleep 2
done

wait "$INSTALL_PID"
RESULT=$?
cat "$LOG"
rm -f "$LOG"
if [ "$RESULT" = 0 ]; then
    echo "INSTALL OK: $APK"
else
    echo "INSTALL FAILED: $APK"
fi
exit "$RESULT"
