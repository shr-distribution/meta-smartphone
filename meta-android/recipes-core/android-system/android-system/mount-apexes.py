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

import argparse
import fnmatch
import os
import subprocess
import sys
import zipfile
import json

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
            with open(json_manifest, "r") as f:
                self.name = json.load(f)["name"]

    def mount(self) -> None:
        target_path = self.get_mountpoint()
        os.makedirs(target_path, mode=0o755, exist_ok=True)

        print(f"Mounting flattened APEX {self.module_path} at {target_path}")
        subprocess.run(
            ["mount", "-o", "bind,ro", self.module_path, target_path], check=True
        )


class ArchivedApex(Apex):
    def __init__(self, module_path: str):
        super().__init__(module_path)

        with zipfile.ZipFile(self.module_path, "r") as zf:
            with zf.open("apex_manifest.pb", "r") as f:
                manifest_bytes = f.read()

        self.name = parse_apex_manifest_for_name(manifest_bytes)

    def mount(self) -> None:
        target_path = self.get_mountpoint()
        os.makedirs(target_path, mode=0o755, exist_ok=True)

        with zipfile.ZipFile(self.module_path, "r") as zf:
            with zf.open("apex_payload.img", "r") as f:
                offset = f._orig_compress_start

        print(f"Mounting APEX file {self.module_path} at {target_path}")

        subprocess.run(
            ["mount", "-o", f"loop,offset={offset},ro", self.module_path, target_path],
            check=True,
        )


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


def parse_arguments():
    parser = argparse.ArgumentParser(
        prog="mount-apexes.py",
        description="Mount APEXes as specified on command line",
    )
    parser.add_argument(
        "apex_globs",
        nargs="*",
        help="APEX name/glob to mount. If not specified, will mount all APEXes.",
    )

    return parser.parse_args()


def main() -> int:
    args = parse_arguments()
    apex_globs: list = args.apex_globs

    os.makedirs(APEX_ROOT, exist_ok=True)
    if not os.path.ismount(APEX_ROOT):
        subprocess.run(
            ["mount", "-t", "tmpfs", "android_apex", APEX_ROOT], check=True
        )

    for dir in APEX_PREINSTALLED_DIRS:
        try:
            entries = os.listdir(dir)
        except:
            continue

        for entry in entries:
            try:
                apex = open_apex(f"{dir}/{entry}")

                if not should_mount_apex(apex.name, apex_globs):
                    continue

                if apex.is_mounted():
                    print(f"WARNING: APEX named {apex.name} is already mounted.")
                    continue

                apex.mount()
            except Exception as e:
                print(f"WARNING: failed to mount APEX {dir}/{entry}: {e}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
