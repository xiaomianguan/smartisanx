#!/usr/bin/env python3
"""检查有没有「只在竖屏手机取不到」的素材。

例如只放在 `drawable-land-xxhdpi/` 的图片，竖屏时 `Resources.getDrawable(id)` 会抛

    Resources$NotFoundException: Resource ID #0x...

（原版计算器的 `sin` / `cos` / `log` 等按键字符就是横屏专属，只放在 `drawable-land-xxhdpi/`；
图标总表会把整个目录 load 一遍，于是直接崩）。修法是在竖屏可达的目录里（同密度，
一般就是 `drawable-xxhdpi/`）补一份同样内容的回落。

在仓库根目录运行：

    python3 tools/check_portrait_assets.py [额外的 res 目录...]
"""
import glob
import os
import re
import sys

# 这些限定符在普通竖屏手机上永远匹配不上。
NARROW_QUALIFIER = re.compile(
    r'^(land|night|car|desk|television|watch|ldrtl|ldltr|small|large|xlarge|'
    r'sw\d+dp|[wh]\d+dp|[a-z]{2}|b\+.*)$'
)
DEFAULT_ROOTS = ('library/*/src/main/res', 'sample/src/main/res')
SAMPLE_LIMIT = 15


def dir_matches_portrait_phone(dirname):
    """dirname 形如 `drawable` / `drawable-xxhdpi` / `drawable-land-xxhdpi`。"""
    qualifier = dirname[len('drawable'):].lstrip('-')
    if not qualifier:
        return True
    return not any(NARROW_QUALIFIER.match(tok) for tok in qualifier.split('-'))


def main():
    roots = sys.argv[1:] or DEFAULT_ROOTS
    resources = {}  # 资源名 -> {目录名: 路径}
    for root in roots:
        for path in glob.glob(f'{root}/drawable*/*'):
            name = os.path.splitext(os.path.basename(path))[0]
            name = re.sub(r'\.9$', '', name)
            resources.setdefault(name, {})[os.path.basename(os.path.dirname(path))] = path

    flagged = [
        (name, sorted(dirs))
        for name, dirs in sorted(resources.items())
        if not any(dir_matches_portrait_phone(d) for d in dirs)
    ]

    for name, dirs in flagged[:SAMPLE_LIMIT]:
        print(f'{name}: only in {", ".join(dirs)}')
    print(f'--- {len(flagged)}/{len(resources)} 个资源没有竖屏手机可达的变体 ---')
    return 1 if flagged else 0


if __name__ == '__main__':
    sys.exit(main())
