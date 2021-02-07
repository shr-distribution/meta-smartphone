#!/bin/sh

# machine.conf should provide $system_partition (for panic scenario)
. /machine.conf

# distro.conf should provide $distro_name
. /distro.conf

# import Halium script
. /functions
. /halium-boot.sh

# This sets up the USB with whatever USB_FUNCTIONS are set to via configfs
USB_FUNCTIONS=adb
ANDROID_USB=/sys/class/android_usb/android0
GADGET_DIR=/config/usb_gadget

write() {
	echo -n "$2" >"$1"
}

usb_setup_configfs() {
    mkdir -p /config
    mount -t configfs none /config || true

    mkdir $GADGET_DIR/g1
    write $GADGET_DIR/g1/idVendor                   "0x18D1"
    write $GADGET_DIR/g1/idProduct                  "0xD001"
    mkdir $GADGET_DIR/g1/strings/0x409
    write $GADGET_DIR/g1/strings/0x409/serialnumber "$1"
    write $GADGET_DIR/g1/strings/0x409/manufacturer "Halium initrd"
    write $GADGET_DIR/g1/strings/0x409/product      "Failed to boot"

    if echo $USB_FUNCTIONS | grep -q "rndis"; then
        mkdir $GADGET_DIR/g1/functions/rndis.usb0
        mkdir $GADGET_DIR/g1/functions/rndis_bam.rndis
    fi
    echo $USB_FUNCTIONS | grep -q "mass_storage" && mkdir $GADGET_DIR/g1/functions/storage.0
    echo $USB_FUNCTIONS | grep -q "adb" && mkdir $GADGET_DIR/g1/functions/ffs.adb

    mkdir $GADGET_DIR/g1/configs/c.1
    mkdir $GADGET_DIR/g1/configs/c.1/strings/0x409
    write $GADGET_DIR/g1/configs/c.1/strings/0x409/configuration "$USB_FUNCTIONS"

    if echo $USB_FUNCTIONS | grep -q "rndis"; then
        ln -s $GADGET_DIR/g1/functions/rndis.usb0 $GADGET_DIR/g1/configs/c.1
        ln -s $GADGET_DIR/g1/functions/rndis_bam.rndis $GADGET_DIR/g1/configs/c.1
    fi
    echo $USB_FUNCTIONS | grep -q "mass_storage" && ln -s $GADGET_DIR/g1/functions/storage.0 $GADGET_DIR/g1/configs/c.1
    echo $USB_FUNCTIONS | grep -q "adb" && ln -s $GADGET_DIR/g1/functions/ffs.adb $GADGET_DIR/g1/configs/c.1
}

# This sets up the USB with whatever USB_FUNCTIONS are set to via android_usb
usb_setup_android_usb() {
    write $ANDROID_USB/enable          0
    write $ANDROID_USB/functions       ""
    write $ANDROID_USB/enable          1
    usleep 500000 # 0.5 delay to attempt to remove rndis function
    write $ANDROID_USB/enable          0
    write $ANDROID_USB/idVendor        18D1
    write $ANDROID_USB/idProduct       D001
    write $ANDROID_USB/iManufacturer   "Halium initrd"
    write $ANDROID_USB/iProduct        "Failed to boot"
    write $ANDROID_USB/iSerial         "$1"
    write $ANDROID_USB/f_ffs/aliases   adb
    write $ANDROID_USB/functions       ffs
    write $ANDROID_USB/enable          1
}

# This determines which USB setup method is going to be used

setup_devtmpfs() {
    mount -t devtmpfs -o mode=0755,nr_inodes=0 devtmpfs $1/dev
    # Create additional nodes which devtmpfs does not provide
    test -c $1/dev/fd || ln -sf /proc/self/fd $1/dev/fd
    test -c $1/dev/stdin || ln -sf fd/0 $1/dev/stdin
    test -c $1/dev/stdout || ln -sf fd/1 $1/dev/stdout
    test -c $1/dev/stderr || ln -sf fd/2 $1/dev/stderr
    test -c $1/dev/socket || mkdir -m 0755 $1/dev/socket
    test -e $1/dev/pts || mkdir -m 0755 -p $1/dev/pts
    test -e $1/dev/pts/0 || mount -t devpts devpts $1/dev/pts
}

