#!/bin/sh
# Run MediaTek's fuel gauge daemon in the host's network namespace.
#
# fuelgauged talks to the kernel battery driver over netlink family 26
# (NETLINK_FGD). The kernel creates that socket in the initial network namespace
# only, and the Android container runs in a network namespace of its own, so the
# container's fuelgauged never registers ("wakeup_fg_daemon: gm->fgd_pid
# exception, valule is 0"), the gauge algorithm never runs, and
# /sys/class/power_supply/battery/capacity stays at -1.
#
# So stop the container's instance and exec the same vendor binary in the
# container's mount, UTS and PID namespaces but the host's network namespace,
# as the user/group its rc file declares (system). Verified on the Minimal Phone
# MP01: the kernel logs FG_DAEMON_CMD_SET_DAEMON_PID and capacity goes from -1
# to a real value.
#
# Only the IPC namespace is left out of nsenter: the container shares the
# host's (lxc.namespace.keep), and on kernels without CONFIG_IPC_NS there is no
# namespace to enter at all ("reassociate to namespaces failed").

log() { echo "mtk-fuelgauged: $*"; }

in_android() { lxc-attach -n android -- /system/bin/sh -c "$1"; }

c=$(lxc-info -n android -pH 2>/dev/null)
case "$c" in
    ''|*[!0-9]*) log "android container is not running"; exit 1 ;;
esac

# The container's init starts it from "class core"; stopping it through init
# keeps init from restarting it behind our back.
in_android "stop fuelgauged" 2>/dev/null
i=0
while [ $i -lt 10 ] && in_android "pidof fuelgauged" >/dev/null 2>&1; do
    i=$((i + 1))
    sleep 1
done

log "starting /vendor/bin/fuelgauged in the host network namespace (container pid $c)"
exec nsenter -t "$c" -m -u -p --net=/proc/1/ns/net -S 1000 -G 1000 -- /vendor/bin/fuelgauged
