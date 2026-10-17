#!/system/bin/sh
# Bring the WCNSS Bluetooth core up with the phone's own address before the HAL opens it: init.mako.bt.sh,
# which the HAL starts as hciattach (see mako-hciattach.rc), without its bdAddrLoader.
#
# The vendor's script reads the address with /vendor/bin/bdAddrLoader, which neither the vendor image nor
# the Halium 9 system it came from has (and its [$BDADDR == ""] tests are broken), so hci_qcomm_init got
# no address and the controller came up with one it picked itself. The address is six bytes, most
# significant first, in /persist/bluetooth/.bdaddr; a test address in net.btdut.address (as the vendor's
# script allows) goes first.

LOG_TAG="mako-bluetooth"

loge ()
{
  /system/bin/log -t $LOG_TAG -p e "$0: $*"
}

logi ()
{
  /system/bin/log -t $LOG_TAG -p i "$0: $*"
}

BDADDR=$(getprop net.btdut.address)
if [ -z "$BDADDR" ] && [ -r /persist/bluetooth/.bdaddr ]; then
  BDADDR=$(od -An -tx1 -N6 /persist/bluetooth/.bdaddr | tr -d ' \n' | sed 's/../&:/g; s/:$//')
fi
case "$BDADDR" in
  ??:??:??:??:??:??)
    [ "$BDADDR" = 00:00:00:00:00:00 ] && BDADDR="" ;;
  *)
    BDADDR="" ;;
esac

case $(getprop qcom.bt.dev_power_class) in
  1) PWR_CLASS="-p 0" ;;
  2) PWR_CLASS="-p 1" ;;
  3) PWR_CLASS="-p 2" ;;
  *) PWR_CLASS="" ;;
esac

setprop vendor.bluetooth.status off
logi "BDADDR: $BDADDR transport: $(getprop ro.qualcomm.bt.hci_transport)"

# PWR_CLASS is unquoted on purpose: it is two words or none.
# shellcheck disable=SC2086
if [ -n "$BDADDR" ]; then
  /vendor/bin/hci_qcomm_init -b "$BDADDR" -e $PWR_CLASS -vv
else
  /vendor/bin/hci_qcomm_init -e $PWR_CLASS -vv
fi
rc=$?
if [ $rc -ne 0 ]; then
  loge "Bluetooth QSoC firmware download failed: exit code $rc"
  exit $rc
fi

setprop vendor.bluetooth.status on
logi "Bluetooth QSoC firmware download succeeded"
exit 0
