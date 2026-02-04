#! /bin/sh

. /init_functions.sh

export PATH=$PATH:/sbin:/bin:/usr/sbin:/usr/bin

echo "Mounting pseudo-filesystems"

mkdir -p -m 0755 /proc
mkdir -p -m 0755 /sys
mkdir -p /dev

# mount basic virtual fs
mount_proc_sys_dev_configfs ""

# Mount devtmpfs for automatic device node creation by kernel
echo "Mounting devtmpfs..."
mount -t devtmpfs devtmpfs /dev 2>/dev/null || echo "devtmpfs mount failed (may already be mounted)"

echo "Populate /dev thanks to mdev"
start_mdev

# Give kernel time to probe devices (eMMC, USB controller)
echo "Waiting for device initialization..."
sleep 2
# redirect log
# setup_log

echo "/proc/cmdline:"
cat /proc/cmdline

### Setup persistent debug log ###
# Mount boot partition early for persistent logging (before USB setup)
# This allows debugging USB issues even without serial console
DEBUGLOG=""
BOOT_DEV=""
# Wait for and find boot partition - try common locations
echo "Looking for boot partition..."
for i in 1 2 3 4 5; do
    for dev in /dev/mmcblk0p13 /dev/mmcblk0p1 /dev/sda1; do
        if [ -b "$dev" ]; then
            BOOT_DEV="$dev"
            break 2
        fi
    done
    echo "Waiting for block devices... ($i)"
    sleep 1
done

if [ -n "$BOOT_DEV" ]; then
    echo "Found boot device: $BOOT_DEV"
    mkdir -p /mnt/boot
    if mount -o rw "$BOOT_DEV" /mnt/boot 2>/dev/null; then
        DEBUGLOG=/mnt/boot/initramfs-debug.log
        echo "=== Initramfs boot log $(date 2>/dev/null || echo 'unknown') ===" > $DEBUGLOG
        echo "Persistent debug log enabled at $DEBUGLOG"
    else
        echo "Failed to mount $BOOT_DEV"
    fi
else
    echo "WARNING: No boot partition found for debug logging"
    echo "Available block devices:"
    ls -la /dev/mmcblk* /dev/sd* 2>/dev/null || echo "  (none)"
fi
export DEBUGLOG
###

### Detect debug mode ###
DEBUGMODE=no
if grep -q luneos_debug /proc/cmdline; then
    DEBUGMODE=yes
    info "Debug mode enabled via luneos_debug cmdline"
    set -x
fi
###

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

# In debug mode, start telnet early so we can observe the entire boot
if [ "$DEBUGMODE" = "yes" ]; then
    info "Starting early telnetd for debug..."
    start_telnetd 172.16.42.2
    info "Debug telnet available at 172.16.42.2"
    info "Waiting 10s for debug connection before continuing boot..."
    sleep 10
fi

if [ -f /scripts/local-premount/ORDER ]; then
    info "Running local-premount hooks..."
    . /scripts/local-premount/ORDER
    info "local-premount hooks done"
fi

if [ "$RECOVERYMODE" = "yes" ] ; then
    # start telnetd for this IP (skip if already started in debug mode)
    if [ "$DEBUGMODE" != "yes" ]; then
        start_telnetd 172.16.42.2
    fi

    # start minimalist recovery UI, and have a shell as fallback
    /usr/bin/luneos_recovery_ui ||
    /sbin/getty -L ttyS0 115200 linux

else
    info "Searching for rootfs partition..."
    # mount partition labeled "luneos-rootfs"
    mount_root_partition "luneos-root" "/rfs"

    info "Moving/mounting pseudo-filesystems to rootfs..."
    # Mount fresh proc and sys for the new root
    mkdir -p /rfs/proc /rfs/sys /rfs/config
    mount -t proc -o nodev,noexec,nosuid proc /rfs/proc
    mount -t sysfs -o nodev,noexec,nosuid sysfs /rfs/sys
    # MOVE configfs (don't create new) to preserve USB gadget configuration
    mount --move /config /rfs/config

    #info "Stopping debug services"
    #stop_telnetd
    stop_mdev

    if [ -f /scripts/local-bottom/ORDER ]; then
        info "Running local-bottom hooks..."
        . /scripts/local-bottom/ORDER
        info "local-bottom hooks done"
    fi

    info "Umounting unneeded filesystems"
    # Only unmount proc and sys; keep configfs mounted so the USB gadget
    # (ECM network) stays alive through switch_root.
    umount -l /sys
    umount -l /proc

    if [ "$DEBUGMODE" = "yes" ]; then
        info "========================================"
        info "Debug: rootfs contents:"
        ls -la /rfs/ 2>/dev/null
        info "Debug: /rfs/sbin/init:"
        ls -la /rfs/sbin/init 2>/dev/null
        info "Debug: rootfs mount:"
        mount | grep rfs
        info "========================================"
        info "Debug: telnet is still running at 172.16.42.2"
        info "Debug: connect now to inspect rootfs at /rfs"
        info "Debug: waiting 10s before switch_root..."
        info "Debug: to skip wait, create /tmp/continue"
        info "========================================"
        waited=0
        while [ $waited -lt 10 ]; do
            [ -f /tmp/continue ] && break
            sleep 1
            waited=$((waited + 1))
        done
        info "Debug: proceeding with switch_root"
        stop_telnetd
    fi

    info "Switching to root filesystem"
    # Sync debug log to disk before switch_root
    [ -n "$DEBUGLOG" ] && sync
    exec switch_root /rfs /sbin/init

    # If switch_root fails we end up here
    fail "switch_root to /rfs failed"
fi
