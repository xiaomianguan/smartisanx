#!/usr/bin/env bash
# 深挖某个应用里的若干自绘组件，给出「可核对的证据」：
#   - 类的 smali 规模与继承
#   - 它用到的资源（smali 里的资源 ID 经 public.xml 换成名字）
#   - 哪些布局引用了它（并打印这些布局）
#   - 它引用的素材文件大小
#
# 用法:
#   tools/app_probe.sh <dump 内的应用目录> <类名1> [类名2 ...]
#   例: tools/app_probe.sh system/system/app/CleanerSmartisan RayLoopView
set -uo pipefail

DIR="${1:-}"; shift || true
CLASSES=("$@")
[ -z "$DIR" ] && { echo "用法: app_probe.sh <应用目录> <类名...>"; exit 2; }

REF=${SWEEP_REF:-qssi-user-11-RKQ1.201217.002-1-dev-keys}
BASE=${SWEEP_BASE:-https://dumps.tadiphone.dev/dumps/smartisan/darwin/-/raw}
WORK=${PROBE_WORK:-/tmp/probe}
NAME=$(basename "$DIR")
APKTOOL=${SWEEP_APKTOOL:-/Users/puffercat/apktool.jar}
DEC="$WORK/${NAME}_full"
APK="$WORK/$NAME.apk"
OUT="$WORK/$NAME.probe.txt"

mkdir -p "$WORK"
{
  echo "================================================================"
  echo "## $NAME   ($DIR)"
  echo "类: ${CLASSES[*]}"
} > "$OUT"

if [ ! -d "$DEC/smali" ] && [ ! -d "$DEC/smali_classes2" ]; then
  [ -f "$APK" ] || curl -sL --max-time 900 -o "$APK" "$BASE/$REF/$DIR/$NAME.apk"
  echo "解包 $(du -h "$APK" | cut -f1) ..." >> "$OUT"
  java -jar "$APKTOOL" d -f "$APK" -o "$DEC" > "$WORK/$NAME.probe.apktool.log" 2>&1
fi

# 资源 ID → 名字
python3 - "$DEC" <<'PY' > "$WORK/$NAME.resmap.txt"
import re, sys, glob
dec = sys.argv[1]
m = {}
for f in glob.glob(f"{dec}/res/values/public.xml"):
    for line in open(f, encoding="utf-8", errors="replace"):
        mm = re.match(r'\s*<public type="(\w+)" name="([^"]+)" id="(0x[0-9a-f]+)"', line)
        if mm:
            m[int(mm.group(3), 16)] = f"{mm.group(1)}/{mm.group(2)}"
for k, v in sorted(m.items()):
    print(f"{k:#010x} {v}")
PY

echo "---- 每个类的证据 ----" >> "$OUT"
for C in "${CLASSES[@]}"; do
  echo "" >> "$OUT"
  echo "### $C" >> "$OUT"
  files=$(find "$DEC" -name "$C.smali" -o -name "$C\$*.smali" 2>/dev/null | sort)
  if [ -z "$files" ]; then echo "  (没找到 smali)" >> "$OUT"; continue; fi
  for f in $files; do
    lines=$(wc -l < "$f" | tr -d ' ')
    sup=$(grep -m1 '^\.super' "$f" | sed 's/^\.super //')
    echo "  $f  ($lines 行, super=$sup)" >> "$OUT"
  done
  # 这个类用到的资源
  echo "  用到的资源:" >> "$OUT"
  grep -oh '0x7f[0-9a-f]\{6\}' $files 2>/dev/null | sort -u | while read -r id; do
    grep -m1 "^$(printf '0x%08x' "$id")" "$WORK/$NAME.resmap.txt" | sed 's/^/    /'
  done | sort -u -k2 | head -40 >> "$OUT"
  # 引用了这个类的布局
  echo "  引用它的布局:" >> "$OUT"
  for lay in $(grep -rl "$C" "$DEC"/res/layout*/*.xml 2>/dev/null | sort -u | head -6); do
    echo "    --- $lay" >> "$OUT"
    grep -n "$C" -A 6 "$lay" | head -24 | sed 's/^/      /' >> "$OUT"
  done
done
echo "(证据写到 $OUT)"
cat "$OUT"
