#! /bin/sh

. /machine.conf
. /distro.conf

info() {
    echo "INFO: $1" > /dev/kmsg
}

fail() {
    echo "FAILED: $1" > /dev/kmsg
    echo "Waiting for 15 seconds before rebooting ..." > /dev/kmsg
    sleep 15s
    reboot
}

setup_devtmpfs() {
    # mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs $1/dev
    mount -t devtmpfs devtmpfs $1/dev
    # Create additional nodes which devtmpfs does not provide
    test -c $1/dev/fd || ln -sf /proc/self/fd $1/dev/fd
    test -c $1/dev/stdin || ln -sf fd/0 $1/dev/stdin
    test -c $1/dev/stdout || ln -sf fd/1 $1/dev/stdout
    test -c $1/dev/stderr || ln -sf fd/2 $1/dev/stderr
}

mkdir -m 0755 /proc
mount -t proc proc /proc
mkdir -m 0755 /sys
mount -t sysfs sys /sys

info "Setting up devtmpfs ..."
setup_devtmpfs ""

info "Mounting rootfs ..."

mkdir -p /mnt/boot
mount /dev/mmcblk0p13 /mnt/boot

export LVM_SYSTEM_DIR=/mnt/boot/etc/lvm
/mnt/boot/usr/sbin/lvm.static vgchange -ay

if [ ! -d /dev/store ]
then
   fail "Failed to start LVM"
fi

mkdir -m 0755 /rfs
mount /dev/store/ext3fs /rfs

info "Done mounting rootfs!"

# Disable MPDEC for now until android layer starts up and make sure atleast
# one CPU stays online to not hangup the boot
echo 0 > /sys/kernel/msm_mpdecision/conf/enabled
echo 1 > /sys/devices/system/cpu/cpu1/online
echo 0 > /sys/module/pm_8x60/modes/cpu0/power_collapse/idle_enabled

umount -l /sys
umount -l /proc

info "Setting up rootfs and switching to it ..."
setup_devtmpfs "/rfs"

info "Switching to rootfs ..."
exec switch_root /rfs /sbin/init
