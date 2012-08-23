#!/bin/sh
### BEGIN INIT INFO
# Provides: fb-console
# Required-Start: mountvirtfs
# Required-Stop:
# Default-Start:     S
# Default-Stop:
### END INIT INFO

echo 1 > /sys/devices/platform/omapdss/overlay0/enabled
