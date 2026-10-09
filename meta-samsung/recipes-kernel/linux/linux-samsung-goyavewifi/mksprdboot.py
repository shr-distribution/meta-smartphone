#!/usr/bin/env python3
"""Build and take apart Spreadtrum (sc8830) Samsung boot images.

The SM-T113 boot.img is a plain v0 Android header (2048 byte pages) whose
`unused` word at offset 40 is the size of a trailing device-tree table, the
way QCOM's dtbTool images use it. The table is Spreadtrum's own:

    "SPRD"  u32 version=1  u32 count
    count * { u32 chip_id  u32 board_rev  u32 variant  u32 offset  u32 size }
    u32 0                                  (terminator)
    <dtb blobs, each at its `offset`, 2048 aligned, slots 0xd800 apart>

Everything below was read off the stock T113XXS0AQC2 boot.img and is checked
by `selftest`, which rebuilds that image byte for byte from its own parts.

    mksprdboot.py unpack  boot.img outdir
    mksprdboot.py build   --kernel zImage --ramdisk rd.gz --dtb 2=a.dtb --dtb 3=b.dtb
                          [--cmdline ...] -o boot.img
    mksprdboot.py selftest stock-boot.img
"""
import argparse, hashlib, os, struct, sys

PAGE = 2048
CHIP_ID = 0x227E          # sc8830
VARIANT = 0x20000
DT_SLOT = 0xD800          # stock gives every dtb a 55296 byte slot
DT_FIRST = 0x800
DEFAULTS = dict(kernel_addr=0x8000, ramdisk_addr=0x1000000, second_addr=0xF00000,
                tags_addr=0x100, os_version=0, cmdline=b"console=ttyS1,115200n8")


def pad(b, n=PAGE):
    return b + b"\0" * (-len(b) % n)


def build_dt(dtbs):
    """dtbs: {board_rev: bytes}. Returns the SPRD table."""
    revs = sorted(dtbs)
    head = struct.pack("<4sII", b"SPRD", 1, len(revs))
    off = DT_FIRST
    ents, blobs = b"", b""
    for r in revs:
        if len(dtbs[r]) > DT_SLOT:
            sys.exit("dtb for rev %d is %d bytes, slot is %d" % (r, len(dtbs[r]), DT_SLOT))
        ents += struct.pack("<5I", CHIP_ID, r, VARIANT, off, DT_SLOT)
        blobs += pad(dtbs[r], DT_SLOT)
        off += DT_SLOT
    table = head + ents + struct.pack("<I", 0)
    assert len(table) <= DT_FIRST
    return table + b"\0" * (DT_FIRST - len(table)) + blobs


def split_dt(table):
    magic, ver, n = struct.unpack("<4sII", table[:12])
    assert magic == b"SPRD" and ver == 1, "not a SPRD dt table"
    out = {}
    for i in range(n):
        chip, rev, var, off, size = struct.unpack("<5I", table[12 + i * 20:32 + i * 20])
        blob = table[off:off + size]
        # the blob is padded to its slot; trim to the FDT's own totalsize
        total = struct.unpack(">I", blob[4:8])[0]
        out[rev] = blob[:total]
    return out


def build_boot(kernel, ramdisk, dt, cmdline=DEFAULTS["cmdline"], **kw):
    a = dict(DEFAULTS, **kw)
    sha = hashlib.sha1()
    for part in (kernel, ramdisk, b"", dt):
        sha.update(part)
        sha.update(struct.pack("<I", len(part)))
    h = struct.pack("<8s10I", b"ANDROID!", len(kernel), a["kernel_addr"], len(ramdisk),
                    a["ramdisk_addr"], 0, a["second_addr"], a["tags_addr"], PAGE,
                    len(dt), a["os_version"])
    h += b"\0" * 16 + cmdline.ljust(512, b"\0") + sha.digest().ljust(32, b"\0")
    return pad(h) + pad(kernel) + pad(ramdisk) + pad(dt)


def unpack_boot(img):
    (magic, ks, ka, rs, ra, ss, sa, ta, ps, dts, osv) = struct.unpack("<8s10I", img[:48])
    assert magic == b"ANDROID!" and ps == PAGE and ss == 0
    cmd = img[64:576].split(b"\0")[0]
    o = PAGE
    k = img[o:o + ks]; o += len(pad(k))
    r = img[o:o + rs]; o += len(pad(r))
    dt = img[o:o + dts]
    return dict(kernel=k, ramdisk=r, dt=dt, cmdline=cmd,
                kernel_addr=ka, ramdisk_addr=ra, second_addr=sa, tags_addr=ta, os_version=osv)


def selftest(path):
    img = open(path, "rb").read()
    u = unpack_boot(img)
    dtbs = split_dt(u["dt"])
    rebuilt_dt = build_dt(dtbs)
    ok_dt = rebuilt_dt == u["dt"]
    rebuilt = build_boot(u["kernel"], u["ramdisk"], rebuilt_dt, u["cmdline"],
                         **{k: u[k] for k in ("kernel_addr", "ramdisk_addr", "second_addr",
                                              "tags_addr", "os_version")})
    ok_img = rebuilt == img[:len(rebuilt)]
    print("dt table  : %s (%d dtbs, revs %s)" % ("identical" if ok_dt else "DIFFERS", len(dtbs), sorted(dtbs)))
    print("boot.img  : %s (%d bytes rebuilt, stock %d, %d trailing stock bytes not reproduced)"
          % ("identical" if ok_img else "DIFFERS", len(rebuilt), len(img), len(img) - len(rebuilt)))
    return ok_dt and ok_img


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest="cmd", required=True)
    u = sub.add_parser("unpack"); u.add_argument("image"); u.add_argument("outdir")
    s = sub.add_parser("selftest"); s.add_argument("image")
    b = sub.add_parser("build")
    b.add_argument("--kernel", required=True); b.add_argument("--ramdisk", required=True)
    b.add_argument("--dtb", action="append", required=True, metavar="REV=FILE")
    b.add_argument("--cmdline", default=DEFAULTS["cmdline"].decode())
    b.add_argument("-o", "--out", required=True)
    a = ap.parse_args()

    if a.cmd == "selftest":
        sys.exit(0 if selftest(a.image) else 1)
    if a.cmd == "unpack":
        x = unpack_boot(open(a.image, "rb").read())
        os.makedirs(a.outdir, exist_ok=True)
        for n in ("kernel", "ramdisk", "dt"):
            open(os.path.join(a.outdir, n), "wb").write(x[n])
        for rev, blob in split_dt(x["dt"]).items():
            open(os.path.join(a.outdir, "rev%02d.dtb" % rev), "wb").write(blob)
        print("cmdline:", x["cmdline"].decode(), {k: hex(v) for k, v in x.items() if k.endswith("addr")})
        return
    dtbs = {}
    for spec in a.dtb:
        rev, f = spec.split("=", 1)
        dtbs[int(rev)] = open(f, "rb").read()
    img = build_boot(open(a.kernel, "rb").read(), open(a.ramdisk, "rb").read(),
                     build_dt(dtbs), a.cmdline.encode())
    open(a.out, "wb").write(img)
    print("wrote %s (%d bytes)" % (a.out, len(img)))


if __name__ == "__main__":
    main()
