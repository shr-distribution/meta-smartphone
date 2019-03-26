#!/bin/sh
# Script for mdev's uevent, which will create by-partname symlinks (like udev does)

case "$ACTION" in
    add|"")
        ACTION="add"
        [ -d /dev/disk/by-partname ] || mkdir -p /dev/disk/by-partname
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
             source "/sys/class/block/$MDEV/uevent"
             # create the symlink
             [ -e /dev/disk/by-partname/"$PARTNAME" ] || ln -s "../../$MDEV" /dev/disk/by-partname/"$PARTNAME"
        fi
        ;;
    remove)
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
            source "/sys/class/block/$MDEV/uevent"
            # remove the symlink
            [ -e /dev/disk/by-partname/"$PARTNAME" ] && rm /dev/disk/by-partname/"$PARTNAME"
        fi
        ;;
    *)
        # Unexpected keyword
        exit 1
        ;;
esac
