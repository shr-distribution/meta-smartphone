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

# --- room in /dev for the container's property area --------------------------
# The container's init builds one 128K prop_area per SELinux property context
# under /dev/__properties__, which the LXC config bind-mounts from the host, so
# the files land on the host's /dev. systemd caps that at 4M (TMPFS_LIMITS_DEV)
# and an Android 16 GSI has ~557 contexts: the tmpfs runs out of pages part-way
# through PropertyInit and the mmap write takes SIGBUS. init is built with
# REBOOT_BOOTLOADER_ON_PANIC, so its handler turns that into reboot(), which the
# pid namespace delivers as SIGHUP - LXC reports only "ended on signal
# Hangup(1)" and nothing is logged, because this happens before the container
# ever gets a working /dev/kmsg. Android 11 fitted in 4M; 16 does not.
dev_kb=$(df -k /dev 2>/dev/null | awk 'NR==2 {print $2}')
if [ -n "$dev_kb" ] && [ "$dev_kb" -lt 131072 ]; then
    if mount -o remount,size=128m /dev 2>/dev/null; then
        log "grew /dev from ${dev_kb}K to 128M for the container property area"
    else
        log "WARNING: could not grow /dev (${dev_kb}K); container init may take SIGBUS in PropertyInit"
    fi
fi

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
    # The container's init symlinks its /dev/binder* into the shared binderfs
    # instance, but host-side hybris (composer, servicemanager clients) opens
    # the plain /dev/hwbinder path. Give the host the same symlinks - without
    # them every hybris HIDL call fails with "failed to get hwcomposer service"
    # while the vendor service is running fine inside the container.
    for n in binder hwbinder vndbinder; do
        [ -e /dev/$n ] || ln -sf /dev/binderfs/$n /dev/$n
    done
else
    log "no binderfs support in this kernel, using static binder nodes"
fi

# --- boot_id-suffixed ashmem node -------------------------------------------
# Android 12 onwards, libcutils does not open /dev/ashmem. It reads
# /proc/sys/kernel/random/boot_id and opens /dev/ashmem<boot_id>, a node the
# container's own init creates inside the container's /dev. Hybris processes
# run on the host, where only the plain /dev/ashmem from our udev rules exists,
# so every ashmem_create_region() there fails:
#
#   E ashmem : Unable to stat ashmem device: No such file or directory
#   E FMQ    : mmap failed: 9
#
# libfmq allocates its ring buffer that way, so the HIDL composer's command
# queue is never created and the compositor sends no display commands at all -
# it starts, composes, reports surfaces and renders nothing, leaving the boot
# splash on screen while the rest of the system runs normally.
#
# A symlink is enough: the check stats the path and compares st_rdev, which
# resolves to the same device. /dev is devtmpfs, so this does not survive a
# reboot and cannot go stale against a new boot_id.
ashmem_boot_id=$(cat /proc/sys/kernel/random/boot_id 2>/dev/null)
if [ -n "$ashmem_boot_id" ] && [ -e /dev/ashmem ]; then
    if ln -sfn /dev/ashmem "/dev/ashmem$ashmem_boot_id" 2>/dev/null; then
        log "created /dev/ashmem$ashmem_boot_id for host-side libcutils"
    else
        log "WARNING: could not create /dev/ashmem$ashmem_boot_id; hybris FMQ will fail"
    fi
else
    log "WARNING: no boot_id or no /dev/ashmem; hybris FMQ will fail"
fi

# --- dynamic partitions -----------------------------------------------------
# From Android 10, system/vendor/product are logical partitions inside 'super',
# described by a custom header rather than LVM. Map them to /dev/mapper/dynpart-*.
# Two layouts to cope with. Devices that launched on Android 10+ have a single
# dedicated "super" partition. Devices that launched earlier and were upgraded
# use *retrofit* dynamic partitions, where the logical partitions are spread
# across the pre-existing physical ones and there is no partition called super
# at all - the Pixel 3a is one of these:
#
#   BOARD_SUPER_PARTITION_METADATA_DEVICE := system
#   BOARD_SUPER_PARTITION_BLOCK_DEVICES   := system vendor
#
# so also try the usual metadata devices. On a pre-dynamic device those hold an
# ordinary filesystem, parse-android-dynparts finds no LP metadata and fails,
# and we simply move on - which makes this self-detecting rather than something
# that has to be configured per device.
if [ -e /dev/mapper/dynpart-vendor ] || [ -e "/dev/mapper/dynpart-vendor$ab_slot_suffix" ]; then
    log "dynamic partitions already mapped"
elif ! command -v parse-android-dynparts >/dev/null 2>&1; then
    log "parse-android-dynparts missing; cannot map dynamic partitions"
