#!/bin/sh
# See fingerprint-hal-fixup.service for the full rationale. Idempotent; safe to
# re-run. No-ops on a Goodix unit's module selection (leaves gf_fingerprint).
set -e

VNDHW=/android/vendor/lib64/hw
ATTACH="lxc-attach -P /var/lib/lxc -n android --"

# Wait for the container's fingerprint module and /dev/uinput to appear.
i=0
while [ "$i" -lt 30 ]; do
    [ -e "$VNDHW/gf_fingerprint.default.so" ] && \
        $ATTACH /system/bin/ls /dev/uinput >/dev/null 2>&1 && break
    i=$((i + 1))
    sleep 1
done

# Detect the sensor: the fpc1020 kernel driver exposes this sysfs node, and the
# vendor's init sets persist.sys.fp.vendor once detection has run.
fpvendor=$($ATTACH /system/bin/getprop persist.sys.fp.vendor 2>/dev/null || true)
if [ -e /sys/devices/platform/soc/soc:fpc1020 ] || [ "$fpvendor" = "fpc" ]; then
    # FPC unit: make the Goodix-hardwired service load the FPC module by binding
    # it over the gf_ path. Skip if it is already the FPC module.
    if ! grep -q fpc_enroll "$VNDHW/gf_fingerprint.default.so" 2>/dev/null; then
        mount -o bind "$VNDHW/fingerprint.default.so" \
                      "$VNDHW/gf_fingerprint.default.so"
    fi
fi

# Restore A9 /dev/uinput ownership on the CONTAINER node (system:bluetooth =
# 1000:1002), which the HAL opens as user "system".
$ATTACH /system/bin/chown 1000:1002 /dev/uinput
$ATTACH /system/bin/chmod 0660 /dev/uinput

# Restart the HAL so it re-opens with the right module + usable uinput, then let
# biomd rebind to the now-working service.
$ATTACH /system/bin/setprop ctl.restart vendor.fps_hal
sleep 3
systemctl restart biomd || true
