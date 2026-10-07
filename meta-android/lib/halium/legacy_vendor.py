# SPDX-License-Identifier: MIT
#
# Turn the vendor half of a pre-Treble Halium 9 system.img into a Treble-shaped
# vendor.img, so a device that never had a /vendor partition can run a GSI.
#
# tissot-halium already does this, but starts from a vendor.img that
# `m vendorimage` produced. hammerhead, mako and tenderloin are older: their
# Halium 9 build ships one monolithic system.img with the vendor payload under
# /system/vendor, and several things a Treble vendor owns are still scattered
# over the rest of the image:
#
#   - HAL modules left in /system/lib/hw (audio.primary, camera.<dev>, lights, ...)
#   - Qualcomm and display libraries in /system/lib that the vendor libraries
#     link against. A real vendor namespace sees only /vendor and the VNDK APEX,
#     so these cannot stay behind in a system image that gets replaced.
#   - the daemons the device init script starts from /system/bin
#   - the device init scripts themselves, which sit at the root of a Halium 9
#     system-as-root image but which Android 16 init reads from
#     /vendor/etc/init/hw/ and /vendor/etc/ueventd.rc
#
# This module moves all of that into the new image, and reports what it could
# not resolve instead of guessing.
#
# Everything is read and written through debugfs, not through a mounted loop
# device or mke2fs -d: the build runs unprivileged, and debugfs is the only way
# to give the files root ownership regardless of who runs bitbake.

import os
import re
import shutil
import struct
import subprocess
import tempfile
import zipfile

# Where the GSI lists the libraries a vendor process may always link: the
# platform's LLNDK. This is read from the GSI rather than written out here, because
# a hand-written list was wrong (it claimed libstdc++.so, which a vendor of this
# generation does not get, so qseecomd and vss_init failed to link).
LLNDK_LIST = "/system/etc/llndk.libraries.txt"

# Android 9 vendors keep these mount points at the root; a Treble vendor and
# mount-android.sh use the right-hand side. The GSI gets a symlink from the
# old name, because vendor binaries still open /firmware/image and /persist.
MOUNT_POINT_MAP = {
    "/firmware": "/vendor/firmware_mnt",
    "/persist": "/mnt/vendor/persist",
    "/dsp": "/vendor/dsp",
}

SHELL_GID = 2000

# Library directories of an image, by ELF class: a 64-bit build has both.
LIBDIRS = ("lib", "lib64")

# ext4 features a 3.4 kernel can mount read-write, as the old Halium 9 images
# use them. mke2fs -O takes this list; check_features() rejects anything else.
MOUNTABLE_ON_3_4 = ("ext_attr", "resize_inode", "dir_index", "filetype", "extent",
                    "flex_bg", "sparse_super", "large_file", "huge_file",
                    "uninit_bg", "dir_nlink", "extra_isize")

_S_IFMT = 0o170000
_S_IFDIR = 0o040000
_S_IFREG = 0o100000
_S_IFLNK = 0o120000


class Image:
    """Read access, and optionally write access, to an ext2/3/4 image."""

    def __init__(self, path):
        self.path = path

    def _run(self, request, write=False):
        cmd = ["debugfs"] + (["-w"] if write else []) + ["-R", request, self.path]
        res = subprocess.run(cmd, capture_output=True)
        return res.stdout.decode("utf-8", "replace")

    def batch(self, lines):
        """Run many debugfs commands in one process."""
        with tempfile.NamedTemporaryFile("w", suffix=".cmd") as f:
            f.write("\n".join(lines) + "\n")
            f.flush()
            res = subprocess.run(["debugfs", "-w", "-f", f.name, self.path],
                                 capture_output=True)
        return res.stdout.decode("utf-8", "replace") + res.stderr.decode("utf-8", "replace")

    def ls(self, path):
        """[(name, mode, uid, gid)] for a directory, [] if it is not one."""
        out = []
        for line in self._run("ls -p %s" % path).splitlines():
            parts = line.split("/")
            # /inode/mode/uid/gid/name/size/
            if len(parts) < 7 or not parts[1].isdigit():
                continue
            name = parts[5]
            if name in (".", ".."):
                continue
            out.append((name, int(parts[2], 8), int(parts[3]), int(parts[4])))
        return out

    def stat_mode(self, path):
        """The mode of path, or None when it does not exist."""
        parent, _, name = path.rstrip("/").rpartition("/")
        for n, mode, _, _ in self.ls(parent or "/"):
            if n == name:
                return mode
        return None

    def exists(self, path):
        return self.stat_mode(path) is not None

    def readlink(self, path):
        out = self._run("stat %s" % path)
        m = re.search(r'Fast link dest: "(.*)"', out)
        if m:
            return m.group(1)
        return self._run("cat %s" % path).rstrip("\n")

    def cat(self, path):
        res = subprocess.run(["debugfs", "-R", "cat %s" % path, self.path],
                             capture_output=True)
        return res.stdout

    def dump_many(self, pairs):
        """Extract [(image_path, host_path)] in one debugfs process."""
        for _, dest in pairs:
            os.makedirs(os.path.dirname(dest), exist_ok=True)
        with tempfile.NamedTemporaryFile("w", suffix=".cmd") as f:
            f.write("".join("dump %s %s\n" % (src, dst) for src, dst in pairs))
            f.flush()
            subprocess.run(["debugfs", "-f", f.name, self.path], capture_output=True)


def raw_image(path, workdir):
    """path as a raw ext image, un-sparsing it into workdir when needed."""
    with open(path, "rb") as f:
        magic = f.read(4)
    if magic != b"\x3a\xff\x26\xed":
        return path
    raw = os.path.join(workdir, os.path.basename(path) + ".raw")
    subprocess.run(["simg2img", path, raw], check=True)
    return raw


def needed(path):
    """DT_NEEDED sonames of an ELF file; [] for anything that is not a 32/64-bit LE ELF."""
    try:
        with open(path, "rb") as f:
            data = f.read()
    except OSError:
        return []
    if data[:4] != b"\x7fELF" or data[5] != 1:
        return []
    is64 = data[4] == 2
    if is64:
        shoff, = struct.unpack_from("<Q", data, 0x28)
        shentsize, shnum = struct.unpack_from("<HH", data, 0x3A)
    else:
        shoff, = struct.unpack_from("<I", data, 0x20)
        shentsize, shnum = struct.unpack_from("<HH", data, 0x2E)
    if not shoff or not shnum:
        return []
    # Qualcomm firmware metadata (.mdt) is an ELF header and program headers only: its section
    # header table points past the end of the file. Nothing in it is linked against anything.
    if shoff + shnum * shentsize > len(data):
        return []
    sections = []
    for i in range(shnum):
        off = shoff + i * shentsize
        if is64:
            typ, = struct.unpack_from("<I", data, off + 4)
            soff, ssize, link = struct.unpack_from("<QQI", data, off + 0x18)
        else:
            typ, = struct.unpack_from("<I", data, off + 4)
            soff, ssize, link = struct.unpack_from("<III", data, off + 0x10)
        sections.append((typ, soff, ssize, link))
    names = []
    for typ, soff, ssize, link in sections:
        if typ != 6:    # SHT_DYNAMIC
            continue
        stroff = sections[link][1]
        step = 16 if is64 else 8
        for pos in range(soff, soff + ssize, step):
            tag, val = struct.unpack_from("<qQ" if is64 else "<iI", data, pos)
            if tag == 0:
                break
            if tag == 1:    # DT_NEEDED
                end = data.index(b"\0", stroff + val)
                names.append(data[stroff + val:end].decode())
    return names


