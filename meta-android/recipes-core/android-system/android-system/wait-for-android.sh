#!/bin/sh

echo "Waiting for container to come up ..."
lxc-wait -n android -s RUNNING

while true; do
	echo "Checking for Livedisplay service ..."
	sleep 1
	livedisplay_status=$(/usr/bin/getprop |grep "init.svc.*livedisplay" |grep -o "\[running\]")
	if [ "$livedisplay_status" = "[running]" ] ; then
		echo "Android Livedisplay service running"
		break
	fi
#	[ "$(getprop sys.init_boot_completed)" = "1" ] && break
done

sleep 1


