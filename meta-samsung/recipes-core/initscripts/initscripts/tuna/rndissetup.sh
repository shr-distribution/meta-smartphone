#!/bin/sh
### BEGIN INIT INFO
# Provides: rndissetup
# Required-Start:    mountkernfs
# Required-Stop:     mountkernfs
# Default-Start:     S
# Default-Stop:
### END INIT INFO

#get the device serial number from /proc/cmdline directly(since we have no getprop on GNU/Linux)
serialno="$(cat /proc/cmdline | sed 's/.*androidboot.serialno=//' | sed 's/ .*//')"

#on init
echo "${serialno}" > /sys/class/android_usb/android0/iSerial
echo "Samsung" > /sys/class/android_usb/android0/f_rndis/manufacturer
echo "04e8" > /sys/class/android_usb/android0/f_rndis/vendorID
echo "1" > /sys/class/android_usb/android0/f_rndis/wceis

#on boot
echo "unknown" > /sys/class/android_usb/android0/iManufacturer
echo  "Full AOSP on Maguro" > /sys/class/android_usb/android0/iProduct

#rndis
echo "0" > /sys/class/android_usb/android0/enable
echo "04e8" > /sys/class/android_usb/android0/idVendor
echo "6863" > /sys/class/android_usb/android0/idProduct
echo "rndis" > /sys/class/android_usb/android0/functions
echo "224" > /sys/class/android_usb/android0/bDeviceClass
echo "1" >  /sys/class/android_usb/android0/enable 1

# wait a little bit until the configuration has accepted; in cases where we're using
# upstart together with sysvinit scripts processing might be to fast and network
# configuration can't be populated as interface is not available
sleep 1
