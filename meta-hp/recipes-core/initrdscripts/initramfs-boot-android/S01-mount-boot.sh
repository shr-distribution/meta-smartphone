#!/bin/sh

# mount boot partition:
mkdir -p /mnt/boot
mount /dev/mmcblk0p13 /mnt/boot

# use lvm.static binary file to initialize /dev/store
[ ! -d /var/lock ] && mkdir -p /var/lock
[ ! -d /run/lock ] && mkdir -p /run/lock

export LVM_SYSTEM_DIR=/mnt/boot/etc/lvm
/mnt/boot/usr/sbin/lvm.static vgchange -ay

if [ ! -d /dev/store ]; then
   panic "Failed to start LVM"
fi

# initialize the "syspart" variable to the correct path
syspart=/dev/store/ext3fs
if [ -e /dev/store/${distro_name}-root ]; then
    syspart=/dev/store/${distro_name}-root
fi
# Halium hesitates a bit between these two names: set them both.
_syspart=$syspart

if [ ! -e $syspart ]; then
    panic "Failed to find root partition !"
fi

# create a file corresponding to Halium's naming criteria for userdata partition
ln -s media /dev/store/userdata

# override process_bind_mounts(), because in our case userdata is vfat
# So the symlinks won't be allowed in there
process_bind_mounts() {
    tell_kmsg "userdata is vfat: override process_bind_mounts"
    datadir=${rootmnt}/android/userdata/$distro_name-data
    mkdir -p $datadir/userdata/.cryptofs

    # Copy initial media to userdata
    mkdir -p $datadir/userdata
    cp -ra ${rootmnt}/media/internal/* $datadir/userdata/
}
