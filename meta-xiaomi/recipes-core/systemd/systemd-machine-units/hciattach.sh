#!/bin/sh

#Maximum number of attempts to enable hcismd to try to get
# hci0 to come online.  Writing to sysfs too early seems to
# not work, so we loop.
MAXTRIES=15

#setprop bluetooth.hciattach true
setprop ro.qualcomm.bt.hci_transport smd
setprop qcom.bt.dev_power_class 2
setprop qcom.bt.le_dev_pwr_class 2

i=1
while [ ! $i -gt $MAXTRIES ]  ; do
	echo 1 > /sys/module/hci_smd/parameters/hcismd_set
	sleep 1
    if [ -e /sys/class/bluetooth/hci0 ] ; then
        # found hci0, so unblock bluetooth and exit successfully
		/usr/sbin/rfkill unblock bluetooth
		hciconfig hci0 up
		/usr/sbin/rfkill block bluetooth
        exit 0
    fi
    i=$((i+1))
done

# must have gotten through all our retries, fail
exit 1

