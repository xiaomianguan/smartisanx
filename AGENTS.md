# 给编码助手的工作约定

这份文件是给 AI 编码助手看的：**每次接手这个仓库先读它**。仓库自己的文档是中文的，
但下面第一条「跟用户说话」有单独的语言要求。

## 语言

- **提交信息用中文**，与仓库历史保持一致：一行概括 + 详细正文，写清楚「照抄了原版的什么、
  真机实测数据是多少、示例与文档同步了什么」。
- **跟用户说话用英文**：聊天回复、Telegram 消息、给用户的说明一律英文。
  （仓库内的文档仍然是中文，这条只管「对用户说的话」。）

## 每完成一件事的固定动作

1. 跑构建与 `./gradlew lintDebug`（仓库里没有单元测试，lint 就是门槛）。
2. `git commit`（中文信息）之后**要 `git push origin main`** —— 不要只提交不推送。
3. **代码有变化就打 release 包并发给用户**（这是用户要包的固定方式）：
   ```bash
   ./tools/release_apk.sh        # 与 debug 同签名，可直接覆盖安装手机上已装的包
   ./tools/send_tg.sh sample/build/outputs/apk/release/smartisanx-sample-release.apk <说明文件>
   ```
   说明文件（caption）用**英文**写，且**别超过 1024 字符** —— 超了 Telegram 会直接回
   `Bad Request: message caption is too long`，包根本不会发出去（发送前可以先数一下：
   `python3 -c "print(len(open('说明文件').read()))"`）。Telegram 凭据在 `~/tg.env`
   （`TG_BOT_TOKEN` / `TG_CHAT_ID`），**不进仓库**；要换文件用 `TG_ENV=<路径>`。
4. 只改了文档 / 脚本这类不进包的东西时，推送即可，不用发包。

## 验证标准

- 组件类改动要**真机逐像素核对**，并把实测值补进 `docs/ComponentVerification.md` 与
  `docs/组件核对记录.md`。测试机是 nubia P0110（1264×2800 @ 560dpi，即 1dp = 3.5px）：
  截图 `adb shell screencap -p`，量像素用 ImageMagick（`magick ... txt:-` 逐列扫色带、
  看颜色分段与边界）。
- ROM 的 AOD 很激进，息屏后会回到 `Dozing` + `NotificationShade`：每次截图前先
  `input keyevent 224` → 上滑 → 输 PIN，并确认 `dumpsys window | grep mCurrentFocus`
  是目标 Activity 再截；`uiautomator dump` 拿控件 bounds 比猜坐标可靠。
- 原版素材一律**照抄 APK**（`~/sos_apps/*_dec` 是 apktool 解出来的原版资源，
  `~/sos_apps/*_jadx` 是 jadx 出来的源码），能对一下 md5 逐字节一致最好。

## 其它

- 动过的设备设置（`settings put` / `svc power`）要还原；临时文件（`/sdcard/*.png`、
  探针 xml）顺手删掉。
- 文档是成对的，改一处要同步另一处：`README.md` ↔ `README_en_US.md`、
  `docs/组件总览.md` ↔ `docs/Components.md`、
  `docs/组件核对记录.md` ↔ `docs/ComponentVerification.md`。
