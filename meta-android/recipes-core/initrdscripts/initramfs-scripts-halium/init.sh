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
# Keep this MINIMAL. Measured on the MP01: with "adb rndis ecm acm" the
# controller registers and then disappears - /sys/class/udc holds musb-hdrc
# before usb_setup_configfs() and nothing after it, so the bind fails with
# "No such device" and no gadget ever enumerates. Retrying the bind does not
# help, because the UDC is gone for good rather than flapping.
#
# The working Droidian MT6789 port creates exactly ONE function (rndis, swapped
# to ncm by its halium-hooks). rndis_bam in particular is Qualcomm-only and has
# no business being created here at all.
#
# A machine can override this; the default stays conservative.
USB_FUNCTIONS="${USB_FUNCTIONS:-rndis}"
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
        # rndis_bam is a Qualcomm function. Creating it on a MediaTek device is
        # at best useless and at worst fatal - see the USB_FUNCTIONS comment.
        [ -d /sys/module/rndis_bam ] && mkdir $GADGET_DIR/g1/functions/rndis_bam.rndis 2>/dev/null
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

    # The netdev does not exist the instant the gadget function does.
    # $GADGET_DIR/.../ifname is readable from configfs as soon as the function
    # is created, but /sys/class/net/<if> only appears once the gadget is bound
    # and the host has enumerated it - so configuring it immediately silently
    # does nothing, and the device comes up with a live gadget, a carrier, and
    # no address. That looks exactly like a dead device from the host: the
    # gadget enumerates, ping gets nowhere, and ARP stays INCOMPLETE. Seen twice
    # on the MP01, both times "fixed" by a power cycle, i.e. by winning the race
    # on the next try. Wait for it instead.
    _n=0
    while [ ! -d /sys/class/net/"$ifname" ] && [ $_n -lt 50 ]; do
        usleep 200000
        _n=$((_n + 1))
    done
    if [ ! -d /sys/class/net/"$ifname" ]; then
        tell_kmsg "debug: $ifname never appeared, telnet unavailable (adb only)"
        return 1
    fi

    # Retry the address too: the interface can exist a moment before it will
    # accept configuration, and a silent failure here costs a whole debug cycle.
    _n=0
    while [ $_n -lt 10 ]; do
        ip link set "$ifname" up 2>/dev/null
        ip addr add $DEBUG_IP/24 dev "$ifname" 2>/dev/null
        if ip addr show dev "$ifname" 2>/dev/null | grep -q "$DEBUG_IP"; then
            break
        fi
        usleep 200000
        _n=$((_n + 1))
    done
    if ip addr show dev "$ifname" 2>/dev/null | grep -q "$DEBUG_IP"; then
        tell_kmsg "debug: $ifname configured with $DEBUG_IP after $_n retries"
    else
        tell_kmsg "debug: WARNING could not set $DEBUG_IP on $ifname"
    fi

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

# Vendor modules that USB itself depends on, loaded before any gadget setup.
#
# Not a guess - this is what the working Droidian MT6789 port does, and its
# comment is the whole story:
#
#   halium_hook_panic() {
#       # USB setup requires this, also avoids mandatory replug issue
#       modprobe tcpci_late_sync.ko
#   }
#     - deathmist/halium-gki, branch q25-droidian, ramdisk-overlay/scripts/halium-hooks
#
# On MT6789 the USB role (peripheral vs host) is decided by the Type-C port
# controller. Until tcpc_class + tcpc_mt6375 + tcpci_late_sync are loaded the
# controller never enters peripheral mode, so **no UDC is ever created**,
# /sys/class/udc stays empty, and the gadget bind below is a silent no-op. On a
# device with no UART and an E Ink panel that is indistinguishable from a hang,
# and it is what made this port so hard to debug: every failure looked the same.
#
# Vendor order (modules.load lines 115, 116, 133); none has a modules.dep entry,
# so none can drag in something that blocks.
#
# Also load the watchdog: MediaTek's bootloader arms a hardware watchdog that
# stock init feeds, and without mtk_wdt the SoC resets this shell after ~20 s.
#
# All no-ops where the file is absent, i.e. every device whose USB is built in.
USB_PREREQ_MODULES="mtk_wdt tcpc_class tcpc_mt6375 tcpci_late_sync"