else
    for meta in super "system$ab_slot_suffix" system; do
        metadev=$(find_partition_path "$meta")
        [ -e "$metadev" ] || continue
        table=$(parse-android-dynparts "$metadev" 2>/dev/null) || continue
        [ -n "$table" ] || continue
        if dmsetup create --concise "$table"; then
            # The /dev/mapper/dynpart-* nodes are created asynchronously by
            # udev; on mindphone the vendor mount below raced ahead of them
            # and the container failed its first start every boot. mknodes
            # creates them synchronously, settle covers the by-label links.
            dmsetup mknodes
            udevadm settle --timeout=10 2>/dev/null
            log "mapped dynamic partitions from $metadev"
        else
            log "WARNING: found LP metadata on $metadev but dmsetup failed"
        fi
        break
    done
fi

# --- vendor -----------------------------------------------------------------
# Order matters: whenever a logical vendor has been mapped, it is the right one.
#
# On a retrofit device the physical vendor partition still mounts, because a
# filesystem superblock survives at offset zero from before the conversion. On
# sargo that mount is empty, so the build.prop check below would reject it and
# fall through to the mapper node anyway - but only by luck. A device where
# more of the old filesystem survived would pass the check and silently run the
# previous Android release's vendor against a newer GSI. Ask for the mapper
# node first rather than depend on the validation catching it.
try_mount_validated "$ANDROID_ROOT/vendor" "/build.prop" \
    "/userdata/vendor.img" \
    "/var/lib/lxc/android/vendor.img" \
    "/dev/mapper/dynpart-vendor$ab_slot_suffix" \
    "/dev/mapper/dynpart-vendor" \
    "$(find_partition_path vendor)" \
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

# --- firmware loader timeout -------------------------------------------------
# The kernel cannot find vendor firmware on its own: it lives in the container's
# /vendor/firmware, which is not on any path the direct loader searches, so
# every request falls through to the ueventd user helper. That works - ueventd
# answers the real ones in 1-4 ms - but a request the vendor genuinely has no
# file for blocks for the full firmware_class timeout, which defaults to 60
# seconds. The modem asks for exactly one of those:
#
#   [23.260] ueventd: firmware: loading 'msadp' ...
#   [83.261] pil-q6v5-mss: Debug policy not present - msadp. Continue.
#
# msadp is an optional debug-policy blob and the kernel says so itself, but the
# modem still takes 60 s longer to boot than it needs to, and everything that
# waits on the modem - the WLAN firmware among them - waits with it.
#
# Ten seconds is still three orders of magnitude more than a real load needs.
if [ -w /sys/class/firmware/timeout ]; then
    echo 10 > /sys/class/firmware/timeout
fi

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
# Prefer the SoC's real fstab (fstab.<ro.hardware>) over decorative ones like
# fstab.enableswap that sort earlier under a plain glob. Getting this wrong on
# mt6739 skipped the modem's nvcfg/protect1/protect2 mounts, and the modem came
# up "exception" with no RIL.
fstab=
hw=$(getprop ro.hardware 2>/dev/null)
[ -n "$hw" ] && [ -e "$ANDROID_ROOT/vendor/etc/fstab.$hw" ] && fstab="$ANDROID_ROOT/vendor/etc/fstab.$hw"
if [ -z "$fstab" ]; then
    for f in "$ANDROID_ROOT"/vendor/etc/fstab*; do
        case "$f" in *.enableswap) continue ;; esac
        [ -e "$f" ] && { fstab="$f"; break; }
    done
fi
if [ -z "$fstab" ]; then
    log "no vendor fstab found, skipping extra mounts"
    exit 0
fi
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

