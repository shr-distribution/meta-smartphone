#!/usr/bin/env python3
"""Build synthetic Android LP (logical partition) metadata images.

Enough of the on-disk format to exercise a reader: geometry + header + the four
tables, checksums included. Lets the normal single-'super' layout and the
retrofit multi-block-device layout both be produced without an AOSP build.
"""
import hashlib, struct, sys

GEOMETRY_MAGIC = 0x616C4467
HEADER_MAGIC = 0x414C5030
RESERVED = 4096
GEOMETRY_SIZE = 4096
SECTOR = 512

def name36(s):
    b = s.encode()
    assert len(b) <= 36, s
    return b + b"\0" * (36 - len(b))

def geometry(metadata_max_size, slot_count, logical_block_size):
    body = lambda cs: struct.pack("<II32sIII", GEOMETRY_MAGIC, 52, cs,
                                  metadata_max_size, slot_count, logical_block_size)
    return body(hashlib.sha256(body(b"\0" * 32)).digest())

def header(tables, descs):
    tsum = hashlib.sha256(tables).digest()
    def body(hsum):
        h = struct.pack("<IHHI32sI32s", HEADER_MAGIC, 10, 2, 256, hsum, len(tables), tsum)
        for off, n, esz in descs:
            h += struct.pack("<III", off, n, esz)
        return h + struct.pack("<I", 0) + b"\0" * 124
    return body(hashlib.sha256(body(b"\0" * 32)).digest())

def build(path, block_devices, partitions, groups=(("default", 0, 0),),
          metadata_max_size=65536, logical_block_size=4096):
    """block_devices: [(name, size_bytes, first_logical_sector)]
       partitions:    [(name, group_index, attributes, [(num_sectors, source, target_data)])]"""
    part_tbl, ext_tbl, grp_tbl, dev_tbl = b"", b"", b"", b""
    ext_index = 0
    for pname, gidx, attrs, extents in partitions:
        part_tbl += struct.pack("<36sIIII", name36(pname), attrs, ext_index, len(extents), gidx)
        for extent in extents:
            num_sectors, source, target_data = extent[:3]
            target_type = extent[3] if len(extent) > 3 else 0
            ext_tbl += struct.pack("<QIQI", num_sectors, target_type, target_data, source)
            ext_index += 1
    for gname, flags, maxsize in groups:
        grp_tbl += struct.pack("<36sIQ", name36(gname), flags, maxsize)
    for dname, size, first_sector in block_devices:
        dev_tbl += struct.pack("<QIIQ36sI", first_sector, 1048576, 0, size, name36(dname), 0)

    o1 = 0
    o2 = o1 + len(part_tbl)
    o3 = o2 + len(ext_tbl)
    o4 = o3 + len(grp_tbl)
    tables = part_tbl + ext_tbl + grp_tbl + dev_tbl
    descs = [(o1, len(partitions), 52),
             (o2, ext_index, 24),
             (o3, len(groups), 48),
             (o4, len(block_devices), 64)]

    img = bytearray(b"\0" * RESERVED)
    g = geometry(metadata_max_size, 2, logical_block_size)
    img += g + b"\0" * (GEOMETRY_SIZE - len(g))          # primary geometry
    img += g + b"\0" * (GEOMETRY_SIZE - len(g))          # backup geometry
    meta = header(tables, descs) + tables
    img += meta + b"\0" * (metadata_max_size - len(meta))  # slot 0
    open(path, "wb").write(bytes(img))
    return len(img)

if __name__ == "__main__":
    which = sys.argv[1]
    out = sys.argv[2]
    MB = 1024 * 1024
    if which == "normal":
        # One dedicated 'super', every extent on block device 0.
        n = build(out,
                  block_devices=[("super", 4 * 1024 * MB, 2048)],
                  partitions=[
                      ("system_a",  0, 1, [(1024 * 1024, 0, 2048)]),
                      ("vendor_a",  0, 1, [(512 * 1024, 0, 1026048)]),
                      ("product_a", 0, 1, [(256 * 1024, 0, 1550336)]),
                  ])
    elif which == "retrofit":
        # Pixel-3a shape: metadata on 'system', extents across system AND vendor.
        n = build(out,
                  block_devices=[("system", 3267362816, 2048),
                                 ("vendor", 805306368, 2048)],
                  partitions=[
                      ("system_a", 0, 1, [(1024 * 1024, 0, 2048)]),
                      ("vendor_a", 0, 1, [(512 * 1024, 1, 2048)]),
                  ])
    elif which == "badtype":
        n = build(out,
                  block_devices=[("super", 4 * 1024 * MB, 2048)],
                  partitions=[("system_a", 0, 1, [(1024 * 1024, 0, 2048, 99)])])
    else:
        sys.exit("usage: mklp.py normal|retrofit|badtype OUT")
    print(f"wrote {out} ({n} bytes)")
