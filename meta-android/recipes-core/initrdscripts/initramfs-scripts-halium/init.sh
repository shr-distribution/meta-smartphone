#!/bin/sh

# machine.conf should provide $system_partition (for panic scenario)
. /machine.conf

# distro.conf should provide $distro_name
. /distro.conf

# import Halium script
. /functions
. /halium-boot.sh

# This sets up the USB with whatever USB_FUNCTIONS are set to via configfs.
# "rndis" and "ecm" are both attempted and either may be absent from the
# kernel: stock GKI (bluejay/panther) ships CONFIG_USB_CONFIGFS_ECM=y but no
# RNDIS, MTK vendor kernels (mindphone) the reverse. Whichever function the
# kernel supports is the one the debug network comes up on.
USB_FUNCTIONS="adb rndis ecm acm"
ANDROID_USB=/sys/class/android_usb/android0
GADGET_DIR=/config/usb_gadget
DEBUG_IP=192.168.2.15
# Fixed gadget MACs (first byte must be even) - without these some kernels
# present 00:00:00:00:00:00 and the host refuses the interface; a stable MAC
# also lets the host remember the connection. Same trick as the tenderloin
# mainline initramfs.
DEBUG_MAC_HOST="FA:75:7F:BB:F4:E6"
DEBUG_MAC_DEV="FA:75:7F:BB:F4:E7"

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

    # Network functions: mkdir fails when the kernel lacks the function driver
    # (RNDIS missing from stock GKI, rndis_bam is Qualcomm-only, ECM missing
    # from some vendor kernels) - tolerate it and link only what exists.
    if echo $USB_FUNCTIONS | grep -q "rndis"; then
        mkdir $GADGET_DIR/g1/functions/rndis.usb0 2>/dev/null
        mkdir $GADGET_DIR/g1/functions/rndis_bam.rndis 2>/dev/null
    fi
    echo $USB_FUNCTIONS | grep -q "ecm" && mkdir $GADGET_DIR/g1/functions/ecm.usb0 2>/dev/null
    # acm gives a USB serial console on the host (/dev/ttyACM0) with no
    # networking at all; present in stock GKI and the MTK 4.14 kernel
    echo $USB_FUNCTIONS | grep -q "acm" && mkdir $GADGET_DIR/g1/functions/acm.usb0 2>/dev/null
    # fixed MACs for the network functions (see DEBUG_MAC_* above)
    for f in rndis.usb0 ecm.usb0; do
        if [ -d $GADGET_DIR/g1/functions/$f ]; then
            write $GADGET_DIR/g1/functions/$f/host_addr "$DEBUG_MAC_HOST" 2>/dev/null
            write $GADGET_DIR/g1/functions/$f/dev_addr  "$DEBUG_MAC_DEV"  2>/dev/null
        fi
    done
    echo $USB_FUNCTIONS | grep -q "mass_storage" && mkdir $GADGET_DIR/g1/functions/storage.0
    echo $USB_FUNCTIONS | grep -q "adb" && mkdir $GADGET_DIR/g1/functions/ffs.adb

    mkdir $GADGET_DIR/g1/configs/c.1
    mkdir $GADGET_DIR/g1/configs/c.1/strings/0x409
    write $GADGET_DIR/g1/configs/c.1/strings/0x409/configuration "$USB_FUNCTIONS"

    [ -d $GADGET_DIR/g1/functions/rndis.usb0 ] && ln -s $GADGET_DIR/g1/functions/rndis.usb0 $GADGET_DIR/g1/configs/c.1
    [ -d $GADGET_DIR/g1/functions/rndis_bam.rndis ] && ln -s $GADGET_DIR/g1/functions/rndis_bam.rndis $GADGET_DIR/g1/configs/c.1
    [ -d $GADGET_DIR/g1/functions/ecm.usb0 ] && ln -s $GADGET_DIR/g1/functions/ecm.usb0 $GADGET_DIR/g1/configs/c.1
    [ -d $GADGET_DIR/g1/functions/acm.usb0 ] && ln -s $GADGET_DIR/g1/functions/acm.usb0 $GADGET_DIR/g1/configs/c.1
    echo $USB_FUNCTIONS | grep -q "mass_storage" && ln -s $GADGET_DIR/g1/functions/storage.0 $GADGET_DIR/g1/configs/c.1
    echo $USB_FUNCTIONS | grep -q "adb" && ln -s $GADGET_DIR/g1/functions/ffs.adb $GADGET_DIR/g1/configs/c.1
}

