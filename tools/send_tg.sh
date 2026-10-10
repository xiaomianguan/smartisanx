#!/usr/bin/env bash
# 把文件／文字发到 Telegram（bot: @PufferfiatBot）。
#
# 凭据不在仓库里，放在 `~/tg.env`（`TG_BOT_TOKEN` + `TG_CHAT_ID`，600 权限）：
# 重启、重开会话都不会丢，所以每次要发东西时不用再问一遍 token。
#
# 用法:
#   tools/send_tg.sh <文件>              # 只发文件
#   tools/send_tg.sh <文件> "说明文字"    # 带说明文字
#   tools/send_tg.sh <文件> <说明文件>    # 说明很长时先写进文件（读它的内容当 caption）
#   tools/send_tg.sh -m "只发一条文字"
set -euo pipefail

ENV_FILE="${TG_ENV:-$HOME/tg.env}"
if [ ! -f "$ENV_FILE" ]; then
  echo "缺少凭据文件 $ENV_FILE（里面要有 TG_BOT_TOKEN 与 TG_CHAT_ID）" >&2
  exit 1
fi
set -a
. "$ENV_FILE"
set +a
: "${TG_BOT_TOKEN:?$ENV_FILE 里没有 TG_BOT_TOKEN}"
: "${TG_CHAT_ID:?$ENV_FILE 里没有 TG_CHAT_ID}"

API="https://api.telegram.org/bot$TG_BOT_TOKEN"

if [ "${1:-}" = "-m" ]; then
  curl -s --max-time 60 -F chat_id="$TG_CHAT_ID" -F text="${2:?用法: send_tg.sh -m \"文字\"}" "$API/sendMessage"
  printf '\n'
  exit 0
fi

FILE="${1:?用法: send_tg.sh <文件> [说明文字或说明文件]}"
if [ ! -f "$FILE" ]; then
  echo "找不到文件 $FILE" >&2
  exit 1
fi
CAP="${2:-}"
case "$CAP" in
  "") curl -s --max-time 300 -F chat_id="$TG_CHAT_ID" -F document=@"$FILE" "$API/sendDocument" ;;
  *) if [ -f "$CAP" ]; then
       curl -s --max-time 300 -F chat_id="$TG_CHAT_ID" -F document=@"$FILE" -F caption="<$CAP" "$API/sendDocument"
     else
       curl -s --max-time 300 -F chat_id="$TG_CHAT_ID" -F document=@"$FILE" -F caption="$CAP" "$API/sendDocument"
     fi ;;
esac
printf '\n'
