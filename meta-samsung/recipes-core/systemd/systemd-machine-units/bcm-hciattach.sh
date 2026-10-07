#!/bin/sh
# Attach the BCM4339 Bluetooth controller of the SM-T520 to bluez.
#
# Run by bcm-hciattach.service at boot, and again by the udev rule 70-bcm-hciattach.rules whenever the
# controller's rfkill switch is unblocked. Switching Bluetooth off in Settings blocks that switch, which cuts
# the chip's power: it comes back without its firmware patch and at its default UART speed, and the kernel's
# own "power on" of hci0 then times out. So every time the chip is powered it has to be attached again.
#
# The address is the one Samsung stored in the tablet's EFS, which the container start mounts at
# /android/efs. Without a valid one hciattach is run without it and the controller keeps the default
# address of its firmware.


bdaddr=""
if [ -r /android/efs/bluetooth/bt_addr ]; then
    addr=$(tr -d '\r\n ' < /android/efs/bluetooth/bt_addr)
    case "$addr" in
        [0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f]:[0-9A-Fa-f][0-9A-Fa-f])
            bdaddr=$addr ;;
        *)
            echo "bcm-hciattach.sh: ignoring bt_addr '$addr', not an address" ;;
    esac
fi

attach() {
    # 3 Mbit/s with hardware flow control, the same as the vendor's library. The address has to follow the
    # flow and sleep tokens: "flow <address>" is read as the sleep setting and the address is ignored (the
    # controller then keeps the default of its firmware, 43:39:...).
    # 9>&-: hciattach stays behind as a daemon and must not inherit (and so hold for ever) the lock below.
    /usr/bin/hciattach /dev/ttySAC0 bcm43xx 3000000 flow nosleep ${bdaddr:+"$bdaddr"} 9>&-
}

# Switch the controller's low-power mode on. Without it the chip keeps its HOST_WAKE line asserted for as long as
# it is powered, and the board file's host-wake interrupt handler (arch/arm/mach-exynos/board-bluetooth-bcm4339.c)
# then holds a wake lock for all that time: BT_host_wake showed as "active" for 9 hours of uptime and every suspend
# attempt of sleepd was refused with "wakeup source still active ... BT_bt_wake" (both locks are listed under that
# name, the driver names them from one shared buffer). hciattach's own "sleep" token does not send this for the
# bcm43xx type, so it is sent here, the way the vendor's library does (hardware/broadcom/libbt hw_lpm_enable):
# Write_Sleep_Mode, vendor command 0xFC27, 12 bytes with the library's defaults: UART sleep mode, idle thresholds
# 1 and 1, BT_WAKE and HOST_WAKE active high, allow host sleep during SCO, combine sleep mode and LPM, no UART TXD
# tri-state, no pulsed host wake. A rfkill cycle (the chip loses its power) forgets it, a power toggle in bluez does
# not, so this runs after every attach.
#
# The command needs hci0 up. Waiting for bluetoothd to bring it up does not work at boot: bcm-hciattach.service is
# ordered before bluetooth.service, so bluetoothd only started once this had waited in vain (it started at 77 s,
# 30 s after the attach, on 6 Oct 2026) and the lock stayed held for the whole boot. Bring hci0 up here instead;
# bluetoothd takes over whatever power state it finds.
enable_lpm() {
    i=0
    while [ "$i" -lt 10 ]; do
        hciconfig hci0 up 2>/dev/null
        if hcitool -i hci0 cmd 0x3f 0x27 01 01 01 01 01 01 01 00 00 00 00 00 2>/dev/null | grep -q "01 27 FC 00"; then
            echo "bcm-hciattach.sh: controller low-power mode on"
            return 0
        fi
        i=$((i + 1))
        sleep 1
    done
    echo "bcm-hciattach.sh: could not switch the controller's low-power mode on; suspend stays blocked by BT_host_wake"
    return 1
}

# One attach at a time. At boot this service and bcm-hciattach-resume.service (started by udev for the rfkill
# cycle below) ran together; each killed the other's hciattach, and neither got to the low-power mode.
exec 9>/run/bcm-hciattach.lock
flock 9

# Already attached and running: only make sure the low-power mode is on. The rfkill cycle further down makes
# udev run this again, and that run lands here once the first one is done.
if hciconfig hci0 2>/dev/null | grep -q "UP RUNNING" && pidof hciattach >/dev/null; then
    enable_lpm
    exit 0
fi

# A daemon from an earlier attach still holds the UART (and hci0).
pkill -x hciattach 2>/dev/null
sleep 1

# Attach to a chip that has just been powered: the rfkill switch is its power pin, and a chip that still
# runs from an earlier attach answers at the new speed already ("Initialization timed out"). Powering it
# takes a moment before it listens on the UART. Each cycle sends udev events that start the resume unit
# again; that one is a no-op while this runs and leaves at once afterwards, as hci0 is up by then.
n=0
while [ "$n" -lt 3 ]; do
    /usr/sbin/rfkill block bluetooth
    sleep 1
    /usr/sbin/rfkill unblock bluetooth
    sleep 2
    if attach; then
        enable_lpm
        exit 0
    fi
    echo "bcm-hciattach.sh: attach failed (try $((n + 1)))"
    pkill -x hciattach 2>/dev/null
    sleep 1
    n=$((n + 1))
done
exit 1
