DESCRIPTION = "Config files for freesmartphone.org API reference implementation (FSO 2.0)"
AUTHOR = "Team FSO"
HOMEPAGE = "http://www.freesmartphone.org"

INHIBIT_DEFAULT_DEPS = "1"

PE = "2"
PR = "r2"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"
ALLOW_EMPTY_${PN} = "1"

## openmoko
#  accelerometer_lis302.so
#  openmoko_powercontrol.so
RDEPENDS_${PN}_om-gta01 += "fsodeviced-module-accelerometer-lis302"
RDEPENDS_${PN}_om-gta01 += "fsodeviced-module-openmoko-powercontrol"

RDEPENDS_${PN}_om-gta02 += "fsodeviced-module-accelerometer-lis302"
RDEPENDS_${PN}_om-gta02 += "fsodeviced-module-openmoko-powercontrol"

## nokia900
#  n900_quirks.so
#  accelerometer_lis302.so (added in f05eded4212e43dd22a5e4e9bd4a3de6d5dd8c0a)
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-n900-quirks"
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-accelerometer-lis302"

## htcdream
#  router_qdsp5.so
#  vibrator_timedoutputclass.so
RDEPENDS_${PN}_htcdream += "fsodeviced-module-router-qdsp5"
RDEPENDS_${PN}_htcdream += "fsodeviced-module-vibrator-timedoutputclass"
