#!/bin/sh

info "[initramfs] S01-mount-boot: starting"

# Check if mmcblk0p13 exists
if [ ! -b /dev/mmcblk0p13 ]; then
    info "[initramfs] Waiting for mmcblk0p13..."
    sleep 2
    if [ ! -b /dev/mmcblk0p13 ]; then
        info "[initramfs] Available block devices:"
        ls -la /dev/mmcblk* 2>/dev/null
        fail "/dev/mmcblk0p13 not found"
    fi
fi

# mount boot partition:
mkdir -p /mnt/boot
mount -o ro /dev/mmcblk0p13 /mnt/boot
if [ $? -ne 0 ]; then
    fail "Failed to mount /dev/mmcblk0p13 on /mnt/boot"
fi

info "[initramfs] mmcblk0p13 mounted."

# use lvm.static binary file to initialize /dev/store
[ ! -d /var/lock ] && mkdir -p /var/lock
[ ! -d /run/lock ] && mkdir -p /run/lock

if [ -e /mnt/boot/usr/sbin/lvm.static ]; then
    info "[initramfs] Activating LVM..."
    export LVM_SYSTEM_DIR=/mnt/boot/etc/lvm
    info "LVM_SYSTEM_DIR: $LVM_SYSTEM_DIR"
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay 2>&1
    LVM_RC=$?
    info "[initramfs] LVM vgchange first attempt: rc=$LVM_RC"
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay 2>&1
    LVM_RC=$?
    info "[initramfs] LVM vgchange second attempt: rc=$LVM_RC"
    sleep 3
else
    info "/mnt/boot/usr/sbin/lvm.static not found: skipping LVM2 volume group activation!"
fi

if [ -d /dev/store ]; then
    info "[initramfs] LVM volumes found in /dev/store:"
    for g in $(ls /dev/store); do
        info "  /dev/store/$g"
    done
else
    info "[initramfs] /dev/store does not exist after LVM activation"
    info "[initramfs] Available /dev/mapper entries:"
    ls -la /dev/mapper/ 2>/dev/null
    info "[initramfs] Available /dev/dm-* entries:"
    ls -la /dev/dm-* 2>/dev/null
    fail "Failed to start LVM - /dev/store not found"
fi

