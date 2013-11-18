#!/bin/sh

if [ ! -d /dev/cpuctl ] && [ -d /sys/fs/cgroup/cpu ] ; then
	mkdir /dev/cpuctl
	mount --bind /sys/fs/cgroup/cpu /dev/cpuctl
fi

lxc-wait -n android -s RUNNING
containerpid="$(lxc-info -n android | grep pid | sed 's/^pid:.* //')"
while true; do
	[ -f /proc/$containerpid/root/dev/.coldboot_done ] && break
	sleep 1
done
