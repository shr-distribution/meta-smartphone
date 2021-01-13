#!/bin/sh

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

# Cleanup everything
if [ -e /android/init ]; then
    echo "System-as-root, only umounting sub-mounts of rootfs"
    cat /proc/self/mounts | while read line; do
	set -- $line
	# Skip any unwanted entry
	echo $2 | egrep -q "^/android/" || continue
	desired_mount=${2/\/android/}
	umount $LXC_ROOTFS_PATH/$desired_mount
    done

    umount /mnt/vendor/persist
else
    umount --recursive $LXC_ROOTFS_PATH
fi

