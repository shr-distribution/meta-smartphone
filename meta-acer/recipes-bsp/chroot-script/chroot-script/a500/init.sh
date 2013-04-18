#! /bin/sh

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
test -c /dev/input/touchscreen0 || ln -s /dev/input/event2 /dev/input/touchscreen0

fail() {
    echo "Failed" > /dev/ttyprintk
    echo "$1" > /dev/ttyprintk
    echo "Waiting for 15 seconds before rebooting ..." > /dev/ttyprintk
    sleep 15s
    reboot
}


while [ ! -e /sys/block/mmcblk1 ] ; do
    echo "Waiting for SD Card" > /dev/ttyprintk
    sleep 1
done

partition=mmcblk1p2

echo "Mounting sdcard ..." > /dev/ttyprintk
mkdir -m 0755 /rfs
mount -t auto -o rw,noatime,nodiratime /dev/$partition /rfs
[ $? -eq 0 ] || fail "Failed to mount the SD card. Cannot continue."

echo "Umount not needed filesystems ..." > /dev/ttyprintk

mount --move /sys /rfs/sys
mount --move /proc /rfs/proc
mount --move /dev /rfs/dev

#umount -l /proc
#umount -l /sys

echo "Switching to rootfs..."

exec switch_root /rfs /sbin/init

