#!/bin/sh

# mount boot partition:
mkdir -p /mnt/boot
mount -o ro /dev/mmcblk0p13 /mnt/boot

info "[initramfs] mmcblk0p13 mounted."

# use lvm.static binary file to initialize /dev/store
[ ! -d /var/lock ] && mkdir -p /var/lock
[ ! -d /run/lock ] && mkdir -p /run/lock

if [ -e /mnt/boot/usr/sbin/lvm.static ]
    then
    info "[initramfs] Activating LVM..."
    export LVM_SYSTEM_DIR=/mnt/boot/etc/lvm 
    info "LVM_SYSTEM_DIR: $LVM_SYSTEM_DIR"
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay 2>&1 > /media/internal/lvm.txt
    sleep 3
    else
        info "/mnt/boot/usr/sbin/lvm.static not found: skipping LVM2 volume group activation!"
    fi

for g in `ls /dev/store`
do
   info "Partition in dev/store: $g"
done

if [ ! -d /dev/store ]; then
   fail "Failed to start LVM"
fi

