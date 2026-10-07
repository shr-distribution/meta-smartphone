#!/bin/sh
# This file will be in /init_functions.sh inside the initramfs.

# Redirect stdout and stderr to logfile
setup_log() {
	sleep 5
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
    echo "Waiting for 10 seconds before rebooting"
    sleep 10
    reboot
}

# $1: root directory for mounts
mount_proc_sys_dev_configfs() {
	# mdev
	mount -t proc -o nodev,noexec,nosuid proc $1/proc
	mount -t sysfs -o nodev,noexec,nosuid sysfs $1/sys
	
	mkdir $1/config
	mount -t configfs -o nodev,noexec,nosuid configfs $1/config
}

# $1: root directory for mounts
umount_proc_sys_dev_configfs() {
	umount -l $1/config
	umount -l $1/sys
	umount -l $1/proc
}

start_mdev() {
	echo "start mdev..."
	echo /sbin/mdev > /sys/kernel/uevent_helper
    /sbin/mdev -s
    echo "mdev started."
}

stop_mdev() {
    killall mdev
    echo "" > /sys/kernel/uevent_helper
}

start_telnetd() {
	# /dev/pts (needed for telnet)
	mkdir /dev/pts
	mount -t devpts none /dev/pts
	
	echo "Starting telnetd..."
	telnetd
	echo "Pidof telnetd: $(pidof telnetd)"
}

stop_telnetd() {
	killall telnetd
	umount /dev/pts
}

# Opt-in boot log: with LUNEOS_BOOTLOG on the kernel command line, keep copying
# the kernel log (which carries this initramfs's own output, see setup_log) to
# $1/luneos-boot.log, so it can be read from recovery when nothing else on the
# device is reachable. It is flushed every couple of seconds so that it survives
# a hang or a reset; the previous boot's log is kept as luneos-boot.log.prev.
# $1: directory the userdata partition is mounted on
start_bootlog() {
	grep -q LUNEOS_BOOTLOG /proc/cmdline || return 0
	BOOTLOG="$1/luneos-boot.log"
	[ -f "$BOOTLOG" ] && mv -f "$BOOTLOG" "$BOOTLOG.prev"
	(
		while :; do
			dmesg > "$BOOTLOG" 2>/dev/null
			sync
			sleep 2
		done
	) &
	echo $! > /run/bootlog.pid
	info "Boot log: $BOOTLOG"
}

# Take one last copy and stop the loop; called before switching to the rootfs.
stop_bootlog() {
	[ -r /run/bootlog.pid ] || return 0
	kill "$(cat /run/bootlog.pid)" 2>/dev/null
	rm -f /run/bootlog.pid
	dmesg > "$BOOTLOG" 2>/dev/null
	sync
}

# $1: target directory of mount
mount_sdcard() {
	# First, try to find the device
	info "Trying to find $sdcard_partition in /dev ..."
	devicepath=$(find /dev -name $sdcard_partition -print -quit)
	info "device path value: $devicepath"
	
	# Unlikely, but it might be that the sdcard hasn't appeared yet
	if [ -z "$devicepath" -o ! -e "$devicepath" ] ; then
		# Wait for sdcard device
		sdcard_device=$( echo "$sdcard_partition" | sed -e 's/p[[:digit:]]\+$//' )
		while [ ! -e /sys/block/$sdcard_device ] ; do
			info "Waiting for SD card/NAND /sys/block/$sdcard_device"
			sleep 1
		done

		# Try unpartitioned card
		if [ ! -e /sys/block/$sdcard_device/$sdcard_partition ] ; then
			sdcard_partition=$sdcard_device
		fi
	fi

	SDCARD_DIR=$1
	info "Mounting SD card/NAND $devicepath to $SDCARD_DIR"
	mkdir -m 0777 $SDCARD_DIR
	mount -t auto -o rw,noatime,nodiratime $devicepath $SDCARD_DIR
	[ $? -eq 0 ] || fail "Failed to mount SD card/NAND, cannot continue"
}	