panic() {
    tell_kmsg "$distro_name initramfs failed:"
    tell_kmsg "$1"
    #tell_kmsg "Waiting for 15 seconds before rebooting"
    #sleep 15s
    #reboot

    if [ -d $ANDROID_USB ]; then
        usb_setup_android_usb "Halium/LuneOS-initrd-functionfs"
    else
        usb_setup_configfs "Halium/LuneOS-initrd-configfs"
    fi
    
    mkdir -p /dev/usb-ffs/adb
    mount -o uid=2000,gid=2000 -t functionfs adb /dev/usb-ffs/adb
    
    usleep 500000
    
    # adbd has to be started before gadget is configured
    /usr/bin/adbd &
    
    usleep 500000
    
    [ -e $GADGET_DIR/g1/UDC ] && write $GADGET_DIR/g1/UDC "$(ls /sys/class/udc)"
    
    /bin/sh
}

mount_kernel_modules() {
    # Avoid overriding kernel modules in LuneOS
    tell_kmsg "Skip overriding of kernel modules"
}

start_mdev() {
    echo /sbin/mdev > /sys/kernel/uevent_helper
    /sbin/mdev -s > /dev/kmsg
}

stop_mdev() {
    killall mdev
    echo "" > /sys/kernel/uevent_helper
}

process_bind_mounts() {
    # We need to mount some directories read-write in order to have a working
    # system so bind mount them from the outside into the rootfs. If we're
    # doing this the first time we have to remove the old data and copy the
    # initial data
    
    # NOTE: for /var it's a bit more complex, as halium can
    # mount or extract some pieces to /var/lib/lxc/android/rootfs.
    # So have to exclude that folder from the duplication.
    datadir=${rootmnt}/userdata/$distro_name-data
    tell_kmsg "Preparing $datadir"

    if [ ! -e $datadir/.firstboot_done ] ; then
        tell_kmsg "First boot detected: binding /var and /home to a read-write copy"
        
        echo "var/lib/lxc/android" > /to_exclude.txt
        for dir in var home ; do
            rm -rf $datadir/$dir
            mkdir -p $datadir/$dir

            # Copy initial content to new location outside rootfs
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
    fi

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
}

quiet="n"

mkdir -m 0755 /rfs
rootmnt=/rfs

mkdir -m 0755 /proc
mount -t proc proc /proc
mkdir -m 0755 /sys
mount -t sysfs sys /sys
mkdir -p /dev

setup_devtmpfs ""

# Check whether we need to boot recovery
cat /proc/cmdline | grep skip_initramfs
if [ $? -eq 1 ] && [ -f /recovery/init ] ; then
    echo "skip_initramfs not found in cmdline. Booting into recovery." > /dev/kmsg

    # mount --bind trick doesn't seem to work with switch_root, using tmpfs
    mount -t tmpfs -o size=40M tmpfs ${rootmnt}
    cp -rf /recovery/* ${rootmnt}/
    exec switch_root ${rootmnt} /init "$@"
fi

echo "======= LuneOS/Halium ===========" > /dev/kmsg

# Check wether we need to start adbd for interactive debugging
cat /proc/cmdline | grep enable_adb
if [ $? -ne 1 ] ; then
    panic "Initramfs Debug Mode"
fi

echo "Starting mdev" > /dev/kmsg
start_mdev

# Disable busybox's over-restrictive behavior with cpio extraction
export EXTRACT_UNSAFE_SYMLINKS=1

# Call Halium's mount script
mountroot

tell_kmsg "Stopping mdev"
stop_mdev

tell_kmsg "Umounting unneeded filesystems"
umount -l /proc
umount -l /sys

tell_kmsg "Setup the user data directory"
# finally setup the user data directory
mount -o bind,rw $datadir/userdata ${rootmnt}/media/internal
mount -o bind,rw $datadir/userdata/.cryptofs ${rootmnt}/media/cryptofs

tell_kmsg "Switching to root filesystem"
exec switch_root ${rootmnt} /sbin/init
