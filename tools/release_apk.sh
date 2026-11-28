#!/bin/bash
# 构建两个独立的 release APK，使用与 debug 相同的 keystore，支持覆盖安装。
#
# 构建环境：JDK 17 及以上即可（本机默认的 JDK 27 直接用，不用 export JAVA_HOME），
# 由 Gradle 9.8.1 + AGP 9.4.1 保证；仓库里没有任何 JDK 版本固定文件。
set -euo pipefail
cd "$(dirname "$0")/.."

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
BT="$SDK/build-tools/36.0.0"
KS="/tmp/dbg.jks"
# 这个 keystore 就是 Android 的 debug keystore 的副本（签名与 debug 包一致，所以能覆盖安装）；
# /tmp 每次重启都会清掉，丢了就自动复制回来。
[ -f "$KS" ] || cp "$HOME/.android/debug.keystore" "$KS"
OUT="sample/build/outputs/apk/release"
mkdir -p "$OUT"

./gradlew :sample:assembleRelease -q

sign_sample() {
  local layout="$1"
  local name="$2"

  "$BT/zipalign" -p -f 4 "sample/build/outputs/apk/$layout/release/sample-$layout-release-unsigned.apk" "/tmp/$name"
  "$BT/apksigner" sign \
    --ks "$KS" --ks-pass pass:android --key-pass pass:android \
    --out "$OUT/$name" "/tmp/$name"
  "$BT/apksigner" verify --print-certs "$OUT/$name" | head -3
  ls -lh "$OUT/$name"
}

sign_sample gallery smartisanx-sample-release.apk
sign_sample list smartisanx-sampleold-release.apk
