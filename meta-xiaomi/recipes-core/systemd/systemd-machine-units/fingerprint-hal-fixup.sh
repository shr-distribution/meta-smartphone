#!/bin/sh
# See fingerprint-hal-fixup.service for the full rationale. Idempotent; safe to
# re-run.
#
# Two independent halves:
#   - module selection, which only tissot needs (guarded on the Goodix module
#     being present, which is the tissot vendor's marker);
#   - container /dev/uinput ownership, which every Halium 16 GSI device whose
#     fingerprint HAL creates an input device needs.
set -e

VNDHW=/android/vendor/lib64/hw
ATTACH="lxc-attach -P /var/lib/lxc -n android --"

# Wait until the container's vendor HAL modules are visible (android-system
# mounts /android/vendor) and its /dev/uinput node has been created by the
# container's ueventd. Device-neutral: no particular module name is assumed,
# which is what lets the same wait serve tissot's Goodix-stack vendor and mido's.
i=0
while [ "$i" -lt 30 ]; do
    if ls "$VNDHW"/*.so >/dev/null 2>&1 && \
       $ATTACH /system/bin/ls /dev/uinput >/dev/null 2>&1; then
        break
    fi
    i=$((i + 1))
    sleep 1
done

# Module selection - tissot only.
#
# Guarded on the Goodix module: tissot's vendor ships the Goodix stack and a
# service hardwired to it, while most Mi A1 units carry an FPC1020. mido has no
# gf_fingerprint.default.so at all - its @2.1-service.xiaomi_mido picks between
# fingerprint.goodix.so and fingerprint.searchf.so from ro.hardware.fingerprint,
# which the vendor's own init sets - so this block is skipped there.
if [ -e "$VNDHW/gf_fingerprint.default.so" ]; then
    # Detect the sensor: the fpc1020 kernel driver exposes this sysfs node, and
    # the vendor's init sets persist.sys.fp.vendor once detection has run.
    fpvendor=$($ATTACH /system/bin/getprop persist.sys.fp.vendor 2>/dev/null || true)
    if [ -e /sys/devices/platform/soc/soc:fpc1020 ] || [ "$fpvendor" = "fpc" ]; then
        # FPC unit: make the Goodix-hardwired service load the FPC module by
        # binding it over the gf_ path. Skip if it is already the FPC module.
        if ! grep -q fpc_enroll "$VNDHW/gf_fingerprint.default.so" 2>/dev/null; then
            mount -o bind "$VNDHW/fingerprint.default.so" \
                          "$VNDHW/gf_fingerprint.default.so"
        fi
    fi
fi

# Restore A9 /dev/uinput ownership on the CONTAINER node (system:bluetooth =
# 1000:1002), which the HAL opens as user "system".
$ATTACH /system/bin/chown 1000:1002 /dev/uinput || true
$ATTACH /system/bin/chmod 0660 /dev/uinput || true

# Restart the HAL so it re-opens with the right module + usable uinput, then let
# biomd rebind to the now-working service.
#
# On mido the HAL is "disabled" in its .rc and is started by the vendor's own
# property triggers (sys.fp.onstart / sys.fp.vendor, set by vendor.gx_fpd), so
# ctl.restart is a no-op until that has happened - which is why this is a
# restart rather than a start, and why it is harmless either way.
$ATTACH /system/bin/setprop ctl.restart vendor.fps_hal
sleep 3
systemctl restart biomd || true
