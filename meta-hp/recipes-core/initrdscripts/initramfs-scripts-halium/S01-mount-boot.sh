#!/bin/sh

# mount boot partition:
mkdir -p /mnt/boot
mount -o ro /dev/mmcblk0p13 /mnt/boot

# use lvm.static binary file to initialize /dev/store
[ ! -d /var/lock ] && mkdir -p /var/lock
[ ! -d /run/lock ] && mkdir -p /run/lock

if [ -e /mnt/boot/usr/sbin/lvm.static ]
    then
    tell_kmsg "[initramfs] Activating LVM..."
    export LVM_SYSTEM_DIR=/mnt/boot/etc/lvm 
    tell_kmsg "LVM_SYSTEM_DIR: $LVM_SYSTEM_DIR"
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay
    sleep 3
    /mnt/boot/usr/sbin/lvm.static vgchange -ay 2>&1 > /media/internal/lvm.txt
    sleep 3
    else
        tell_kmsg "/mnt/boot/usr/sbin/lvm.static not found: skipping LVM2 volume group activation!"
    fi

for g in `ls /dev/store`
do
   tell_kmsg "Partition in dev/store: $g"
done

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
    tell_kmsg "We are using process_bind_mounts from S01-mount-boot.sh"
    tell_kmsg "userdata is vfat: override process_bind_mounts"
    tell_kmsg "rootmnt: ${rootmnt}"
    datadir=${rootmnt}/userdata/$distro_name-data
    tell_kmsg "Preparing datadir: $datadir"
    if [ ! -e $datadir/.firstboot_done ] ; then
        tell_kmsg "First boot detected: binding /var and /home to a read-write copy"

        echo "var/lib/lxc/android" > /to_exclude.txt
    
        for dir in var home ; do
            rm -rf $datadir/$dir
            mkdir -p $datadir/$dir

            # Copy initial content to new location outside rootfs
            #cp -ra ${rootmnt}/$dir/* $datadir/$dir
            # Use 'tar' to be able to exclude /var/lib/lxc/android 
            tar -C ${rootmnt} -c -X /to_exclude.txt $dir | tar -x -C $datadir/
            # cp -ra ${rootmnt}/$dir/* $datadir/$dir
        done
        rm /to_exclude.txt
    
        mkdir -p $datadir/userdata
        # Copy initial media to userdata
        cp -ra ${rootmnt}/media/internal/* $datadir/userdata/

        # setup cryptofs which is not a real cryptofs yet
        if [ -d $datadir/userdata/.cryptofs ] ; then
            rm -rf $datadir/userdata/.cryptofs
        fi
        mkdir -p $datadir/userdata/.cryptofs

        # We're done with our first boot actions
        touch $datadir/.firstboot_done
        
        # before bind-mounting, keep a mount point to the original lxc-android copy
        mkdir -p $datadir/luneos-lxc-android
        mount -o bind ${rootmnt}/var/lib/lxc/android $datadir/luneos-lxc-android
        # this is also needed, in the scenario of a system-as-root mount
        mount --move ${rootmnt}/var/lib/lxc/android/rootfs $datadir/luneos-lxc-android/rootfs || true
        # point lxc android container to the read-only folder containing the configuration and the rootfs
        if [ ! -e $datadir/var/lib/lxc/android ]; then
            mkdir -p $datadir/var/lib/lxc
            ln -sf /userdata/luneos-data/luneos-lxc-android $datadir/var/lib/lxc/android
        fi
        
        tell_kmsg "Bind-mount the directories"
        # bind-mount the directories to their correct place
        for dir in var home ; do
            mount -o bind,rw $datadir/$dir ${rootmnt}/$dir
        done

    fi
}
