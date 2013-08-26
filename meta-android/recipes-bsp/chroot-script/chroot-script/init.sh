#! /bin/sh

. /machine.conf

setup_devtmpfs() {
    mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs $1/dev
    # Create additional nodes which devtmpfs does not provide
    test -c $1/dev/fd || ln -sf /proc/self/fd $1/dev/fd
    test -c $1/dev/stdin || ln -sf fd/0 $1/dev/stdin
    test -c $1/dev/stdout || ln -sf fd/1 $1/dev/stdout
    test -c $1/dev/stderr || ln -sf fd/2 $1/dev/stderr
}

echo "Mounting relevant filesystems ..."
mkdir -m 0755 /proc
mount -t proc proc /proc
mkdir -m 0755 /sys
mount -t sysfs sys /sys
mkdir -p /dev

setup_devtmpfs ""

info() {
    echo "$1" > /dev/ttyprintk
}

fail() {
    echo "Failed" > /dev/ttyprintk
    echo "$1" > /dev/ttyprintk
    echo "Waiting for 15 seconds before rebooting ..." > /dev/ttyprintk
    sleep 15s
    reboot
}

# Check wether we need to start adbd for interactive debugging
cat /proc/cmdline | grep enable_adb
if [ $? -ne 1 ] ; then
    /usr/bin/android-gadget-setup adb
    /usr/bin/adbd
fi


mkdir -m 0755 /rfs

while [ ! -e /sys/block/mmcblk0 ] ; do
    info "Waiting for sdcard/nand ..."
    sleep 1
done

# Try unpartitioned card
if [ ! -e /sys/block/mmcblk0/$sdcard_partition ] ; then
    sdcard_partition=mmcblk0
fi

info "Mounting sdcard/nand ..."
mkdir -m 0777 /sdcard
mount -t auto -o rw,noatime,nodiratime /dev/$sdcard_partition /sdcard
[ $? -eq 0 ] || fail "Failed to mount the sdcard/nan. Cannot continue."

BASE_ANDROID_DIR="/sdcard/media/"

# Workaround for multi-user functionality in Android 4.2
if [ -d /sdcard/media/0 ] ; then
    BASE_ANDROID_DIR="/sdcard/media/0"
fi

if [ -e $BASE_ANDROID_DIR/linux/installer ] ; then
    sh $BASE_ANDROID_DIR/linux/installer

    # When we're done with the installation process we're removing the installer script to
    # not install everything again on next boot
    rm $BASE_ANDROID_DIR/linux/installer
fi

info "Checking for rootfs image on sdcard/nand ..."
if [ -e $BASE_ANDROID_DIR/linux/rootfs.ext2 ] ; then
    info "Rootfs image found; mounting it now ..."
    losetup /dev/loop2 $BASE_ANDROID_DIR/linux/rootfs.ext2
    [ $? -eq 0 ] || fail "Failed to find rootfs.img on SD Card!"
    e2fsck -y /dev/loop2
    mount -t ext2 -o noatime,nodiratime,sync,rw /dev/loop2 /rfs
    [ $? -eq 0 ] || fail "Failed to mount /rootfs"
elif [ -d $BASE_ANDROID_DIR/linux/rootfs ] ; then
    info "Rootfs folder found; chrooting into ..."
    mount -o bind $BASE_ANDROID_DIR/linux/rootfs /rfs
    [ $? -eq 0 ] || fail "Failed to mount /rootfs"
else
    # We don't have anything to boot from sdcard. Cleanup and boot
    # from system partition.
    umount /sdcard

    mount -t auto -o rw,noatime,nodiratime /dev/$system_partition /rfs
    [ $? -eq 0 ] || fail "Failed to mount /rootfs"
fi

setup_devtmpfs "/rfs"

info "Umount not needed filesystems ..."
umount -l /proc
umount -l /sys

info "Switching to rootfs..."
exec switch_root /rfs /sbin/init
