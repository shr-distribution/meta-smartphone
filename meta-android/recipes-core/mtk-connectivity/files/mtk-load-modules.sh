#!/bin/sh
# Load the MediaTek combo connectivity modules (WMT era: wmt_drv/wlan_drv_gen2/
# bt_drv/gps_drv; connac era: conninfra/wlan_drv_gen4m/...) from the vendor
# partition against the LuneOS kernel.
#
# The modules are the stock vendor .ko. Their CRCs disagree with our kernel
# (the LuneOS config fragment shifts module_layout), so they can only be
# force-loaded - the kernel is built with CONFIG_MODULE_FORCE_LOAD for exactly
# this. Which modules to load, and in what order, comes from the vendor's own
# modules.load, so this needs no per-device or per-chip-generation list.
#
# The container's wmt_loader/wmt_launcher (or the connac equivalent) poll for
# the /dev nodes these modules create and patch the CONSYS firmware once they
# appear, so this can run after the container is up - the loader picks the
# modules up on its next retry.

VMOD=/android/vendor/lib/modules
KREL=$(uname -r)

[ -f "$VMOD/modules.load" ] || exit 0

# wmt_drv has one nvram path baked into it, /data/nvram/APCFG/APRDEB/WIFI, and
# it opens it from kernel space - so it resolves against the host rootfs, not
# the container's. Android has /data/nvram symlinked to the nvdata partition; on
# LuneOS /data is the container's userdata and that link does not exist, so the
# open cannot succeed. Give the driver the path it asks for, before it powers
# the chip on, which is why this runs here.
#
# Note this was tried as a fix for BT scanning finding nothing and did not fix
# it - Wi-Fi RX works, and BT still returns no advertising reports with the link
# in place - so it is correctness rather than a known-necessary fix.
if [ -d /mnt/vendor/nvdata ] && [ ! -L /data/nvram ]; then
    # The container's init may have left an empty /data/nvram directory behind,
    # which is not the real thing - rmdir declines if anything is in it.
    rmdir /data/nvram 2>/dev/null
    [ -e /data/nvram ] || ln -sfn /mnt/vendor/nvdata /data/nvram 2>/dev/null || true
fi

# Firmware lives on the vendor partition; point the kernel loader at it.
if [ -d "$VMOD/../firmware" ]; then
    echo "$VMOD/../firmware" > /sys/module/firmware_class/parameters/path 2>/dev/null || true
fi

# modprobe --force needs a depmod'd tree and the vendor .ko are not under
# /lib/modules, so mirror them once.
DST="/lib/modules/$KREL"
mkdir -p "$DST"
cp -u "$VMOD"/*.ko "$DST"/ 2>/dev/null || true
depmod "$KREL" 2>/dev/null || true

# Load only the connectivity family from modules.load, in its declared order,
# leaving the unrelated vendor modules (fpsgo, met, ...) to the container.
grep -iE 'wmt|wlan|conn|bt_drv|gps|fmradio|fm_drv' "$VMOD/modules.load" 2>/dev/null | \
while read -r m; do
    [ -n "$m" ] || continue
    mod=$(basename "$m" .ko)
    modprobe --force "$mod" 2>/dev/null || true
done

command -v rfkill >/dev/null 2>&1 && rfkill unblock all 2>/dev/null || true
exit 0
