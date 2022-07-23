#!/bin/sh

# first, get the bluetooth address if possible
if [ -e /persist/bluetooth/.bdaddr ] ; then
	bdaddr=$(hexdump -e  '/1 "%X:"' /persist/bluetooth/.bdaddr)
else
	bdaddr=""
fi

# call hciattach (and strip the last char of bdaddr)
/usr/bin/hciattach ttyHS99 bcm43xx 4000000 flow sleep ${bdaddr%?}

sleep 1

# unblock bluetooth
/usr/sbin/rfkill unblock bluetooth
