import struct, sys, re

def uleb(d, off):
    r=0; s=0
    while True:
        b=d[off]; off+=1
        r |= (b & 0x7f) << s
        if not (b & 0x80): break
        s += 7
    return r, off

def strings(d, off, size):
    out=[]
    for i in range(size):
        o=struct.unpack('<I', d[off+4*i:off+4*i+4])[0]
        n, p = uleb(d, o)
        out.append(d[p:p+n].decode('utf-8', 'replace'))
    return out

def types(d, off, size, strs):
    return [strs[struct.unpack('<I', d[off+4*i:off+4*i+4])[0]] for i in range(size)]

def main(path):
    d=open(path,'rb').read()
    s_size,s_off = struct.unpack("<II", d[56:64])
    t_size,t_off = struct.unpack("<II", d[64:72])
    c_size,c_off = struct.unpack("<II", d[96:104])
    strs=strings(d,s_off,s_size)
    tys=types(d,t_off,t_size,strs)
    out=[]
    for i in range(c_size):
        b=c_off+32*i
        cidx, acc, sidx = struct.unpack('<III', d[b:b+12])
        cls=tys[cidx]; sup=tys[sidx] if sidx!=0xffffffff and sidx<len(tys) else None
        out.append((cls, sup))
    return out

if __name__=='__main__':
    rows=main(sys.argv[1])
    pat=re.compile(sys.argv[2], re.I) if len(sys.argv)>2 else re.compile(r'View|Widget|Dialog|Layout|Button|Bar|Picker')
    bases=('Landroid/view/View;','Landroid/view/ViewGroup;','Landroid/widget/FrameLayout;','Landroid/widget/LinearLayout;','Landroid/widget/RelativeLayout;','Landroid/widget/ImageView;','Landroid/widget/TextView;','Landroid/widget/ListView;','Landroid/widget/ScrollView;','Landroid/widget/CheckBox;','Landroid/widget/Switch;','Landroid/widget/Button;','Landroid/widget/SeekBar;','Landroid/widget/ProgressBar;','Landroid/widget/EditText;','Landroid/widget/CompoundButton;','Landroid/app/Dialog;','Landroid/app/AlertDialog;','Landroid/widget/PopupWindow;','Landroid/widget/BaseAdapter;','Landroid/widget/ArrayAdapter;')
    print('dex 类总数:', len(rows))
    hits=[(c,s) for c,s in rows if s in bases]
    print('自定义 View/Dialog/Adapter 类:', len(hits))
    for c,s in sorted(set(hits)):
        print(f'  {c[1:-1].replace("/","."):62s} : {s[1:-1]}')
