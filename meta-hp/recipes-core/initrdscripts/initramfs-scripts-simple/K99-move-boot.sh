#!/bin/sh

info "[initramfs] K99-move-boot: starting"

# Disable MPDEC for now until android layer starts up and make sure atleast
# one CPU stays online to not hangup the boot
#echo 0 > /sys/kernel/msm_mpdecision/conf/enabled
echo 1 > /sys/devices/system/cpu/cpu1/online 2>/dev/null
echo 0 > /sys/module/pm_8x60/modes/cpu0/power_collapse/idle_enabled 2>/dev/null

# Move /mnt/boot to a location under the new root
info "[initramfs] Moving boot mount to: /rfs/uboot"
mkdir -p /rfs/uboot
mount --move /mnt/boot /rfs/uboot
info "[initramfs] K99-move-boot: done"