# Pick the first real (non-dummy) UDC.
#
# NOT "ls /sys/class/udc | grep -v dummy". busybox ls column-formats even when
# its stdout is a pipe, so it emits "dummy_udc.0  musb-hdrc" as ONE line; that
# line contains "dummy", so grep -v drops BOTH entries and the result is empty.
# That single bug cost an entire debugging session: the gadget bind failed with
# "No such device" on a device that had a perfectly good musb-hdrc UDC all
# along, and every downstream conclusion - flapping controller, host-mode role,
# hostile function creation - was chasing an artefact of it.
#
# Globbing the directory avoids parsing ls output at all.
first_real_udc() {
    for _u in /sys/class/udc/*; do
        [ -e "$_u" ] || continue
        _n=${_u##*/}
        case "$_n" in *dummy*) continue ;; esac
        echo "$_n"
        return 0
    done
    return 1
}

have_real_udc() { [ -n "$(first_real_udc)" ]; }

load_usb_prereq_modules() {
    [ -d /lib/modules ] || return 0
    have_real_udc && { tell_kmsg "initrd: UDC already present"; return 0; }

    # The named chain first - the Type-C controller decides peripheral vs host
    # mode, and the working Droidian MT6789 port says USB setup requires it.
    for m in $USB_PREREQ_MODULES; do
        [ -f "/lib/modules/$m.ko" ] || continue
        insmod "/lib/modules/$m.ko" 2>/dev/null && tell_kmsg "initrd: loaded $m"
    done
    have_real_udc && { tell_kmsg "initrd: UDC after named chain"; return 0; }

    # Still nothing: the controller driver cannot *probe* until its clocks,
    # pinctrl and phy exist, and on MediaTek those are all vendor modules too
    # (clk-mt6789, pinctrl-mt6789, phy-mtk-tphy ...). Rather than hardcode that
    # chain - guessing it wrong has cost this port several hours - walk the
    # vendor's own modules.load, which is already correctly ordered, and stop at
    # the first module that produces a UDC.
    [ -f /lib/modules/modules.load ] || return 1
    tell_kmsg "initrd: no UDC yet, walking modules.load for one"
    n=0
    while read -r m; do
        [ -n "$m" ] || continue
        insmod "/lib/modules/$m" 2>/dev/null
        n=$((n+1))
        if have_real_udc; then
            tell_kmsg "initrd: UDC appeared after $n modules (last: $m)"
            return 0
        fi
    done < /lib/modules/modules.load
    tell_kmsg "initrd: walked $n modules, still no UDC"
    return 1
}

panic() {
    tell_kmsg "$distro_name initramfs failed:"
    tell_kmsg "$1"

    # Bring up the hardware this shell needs, with the REAL loader.
    #
    # Measured on the MP01, from a log recovered off a spare partition because
    # the device has no console at all:
    #
    #   [0.73s] no UDC yet, walking modules.load for one
    #   [5.84s] walked 159 modules, still no UDC    <- raw modules.load order
    #   [5.86s] resolved dependency order for 159 modules
    #   [9.68s] userdata partition appeared: sdc59
    #   [9.69s] udc=[dummy_udc.0 musb-hdrc]         <- appears only now
    #
    # modules.load is a LIST, not an order. Walking it directly loads almost
    # nothing, so musb-hdrc never probes and /sys/class/udc holds only
    # dummy_udc.0 - which is precisely why the debug gadget never appeared.
    # load_kernel_modules() resolves modules.dep and softdep the way libmodprobe
    # does, and then a real UDC exists.
    load_usb_prereq_modules
    load_kernel_modules
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

    # Bind to a real UDC, RETRYING - the controller comes and goes.
    #
    # Measured on the MP01: musb-hdrc is present one moment and absent the next,
    # while the Type-C attach thread runs (mt6375_tcpc_probe succeeds,
    # typec_attach_thread starts, and MUSB in dual-role only keeps a UDC
    # registered while the port is in peripheral mode). A single sample of
    # /sys/class/udc therefore reads empty about as often as not, the write fails
    # with "No such device", and there is no gadget - which is what made this
    # device look completely dead for hours.
    #
    # grep -v dummy is mandatory: dummy_udc.0 sorts first, accepts the bind, and
    # does nothing.
    bound=0
    i=0
    while [ $i -lt 100 ]; do
        udc=$(first_real_udc)
        if [ -n "$udc" ] && [ -e $GADGET_DIR/g1/UDC ]; then
            if echo "$udc" > $GADGET_DIR/g1/UDC 2>/dev/null; then
                tell_kmsg "initrd: gadget bound to UDC $udc after $i tries"
                bound=1
                break
            fi
        fi
        i=$((i+1))
        usleep 200000
    done
    [ $bound -eq 1 ] || tell_kmsg "initrd: NO UDC after 20s - no adb, no telnet, no acm"

    # gadget is live - the network interface exists only from this point on
    start_debug_network

    /bin/sh
}

