#!/bin/bash
# 打一个可以装的 release 包并发到 Telegram：与 debug 同一个 keystore，所以能覆盖安装。
#
# 构建环境：JDK 17 及以上即可（本机默认的 JDK 27 直接用，不用 export JAVA_HOME），
# 由 Gradle 9.8.1 + AGP 9.4.1 保证；仓库里没有任何 JDK 版本固定文件。
set -euo pipefail
cd "$(dirname "$0")/.."

BT="$HOME/Library/Android/sdk/build-tools/36.0.0"
KS="/tmp/dbg.jks"
# 这个 keystore 就是 Android 的 debug keystore 的副本（签名与 debug 包一致，所以能覆盖安装）；
# /tmp 每次重启都会清掉，丢了就自动复制回来。
[ -f "$KS" ] || cp "$HOME/.android/debug.keystore" "$KS"
OUT="sample/build/outputs/apk/release"
NAME="smartisanx-sample-release.apk"

./gradlew :sample:assembleRelease -q

"$BT/zipalign" -p -f 4 "$OUT/sample-release-unsigned.apk" "/tmp/$NAME"
"$BT/apksigner" sign \
  --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/$NAME" "/tmp/$NAME"
"$BT/apksigner" verify --print-certs "$OUT/$NAME" | head -3

ls -lh "$OUT/$NAME"
