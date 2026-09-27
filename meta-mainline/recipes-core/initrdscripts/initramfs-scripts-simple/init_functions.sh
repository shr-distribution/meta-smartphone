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
# Stop the udhcpd started by setup_usb_network, before switch_root.
#
# It must not outlive the initramfs. switch_root does not kill anything, so the
# process would keep running on an unlinked binary while still holding UDP 67:
# the rootfs's own udhcpd (luneos-usb-gadget.sh, usb-ip.sh) then cannot bind and
# exits, and if the rootfs rebuilds the gadget the surviving daemon is left
# answering on an interface that no longer exists. Either way the host stops
# being offered an address, which is the one thing this is here to do.
stop_udhcpd() {
	if [ -r /run/udhcpd-usb.pid ]; then
		kill "$(cat /run/udhcpd-usb.pid)" 2>/dev/null
		rm -f /run/udhcpd-usb.pid
	fi
}

start_telnetd() {
	# /dev/pts (needed for telnet)
	mkdir /dev/pts
	mount -t devpts none /dev/pts
	
  # Add root user
  cat > /etc/passwd << "EOF"
root::0:0:root:/root:/bin/sh
EOF
    	
	echo "Starting telnetd..."
	/usr/sbin/telnetd -b $1
	echo "Pidof telnetd: $(pidof telnetd)"
}

stop_telnetd() {
	killall telnetd
	umount /dev/pts
}


resize_rootfs_if_needed() {

	# See if the filesystem on the rootfs partition needs resizing (usually on first boot).
	# If the difference between the partition size and the filesystem size is above a small
	# threshold, assume it needs resizing to fill the partition.
	path=$(readlink -f $1)

	# Partition size in 1k blocks
	case $path in
	/dev/mmcblk*)
		# First enlarge the partition
		
		# split path between partition device and partition number
		# /dev/mmcblk1p2 gives "/dev/mmcblk1" and "2"
		subpaths=( $(grep -Eo '/dev/mmcblk[[:digit:]]+|[[:digit:]]+$' <<< "$path") )
		devrootfs=${subpaths[0]}
		partrootfs=${subpaths[1]}
		
		# Thanks to lsblk, compute the space that isn't used by any partition
		freespace=$(grep -E $(basename $devrootfs)'(p|$)' /proc/partitions | awk '{if(left==0) left=$3; else left-=$3} END{print left}')
		if [ ${freespace} -gt 100000 ]; then
			echo "... issuing: parted -s -f -a opt $devrootfs \"resizepart $partrootfs 100%\""
			parted -s -f -a opt $devrootfs "resizepart $partrootfs 100%"
		fi

		# Then, enlarge the filesystem

		# read the updated partition info
		pblocks=$(grep $(basename $path) /proc/partitions | awk {'print $3'})
		;;
	esac
	# Filesystem size in 4k blocks
	fsblocks=$(dumpe2fs -h $path | grep "Block count" | awk {'print $3'})
	# Difference between the reported sizes in 1k blocks
	dblocks=$((pblocks - 4 * fsblocks))
	if [ $dblocks -gt 10000 ]; then
		echo "... issuing: resize2fs -f $path"
		resize2fs -f $path && echo "INFO: resized rootfs filesystem to fill $path"
	fi
}

