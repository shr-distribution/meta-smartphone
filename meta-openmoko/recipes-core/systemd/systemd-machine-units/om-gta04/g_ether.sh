#!/bin/sh

# This script runs very early in order to ensure that the USB network comes
# up at first system boot.  It gathers or creates the necessary host and device
# mac addresses for the g_ether module at first boot, and writes them to the
# /etc/modprobe.conf/g_ether.conf file.
#
# Due to some oddness with udev, it also modprobes g_ether, only on first boot.

# If the g_ether module options file exists, nothing to do.

if [ ! -e /etc/modprobe.d/g_ether.conf ] ; then

# Gather up all g_ether parameters passed in on the kernel command line, and
# make sure to pass them to the module; this will ensure similar behavior
# between the module and built-in.

# Begin by searching the command line for the host and dev addrs
da=`sed -n -e 's|.*g_ether\.dev_addr\=\(..:..:..:..:..:..\).*|\1|p' /proc/cmdline`
ha=`sed -n -e 's|.*g_ether\.host_addr\=\(..:..:..:..:..:..\).*|\1|p' /proc/cmdline`

# If the device address is missing, try to find it some other way
if [ -z "$da" ] ; then

  echo "nothing to do";
  # TODO: add code to compute a static random address for the GTA01.
  #       For now, GTA04 owners should probably set the addresses in their
  #       u-boot environment and pass in on the command line.  A reasonable
  #       solution would be to the random prefix range with the last part of
  #       the mac copied from the bluetooth device.

fi

# If have a device address, now we need to sort out the host address.  If it
# is unspecified, or if it is the same as the device address (Qi does this),
# compute a new address by just incrementing the device address.

if [ -n "$da" ] ; then
  if [ -z "$ha" -o "$da" = "$ha" ] ; then

    # We need to compute a new address - split the device address into two
    # part, and increment the second part.
    pfx=`echo "$da" | sed -n -e 's|\(..:..:..:\)..:..:..|\1|p'`
    i=`echo "$da" | sed -n -e 's|..:..:..:\(..\):\(..\):\(..\)|0x\1\2\3|p'`

    # Now increment the mac addr
    i=$(printf %06x $(($i+1)))

    # And glue the parts back together again.
    i=`echo "$i" | sed -n -e 's|\(..\)\(..\)\(..\)|\1:\2:\3|p'`

    # Assign the computed host address, if this all worked out.
    [ -n "$i" -a -n "$pfx" ] && ha=$pfx$i

  fi
fi

# Compute the command-line options themselves
[ -n "$da" ] && daddr="dev_addr=$da"
[ -n "$ha" ] && haddr="host_addr=$ha"

# Write the module options file out even if we have no addresses to write,
# so that we do not need to run again at next boot.
[ -d /etc/modprobe.d ] || mkdir /etc/modprobe.d
echo "options g_ether $daddr $haddr" >/etc/modprobe.d/g_ether.conf

# And now, since this is first boot, we need to probe the modules
modprobe g_ether 2>/dev/null || true

fi

# power up interface
ifup -f usb0
