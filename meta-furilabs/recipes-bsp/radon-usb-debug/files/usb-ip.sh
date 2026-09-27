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

# One /24 per machine, so several LuneOS devices can be attached to one host at
# once. This used to hard-code 172.16.42.2/24, which is the address every LuneOS
# device took: the host then has several routes for one subnet, picks one, and
# traffic for the second device leaves down the first device's interface.
#
# Keep this table in sync with the four other copies:
#   meta-android/.../initramfs-scripts-android/init_functions.sh
#   meta-android/.../android-tools-conf/android-gadget-setup
#   meta-mainline/.../initramfs-scripts-simple/init_functions.sh
#   meta-pine64-luneos/.../luneos-usb-gadget.sh
# .42/.43/.44 belong to pinephone/pinephonepro/pinetab2 there; halium devices
# share .45 unless they have an entry, and radon has .46.
case "$(tr -d " \t\n" < /etc/hostname 2>/dev/null)" in
    radon) NET=172.16.46 ;;
    *)     NET=172.16.45 ;;
esac

ip addr show dev "$IF" | grep -q "$NET\.2/24" || \
    ip addr add "$NET.2/24" dev "$IF" 2>/dev/null
ip link set "$IF" up 2>/dev/null

# Hand the host its address, so the gadget is reachable without the host being
# configured per device. No "option router" and no "option dns" on purpose: this
# must never become the host's default gateway or resolver. Idempotent - this
# script also runs from a udev rule on every add event, so do not start a second
# udhcpd over the first.
if command -v udhcpd >/dev/null 2>&1 && \
   ! { [ -r /run/udhcpd-usb.pid ] && kill -0 "$(cat /run/udhcpd-usb.pid)" 2>/dev/null; }; then
    cat > /run/udhcpd-usb.conf <<EOF
interface $IF
start $NET.1
end $NET.1
max_leases 1
option subnet 255.255.255.0
lease_file /run/udhcpd-usb.leases
pidfile /run/udhcpd-usb.pid
EOF
    : > /run/udhcpd-usb.leases
    udhcpd /run/udhcpd-usb.conf 2>/dev/null ||
        echo "usb-ip: udhcpd failed; host must set $NET.1/24 itself" >&2
fi
exit 0