# $1: label of the partition
# $2: target directory of mount
mount_root_partition() {
	partname=$1
	rfs=$2

	if [ -z "$partname" ] ; then
		partname="luneos-root"
	fi

	# when booting with systemd-boot, it if possible to know from which partition the EFI binary was used.
	# if available, use that to better guess where the rootfs partition should be
	if grep -q efivarfs /proc/filesystems; then
		mount -t efivarfs efivarfs /sys/firmware/efi/efivars
		efi_partition=$(cat /sys/firmware/efi/efivars/LoaderDevicePartUUID-4a67b082-0a4c-41cf-b6c7-440b29bb8c4f | tr -d '\x0-\x20' | tr A-Z a-z)
		umount /sys/firmware/efi/efivars

		# use util-linux's blkid, without cache, to find the corresponding device, as
		# busybox's blkid doesn't output PARTUUID information
		efi_mmcblk=$(/sbin/blkid.util-linux -c /dev/null -t "PARTUUID=$efi_partition" -o device)
		info "Booting from: $efi_mmcblk (PARTUUID=$efi_partition)"
		if [ -b ${efi_mmcblk::-2} ]; then
			info "Detected boot from $efi_mmcblk, using same MMC for rootfs !"
			partname_detected=$(blkid -t LABEL=$partname -o device | grep ${efi_mmcblk::-2})
			if [ -n "$partname_detected" ] ; then
				partname=$(basename $partname_detected)
				info "... using $partname"
			fi
		fi
	fi

    part=$(find /dev -name $partname* | tail -1)
    if [ -n "$part" ]; then
		rootfs_path=$(readlink -f $part)
    fi

	info "Eventually resize $rootfs_path to fill the available space..."
	resize_rootfs_if_needed $rootfs_path

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

	# Name the gadget after this device instead of a constant "LuneOS Device"
	# with a constant serial. The initramfs carries a per-machine /etc/hostname,
	# so this works here too, and it is the only way to tell two LuneOS devices
	# apart on the host: the descriptors were byte-identical and both ends use
	# 172.16.42.2, so udev reported ID_MODEL=LuneOS_Device for both and only one
	# could hold the address. There is no machine-id in the initramfs, so the
	# serial falls back to the DT serial-number.
	gadget_name=$(tr -d " \t\n" < /etc/hostname 2>/dev/null)
	[ -n "$gadget_name" ] || gadget_name=$(tr -d "\0" < /proc/device-tree/model 2>/dev/null | tr " /" "--")
	[ -n "$gadget_name" ] || gadget_name=device
	gadget_serial=$(tr -d "\0" < /proc/device-tree/serial-number 2>/dev/null)
	[ -n "$gadget_serial" ] || gadget_serial=fedcba9876543210

	mkdir -p $CONFIGFS/g1/strings/0x409
	echo "$gadget_serial" > $CONFIGFS/g1/strings/0x409/serialnumber
	echo "LuneOS" > $CONFIGFS/g1/strings/0x409/manufacturer
	echo "LuneOS $gadget_name" > $CONFIGFS/g1/strings/0x409/product

	N="usb0"
	mkdir -p $CONFIGFS/g1/functions/ecm.$N

	# Host-side MAC, derived per device rather than one constant for all of them.
	# With an identical MAC the host's predictable-interface-name scheme wants to
	# call every link enx<samemac>: only the first gets that name and the rest
	# fall back to usb0/usb1 in probe order, so NetworkManager profiles pinned to
	# an interface name keep attaching to whichever device enumerated first.
	# gadget_serial is set above. First byte stays fa: locally administered
	# (bit 1) and unicast (bit 0), which is what "must be even" was about.
	if [ -n "$gadget_serial" ] && command -v md5sum >/dev/null 2>&1; then
		HOST="fa:$(printf "%s" "$gadget_serial" | md5sum |
			sed "s/\(..\)\(..\)\(..\)\(..\)\(..\).*/\1:\2:\3:\4:\5/")"
	else
		HOST="FA:75:7F:BB:F4:E6" # "HostPC"
	fi
	echo $HOST > $CONFIGFS/g1/functions/ecm.$N/host_addr

	C=1
	mkdir -p $CONFIGFS/g1/configs/c.$C/strings/0x409
	echo "Config $C: ECM network" > $CONFIGFS/g1/configs/c.$C/strings/0x409/configuration 
	echo 250 > $CONFIGFS/g1/configs/c.$C/MaxPower 
	ln -s $CONFIGFS/g1/functions/ecm.$N          $CONFIGFS/g1/configs/c.$C/

    if grep -q bootmode=recovery /proc/cmdline || [ "$RECOVERYMODE" = "yes" ]; then
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
# $1: optional "addr/prefix" override; without it, one /24 per machine (below)
setup_usb_network() {
	# Run all usb network setup functions (add more below!)
	setup_usb_network_configfs

	# One /24 per machine, so several LuneOS devices can be connected to one
	# host at once. Every device used to claim 172.16.42.2/16 and every
	# host-side link 172.16.42.1/24, which leaves the host with several routes
	# for one subnet: it picks one, and traffic for the second device goes out
	# the first device's interface. Distinct subnets are the actual fix.
	#
	# Keep in sync with the same table in meta-pine64-luneos'
	# luneos-usb-gadget.sh, which does this from the rootfs.
	if [ -n "$1" ]; then
		IP="$1"
		NET=$(echo "$1" | cut -d. -f1-3)
	else
		case "$(tr -d " \t\n" < /etc/hostname 2>/dev/null)" in
			pinephone)    NET=172.16.42 ;;
			pinephonepro) NET=172.16.43 ;;
			pinetab2)     NET=172.16.44 ;;
			*)            NET=172.16.45 ;;
		esac
		IP="$NET.2/24"
	fi

	# Publish it: recovery's telnetd has to bind the address this function
	# actually assigned. It used to be told 172.16.42.2 literally, which is only
	# still right for the pinephone - on every other machine telnetd would bind
	# an address that is not on the interface, fail, and take recovery's only
	# remote entry point with it.
	USB_ADDR="${IP%%/*}"
	USB_NET="$NET"

	for INTERFACE in usb0 rndis0 eth0 usb1; do
		# try to setup interface. If it fails, try the next one.
		ip address add "$IP" dev $INTERFACE || continue
		# It succeeded, now bring it up and exit
		ip link set $INTERFACE up
		echo "  $INTERFACE up at $IP (host gets $NET.1)"

		# Hand the host its address so recovery is reachable without the host
		# being configured per device. No "option router" and no "option dns" on
		# purpose: this must never become the host's default gateway or
		# resolver - it answers on one point-to-point link with one address.
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

loop_forever() {
	while true; do
		sleep 1
	done
}
