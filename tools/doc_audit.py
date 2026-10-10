#!/usr/bin/env python3
"""文档 / 代码一致性体检。

这个仓库的文档（README、docs/*.md）写了大量「实测数字」和「API 签名」，
代码一改它们就会过期 —— 之前就出现过删掉 `library/icons` 模块后文档还在教人
`include(":library:icons")`、资源数量停在 481 个之类的问题。

这个脚本把能自动核对的部分全部核一遍，改完代码/文档跑一次即可：

    python3 tools/doc_audit.py            # 打印全部结果
    python3 tools/doc_audit.py --check    # 有问题时退出码非 0（给 CI / 提交前用）

检查项：

1. API 覆盖：文档里写的 `Smartisan*` 名字是否真的存在
   （`SmartisanXStatusIcons` 这种已删除的就会报出来）。
2. 签名：文档 kotlin 代码块里的 `fun X(...)` 参数名与真实签名比对。
3. 主题取值：色板表（浅色 / 深色）、尺寸常量、文字样式、形状四张表逐项比对。
4. 文档配对：README ↔ README_en_US、组件总览 ↔ Components、主题与设计变量 ↔ Theme、
   快速开始 ↔ QuickStart、组件核对记录 ↔ ComponentVerification、迁移文档 ↔ Migration
   的名字集合必须一致。
5. 多语言：`values-*/smartisanx_strings.xml` 的 key 集合与默认语言比对。
6. 资源数量：`library/ui/src/main/res` 的文件总数、分类与夜间变体数量，
   用来核对 README「资源清单」里的数字。
"""

from __future__ import annotations

import argparse
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
OPEN, CLOSE = "(<{[", ")>}]"


def split_top(text: str) -> list[str]:
    """按顶层逗号切分（跳过 () <> {} 内部的逗号，把 `->` 当成一个整体）。"""
    out: list[str] = []
    depth, cur, i = 0, "", 0
    while i < len(text):
        ch = text[i]
        if ch == "-" and i + 1 < len(text) and text[i + 1] == ">":
            cur += "->"
            i += 2
            continue
        if ch in OPEN:
            depth += 1
        elif ch in CLOSE:
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
        else:
            cur += ch
        i += 1
    if cur.strip():
        out.append(cur)
    return out


def params_after(text: str, i: int) -> str:
    depth, start = 1, i
    while i < len(text) and depth:
        ch = text[i]
        if ch == "-" and i + 1 < len(text) and text[i + 1] == ">":
            i += 2
            continue
        if ch in OPEN:
            depth += 1
        elif ch in CLOSE:
            depth -= 1
        i += 1
    return text[start : i - 1]


def param_names(param_str: str) -> list[str]:
    # 先按行去掉 `//` 注释（注释里可能有逗号，会让下面的切分跑偏）
    param_str = "\n".join(line.split("//")[0] for line in param_str.splitlines())
    names = []
    for part in split_top(param_str):
        part = part.strip()
        if not part or "…" in part or "..." in part:  # 文档里用省略号简写的地方
            continue
        part = re.sub(r"^(?:@\w+(?:\([^)]*\))?\s+)*", "", part)
        part = re.sub(
            r"^(?:vararg|noinline|crossinline|private|internal|public|protected|override|lateinit)\s+",
            "",
            part,
        )
        m = re.match(r"([A-Za-z_][A-Za-z0-9_]*)", part)
        if m:
            names.append(m.group(1))
    return names


def collect_functions(text: str) -> dict[str, list[list[str]]]:
    out: dict[str, list[list[str]]] = {}
    for m in re.finditer(r"\bfun\s+([A-Za-z0-9_]+)\s*\(", text):
        out.setdefault(m.group(1), []).append(param_names(params_after(text, m.end())))
    return out


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def library_sources() -> list[pathlib.Path]:
    return sorted(ROOT.glob("library/*/src/main/kotlin/**/*.kt"))


DOCS = [
    "README.md",
    "README_en_US.md",
    "docs/组件总览.md",
    "docs/Components.md",
    "docs/快速开始.md",
    "docs/QuickStart.md",
    "docs/主题与设计变量.md",
    "docs/Theme.md",
    "docs/组件核对记录.md",
    "docs/ComponentVerification.md",
    "docs/从三个复刻项目迁移.md",
    "docs/Migration.md",
]

PAIRS = [
    ("README.md", "README_en_US.md"),
    ("docs/组件总览.md", "docs/Components.md"),
    ("docs/主题与设计变量.md", "docs/Theme.md"),
    ("docs/快速开始.md", "docs/QuickStart.md"),
    ("docs/组件核对记录.md", "docs/ComponentVerification.md"),
    ("docs/从三个复刻项目迁移.md", "docs/Migration.md"),
]

problems: list[str] = []

