#!/bin/sh
LOGDIR=/root/debug-logs
mkdir -p $LOGDIR
LOG=$LOGDIR/usb-watchdog.log
echo "=== USB watchdog started at $(date) ===" >> $LOG
while true; do
    TS=$(date "+%H:%M:%S")
    USB_STATE="none"
    [ -d /config/usb_gadget/g1 ] && USB_STATE="configfs_ok"
    [ -d /sys/kernel/config/usb_gadget/g1 ] && USB_STATE="${USB_STATE}+syskconfig_ok"
    IP=$(ip -4 addr show usb0 2>/dev/null | grep inet | awk '{print $2}')
    LINK=$(cat /sys/class/net/usb0/operstate 2>/dev/null || echo "missing")
    echo "$TS gadget=$USB_STATE ip=$IP link=$LINK" >> $LOG
    if [ "$LINK" = "missing" ]; then
        echo "$TS USB INTERFACE GONE - dumping state" >> $LOG
        ls -la /config/usb_gadget/ >> $LOG 2>&1
        ls -la /sys/kernel/config/usb_gadget/ >> $LOG 2>&1
        mount | grep config >> $LOG 2>&1
        dmesg | tail -50 >> $LOG 2>&1
    fi
    sleep 5
done
