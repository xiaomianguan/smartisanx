#!/usr/bin/env python3
"""检查 res/drawable*/*.xml 里有没有「既没有 drawable 也没有 color」的 <item>。

Android 运行时遇到这种 item 会抛

    XmlPullParserException: <item> tag requires a 'drawable' attribute or child
    tag defining a drawable

整个 drawable 直接加载失败（`Resources.getDrawable` 抛 NotFoundException）。
原版 APK 里个别 selector 就是这样写的（例如 `btn_radio.xml` 的最后两条），
从固件抄素材时容易一起抄进来。

在仓库根目录运行：

    python3 tools/check_selector_items.py [额外的 res 目录...]
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET

ANDROID = '{http://schemas.android.com/apk/res/android}'
DEFAULT_ROOTS = ('library/*/src/main/res', 'sample/src/main/res')
SAMPLE_LIMIT = 15


def invalid_items(path):
    """返回该 xml 里所有缺 drawable / color 的 <item>，元素是 (根标签, 属性串)。"""
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError as exc:
        return [('PARSE', str(exc))]
    bad = []
    for item in root.iter('item'):
        if item.get(ANDROID + 'drawable') or item.get(ANDROID + 'color'):
            continue
        if len(item):
            continue  # 有子标签定义 drawable，合法
        attrs = ' '.join(
            f'{k.replace(ANDROID, "android:")}={v!r}' for k, v in item.attrib.items()
        )
        bad.append((root.tag, attrs))
    return bad


def main():
    roots = sys.argv[1:] or DEFAULT_ROOTS
    files = []
    for root in roots:
        files += glob.glob(f'{root}/drawable*/*.xml')

    hit_files = 0
    hit_items = 0
    for path in sorted(files):
        bad = invalid_items(path)
        if not bad:
            continue
        hit_files += 1
        hit_items += len(bad)
        if hit_files <= SAMPLE_LIMIT:
            print(os.path.relpath(path))
            for tag, attrs in bad[:3]:
                print(f'    <{tag}> item  {attrs}')
    print(f'--- {hit_files}/{len(files)} 个文件、{hit_items} 条非法 <item> ---')
    return 1 if hit_files else 0


if __name__ == '__main__':
    sys.exit(main())
