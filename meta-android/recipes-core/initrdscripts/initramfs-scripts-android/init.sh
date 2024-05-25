#! /bin/sh

. /machine.conf
. /distro.conf

. /init_functions.sh

export PATH=$PATH:/sbin:/bin:/usr/sbin:/usr/bin

echo "Mounting pseudo-filesystems"
mkdir -m 0755 /proc
mkdir -m 0755 /sys
mkdir -p /dev

# mount basic virtual fs
mount_proc_sys_dev_configfs ""
# populate /dev thanks to mdev
start_mdev
# redirect log
setup_log

# setup_framebuffer

setup_usb_network 172.16.42.2/16

# Check whether we need to boot recovery
if grep -qv skip_initramfs /proc/cmdline ; then
    # If there is a recovery image embedded, and we're *not* using lk2nd, boot it
    if grep -qv lk2nd /proc/cmdline && [ -f /recovery/init ] ; then
        echo "skip_initramfs not found in cmdline. Booting into recovery." > /dev/kmsg

        # mount --bind trick doesn't seem to work with switch_root, using tmpfs
        mount -t tmpfs -o size=100M tmpfs ${rootmnt}
        cp -rf /recovery/* ${rootmnt}/
        exec switch_root ${rootmnt} /init "$@"
    else
    # otherwise fallback to luneos-recovery-ui and telnet shell
    
        # Add root user
        cat > /etc/passwd << "EOF"
root::0:0:root:/root:/bin/sh
EOF
        # start telnetd for this IP
        start_telnetd 172.16.42.2
        
        # start minimalist recovery UI, and have a shell as fallback
        /usr/bin/luneos_recovery_ui ||
        /sbin/getty -L ttyS0 115200 linux
    fi
fi

mount_sdcard "/sdcard"

# Determine Android's media directory
ANDROID_MEDIA_DIR="/sdcard/media"
# Workaround for multi-user functionality in Android 4.2
if [ -d /sdcard/media/0 ] ; then
    ANDROID_MEDIA_DIR="/sdcard/media/0"
fi

# Eventually run an installer hook, if available
if [ -e $ANDROID_MEDIA_DIR/linux/installer ] ; then
    sh $ANDROID_MEDIA_DIR/linux/installer

    # When we're done with the installation process we're removing the installer script to
    # not install everything again on next boot
    rm $ANDROID_MEDIA_DIR/linux/installer
fi

mount_root_partition "/rfs"

mount_proc_sys_dev_configfs "/rfs"

#info "Stopping debug services"
#stop_telnetd
stop_mdev

info "Umounting unneeded filesystems"
umount_proc_sys_dev_configfs ""

info "Switching to root filesystem"
exec switch_root /rfs /sbin/init
