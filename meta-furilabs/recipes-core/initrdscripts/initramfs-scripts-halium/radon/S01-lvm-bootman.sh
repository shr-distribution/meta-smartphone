#!/bin/sh
# radon: boot LuneOS from an LVM logical volume, honouring FuriLabs' bootman.
#
# This runs from /scripts/local-premount, i.e. at the very top of Halium's
# mountroot(), before it goes looking for a userdata partition. Same hook and
# the same two tricks tenderloin uses (meta-hp's S01-mount-boot.sh): set
# $_syspart so mountroot mounts a block device instead of loop-mounting
# rootfs.img, and synthesise /dev/userdata so the rest of mountroot is
# unmodified.
#
# WHY THIS EXISTS
#
# The standalone install writes a 4.2 GiB image over the whole userdata
# partition, which destroys FuriOS. FuriLabs' own dual-boot mechanism does not
# need that: each OS lives in an LVM logical volume inside userdata, and their
# boot manager switches between them. This script is the LuneOS side of that
# contract.
#
# THE CONTRACT (read out of furilabs/bootman main.c and
# furilabs/initramfs-tools-halium scripts/halium)
#
# - userdata is an LVM PV; the volume group is "furios" (Droidian's is
#   "droidian"). FuriOS's own root is the LV "furios-rootfs".
# - bootman is an LVGL menu that runs *in the initramfs* of whichever OS is
#   currently flashed. When the user picks an entry it:
#     * mounts that LV,
#     * reads VERSION= from <lv>/usr/lib/furios/device/flash-bootimage.conf,
#     * dd's <lv>/boot/boot.img-$VERSION  to /dev/disk/by-partlabel/boot_a
#       and  <lv>/boot/dtbo.img-$VERSION  to /dev/disk/by-partlabel/dtbo_a,
#     * writes the chosen LV to /furios-persist/bootman/next-boot,
#     * reboots.
#   So every OS carries its own kernel inside its own rootfs, and the *next*
#   boot is the one that runs it.
# - The initramfs that then comes up reads next-boot, mounts that LV as root,
#   and RENAMES next-boot to old-boot. bootman compares its selection against
#   old-boot and exits without reflashing when nothing changed.
#
# That rename is not optional. bootman only shows its menu when next-boot is
# absent (scripts/halium: `if [ ! -f .../next-boot ]; then run_bootman`), so an
# initramfs that reads next-boot and leaves it in place locks the device out of
# the menu forever.
#
# - The state lives on the "furios-persist" partition, which on this device is
#   vendor_boot_a (FuriLabs' usr/lib/furios/device/furios-persist-partition).
#   That is why the LuneOS flash kit never writes vendor_boot.
#
# RUNNING THE MENU
#
# bootman runs from *this* initramfs, because that is how the mechanism works:
# whichever OS is flashed provides the menu for the whole device. radon.conf
# installs it (ANDROID_EXTRA_INITRAMFS_IMAGE_INSTALL), and the logic below
# mirrors FuriOS's own gating in scripts/halium:
#
#   - only when there is more than one thing to boot,
#   - only when next-boot is absent (i.e. this boot was not already chosen),
#   - and bootman itself exits immediately when the user picks what is already
#     running, by comparing against old-boot.
#
# So the menu appears when a choice exists and is skipped otherwise.

LUNEOS_VG="${LUNEOS_VG:-furios}"
LUNEOS_LV="${LUNEOS_LV:-luneos}"
LUNEOS_DATA_LV="${LUNEOS_DATA_LV:-luneos-data}"
FURIOS_PERSIST="${FURIOS_PERSIST:-/dev/disk/by-partlabel/vendor_boot_a}"

# Opt out and fall back to the plain rootfs.img layout, for A/B testing a
# failure without reflashing: add luneos.lvm.disable to the kernel command line.
if grep -q luneos.lvm.disable /proc/cmdline; then
    tell_kmsg "initrd: luneos.lvm.disable given, skipping LVM/bootman"
    return 0 2>/dev/null || exit 0
fi

# The lvm2 package installs /usr/sbin/lvm; lvm.static is checked first only so
# a future static build would still be picked up.
lvm_bin=""
for c in /sbin/lvm.static /usr/sbin/lvm /sbin/lvm; do
    [ -x "$c" ] && { lvm_bin="$c"; break; }
done

