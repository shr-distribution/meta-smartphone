#!/bin/sh
# Map and mount the device's own Android partitions before the container starts.
#
# Ported from droidian/lxc-android's usr/sbin/mount-android.sh, adapted to
# LuneOS's layout (everything under /android rather than Droidian's / plus
# BIND_MOUNT_PATH) and rewritten in POSIX sh, since /bin/sh here is busybox and
# the original relies on bash [[ ]] and += .
#
# Why this runs from systemd rather than the initramfs, where
# initramfs-scripts-halium currently does a simpler version of the same job:
# udev is not running in the initramfs, so /dev/disk/by-* does not exist, and
# neither dmsetup nor parse-android-dynparts are available there. Dynamic
# partitions need all three. From Android 10 'super' is mandatory - and sargo
# gets retrofit dynamic partitions from Android 11 - so the initramfs approach
# cannot survive the move off an Android 9 vendor.
#
# The script is idempotent: anything already mounted (e.g. by the initramfs on
# a legacy layout) is left alone.

ANDROID_ROOT="${ANDROID_ROOT:-/android}"

log() { echo "mount-android: $*"; }

[ -d "$ANDROID_ROOT" ] || { log "$ANDROID_ROOT missing, nothing to do"; exit 0; }

# A/B slot. Android 12 moved androidboot.* from the kernel cmdline to bootconfig,
# so check both.
ab_slot_suffix=""
if [ -e /proc/bootconfig ]; then
    ab_slot_suffix=$(grep -o 'androidboot\.slot_suffix *= *".."' /proc/bootconfig 2>/dev/null | cut -d '"' -f2)
fi
if [ -z "$ab_slot_suffix" ]; then
    ab_slot_suffix=$(grep -o 'androidboot\.slot_suffix=..' /proc/cmdline 2>/dev/null | cut -d '=' -f2)
fi
[ -n "$ab_slot_suffix" ] && log "A/B slot suffix is $ab_slot_suffix"

# Resolve a partition label to a device node. On A/B systems not every partition
# is duplicated, so try the suffixed name first and fall back to the bare one.
find_partition_path() {
    _label=$1
    _path="/dev/$_label"
    for _dir in by-partlabel by-name by-label by-path by-uuid by-partuuid by-id; do
        if [ -e "/dev/disk/$_dir/$_label$ab_slot_suffix" ]; then
            _path="/dev/disk/$_dir/$_label$ab_slot_suffix"
            break
        elif [ -e "/dev/disk/$_dir/$_label" ]; then
            _path="/dev/disk/$_dir/$_label"
            break
        fi
    done
    echo "$_path"
}

# Drop flags the kernel will reject outside of an SELinux-enforcing Android.
parse_mount_flags() {
    echo "$1" | tr ',' '\n' | grep -vE '^(context|fscontext|defcontext|rootcontext|trusted)' \
        | paste -sd ',' - 2>/dev/null || \
    echo "$1" | tr ',' '\n' | grep -vE '^(context|fscontext|defcontext|rootcontext|trusted)' \
        | tr '\n' ',' | sed 's/,$//'
}

# Try each candidate device until one mounts and looks like the real thing.
try_mount_validated() {
    _target=$1; _marker=$2; shift 2
    mountpoint -q "$_target" 2>/dev/null && return 0
    [ -e "$_target$_marker" ] && return 0
    mkdir -p "$_target"
    for _dev in "$@"; do
        [ -e "$_dev" ] || continue
        log "trying $_dev for $_target"
        if mount -o ro "$_dev" "$_target" 2>/dev/null; then
            if [ -e "$_target$_marker" ]; then
                log "mounted $_dev at $_target"
                return 0
            fi
            log "$_dev is not a valid $_target (no $_marker)"
            umount "$_target" 2>/dev/null
        fi
    done
    return 1
}

# --- binderfs ---------------------------------------------------------------
# From Android 11 (kernel 5.x) the binder nodes come from binderfs rather than
# static /dev/binder* devices. Mount it on the host so the container config's
# optional bind of /dev/binderfs has something to bind. Older kernels have no
# CONFIG_ANDROID_BINDERFS and simply skip this.
if grep -qw binder /proc/filesystems 2>/dev/null; then
    if ! mountpoint -q /dev/binderfs 2>/dev/null; then
        mkdir -p /dev/binderfs
        if mount -t binder binder /dev/binderfs 2>/dev/null; then
            log "mounted binderfs at /dev/binderfs"
        else
            log "WARNING: binder filesystem available but mounting /dev/binderfs failed"
        fi
    fi
else
    log "no binderfs support in this kernel, using static binder nodes"
fi

