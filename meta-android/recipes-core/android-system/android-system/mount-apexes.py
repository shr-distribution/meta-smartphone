#!/usr/bin/python3

# Copyright (C) 2026 UBports Foundation.
# SPDX-License-Identifier: GPL-3.0-or-later

# This script mounts either flattened or non-flattened (archived) APEXes based
# on its name as defined in `apex_manifest.pb` which is a Protobuf-encoded
# manifest.
#
# We need to read `apex_manifest.pb` rather than using the name from the file/
# directory name because they can mismatch. For example, an APEX with directory
# name `com.android.vndk.current` actually has the name of `com.android.vndk.
# v__` where `_` represent digits. Mounting this APEX incorrectly will lead to
# `linkerconfig` crashes with `SIGABRT` later on.

import fnmatch
import os
import subprocess
import sys
import zipfile

# The host's own /apex, not /android/apex.
#
# /android is the container's rootfs, and LXC re-establishes that when it
# starts: anything mounted underneath it beforehand is torn out again about a
# second and a half later, which left the APEXes as empty directories. Droidian
# puts them on a top-level /apex and binds that into the container instead
# (lxc.mount.entry = /apex apex bind rbind,optional), so the mounts live
# outside the rootfs LXC manages.
#
# It is also where the host needs them. libhybris's Android 10+ linker looks
# for bionic at a literal /apex/com.android.runtime/lib64, so with this layout
# host and container are served by the same mounts and no symlink is involved.
APEX_ROOT = "/apex"

# What was mounted, for the cache mount-android.sh mounts from on the next boot
# (see write_cache).
MOUNTED_RECORDS = []
APEX_PREINSTALLED_DIRS = [
    "/android/system/apex",
    "/android/system_ext/apex",
]


def parse_apex_manifest_for_name(manifest_bytes: bytes):
    # This is a very rudimentary parser for `apex_manifest.pb`, just enough to
    # extract APEX name and nothing else, and only if the name field is the first
    # value in the manifest.

    if manifest_bytes[0] != 0b00001_010:
        raise RuntimeError("Wrong first tag/unhandled length varint")

    length = manifest_bytes[1]
    if length >= 0b1000_0000:
        raise RuntimeError("Unhandled length varint")

    name = manifest_bytes[2 : length + 2].decode("utf-8")

    return name


class Apex:
    module_path: str
    name: str

    def __init__(self, module_path: str):
        self.module_path = module_path

    def mount(self) -> None:
        raise NotImplementedError()

    def get_mountpoint(self) -> str:
        return f"{APEX_ROOT}/{self.name}"

    def is_mounted(self) -> bool:
        return os.path.ismount(self.get_mountpoint())

    def record(self) -> None:
        raise NotImplementedError()


class FlattenedApex(Apex):
    def __init__(self, module_path: str):
        super().__init__(module_path)

        pb_manifest = f"{self.module_path}/apex_manifest.pb"
        json_manifest = f"{self.module_path}/apex_manifest.json"
        if os.path.isfile(pb_manifest):
            with open(pb_manifest, "rb") as f:
                manifest_bytes = f.read()

            self.name = parse_apex_manifest_for_name(manifest_bytes)
        elif os.path.isfile(json_manifest):
            import json

            with open(json_manifest, "r") as f:
                self.name = json.load(f)["name"]

    def mount(self) -> None:
        target_path = self.get_mountpoint()
        os.makedirs(target_path, mode=0o755, exist_ok=True)

        print(f"Mounting flattened APEX {self.module_path} at {target_path}")
        subprocess.run(
            ["mount", "-o", "bind,ro", self.module_path, target_path], check=True
        )
        self.record()

    def record(self) -> None:
        MOUNTED_RECORDS.append((self.name, self.module_path, "bind", "-"))