def elf_libdir(path):
    """"lib64" for a 64-bit ELF file, "lib" for anything else."""
    try:
        with open(path, "rb") as f:
            head = f.read(5)
    except OSError:
        return "lib"
    return "lib64" if head[:4] == b"\x7fELF" and head[4:5] == b"\x02" else "lib"


def vndk_sonames(gsi, version, workdir):
    """Library names the GSI's com.android.vndk.v<version> APEX provides."""
    apex = None
    for base in ("/system_ext/apex", "/system/system_ext/apex", "/system/apex"):
        p = "%s/com.android.vndk.v%s.apex" % (base, version)
        if gsi.exists(p):
            apex = p
            break
    if apex is None:
        raise RuntimeError("the GSI carries no com.android.vndk.v%s APEX; this vendor "
                           "needs a GSI built with PRODUCT_EXTRA_VNDK_VERSIONS including "
                           "%s" % (version, version))
    host = os.path.join(workdir, "vndk%s.apex" % version)
    gsi.dump_many([(apex, host)])
    with zipfile.ZipFile(host) as z:
        z.extract("apex_payload.img", workdir)
    payload = Image(os.path.join(workdir, "apex_payload.img"))
    names = set()
    for d in ("/lib", "/lib64"):
        names.update(n for n, _, _, _ in payload.ls(d) if n.endswith(".so"))
    # The APEX also carries the VNDK-private libraries (libgui, libbacktrace,
    # libunwind, ...). Only other VNDK libraries may link those; vendor code that
    # needs one has to ship its own copy, so they do not count as provided.
    private = set(payload.cat("/etc/vndkprivate.libraries.%s.txt" % version)
                  .decode("utf-8", "replace").split())
    names -= private
    os.unlink(host)
    os.unlink(os.path.join(workdir, "apex_payload.img"))
    if not names:
        raise RuntimeError("com.android.vndk.v%s.apex contains no libraries" % version)
    return names



def _walk(img, root):
    """[(path_relative_to_root, mode, uid, gid)] for everything below root."""
    out = []
    stack = [""]
    while stack:
        rel = stack.pop()
        for name, mode, uid, gid in img.ls(root + "/" + rel if rel else root):
            r = rel + "/" + name if rel else name
            out.append((r, mode, uid, gid))
            if (mode & _S_IFMT) == _S_IFDIR:
                stack.append(r)
    return out


def _stanzas(text):
    """Split an Android init script into (kind, name, [lines]) stanzas."""
    out = []
    cur = None
    for line in text.splitlines():
        m = re.match(r"(service|on|import)\s+(\S+)", line)
        if m and not line[:1].isspace():
            cur = [m.group(1), m.group(2), [line]]
            out.append(cur)
        elif cur is None:
            cur = ["", "", [line]]
            out.append(cur)
        else:
            cur[2].append(line)
    return out


# Mount points that must stay real directories, not symlinks to their /mnt/vendor home.
# The sensor daemon resolves /persist/sensors, sees /mnt/vendor/persist/sensors and
# rejects it ("sns_fsa_la.c: invalid directory path"), so it never gets a registry.
# The container config bind-mounts the host's /mnt/vendor/persist onto /persist.
REAL_DIR_MOUNTS = {"/persist"}

# Mount points an fstab names that mount-android.sh does not mount itself (the host or the container's
# own init owns them), and filesystem types that are not mounts.
FSTAB_SKIP_MOUNTS = {"/", "/system", "/data", "/vendor", "/misc", "/system_ext", "/product", "/odm",
                     "/cache", "/boot", "/recovery", "auto", "none"}
FSTAB_SKIP_TYPES = {"emmc", "swap", "mtd"}


def vendor_mount_map(old, gsi):
    """Where the partitions an Android 10/11 vendor's own fstab mounts at the root have to go.

    Such a vendor keeps its fstab in /system/vendor/etc already, so the root fstab handling of a
    Halium 9 image does not see it, but its mount points are still root directories the old system
    image provided (BOARD_ROOT_EXTRA_FOLDERS) and a plain GSI does not have: mount-android.sh mounts
    at /android<mount point> and skips any it cannot create on the read-only GSI. A mount point the
    GSI has as a real directory stays; one MOUNT_POINT_MAP knows moves there (/firmware ->
    /vendor/firmware_mnt, which the GSI links /firmware to); any other moves to a directory of the
    same name in the vendor image (/firmware-modem -> /vendor/firmware-modem).

    Returns {old_mount_point: new_mount_point} for the mount points that move.
    """
    moves = {}
    for name, mode, _, _ in old.ls("/system/vendor/etc"):
        if not name.startswith("fstab.") or (mode & _S_IFMT) != _S_IFREG:
            continue
        for line in old.cat("/system/vendor/etc/" + name).decode("utf-8", "replace").splitlines():
            fields = line.split()
            if len(fields) < 4 or line.lstrip().startswith("#"):
                continue
            dst, fstype = fields[1], fields[2]
            if dst in FSTAB_SKIP_MOUNTS or fstype in FSTAB_SKIP_TYPES:
                continue
            if not dst.startswith("/") or "/" in dst[1:]:
                continue
            gsi_mode = gsi.stat_mode(dst)
            if gsi_mode is not None and (gsi_mode & _S_IFMT) == _S_IFDIR:
                continue
            moves[dst] = MOUNT_POINT_MAP.get(dst, "/vendor" + dst)
    return moves


def remap_mount_target(target, moves):
    """A path under a moved mount point, at its new place; anything else unchanged."""
    for old_dst, new_dst in moves.items():
        if target == old_dst or target.startswith(old_dst + "/"):
            return new_dst + target[len(old_dst):]
    return target


