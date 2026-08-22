#!/bin/bash
# Exercise mount-android.sh's dynamic-partition path against a synthetic
# retrofit layout on loop devices.
#
# Why this exists: sargo gets *retrofit* dynamic partitions when it moves to
# Android 11 - no "super" partition ever appears, the LP metadata lives on
# "system", and the logical extents span system AND vendor. Both
# parse-android-dynparts and mount-android.sh were changed for that case, and
# neither change has ever run on hardware. Flashing stock Android 11 destroys
# the working install, so the bugs are much cheaper to find here.
#
# What is real and what is faked:
#
#   real    the shipped mount-android.sh, unmodified
#   real    parse-android-dynparts with both LuneOS patches, built natively
#   real    device-mapper, loop devices, ext4, the mounts themselves
#   faked   the block devices (sparse files), the LP metadata (make-lp-metadata.py),
#           /dev/disk/by-partlabel, and /proc/cmdline's slot suffix
#
# Everything except the device-mapper nodes lives in a private mount namespace,
# so /dev/disk and /proc/cmdline on the host are untouched. The dm nodes are
# global and are removed on exit.
#
# Usage: sudo ./loopback-retrofit-test.sh [--keep]
#          --keep  leave the work directory, loop devices and dm nodes in place
set -u

HERE=$(cd "$(dirname "$0")" && pwd)
LAYER=$(cd "$HERE/../../../.." && pwd)              # meta-android
MOUNT_ANDROID="$LAYER/recipes-core/android-system/android-system/mount-android.sh"
PATCH_DIR=$(cd "$HERE/.." && pwd)

SLOT="_a"
SYS_MB=256; SYS_LOGICAL_MB=64
VEN_MB=128; VEN_LOGICAL_MB=32
MODEM_MB=16
EXTENT_START_SECTOR=2048                            # 1MiB, past the metadata

pass=0; fail=0
ok()   { printf '  \033[32mPASS\033[0m %s\n' "$1"; pass=$((pass + 1)); }
bad()  { printf '  \033[31mFAIL\033[0m %s\n' "$1"; fail=$((fail + 1)); }
note() { printf '  ---- %s\n' "$1"; }
head_() { printf '\n\033[1m%s\033[0m\n' "$1"; }

check() { # check <description> <expected> <actual>
    if [ "$2" = "$3" ]; then ok "$1"; else bad "$1"; note "expected: $2"; note "actual:   $3"; fi
}