# Bring up telnet on the USB gadget network so the debug shell is reachable
# without adb (UBports/Mer convention: device 192.168.2.15, telnet port 23).
# The interface name is read back from the configfs function that actually
# bound; the sysfs scan covers the legacy android_usb path. All tools used
# here (telnetd, udhcpd, ip) are busybox applets already in the initramfs.
start_debug_network() {
    ifname=""
    for f in $GADGET_DIR/g1/functions/rndis.usb0 $GADGET_DIR/g1/functions/ecm.usb0; do
        [ -f "$f/ifname" ] && ifname=$(cat "$f/ifname") && [ -n "$ifname" ] && break
        ifname=""
    done
    if [ -z "$ifname" ]; then
        for i in usb0 rndis0; do
            [ -d /sys/class/net/$i ] && ifname=$i && break
        done
    fi
    if [ -z "$ifname" ]; then
        tell_kmsg "debug: no usb network interface found, telnet unavailable (adb only)"
        return 1
    fi

    ip link set "$ifname" up
    ip addr add $DEBUG_IP/24 dev "$ifname" 2>/dev/null

    # DHCP so the host side needs zero configuration (UBports lease range)
    mkdir -p /var/lib/misc
    touch /var/lib/misc/udhcpd.leases
    cat > /etc/udhcpd.conf <<EOF
start 192.168.2.20
end 192.168.2.90
interface $ifname
option subnet 255.255.255.0
EOF
    udhcpd /etc/udhcpd.conf 2>/dev/null

    # busybox telnetd needs devpts (mounted by setup_devtmpfs, guard anyway)
    mkdir -p /dev/pts
    mountpoint -q /dev/pts || mount -t devpts devpts /dev/pts
    telnetd -b $DEBUG_IP:23 -l /bin/sh
    tell_kmsg "debug: telnet ready on $ifname, $DEBUG_IP port 23"

    # netconsole (Tier B kernels with CONFIG_NETCONSOLE_DYNAMIC only - it is
    # KMI-poison on Tier A GKI, NETPOLL changes struct net_device): stream
    # kmsg to the host over UDP, surviving switch_root. Broadcast target so
    # the host address does not matter; listen with  nc -ul 6666
    # (same receiver as the tenderloin mainline netconsole, which uses the
    # kernel cmdline instead because its gadget is kernel-owned from boot).
    modprobe netconsole 2>/dev/null
    if [ -d /sys/kernel/config ]; then
        mountpoint -q /sys/kernel/config || mount -t configfs none /sys/kernel/config 2>/dev/null
    fi
    NCDIR=/sys/kernel/config/netconsole
    if [ -d "$NCDIR" ] && mkdir "$NCDIR/target1" 2>/dev/null; then
        write "$NCDIR/target1/dev_name"    "$ifname"
        write "$NCDIR/target1/local_ip"    "$DEBUG_IP"
        write "$NCDIR/target1/remote_ip"   "192.168.2.255"
        write "$NCDIR/target1/remote_mac"  "ff:ff:ff:ff:ff:ff"
        write "$NCDIR/target1/remote_port" "6666"
        if write "$NCDIR/target1/enabled" "1" 2>/dev/null; then
            tell_kmsg "debug: netconsole streaming kmsg to udp broadcast port 6666"
            # Boot-param netconsole targets replay the early printk buffer
            # (CON_PRINTBUFFER); dynamic targets do not. Re-inject the buffer
            # so the host sees history from power-on too. One write per line
            # (a bulk redirect would chunk arbitrarily into kmsg records);
            # needs printk.devkmsg=on on the cmdline or the writes ratelimit.
            dmesg 2>/dev/null | while read -r l; do
                echo "replay: $l" > /dev/kmsg
            done
        else
            rmdir "$NCDIR/target1" 2>/dev/null
        fi
    fi

    # ACM serial console on the host's /dev/ttyACM0 (no networking needed);
    # the gadget tty appears as /dev/ttyGS0 shortly after UDC bind
    i=0
    while [ $i -lt 10 ] && [ ! -c /dev/ttyGS0 ]; do i=$((i+1)); usleep 200000; done
    if [ -c /dev/ttyGS0 ]; then
        (while :; do getty -n -l /bin/sh 115200 ttyGS0 2>/dev/null || sleep 2; done) &
        tell_kmsg "debug: serial console on gadget acm (host: screen /dev/ttyACM0 115200)"
    fi
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

    # The iSerial string doubles as a host-visible stage marker: watch it with
    #   while :; do lsusb -v 2>/dev/null | grep 'iSerial'; done | uniq
    if [ -d $ANDROID_USB ]; then
        usb_setup_android_usb "LuneOS initrd telnet $DEBUG_IP: $1"
    else
        usb_setup_configfs "LuneOS initrd telnet $DEBUG_IP: $1"
    fi

    mkdir -p /dev/usb-ffs/adb
    mount -o uid=2000,gid=2000 -t functionfs adb /dev/usb-ffs/adb

    usleep 500000

    # adbd has to be started before gadget is configured
    /usr/bin/adbd &

    usleep 500000

    [ -e $GADGET_DIR/g1/UDC ] && write $GADGET_DIR/g1/UDC "$(ls /sys/class/udc)"

    # gadget is live - the network interface exists only from this point on
    start_debug_network

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
    # NB: use a relative path, which will be valid both before and after chroot
    if [ ! -e $datadir/var/lib/lxc/android ]; then
        mkdir -p $datadir/var/lib/lxc
        ln -sf ../../../userdata/luneos-data/luneos-lxc-android $datadir/var/lib/lxc/android
    fi

    tell_kmsg "Bind-mount the directories"
    # bind-mount the directories to their correct place
    for dir in var home ; do
        mount -o bind,rw $datadir/$dir ${rootmnt}/$dir
    done
}

