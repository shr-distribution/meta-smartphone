#!/bin/sh
# Load the MediaTek combo connectivity modules from the vendor partition against
# the LuneOS kernel.
#
# Which modules, and in what order, is DERIVED from the vendor's own init rc
# files rather than declared here - same principle as start-android-hals.sh.
# Two eras are covered and they are not the same shape:
#
#   WMT era (mt6739/mindphone, Android 11)
#       The connectivity modules are listed in the vendor's modules.load and
#       loading that file in order is enough.
#
#   connac era (mt6789/q25, Android 12, GKI)
#       They are NOT in modules.load - only connadp and c2k_usb_f_via_gps are.
#       The real drivers are insmod'ed by init rc files behind a two-stage gate:
#
#           init.wmt_drv.rc     on boot                  -> wmt_drv.ko
#           init.connfem.rc     on boot                  -> connfem.ko
#           init.wlan_drv.rc    on property:vendor.connsys.driver.ready=yes
#                                                        -> ${ro.vendor.wlan.chrdev}.ko
#                                                        -> wlan_drv_${ro.vendor.wlan.gen}.ko
#           init.bt_drv.rc      ... same trigger         -> bt_drv_${ro.vendor.bt.platform}.ko
#           init.gps_drv.rc / init.gps_pwr.rc / init.fmradio_drv.rc likewise
#
#       vendor.connsys.driver.ready is set by the container's own connsys
#       daemon after it patches the CONSYS firmware, and in a Halium container
#       that property trigger may never fire - the same class of problem as the
#       wait_for_prop gates start-android-hals.sh opens. Loading them here in
#       the rc-declared order sidesteps it.
#
# Note the file names are property-templated. Hardcoding the expansions would
# make this a per-device list again, so the properties are read off the mounted
# vendor partition and substituted.

# Same default mount-android.sh uses; this unit does not source it, so set it
# independently rather than assuming the caller's environment carries it.
ANDROID_ROOT="${ANDROID_ROOT:-/android}"
VMOD="$ANDROID_ROOT/vendor/lib/modules"
KREL=$(uname -r)
DST="/lib/modules/$KREL"
log() { echo "mtk-connectivity: $*"; }

# On a GKI device /vendor/lib/modules is an absolute symlink to
# /vendor_dlkm/lib/modules, which is meant to resolve here because
# mount-android.sh mounts vendor_dlkm on the HOST at /vendor_dlkm (it cannot go
# under /android - LXC drops anything mounted on the container rootfs). In
# practice that bare host mount does not always take - confirmed on the MP01,
# where only /android/vendor_dlkm ends up populated - so fall through the same
# candidate list mount-android.sh's own load_vendor_dlkm_modules() already uses,
# rather than assuming either one.
if [ ! -d "$VMOD" ]; then
    for _cand in /vendor_dlkm/lib/modules "$ANDROID_ROOT"/vendor_dlkm/lib/modules \
                 /android/vendor_dlkm/lib/modules; do
        [ -d "$_cand" ] && { VMOD=$_cand; break; }
    done
fi
[ -d "$VMOD" ] || { log "no vendor module directory; nothing to do"; exit 0; }

# --- nvram path for the WMT-era driver --------------------------------------
# wmt_drv has one nvram path baked into it, /data/nvram/APCFG/APRDEB/WIFI, and
# it opens it from kernel space - so it resolves against the host rootfs, not
# the container's. Android has /data/nvram symlinked to the nvdata partition; on
# LuneOS /data is the container's userdata and that link does not exist, so the
# open cannot succeed. Give the driver the path it asks for, before the chip is
# powered on, which is why this runs here.
if [ -d /mnt/vendor/nvdata ] && [ ! -L /data/nvram ]; then
    rmdir /data/nvram 2>/dev/null
    [ -e /data/nvram ] || ln -sfn /mnt/vendor/nvdata /data/nvram 2>/dev/null || true
fi

# Firmware lives on the vendor partition; point the kernel loader at it.
if [ -d "$VMOD/../firmware" ]; then
    echo "$VMOD/../firmware" > /sys/module/firmware_class/parameters/path 2>/dev/null || true
fi

