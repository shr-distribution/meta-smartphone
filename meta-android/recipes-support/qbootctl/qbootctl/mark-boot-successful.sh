#!/bin/sh
# Confirm the current A/B slot to the bootloader, and leave a retry banked.
#
# "qbootctl -m" on its own sets the success flag but does not touch the retry
# counter, so the slot ends up as
#
#     boot_a  flag=0x47  prio=3 active=1 retry=0 successful=1
#
# which is fine right up until something clears the success flag - which is
# exactly what flashing does. The next boot is then retry=0 and successful=0,
# the bootloader has no attempts left to spend, and the device needs
# "fastboot --set-active=a" by hand.
#
# Setting the slot active first re-arms the counter. qbootctl writes
# AB_SLOT_ACTIVE_VAL, which is 0xF here, so that is one attempt rather than the
# seven the field can hold - enough to cover the post-flash boot, which is the
# case that actually bites:
#
#     qbootctl -s   boot_a  flag=0x0f  retry=1 successful=0
#     qbootctl -m   boot_a  flag=0x4f  retry=1 successful=1
#
# Order matters: -s clears the success flag, so -m has to come second. Losing
# power between the two is harmless - the slot is left bootable with a retry.
#
# The slot index is taken from the command line the bootloader supplied rather
# than from "qbootctl -c", and mapped explicitly. Nothing here is allowed to
# guess: "qbootctl -s" takes an index, and feeding it something that atoi()s to
# 0 while running from _b would hand the next boot to the wrong slot.
set -u

suffix=$(sed -n 's/.*androidboot\.slot_suffix=\(_[ab]\).*/\1/p' /proc/cmdline)
case "$suffix" in
    _a) slot=0 ;;
    _b) slot=1 ;;
    *)  echo "no A/B slot suffix on the command line, nothing to do"; exit 0 ;;
esac

qbootctl -s "$slot" || echo "could not re-arm slot $suffix; marking anyway"
exec qbootctl -m