def remap_vendor_fstab(text, moves):
    """Rewrite the mount points of a vendor's own fstab with vendor_mount_map's moves."""
    out = []
    for line in text.splitlines():
        fields = line.split()
        if len(fields) >= 4 and not line.lstrip().startswith("#") and fields[1] in moves:
            line = re.sub(r"^(\S+\s+)%s(\s)" % re.escape(fields[1]),
                          lambda m: m.group(1) + moves[fields[1]] + m.group(2), line)
        out.append(line)
    return "\n".join(out) + "\n"


def adapt_fstab(text):
    """Point an Android 9 fstab's mount points at where a Treble vendor mounts them.

    Returns (new_text, [(old_mount_point, new_mount_point)]).
    """
    renamed = []
    out = []
    for line in text.splitlines():
        fields = line.split()
        if len(fields) >= 4 and not line.lstrip().startswith("#") and fields[1] in MOUNT_POINT_MAP:
            new = MOUNT_POINT_MAP[fields[1]]
            if (fields[1], new) not in renamed:
                renamed.append((fields[1], new))
            line = re.sub(r"^(\S+\s+)%s(\s)" % re.escape(fields[1]), lambda m: m.group(1) + new + m.group(2), line)
        out.append(line)
    return "\n".join(out) + "\n", renamed


def adapt_vintf(text, hals):
    """Declare the named HIDL HALs as hwbinder instead of passthrough.

    The manifest of a monolithic Halium 9 build calls every HAL passthrough, because
    there the HAL library is loaded into whatever process asks for it. Here the
    composer runs as its own service in the container, and a libhidl client on the
    host that is told "passthrough" skips that service and loads the HAL into its own
    process: it then cannot own the display the service already holds, and the
    compositor dies with "failed to get hwcomposer service".
    """
    changed = []

    def fix(match):
        block = match.group(0)
        name = re.search(r"<name>([^<]+)</name>", block)
        if name and name.group(1).strip() in hals and "passthrough" in block:
            changed.append(name.group(1).strip())
            # arch="32" belongs to a passthrough transport only; on hwbinder VINTF
            # rejects the entry, and hwservicemanager then cannot find the HAL at all.
            block = re.sub(r'<transport\b[^>]*>\s*passthrough\s*</transport>',
                           "<transport>hwbinder</transport>", block)
        return block

    text = re.sub(r"<hal\b.*?</hal>", fix, text, flags=re.S)
    return text, changed


def adapt_init_script(text, old, gsi, skip, shipped, log, skip_commands=()):
    """Rewrite one device init script for life under /vendor.

    Returns (new_text, [daemon names that must move from /system/bin]).
    """
    moves = []
    out = []
    for kind, name, lines in _stanzas(text):
        if kind == "service" and name in skip:
            log("dropped service %s" % name)
            continue
        for i, line in enumerate(lines):
            if kind == "import":
                m = re.match(r"import\s+(?!/)(\S+\.rc)\s*$", line)
                if m and m.group(1) in shipped and old.exists("/" + m.group(1)):
                    lines[i] = "import /vendor/etc/init/hw/%s" % m.group(1)
                elif m:
                    # Only scripts this conversion ships stay imported. In particular
                    # init.<device>.usb.rc must not: it writes the shared
                    # /sys/class/android_usb gadget, which the host's adbd owns.
                    log("import %s dropped: not shipped" % m.group(1))
                    lines[i] = "# import %s dropped: not shipped" % m.group(1)
            elif kind == "service" and i == 0:
                m = re.match(r"(service\s+\S+\s+)/system/bin/(\S+)(.*)", line)
                if m:
                    binary = m.group(2)
                    if old.exists("/system/bin/" + binary) and not gsi.exists("/system/bin/" + binary):
                        moves.append(binary)
                        lines[i] = "%s/vendor/bin/%s%s" % (m.group(1), binary, m.group(3))
                    elif not old.exists("/system/bin/" + binary):
                        log("service %s: /system/bin/%s is in neither image" % (name, binary))
            elif kind == "on" and re.match(r"\s+mount_all\b", line):
                # The container has no block devices of its own to mount.
                lines[i] = "    # mount_all dropped: no fstab in a container"
            elif kind == "on" and any(re.search(p, line) for p in skip_commands):
                # Things the host already does and a container must not do again (mounting a
                # partition the host has mounted, activating volume groups it has activated).
                log("command dropped: %s" % line.strip())
                lines[i] = "    # dropped: %s" % line.strip()
        out.extend(lines)
    return "\n".join(out) + "\n", moves


class Plan:
    """Everything that will go into the vendor image, and what is still open."""

    def __init__(self):
        self.entries = []       # (path, mode, uid, gid, kind, payload)
        self.paths = set()
        self.unresolved = {}
        self.gsi_files = []     # /system/etc paths to copy from the old image
        self.gsi_symlinks = []  # (link, target) to create in the GSI
        self.gsi_dirs = []      # empty directories the container bind-mounts onto
        self.notes = []

    def add(self, path, mode, uid, gid, kind, payload=None):
        if path in self.paths:
            return
        self.paths.add(path)
        self.entries.append((path, mode, uid, gid, kind, payload))

    def add_parents(self, path):
        parts = path.strip("/").split("/")[:-1]
        for i in range(1, len(parts) + 1):
            self.add("/" + "/".join(parts[:i]), _S_IFDIR | 0o755, 0, SHELL_GID, "dir")


def _apply_shims(plan, stage, shim_lib, shim_targets, ld_shims, log):
    """Add dependencies to libraries of the vendor image, on the staged host copies.

    ld_shims are "<library>|<shim library>" pairs, what LineageOS calls TARGET_LD_SHIM_LIBS: its own
    linker loads the shim next to the library, the Android 16 linker does not, so the shim becomes a
    DT_NEEDED entry of the library. Both are file names in the vendor image's /lib.

    shim_targets are libraries that get the generic shim (libhalium_legacy_shim.so, from shim_lib) as a
    dependency, when the vendor image has them: the framework copies a legacy vendor brings along want
    symbols of the full libbinder and libmedia that the VNDK snapshot leaves out.
    """
    if not shim_targets and not ld_shims:
        return
    patchelf = shutil.which("patchelf")
    if patchelf is None:
        raise RuntimeError("HALIUM_LEGACY_SHIM_TARGETS / HALIUM_LEGACY_LD_SHIMS need patchelf; "
                           "add patchelf-native to DEPENDS")
    by_name = {}
    for e in plan.entries:
        if e[4] == "file" and isinstance(e[5], str) and e[0].endswith(".so"):
            by_name.setdefault(os.path.basename(e[0]), []).append(e)

    def add_needed(name, soname):
        done = False
        for e in by_name.get(name, []):
            if soname not in needed(e[5]):
                subprocess.run([patchelf, "--add-needed", soname, e[5]], check=True)
                done = True
        return done

    present = [t for t in shim_targets if t in by_name]
    if present:
        if not shim_lib or not os.path.isfile(shim_lib):
            raise RuntimeError("HALIUM_LEGACY_SHIM_TARGETS names %s but there is no shim library at %r"
                               % (" ".join(present), shim_lib))
        name = "libhalium_legacy_shim.so"
        host = os.path.join(stage, name)
        shutil.copyfile(shim_lib, host)
        plan.add_parents("/lib/" + name)
        plan.add("/lib/" + name, _S_IFREG | 0o644, 0, 0, "file", host)
        for t in present:
            if add_needed(t, name):
                log("shim: %s now needs %s" % (t, name))
    for pair in ld_shims:
        target, _, shim = pair.partition("|")
        if target not in by_name:
            log("ld shim: %s is not in the vendor image, skipping %s" % (target, pair))
        elif shim not in by_name:
            raise RuntimeError("ld shim %s: %s is not in the vendor image" % (pair, shim))
        elif add_needed(target, shim):
            log("shim: %s now needs %s" % (target, shim))