if [ -z "$lvm_bin" ]; then
    tell_kmsg "initrd: no lvm binary in the initramfs, leaving the rootfs.img layout alone"
    return 0 2>/dev/null || exit 0
fi

[ -d /var/lock ] || mkdir -p /var/lock
[ -d /run/lock ] || mkdir -p /run/lock

tell_kmsg "initrd: activating LVM with $lvm_bin"
$lvm_bin vgscan >/dev/null 2>&1

# The PV sits on userdata, which on this device is UFS and appears a little
# after the storage driver loads. mountroot's own wait happens later, so do a
# short one here.
_found=""
for _try in 1 2 3 4 5; do
    $lvm_bin vgscan --mknodes >/dev/null 2>&1
    if $lvm_bin vgchange -ay "$LUNEOS_VG" >/dev/null 2>&1 && \
       [ -e "/dev/$LUNEOS_VG/$LUNEOS_LV" ]; then
        _found=yes
        break
    fi
    sleep 2
done

if [ -z "$_found" ]; then
    tell_kmsg "initrd: no /dev/$LUNEOS_VG/$LUNEOS_LV - falling back to the rootfs.img layout"
    return 0 2>/dev/null || exit 0
fi

tell_kmsg "initrd: found LVM root /dev/$LUNEOS_VG/$LUNEOS_LV"
_syspart="/dev/$LUNEOS_VG/$LUNEOS_LV"

