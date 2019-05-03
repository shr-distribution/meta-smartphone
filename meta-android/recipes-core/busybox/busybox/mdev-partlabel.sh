#!/bin/sh
# Script for mdev's uevent, which will create by-partlabel symlinks (like udev does)

case "$ACTION" in
    add|"")
        [ -d /dev/disk/by-partlabel ] || mkdir -p /dev/disk/by-partlabel
        [ -d /dev/disk/by-label ]     || mkdir -p /dev/disk/by-label
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
             source "/sys/class/block/$MDEV/uevent"
             if [ -n "$PARTNAME" ]; then
                # create the symlink
                [ -e /dev/disk/by-partlabel/"$PARTNAME" ] || ln -s "../../$MDEV" /dev/disk/by-partlabel/"$PARTNAME"
              fi
        fi

        if [ -e "/dev/$MDEV"  ]; then
            # Also give blkid a chance
            LABEL=$(blkid "/dev/$MDEV" | sed -n 's/.*LABEL=\"\([^\"]*\)\".*/\1/p')
            if [ -n "$LABEL" ]; then
                [ -e /dev/disk/by-label/"$LABEL" ] || ln -s "../../$MDEV" /dev/disk/by-label/"$LABEL"
            fi
        fi
        ;;
    remove)
        # find PARTNAME in /sys/class/block/$MDEV/uevent
        if [ -e "/sys/class/block/$MDEV/uevent" -a -e "/dev/$MDEV"  ]; then
            source "/sys/class/block/$MDEV/uevent"
            # remove the symlink
            [ -e /dev/disk/by-partlabel/"$PARTNAME" ] && rm /dev/disk/by-partlabel/"$PARTNAME"
        fi

        if [ -e "/dev/$MDEV"  ]; then
            # Also give blkid a chance
            LABEL=$(blkid "/dev/$MDEV" | sed -n 's/.*LABEL=\"\([^\"]*\)\".*/\1/p')
            if [ -n "$LABEL" ]; then
                [ -e /dev/disk/by-label/"$LABEL" ] && rm /dev/disk/by-label/"$LABEL"
            fi
        fi
        ;;
    *)
        # Unexpected keyword
        exit 1
        ;;
esac
