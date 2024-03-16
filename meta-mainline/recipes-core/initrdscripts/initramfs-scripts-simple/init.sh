#! /bin/sh

. /init_functions.sh

export PATH=$PATH:/sbin:/bin:/usr/sbin:/usr/bin

echo "Mounting pseudo-filesystems"

mkdir -p -m 0755 /proc
mkdir -p -m 0755 /sys
mkdir -p /dev

# mount basic virtual fs
mount_proc_sys_dev_configfs ""
echo "Populate /dev thanks to mdev"
start_mdev
# redirect log
# setup_log

echo "/proc/cmdline:"
cat /proc/cmdline

### Detect recovery mode ###
RECOVERYMODE=no
# extract input device for key testing
# if recovery_trigger_input is empty, key-state will exit with an error
recovery_trigger_input=$(cat /proc/cmdline | grep recovery_trigger_input= | sed -E 's/.*recovery_trigger_input=([^ ]+).*/\1/')
if [ -n "$recovery_trigger_input" ]; then
    echo "Recovery trigger input: $recovery_trigger_input"
fi
if grep -q bootmode=recovery /proc/cmdline || key-state $recovery_trigger_input ; then
    RECOVERYMODE=yes
fi
echo "Recovery mode: $RECOVERYMODE"
###

setup_usb_network 172.16.42.2/16

if [ -f /scripts/local-premount/ORDER ]; then
    . /scripts/local-premount/ORDER
fi

if [ "$RECOVERYMODE" = "yes" ] ; then
    # Add root user
    cat > /etc/passwd << "EOF"
root::0:0:root:/root:/bin/sh
EOF
    
    # start telnetd for this IP
    start_telnetd 172.16.42.2
    
    # start minimalist recovery UI, and have a shell as fallback
    /usr/bin/luneos_recovery_ui ||
    /sbin/getty -L ttyS0 115200 linux
   
else
    # mount partition labeled "luneos-rootfs"
    mount_root_partition "luneos-root" "/rfs"

    mount_proc_sys_dev_configfs "/rfs"

    #info "Stopping debug services"
    #stop_telnetd
    stop_mdev

    if [ -f /scripts/local-bottom/ORDER ]; then
        . /scripts/local-bottom/ORDER
    fi


    info "Umounting unneeded filesystems"
    umount_proc_sys_dev_configfs ""

    info "Switching to root filesystem"
    exec switch_root /rfs /sbin/init
fi