# bootman's selection, if there is one.
#
# Mounted read-write because the next-boot -> old-boot rename has to persist;
# everything else here only reads. Failures are non-fatal: without this file we
# simply boot our own LV, which is the right answer when LuneOS is the flashed
# OS anyway.
if [ -e "$FURIOS_PERSIST" ]; then
    _fs=$(blkid -o value -s TYPE "$FURIOS_PERSIST" 2>/dev/null)
    if [ "$_fs" = "ext4" ]; then
        mkdir -p /furios-persist
        if mount "$FURIOS_PERSIST" /furios-persist 2>/dev/null; then
            # Apply any pending install/remove work bootman-gui queued from
            # the booted system. It writes shell commands (lvresize, lvcreate,
            # mke2fs) to be run here, where the filesystems are not mounted.
            if [ -f /furios-persist/bootman/commands ]; then
                tell_kmsg "initrd: running queued bootman commands"
                while IFS= read -r _cmd; do
                    [ -z "$_cmd" ] && continue
                    tell_kmsg "initrd: bootman cmd: $_cmd"
                    eval "$_cmd" || tell_kmsg "initrd: FAILED: $_cmd"
                done < /furios-persist/bootman/commands
                if [ -f /furios-persist/bootman/wip-partitions ]; then
                    cat /furios-persist/bootman/wip-partitions \
                        >> /furios-persist/bootman/partitions
                fi
                rm -f /furios-persist/bootman/wip-partitions \
                      /furios-persist/bootman/commands
                $lvm_bin vgscan >/dev/null 2>&1
            fi

            # Show the menu, on the same conditions FuriOS uses.
            #
            # Only when something else is installed: with one OS there is
            # nothing to choose. The two 32 MiB LVs are FuriOS's persist and
            # reserved volumes and are not bootable, so they are excluded the
            # way scripts/halium excludes them.
            if [ ! -f /furios-persist/bootman/next-boot ] && [ -x /usr/bin/bootman ]; then
                _lv_count=$($lvm_bin lvs --noheadings --options lv_size 2>/dev/null \
                            | grep -v "32.00m" | wc -l)
                # grep -c prints a count AND exits 1 when nothing matched, so
                # a "|| echo 0" fallback here would append a second line and
                # make the arithmetic below fail - in the common case, where
                # the partitions file lists only LVs.
                _dev_count=0
                if [ -f /furios-persist/bootman/partitions ]; then
                    _dev_count=$(grep -c "/dev" /furios-persist/bootman/partitions 2>/dev/null)
                    [ -n "$_dev_count" ] || _dev_count=0
                fi
                if [ $((_lv_count + _dev_count)) -gt 1 ]; then
                    tell_kmsg "initrd: starting bootman ($_lv_count LVs, $_dev_count external)"
                    # bootman drives the panel and the touchscreen directly.
                    # Both drivers are built into this kernel, so there is
                    # nothing to load first - unlike FuriOS, which calls a
                    # setup_touchscreen hook here.
                    #
                    # It normally flashes the chosen OS's boot image and
                    # reboots, so this call does not usually return. When the
                    # user picks what is already running it exits 0 and we fall
                    # through and boot it.
                    bootman >/dev/null 2>&1 || \
                        tell_kmsg "initrd: bootman exited $? - continuing"
                else
                    tell_kmsg "initrd: only one bootable volume, skipping the menu"
                fi
            fi

            if [ -f /furios-persist/bootman/next-boot ]; then
                _chosen=$(cat /furios-persist/bootman/next-boot 2>/dev/null)
                if [ -n "$_chosen" ]; then
                    case "$_chosen" in
                        /*) _cand="$_chosen" ;;
                        *)  _cand="/dev/$LUNEOS_VG/$_chosen" ;;
                    esac
                    # Honour it only if it is a block device we can actually
                    # boot. bootman flashes the chosen OS's own boot image
                    # before setting this, so if we are the running kernel the
                    # value should name us; anything else means the two got out
                    # of step, and booting our own LV is the safe answer.
                    if [ -b "$_cand" ]; then
                        _syspart="$_cand"
                        tell_kmsg "initrd: bootman selected $_chosen"
                    else
                        tell_kmsg "initrd: bootman selected $_chosen but $_cand is not a block device - booting $LUNEOS_LV"
                    fi
                fi
                # Rotate, so bootman shows its menu again next time. See the
                # long comment at the top: skipping this hides the menu for
                # good.
                mv /furios-persist/bootman/next-boot \
                   /furios-persist/bootman/old-boot 2>/dev/null \
                    && tell_kmsg "initrd: rotated next-boot -> old-boot"
            elif [ -f /furios-persist/bootman/old-boot ]; then
                # bootman returned without setting a new choice, which means
                # the user picked what is already running. old-boot names it.
                _chosen=$(cat /furios-persist/bootman/old-boot 2>/dev/null)
                if [ -n "$_chosen" ]; then
                    case "$_chosen" in
                        /*) _cand="$_chosen" ;;
                        *)  _cand="/dev/$LUNEOS_VG/$_chosen" ;;
                    esac
                    if [ -b "$_cand" ]; then
                        _syspart="$_cand"
                        tell_kmsg "initrd: continuing with the current choice $_chosen"
                    fi
                fi
            else
                tell_kmsg "initrd: no bootman next-boot, booting $LUNEOS_LV"
            fi
            sync
            umount /furios-persist 2>/dev/null
        else
            tell_kmsg "initrd: could not mount furios-persist ($FURIOS_PERSIST)"
        fi
    else
        tell_kmsg "initrd: furios-persist is '$_fs', not ext4 - skipping bootman"
    fi
else
    tell_kmsg "initrd: $FURIOS_PERSIST absent - skipping bootman"
fi

# Give mountroot a userdata to find.
#
# mountroot looks for a device *named* userdata (`find /dev -name userdata`),
# mounts it at /tmpmnt and later moves it to ${rootmnt}/userdata, which is where
# /var, /home and the Android container's data live. Under LVM the real
# userdata partition is the PV and must not be mounted as a filesystem, so point
# the name at our data LV instead - exactly as tenderloin does with
# `ln -s media /dev/store/userdata`.
#
# This is what keeps the rest of mountroot unpatched.
if [ -b "/dev/$LUNEOS_VG/$LUNEOS_DATA_LV" ]; then
    ln -sf "/dev/$LUNEOS_VG/$LUNEOS_DATA_LV" /dev/userdata
    tell_kmsg "initrd: /dev/userdata -> $LUNEOS_DATA_LV"
else
    # No data LV. mountroot would then find the real userdata partition, try to
    # fsck and mount the PV, and fail in a way that reads as a corrupt
    # filesystem. Say plainly what is wrong instead.
    tell_kmsg "initrd: WARNING no /dev/$LUNEOS_VG/$LUNEOS_DATA_LV."
    tell_kmsg "initrd: mountroot will now try to mount the LVM PV as a filesystem and fail."
    tell_kmsg "initrd: create it with: lvcreate -L <size> -n $LUNEOS_DATA_LV $LUNEOS_VG && mke2fs -t ext4 /dev/$LUNEOS_VG/$LUNEOS_DATA_LV"
fi
