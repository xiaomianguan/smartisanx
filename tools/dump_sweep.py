#!/usr/bin/env python3
"""原版资源体检：把本库写死的尺寸 / 颜色拉回原版 APK 解码目录对一遍。

原版素材目录（可用 SMARTISANX_DUMPS 覆盖，格式 "标签=路径,标签=路径"）：
  ~/fw/dec                                framework-smartisanos-res.apk
  ~/sos_apps/{fm,settings,contacts,kg}_dec
  ~/sos_apps/{MusicPlayer,ClockSmartisan,NotesSmartisan,CalculatorSmartisan,FilePreviewSmartisan}_dec

每个常量的注释里通常写着原版资源名（`list_item_min_height` 这种），
脚本先查 dimens.xml / colors.xml；写的是布局文件（`abs_editor_layout`）时，
就到那个布局里找写死的 dp / px 值。

用法：python3 tools/dump_sweep.py [--check]
"""
import argparse
import collections
import os
import pathlib
import re
import sys

HOME = pathlib.Path.home()
DEFAULT_DUMPS = [
    ("framework", HOME / "fw/dec"),
    ("fm", HOME / "sos_apps/fm_dec"),
    ("settings", HOME / "sos_apps/settings_dec"),
    ("contacts", HOME / "sos_apps/contacts_dec"),
    ("kg", HOME / "sos_apps/kg_dec"),
    ("music", HOME / "sos_apps/MusicPlayer_dec"),
    ("clock", HOME / "sos_apps/ClockSmartisan_dec"),
    ("notes", HOME / "sos_apps/NotesSmartisan_dec"),
    ("calc", HOME / "sos_apps/CalculatorSmartisan_dec"),
    ("fpreview", HOME / "sos_apps/FilePreviewSmartisan_dec"),
]
# 原版机器的 px → dp 换算基准：面板 560dpi（`2.0px` 这类写死的值按它折）
PX_PER_DP = 560 / 160.0
DIMENS_KT = "library/core/src/main/kotlin/cc/wuersan008/smartisanx/core/theme/SmartisanDimens.kt"


def dumps():
    env = os.environ.get("SMARTISANX_DUMPS")
    if env:
        out = []
        for item in env.split(","):
            tag, _, path = item.partition("=")
            out.append((tag.strip(), pathlib.Path(path.strip()).expanduser()))
        return out
    return DEFAULT_DUMPS


def to_dp(raw):
    m = re.match(r"^(-?[\d.]+)(dip|dp|px|sp|pt|in|mm)$", raw.strip())
    if not m:
        return None
    v, unit = float(m.group(1)), m.group(2)
    if unit in ("dp", "dip", "sp"):
        return v
    if unit == "px":
        return v / PX_PER_DP
    if unit == "pt":
        return v * 160 / 72
    if unit == "in":
        return v * 160
    return v * 160 / 25.4


def build_tables():
    dimen = collections.defaultdict(list)
    color = collections.defaultdict(list)
    xmls = collections.defaultdict(list)
    for tag, root in dumps():
        if not root.exists():
            continue
        for f in sorted(root.glob("res/values*/dimens.xml")):
            text = f.read_text(encoding="utf-8", errors="replace")
            for m in re.finditer(r'<dimen name="([^"]+)">([^<]+)</dimen>', text):
                v = to_dp(m.group(2))
                where = "%s/%s" % (tag, f.parent.name)
                if v is not None and (v, where) not in dimen[m.group(1)]:
                    dimen[m.group(1)].append((v, where))
        for f in sorted(root.glob("res/values*/colors.xml")):
            text = f.read_text(encoding="utf-8", errors="replace")
            for m in re.finditer(r'<color name="([^"]+)">([^<]+)</color>', text):
                if (m.group(2), tag) not in color[m.group(1)]:
                    color[m.group(1)].append((m.group(2), tag))
        # 注释里点名布局 / drawable 时，去那个 xml 里找写死的值
        for f in sorted(root.glob("res/layout*/*.xml")) + sorted(root.glob("res/drawable*/*.xml")):
            xmls[f.stem].append(f)
    return dimen, color, xmls


def kdoc_entries(src):
    """(名字, 数值, 单位, 注释)。注释体里不可能有 `*/`，用它卡住边界。"""
    return [
        (m.group(2), float(m.group(3)), m.group(4), m.group(1))
        for m in re.finditer(r"(/\*\*(?:(?!\*/).)*\*/)\s*val (\w+) = ([\d.]+)\.(dp|sp)", src, re.S)
    ]