# --- dynamic partitions -----------------------------------------------------
# From Android 10, system/vendor/product are logical partitions inside 'super',
# described by a custom header rather than LVM. Map them to /dev/mapper/dynpart-*.
super=$(find_partition_path super)
if [ -e "$super" ]; then
    if [ -e /dev/mapper/dynpart-vendor ] || [ -e "/dev/mapper/dynpart-vendor$ab_slot_suffix" ]; then
        log "super already mapped"
    elif command -v parse-android-dynparts >/dev/null 2>&1; then
        log "mapping super partition at $super"
        table=$(parse-android-dynparts "$super") && [ -n "$table" ] && \
            dmsetup create --concise "$table" || \
            log "WARNING: failed to map super; vendor will not be found"
    else
        log "WARNING: super present but parse-android-dynparts missing; cannot map dynamic partitions"
    fi
fi

# --- vendor -----------------------------------------------------------------
try_mount_validated "$ANDROID_ROOT/vendor" "/build.prop" \
    "/userdata/vendor.img" \
    "/var/lib/lxc/android/vendor.img" \
    "$(find_partition_path vendor)" \
    "/dev/mapper/dynpart-vendor$ab_slot_suffix" \
    "/dev/mapper/dynpart-vendor" \
    || log "no vendor partition found yet"

# Some devices describe vendor only in the devicetree fstab.
sys_vendor="/sys/firmware/devicetree/base/firmware/android/fstab/vendor"
if [ -e "$sys_vendor" ] && ! mountpoint -q "$ANDROID_ROOT/vendor" 2>/dev/null \
   && [ ! -e "$ANDROID_ROOT/vendor/build.prop" ]; then
    label=$(awk -F/ '{print $NF}' "$sys_vendor/dev" 2>/dev/null | tr -d '\0')
    path=$(find_partition_path "$label")
    if [ -e "$path" ]; then
        type=$(tr -d '\0' < "$sys_vendor/type")
        options=$(parse_mount_flags "$(tr -d '\0' < "$sys_vendor/mnt_flags")")
        log "mounting $path as $ANDROID_ROOT/vendor (from devicetree fstab)"
        mount "$path" "$ANDROID_ROOT/vendor" -t "$type" -o "$options"
    fi
fi

# --- vendor_dlkm and odm (Android 10/11+, usually logical) ------------------
# On the host, not under $ANDROID_ROOT, for the same reason as the APEXes:
# $ANDROID_ROOT is the container's rootfs and LXC drops anything mounted there
# before starting. Droidian binds both in from the host with optional entries
# and so do we now. Neither exists on an Android 9 device, and neither exists on
# sargo, so the entries stay optional and this is a no-op where they are absent.
try_mount_validated /vendor_dlkm "/etc/build.prop" \
    "/dev/mapper/dynpart-vendor_dlkm$ab_slot_suffix" \
    "/dev/mapper/dynpart-vendor_dlkm" \
    >/dev/null 2>&1

try_mount_validated /odm "/etc" \
    "/dev/mapper/dynpart-odm$ab_slot_suffix" \
    "/dev/mapper/dynpart-odm" \
    "$(find_partition_path odm)" \
    >/dev/null 2>&1

# --- vndbinder context manager ----------------------------------------------
# A stock Android 11 vendor ships AOSP's vndservicemanager, whose Access
# constructor does CHECK(selinux_status_open(true) >= 0). Halium's init never
# mounts selinuxfs inside the container, so that CHECK fails, the process
# aborts on SIGABRT, and init restarts it forever. Nothing ever becomes the
# context manager for /dev/vndbinder, so every vendor HAL that opens it blocks
# in ProcessState waiting for a context object that never arrives:
#
#     W ProcessState: Not able to get context object on /dev/vndbinder.
#     E ServiceManager: Waiting 1s on context object on /dev/vndbinder.
#
# The display composer is one of those. It never gets far enough to register
# android.hardware.graphics.composer@2.x::IComposer, so the compositor's
# getService() call spins forever, surface-manager times out, and the device
# has no UI at all. This is the whole reason a GSI on a stock Android 11 vendor
# comes up headless.
#
# Halium patches the SELinux checks out of Access.cpp, which vndservicemanager
# is built from, and lists it in PRODUCT_PACKAGES - but the module is
# vendor:true and a system-only GSI build has no vendor image, so the patched
# binary is built and then dropped. android-system-image ships it next to the
# rootfs image and we bind it over the vendor one here.
#
# It has to be exec'd from a /vendor/bin path. libbinder stamps a different
# Parcel header for the vendor and system copies of itself ("VNDR" against
# "SYST"), and which one a process gets is decided by the linker namespace,
# which the linker picks from the executable's path. Pointing the system
# servicemanager at /dev/vndbinder does make it the context manager, but every
# vendor client's ping is then rejected:
#
#     E Parcel: Expecting header 0x53595354 but found 0x564e4452.
#               Mixing copies of libbinder?
#
# so the bind mount, rather than a service override, is the point.
halium_vndservicemanager=/var/lib/lxc/android/vndservicemanager
vendor_vndservicemanager=$ANDROID_ROOT/vendor/bin/vndservicemanager
if [ -x "$halium_vndservicemanager" ] && [ -e "$vendor_vndservicemanager" ]; then
    log "using the Halium vndservicemanager instead of the vendor's"
    mount --bind "$halium_vndservicemanager" "$vendor_vndservicemanager"