# $1: target directory of mount
mount_root_partition() {
	rfs=$1
	
	if [ -z "$rootfs_type" ] ; then
		rootfs_type=ext2
	fi
	mkdir -m 0755 $rfs
	info "Checking for root filesystem image on SD card/NAND"
	if [ -e $ANDROID_MEDIA_DIR/$distro_name/rootfs.$rootfs_type ] ; then
		info "Mounting root filesystem image $ANDROID_MEDIA_DIR/$distro_name/rootfs.$rootfs_type"
		losetup /dev/loop2 $ANDROID_MEDIA_DIR/$distro_name/rootfs.$rootfs_type
		[ $? -eq 0 ] || fail "Failed to create loop device for root filesystem image!"
		e2fsck -y /dev/loop2
		mount -t $rootfs_type -o noatime,nodiratime,sync,rw /dev/loop2 $rfs
		[ $? -eq 0 ] || fail "Failed to mount root filesystem image!"
	elif [ -d $ANDROID_MEDIA_DIR/$distro_name/rootfs ] ; then
		info "Chrooting into root filesystem folder $ANDROID_MEDIA_DIR/$distro_name/rootfs"
		mount -o bind,rw $ANDROID_MEDIA_DIR/$distro_name/rootfs $rfs
		[ $? -eq 0 ] || fail "Failed to bind mount $ANDROID_MEDIA_DIR/$distro_name/rootfs"
	elif [ -d $SDCARD_DIR/$distro_name ] ; then
		info "Chrooting into root filesystem folder $SDCARD_DIR/$distro_name"
		mount -o bind,rw $SDCARD_DIR/$distro_name $rfs
		[ $? -eq 0 ] || fail "Failed to bind mount $SDCARD_DIR/$distro_name"
	else
		info "No root filesystem found on SD card/NAND; using /dev/$system_partition"

		# We don't have anything to boot from sdcard. Cleanup and boot
		# from system partition.
		umount $SDCARD_DIR

		mount -t auto -o rw,noatime,nodiratime /dev/$system_partition $rfs
		[ $? -eq 0 ] || fail "Failed to mount system partition /dev/$system_partition"
	fi
}

# Stop the udhcpd started by setup_usb_network, before switch_root.
#
# It must not outlive the initramfs. switch_root does not kill anything, so the
# process would keep running on an unlinked binary while still holding UDP 67:
# the rootfs's own udhcpd (usb-ip.sh on radon) then cannot bind and exits, and if
# the rootfs rebuilds the gadget the surviving daemon is left answering on an
# interface that no longer exists. Either way the host stops being offered an
# address, which is the one thing this is here to do.
stop_udhcpd() {
	if [ -r /run/udhcpd-usb.pid ]; then
		kill "$(cat /run/udhcpd-usb.pid)" 2>/dev/null
		rm -f /run/udhcpd-usb.pid
	fi
}

setup_usb_network_android() {
	# Only run when the legacy android usb driver is really there - not merely
	# when its class directory is. On some vendor kernels (radon, MediaTek 4.19)
	# /sys/class/android_usb/android0 exists with none of its attributes, so
	# "[ -e $SYS ]" passed and every write below then failed with "Permission
	# denied" or "No such file or directory". Nothing was built, and the boot log
	# filled with errors that look like a broken gadget but are not: the gadget on
	# those devices comes from the configfs path below. Test an attribute this
	# function actually writes. Same test as android-gadget-setup uses.
	SYS=/sys/class/android_usb/android0
	[ -w "$SYS/enable" ] || return
	
	# Do the setup
	printf "%s" "0" >"$SYS/enable"
	printf "%s" "18D1" >"$SYS/idVendor"
	printf "%s" "D001" >"$SYS/idProduct"
	printf "%s" "rndis" >"$SYS/functions"
	printf "%s" "1" >"$SYS/enable"
}

setup_usb_network_configfs() {
	# Only run, when we have the gadget usb driver
	CONFIGFS=/config/usb_gadget
	[ -e "$CONFIGFS" ] || return

	# Create new gadget module template
	mkdir $CONFIGFS/g1
	# Congifure vendor and product IDs
	printf "%s" "0x18D1" >"$CONFIGFS/g1/idVendor"
	printf "%s" "0xD001" >"$CONFIGFS/g1/idProduct"

	# Setup english strings
	mkdir $CONFIGFS/g1/strings/0x409
	echo "0123456789" > $CONFIGFS/g1/strings/0x409/serialnumber
	echo "LuneOS" > $CONFIGFS/g1/strings/0x409/manufacturer
	echo "LuneOS device" > $CONFIGFS/g1/strings/0x409/product

	# Create function instances
	#mkdir $CONFIGFS/g1/functions/ffs.adb
	#mkdir $CONFIGFS/g1/functions/ffs.mtp
	mkdir $CONFIGFS/g1/functions/ecm.usb0
	echo "FA:75:7F:BB:F4:E6" > $CONFIGFS/g1/functions/ecm.usb0/host_addr

	# Create configuration instance
	mkdir $CONFIGFS/g1/configs/c.1
	mkdir $CONFIGFS/g1/configs/c.1/strings/0x409
	echo "120" > $CONFIGFS/g1/configs/c.1/MaxPower
	printf "%s" "rndis" > $CONFIGFS/g1/configs/c.1/strings/0x409/configuration

	# Bind function instances and their configuration
	# NOTE: binding ffs currently doesn't work and will disable ECM...
	#ln -s $CONFIGFS/g1/functions/ffs.adb $CONFIGFS/g1/configs/c.1
	#ln -s $CONFIGFS/g1/functions/ffs.mtp $CONFIGFS/g1/configs/c.1
	ln -s $CONFIGFS/g1/functions/ecm.usb0 $CONFIGFS/g1/configs/c.1

	echo "$(ls /sys/class/udc)" > $CONFIGFS/g1/UDC
}

