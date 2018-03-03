#!/bin/sh

# (c) 2013 Canonical LTD.
# (c) 2013 Simon Busch <morphis@gravedo.de>
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the the GNU General Public License version 3, as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranties of
# MERCHANTABILITY, SATISFACTORY QUALITY or FITNESS FOR A PARTICULAR
# PURPOSE.  See the applicable version of the GNU Lesser General Public
# License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# /dev/pts needs to be there
mkdir -p $LXC_ROOTFS_PATH/dev/pts

mount -t devtmpfs none $LXC_ROOTFS_PATH/dev

mkdir $LXC_ROOTFS_PATH/sys
mount -t sysfs none $LXC_ROOTFS_PATH/sys

mount_bind_ro() {
	if [ ! -d $2 ] ; then
		mkdir $2
	fi
	mount --bind $1 $2
	mount -o remount,ro,bind $2
}

mount_bind_ro /system $LXC_ROOTFS_PATH/system
mount_bind_ro /system/vendor $LXC_ROOTFS_PATH/vendor
mount_bind_ro /firmware $LXC_ROOTFS_PATH/firmware
mount_bind_ro /system/etc $LXC_ROOTFS_PATH/etc
if [ -d /persist ] ; then
  mount_bind_ro /persist $LXC_ROOTFS_PATH/persist
fi

# usage existing /data directory, rw
mount -o bind /data $LXC_ROOTFS_PATH/data

# Avoid being spammed with "missing packages.list; retrying"
touch $LXC_ROOTFS_PATH/data/system/packages.list

# Process any overrides
if [ -d /var/lib/lxc/android/overrides ] ; then
	cp -a /var/lib/lxc/android/overrides/* $LXC_ROOTFS_PATH || true
fi

# Passing sockets between system and container
rm -Rf /dev/socket
mkdir -p /dev/socket $LXC_ROOTFS_PATH/socket
mount -n -o bind,rw /dev/socket $LXC_ROOTFS_PATH/socket
sed -i '/on early-init/a \    mkdir /dev/socket\n\    mount none /socket /dev/socket bind' $LXC_ROOTFS_PATH/init.rc

# Process any further scripts
run-parts /var/lib/lxc/android/pre-start.d || true