load_kernel_modules() {
    # GKI devices ship the early kernel modules at /lib/modules in a vendor
    # ramdisk the bootloader merges before this one (a vendor_boot "dlkm"
    # fragment on A12-launch devices, the separate vendor_kernel_boot
    # partition on A13-launch ones). The stock first-stage init we replaced
    # normally insmods them; without this, storage (UFS on Tensor) never
    # appears and mountroot panics.
    [ -f /lib/modules/modules.load ] || return 0
    tell_kmsg "initrd: loading $(wc -l < /lib/modules/modules.load) vendor kernel modules"
    # The kernel does NOT apply "module.param=" cmdline options to loadable
    # modules - modprobe does, by parsing /proc/cmdline and passing them to
    # the load call ('-' and '_' are equivalent in the module name). Stock
    # libmodprobe does the same. Without this, ufs_pixel_fips140.fips_*_lba
    # arrive as 0 (FMP self-test panic) and exynos_drm.panel_name is lost.
    read -r CMDLINE < /proc/cmdline
    module_cmdline_args() {
        n="${1%.ko}"
        n2=$(echo "$n" | tr '_-' '-_')
        args=""
        for tok in $CMDLINE; do
            case "$tok" in
                "$n".*=*|"$n2".*=*) args="$args ${tok#*.}";;
            esac
        done
        echo "$args"
    }
    # modules.load is a LIST, not an order: stock init hands it to libmodprobe,
    # which resolves /lib/modules/modules.dep (and modules.softdep) for every
    # entry. Taking it as an order is wrong on every Tensor device measured --
    # bluejay 335 and panther 332 hard-dependency pairs are listed backwards
    # (clk_exynos_gs 29 lines before the cmupmucal that exports cal_clk_*) --
    # plus ~15 softdep "pre" edges that no symbol dependency implies, which a
    # retry loop can never fix because insmod succeeds anyway (exynos-drm
    # before phy-exynos-mipi). So resolve the order first, the way modprobe
    # does. Each modules.dep line carries the module's full dependency closure,
    # nearest first and deepest last, and is itself correctly ordered (checked
    # per build by tools/module-order.py), so emitting a line reversed, then
    # the module's softdep "pre" entries, then the module, is a valid order.
    if [ -f /lib/modules/modules.dep ]; then
        awk -v S=/lib/modules/modules.softdep '
        BEGIN {
            if (S != "") {
                while ((getline l < S) > 0) {
                    n = split(l, f, /[ \t]+/)
                    if (n < 3 || f[1] != "softdep") continue
                    k = f[2]; gsub(/-/, "_", k); which = ""
                    for (i = 3; i <= n; i++) {
                        if (f[i] == "pre:") { which = "pre"; continue }
                        if (f[i] == "post:") { which = "post"; continue }
                        x = f[i]; gsub(/-/, "_", x)
                        if (which == "pre") pre[k] = pre[k] x " "
                        else if (which == "post") post[k] = post[k] x " "
                    }
                }
                close(S)
            }
        }
        FNR == NR {
            key = $1; sub(/:$/, "", key); sub(/.*\//, "", key)
            nd[key] = NF - 1
            for (i = 2; i <= NF; i++) { d = $i; sub(/.*\//, "", d); dep[key, i - 1] = d }
            nk = key; gsub(/-/, "_", nk); sub(/\.ko$/, "", nk); byname[nk] = key
            next
        }
        { emit($1) }
        function emit(m,   i, d, k, n, a) {
            if (m == "" || (m in seen)) return
            seen[m] = 1
            for (i = nd[m]; i >= 1; i--) { d = dep[m, i]; if (!(d in done)) emit(d) }
            k = m; gsub(/-/, "_", k); sub(/\.ko$/, "", k)
            if (k in pre) { n = split(pre[k], a, " ")
                for (i = 1; i <= n; i++)
                    if (a[i] != "" && (a[i] in byname) && !(byname[a[i]] in done)) emit(byname[a[i]]) }
            if (!(m in done)) { done[m] = 1; print m }
            if (k in post) { n = split(post[k], a, " ")
                for (i = 1; i <= n; i++)
                    if (a[i] != "" && (a[i] in byname) && !(byname[a[i]] in done)) emit(byname[a[i]]) }
        }' /lib/modules/modules.dep /lib/modules/modules.load > /mods.todo
        tell_kmsg "initrd: resolved dependency order for $(wc -l < /mods.todo) modules"
    fi
    if [ ! -s /mods.todo ]; then
        tell_kmsg "initrd: WARNING: no usable modules.dep, using modules.load order"
        cp /lib/modules/modules.load /mods.todo
    fi
    # One pass should now be enough; the retry loop stays as a safety net for
    # anything whose dependency ships elsewhere (vendor_dlkm, loaded later).
    pass=0
    while [ -s /mods.todo ]; do
        pass=$((pass+1))
        : > /mods.next
        while read -r m; do
            insmod "/lib/modules/$m" $(module_cmdline_args "$m") 2>/dev/null || echo "$m" >> /mods.next
        done < /mods.todo
        left=$(wc -l < /mods.next)
        todo=$(wc -l < /mods.todo)
        tell_kmsg "initrd: module pass $pass: loaded $((todo-left)) of $todo"
        [ "$left" -eq "$todo" ] && break
        mv /mods.next /mods.todo
    done
    [ -s /mods.todo ] && tell_kmsg "initrd: WARNING: modules not loaded: $(tr '\n' ' ' < /mods.todo)"
    rm -f /mods.todo /mods.next
    # storage probes asynchronously; wait up to 10s for a userdata partition
    i=0
    while [ $i -lt 50 ]; do
        for blk in /sys/class/block/*; do
            pn=$(sed -n 's/^PARTNAME=//p' "$blk/uevent" 2>/dev/null)
            case "$pn" in userdata*)
                tell_kmsg "userdata partition appeared: $(basename $blk)"
                return 0;;
            esac
        done
        i=$((i+1))
        usleep 200000
    done
    tell_kmsg "WARNING: no userdata partition appeared after module load"
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
    mount -t tmpfs -o size=100M tmpfs ${rootmnt}
    cp -rf /recovery/* ${rootmnt}/
    exec switch_root ${rootmnt} /init "$@"
fi

echo "======= LuneOS/Halium ===========" > /dev/kmsg

# Check wether we need to start adbd for interactive debugging
cat /proc/cmdline | grep enable_adb
if [ $? -ne 1 ] ; then
    panic "Initramfs Debug Mode"
fi

mirror_trusty_log() {
    # Trusty is a separate secure OS on Pixels (and other TEE-using devices).
    # When its own apps assert, it takes the kernel down with it, and its
    # explanation lives only in /dev/trusty-log0 - which nothing reads once
    # Android's init has been replaced by this one. Mirroring it into kmsg puts
    # it in pstore, so it survives the reboot and can be read back from
    # /sys/fs/pstore/console-ramoops-0 - the only way to see a Trusty panic
    # reason on a device with no serial console.
    #
    # Guarded on the node, so it is a no-op everywhere else. Costs one
    # background reader that exits when the initramfs does.
    [ -c /dev/trusty-log0 ] || return 0
    tell_kmsg "initrd: mirroring /dev/trusty-log0 to kmsg"
    (while read -r l; do echo "trusty: $l" > /dev/kmsg; done < /dev/trusty-log0) &
}

echo "Loading kernel modules" > /dev/kmsg
load_kernel_modules
mirror_trusty_log

echo "Starting mdev" > /dev/kmsg
start_mdev

# Disable busybox's over-restrictive behavior with cpio extraction
export EXTRACT_UNSAFE_SYMLINKS=1


# When no vendor.img is shipped in the rootfs (the GSI case), Halium's mountroot
# leaves /android/vendor as the empty directory the generic system image carries:
# it mounts a real vendor only from /android-vendor, which is set up solely when
# a vendor.img exists, and the fstab it would otherwise consult lives inside the
# Android image - a device-agnostic GSI has none. Without this the container
# comes up with no HALs at all.
#
# Mount the device's own vendor partition there instead. Partitions are found by
# parsing PARTNAME out of sysfs rather than relying on /dev/disk/by-partlabel,
# so this does not depend on mdev having populated those symlinks yet.
find_partition_by_name() {
    want=$1
    for blk in /sys/class/block/*; do
        [ -f "$blk/uevent" ] || continue
        pn=$(sed -n 's/^PARTNAME=//p' "$blk/uevent")
        if [ "$pn" = "$want" ]; then
            echo "/dev/$(basename $blk)"
            return 0
        fi
    done
    return 1
}

mount_device_vendor() {
    # Already populated - a vendor.img was shipped, nothing to do.
    if [ -e ${rootmnt}/android/vendor/etc ] || [ -e ${rootmnt}/android/vendor/build.prop ]; then
        tell_kmsg "/android/vendor already populated, not mounting device vendor"
        return 0
    fi

    slot=$(grep -o 'androidboot\.slot_suffix=..' /proc/cmdline | cut -d "=" -f2)
    [ -n "$slot" ] && tell_kmsg "A/B slot suffix is $slot"

    vpart=""
    for name in vendor$slot vendor; do
        vpart=$(find_partition_by_name "$name") && [ -n "$vpart" ] && break
        vpart=""
    done

    if [ -z "$vpart" ]; then
        tell_kmsg "WARNING: no vendor partition found; Android HALs will be missing"
        return 1
    fi

    tell_kmsg "mounting device vendor $vpart at /android/vendor"
    if mount -o ro "$vpart" ${rootmnt}/android/vendor; then
        # Check that what mounted really is a vendor, and get out of the way if
        # it is not.
        #
        # On a device with retrofit dynamic partitions the logical vendor is not
        # this partition: it is spread across the physical system and vendor
        # partitions and has to be assembled with dm-linear first. Mounting the
        # physical partition raw still succeeds, because a filesystem superblock
        # survives at offset zero from before the conversion - on a Pixel 3a
        # upgraded from Android 9 to 11 it mounts as ext4 and is empty.
        #
        # An empty mount would be merely useless; the real damage is that it
        # holds the partition open, so mount-android.sh cannot claim it for
        # dm-linear later. dmsetup then fails the whole table with EBUSY:
        #
        #   device-mapper: table: 253:1: linear: Device lookup failed
        #   reload ioctl on dynpart-vendor_a failed: Device or resource busy
        #
        # and the device ends up with no vendor at all.
        #
        # The initramfs cannot do the mapping itself - no udev, no dmsetup, no
        # parse-android-dynparts - so it should recognise the situation and
        # leave the partition alone for mount-android.sh to deal with.
        if [ ! -e ${rootmnt}/android/vendor/build.prop ] && \
           [ ! -d ${rootmnt}/android/vendor/etc ]; then
            tell_kmsg "$vpart holds no vendor filesystem (no build.prop, no etc);"
            tell_kmsg "unmounting it so the logical vendor can be mapped later"
            umount ${rootmnt}/android/vendor
            return 1
        fi
        # The real vendor carries its own fstab (firmware, persist, dsp ...);
        # mountroot already ran this against an empty directory, so run it again
        # now that there is something to read.
        mount_android_partitions "${rootmnt}/android/vendor/etc/fstab*" ${rootmnt}/android ${rootmnt}/userdata
    else
        tell_kmsg "WARNING: failed to mount $vpart at /android/vendor"
        return 1
    fi
}

# Call Halium's mount script
mountroot

# GSI case: bring in the device's own /vendor (no-op when a vendor.img shipped)
mount_device_vendor

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
