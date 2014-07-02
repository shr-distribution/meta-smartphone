#!/bin/sh

if [ ! -d /dev/cpuctl ] && [ -d /sys/fs/cgroup/cpu ] ; then
	mkdir /dev/cpuctl
	mount --bind /sys/fs/cgroup/cpu /dev/cpuctl
fi

echo "Waiting for container to come up ..."
lxc-wait -n android -s RUNNING

containerpid="$(lxc-info -n android -p | grep PID | sed 's/^PID:.* //')"
while true; do
	echo "Checking for boot done flag ..."
	[ -f /proc/$containerpid/root/dev/.android_boot_done ] && break
	sleep 1
done
