#!/bin/sh
# Recover WCNSS_qcom_wlan_nv.bin - the per-device WiFi/BT calibration for the
# WCN3620 - from the stock Samsung system partition.
#
# Why this is not a normal firmware package: unlike the modem, WCNSS and venus
# blobs, which Samsung puts on the apnhlos/modem partitions where
# msm-firmware-loader finds them, the NV file lives on /system, and it is
# calibration data rather than firmware, so it is not in linux-firmware and no
# LineageOS vendor tree for this device carries it either. postmarketOS
# resorted to pasting one unit's copy on a paste site
# (device/community/firmware-samsung-a3). Reading the device's own copy is both
# more correct - it is *this* unit's calibration - and avoids redistributing it.
#
# Without it wcn36xx still probes, but the firmware request fails and there is
# no wlan0 at all.

set -u

TARGET=/lib/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin
MNT=/run/samsung-a3-wcnss-nv

log() { echo "samsung-a3-wcnss-nv: $*"; }

[ -e "$TARGET" ] && exit 0

SYSPART=
for cand in /dev/disk/by-partlabel/system /dev/disk/by-partlabel/SYSTEM; do
    [ -e "$cand" ] && SYSPART="$cand" && break
done

# Before udev, so the by-partlabel symlinks may not exist yet: fall back to the
# kernel's own view of the partition names.
if [ -z "$SYSPART" ]; then
    # Parsed, not sourced. bash 5.3 fails to source files in sysfs (it reports
    # errno 0, so the message reads "... : Success") and the failed source
    # aborts the script - the same trap mdev-partlabel.sh hit on wrynose.
    for part in /sys/block/mmcblk*/mmcblk*p*; do
        [ -r "$part/uevent" ] || continue
        _partname=$(sed -n 's/^PARTNAME=//p' "$part/uevent")
        case "$_partname" in
            system|SYSTEM)
                SYSPART="/dev/$(sed -n 's/^DEVNAME=//p' "$part/uevent")"
                break
                ;;
        esac
    done
fi

if [ -z "$SYSPART" ]; then
    log "no stock system partition found, leaving WiFi firmware alone"
    exit 0
fi

mkdir -p "$MNT"
if ! mount -o ro,nodev,noexec,nosuid "$SYSPART" "$MNT" 2>/dev/null; then
    log "could not mount $SYSPART read-only"
    rmdir "$MNT" 2>/dev/null
    exit 0
fi

SRC=
for cand in \
    "$MNT/etc/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin" \
    "$MNT/vendor/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin" \
    "$MNT/system/etc/firmware/wlan/prima/WCNSS_qcom_wlan_nv.bin"
do
    [ -f "$cand" ] && SRC="$cand" && break
done

if [ -n "$SRC" ]; then
    mkdir -p "$(dirname "$TARGET")"
    if cp "$SRC" "$TARGET.tmp" && mv "$TARGET.tmp" "$TARGET"; then
        log "installed $TARGET from ${SRC#$MNT}"
    else
        log "failed to copy $SRC"
        rm -f "$TARGET.tmp"
    fi
else
    log "WCNSS_qcom_wlan_nv.bin not present on the stock system partition"
fi

umount "$MNT"
rmdir "$MNT" 2>/dev/null
exit 0