def candidates(doc):
    names = re.findall(r"`([A-Za-z][A-Za-z0-9_]*[A-Za-z0-9])`", doc)
    names += [n[:-4] for n in re.findall(r"`([a-z0-9_]+\.xml)`", doc)]  # 注释里直接点名布局文件
    names += re.findall(r"@dimen/([a-z0-9_]+)", doc)
    skip = {"dp", "sp", "px", "dpi", "pt", "in", "mm"}
    out = []
    for n in names:
        if n not in skip and n not in out:
            out.append(n)
    return out



def in_xml(name, value, unit, xmls):
    """在布局 / drawable 的 xml 里找写死的值（dp 或 px，px 按 560dpi 折）。"""
    wants = {"%.1f%s" % (value, unit)}
    if unit == "dp":
        wants.add("%gdp" % value)
        px = round(value * PX_PER_DP)
        if abs(px - value * PX_PER_DP) < 0.2:
            wants.add("%.1fpx" % px)
    for path in xmls.get(name, []):
        text = path.read_text(encoding="utf-8", errors="replace")
        for w in wants:
            if '="%s"' % w in text:
                return "%s 里写死的 %s" % (path.name, w)
    return None


def main():
    ap = argparse.ArgumentParser(description="原版资源体检")
    ap.add_argument("--check", action="store_true", help="对不上时退出码非 0")
    args = ap.parse_args()

    dimen, color, xmls = build_tables()
    missing = [t for t, p in dumps() if not p.exists()]
    print("原版解码目录：%d 个可用%s" % (
        len(dumps()) - len(missing), "（缺 %s）" % ", ".join(missing) if missing else ""))
    print("   dimens 条目 %d，colors 条目 %d，布局+drawable %d 个\n" % (len(dimen), len(color), len(xmls)))

    root = pathlib.Path(__file__).resolve().parent.parent
    src = (root / DIMENS_KT).read_text(encoding="utf-8")
    ok = bad = unverified = uncited = measured = 0
    rows = []
    for name, value, unit, doc in kdoc_entries(src):
        cands = candidates(doc)
        hit = next((c for c in cands if c in dimen), None)
        if hit:
            vals = dimen[hit]
            same = any(abs(v - value) < 0.011 for v, _ in vals)
            ok, bad = (ok + 1, bad) if same else (ok, bad + 1)
            rows.append(("BAD" if not same else "ok", name, "%g%s" % (value, unit),
                         "、".join("%g" % v for v, _ in vals[:3]), "%s ← %s" % (hit, vals[0][1])))
            continue
        for c in cands:
            note = in_xml(c, value, unit, xmls)
            if note:
                ok += 1
                rows.append(("ok", name, "%g%s" % (value, unit), "布局写死", "%s %s" % (c, note)))
                break
        else:
            if not cands:
                uncited += 1
                rows.append(("?", name, "%g%s" % (value, unit), "-", "注释没写原版资源名"))
            elif "实测" in doc:
                measured += 1
                rows.append(("~", name, "%g%s" % (value, unit), "实测",
                             "注释写的是实测值（%s），工具不校验" % ", ".join(cands[:2])))
            else:
                unverified += 1
                rows.append(("?", name, "%g%s" % (value, unit), "找不到", "注释写的是 " + ", ".join(cands[:3])))

    print("== SmartisanDimens ↔ 原版")
    print("   %-34s %-9s %-12s %s" % ("本库常量", "本库值", "原版值", "来源 / 备注"))
    for mark, name, val, orig, note in rows:
        print("   %-34s %-9s %-12s %s%s" % (name, val, orig, "⚠ " if mark == "BAD" else "", note))
    print("\n   对得上 %d，对不上 %d，原版里找不到 %d，实测值 %d，注释没写 %d\n" % (ok, bad, unverified, measured, uncited))

    lib = "\n".join(
        p.read_text(encoding="utf-8", errors="replace")
        for p in (root / "library").rglob("*.kt")
        if "/build/" not in str(p)
    )
    wanted = sorted(set(re.findall(
        r"`((?:smartisan|revone|settings|list|clock|title|bar|menu|modal|editor|switch|about|left|"
        r"right|mid|hidden|flexible|standard|check|message|item)_[a-z0-9_]+)`", lib)))
    found = 0
    print("== 注释里点名的原版资源（%d 个）" % len(wanted))
    for w in wanted:
        bits = []
        if w in dimen:
            bits.append("dimens " + "、".join("%g" % v for v, _ in dimen[w][:4]))
        if w in color:
            bits.append("colors " + "、".join(c for c, _ in color[w][:4]))
        if bits:
            found += 1
            print("   %-42s %s" % (w, " | ".join(bits)))
    print("\n   查到 %d / %d；其余在原版解码目录里没有同名条目" % (found, len(wanted)))
    return 1 if (bad and args.check) else 0


if __name__ == "__main__":
    sys.exit(main())
