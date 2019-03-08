#! /bin/sh

. /machine.conf
. /distro.conf

setup_devtmpfs() {
    mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs $1/dev
    # Create additional nodes which devtmpfs does not provide
    test -c $1/dev/fd || ln -sf /proc/self/fd $1/dev/fd
    test -c $1/dev/stdin || ln -sf fd/0 $1/dev/stdin
    test -c $1/dev/stdout || ln -sf fd/1 $1/dev/stdout
    test -c $1/dev/stderr || ln -sf fd/2 $1/dev/stderr
}

echo "Mounting pseudo-filesystems"
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
    echo "$distro_name initramfs failed:" > /dev/ttyprintk
    echo "$1" > /dev/ttyprintk
    echo "Waiting for 15 seconds before rebooting" > /dev/ttyprintk
    sleep 15s
    reboot
}

# Check wether we need to start adbd for interactive debugging
cat /proc/cmdline | grep enable_adb
if [ $? -ne 1 ] ; then
    /usr/bin/android-gadget-setup adb
    /usr/bin/adbd
fi


sdcard_device=$( echo "$sdcard_partition" | sed -e 's/p[[:digit:]]\+$//' )
while [ ! -e /sys/block/$sdcard_device ] ; do
    info "Waiting for SD card/NAND"
    sleep 1
done

# Try unpartitioned card
if [ ! -e /sys/block/$sdcard_device/$sdcard_partition ] ; then
    sdcard_partition=$sdcard_device
fi

SDCARD_DIR="/sdcard"
info "Mounting SD card/NAND /dev/$sdcard_partition"
mkdir -m 0777 $SDCARD_DIR
mount -t auto -o rw,noatime,nodiratime /dev/$sdcard_partition $SDCARD_DIR
[ $? -eq 0 ] || fail "Failed to mount SD card/NAND, cannot continue"


ANDROID_MEDIA_DIR="$SDCARD_DIR/media"
# Workaround for multi-user functionality in Android 4.2
if [ -d $SDCARD_DIR/media/0 ] ; then
    ANDROID_MEDIA_DIR="$SDCARD_DIR/media/0"
fi

if [ -e $ANDROID_MEDIA_DIR/linux/installer ] ; then
    sh $ANDROID_MEDIA_DIR/linux/installer

    # When we're done with the installation process we're removing the installer script to
    # not install everything again on next boot
    rm $ANDROID_MEDIA_DIR/linux/installer
fi


if [ -z "$rootfs_type" ] ; then
    rootfs_type=ext2
fi
mkdir -m 0755 /rfs
info "Checking for root filesystem image on SD card/NAND"
if [ -e $ANDROID_MEDIA_DIR/linux/rootfs.$rootfs_type ] ; then
    info "Mounting root filesystem image $ANDROID_MEDIA_DIR/linux/rootfs.$rootfs_type"
    losetup /dev/loop2 $ANDROID_MEDIA_DIR/linux/rootfs.$rootfs_type
    [ $? -eq 0 ] || fail "Failed to create loop device for root filesystem image!"
    e2fsck -y /dev/loop2
    mount -t $rootfs_type -o noatime,nodiratime,sync,rw /dev/loop2 /rfs
    [ $? -eq 0 ] || fail "Failed to mount root filesystem image!"
elif [ -d $ANDROID_MEDIA_DIR/linux/rootfs ] ; then
    info "Chrooting into root filesystem folder $ANDROID_MEDIA_DIR/linux/rootfs"
    mount -o bind,rw $ANDROID_MEDIA_DIR/linux/rootfs /rfs
    [ $? -eq 0 ] || fail "Failed to bind mount $ANDROID_MEDIA_DIR/linux/rootfs"
elif [ -d $SDCARD_DIR/$distro_name ] ; then
    info "Chrooting into root filesystem folder $SDCARD_DIR/$distro_name"
    mount -o bind,rw $SDCARD_DIR/$distro_name /rfs
    [ $? -eq 0 ] || fail "Failed to bind mount $SDCARD_DIR/$distro_name"
else
    info "No root filesystem found on SD card/NAND; using /dev/$system_partition"

    # We don't have anything to boot from sdcard. Cleanup and boot
    # from system partition.
    umount $SDCARD_DIR

    mount -t auto -o rw,noatime,nodiratime /dev/$system_partition /rfs
    [ $? -eq 0 ] || fail "Failed to mount system partition /dev/$system_partition"
fi

setup_devtmpfs "/rfs"

info "Umounting unneeded filesystems"
umount -l /proc
umount -l /sys

info "Switching to root filesystem"
exec switch_root /rfs /sbin/init
