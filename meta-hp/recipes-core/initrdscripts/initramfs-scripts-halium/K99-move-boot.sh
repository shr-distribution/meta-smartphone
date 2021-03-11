#!/bin/sh

# Disable MPDEC for now until android layer starts up and make sure atleast
# one CPU stays online to not hangup the boot
#echo 0 > /sys/kernel/msm_mpdecision/conf/enabled
echo 1 > /sys/devices/system/cpu/cpu1/online
echo 0 > /sys/module/pm_8x60/modes/cpu0/power_collapse/idle_enabled

# Move /mnt/boot to a location under the new root
tell_kmsg "rootmount: ${rootmnt}"
mkdir ${rootmnt}/android/boot

for a in `ls ${rootmnt}/android/boot`
do
   tell_kmsg "Contents before mount --move in ${rootmnt}/android/boot: $a"
done

mount --move /mnt/boot ${rootmnt}/android/boot

for b in `ls ${rootmnt}/android/boot`
do
   tell_kmsg "Contents after mount --move in ${rootmnt}/android/boot: $b"
done