# 原版（反编译）里的类名，不是本库的 API：README / 核对记录里拿它们说明「合并了谁」。
ORIGINAL_NAMES = {
    "SmartisanAnimatedSheet",
    "SmartisanCheckboxHit",
    "SmartisanClick",
    "SmartisanEmptyHint",
    "SmartisanListDrag",
    "SmartisanModalDialog",
    "SmartisanPainterBackground",
    "SmartisanPressFeedback",
    "SmartisanScrollbar",
    "SmartisanShadowDrawable",
    "SmartisanSlideSelection",
    "SmartisanSwipeDeleteMotion",
    "SmartisanSwipeDeleteRow",
    "SmartisanSwitchExView",
    "SmartisanSwitchView",
    "SmartisanTimePickerView",
    "SmartisanTitleText",
}


def report(section: str, lines: list[str]) -> None:
    print("== %s" % section)
    if not lines:
        print("   ok")
    for line in lines:
        print("   " + line)
    problems.extend(lines)
    print()


def check_api_coverage() -> None:
    lib_text = "\n".join(p.read_text(encoding="utf-8") for p in library_sources())
    docs_text = "\n".join(read(d) for d in DOCS)
    migration = read("docs/从三个复刻项目迁移.md") + read("docs/Migration.md")
    lines = []
    for name in sorted(set(re.findall(r"`(Smartisan[A-Za-z0-9_]*)`", docs_text))):
        if name in lib_text or name in ORIGINAL_NAMES:
            continue
        # 迁移文档里的名字是原版反编译产物的类名 / 文件名，不是本库的 API
        if re.search(r"`%s(\.kt)?`" % re.escape(name), migration):
            continue
        lines.append("文档提到但代码里没有：%s" % name)
    report("文档里的 Smartisan* 名字是否都存在", lines)


def check_signatures() -> None:
    lib_text = "\n".join(p.read_text(encoding="utf-8") for p in library_sources())
    code = collect_functions(lib_text)
    lines = []
    for doc in DOCS:
        blocks = "\n".join(re.findall(r"```kotlin\n(.*?)```", read(doc), re.S))
        for name, sigs in sorted(collect_functions(blocks).items()):
            if name not in code:
                continue  # 文档里的用户代码（App() / onCreate() 之类）
            for sig in sigs:
                if any(set(sig) <= set(known) for known in code[name]):
                    continue
                all_known = set().union(*[set(k) for k in code[name]])
                extra = sorted(set(sig) - all_known)
                if extra:
                    lines.append(
                        "%s: %s(%s) 里的 %s 在代码里没有" % (doc, name, ", ".join(sig), ", ".join(extra))
                    )
    report("文档签名与真实签名", lines)


def check_theme_tokens() -> None:
    core = ROOT / "library/core/src/main/kotlin/cc/wuersan008/smartisanx/core/theme"
    colors_src = (core / "SmartisanColors.kt").read_text(encoding="utf-8")
    doc = read("docs/主题与设计变量.md")
    lines = []

    def color_map(fn: str) -> dict[str, str]:
        body = re.search(r"fun %s\(\): SmartisanColors =.*?\n    \)" % fn, colors_src, re.S).group(0)
        return {
            m.group(1): m.group(2)[2:].upper().zfill(8)
            for m in re.finditer(r"(\w+) = Color\((0x[0-9A-Fa-f]+)\)", body)
        }

    light, dark = color_map("lightSmartisanColors"), color_map("darkSmartisanColors")
    for m in re.finditer(r"^\| `(\w+)` \| `#([0-9A-Fa-f]{6,8})` \| `#([0-9A-Fa-f]{6,8})` \|", doc, re.M):
        name, l, d = m.group(1), m.group(2).upper(), m.group(3).upper()
        l = l if len(l) == 8 else "FF" + l
        d = d if len(d) == 8 else "FF" + d
        if name not in light:
            lines.append("色板表里的 %s 代码里没有" % name)
        elif (light[name], dark[name]) != (l, d):
            lines.append("色板 %s 文档 %s/%s，代码 %s/%s" % (name, l, d, light[name], dark[name]))

    dimens = dict(
        re.findall(r"val (\w+) = ([\d.]+)\.dp", (core / "SmartisanDimens.kt").read_text(encoding="utf-8"))
    )
    block = re.search(r"`SmartisanDimens` 中的关键值.*?```\n(.*?)```", doc, re.S).group(1)
    for name, value in re.findall(r"(\w+)\s+([\d.]+)", block):
        if name not in dimens:
            lines.append("尺寸表里的 %s 代码里没有" % name)
        elif dimens[name] != value:
            lines.append("尺寸 %s 文档 %s，代码 %s" % (name, value, dimens[name]))

    typo_src = (core / "SmartisanTypography.kt").read_text(encoding="utf-8")
    typo = {
        m.group(1): (m.group(2), m.group(3))
        for m in re.finditer(
            r"val (\w+): TextStyle = TextStyle\(\s*fontSize = ([\d.]+)\.sp,\s*fontWeight = FontWeight\.(\w+)",
            typo_src,
        )
    }
    documented = {
        m.group(1): (m.group(2), m.group(3))
        for m in re.finditer(r"^\| `(\w+)` \| ([\d.]+)sp \| (\w+) \|", doc, re.M)
    }
    for name, (size, weight) in typo.items():
        if name not in documented:
            lines.append("文字样式 %s 没写进文档表" % name)
        elif documented[name] != (size, weight):
            lines.append("文字样式 %s 文档 %s/%s，代码 %s/%s" % (name, *documented[name], size, weight))
    for name in documented:
        if name not in typo:
            lines.append("文字样式表里的 %s 代码里没有" % name)

    shapes = dict(
        re.findall(
            r"val (\w+): Shape = RoundedCornerShape\((\d+)\.dp",
            (core / "SmartisanShapes.kt").read_text(encoding="utf-8"),
        )
    )
    for name, value in re.findall(r"^\| `(\w+)` \| (\d+)dp", doc, re.M):
        if name in shapes and shapes[name] != value:
            lines.append("形状 %s 文档 %s，代码 %s" % (name, value, shapes[name]))
    report("主题取值表（色板 / 尺寸 / 文字样式 / 形状）", lines)



