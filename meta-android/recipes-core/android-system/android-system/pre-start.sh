#!/bin/sh

if [ -e /android/init ]; then
	echo "System-as-root, only bind-mounting sub-mounts of /android/"
	cat /proc/self/mounts | while read line; do
		set -- $line
		# Skip any unwanted entry
		echo $2 | egrep -q "^/android/" || continue
		desired_mount=${2/\/android/}
		mount --bind $2 $LXC_ROOTFS_PATH/$desired_mount
	done

	rm -rf /dev/__properties__
	mkdir -p /dev/__properties__
	if [ -e /dev/disk/by-partlabel/persist ]; then
		mkdir -p /mnt/vendor/persist && mount /dev/disk/by-partlabel/persist /mnt/vendor/persist
	fi
else
	for mountpoint in /android/*; do
		mount_name=$(basename $mountpoint)
		desired_mount=$LXC_ROOTFS_PATH/$mount_name
	
		# Remove symlinks, for example bullhead has /vendor -> /system/vendor
		[ -L $desired_mount ] && rm $desired_mount

		[ -d $desired_mount ] || mkdir $desired_mount
		mount --bind $mountpoint $desired_mount
	done

	[ ! -e $LXC_ROOTFS_PATH/dev/null ] && mknod -m 666 $LXC_ROOTFS_PATH/dev/null c 1 3

	# Create /dev/pts if missing
	mkdir -p $LXC_ROOTFS_PATH/dev/pts

	# Pass /sockets through
	mkdir -p /dev/socket $LXC_ROOTFS_PATH/socket
	mount -n -o bind,rw /dev/socket $LXC_ROOTFS_PATH/socket

	rm $LXC_ROOTFS_PATH/sbin/adbd

	sed -i '/on early-init/a \    mkdir /dev/socket\n\    mount none /socket /dev/socket bind' $LXC_ROOTFS_PATH/init.rc

	sed -i "/mount_all /d" $LXC_ROOTFS_PATH/init.*.rc
	sed -i "/swapon_all /d" $LXC_ROOTFS_PATH/init.*.rc
	sed -i "/on nonencrypted/d" $LXC_ROOTFS_PATH/init.rc

fi

# Config snippet scripts.
#
# These used to live inside the legacy branch above, so on a system-as-root
# image - which is what every GSI is - none of them ran at all. Ubuntu Touch
# runs seven hooks here on every layout (no-adbd, no-surface-flinger,
# process-overrides and friends) and Droidian does the same; LuneOS silently ran
# zero. Device adaptations have nowhere else to hook in, so run them in both
# layouts.
#
# Hooks that rewrite init.rc can only work when the Android image is writable.
# A GSI is loop-mounted read-only unless .writable_device_image is set, so tell
# the hooks which case they are in rather than letting each one fail at sed.
if [ -w "$LXC_ROOTFS_PATH/init.rc" ]; then
    LXC_ROOTFS_WRITABLE=1
else
    LXC_ROOTFS_WRITABLE=0
    echo "Android rootfs is read-only; hooks that patch init.rc will skip"
fi
export LXC_ROOTFS_WRITABLE LXC_ROOTFS_PATH

run-parts /var/lib/lxc/android/pre-start.d || true
