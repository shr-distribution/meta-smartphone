#!/bin/sh
# Bring the MediaTek Bluetooth HAL up once the combo chip can serve it, then
# restart bluebinder so it initialises against a live controller.
#
# Two things went wrong with the previous inline version:
#   - it started "bluetooth-1-0", a name no MediaTek vendor has to use. The MP01
#     declares bluetooth-1-1 (android.hardware.bluetooth@1.1-service-mediatek),
#     so every attempt failed with "Could not find 'bluetooth-1-0'".
#   - bluebinder had already started at boot, before the HAL was usable, so it
#     has to be started again once the HAL is up - and stopped first, see below.
#
# The controller workaround itself (masking Synchronization Train support) is
# a bluebinder setting, applied per device by luneos-device-config.
#
# The HAL opens /dev/stpbt, which is what powers the BT function on; that only
# works once the vendor's connsys stage has run (vendor.connsys.driver.ready).

log() { echo "mtk-bt-bringup: $*"; }

in_android() { lxc-attach -n android -- /system/bin/sh -c "$1"; }

# The vendor's own name for the HAL service, from its rc files.
hal=""
for rc in /android/vendor/etc/init/*.rc /android/odm/etc/init/*.rc; do
    [ -f "$rc" ] || continue
    hal=$(awk '$1 == "service" && $3 ~ /android\.hardware\.bluetooth@/ { print $2; exit }' "$rc")
    [ -n "$hal" ] && break
done
if [ -z "$hal" ]; then
    log "no vendor Bluetooth HAL service found, nothing to do"
    exit 0
fi
log "vendor Bluetooth HAL is $hal"

i=0
while [ $i -lt 60 ]; do
    ready=$(in_android "getprop vendor.connsys.driver.ready" 2>/dev/null)
    if [ "$ready" = "yes" ] && [ -e /dev/stpbt ]; then
        break
    fi
    i=$((i + 1))
    sleep 2
done
if [ $i -ge 60 ]; then
    log "connsys not ready or /dev/stpbt missing after 120s"
    exit 1
fi

# The container's ueventd creates /dev/stpbt as system:system; the HAL runs as
# bluetooth and needs it per the vendor ueventd.rc.
in_android "chown bluetooth:bluetooth /dev/stpbt" || log "could not chown /dev/stpbt"

# bluebinder and BlueZ go down first. Restarting the HAL under a connected
# bluebinder leaves it looping on "Remote has died"; systemd then kills it, and
# the vhci it leaves behind is an hci0 that no longer initialises ("Invalid
# request code") until the whole stack is restarted in order.
systemctl stop bluetooth.service bluebinder.service 2>/dev/null

in_android "stop $hal; start $hal" || { log "could not restart $hal"; exit 1; }

# Wait for init to report the HAL running before handing over to bluebinder.
i=0
while [ $i -lt 15 ]; do
    [ "$(in_android "getprop init.svc.$hal" 2>/dev/null)" = "running" ] && break
    i=$((i + 1))
    sleep 1
done
log "$hal is $(in_android "getprop init.svc.$hal" 2>/dev/null)"

if systemctl -q is-enabled bluebinder.service 2>/dev/null; then
    systemctl start bluebinder.service && log "started bluebinder"
fi
systemctl start bluetooth.service 2>/dev/null
exit 0
