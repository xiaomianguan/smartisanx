#!/usr/bin/env bash
# 批量扫 dump 里的应用：读一个「每行一个应用目录」的清单，限并发跑 apk_sweep.sh。
#
# 用法:
#   tools/sweep_all.sh <清单文件> [并发数] [keep]
# 例:
#   tools/sweep_all.sh /tmp/apps.txt 3
#
# 摘要写到 /tmp/sweep/<名字>.summary.txt，进度写到 /tmp/sweep/sweep.log。
set -uo pipefail

LIST="$1"
JOBS="${2:-3}"
KEEP="${3:-}"
HERE=$(cd "$(dirname "$0")" && pwd)
WORK=${SWEEP_WORK:-/tmp/sweep}
mkdir -p "$WORK"

grep -vE '^\s*(#|$)' "$LIST" | while read -r dir; do
  echo "$dir"
done | xargs -P "$JOBS" -I{} bash -c "'$HERE/apk_sweep.sh' '{}' '$KEEP'"
echo "ALL DONE" >> "$WORK/sweep.log"