mount_kernel_modules() {
    # Avoid overriding kernel modules in LuneOS
    tell_kmsg "Skip overriding of kernel modules"
}

# Create /dev/<partname> symlinks from sysfs PARTNAME.
#
# mountroot() locates the data partition with
#     part=$(find /dev -name userdata | tail -1)
# i.e. it expects a device node *named* userdata. mdev only creates kernel names
# (/dev/sdc59 here) and no by-name links, so on this device the search finds
# nothing and mountroot panics with "Couldn't find data partition" - while the
# partition is present, mounted cleanly by hand, and holding both rootfs.img and
# android-rootfs.img.
#
# udev-based systems get /dev/disk/by-partlabel for free; a busybox mdev
# initramfs does not. Synthesising the links keeps mountroot unmodified and
# works for every partlist entry (userdata/UDA/DATAFS/USERDATA) and for the
# A/B-suffixed lookups further down.
#
# No-op where the links already exist.
create_partition_links() {
    for _blk in /sys/class/block/*; do
        [ -r "$_blk/uevent" ] || continue
        _pn=$(sed -n 's/^PARTNAME=//p' "$_blk/uevent" 2>/dev/null)
        [ -n "$_pn" ] || continue
        _dev="/dev/${_blk##*/}"
        [ -e "$_dev" ] || continue
        [ -e "/dev/$_pn" ] || ln -sf "$_dev" "/dev/$_pn"
    done
    tell_kmsg "initrd: created /dev/<partname> links ($(ls /dev/userdata 2>/dev/null && echo 'userdata ok' || echo 'no userdata'))"
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

# "initrd_heartbeat" - answer ONE question with no console, no USB and no
# working expdb: does our init run at all?
#
# If it does, exiting panics the kernel; CONFIG_PANIC_TIMEOUT=-1 reboots
# immediately; the device therefore loops boot->panic->boot every few seconds,
# and MediaTek's preloader enumerates on each pass. So a REPEATING 0e8d:2000 on
# the host's lsusb means "init runs"; a single one followed by silence means it
# does not. No log required, which is the point - on this device every other
# channel depends on something that may itself be broken.
#
# Diagnostic only, gated on the cmdline.
if grep -q initrd_heartbeat /proc/cmdline 2>/dev/null; then
    tell_kmsg "HEARTBEAT: init is running; exiting to panic so the host sees a reboot loop"
    exit 1
fi