def check_doc_pairs() -> None:
    lines = []
    for zh, en in PAIRS:
        a = set(re.findall(r"`(Smartisan[A-Za-z0-9_]*)`", read(zh)))
        b = set(re.findall(r"`(Smartisan[A-Za-z0-9_]*)`", read(en)))
        # 代码块里的名字没有反引号，两边都算上
        a |= set(re.findall(r"\b(Smartisan[A-Za-z0-9_]+)\s*\(", read(zh)))
        b |= set(re.findall(r"\b(Smartisan[A-Za-z0-9_]+)\s*\(", read(en)))
        only_zh, only_en = sorted(a - b), sorted(b - a)
        if only_zh:
            lines.append("%s 里有、%s 里没有：%s" % (zh, en, ", ".join(only_zh)))
        if only_en:
            lines.append("%s 里有、%s 里没有：%s" % (en, zh, ", ".join(only_en)))
    report("中英文档配对", lines)


def check_locales() -> None:
    res = ROOT / "library/ui/src/main/res"
    pattern = r'<(?:string|plurals) name="([^"]+)"'
    default = set(re.findall(pattern, (res / "values/smartisanx_strings.xml").read_text(encoding="utf-8")))
    locales = incomplete = 0
    missing_keys: set[str] = set()
    for d in sorted(res.glob("values-*")):
        f = d / "smartisanx_strings.xml"
        if not f.exists():
            continue
        locales += 1
        gap = default - set(re.findall(pattern, f.read_text(encoding="utf-8")))
        if gap:
            incomplete += 1
            missing_keys |= gap
    print("== 多语言")
    print("   %d 个语言目录，%d 条默认字符串" % (locales, len(default)))
    if incomplete:
        print(
            "   %d 个语言缺 %s —— README「多语言」已写明这几条只有中（简繁）日韩，其余语言回落英文"
            % (incomplete, "、".join(sorted(missing_keys)))
        )
    print()


def check_resources() -> None:
    res = ROOT / "library/ui/src/main/res"
    buckets: dict[str, int] = {}
    drawables: set[str] = set()
    night: set[str] = set()
    for path in res.rglob("*"):
        if not path.is_file():
            continue
        top = path.relative_to(res).parts[0]
        if top.startswith("drawable"):
            key = "drawable*"
        elif top.startswith("mipmap"):
            key = "mipmap*"
        elif top.startswith("values"):
            key = "values*"
        else:
            key = top
        buckets[key] = buckets.get(key, 0) + 1
        if top.startswith("drawable"):
            drawables.add(path.stem)
            if "night" in top:
                night.add(path.stem)
    print("== 资源数量（README「资源清单」用的数字）")
    print(
        "   总计 %d 个文件：%s"
        % (sum(buckets.values()), ", ".join("%s %d" % kv for kv in sorted(buckets.items(), key=lambda kv: -kv[1])))
    )
    print(
        "   drawable 去重后 %d 个，其中 %d 个带夜间变体（%.1f%%）"
        % (len(drawables), len(night), 100 * len(night) / len(drawables))
    )
    print()


def main() -> int:
    parser = argparse.ArgumentParser(description="文档 / 代码一致性体检")
    parser.add_argument("--check", action="store_true", help="有问题时退出码非 0")
    args = parser.parse_args()

    check_api_coverage()
    check_signatures()
    check_theme_tokens()
    check_doc_pairs()
    check_locales()
    check_resources()

    if problems:
        print("发现 %d 处不一致（见上）。" % len(problems))
        return 1 if args.check else 0
    print("全部一致。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