# modprobe wants a depmod'd tree and the vendor .ko are not under /lib/modules,
# so mirror them once. All of them, not just the connectivity family: depmod
# needs the full set to resolve inter-module dependencies.
mkdir -p "$DST"
cp -u "$VMOD"/*.ko "$DST"/ 2>/dev/null || true
depmod "$KREL" 2>/dev/null || true

# --- property expansion ------------------------------------------------------
# ${ro.vendor.wlan.gen} and friends. getprop works only once the container's
# property service is up, and this may run before that, so read the prop files
# directly and let getprop fill any gaps.
PROPFILES="/android/vendor/build.prop /android/vendor/default.prop
           /android/odm/etc/build.prop /android/odm/build.prop
           /android/system/build.prop"
getprop_static() {
    _v=$(cat $PROPFILES 2>/dev/null | sed -n "s/^$1=//p" | tail -1)
    [ -n "$_v" ] || _v=$(getprop "$1" 2>/dev/null)
    printf '%s' "$_v"
}
expand_props() {
    _s=$1
    while :; do
        case "$_s" in
            *'${'*'}'*) ;;
            *) break ;;
        esac
        _name=${_s#*\$\{}; _name=${_name%%\}*}
        _val=$(getprop_static "$_name")
        [ -n "$_val" ] || { log "cannot expand \${$_name}; skipping '$_s'"; return 1; }
        _s="${_s%%\$\{$_name\}*}${_val}${_s#*\$\{$_name\}}"
    done
    printf '%s' "$_s"
}

# --- the ordered module list -------------------------------------------------
# Rank 0: insmod under a boot-stage trigger (on boot, on post-fs-data, ...).
# Rank 1: insmod under a property trigger - the second stage above.
# sort -s keeps rc file and line order inside each rank, which is the order the
# vendor intends. wmt_drv must precede wmt_chrdev_wifi and wlan_drv.
rc_modules() {
    for rc in /android/vendor/etc/init/*.rc /android/odm/etc/init/*.rc; do
        [ -f "$rc" ] || continue
        awk '
            /^[[:space:]]*on[[:space:]]/ {
                rank = ($0 ~ /on[[:space:]]+property:/) ? 1 : 0; next
            }
            /^[[:space:]]*insmod[[:space:]]+.*\/lib\/modules\// {
                p = $2
                sub(/.*\/lib\/modules\//, "", p)
                sub(/\.ko$/, "", p)
                printf "%d\t%s\n", rank, p
            }
        ' "$rc"
    # awk '!seen[$0]++' after the sort, not before: init.bt_drv.rc lists the
    # same module under both the ready=yes and ready=no triggers, and loading
    # it twice is harmless but makes the count lie.
    done | sort -s -k1,1n | cut -f2 | awk '!seen[$0]++'
}

loaded=0
load_one() {
    _m=$1
    [ -f "$DST/$_m.ko" ] || { log "$_m.ko not on the vendor partition, skipping"; return; }
    # Plain first: on a KMI-preserving port the CRCs match and only the
    # vermagic string differs (ours carries our own scmversion), so
    # --force-vermagic is the precise tool and --force-modversions is not
    # needed. Falling back through all three keeps the pre-GKI devices working,
    # where the CRCs really do disagree.
    if modprobe "$_m" 2>/dev/null; then :
    elif modprobe --force-vermagic "$_m" 2>/dev/null; then :
    elif modprobe --force "$_m" 2>/dev/null; then
        log "$_m loaded with --force (CRC mismatch - check the KMI)"
    else
        log "$_m failed to load"; return
    fi
    loaded=$((loaded + 1))
}

for m in $(rc_modules); do
    m=$(expand_props "$m") || continue
    [ -n "$m" ] && load_one "$m"
done

# Fallback for the WMT era, where the modules really are in modules.load and the
# rc files do not insmod them.
if [ "$loaded" -eq 0 ] && [ -f "$VMOD/modules.load" ]; then
    log "no insmod lines in the vendor rc files; falling back to modules.load"
    # Not a "| while read" loop: that runs in a subshell and the counter
    # load_one keeps would never come back out of it.
    for m in $(grep -iE 'wmt|wlan|conn|bt_drv|gps|fmradio|fm_drv' "$VMOD/modules.load" 2>/dev/null); do
        [ -n "$m" ] || continue
        load_one "$(basename "$m" .ko)"
    done
fi

log "loaded $loaded connectivity module(s)"
command -v rfkill >/dev/null 2>&1 && rfkill unblock all 2>/dev/null || true
exit 0