# --- vendor_dlkm kernel modules ---------------------------------------------
# A vendor ships its kernel modules in two sets, and only one of them is the
# initramfs's job:
#
#   vendor_boot ramdisk   loaded by the initramfs, early, to reach storage
#   vendor_dlkm           loaded by Android's init, later, from this partition
#
# Halium curtails the container's init long before it reaches the second, so on
# a stock Halium boot the vendor_dlkm set is simply never loaded. On the MP01
# that is 164 modules - more than the 159 the initramfs loads - and the
# consequences do not look like "missing modules" at all:
#
#   - nvmem-mt635x-efuse.ko is here, and mt635x-auxadc (which IS in the early
#     set) needs it. Without it the auxadc probe returns -517 forever, the fuel
#     gauge never comes up, and healthd reports "battery l=-1". lk then paints a
#     battery with a question mark, decides the device is in charger mode, and
#     reboots in a loop. Days can be lost reading that as a flat battery.
#   - the touchscreen drivers (gt9886, gt9896s, focaltech_touch) are here too,
#     so the panel has no touch at all and luneos-device-config leaves
#     evdevtouch pointed at its PLACEHOLDER.
#
# So load them here, once vendor_dlkm is mounted and before the container's init
# starts. modules.load is a LIST, not a dependency order - the same trap as in
# the initramfs - so make repeated passes and let each one resolve what the
# previous pass made loadable, rather than trusting the file's order.
load_vendor_dlkm_modules() {
    # The partition turns up under more than one root depending on which of the
    # mounts above claimed it first - try_mount_validated puts it at
    # /vendor_dlkm, while the devicetree-fstab path mounts it under
    # $ANDROID_ROOT. Guarding on one of them silently skips the whole function,
    # which is exactly what happened the first time this was written.
    _d=""
    for _cand in /vendor_dlkm/lib/modules \
                 "$ANDROID_ROOT"/vendor_dlkm/lib/modules \
                 /android/vendor_dlkm/lib/modules; do
        if [ -f "$_cand/modules.load" ]; then _d="$_cand"; break; fi
    done
    [ -n "$_d" ] || { log "vendor_dlkm: no modules.load found, skipping"; return 0; }
    log "vendor_dlkm: loading modules from $_d"

    # Which of them to load.
    #
    # Loading the whole set is what Android does, but Android also has its full
    # init, ueventd and HALs around them. Here it means pulling in camera
    # actuators, flashlight drivers and GPIO expanders that nothing on a LuneOS
    # boot will ever use, on a device that is already fragile - and a single bad
    # probe costs a whole debug cycle to identify. So default to the modules
    # that are actually load-bearing and let the rest stay unloaded:
    #
    #   nvmem-mt635x-efuse   the fuel gauge's efuse provider. Without it
    #                        mt635x-auxadc returns -517 forever and healthd
    #                        reports "battery l=-1", which makes lk paint a
    #                        battery with a question mark and reboot in a loop.
    #   gt9886 / gt9896s /   the touchscreen. Which one probes depends on the
    #   focaltech_touch      panel; loading all three is harmless, the wrong
    #                        ones simply do not match.
    #
    # Default is the narrow list, and that is a measured decision, not caution.
    #
    # Loading the whole set is what Android does, but Android has its full init,
    # ueventd and HAL set around these drivers. Loading all 164 on a LuneOS boot
    # was tried on the MP01 (15 Sep 2026) and turned a device that reset every
    # ~104s into a hard bootloop: pairs of 2-3 second boots about 25s apart,
    # with the journal ending mid-load every time. The set includes camera
    # actuators, flashlight and GPIO-expander drivers that nothing here will
    # ever open, and one bad probe is enough.
    #
    # So load what is actually load-bearing:
    #
    #   nvmem-mt635x-efuse   the fuel gauge's efuse provider. Without it
    #                        mt635x-auxadc returns -517 forever and healthd
    #                        reports "battery l=-1", which makes lk paint a
    #                        battery with a question mark, boot into charger
    #                        mode, and reset in a loop. Confirmed to probe
    #                        cleanly ("mt635x_efuse_probe done") once loaded.
    #   gt9886 / gt9896s /   the touchscreen. Which one probes depends on the
    #   focaltech_touch      panel; loading all three is harmless, the wrong
    #                        ones simply do not match.
    #
    # VENDOR_DLKM_MODULES=all restores the full set - worth trying when a new
    # subsystem turns out to be missing, but expect to bisect it.
    #   mali_kbase_mt6789    the GPU. Without it the Mali userspace fails with
    #                        "Failed creating base context during opening of
    #                        kernel driver / Kernel module may not have been
    #                        loaded", and the compositor segfaults on every
    #                        start. It pulls in 14 dependencies (ged, gpueb,
    #                        gpufreq, qos, mbox...), which is why the dependency
    #                        resolution below exists rather than a hand list.
    #   fhctl + mcupm         gpufreq's DT node lists "fhctl-supply", and
#                         MediaTek uses supply phandles as probe-ordering
#                         dependencies, not just power. Without fhctl the
#                         gpufreq device never binds, so ged and mali defer
#                         behind it and there is no /dev/mali0 at all. fhctl in
#                         turn needs get_mcupm_ipidev from mcupm. Neither is
#                         pulled in by mali_kbase's own modules.dep entry, which
#                         is why the GPU stayed dead with every Mali module
#                         loaded. modules.dep does list fhctl's own deps
#                         (mcupm, gpueb, tinysys_ipi, rpmsg_mbox, mbox, ssc), so
#                         naming fhctl here is enough - the resolver below pulls
#                         the rest.
#   connfem / wmt_drv /  Wi-Fi, Bluetooth and GPS all sit behind MediaTek's
#   wmt_chrdev_wifi /    connsys stack, and none of them exist until it is
#   wlan_drv_gen4m_6789  loaded: no /dev/wmtdetect, no /dev/stpbt, and
#   bt_drv_connac1x /    vendor.connsys.driver.ready stays "no" forever. The BT
#   gps_drv_stp          HAL's "init_uart: Can't open /dev/stpbt" and wmt_loader
#                        spinning on /dev/wmtdetect are both just this. wlan0
#                        appears once something writes 1 to /dev/wmtWifi.
#   scp / sensorhub /    the sensors. MediaTek runs them on the SCP, so the
#   hf_manager           co-processor has to come up before the hub does.
#   snd-soc-* / audio_ipi / the audio card. mt6789-mt6366 is the machine driver
#   mtk-sp-spk-amp /     and the one that finally registers the card;
#   mt6358-accdet /      mtk-sp-spk-amp is not optional despite sounding like a
#   mtk-scp-audio /      speaker-amp detail, because mt6789-mt6366 imports
#   mt6789-mt6366        mtk_spk_get_type and friends from it and will not load
#                        without it.
# NOT here, deliberately: ccci_md_all (the modem interface). It loads and
# creates the ccci character devices and the ccmni interfaces, but on the MP01 a
# boot with it in this list wedges PID 1 - systemd sits in uninterruptible sleep
# (state D), the journal stops around 81s while the device stays up, and the bus
# goes away ("Failed to retrieve unit state: Transport endpoint is not
# connected"). The last thing logged before the stall is ccci closing ttyC2 on a
# POSIX timer. Cellular needs the modem started properly rather than the module
# merely inserted, so it wants its own investigation, not a line here.
#
# All of the above were loaded by hand on a running MP01 first, in dependency
# order, and none failed to insert. That turned out NOT to be sufficient, and
# they are deliberately not in the list below - see the next paragraph.
#
# MEASURED, 16 Sep 2026: adding them breaks the boot, and not because any of them
# misbehaves. They are simply slow. The Wi-Fi set alone took about 90 seconds to
# insert with its dependency closure, and that time lands squarely on the
# critical path, because this function runs before the compositor:
#
#   narrow list                     surface-manager active at  ~70s   boots
#   + connsys (6 named, 26 total)   started 109s, still starting 177s  device
#                                                                      power-cycles
#   + sensors + audio               never starts                       PID 1 wedged
#
# The MP01 has an unidentified power-off that fires around 190s into a boot that
# has not completed (systemd-logind receives PowerOffWithFlags from a short-lived
# client - see mp01-notes.md), so anything that delays the UI past that point
# turns into a reboot loop rather than a slow boot. Loading these modules is the
# right thing to do, but it has to happen off the critical path - after the
# compositor is up, the way camera-droid-heal was moved - and that wants its own
# unit rather than a longer list here.
VENDOR_DLKM_MODULES="${VENDOR_DLKM_MODULES:-nvmem-mt635x-efuse.ko gt9886.ko gt9896s.ko focaltech_touch.ko mtk_gpufreq_mt6789.ko mali_mgm_mt6789.ko mali_prot_alloc_mt6789.ko fhctl.ko mali_kbase_mt6789.ko}"

    if [ "$VENDOR_DLKM_MODULES" = "all" ]; then
        _todo=$(sed 's/#.*//; s/[[:space:]]//g; /^$/d' "$_d/modules.load")
    else
        # Expand each requested module to itself plus everything modules.dep
        # says it needs, transitively. insmod - unlike modprobe - will not do
        # this for us, and modprobe cannot be used here: it wants the modules
        # under <dir>/lib/modules/$(uname -r)/, which would mean creating a
        # symlink inside a read-only mount.
        _todo=""
        _pending="$VENDOR_DLKM_MODULES"
        while [ -n "$_pending" ]; do
            _cur=$(echo "$_pending" | awk '{print $1}')
            _pending=$(echo "$_pending" | cut -s -d' ' -f2-)
            case " $_todo " in *" $_cur "*) continue ;; esac
            _todo="$_todo $_cur"
            for _dep in $(sed -n "s|^[^ ]*/$_cur: *||p" "$_d/modules.dep" 2>/dev/null |
                          head -n 1 | tr ' ' '\n' | sed 's|.*/||; /^$/d'); do
                case " $_todo $_pending " in
                    *" $_dep "*) ;;
                    *) _pending="$_pending $_dep" ;;
                esac
            done
        done
        log "vendor_dlkm: $(echo $VENDOR_DLKM_MODULES | wc -w) requested, $(echo $_todo | wc -w) with dependencies"
    fi
    _pass=0
    # The bound is just a backstop: the loop already stops the moment a pass
    # loads nothing new, so it costs nothing to make it generous. It was 6, and
    # that was too few - loading the Mali stack was still resolving two modules
    # per pass when it ran out ("pass 6 loaded 2, 1 left"), leaving mali_kbase
    # unloaded and the GPU dead for want of one more iteration.
    while [ -n "$_todo" ] && [ "$_pass" -lt 24 ]; do
        _failed=""
        _loaded=0
        for _m in $_todo; do
            [ -f "$_d/$_m" ] || continue
            if insmod "$_d/$_m" 2>/dev/null; then
                _loaded=$((_loaded + 1))
            else
                # Already loaded counts as success; anything else waits for a
                # later pass, when its dependency may have appeared.
                if grep -q "^${_m%.ko}[[:space:]]" /proc/modules 2>/dev/null ||
                   grep -q "^$(echo "${_m%.ko}" | tr '-' '_')[[:space:]]" /proc/modules 2>/dev/null; then
                    _loaded=$((_loaded + 1))
                else
                    _failed="$_failed $_m"
                fi
            fi
        done
        _pass=$((_pass + 1))
        log "vendor_dlkm: pass $_pass loaded $_loaded, $(echo $_failed | wc -w) left"
        # No progress means the rest cannot load however many passes we make.
        [ "$_loaded" -eq 0 ] && break
        _todo="$_failed"
    done
    [ -n "$_todo" ] && log "vendor_dlkm: not loaded:$_todo"

    # Give the modules we actually asked for a second probe.
    #
    # A module can load fine and still not bind to its device. On the MP01,
    # mali_kbase loads on the last pass - by definition after everything it
    # depends on - and its probe returns -517 (EPROBE_DEFER) because the
    # dependency was not there yet when the device was first matched:
    #
    #   probe of 13000000.mali returned -517 after 3 usecs
    #
    # The kernel retries deferred probes whenever a new driver registers, but
    # this was the last insmod, so nothing ever triggered the retry. The driver
    # is resident, the dependencies are resident, and /dev/mali0 still never
    # appears - which the compositor reports as "Failed creating base context
    # during opening of kernel driver".
    #
    # Re-inserting the requested modules once, now that the whole set is up,
    # gives each a clean match. Only the explicitly requested ones: the
    # dependency closure is what they need in place, not what needs re-probing.
    # No re-probe pass here, deliberately.
    #
    # An earlier version re-inserted each requested module once the whole set
    # was resident, to shake loose a mali probe that had deferred. That deferral
    # turned out to be caused by fhctl/mcupm missing, and once those load the
    # mali device binds on its first try. Re-inserting it after that is actively
    # harmful: the rmmod tears down a working mali0 and the fresh insmod comes
    # back as "Probed as mali1" with no device node at all.
    #
    # If something here ever defers again, find what it is waiting for
    # (/sys/kernel/debug/devices_deferred) rather than re-probing blindly.
    return 0
}

