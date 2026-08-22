#!/usr/bin/env python3
"""Inspect an Android boot image, and check it against the device it is for.

Written after a Pixel 3a on stock Android 11 refused a LuneOS boot image with

    Booting  FAILED (remote: 'Error verifying the received boot.img:
                              Invalid Parameter')

which took several flash-and-reboot cycles to pin down. Everything that
actually mattered was visible in the files on the build host: the boot image
had a v0 header where Android 11 wants v2, and its device tree was missing the
symbol msm_poweroff, which every overlay in the device's dtbo references. Both
are checked here.

Usage:
    check-bootimg.py BOOT.IMG [--dtbo DTBO.IMG] [--reference STOCK-BOOT.IMG]

    --dtbo       the device's dtbo partition, or the dtbo.img from its factory
                 image. Every symbol the overlays reference must exist in the
                 boot image's device tree, or the bootloader cannot apply them
                 and refuses the image outright.
    --reference  a boot image known to work on the device, usually the stock
                 one. Header fields are compared against it.

Exits non-zero if a check fails.
"""
import argparse
import struct
import sys

FDT_MAGIC = b"\xd0\x0d\xfe\xed"
BOOT_MAGIC = b"ANDROID!"
DT_TABLE_MAGIC = 0xD7B7AB1E

FDT_BEGIN_NODE, FDT_END_NODE, FDT_PROP, FDT_NOP, FDT_END = 1, 2, 3, 4, 9

problems = []


def fail(msg):
    problems.append(msg)
    print(f"  FAIL  {msg}")


def ok(msg):
    print(f"  ok    {msg}")


def read_bootimg(path):
    d = open(path, "rb").read()
    if d[:8] != BOOT_MAGIC:
        sys.exit(f"{path}: not an Android boot image (magic {d[:8]!r})")
    f = struct.unpack_from("<8sIIIIIIIIII", d, 0)
    img = dict(kernel_size=f[1], kernel_addr=f[2], ramdisk_size=f[3],
               ramdisk_addr=f[4], second_size=f[5], second_addr=f[6],
               tags_addr=f[7], page_size=f[8], header_version=f[9],
               os_version=f[10], raw=d)
    img["cmdline"] = (struct.unpack_from("<512s", d, 64)[0].rstrip(b"\0")
                      + struct.unpack_from("<1024s", d, 608)[0].rstrip(b"\0")).decode(errors="replace")
    hv, ps = img["header_version"], img["page_size"]
    img["recovery_dtbo_size"] = struct.unpack_from("<I", d, 1632)[0] if hv >= 1 else 0
    img["header_size"] = struct.unpack_from("<I", d, 1644)[0] if hv >= 1 else 0
    img["dtb_size"] = struct.unpack_from("<I", d, 1648)[0] if hv >= 2 else 0
    img["dtb_addr"] = struct.unpack_from("<Q", d, 1652)[0] if hv >= 2 else 0

    def npages(n):
        return (n + ps - 1) // ps * ps

    off = ps
    img["kernel"] = d[off:off + img["kernel_size"]]; off += npages(img["kernel_size"])
    img["ramdisk"] = d[off:off + img["ramdisk_size"]]; off += npages(img["ramdisk_size"])
    off += npages(img["second_size"]) + npages(img["recovery_dtbo_size"])
    img["dtb"] = d[off:off + img["dtb_size"]] if img["dtb_size"] else b""
    return img


def split_fdts(blob):
    out, i = [], 0
    while i < len(blob) - 8 and blob[i:i + 4] == FDT_MAGIC:
        total, = struct.unpack_from(">I", blob, i + 4)
        if total == 0 or i + total > len(blob):
            break
        out.append(blob[i:i + total])
        i += total
        while i < len(blob) and blob[i] == 0:
            i += 1
    return out


def fdt_walk(fdt):
    """Yield (depth, path, propname, value). Nodes yield propname None."""
    (magic, total, off_s, off_str, off_mem,
     ver, lcv, cpu, size_str, size_s) = struct.unpack_from(">10I", fdt, 0)
    strings = fdt[off_str:off_str + size_str]
    i, depth, path = off_s, 0, []
    while i < off_s + size_s:
        tok, = struct.unpack_from(">I", fdt, i); i += 4
        if tok == FDT_BEGIN_NODE:
            end = fdt.index(b"\0", i)
            path.append(fdt[i:end].decode(errors="replace"))
            i = (end + 4) & ~3
            depth += 1
            yield depth, list(path), None, None
        elif tok == FDT_END_NODE:
            depth -= 1
            if path:
                path.pop()
            if depth == 0:
                break
        elif tok == FDT_PROP:
            plen, noff = struct.unpack_from(">II", fdt, i); i += 8
            val = fdt[i:i + plen]
            i = (i + plen + 3) & ~3
            name = strings[noff:strings.index(b"\0", noff)].decode(errors="replace")
            yield depth, list(path), name, val
        elif tok == FDT_END:
            break


