#!/bin/bash
# 打一个可以装的 release 包并发到 Telegram：与 debug 同一个 keystore，所以能覆盖安装。
#
# 注意：release（R8 + 资源压缩）在 JDK 27 上会直接失败（Gradle 客户端只抛一句 “27”），
# 用 JDK 17 才正常；这里显式挑一个可用的 JDK。
set -euo pipefail
cd "$(dirname "$0")/.."

BT="$HOME/Library/Android/sdk/build-tools/35.0.0"
KS="/tmp/dbg.jks"
OUT="sample/build/outputs/apk/release"
NAME="smartisanx-sample-release.apk"

if [[ -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
fi

./gradlew :sample:assembleRelease -q

"$BT/zipalign" -p -f 4 "$OUT/sample-release-unsigned.apk" "/tmp/$NAME"
"$BT/apksigner" sign \
  --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --out "$OUT/$NAME" "/tmp/$NAME"
"$BT/apksigner" verify --print-certs "$OUT/$NAME" | head -3

ls -lh "$OUT/$NAME"