def build(old_system, gsi_system, out_vendor, *, vndk, init_files,
          skip_services=(), extra_libs=(), extra_files=(), vndk_names=None,
          binderized_hals=(), copy_files=(), gsi_files=(), gsi_links=(), gsi_dirs=(), gsi_autodetect=True, system_prop_prefixes=(),
          shim_lib=None, shim_targets=(), ld_shims=(), skip_commands=(), ueventd_extra=(),
          slack_mb=24, workdir=None, log=print):
    """Write out_vendor, and return (plan, gsi_patches) for the caller to apply."""
    own = workdir is None
    if own:
        tmp = tempfile.TemporaryDirectory()
        workdir = tmp.name
    os.makedirs(workdir, exist_ok=True)
    old = Image(raw_image(old_system, workdir))
    gsi = Image(raw_image(gsi_system, workdir))
    stage = os.path.join(workdir, "stage")
    shutil.rmtree(stage, ignore_errors=True)
    os.makedirs(stage)

    # vndk "" is a vendor built without VNDK (RELEASE_DEPRECATE_VNDK, Android 14 QPR2 and later):
    # it carries its own copies of libbinder, libhidlbase and the rest, sets no ro.vndk.version,
    # and the GSI's linkerconfig then gives it the LLNDK only. No VNDK APEX is needed.
    if vndk_names is None:
        vndk_names = vndk_sonames(gsi, vndk, workdir) if vndk else set()
    plan = Plan()

    # 1. /system/vendor becomes the root of the new image, ownership included.
    pull = []
    for rel, mode, uid, gid in _walk(old, "/system/vendor"):
        path = "/" + rel
        kind = {_S_IFDIR: "dir", _S_IFLNK: "link"}.get(mode & _S_IFMT, "file")
        if kind == "file":
            host = os.path.join(stage, "vendor", rel)
            pull.append(("/system/vendor/" + rel, host))
            plan.add(path, mode, uid, gid, "file", host)
        elif kind == "link":
            plan.add(path, mode, uid, gid, "link", old.readlink("/system/vendor/" + rel))
        else:
            plan.add(path, mode, uid, gid, "dir")

    # 2. HAL modules the old build left in /system/lib/hw, and /system/lib64/hw when the vendor is
    #    64-bit too (bullhead's LineageOS 21: camera.msm8992 in lib, gps.msm8992 and power.bullhead in
    #    both). Only then: the Halium 9 image of the 32-bit hammerhead has a /system/lib64/hw as well.
    libdirs = LIBDIRS if "/lib64" in plan.paths else ("lib",)
    for libdir in libdirs:
        gsi_hw = {n for n, _, _, _ in gsi.ls("/system/%s/hw" % libdir)}
        for name, mode, uid, gid in old.ls("/system/%s/hw" % libdir):
            dest = "/%s/hw/%s" % (libdir, name)
            if name.endswith(".so") and name not in gsi_hw and dest not in plan.paths:
                host = os.path.join(stage, "syshw" if libdir == "lib" else "syshw64", name)
                pull.append(("/system" + dest, host))
                plan.add_parents(dest)
                plan.add(dest, _S_IFREG | 0o644, 0, 0, "file", host)

    # 2a. The GPU driver, when the old build left it in /system/lib/egl rather than
    #     /system/vendor/lib/egl (tenderloin: libEGL_adreno200.so, libGLESv2_adreno200.so,
    #     eglsubAndroid.so, ...). The EGL loader of the GSI and libhybris on the host look in
    #     /vendor/lib/egl; without it the compositor finds no OpenGL ES implementation and
    #     aborts. What the GSI ships in its own /system/lib/egl stays the GSI's. The libraries
    #     these need (libgsl.so, libC2D2.so, ...) follow in step 4, as every file is a root.
    gsi_egl = {n for n, _, _, _ in gsi.ls("/system/lib/egl")}
    moved_egl = []
    for name, mode, uid, gid in old.ls("/system/lib/egl"):
        if (mode & _S_IFMT) != _S_IFREG or name in gsi_egl or "/lib/egl/" + name in plan.paths:
            continue
        host = os.path.join(stage, "sysegl", name)
        pull.append(("/system/lib/egl/" + name, host))
        plan.add_parents("/lib/egl/" + name)
        plan.add("/lib/egl/" + name, _S_IFREG | 0o644, 0, 0, "file", host)
        moved_egl.append(name)
    if moved_egl:
        log("egl: %d files from /system/lib/egl to /vendor/lib/egl: %s"
            % (len(moved_egl), " ".join(moved_egl)))

    # 2b. Firmware the old build left in /system/etc/firmware. ueventd searches
    #     /vendor/firmware but no longer /system/etc/firmware, and a missing blob
    #     is not harmless: hammerhead's msm_cpp driver dereferences NULL when
    #     cpp_firmware_v1_2_0.fw cannot be loaded, which panics the kernel as soon
    #     as the camera daemon opens it.
    #
    #     An Android 10/11 build keeps symlinks there instead, into the partitions its vendor fstab
    #     mounts (msm8916's modem.mdt -> /firmware-modem/image/modem.mdt). They go to /vendor/firmware
    #     as links, pointing at where those partitions are mounted now (see vendor_mount_map).
    #
    #     The whole tree, not only its top level: the TouchPad keeps its Wi-Fi firmware and board
    #     data in /system/etc/firmware/ath6k/AR6003/hw2.1.1/, and ath6kl asks for
    #     ath6k/AR6003/hw2.1.1/bdata.bin, which the container's ueventd then has to find in
    #     /vendor/firmware/ath6k/... (7 Oct 2026: no wlan0 without it).
    mount_moves = vendor_mount_map(old, gsi)
    moved_fw = []
    linked_fw = []
    for name, mode, uid, gid in _walk(old, "/system/etc/firmware"):
        if (mode & _S_IFMT) == _S_IFDIR:
            continue
        if (mode & _S_IFMT) == _S_IFLNK and "/firmware/" + name not in plan.paths:
            target = old.readlink("/system/etc/firmware/" + name)
            if target.startswith("/"):
                plan.add_parents("/firmware/" + name)
                plan.add("/firmware/" + name, _S_IFLNK | 0o777, 0, 0, "link",
                         remap_mount_target(target, mount_moves))
                linked_fw.append(name)
            continue
        if (mode & _S_IFMT) != _S_IFREG or "/firmware/" + name in plan.paths:
            continue
        host = os.path.join(stage, "sysfw", name)
        pull.append(("/system/etc/firmware/" + name, host))
        plan.add_parents("/firmware/" + name)
        plan.add("/firmware/" + name, _S_IFREG | 0o644, 0, 0, "file", host)
        moved_fw.append(name)
    if moved_fw:
        log("firmware: %d files from /system/etc/firmware to /vendor/firmware: %s"
            % (len(moved_fw), " ".join(moved_fw)))
    if linked_fw:
        log("firmware: %d links from /system/etc/firmware to /vendor/firmware: %s"
            % (len(linked_fw), " ".join(linked_fw)))

    # 2c. Files the caller supplies from outside the old image, as
    #     [(host_path, path_in_vendor, mode)]. This is for what a monolithic Halium 9
    #     build never had to ship: the HIDL service wrappers (composer, allocator)
    #     for the passthrough -impl libraries the vendor does carry.
    for src, dest, mode in extra_files:
        if not os.path.isfile(src):
            raise RuntimeError("extra file %s does not exist" % src)
        plan.add_parents(dest)
        plan.add(dest, _S_IFREG | mode, 0, SHELL_GID if dest.startswith("/bin/") else 0, "file", src)
        log("extra: %s -> /vendor%s" % (os.path.basename(src), dest))

    # 2d. Files of the old image the caller wants in the vendor image, as
    #     [(path_in_old_image, path_in_vendor)]. For config the host or the kernel opens
    #     by path and that the GSI, with no inodes to spare, cannot carry (hammerhead's
    #     bcmdhd reads /etc/wifi/bcmdhd.cal, which the image links to /vendor/etc/wifi).
    for src, dest in copy_files:
        if not old.exists(src):
            log("copy: %s is not in the old image" % src)
            continue
        host = os.path.join(stage, "copy", dest.lstrip("/"))
        os.makedirs(os.path.dirname(host), exist_ok=True)
        pull.append((src, host))
        plan.add_parents(dest)
        plan.add(dest, _S_IFREG | 0o644, 0, 0, "file", host)
        log("copy: %s -> /vendor%s" % (src, dest))

    # 3. The device init scripts, and the daemons they start from /system/bin.
    skip = set(skip_services)
    for rc in init_files:
        if not old.exists("/" + rc):
            log("init script /%s is not in the old image" % rc)
            continue
        if rc.startswith("ueventd."):
            dest = "/etc/ueventd.rc"
            text = old.cat("/" + rc).decode("utf-8", "replace")
            moves = []
            # Rules the caller adds, as host files appended in order. The GSI's ueventd reads only
            # /system/etc/ueventd.rc and what it imports (/vendor/etc/ueventd.rc), so a fragment of
            # its own would not be read: the tenderloin camera's `subsystem msm_camera` block.
            for extra in ueventd_extra:
                with open(extra, encoding="utf-8") as f:
                    text = text.rstrip("\n") + "\n\n" + f.read()
                log("ueventd: appended %s" % os.path.basename(extra))
        else:
            dest = "/etc/init/hw/" + rc
            text, moves = adapt_init_script(old.cat("/" + rc).decode("utf-8", "replace"),
                                            old, gsi, skip, set(init_files), log,
                                            skip_commands=skip_commands)
        # `symlink /system/etc/<file> <elsewhere>` in a board rc points a runtime path at
        # a config file of the old /system. The init script is ours to edit, so ship the
        # file in the vendor image and point the link there, instead of spending an
        # inode on it in the GSI (qcril.db: no free inode left there).
        if not rc.startswith("ueventd."):
            def _move_symlink_target(m):
                src = m.group(3)
                if not old.exists(src) or gsi.exists(src):
                    return m.group(0)
                new_src = "/vendor" + src[len("/system"):]
                host_cfg = os.path.join(stage, "cfg", os.path.basename(src))
                os.makedirs(os.path.dirname(host_cfg), exist_ok=True)
                pull.append((src, host_cfg))
                plan.add_parents(src[len("/system"):])
                plan.add(src[len("/system"):], _S_IFREG | 0o644, 0, 0, "file", host_cfg)
                log("rc: %s moves to %s" % (src, new_src))
                # init's `symlink` fails when the path exists, and a data partition that predates this
                # conversion still has the old link (qcril.db: "Failed to open qcril db 14"), so remove
                # whatever is there first.
                indent, gap, dest = m.group(1), m.group(2), m.group(4)
                return "%srm %s\n%ssymlink%s%s%s" % (indent, dest.strip(), indent, gap, new_src, dest)
            text = re.sub(r"^([ \t]+)symlink([ \t]+)(/system/etc/\S+)([ \t]+\S+)", _move_symlink_target,
                          text, flags=re.M)
        host = os.path.join(stage, "rc", rc)
        os.makedirs(os.path.dirname(host), exist_ok=True)
        with open(host, "w") as f:
            f.write(text)
        plan.add_parents(dest)
        plan.add(dest, _S_IFREG | 0o644, 0, 0, "file", host)
        for binary in moves:
            host = os.path.join(stage, "bin", binary)
            pull.append(("/system/bin/" + binary, host))
            plan.add_parents("/bin/" + binary)
            plan.add("/bin/" + binary, _S_IFREG | 0o755, 0, SHELL_GID, "file", host)
        if moves:
            log("%s: moving %d daemons to /vendor/bin: %s" % (rc, len(moves), " ".join(moves)))

    # 3b. The device fstab. A Halium 9 image keeps /fstab.<device> at its root,
    #     a Treble vendor keeps it in /vendor/etc, and that is where
    #     mount-android.sh looks for the partitions the vendor needs mounted
    #     (/persist and the modem's /firmware here). Without one it logs "no
    #     vendor fstab found" and exits, which also skips its APEX mounting.
    fstabs = []
    for name, mode, _, _ in old.ls("/"):
        if name.startswith("fstab.") and (mode & _S_IFMT) == _S_IFREG:
            host = os.path.join(stage, "fstab", name)
            pull.append(("/" + name, host))
            plan.add_parents("/etc/" + name)
            plan.add("/etc/" + name, _S_IFREG | 0o644, 0, 0, "file", host)
            fstabs.append(host)
            log("fstab: /%s -> /vendor/etc/%s" % (name, name))

    old.dump_many(pull)
    # The EGL libraries moved in 2a open each other and their helpers by absolute path:
    # libEGL_adreno200.so dlopen()s "/system/lib/egl/eglsubAndroid.so", libGLESv2_adreno200.so
    # "/system/lib/libsc-a2xx.so", and so on. In the GSI those paths do not exist; the driver then
    # fails to load half of itself and the compositor crashes in it (tenderloin, 7 Oct 2026). The
    # files are in this image now, and "/system/lib/" and "/vendor/lib/" have the same length, so
    # the paths are rewritten in place.
    for name in moved_egl:
        host = os.path.join(stage, "sysegl", name)
        with open(host, "rb") as f:
            blob = f.read()
        n = blob.count(b"/system/lib/")
        if n:
            with open(host, "wb") as f:
                f.write(blob.replace(b"/system/lib/", b"/vendor/lib/"))
            log("egl: %s: %d /system/lib/ paths now /vendor/lib/" % (name, n))
    for host in fstabs:
        text, renamed = adapt_fstab(open(host).read())
        with open(host, "w") as f:
            f.write(text)
        for old_dst, new_dst in renamed:
            log("fstab: %s -> %s" % (old_dst, new_dst))
            if old_dst in REAL_DIR_MOUNTS:
                plan.gsi_dirs.append(old_dst)
            else:
                plan.gsi_symlinks.append((old_dst, new_dst))
            if new_dst.startswith("/vendor/"):
                plan.add(new_dst[len("/vendor"):], _S_IFDIR | 0o755, 0, 0, "dir")
    # An Android 10/11 vendor's own fstab, already in /vendor/etc: its root mount points that the
    # GSI does not have move with vendor_mount_map, and get their directory in the vendor image.
    if mount_moves:
        for entry in plan.entries:
            if entry[4] == "file" and re.match(r"^/etc/fstab\.", entry[0]) and \
                    entry[5].startswith(os.path.join(stage, "vendor") + os.sep):
                with open(entry[5]) as f:
                    text = f.read()
                with open(entry[5], "w") as f:
                    f.write(remap_vendor_fstab(text, mount_moves))
                log("fstab: %s: %s" % (entry[0], ", ".join("%s -> %s" % m for m in sorted(mount_moves.items()))))
        for new_dst in sorted(set(mount_moves.values())):
            if new_dst.startswith("/vendor/") and new_dst[len("/vendor"):] not in plan.paths:
                plan.add(new_dst[len("/vendor"):], _S_IFDIR | 0o755, 0, 0, "dir")

    # 3c. The VINTF manifest: HALs that run as a service here are hwbinder, not
    #     passthrough (see adapt_vintf).
    if binderized_hals:
        for entry in plan.entries:
            if entry[0] == "/etc/vintf/manifest.xml" and entry[4] == "file":
                host = entry[5]
                with open(host) as f:
                    text, changed = adapt_vintf(f.read(), set(binderized_hals))
                with open(host, "w") as f:
                    f.write(text)
                log("vintf: now hwbinder: %s" % (" ".join(changed) or "(none matched)"))
                break
        else:
            log("vintf: the old image has no /vendor/etc/vintf/manifest.xml to adapt")

    # 4. Libraries: close over DT_NEEDED against vendor + VNDK + NDK, pulling
    #    anything else from the old /system/lib. Walk the roots, not the tree.
    llndk = set(gsi.cat(LLNDK_LIST).decode("utf-8", "replace").split())
    if not llndk:
        raise RuntimeError("the GSI has no %s" % LLNDK_LIST)
    # Per ELF class: a 64-bit library is satisfied only by a 64-bit one (/lib64), a 32-bit one
    # by /lib. A 32-bit build has no /lib64, and this is then the single set it always was.
    available = {d: set(vndk_names) | llndk for d in LIBDIRS}
    for path in plan.paths:
        if path.endswith(".so"):
            available["lib64" if "/lib64/" in path else "lib"].add(os.path.basename(path))
    # An Android 10+ /system/lib holds symlinks into /apex for the libraries the apexes
    # own (libicuuc, libandroidicu, libnativehelper -> com.android.art, libdl_android ->
    # com.android.runtime). The GSI mounts those apexes and its linker finds the real file
    # there. Pulling the link would dump an empty regular file into /vendor/lib, ahead of
    # the apex on the search path, and the loader then fails on "file size 0" (a compositor
    # gst-plugin-scanner hung on libandroidicu.so for it). So they are not candidates.
    old_lib = {d: set() for d in LIBDIRS}
    apex_provided = set()
    for libdir in libdirs:
        for n, mode, _, _ in old.ls("/system/" + libdir):
            if not n.endswith(".so"):
                continue
            if (mode & _S_IFMT) == _S_IFLNK and \
                    old.readlink("/system/%s/%s" % (libdir, n)).startswith("/apex/"):
                apex_provided.add(n)
                continue
            old_lib[libdir].add(n)
    if apex_provided:
        log("apex: not pulled from /system/lib, the GSI provides them from /apex: %s"
            % " ".join(sorted(apex_provided)))
    dl_pulled = set()
    # Every file is a root: the vendor's own bin/hw services link libraries too.
    # needed() returns [] for anything that is not an ELF.
    queue = [e[5] for e in plan.entries if e[4] == "file"]
    # A library named in extra_libs is pulled even when the platform "provides" it: the GSI's LLNDK list names
    # libft2.so, for one, and the vendor linker of the container still does not hand it to the vendor. Only
    # one the vendor image already holds is left alone.
    in_vendor = {os.path.basename(p) for p in plan.paths if p.endswith(".so")}
    for lib in extra_libs:
        for libdir in libdirs:
            if lib in old_lib[libdir] and lib not in in_vendor:
                queue.append((libdir, lib))
    pulled = []
    seen = set()
    # Libraries opened with dlopen() are not in DT_NEEDED, so the walk below cannot
    # see them (qseecomd dlopens librpmb.so and fails with "RPMB_INIT failed"
    # without it). Treat a library name that appears as a string in a vendor ELF and
    # exists in the old /system/lib, but is not otherwise provided, as needed.
    # A name that is only text costs some disk space; a missing one costs a daemon.
    dl_name = re.compile(rb"lib[A-Za-z0-9_+.-]{1,60}\.so\b")
    for e in plan.entries:
        if e[4] != "file" or not isinstance(e[5], str) or not os.path.isfile(e[5]):
            continue
        with open(e[5], "rb") as f:
            data = f.read()
        if data[:4] != b"\x7fELF":
            continue
        libdir = "lib64" if data[4:5] == b"\x02" else "lib"
        for m in set(dl_name.findall(data)):
            n = m.decode()
            if n in old_lib[libdir] and n not in available[libdir] and (libdir, n) not in dl_pulled:
                dl_pulled.add((libdir, n))
                queue.append((libdir, n))
    if dl_pulled:
        log("dlopen: libraries named inside vendor binaries, taken from /system/lib: %s"
            % " ".join("/system/%s/%s" % p for p in sorted(dl_pulled)))
    while queue:
        item = queue.pop()
        if isinstance(item, tuple):
            libdir, name = item
            host = os.path.join(stage, "syslib" if libdir == "lib" else "syslib64", name)
            pulled.append(("/system/%s/%s" % (libdir, name), host))
            plan.add_parents("/%s/%s" % (libdir, name))
            plan.add("/%s/%s" % (libdir, name), _S_IFREG | 0o644, 0, 0, "file", host)
            available[libdir].add(name)
            old.dump_many([(pulled[-1][0], host)])
            item = host
        if item in seen:
            continue
        seen.add(item)
        libdir = elf_libdir(item)
        for dep in needed(item):
            if dep in available[libdir]:
                continue
            if dep in old_lib[libdir]:
                queue.append((libdir, dep))
            else:
                plan.unresolved.setdefault(dep, set()).add(os.path.basename(item))

    _apply_shims(plan, stage, shim_lib, shim_targets, ld_shims, log)

    # 5. Config files the vendor code still opens under /system/etc. They have
    #    to land in the GSI at the same path: the string is baked into the binary.
    refs = set()
    for _, _, _, _, kind, payload in plan.entries:
        if kind == "file" and isinstance(payload, str) and os.path.isfile(payload):
            with open(payload, "rb") as f:
                refs.update(m.decode() for m in re.findall(rb"/system/etc/[A-Za-z0-9_./-]+", f.read()))
    for ref in sorted(refs) if gsi_autodetect else ():
        ref = ref.rstrip(".")
        if old.exists(ref) and not gsi.exists(ref):
            plan.gsi_files.append(ref)
    # Empty directories the vendor's fstab mounts onto that the GSI does not have (the container
    # mounts the partitions there and cannot create a directory on a read-only image).
    for d_ in gsi_dirs:
        if d_ not in plan.gsi_dirs:
            plan.gsi_dirs.append(d_)
    # Symlinks the caller wants in the GSI, as [(link, target)] (the target may be relative).
    for link, target in gsi_links:
        if (link, target) not in plan.gsi_symlinks:
            plan.gsi_symlinks.append((link, target))
    # Files the caller knows the vendor opens by a path that is not spelled /system/etc/... in any
    # binary (the NFC HAL opens "/etc/libnfc-nci.conf", and /etc is a link to /system/etc).
    for ref in gsi_files:
        if ref in plan.gsi_files:
            continue
        if not old.exists(ref):
            log("gsi file %s is not in the old image" % ref)
        elif gsi.exists(ref):
            log("gsi file %s is already in the GSI" % ref)
        else:
            plan.gsi_files.append(ref)

    # 6. The Treble metadata `m vendorimage` writes and a monolithic build does not.
    stage_prop = os.path.join(stage, "build.prop")
    props = old.cat("/system/vendor/build.prop").decode("utf-8", "replace")
    # A monolithic Halium 9 build keeps part of the device's tuning in /system/build.prop (the
    # device makefile's PRODUCT_PROPERTY_OVERRIDES): ro.qti.sensors.*, persist.radio.*,
    # ro.telephony.default_network, audio fluence settings and so on. The GSI's own build.prop does
    # not have them, so carry the ones whose names the caller allows over to the vendor build.prop.
    if system_prop_prefixes:
        have = set(re.findall(r"^([^#=\s]+)=", props, re.M))
        taken = []
        for line in old.cat("/system/build.prop").decode("utf-8", "replace").splitlines():
            m = re.match(r"^([^#=\s]+)=(.*)$", line)
            if not m or not m.group(2).strip() or m.group(1) in have:
                continue
            if m.group(1).startswith(tuple(system_prop_prefixes)):
                taken.append(line)
                have.add(m.group(1))
        if taken:
            props = props.rstrip("\n") + "\n# from the old /system/build.prop\n" + "\n".join(taken) + "\n"
            log("build.prop: %d properties from the old /system/build.prop" % len(taken))
    if vndk and not re.search(r"^ro\.vndk\.version=", props, re.M):
        props = props.rstrip("\n") + "\nro.vndk.version=%s\n" % vndk
    # The A16 EGL loader tries persist.graphics.egl, ro.hardware.egl, then
    # ro.board.platform, and opens libEGL_<value>.so. A Halium 9 build never needed
    # the first two, and ro.board.platform names the SoC (msm8974), not the driver
    # (libEGL_adreno.so), so surface-manager aborts with "couldn't find an OpenGL
    # ES implementation". Derive the value from the driver the vendor ships.
    if not re.search(r"^ro\.hardware\.egl=", props, re.M):
        # Either the split form (libEGL_<name>.so beside libGLESv1_CM_ and
        # libGLESv2_) or the single-file form ARM's Mali ships, libGLES_<name>.so,
        # which the Android loader also finds by the same property.
        egl_re = r"/lib(?:64)?/egl/lib(?:EGL|GLES)_(.+)\.so$"
        drivers = sorted(set(re.match(egl_re, p).group(1)
                             for p in plan.paths if re.match(egl_re, p)))
        if len(drivers) == 1:
            props = props.rstrip("\n") + "\nro.hardware.egl=%s\n" % drivers[0]
            log("build.prop: ro.hardware.egl=%s" % drivers[0])
        else:
            log("build.prop: not setting ro.hardware.egl, EGL drivers found: %s"
                % (" ".join(drivers) or "none"))
    with open(stage_prop, "w") as f:
        f.write(props)
    plan.entries = [e for e in plan.entries if e[0] != "/build.prop"]
    plan.paths.discard("/build.prop")
    plan.add("/build.prop", _S_IFREG | 0o600, 0, 0, "file", stage_prop)
    # Android 16's init reads this unconditionally. A vendor built without VNDK ships its own
    # (202404 for bullhead's LineageOS 21), and Plan.add keeps that one.
    if vndk:
        sep = os.path.join(stage, "plat_sepolicy_vers.txt")
        with open(sep, "w") as f:
            f.write("%s.0\n" % vndk)
        plan.add_parents("/etc/selinux/plat_sepolicy_vers.txt")
        plan.add("/etc/selinux/plat_sepolicy_vers.txt", _S_IFREG | 0o644, 0, 0, "file", sep)
    elif "/etc/selinux/plat_sepolicy_vers.txt" not in plan.paths:
        raise RuntimeError("the vendor has no /etc/selinux/plat_sepolicy_vers.txt and no VNDK version "
                           "was given to derive one from; Android 16's init cannot start without it")

    _write_image(plan, out_vendor, slack_mb)
    if own:
        tmp.cleanup()
    return plan


