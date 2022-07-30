#!/bin/sh
# This file will be in /init_functions.sh inside the initramfs.

# Redirect stdout and stderr to logfile
setup_log() {
	# default redirect goes to /dev/kmsg
	[ -e /dev/kmsg ] && exec >/dev/kmsg 2>&1
	# Bail out if PMOS_NO_OUTPUT_REDIRECT is set
	echo "======= LuneOS initrd ==========="
	grep -q LUNEOS_NO_OUTPUT_REDIRECT /proc/cmdline && return

	# Print a message about what is going on to the normal output
	echo "NOTE: All output from the initramfs gets redirected to:"
	echo "/LuneOS_init.log"
	echo "If you want to disable this behavior (e.g. because you're"
	echo "debugging over serial), please add this to your kernel"
	echo "command line: LUNEOS_NO_OUTPUT_REDIRECT"

	# Start redirect, print the first line again
	exec >/LuneOS_init.log 2>&1
	echo "======= LuneOS initrd ==========="
}

info() {
    echo "$1"
}

fail() {
    echo "$distro_name initramfs failed:"
    echo "$1"
    echo "Waiting for 5 seconds before rebooting"
    sleep 5
    mkdir -p /boottmp
    #mount /dev/mmcblk0p1 /boottmp
    [ -e /LuneOS_init.log ] && cp /LuneOS_init.log /boottmp/LuneOS_init.log
    dmesg >> /boottmp/LuneOS_init.log
    #umount /boottmp
    loop_forever
}

# $1: root directory for mounts
mount_proc_sys_dev_configfs() {
	# mdev
	mount -t proc -o nodev,noexec,nosuid proc $1/proc
	mount -t sysfs -o nodev,noexec,nosuid sysfs $1/sys
	
	mkdir -p $1/config
	mount -t configfs -o nodev,noexec,nosuid configfs $1/config
}

# $1: root directory for mounts
umount_proc_sys_dev_configfs() {
	umount -l $1/config
	umount -l $1/sys
	umount -l $1/proc
}

start_mdev() {
    echo /sbin/mdev > /sys/kernel/uevent_helper
	mdev -s
}

stop_mdev() {
	killall mdev
    echo "" > /sys/kernel/uevent_helper
}

# $1: IP address to listen to
start_telnetd() {
	# /dev/pts (needed for telnet)
	mkdir /dev/pts
	mount -t devpts none /dev/pts
	
	echo "Starting telnetd..."
	/usr/sbin/telnetd -b $1
	echo "Pidof telnetd: $(pidof telnetd)"
}

stop_telnetd() {
	killall telnetd
	umount /dev/pts
}

# $1: label of the partition
# $2: target directory of mount
mount_root_partition() {
	partname=$1
	rfs=$2

	if [ -z "$partname" ] ; then
		partname="luneos-root"
	fi

    part=$(find /dev -name $partname* | tail -1)
    if [ -n "$part" ]; then
		rootfs_path=$(readlink -f $part)
    fi

	mkdir -p $rfs
	mount -t ext4 -o rw,noatime,nodiratime $rootfs_path $rfs
	[ $? -eq 0 ] || fail "Failed to mount rootfs partition $rootfs_path,$partname,$part on $rfs"
}

setup_usb_network_configfs() {
	# Only run, when we have the gadget usb driver
	CONFIGFS=/config/usb_gadget
	[ -e "$CONFIGFS" ] || return

	mkdir -p $CONFIGFS/g1

	echo 0x1d6b > $CONFIGFS/g1/idVendor # Linux Foundation
	echo 0x0104 > $CONFIGFS/g1/idProduct # Multifunction Composite Gadget
	echo 0x0100 > $CONFIGFS/g1/bcdDevice # v1.0.0
	echo 0x0200 > $CONFIGFS/g1/bcdUSB # USB2

	mkdir -p $CONFIGFS/g1/strings/0x409
	echo "fedcba9876543210" > $CONFIGFS/g1/strings/0x409/serialnumber
	echo "LuneOS" > $CONFIGFS/g1/strings/0x409/manufacturer 
	echo "LuneOS Device" > $CONFIGFS/g1/strings/0x409/product 

	N="usb0"
	mkdir -p $CONFIGFS/g1/functions/ecm.$N

	# first byte of address must be even
	HOST="FA:75:7F:BB:F4:E6" # "HostPC"
	echo $HOST > $CONFIGFS/g1/functions/ecm.$N/host_addr

	C=1
	mkdir -p $CONFIGFS/g1/configs/c.$C/strings/0x409
	echo "Config $C: ECM network" > $CONFIGFS/g1/configs/c.$C/strings/0x409/configuration 
	echo 250 > $CONFIGFS/g1/configs/c.$C/MaxPower 
	ln -s $CONFIGFS/g1/functions/ecm.$N          $CONFIGFS/g1/configs/c.$C/

    if grep -q bootmode=recovery /proc/cmdline; then
		# recovery mode: expose sdcard
		FILE=/dev/mmcblk0
		
		mkdir -p $CONFIGFS/g1/functions/mass_storage.$N

		echo 1 > $CONFIGFS/g1/functions/mass_storage.$N/stall
		echo 0 > $CONFIGFS/g1/functions/mass_storage.$N/lun.0/cdrom
		echo 0 > $CONFIGFS/g1/functions/mass_storage.$N/lun.0/ro
		echo 0 > $CONFIGFS/g1/functions/mass_storage.$N/lun.0/nofua
		echo $FILE > $CONFIGFS/g1/functions/mass_storage.$N/lun.0/file

		ln -s $CONFIGFS/g1/functions/mass_storage.$N $CONFIGFS/g1/configs/c.$C/
    fi

	# this lists available UDC drivers
	echo "$(ls /sys/class/udc)" > $CONFIGFS/g1/UDC
}

# $1: IP address of usb interface
setup_usb_network() {
	# Run all usb network setup functions (add more below!)
	setup_usb_network_configfs

	# Setup usb IP address
	IP=$1
	for INTERFACE in usb0 rndis0 eth0; do
		# try to setup interface. If it fails, try the next one.
		ip address add "$IP" dev $INTERFACE || continue
		# It succeeded, now bring it up and exit
		ip link set $INTERFACE up
		break
	done
}

# $1: path to ppm.gz file
show_splash() {
	# Skip for non-framebuffer devices
	# shellcheck disable=SC2154
	if [ "$deviceinfo_no_framebuffer" = "true" ]; then
		echo "NOTE: Skipping framebuffer splashscreen (deviceinfo_no_framebuffer)"
		return
	fi

	gzip -c -d "$1" >/tmp/splash.ppm
	fbsplash -s /tmp/splash.ppm
}

loop_forever() {
	while true; do
		sleep 1
	done
}
