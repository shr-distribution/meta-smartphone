#!/bin/sh
# Bring up a USB gadget for debugging: rndis + adb if that binds, rndis alone if
# it does not. NEVER leave the device with no gadget.
#
# WHY THE ORDER MATTERS
#
# android-gadget-setup creates ffs.adb, mounts functionfs on /dev/usb-ffs/adb,
# links both functions into c.1 - and then, because the function list contains
# rndis, binds the UDC straight away. FunctionFS will not let a gadget bind
# until a process has opened ep0 and written descriptors, i.e. until adbd runs.
# Binding first therefore fails, and because rndis shares the config it goes
# down too: the device ends up with NO gadget. That is the failure the old
# luneos-usb-rndis.service documented when it declared
# Conflicts=android-tools-adbd.service and settled for rndis alone.
#
# The script's own adb-only path already knows the trick - it defers the UDC
# write by a second in a subshell - but that path is not taken when rndis is
# present. So: set up, start adbd, wait for the endpoints, bind explicitly.
#
# AND FALL BACK. A first attempt at this left the phone with no gadget and no
# Wi-Fi, i.e. no way in at all, which is strictly worse than rndis alone. So if
# the combined gadget has not bound, tear it down and rebuild rndis-only before
# giving up. Losing adb is an inconvenience; losing every channel costs a
# reboot into the other OS.
set -x
GADGET=/sys/kernel/config/usb_gadget/g1

bound() { [ -s "$GADGET/UDC" ] && [ -n "$(cat "$GADGET/UDC" 2>/dev/null)" ]; }

# The first UDC that is not a dummy. "ls /sys/class/udc | head -1" is not good
# enough: a kernel built with CONFIG_USB_DUMMY_HCD exports dummy_udc, which sorts
# before every real controller name, so the gadget gets bound to the loopback
# controller and no USB appears on the cable at all. android-gadget-setup skips
# dummies for the same reason; keep the two in agreement.
first_real_udc() {
    for _u in /sys/class/udc/*; do
        [ -e "$_u" ] || continue
        _n=${_u##*/}
        case "$_n" in *dummy*) continue ;; esac
        echo "$_n"
        return 0
    done
    return 1
}

teardown() {
    [ -d "$GADGET" ] || return 0
    echo "" > "$GADGET/UDC" 2>/dev/null
    rm -f "$GADGET"/configs/c.1/rndis.usb0 "$GADGET"/configs/c.1/ffs.adb 2>/dev/null
    rmdir "$GADGET"/functions/rndis.usb0 "$GADGET"/functions/ffs.adb 2>/dev/null
    rmdir "$GADGET"/configs/c.1/strings/0x409 "$GADGET"/configs/c.1 2>/dev/null
    rmdir "$GADGET"/strings/0x409 2>/dev/null
    rmdir "$GADGET" 2>/dev/null
    umount /dev/usb-ffs/adb 2>/dev/null
}

# adbd refuses to start without this marker (ConditionPathExists in its unit).
[ -e /etc/usb-debugging-enabled ] || touch /etc/usb-debugging-enabled

/usr/bin/android-gadget-setup rndis,adb

# Unbind whatever the setup script managed, so the bind below is the only one.
[ -d "$GADGET" ] && echo "" > "$GADGET/UDC" 2>/dev/null

# adbd opens /dev/usb-ffs/adb/ep0 and writes its descriptors; until it has, the
# gadget cannot bind. Started here rather than left to systemd ordering because
# the window between "functionfs mounted" and "UDC written" is what has to be
# held open, and that is not expressible as a unit dependency.
#
# --no-block is NOT optional. This unit runs Before=basic.target with
# DefaultDependencies=no, and android-tools-adbd is WantedBy=basic.target, so a
# blocking `systemctl start` waits on a job that is ordered after the unit
# making the call. It deadlocks until TimeoutStartSec, the UDC is never written,
# and the device comes up with NO usb gadget at all - which is how the first
# version of this shipped. Fire and poll for the endpoints instead.
systemctl --no-block start android-tools-adbd.service || true

i=0
while [ $i -lt 10 ]; do
    [ -e /dev/usb-ffs/adb/ep1 ] && break   # adbd has claimed the endpoints
    sleep 1
    i=$((i+1))
done

UDC=$(first_real_udc)
[ -n "$UDC" ] && echo "$UDC" > "$GADGET/UDC" 2>/dev/null

if ! bound; then
    # Combined gadget did not take. Get rndis back rather than leaving nothing.
    systemctl stop android-tools-adbd.service 2>/dev/null
    teardown
    /usr/bin/android-gadget-setup rndis
    UDC=$(first_real_udc)
    [ -n "$UDC" ] && ! bound && echo "$UDC" > "$GADGET/UDC" 2>/dev/null
fi

# rndis0 comes up DOWN with no address; usb-ip.sh sets both and is idempotent.
[ -x /usr/local/bin/usb-ip.sh ] && /usr/local/bin/usb-ip.sh

# Leave a breadcrumb: with no Wi-Fi and no gadget there is nothing to read this
# from except the other OS, and "which branch did it take" is the first question.
{
    echo "=== $(date) luneos-usb-setup ==="
    echo "UDC=$(cat "$GADGET/UDC" 2>/dev/null)"
    ls "$GADGET/configs/c.1/" 2>/dev/null
    ls /dev/usb-ffs/adb/ 2>/dev/null
    ip -br link 2>/dev/null | grep -E "rndis|usb"
} > /var/log/usb-setup.txt 2>&1

exit 0