class ArchivedApex(Apex):
    def __init__(self, module_path: str):
        super().__init__(module_path)

        with zipfile.ZipFile(self.module_path, "r") as zf:
            with zf.open("apex_manifest.pb", "r") as f:
                manifest_bytes = f.read()

        self.name = parse_apex_manifest_for_name(manifest_bytes)

    def payload(self):
        # Where the filesystem image starts inside the archive, and its type
        # when it is ext4: left to guess, mount tries ext3 and ext2 first and
        # each attempt fails on the loop device.
        with zipfile.ZipFile(self.module_path, "r") as zf:
            with zf.open("apex_payload.img", "r") as f:
                offset = f._orig_compress_start
        with open(self.module_path, "rb") as f:
            f.seek(offset + 1024 + 56)
            fstype = "ext4" if f.read(2) == b"\x53\xef" else "-"
        return offset, fstype

    def mount(self) -> None:
        target_path = self.get_mountpoint()
        os.makedirs(target_path, mode=0o755, exist_ok=True)

        offset, fstype = self.payload()
        cmd = ["mount", "-o", f"loop,offset={offset},ro"]
        if fstype != "-":
            cmd += ["-t", fstype]

        print(f"Mounting APEX file {self.module_path} at {target_path}")

        subprocess.run(cmd + [self.module_path, target_path], check=True)
        MOUNTED_RECORDS.append((self.name, self.module_path, str(offset), fstype))

    def record(self) -> None:
        offset, fstype = self.payload()
        MOUNTED_RECORDS.append((self.name, self.module_path, str(offset), fstype))


def open_apex(module_path: str) -> Apex:
    if os.path.isdir(module_path):
        return FlattenedApex(module_path)
    elif (
        os.path.isfile(module_path)
        and module_path.endswith(".apex")
        and not module_path.endswith("_compressed.apex")
    ):
        return ArchivedApex(module_path)
    else:
        raise RuntimeError(f"Don't know how to handle {module_path}")


def should_mount_apex(apex_name: str, apex_globs: list):
    if len(apex_globs) == 0:
        return True

    for glob in apex_globs:
        if fnmatch.fnmatch(apex_name, glob):
            return True

    return False


def name_hint(entry: str) -> str:
    # The name a module's file or directory suggests. Usually the APEX's own
    # name, but not always (see the top of this file), so it only decides
    # which manifests are worth reading first.
    return entry[: -len(".apex")] if entry.endswith(".apex") else entry


def mount_one(path: str, apex_globs: list, mounted: set) -> None:
    try:
        apex = open_apex(path)

        if not should_mount_apex(apex.name, apex_globs):
            return

        if apex.is_mounted():
            print(f"WARNING: APEX named {apex.name} is already mounted.")
            apex.record()
            mounted.add(apex.name)
            return

        apex.mount()
        mounted.add(apex.name)
    except Exception as e:
        print(f"WARNING: failed to mount APEX {path}: {e}")


def write_cache(path: str, apex_globs: list) -> None:
    # One line per mounted module: name, file, its size and mtime (so a changed
    # GSI invalidates the entry), and how to mount it. The first line records
    # the arguments, since a different request needs a different set.
    lines = ["# args: " + " ".join(apex_globs)]
    for name, module_path, offset, fstype in MOUNTED_RECORDS:
        st = os.stat(module_path)
        lines.append(f"{name} {module_path} {st.st_size} {int(st.st_mtime)} {offset} {fstype}")
    tmp = path + ".tmp"
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(tmp, "w") as f:
        f.write("\n".join(lines) + "\n")
    os.chmod(tmp, 0o644)
    os.replace(tmp, path)


def main() -> int:
    # No argparse: on the slower devices its import alone is a noticeable part
    # of this step, and all there is to parse is a list of names.
    apex_globs: list = sys.argv[1:]

    os.makedirs(APEX_ROOT, exist_ok=True)
    if not os.path.ismount(APEX_ROOT):
        subprocess.run(
            ["mount", "-t", "tmpfs", "android_apex", APEX_ROOT], check=True
        )

    paths = []
    for dir in APEX_PREINSTALLED_DIRS:
        try:
            entries = sorted(os.listdir(dir))
        except:
            continue
        paths += [f"{dir}/{entry}" for entry in entries]

    # Opening every archive to read its manifest was most of the time this took:
    # the GSI carries twenty-odd and only a handful are wanted. Read the ones
    # whose file name already matches first, and only read the rest for a name
    # or glob that is still unmatched after that - a module whose file is named
    # differently from the APEX inside it is still found, it just costs the
    # full scan it always did.
    mounted: set = set()
    hinted = [p for p in paths if should_mount_apex(name_hint(os.path.basename(p)), apex_globs)]
    for path in hinted:
        mount_one(path, apex_globs, mounted)

    unmatched = [g for g in apex_globs if not any(fnmatch.fnmatch(n, g) for n in mounted)]
    if apex_globs and not unmatched:
        paths = []

    for path in paths:
        if path in hinted:
            continue
        mount_one(path, unmatched if apex_globs else [], mounted)

    cache = os.environ.get("MOUNT_APEXES_CACHE")
    if cache and MOUNTED_RECORDS:
        try:
            write_cache(cache, apex_globs)
        except OSError as e:
            print(f"WARNING: could not write {cache}: {e}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