def _write_image(plan, out, slack_mb):
    size = 0
    for _, _, _, _, kind, payload in plan.entries:
        if kind == "file":
            size += os.path.getsize(payload) + 4096
        else:
            size += 4096
    mb = int(size * 1.15 / (1024 * 1024)) + slack_mb
    with open(out, "wb") as f:
        f.truncate(mb * 1024 * 1024)
    # The feature set of the old Halium 9 images, spelled out. A current mke2fs
    # defaults to 64bit, metadata_csum and orphan_file, and the 3.4 kernels these
    # devices run refuse to mount a filesystem carrying any of them.
    subprocess.run(["mke2fs", "-q", "-t", "ext4", "-b", "4096", "-I", "256", "-L", "vendor",
                    "-O", "none," + ",".join(MOUNTABLE_ON_3_4), "-F", out], check=True)
    cmds = []
    # parents first, and sort so a directory always precedes its children
    for path, mode, uid, gid, kind, payload in sorted(plan.entries, key=lambda e: e[0].count("/")):
        if kind == "dir":
            cmds.append("mkdir %s" % path)
        elif kind == "file":
            cmds.append("write %s %s" % (payload, path))
        else:
            cmds.append("symlink %s %s" % (path, payload))
        cmds.append("sif %s mode 0%o" % (path, mode))
        cmds.append("sif %s uid %d" % (path, uid))
        cmds.append("sif %s gid %d" % (path, gid))
    out_text = Image(out).batch(cmds)
    # -f echoes every command as "debugfs: <cmd>", which can contain any word.
    errors = [l for l in out_text.splitlines()
              if not l.startswith("debugfs:") and re.search(r"(?i)error|no space|not found|exist", l)]
    if errors:
        raise RuntimeError("debugfs reported problems writing %s:\n%s" % (out, "\n".join(errors[:15])))
    res = subprocess.run(["e2fsck", "-fn", out], capture_output=True)
    if res.returncode != 0:
        raise RuntimeError("e2fsck rejects the new vendor image:\n%s" % res.stdout.decode()[-1500:])
    check_features(out)