# $1: IP address of usb interface
# $1: optional "addr/prefix" override; without it, one /24 per machine (below)
setup_usb_network() {
	# Run all usb network setup functions (add more below!)
	setup_usb_network_android
	setup_usb_network_configfs

	# One /24 per machine, so several LuneOS devices can be attached to one
	# host at once. Every device used to take 172.16.42.2/16 and every
	# host-side link 172.16.42.1/24, which leaves the host with several routes
	# for one subnet: it picks one, and traffic for the second device goes out
	# the first device's interface.
	#
	# Keep this table in sync with the two other copies:
	#   meta-mainline/.../initramfs-scripts-simple/init_functions.sh
	#   meta-pine64-luneos/.../luneos-usb-gadget.sh
	# .42/.43/.44 belong to pinephone/pinephonepro/pinetab2 there. Halium
	# devices share .45 by default, so add an entry here before connecting two
	# of them at the same time.
	if [ -n "$1" ]; then
		IP="$1"
		NET=$(echo "$1" | cut -d. -f1-3)
	else
		case "$(tr -d " \t\n" < /etc/hostname 2>/dev/null)" in
			radon) NET=172.16.46 ;;
			*)     NET=172.16.45 ;;
		esac
		IP="$NET.2/24"
	fi

	for INTERFACE in usb0 rndis0 eth0; do
		# try to setup interface. If it fails, try the next one.
		ip address add "$IP" dev $INTERFACE || continue
		# It succeeded, now bring it up and exit
		ip link set $INTERFACE up
		echo "  $INTERFACE up at $IP (host gets $NET.1)"

		# Hand the host its address so recovery is reachable without the
		# host being configured per device. No "option router" and no
		# "option dns" on purpose: this must never become the host's
		# default gateway or resolver.
		if command -v udhcpd >/dev/null 2>&1; then
			mkdir -p /run
			cat > /run/udhcpd-usb.conf <<EOF
interface $INTERFACE
start $NET.1
end $NET.1
max_leases 1
option subnet 255.255.255.0
lease_file /run/udhcpd-usb.leases
pidfile /run/udhcpd-usb.pid
EOF
			: > /run/udhcpd-usb.leases
			udhcpd /run/udhcpd-usb.conf ||
				echo "  warning: udhcpd failed; host must set $NET.1/24 itself"
		fi
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

set_framebuffer_mode() {
	[ -e "/sys/class/graphics/fb0/modes" ] || return
	[ -z "$(cat /sys/class/graphics/fb0/mode)" ] || return

	_mode="$(cat /sys/class/graphics/fb0/modes)"
	echo "Setting framebuffer mode to: $_mode"
	echo "$_mode" > /sys/class/graphics/fb0/mode
}

setup_framebuffer() {
	# Skip for non-framebuffer devices
	# shellcheck disable=SC2154
	if [ "$deviceinfo_no_framebuffer" = "true" ]; then
		echo "NOTE: Skipping framebuffer setup (deviceinfo_no_framebuffer)"
		return
	fi

	# Wait for /dev/fb0
	echo "NOTE: Waiting 10 seconds for the framebuffer /dev/fb0."
	echo "If your device does not have a framebuffer, disable this with:"
	echo "no_framebuffer=true in <https://postmarketos.org/deviceinfo>"
	for _ in $(seq 1 100); do
		[ -e "/dev/fb0" ] && break
		sleep 0.1
	done
	if ! [ -e "/dev/fb0" ]; then
		echo "ERROR: /dev/fb0 did not appear!"
		return
	fi

	set_framebuffer_mode
}

loop_forever() {
	while true; do
		sleep 1
	done
}
