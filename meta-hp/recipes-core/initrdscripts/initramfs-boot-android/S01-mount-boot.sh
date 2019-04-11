#!/bin/sh

# mount boot partition:
mkdir -p /mnt/boot
mount -o ro /dev/mmcblk0p13 /mnt/boot

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
# Therefore, don't move /home and /var out of the system partition.
# Of course, it means the latter needs to be read-write on tenderloin.
process_bind_mounts() {
    tell_kmsg "userdata is vfat: override process_bind_mounts"
    datadir=${rootmnt}/userdata/$distro_name-data
    tell_kmsg "Preparing $datadir"
    if [ ! -e ${rootmnt}/.firstboot_done ] ; then
        mkdir -p $datadir/userdata
        # Copy initial media to userdata
        cp -ra ${rootmnt}/media/internal/* $datadir/userdata/

        # setup cryptofs which is not a real cryptofs yet
        if [ -d $datadir/userdata/.cryptofs ] ; then
            rm -rf $datadir/userdata/.cryptofs
        fi
        mkdir -p $datadir/userdata/.cryptofs

        # We're done with our first boot actions
        touch ${rootmnt}/.firstboot_done
    fi
}
