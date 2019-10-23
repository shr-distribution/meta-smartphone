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

# start_telnetd

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
mount_proc_sys_dev_configfs ""

info "Switching to root filesystem"
exec switch_root /rfs /sbin/init