load_vendor_dlkm_modules

# Point libprocessgroup at the container's cgroup descriptions.
#
# Every Android library we load through libhybris pulls in libprocessgroup, and
# it reads its two descriptions from /etc on the *host* side - where nothing
# ships them. The result is a wall of noise in logcat from every hybris client,
# surface-manager included:
#
#   E /usr/bin/surface-manager: Failed to read task profiles from /etc/cgroups.json
#   E libprocessgroup: CgroupMap::LoadDescriptors called for [N] failed
#   E libprocessgroup: CgroupMap::FindController called for [N] failed, cgroups
#                      were not initialized properly
#   W libprocessgroup: JoinCgroup: controller blkio is not found
#
# repeated for every controller and every profile lookup. The container ships
# both files, and they describe the same cgroup layout the host mounted, so
# symlinking is enough - with them in place the same process logs none of it.
#
# Not fatal either way: the profile calls fail closed and nothing depends on
# them (see the SetTaskProfiles hook in libhybris). This is about being able to
# read logcat.
link_cgroup_descriptions() {
    for _f in cgroups.json task_profiles.json; do
        [ -e "$ANDROID_ROOT/system/etc/$_f" ] || continue
        [ -e "/etc/$_f" ] && continue
        ln -sf "$ANDROID_ROOT/system/etc/$_f" "/etc/$_f" 2>/dev/null &&
            log "linked /etc/$_f -> $ANDROID_ROOT/system/etc/$_f"
    done
}

link_cgroup_descriptions

exit 0