# "initrd_probe_udc" - a one-bit answer over the only channel this device has.
#
# Nothing else works here: no UART, an E Ink panel that holds its last image, no
# USB gadget (that is what we are testing), and expdb turns out to be written
# only by the hardware-watchdog path, not by a kernel panic - so it has been
# frozen on one old record all night.
#
# What DOES work: exiting init panics the kernel, CONFIG_PANIC_TIMEOUT=-1
# reboots immediately, and the preloader enumerates on every pass. So a
# repeating 0e8d:2000 on the host is an observable "true", and a single
# appearance followed by silence is "false". Crude, but it is a measurement
# rather than an inference, which is more than anything else here has offered.
#
#   REBOOT LOOP  -> a real (non-dummy) UDC exists after the tcpc chain loaded
#   ONE, SILENCE -> it does not, and the gadget can never bind
# "initrd_report_code" - a MULTI-bit channel on a device that has none.
#
# One bit per flash (reboot-loop = true) is correct but far too slow. The same
# trick carries a number: sleep N seconds before exiting, and the interval
# between preloader appearances on the host becomes N + boot time (~12 s here,
# measured). So the host reads a status code off lsusb timing alone - no
# console, no gadget, no expdb.
#
#   code 5  : musb_hdrc.ko absent or insmod failed
#   code 15 : musb loaded, still no UDC
#   code 25 : a real UDC exists
#   code 35 : UDC exists AND the gadget bound to it
# "initrd_log_to_part" - write the kernel log to an UNUSED partition so it can
# be read back over the preloader with mtkclient.
#
# This device has no other channel. No UART; no USB gadget (nothing in the
# vendor module set creates a UDC - measured); fbcon is impossible because
# MediaTek's mtk_drm_fbdev.c does not build and the stock mediatek-drm.ko has no
# fbdev anyway; and expdb is only written by the hardware-watchdog path, so a
# kernel panic leaves no record. That left one bit per flash cycle, which cannot
# debug a boot.
#
# init_boot_a is 8 MiB and genuinely unused on this device - the MT6789 scatter
# declares it "file_name: NONE, is_download: false" and the factory image ships
# no init_boot.img - so writing there destroys nothing and is undone by any
# future firmware flash. Afterwards:
#
#   mtk r init_boot_a log.bin && strings log.bin | head -200
#
# Requires storage to be up, which is itself informative: if the log appears at
# all, the storage modules loaded.
# "initrd_probe_modules" - the assumption this whole port rests on, finally
# tested: is /lib/modules actually there?
#
# load_kernel_modules() opens with
#     [ -f /lib/modules/modules.load ] || return 0
# so if the bootloader does NOT concatenate vendor_boot's ramdisk under ours,
# every module silently fails to load - no storage, no USB, no watchdog - and
# the function reports nothing at all. Every "storage does not come up" result
# so far is equally consistent with that.
#
#   REBOOT LOOP  -> /lib/modules/modules.load exists (concatenation works)
#   NO LOOP      -> it does not, and nothing was ever going to load
# "initrd_count_loaded" - report how many modules actually loaded, as a reboot
# interval. /lib/modules is confirmed present (see initrd_probe_modules), so if
# storage still does not come up the question is how many of them take.
#
# Encoding: sleep (loaded / 10) seconds before exiting, so the host sees
# interval = 12s baseline + N/10. 180 modules all loading -> ~30s; none -> ~12s.
if grep -q initrd_count_loaded /proc/cmdline 2>/dev/null; then
    load_usb_prereq_modules
    load_kernel_modules
    loaded=$(lsmod 2>/dev/null | wc -l)
    blocks=$(ls /sys/class/block 2>/dev/null | wc -l)
    named=$(for b in /sys/class/block/*; do sed -n 's/^PARTNAME=//p' "$b/uevent" 2>/dev/null; done | grep -c .)
    tell_kmsg "COUNT: loaded=$loaded blocks=$blocks named=$named"

    # Report BLOCK DEVICES now, not module count - we know ~all modules load.
    # The open question is whether storage produced any block devices at all,
    # or whether it did and the PARTNAME lookup is what failed.
    #
    #   ~12s baseline + blocks seconds
    #     no extra      -> zero block devices; UFS/MMC never probed
    #     +5..+80       -> that many block devices exist, so the partition
    #                      search (PARTNAME in sysfs uevent) is the broken part
    sleep $blocks
    exit 1
fi

if grep -q initrd_probe_modules /proc/cmdline 2>/dev/null; then
    n=$(ls /lib/modules/*.ko 2>/dev/null | wc -l)
    tell_kmsg "PROBEMOD: /lib/modules has $n .ko; modules.load $([ -f /lib/modules/modules.load ] && echo present || echo ABSENT)"
    if [ -f /lib/modules/modules.load ]; then
        exit 1
    fi
    while :; do sleep 3600; done
fi

if grep -q initrd_log_to_part /proc/cmdline 2>/dev/null; then
    # Use the REAL loader, not a naive walk of modules.load.
    #
    # modules.load is a list, not an order - load_kernel_modules() resolves
    # modules.dep and softdep the way libmodprobe does, because taking the file
    # as an order is measurably wrong (335 hard-dependency pairs listed backwards
    # on bluejay). An earlier version of this probe insmod'd straight down the
    # file and reported "storage did not come up", which was an artefact of the
    # wrong order rather than a fact about the device.
    tell_kmsg "LOGPART: loading modules (dependency-resolved) so storage exists"
    load_usb_prereq_modules
    load_kernel_modules
    tell_kmsg "LOGPART: udc=[$(ls /sys/class/udc 2>/dev/null | tr '\n' ' ')]"
    tell_kmsg "LOGPART: modules=$(lsmod 2>/dev/null | wc -l) blocks=[$(ls /sys/class/block 2>/dev/null | tr '\n' ' ')]"

    # Find init_boot on this slot by PARTNAME, the same way mountroot does.
    # Match init_boot on ANY slot rather than composing the name from
    # androidboot.slot_suffix.
    #
    # The earlier version searched for "init_boot$slot", which silently becomes
    # plain "init_boot" whenever slot_suffix is missing from /proc/cmdline - and
    # that partition does not exist, the real ones being init_boot_a/_b. It then
    # reported "storage did not come up", which was wrong and cost hours: there
    # are ~94 block devices, measured. Storage was never the problem.
    target=""
    for blk in /sys/class/block/*; do
        pn=$(sed -n 's/^PARTNAME=//p' "$blk/uevent" 2>/dev/null)
        case "$pn" in
            init_boot*)
                # Make the device node ourselves from sysfs major:minor.
                #
                # /dev has NO block nodes here: start_mdev() runs after the
                # debug branch, so nothing has populated them. An earlier
                # version just used /dev/<name>, dd wrote to a path that did not
                # exist, and both init_boot partitions read back as all zeros -
                # while the success signal still fired, because `target` was
                # non-empty. A false positive that looked exactly like a false
                # negative.
                devnum=$(cat "$blk/dev" 2>/dev/null)   # e.g. "8:33"
                maj=${devnum%%:*}
                min=${devnum##*:}
                target=/dev/logpart
                rm -f "$target"
                if mknod "$target" b "$maj" "$min" 2>/dev/null; then
                    tell_kmsg "LOGPART: $pn is $devnum -> $target"
                else
                    tell_kmsg "LOGPART: mknod failed for $pn ($devnum)"
                    target=""
                fi
                break ;;
        esac
    done

    # Two channels at once, because neither alone has been enough:
    #
    #   reboot loop observed  -> the write happened; go read init_boot_a
    #   no loop, device idle  -> storage never came up, nothing was written
    #
    # The one-bit signal is what tells us WHICH, since the explanatory kmsg line
    # would itself be unreadable if storage is down.
    if [ -n "$target" ]; then
        tell_kmsg "LOGPART: writing to $target"
        sleep 2
        # Write STRAIGHT to the block device. The previous version staged into
        # /tmp_log and dd'd it across; the partition came back all zeros, which
        # means the temp file was empty (or dd never ran) - and a marker is the
        # only way to tell that apart from "the write failed". Shell redirection
        # to a block device needs no temp file and no dd.
        {
            echo "=== LUNEOS INITRAMFS LOG ==="
            echo "cmdline: $(cat /proc/cmdline 2>/dev/null)"
            echo "udc: [$(ls /sys/class/udc 2>/dev/null | tr '\n' ' ')]"
            echo "modules loaded: $(lsmod 2>/dev/null | wc -l)"
            echo "block devices: $(ls /sys/class/block 2>/dev/null | wc -l)"
            echo "--- partnames ---"
            for b in /sys/class/block/*; do
                sed -n 's/^PARTNAME=/  /p' "$b/uevent" 2>/dev/null
            done
            echo "--- usb role controls ---"
            # musb-hdrc came up as a HOST ("new USB bus registered, assigned bus
            # number 2"). In dual-role it still shows in /sys/class/udc, but a
            # gadget bind against a host-mode controller fails with exactly the
            # "No such device" we see. So find the role switch.
            echo "proc/mtk_usb: [$(ls /proc/mtk_usb 2>/dev/null | tr '\n' ' ')]"
            for f in /proc/mtk_usb/*; do
                [ -f "$f" ] && echo "  $f = [$(cat "$f" 2>/dev/null | head -1)]"
            done
            mount -t debugfs none /sys/kernel/debug 2>/dev/null
            echo "musb debugfs: [$(ls /sys/kernel/debug/musb-hdrc 2>/dev/null | tr '\n' ' ')]"
            echo "musb mode: [$(cat /sys/kernel/debug/musb-hdrc/mode 2>/dev/null)]"
            echo "udc dir: [$(ls /sys/class/udc 2>/dev/null | tr '\n' ' ')]"
            for u in /sys/class/udc/*; do
                echo "  $(basename $u): state=[$(cat $u/state 2>/dev/null)] func=[$(cat $u/function 2>/dev/null)] soft=[$(cat $u/soft_connect 2>/dev/null)]"
            done
            echo "otg/role files:"
            find /sys/devices -maxdepth 6 \( -name "mode" -o -name "role" -o -name "otg_mode" -o -name "cmode" \) -path "*usb*" 2>/dev/null | head -10
            echo "--- usb gadget attempt ---"
            mkdir -p /config 2>/dev/null
            mount -t configfs none /config 2>/dev/null
            echo "configfs mounted: $(mountpoint -q /config && echo yes || echo no)"
            echo "gadget dir exists: $([ -d /config/usb_gadget ] && echo yes || echo no)"
            echo "android_usb exists: $([ -d /sys/class/android_usb/android0 ] && echo yes || echo no)"
            echo "udc BEFORE setup: [$(ls /sys/class/udc 2>/dev/null | grep -v dummy | head -1)]"
            # Try binding BEFORE any function exists. If this works, creating
            # functions is what breaks the controller; if it fails the same way,
            # the UDC is unusable from the start and the problem is upstream.
            mkdir -p /config 2>/dev/null; mount -t configfs none /config 2>/dev/null
            mkdir -p $GADGET_DIR/early 2>/dev/null
            early_udc=$(first_real_udc)
            echo "early bind udc=[$early_udc]"
            echo "$early_udc" > $GADGET_DIR/early/UDC 2>/tmp_e
            echo "early bind rc=$? err=[$(cat /tmp_e 2>/dev/null)]"
            echo "udc AFTER early bind: [$(ls /sys/class/udc 2>/dev/null | grep -v dummy | head -1)]"
            echo "" > $GADGET_DIR/early/UDC 2>/dev/null
            rmdir $GADGET_DIR/early 2>/dev/null

            usb_setup_configfs "logprobe" 2>&1
            echo "udc AFTER usb_setup_configfs: [$(ls /sys/class/udc 2>/dev/null | grep -v dummy | head -1)]"
            echo "after setup, g1 exists: $([ -d /config/usb_gadget/g1 ] && echo yes || echo no)"
            echo "functions: [$(ls /config/usb_gadget/g1/functions 2>/dev/null | tr '\n' ' ')]"
            echo "configs c.1: [$(ls /config/usb_gadget/g1/configs/c.1 2>/dev/null | tr '\n' ' ')]"
            real_udc=$(first_real_udc)
            echo "real udc: [$real_udc]"
            echo "$real_udc" > /config/usb_gadget/g1/UDC 2>/tmp_err
            echo "UDC write rc=$? err=[$(cat /tmp_err 2>/dev/null)]"
            echo "UDC now reads: [$(cat /config/usb_gadget/g1/UDC 2>/dev/null)]"
            echo "--- lsmod ---"
            lsmod 2>/dev/null
            echo "--- dmesg ---"
            dmesg 2>/dev/null
            echo "=== END ==="
        } > "$target" 2>/dev/null
        sync
        sleep 2
        tell_kmsg "LOGPART: written, exiting to signal success via reboot loop"
        exit 1
    fi
    tell_kmsg "LOGPART: no init_boot partition - storage did not come up; halting"
    while :; do sleep 3600; done
fi

if grep -q initrd_report_code /proc/cmdline 2>/dev/null; then
    code=5
    if [ -f /lib/modules/musb_hdrc.ko ]; then
        insmod /lib/modules/musb_main.ko 2>/dev/null
        if insmod /lib/modules/musb_hdrc.ko 2>/dev/null; then
            code=15
        fi
    fi
    load_usb_prereq_modules
    have_real_udc && code=25
    tell_kmsg "CODE: reporting $code via reboot interval"
    sleep $code
    exit 1
fi

if grep -q initrd_probe_udc /proc/cmdline 2>/dev/null; then
    load_usb_prereq_modules
    probe_udc=$(first_real_udc)
    if [ -n "$probe_udc" ]; then
        tell_kmsg "PROBE: UDC present ($probe_udc) - exiting to signal TRUE via reboot loop"
        exit 1
    fi
    tell_kmsg "PROBE: no UDC - sleeping forever to signal FALSE (no reboot loop)"
    while :; do sleep 3600; done
fi

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

# Load override modules before the vendor's, so ours win.
#
# Some vendor prebuilt modules have to be replaced rather than supplemented:
# the driver we need to change ships as a .ko built against the vendor's own
# kernel, so patching our source does nothing - the vendor's binary is what
# loads. insmod refuses a second copy of an already-loaded module, so the only
# lever is to get ours in first.
#
# On the MP01 this is pinctrl-mtk-v2.ko. The SoC pin table has 222 pins while
# the DT's gpio-ranges maps 188, so GPIO 188 - rt5133's HWEN - has no pinctrl
# range and its request returns -517 forever. That stalls rt5133, its LDOs,
# then gpufreq, ged and finally mali, so the device never gets /dev/mali0 and
# the compositor cannot create a GPU context. Our build of the same module
# carries a gpiochip_add_pin_range() call covering the whole chip.
#
# Anything dropped in /override/modules is loaded here, in plain filename
# order, before the vendor set. Failures are not fatal: a module that will not
# insert leaves the vendor's copy to load normally a moment later, which is the
# same behaviour as not shipping an override at all.
load_override_modules() {
    [ -d /override/modules ] || return 0
    for _m in /override/modules/*.ko; do
        [ -e "$_m" ] || continue
        if insmod "$_m" 2>/dev/null; then
            tell_kmsg "initrd: override: loaded ${_m##*/}"
        else
            tell_kmsg "initrd: override: FAILED ${_m##*/} (vendor copy will load instead)"
        fi
    done
}

