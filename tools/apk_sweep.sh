#!/usr/bin/env bash
# 扫描 dump 里的一个 Smartisan 应用，产出一份摘要，然后清理掉临时文件。
#
# 用法:
#   tools/apk_sweep.sh <dump 内的应用目录> [keep]
# 例:
#   tools/apk_sweep.sh system/system/priv-app/SecurityCenter
#   tools/apk_sweep.sh system/system/app/GallerySmartisan keep   # 保留 APK 与解出的目录（要读代码时用）
#
# 产出: /tmp/sweep/<名字>.summary.txt
#   含 APK 体积、资源规模、自有自定义 View 列表、引用自定义 view 的布局、
#   app 自己的 xml 素材（shape / layer-list）、以及布局名清单。
#
# 环境变量: SWEEP_REF / SWEEP_BASE / SWEEP_WORK
set -uo pipefail

DIR="${1:-}"
KEEP="${2:-}"
if [ -z "$DIR" ]; then echo "用法: apk_sweep.sh <dump 内的应用目录> [keep]" >&2; exit 2; fi

REF=${SWEEP_REF:-qssi-user-11-RKQ1.201217.002-1-dev-keys}
BASE=${SWEEP_BASE:-https://dumps.tadiphone.dev/dumps/smartisan/darwin/-/raw}
API=${SWEEP_API:-https://dumps.tadiphone.dev/api/v4/projects/2042/repository/tree}
WORK=${SWEEP_WORK:-/tmp/sweep}
NAME=$(basename "$DIR")
APKTOOL=${SWEEP_APKTOOL:-/Users/puffercat/apktool.jar}
DEXSCAN=${SWEEP_DEXSCAN:-/Users/puffercat/smartisanx/tools/dexscan.py}

mkdir -p "$WORK"
OUT="$WORK/$NAME.summary.txt"
DEC="$WORK/${NAME}_dec"
APK="$WORK/$NAME.apk"

# 第三方 / 系统自带的包，跟锤子自绘组件无关，摘要里滤掉
NOISE='androidx\.|^android\.|android\.support|kotlinx\.|^kotlin\.|^com\.google\.|^org\.apache\.|^com\.bumptech\.|^com\.squareup\.|^io\.reactivex\.|^com\.facebook\.|^org\.json\.|^com\.tencent\.|^com\.iflytek\.|^com\.sohu\.|^com\.sogou\.'

{
  echo "================================================================"
  echo "## $NAME    ($DIR)"
} > "$OUT"
log() { echo "$@" >> "$OUT"; }

# 1) 目录里找 apk（名字偶尔和目录名不一致）
apkname=$(curl -s --max-time 60 "$API?ref=$REF&path=$DIR&per_page=100" \
  | python3 -c 'import sys,json
try: d=json.load(sys.stdin)
except Exception: d=[]
print(next((x["name"] for x in d if x["name"].endswith(".apk")),""))' 2>/dev/null)
[ -z "$apkname" ] && apkname="$NAME.apk"

# 2) 取 APK
code=$(curl -sL --max-time 900 -w '%{http_code}' -o "$APK" "$BASE/$REF/$DIR/$apkname")
sz=$(stat -f%z "$APK" 2>/dev/null || echo 0)
log "apk=$apkname http=$code bytes=$sz"
if [ "$code" != "200" ] || [ "$sz" -lt 2000 ]; then
  log "结论: 下载失败或空包，跳过"
  rm -f "$APK"
  exit 0
fi

# 3) 解资源（-s 保留原始 dex，快且省地方）
java -jar "$APKTOOL" d -f -s "$APK" -o "$DEC" > "$WORK/$NAME.apktool.log" 2>&1
if [ ! -d "$DEC/res" ]; then
  log "结论: apktool 解包失败，见 $NAME.apktool.log"
  rm -rf "$APK" "$DEC"
  exit 0
fi

R="$DEC/res"
cnt() { ls "$1" 2>/dev/null | wc -l | tr -d ' '; }
log "res: layout=$(cnt $R/layout*) layout-scale=$(ls -d $R/layout-* 2>/dev/null | wc -l | tr -d ' ') drawable=$(cnt $R/drawable*) anim=$(cnt $R/anim*) animator=$(cnt $R/animator*) xml=$(cnt $R/xml*) raw=$(cnt $R/raw*)"
[ -d "$DEC/assets" ] && log "assets: $(ls "$DEC/assets" | head -20 | tr '\n' ' ')"
log "dex: $(ls "$DEC"/classes*.dex 2>/dev/null | wc -l | tr -d ' ') 个"

# 4) 扫 dex，列出自有自定义 View / Dialog / Adapter
for dx in "$DEC"/classes*.dex; do
  [ -f "$dx" ] || continue
  echo "---- $(basename "$dx") ----" >> "$OUT"
  python3 "$DEXSCAN" "$dx" 2>/dev/null \
    | grep -E '^  ' | grep -vE "$NOISE" | sed 's/^  //' >> "$OUT"
done

# 5) 布局里直接引用的自定义 view（含 <view class=...> 这种）
echo "---- 布局里引用的自定义 view ----" >> "$OUT"
grep -oh 'class="[^"]*"' "$R"/layout*/*.xml 2>/dev/null | sed 's/class="//;s/"$//' | sort -u \
  | grep -vE '^(android|androidx)' >> "$OUT"
grep -ohE '<(smartisanos|com\.smartisanos|com\.android\.contacts)[A-Za-z0-9_.$]*' "$R"/layout*/*.xml 2>/dev/null \
  | sed 's/^<//' | sort -u >> "$OUT"

# 6) 自有 xml 素材（shape / layer-list / selector：几何与配色都写在里面）
echo "---- 自有 xml 素材（前 80 个）----" >> "$OUT"
for f in "$R"/drawable*/*.xml; do
  [ -f "$f" ] || continue
  grep -lqE '<(shape|layer-list|selector|ripple|vector)' "$f" 2>/dev/null && basename "$f"
done | grep -vE '^(abc_|common_|$)' | head -80 >> "$OUT"

# 7) 布局名清单（看这套 UI 有哪些屏）
echo "---- 布局名 ----" >> "$OUT"
ls "$R"/layout 2>/dev/null | sed 's/\.xml$//' | tr '\n' ' ' >> "$OUT"
echo >> "$OUT"

# 8) 清理
if [ "$KEEP" = "keep" ]; then
  log "(保留 $APK 与 $DEC)"
else
  rm -rf "$APK" "$DEC" "$WORK/$NAME.apktool.log"
fi
echo "done $NAME" >> "$WORK/sweep.log"
