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

ramdisk=/usr/lib/android-system/ramdisk.img

if [ ! -e $ramdisk ] ; then
	exit 1
fi

if [ -d $LXC_ROOTFS_PATH ] ; then
	umount --recursive $LXC_ROOTFS_PATH
	rm -rf $LXC_ROOTFS_PATH
fi

# Make sure we're always starting from a clean base
mkdir -p $LXC_ROOTFS_PATH
mount -t tmpfs none $LXC_ROOTFS_PATH
(cd $LXC_ROOTFS_PATH ; cat $ramdisk | gzip -d | cpio -i)

# /dev/pts needs to be there
mkdir -p $LXC_ROOTFS_PATH/dev/pts

mount_bind_ro() {
	if [ ! -d $2 ] ; then
		mkdir $2
	fi
	mount --bind $1 $2
	mount -o remount,ro,bind $2
}

mount_bind_ro /system $LXC_ROOTFS_PATH/system
mount_bind_ro /system/vendor $LXC_ROOTFS_PATH/vendor

# usage existing /data directory
mkdir -p /data
mount -o bind /data $LXC_ROOTFS_PATH/data

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