# Replace vendor modules with our own builds of the same name.
#
# Different from load_override_modules() below, and needed because that
# mechanism cannot carry a module with real dependencies: overrides are inserted
# *before* the vendor set, so anything importing vendor symbols (mediatek-drm
# imports 487 of them) fails to insmod and the vendor copy loads anyway.
#
# Replacing the file instead lets load_kernel_modules() insert ours in the
# correct dependency order. It has to be a copy rather than a cpio layering
# trick: the bootloader concatenates the vendor_boot ramdisk and ours, and while
# later archives do win, the vendor ships a real /lib/modules directory whereas
# this rootfs has /lib as a symlink to usr/lib - so our files land in
# /usr/lib/modules and never appear at the path the vendor set is loaded from.
#
# Recipes install replacements to ${nonarch_base_libdir}/modules, i.e.
# /usr/lib/modules here. On the MP01 that is mediatek-drm.ko, patched to create
# the standard DRM plane properties the vendor hwcomposer needs.
replace_vendor_modules() {
    [ -d /usr/lib/modules ] || return 0
    [ -d /lib/modules ] || return 0
    for _m in /usr/lib/modules/*.ko; do
        [ -e "$_m" ] || continue
        _n=${_m##*/}
        [ -e "/lib/modules/$_n" ] || continue
        if cp "$_m" "/lib/modules/$_n" 2>/dev/null; then
            tell_kmsg "initrd: replaced vendor module $_n with ours"
        else
            tell_kmsg "initrd: FAILED to replace vendor module $_n"
        fi
    done
}