def fdt_symbols(fdt):
    return {name for depth, path, name, _ in fdt_walk(fdt)
            if name and len(path) == 2 and path[1] == "__symbols__"}


def fdt_root_prop(fdt, want):
    for depth, path, name, val in fdt_walk(fdt):
        if name == want and len(path) == 1:
            return val
    return None


def dtbo_referenced_symbols(path):
    """Symbols the overlays in a dtbo image refer to.

    An overlay records what it targets in __fixups__, whose property names are
    the labels it expects the base tree to define.
    """
    d = open(path, "rb").read()
    magic, total, hsz, esz, ecount, eoff, pagesz, ver = struct.unpack_from(">8I", d, 0)
    if magic != DT_TABLE_MAGIC:
        sys.exit(f"{path}: not a dtbo table (magic {magic:#x})")
    syms = set()
    for i in range(ecount):
        dsize, doff, _id, _rev = struct.unpack_from(">4I", d, eoff + i * esz)
        fdt = d[doff:doff + dsize]
        if fdt[:4] != FDT_MAGIC:
            continue
        for depth, p, name, _ in fdt_walk(fdt):
            if name and len(p) == 2 and p[1] == "__fixups__":
                syms.add(name)
    return syms, ecount


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("bootimg")
    ap.add_argument("--dtbo")
    ap.add_argument("--reference")
    a = ap.parse_args()

    img = read_bootimg(a.bootimg)
    print(f"\n{a.bootimg}")
    print(f"  header_version {img['header_version']}   page_size {img['page_size']}"
          f"   os_version {img['os_version']:#x}")
    print(f"  kernel  {img['kernel_size']:>9} @ {img['kernel_addr']:#x}")
    print(f"  ramdisk {img['ramdisk_size']:>9} @ {img['ramdisk_addr']:#x}")
    print(f"  dtb     {img['dtb_size']:>9} @ {img['dtb_addr']:#x}")
    print(f"  cmdline {img['cmdline'][:100]}")

    print("\nstructure")
    if img["header_version"] >= 2 and not img["dtb_size"]:
        fail("header version 2 but the dtb section is empty; the bootloader has "
             "no device tree to apply overlays to")
    elif img["header_version"] >= 2:
        ok(f"dtb section present ({img['dtb_size']} bytes)")
    if img["header_version"] >= 1 and img["header_size"] not in (1648, 1660):
        fail(f"header_size {img['header_size']}, expected 1648 (v1) or 1660 (v2)")
    if img["kernel"][:2] == b"\x1f\x8b" and FDT_MAGIC in img["kernel"] and img["header_version"] >= 2:
        fail("the kernel still has a device tree appended to it AND the image "
             "has a v2 dtb section; the tree should be in one place only")
    else:
        ok("kernel and device tree are not double-counted")

    fdts = split_fdts(img["dtb"]) if img["dtb"] else []
    if img["dtb"]:
        print(f"\ndevice trees ({len(fdts)})")
        for n, f in enumerate(fdts, 1):
            model = (fdt_root_prop(f, "model") or b"").rstrip(b"\0").decode(errors="replace")
            msm = fdt_root_prop(f, "qcom,msm-id")
            msm = " ".join(f"{x:#x}" for x, in struct.iter_unpack(">I", msm)) if msm else "-"
            print(f"  fdt#{n}: {len(f):>8} bytes  msm-id={msm}  {model}")

    if a.reference:
        ref = read_bootimg(a.reference)
        print(f"\nagainst {a.reference}")
        for field in ("header_version", "page_size", "kernel_addr",
                      "ramdisk_addr", "tags_addr", "dtb_addr"):
            if img[field] == ref[field]:
                ok(f"{field} matches ({img[field]:#x})" if "addr" in field
                   else f"{field} matches ({img[field]})")
            else:
                fail(f"{field} is {img[field]:#x} but the reference has {ref[field]:#x}")

    if a.dtbo:
        want, ecount = dtbo_referenced_symbols(a.dtbo)
        have = set()
        for f in fdts:
            have |= fdt_symbols(f)
        print(f"\nagainst {a.dtbo} ({ecount} overlays, {len(want)} symbols referenced)")
        missing = sorted(want - have)
        if not fdts:
            fail("no device tree in the image, so no overlay can be applied")
        elif missing:
            fail(f"the device tree is missing {len(missing)} symbol(s) the overlays "
                 f"need, so the bootloader will refuse the image: {', '.join(missing[:10])}")
        else:
            ok(f"all {len(want)} referenced symbols are defined")

    print()
    if problems:
        print(f"{len(problems)} problem(s) found")
        return 1
    print("no problems found")
    return 0


if __name__ == "__main__":
    sys.exit(main())
