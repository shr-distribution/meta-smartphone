#!/system/bin/sh
# Start rild only once the modem subsystem is online.
#
# The container's ueventd loads the modem firmware (mba.mdt, modem.mdt) when the modem driver asks
# for it, about 22 s into the boot, and the modem takes a while to come up. init starts rild at
# about 24 s, from "class main". A rild that starts before the modem is online comes up half
# initialised (qcril_qmi_presence_init fails, the first getIccCardStatus never returns), ofono
# registers the modem with "Features = sim" only, and after that the network search stays at
# "searching". Restarting rild once the modem is up fixes all of it, so wait for that instead.
n=0
ready=""
while [ -z "$ready" ] && [ $n -lt 120 ]; do
    for d in /sys/bus/msm_subsys/devices/*; do
        # "modem" is the SoC's own baseband (hammerhead); a kernel with an external one over HSIC
        # may name it "external_modem".
        name=$(cat $d/name 2>/dev/null)
        if { [ "$name" = modem ] || [ "$name" = external_modem ]; } && [ "$(cat $d/state 2>/dev/null)" = ONLINE ]; then
            ready=1
        fi
    done
    # A 3.4 kernel without msm_subsys (mako) has no such node, and rild then started only after the
    # whole 120 s. There the modem is an MDM9215 on HSIC: once it has booted it enumerates on USB and
    # rmnet_usb binds its QMI interfaces (1-1:1.5 and on), which is what qmuxd and qcril talk through.
    for b in /sys/bus/usb/drivers/rmnet_usb/*:*; do
        [ -e "$b" ] && ready=1
    done
    [ -z "$ready" ] && sleep 1
    n=$((n + 1))
done
# The QMI ports (qmuxd's) are up a moment after the subsystem says ONLINE.
sleep 3
exec /vendor/bin/hw/rild -l /vendor/lib/libril-qc-qmi-1.so