echo "Replacing vendor modules" > /dev/kmsg
replace_vendor_modules

echo "Loading override modules" > /dev/kmsg
load_override_modules

echo "Loading kernel modules" > /dev/kmsg
load_kernel_modules
mirror_trusty_log

echo "Starting mdev" > /dev/kmsg
start_mdev

# mountroot() searches /dev by partition NAME; mdev only makes kernel names.
create_partition_links

# Refuse to boot on a battery too flat to survive it, and charge instead.
#
# Learned the hard way on the MP01 (15 Sep 2026). That device never powers off -
# holding power just reboots it, plugged in or not - so the usual "power off and
# charge" state does not exist. Charging only happens while a kernel is running,
# because MediaTek does the charging algorithm in-kernel
# (drivers/power/supply/mtk_charger.c runs charger_routine_thread). lk and
# fastboot therefore charge nothing at all.
#
# Booting all the way to the UI draws more than a 500mA USB port supplies, so a
# low battery just gets lower, and eventually the device browns out mid-transfer
# and cannot even be reflashed:
#
#   fastboot: Sending 'boot_b' FAILED (Write to device failed in SendBuffer()
#             (Cannot send after transport endpoint shutdown))
#   host dmesg: usb 1-1: USB disconnect, device number 50
#
# The escape is to stop here instead. Modules are already loaded at this point,
# so the charger and Type-C drivers are up and the in-kernel thread is charging;
# idling in the initramfs is about the lowest-draw state we can offer while
# still charging. Boot proceeds normally as soon as there is enough charge.
#
# Everything here is best-effort and fails open: no battery node, an unreadable
# or non-numeric capacity, or a device that reports "Full" all mean "just boot".
LOW_BATTERY_THRESHOLD="${LOW_BATTERY_THRESHOLD:-5}"