# --------------------------------------------------------------------------
# inner: runs inside the private mount namespace
# --------------------------------------------------------------------------
if [ "${1:-}" = "--inner" ]; then
    WORK=$2; LOOP_SYS=$3; LOOP_VEN=$4; LOOP_MODEM=$5; TOOLDIR=$6

    mount --make-rprivate / 2>/dev/null

    # A fake by-partlabel directory. The names carry the A/B suffix exactly as
    # they do on a retrofit device, because that is what the sibling lookup in
    # parse-android-dynparts keys off: given .../system_a it derives .../vendor_a.
    mkdir -p /dev/disk
    mount -t tmpfs tmpfs /dev/disk
    mkdir -p /dev/disk/by-partlabel
    ln -s "$LOOP_SYS"   /dev/disk/by-partlabel/system$SLOT
    ln -s "$LOOP_VEN"   /dev/disk/by-partlabel/vendor$SLOT
    ln -s "$LOOP_MODEM" /dev/disk/by-partlabel/modem$SLOT

    # A/B detection reads /proc/bootconfig then /proc/cmdline; bind a fake one.
    printf 'androidboot.slot_suffix=%s\n' "$SLOT" > "$WORK/cmdline"
    mount --bind "$WORK/cmdline" /proc/cmdline

    mkdir -p "$WORK/android"

    head_ "run 1: cold"
    ANDROID_ROOT="$WORK/android" PATH="$TOOLDIR:$PATH" \
        sh "$MOUNT_ANDROID" > "$WORK/run1.log" 2>&1
    rc1=$?
    sed 's/^/      /' "$WORK/run1.log"
    check "exits 0" "0" "$rc1"

    check "slot suffix detected" \
        "1" "$(grep -c 'A/B slot suffix is _a' "$WORK/run1.log")"
    check "mapped from the system partition, not a 'super'" \
        "1" "$(grep -c 'mapped dynamic partitions from /dev/disk/by-partlabel/system_a' "$WORK/run1.log")"

    head_ "device-mapper tables"
    sys_tbl=$(dmsetup table dynpart-system$SLOT 2>/dev/null)
    ven_tbl=$(dmsetup table dynpart-vendor$SLOT 2>/dev/null)
    note "dynpart-system$SLOT: ${sys_tbl:-<missing>}"
    note "dynpart-vendor$SLOT: ${ven_tbl:-<missing>}"

    # dmsetup normally prints the target as major:minor, but has printed a path
    # in the past; resolve whatever it prints back to major:minor so the
    # assertion does not depend on that.
    # stat prints the major:minor of a device node in hex; dmsetup prints it in
    # decimal. Convert with bash printf, which takes the 0x prefix - awk does
    # not, and silently yields 0 for "0x16" without --non-decimal-data.
    devid() {
        _mt=$(stat -c '%t:%T' "$1")
        printf '%d:%d' "0x${_mt%:*}" "0x${_mt#*:}"
    }
    target_devid() {
        _t=$(echo "$1" | awk '{print $4}')
        case "$_t" in /*) devid "$_t" ;; *) echo "$_t" ;; esac
    }
    sys_major_minor=$(devid "$LOOP_SYS")
    ven_major_minor=$(devid "$LOOP_VEN")

    check "system_a size in sectors" \
        "$((SYS_LOGICAL_MB * 1024 * 2))" "$(echo "$sys_tbl" | awk '{print $2}')"
    check "vendor_a size in sectors" \
        "$((VEN_LOGICAL_MB * 1024 * 2))" "$(echo "$ven_tbl" | awk '{print $2}')"
    check "system_a start sector" "$EXTENT_START_SECTOR" "$(echo "$sys_tbl" | awk '{print $5}')"
    check "vendor_a start sector" "$EXTENT_START_SECTOR" "$(echo "$ven_tbl" | awk '{print $5}')"

    # The retrofit assertion. Before the patch every linear target pointed at
    # argv[1], so vendor_a would have resolved to the *system* device and the
    # mount would have produced garbage rather than an error.
    check "system_a targets the system device" "$sys_major_minor" "$(target_devid "$sys_tbl")"
    check "vendor_a targets the VENDOR device (retrofit)" \
        "$ven_major_minor" "$(target_devid "$ven_tbl")"

    head_ "mounts"
    if mountpoint -q "$WORK/android/vendor"; then
        ok "vendor mounted"
        check "vendor came from the mapper node" \
            "/dev/mapper/dynpart-vendor$SLOT" \
            "$(findmnt -no SOURCE "$WORK/android/vendor")"
        check "vendor content is the vendor filesystem" \
            "vendor-marker" "$(cat "$WORK/android/vendor/marker" 2>/dev/null)"
        if [ -e "$WORK/android/vendor/build.prop" ]; then
            ok "build.prop validated the mount"
        else
            bad "build.prop validated the mount"
        fi
    else
        bad "vendor mounted"
    fi

    # The vendor's own fstab is the source of truth for the extra mounts; this
    # is the loop that makes a device-agnostic GSI possible at all.
    if mountpoint -q "$WORK/android/vendor/firmware_mnt"; then
        ok "extra mount from the vendor fstab (firmware_mnt)"
        check "firmware_mnt content" \
            "modem-marker" "$(cat "$WORK/android/vendor/firmware_mnt/marker" 2>/dev/null)"
    else
        bad "extra mount from the vendor fstab (firmware_mnt)"
    fi

    head_ "run 2: idempotency"
    ANDROID_ROOT="$WORK/android" PATH="$TOOLDIR:$PATH" \
        sh "$MOUNT_ANDROID" > "$WORK/run2.log" 2>&1
    rc2=$?
    sed 's/^/      /' "$WORK/run2.log"
    check "exits 0" "0" "$rc2"
    check "notices the mapping already exists" \
        "1" "$(grep -c 'dynamic partitions already mapped' "$WORK/run2.log")"
    check "does not remount vendor" "0" "$(grep -c 'trying .* for .*/vendor$' "$WORK/run2.log")"
    check "vendor still mounted once" \
        "1" "$(findmnt -nl "$WORK/android/vendor" | wc -l)"

    head_ "run 3: a device with no LP metadata at all"
    # The comment in mount-android.sh claims this is self-detecting: on a
    # pre-dynamic device the metadata devices hold an ordinary filesystem,
    # parse-android-dynparts finds nothing, and the script moves on. Prove it,
    # because that is the path every Android 9 device takes today.
    findmnt -nlo TARGET | grep "^$WORK/android" | sort -r | while read -r m; do
        umount "$m" 2>/dev/null
    done
    dmsetup remove "dynpart-system$SLOT" "dynpart-vendor$SLOT" 2>/dev/null
    rm -f /dev/disk/by-partlabel/system$SLOT
    ln -s "$LOOP_MODEM" /dev/disk/by-partlabel/system$SLOT   # plain ext4, no metadata
    ANDROID_ROOT="$WORK/android" PATH="$TOOLDIR:$PATH" \
        sh "$MOUNT_ANDROID" > "$WORK/run3.log" 2>&1
    rc3=$?
    sed 's/^/      /' "$WORK/run3.log"
    check "exits 0 on a non-dynamic device" "0" "$rc3"
    check "maps nothing" "0" "$(grep -c 'mapped dynamic partitions' "$WORK/run3.log")"
    check "creates no dm nodes" "0" "$(dmsetup ls 2>/dev/null | grep -c '^dynpart-')"

    printf '\n\033[1m%d passed, %d failed\033[0m\n' "$pass" "$fail"
    [ "$fail" -eq 0 ]
    exit $?
fi

# --------------------------------------------------------------------------
# outer: setup and teardown
# --------------------------------------------------------------------------
KEEP=0
[ "${1:-}" = "--keep" ] && KEEP=1

[ "$(id -u)" = "0" ] || { echo "must run as root (losetup, dmsetup, mount)"; exit 1; }
[ -r "$MOUNT_ANDROID" ] || { echo "cannot find mount-android.sh at $MOUNT_ANDROID"; exit 1; }
for t in losetup dmsetup mkfs.ext4 unshare cmake findmnt; do
    command -v "$t" >/dev/null || { echo "missing required tool: $t"; exit 1; }
done

WORK=$(mktemp -d /var/tmp/mount-android-test.XXXXXX)
LOOP_SYS=""; LOOP_VEN=""; LOOP_MODEM=""

cleanup() {
    [ "$KEEP" = "1" ] && { echo "kept: $WORK  loops: $LOOP_SYS $LOOP_VEN $LOOP_MODEM"; return; }
    for n in dynpart-system$SLOT dynpart-vendor$SLOT; do
        dmsetup remove "$n" 2>/dev/null
    done
    for l in $LOOP_SYS $LOOP_VEN $LOOP_MODEM; do losetup -d "$l" 2>/dev/null; done
    rm -rf "$WORK"
}
trap cleanup EXIT

head_ "building parse-android-dynparts with both LuneOS patches"
SRC="$WORK/src"
git clone -q "$LAYER/../../downloads/git2/github.com.droidian.parse-android-dynparts.git" "$SRC" 2>/dev/null \
    || git clone -q https://github.com/droidian/parse-android-dynparts.git -b droidian "$SRC"
( cd "$SRC" && git checkout -q b776ff276f20b91c8df4e2c84cf314ba7b7a3fb5 ) 2>/dev/null
for p in "$PATCH_DIR"/000*.patch; do
    ( cd "$SRC" && patch -p1 -s -i "$p" ) || { echo "patch failed: $p"; exit 1; }
    note "applied $(basename "$p")"
done
cmake -S "$SRC" -B "$SRC/build" -DCMAKE_BUILD_TYPE=Release >/dev/null 2>&1
cmake --build "$SRC/build" -j"$(nproc)" >/dev/null 2>&1
TOOLDIR="$SRC/build"
[ -x "$TOOLDIR/parse-android-dynparts" ] || { echo "build failed"; exit 1; }
ok "built $TOOLDIR/parse-android-dynparts"

head_ "creating the backing devices"
truncate -s "${SYS_MB}M"   "$WORK/system.img"
truncate -s "${VEN_MB}M"   "$WORK/vendor.img"
truncate -s "${MODEM_MB}M" "$WORK/modem.img"

python3 "$HERE/make-lp-metadata.py" retrofit-small "$WORK/lp.img" >/dev/null
dd if="$WORK/lp.img" of="$WORK/system.img" conv=notrunc status=none
ok "LP metadata written to the head of system.img ($(stat -c%s "$WORK/lp.img") bytes)"

# Lay a filesystem inside each logical extent, at the offset the metadata says
# it lives at, so the mapper nodes have something real behind them.
mkfs_at() { # mkfs_at <image> <offset bytes> <size MiB> <marker> [extra dir]
    _l=$(losetup --show -f -o "$2" --sizelimit "$(( $3 * 1024 * 1024 ))" "$1")
    mkfs.ext4 -q -F "$_l"
    _m=$(mktemp -d); mount "$_l" "$_m"
    echo "$4" > "$_m/marker"
    [ -n "${5:-}" ] && mkdir -p "$_m/$5"
    printf 'ro.fake.marker=%s\n' "$4" > "$_m/build.prop"
    _extra_cb "$_m"
    umount "$_m"; rmdir "$_m"; losetup -d "$_l"
}
_extra_cb() { :; }

OFF=$((EXTENT_START_SECTOR * 512))
mkfs_at "$WORK/system.img" "$OFF" "$SYS_LOGICAL_MB" "system-marker"

# The vendor filesystem carries the fstab the script reads for extra mounts,
# and the firmware_mnt directory it needs as a mount point (the vendor is
# mounted read-only, so the directory has to pre-exist - exactly as on device).
_extra_cb() {
    mkdir -p "$1/etc"
    cat > "$1/etc/fstab.fake" <<'FSTAB'
# label                                     mountpoint            type  flags        fsmgr
/dev/block/bootdevice/by-name/modem         /vendor/firmware_mnt  ext4  ro,barrier=1 wait
/dev/block/bootdevice/by-name/system        /system               ext4  ro           wait
/dev/block/bootdevice/by-name/nonexistent   /vendor/nowhere       ext4  ro           wait
FSTAB
}
mkfs_at "$WORK/vendor.img" "$OFF" "$VEN_LOGICAL_MB" "vendor-marker" "firmware_mnt"
_extra_cb() { :; }

# modem is a plain partition with no LP metadata - it doubles as the negative
# case in run 3.
_l=$(losetup --show -f "$WORK/modem.img"); mkfs.ext4 -q -F "$_l"
_m=$(mktemp -d); mount "$_l" "$_m"; echo "modem-marker" > "$_m/marker"; umount "$_m"; rmdir "$_m"
losetup -d "$_l"
ok "filesystems laid at offset $OFF in system.img and vendor.img"

LOOP_SYS=$(losetup --show -f "$WORK/system.img")
LOOP_VEN=$(losetup --show -f "$WORK/vendor.img")
LOOP_MODEM=$(losetup --show -f "$WORK/modem.img")
note "system=$LOOP_SYS vendor=$LOOP_VEN modem=$LOOP_MODEM"

unshare -m --propagation private \
    bash "$0" --inner "$WORK" "$LOOP_SYS" "$LOOP_VEN" "$LOOP_MODEM" "$TOOLDIR"
exit $?
