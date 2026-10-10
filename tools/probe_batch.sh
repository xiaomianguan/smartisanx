#!/usr/bin/env bash
# 按清单（每行「应用目录|类名...」）批量跑 app_probe.sh，限并发。
# 默认跑完删掉解出的目录（证据已在 .probe.txt 里）；要留着看素材就设 PROBE_KEEP=1。
set -uo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
LIST="${1:-/tmp/probe_list.txt}"
JOBS="${2:-2}"

n=0
while IFS='|' read -r dir classes; do
  case "$dir" in ''|\#*|' '*) continue;; esac
  echo "-> $dir  [$classes]"
  bash "$HERE/app_probe.sh" "$dir" $classes > "/tmp/probe/$(basename "$dir").run.log" 2>&1 &
  n=$((n + 1))
  if [ $((n % JOBS)) -eq 0 ]; then wait; fi
done < "$LIST"
wait

if [ "${PROBE_KEEP:-}" != "1" ]; then
  rm -rf /tmp/probe/*_full /tmp/probe/*.apk
fi
echo "PROBE ALL DONE"
