#!/bin/sh

# udevd does clearenv(). Export shell PATH to children.
export PATH

sysfspowernode=/sys/devices/platform/gta02-pm-gps.0/power_on

chgrp dialout ${sysfspowernode}
chmod g+w ${sysfspowernode}
