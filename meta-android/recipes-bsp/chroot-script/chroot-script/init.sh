#! /bin/sh

sleep 5

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

setup_from_sdcard() {
    while [ ! -e /sys/block/mmcblk0 ] ; do
        info "Waiting for SD Card"
        sleep 1
    done

    # Try unpartitioned card
    if [ ! -e /sys/block/mmcblk0/$partition ] ; then
        partition=mmcblk0
    fi

    info "Mounting sdcard/nand ..."
    mkdir -m 0777 /sdcard
    mount -t auto -o rw,noatime,nodiratime /dev/$partition /sdcard
    [ $? -eq 0 ] || fail "Failed to mount the SD card. Cannot continue."

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
    fi
}

mkdir -m 0755 /rfs
setup_from_sdcard
setup_devtmpfs "/rfs"

info "Umount not needed filesystems ..."
umount -l /proc
umount -l /sys

info "Switching to rootfs..."
exec switch_root /rfs /sbin/init
