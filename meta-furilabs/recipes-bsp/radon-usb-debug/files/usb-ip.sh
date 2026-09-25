#!/bin/sh
# Give the USB gadget netdev its address AND bring it up.
#
# Both halves are needed and the second is the one that was missing. The gadget
# binds fine and the host enumerates it, but rndis0 appears as
#
#   rndis0: <BROADCAST,MULTICAST,DYNAMIC> ... state DOWN
#   rndis0: operstate=down flags=0x9002 carrier=   rx=0   (no IP)
#
# - down, with no address - so the host sees carrier and nothing else, and every
# transmit ends in "rndis_host ... NETDEV WATCHDOG: transmit queue 0 timed out".
# That looked like a cable or port fault for a long time and is not one.
#
# Idempotent, because it runs from a udev rule on every add event as well as
# from the gadget unit's tail.
# Wait for the netdev. luneos-usb-setup.sh calls this straight after writing
# the UDC, and the kernel creates rndis0 asynchronously, so a bare check loses
# the race on a cold boot. udev calls it again with $INTERFACE when the device
# really appears, but do not depend on that alone - udev is not necessarily
# processing events yet at Before=basic.target.
IF="${1:-}"
if [ -z "$IF" ]; then
    i=0
    while [ $i -lt 20 ]; do
        for c in rndis0 usb0; do
            [ -e "/sys/class/net/$c" ] && IF="$c" && break 2
        done
        sleep 1
        i=$((i+1))
    done
fi
[ -n "$IF" ] || exit 0
[ -e "/sys/class/net/$IF" ] || exit 0

ip addr show dev "$IF" | grep -q "172\.16\.42\.2/24" || \
    ip addr add 172.16.42.2/24 dev "$IF" 2>/dev/null
ip link set "$IF" up 2>/dev/null
exit 0
