#!/bin/sh

echo "This startup script was originally created by Martin J. Johnson for the HTC Vogue device."
echo  "
####################################################################################################
#                                                                                                  #
#                              WELCOME TO UBUNTU LINUX ON THE HTC HD2                              #
#                                               v0.3                                               #
#                                                                                                  #
#                             See included readme.txt for instructions                             #
#                                                                                                  #
#                              Lukas-David Gorris, December 27th 2010                              #
#                                                                                                  #
####################################################################################################
"
sleep 5
mkdir -m 0755 /proc
mount -t proc proc /proc
mkdir -m 0755 /sys
mount -t sysfs sys /sys
mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs /dev

# Create additional nodes which devtmpfs does not provide
test -c /dev/fd || ln -s /proc/self/fd /dev/fd
test -c /dev/stdin || ln -s fd/0 /dev/stdin
test -c /dev/stdout || ln -s fd/1 /dev/stdout
test -c /dev/stderr || ln -s fd/2 /dev/stderr

fail() {
    echo "Failed"
    echo "$1"
    exec /bin/sh
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

mkdir -m 0777 /sdcard
mount -t vfat -o fmask=0000,dmask=0000,rw,flush,noatime,nodiratime /dev/$partition /sdcard
[ $? -eq 0 ] || fail "Failed to mount the SD card. Cannot continue."

mkdir -m 0755 /rfs
if [ -e /sdcard/linux/rootfs.ext2 ] ; then
	losetup /dev/block/loop2 /sdcard/linux/rootfs.ext2
	[ $? -eq 0 ] || fail "Failed to find rootfs.img on SD Card. You need to unzip a rootfs zip file to the root of your SD card."
	e2fsck -y /dev/block/loop2
	mount -t ext2 -o noatime,nodiratime,sync,rw /dev/block/loop2 /rfs
	[ $? -eq 0 ] || fail "Failed to mount /rootfs"
fi

umount -l /proc
umount -l /sys

echo "Switching to rootfs..."
exec switch_root /rfs /sbin/init
