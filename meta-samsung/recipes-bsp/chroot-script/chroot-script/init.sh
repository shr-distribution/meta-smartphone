#!/bin/sh

sleep 5
echo "Mounting relevant filesystems ..."
mkdir -m 0755 /proc
mount -t proc proc /proc
mkdir -m 0755 /sys
mount -t sysfs sys /sys
mkdir -p /dev
mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs /dev

# Create additional nodes which devtmpfs does not provide
test -c /dev/fd || ln -s /proc/self/fd /dev/fd
test -c /dev/stdin || ln -s fd/0 /dev/stdin
test -c /dev/stdout || ln -s fd/1 /dev/stdout
test -c /dev/stderr || ln -s fd/2 /dev/stderr

fail() {
    echo "Failed"
    echo "$1"
    echo "Waiting for 15 seconds before rebooting ..."
    sleep 15s
    reboot
}

while [ ! -e /sys/block/mmcblk0 ] ; do
    echo "Waiting for SD Card"
    sleep 1
done

partition=mmcblk0p3

# Try unpartitioned card
if [ ! -e /sys/block/mmcblk0/$partition ] ; then
    partition=mmcblk0
fi

echo "Mounting sdcard/nand ..."
mkdir -m 0777 /sdcard
mount -t vfat -o fmask=0000,dmask=0000,rw,flush,noatime,nodiratime /dev/$partition /sdcard
[ $? -eq 0 ] || fail "Failed to mount the SD card. Cannot continue."

mkdir -m 0755 /rfs
echo "Checking for rootfs image on sdcard/nand ..."
if [ -e /sdcard/linux/rootfs.ext2 ] ; then
	echo "Rootfs image found; mounting it now ..."
	losetup /dev/loop2 /sdcard/linux/rootfs.ext2
	[ $? -eq 0 ] || fail "Failed to find rootfs.img on SD Card!"
	e2fsck -y /dev/loop2
	mount -t ext2 -o noatime,nodiratime,sync,rw /dev/loop2 /rfs
	[ $? -eq 0 ] || fail "Failed to mount /rootfs"
elif [ -d /sdcard/linux/rootfs ] ; then
	echo "Rootfs folder found; chrooting into ..."
	mount -o bind /sdcard/linux/rootfs /rfs
	[ $? -eq 0 ] || fail "Failed to mount /rootfs"
fi

echo "Umount not needed filesystems ..."
umount -l /proc
umount -l /sys

echo "Switching to rootfs..."
exec switch_root /rfs /sbin/init
