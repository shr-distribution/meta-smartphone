#!/bin/sh

if [ -e /persist/wifi/.macaddr ] ; then
	echo "File /persist/wifi/.macaddr already exists"
	exit 0
fi

timeout=10
while [ ! -e /sys/class/net/wlan0 ] ; do
	sleep 1
	if [ "$timeout" -le 0 ]; then
		echo "Could not persist WiFi mac addr cause the network interface isn't availalbe"
		exit 0
	fi
	timeout=$(($timeout - 1))
done

mkdir -p /persist/wifi
chmod 755 /persist/wifi
# replace plain address by binary hex chars
wifi_mac="$(sed -e 's/\(..\)[:]\?/\\x\1/g' < /sys/class/net/wlan0/address)"
echo -ne $wifi_mac > /persist/wifi/.macaddr
