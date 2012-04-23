DESCRIPTION = "Config files for freesmartphone.org API reference implementation (FSO 2.0)"
AUTHOR = "Team FSO"
HOMEPAGE = "http://www.freesmartphone.org"

INHIBIT_DEFAULT_DEPS = "1"

PE = "1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"
ALLOW_EMPTY = "1"

## palmpre
#  palmpre_quirks.so
#  accelerometer_kxsd9.so
#  proximity_palmpre.so (dropped in f3bd874dc0422558bd709ee706d02f28d09a6d59)
#  vibrator_omapvibe.so
#  backlight_omappanel.so
RDEPENDS_${PN}_palmpre += "fsodeviced-module-accelerometer-kxsd9"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-palmpre-quirks"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-backlight-omappanel"
RDEPENDS_${PN}_palmpre += "fsodeviced-module-vibrator-omapvibe"

## openmoko
#  accelerometer_lis302.so
#  openmoko_powercontrol.so
RDEPENDS_${PN}_om-gta01 += "fsodeviced-module-accelerometer-lis302"
RDEPENDS_${PN}_om-gta01 += "fsodeviced-module-openmoko-powercontrol"

RDEPENDS_${PN}_om-gta02 += "fsodeviced-module-accelerometer-lis302"
RDEPENDS_${PN}_om-gta02 += "fsodeviced-module-openmoko-powercontrol"

## nokia900
#  ambientlight_n900.so
#  n900_powercontrol.so
#  powersupply_n900.so
#  proximity_n900.so
#  accelerometer_lis302.so (added in f05eded4212e43dd22a5e4e9bd4a3de6d5dd8c0a)
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-ambientlight-n900"
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-powersupply-n900"
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-proximity-n900"
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-n900-powercontrol"
RDEPENDS_${PN}_nokia900 += "fsodeviced-module-accelerometer-lis302"

## htcdream
#  router_qdsp5.so
#  vibrator_timedoutputclass.so
RDEPENDS_${PN}_htcdream += "fsodeviced-module-router-qdsp5"
RDEPENDS_${PN}_htcdream += "fsodeviced-module-vibrator-timedoutputclass"
