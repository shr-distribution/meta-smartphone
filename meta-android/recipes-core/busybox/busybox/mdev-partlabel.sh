#!/bin/sh
# Script for mdev's uevent, which will create by-partlabel symlinks (like udev does)

case "$ACTION" in
    add|"")
        [ -d /dev/disk/by-partlabel ] || mkdir -p /dev/disk/by-partlabel
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
             source "/sys/class/block/$MDEV/uevent"
             # create the symlink
             [ -e /dev/disk/by-partlabel/"$PARTNAME" ] || ln -s "../../$MDEV" /dev/disk/by-partlabel/"$PARTNAME"
        fi
        ;;
    remove)
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
            source "/sys/class/block/$MDEV/uevent"
            # remove the symlink
            [ -e /dev/disk/by-partlabel/"$PARTNAME" ] && rm /dev/disk/by-partlabel/"$PARTNAME"
        fi
        ;;
    *)
        # Unexpected keyword
        exit 1
        ;;
esac