fi

# --- persist ----------------------------------------------------------------
# NB: /mnt is rbind-mounted into the container straight from the host
# (lxc.mount.entry = /mnt mnt bind rbind), so persist goes to the host path, not
# under $ANDROID_ROOT. $ANDROID_ROOT is a read-only loop mount of the GSI anyway,
# so we could not create the mount point there.
sys_persist="/sys/firmware/devicetree/base/firmware/android/fstab/persist"
if [ -e "$sys_persist" ] && ! mountpoint -q /mnt/vendor/persist 2>/dev/null; then
    label=$(awk -F/ '{print $NF}' "$sys_persist/dev" 2>/dev/null | tr -d '\0')
    path=$(find_partition_path "$label")
    if [ -e "$path" ]; then
        type=$(tr -d '\0' < "$sys_persist/type")
        options=$(parse_mount_flags "$(tr -d '\0' < "$sys_persist/mnt_flags")")
        mkdir -p /mnt/vendor/persist
        log "mounting $path as /mnt/vendor/persist"
        mount "$path" /mnt/vendor/persist -t "$type" -o "$options"
    fi
fi

# --- everything else the vendor's own fstab asks for ------------------------
# The vendor is the source of truth for firmware_mnt, dsp, metadata and friends;
# this is the whole reason a device-agnostic GSI can work at all.
set -- "$ANDROID_ROOT"/vendor/etc/fstab*
if [ ! -e "$1" ]; then
    log "no vendor fstab found, skipping extra mounts"
    exit 0
fi
fstab=$1
log "reading $fstab for additional mount points"

# shellcheck disable=SC2002
cat "$fstab" | while read -r src dst fstype flags _rest; do
    case "$src" in
        '#endhalium'*) break ;;
        '#'*|'') continue ;;
    esac
    [ -n "$src" ] && [ -n "$dst" ] && [ -n "$fstype" ] && [ -n "$flags" ] || continue

    # Handled above, or owned by the host / the container's own init.
    case "$dst" in
        /system|/data|/|auto|/vendor|none|/misc|/system_ext|/product|/odm) continue ;;
    esac
    case "$fstype" in
        emmc|swap|mtd) continue ;;
    esac

    label=${src##*/}
    [ -n "$label" ] || continue
    path=$(find_partition_path "$label")
    [ -e "$path" ] || continue

    # Same split as above: /mnt comes from the host, everything else is bind
    # mounted into the container by pre-start.sh, which strips the /android prefix.
    case "$dst" in
        /mnt/*) target="$dst" ;;
        *)      target="$ANDROID_ROOT$dst" ;;
    esac
    mountpoint -q "$target" 2>/dev/null && continue
    if [ ! -d "$target" ] && ! mkdir -p "$target" 2>/dev/null; then
        log "skipping $dst: cannot create $target (read-only GSI and no such directory)"
        continue
    fi
    log "mounting $path as $target"
    mount "$path" "$target" -t "$fstype" -o "$(parse_mount_flags "$flags")" 2>/dev/null \
        || log "WARNING: failed to mount $path at $target"
done

# --- APEX (Android 10+) -----------------------------------------------------
# The GSI's linker needs /apex/com.android.runtime for bionic, and linkerconfig
# aborts if the VNDK apex is missing or mounted under the wrong name, so these
# have to be in place before the container's init runs.
#
# Only a minimal set is mounted, following UBports: runtime, art, i18n and the
# VNDK. Mounting everything is unnecessary and pulls in modules that expect a
# running apexd.
#
# They go on the host's own /apex, which the container config binds back in
# with rbind. Mounting them under $ANDROID_ROOT instead does not survive: that
# is the rootfs LXC sets up, and it drops them again a second later.
#
# Android 9 has no APEX at all, so key off the image actually carrying them
# rather than off a directory that now always exists on the host.
if [ -d "$ANDROID_ROOT/system/apex" ] && command -v mount-apexes.py >/dev/null 2>&1; then
    log "mounting APEX modules"
    mount-apexes.py "com.android.runtime" "com.android.art" "com.android.i18n" "com.android.vndk.*" \
        || log "WARNING: APEX mounting reported errors"
fi

exit 0
