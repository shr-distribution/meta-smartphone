#!/bin/sh
# Re-apply USB gadget network IP after switch_root
# Called by luneos-debug-usbnet.service

for i in 1 2 3 4 5 6 7 8 9 10; do
    if [ -d /sys/class/net/usb0 ]; then
        /sbin/ip link set usb0 up 2>/dev/null
        /sbin/ip address replace 172.16.42.2/16 dev usb0
        echo "USB network configured on attempt $i" > /dev/kmsg
        exit 0
    fi
    echo "Waiting for usb0... attempt $i" > /dev/kmsg
    sleep 1
done

echo "ERROR: usb0 not found after 10 attempts" > /dev/kmsg
exit 1