battery_capacity() {
    for _b in /sys/class/power_supply/*/capacity; do
        [ -r "$_b" ] || continue
        # Skip anything that is not the battery itself - USB/charger supplies
        # also carry a capacity node on some MediaTek trees.
        _t="$(dirname "$_b")/type"
        [ -r "$_t" ] && [ "$(cat "$_t" 2>/dev/null)" = "Battery" ] || continue
        _c=$(cat "$_b" 2>/dev/null)
        case "$_c" in ''|*[!0-9]*) continue ;; esac
        echo "$_c"; return 0
    done
    return 1
}

wait_if_battery_flat() {
    grep -q no_low_battery_hold /proc/cmdline 2>/dev/null && return 0

    _cap=$(battery_capacity) || {
        tell_kmsg "initrd: no battery capacity node, not holding"
        return 0
    }
    [ "$_cap" -gt "$LOW_BATTERY_THRESHOLD" ] && {
        tell_kmsg "initrd: battery ${_cap}%, continuing boot"
        return 0
    }

    tell_kmsg "initrd: battery ${_cap}% is below ${LOW_BATTERY_THRESHOLD}% - holding here to charge."
    tell_kmsg "initrd: the in-kernel charger thread is running; boot resumes above the threshold."
    tell_kmsg "initrd: override with no_low_battery_hold on the kernel cmdline."

    while :; do
        sleep 30
        _cap=$(battery_capacity) || return 0
        tell_kmsg "initrd: charging, battery ${_cap}%"
        [ "$_cap" -gt "$LOW_BATTERY_THRESHOLD" ] && {
            tell_kmsg "initrd: battery ${_cap}%, resuming boot"
            return 0
        }
    done
}

wait_if_battery_flat

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