def check_features(path):
    """Fail if the image carries an ext4 feature a 3.4 kernel cannot mount."""
    head = subprocess.run(["dumpe2fs", "-h", path], capture_output=True).stdout.decode()
    m = re.search(r"^Filesystem features:\s*(.*)$", head, re.M)
    feats = set(m.group(1).split()) if m else set()
    extra = feats - set(MOUNTABLE_ON_3_4) - {"has_journal"}
    if extra:
        raise RuntimeError("%s has ext4 features a 3.4 kernel cannot mount: %s"
                           % (path, " ".join(sorted(extra))))


def free_inodes(path):
    head = subprocess.run(["dumpe2fs", "-h", path], capture_output=True).stdout.decode()
    m = re.search(r"^Free inodes:\s*(\d+)", head, re.M)
    return int(m.group(1)) if m else 0


def patch_gsi(gsi_system, files, old_system, workdir, symlinks=(), dirs=(), prune=(), log=print):
    """Make the GSI carry what the converted vendor still reaches by absolute path.

    The GSI image is packed with only a handful of spare inodes, so the symlinks
    (which the vendor cannot work without) go in first and the /system/etc files
    (which it only opens for optional features) take whatever is left.
    """
    gsi = Image(gsi_system)
    budget = free_inodes(gsi_system)
    cmds = []
    # Free inodes first: the GSI is packed with almost none to spare, and a few of its files are
    # documentation or sample data nothing here reads.
    for victim in prune:
        mode = gsi.stat_mode(victim)
        if mode is None:
            continue
        if (mode & _S_IFMT) != _S_IFREG:
            log("not pruning %s from the GSI: not a regular file" % victim)
            continue
        cmds.append("rm %s" % victim)
        budget += 1
    for d in dirs:
        if gsi.exists(d):
            # An older run left a symlink here; a bind mount needs a real directory.
            if gsi.stat_mode(d) is not None and (gsi.stat_mode(d) & _S_IFMT) == _S_IFLNK:
                cmds.append("rm %s" % d)
                cmds.append("mkdir %s" % d)
            else:
                log("%s already exists in the GSI, leaving it" % d)
        elif budget <= 0:
            raise RuntimeError("no free inode left in the GSI for the %s directory" % d)
        else:
            cmds.append("mkdir %s" % d)
            budget -= 1
    for link, target in symlinks:
        if gsi.exists(link):
            log("%s already exists in the GSI, leaving it" % link)
        elif budget <= 0:
            raise RuntimeError("no free inode left in the GSI for the %s symlink" % link)
        else:
            cmds.append("symlink %s %s" % (link, target))
            budget -= 1
    pull = []
    old = Image(raw_image(old_system, workdir)) if files else None
    for ref in files:
        host = os.path.join(workdir, "gsi_etc", ref.lstrip("/"))
        mode = old.stat_mode(ref)
        if mode is None or (mode & _S_IFMT) != _S_IFREG:
            log("not copying %s into the GSI: not a regular file" % ref)
            continue
        if budget <= 0:
            log("not copying %s into the GSI: no free inode left" % ref)
            continue
        pull.append((ref, host))
        parent = ref.rpartition("/")[0]
        while parent and not gsi.exists(parent):
            cmds.insert(0, "mkdir %s" % parent)
            budget -= 1
            parent = parent.rpartition("/")[0]
        cmds.append("write %s %s" % (host, ref))
        budget -= 1
    if old is not None:
        old.dump_many(pull)
    if not cmds:
        return
    out_text = gsi.batch(cmds)
    res = subprocess.run(["e2fsck", "-fy", gsi_system], capture_output=True)
    if res.returncode > 1:
        raise RuntimeError("e2fsck on the patched GSI failed:\n%s" % res.stdout.decode()[-1500:])
    errors = [l for l in out_text.splitlines()
              if not l.startswith("debugfs:") and re.search(r"(?i)could not|no space|not found|error", l)]
    if errors:
        raise RuntimeError("could not write everything into the GSI:\n%s" % "\n".join(errors[:10]))
