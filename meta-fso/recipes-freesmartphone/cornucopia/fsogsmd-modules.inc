DESCRIPTION = "Config files for freesmartphone.org API reference implementation (FSO 2.0)"
AUTHOR = "Team FSO"
HOMEPAGE = "http://www.freesmartphone.org"

INHIBIT_DEFAULT_DEPS = "1"

PE = "2"
PR = "r1"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"
ALLOW_EMPTY_${PN} = "1"

## htcdream
#  modem_qualcomm_htc.so
RDEPENDS_${PN}_htcdream += "fsogsmd-module-modem-qualcomm-htc"

## nokia900
#  lowlevel_nokia900.so
#  modem_nokia_isi.so
RDEPENDS_${PN}_nokia900 += "fsogsmd-module-modem-nokia-isi"

## openmoko
#  lowlevel_openmoko.so
#  modem_ti_calypso.so
RDEPENDS_${PN}_om-gta01 += "fsogsmd-module-lowlevel-openmoko"
RDEPENDS_${PN}_om-gta01 += "fsogsmd-module-modem-ti-calypso"

RDEPENDS_${PN}_om-gta02 += "fsogsmd-module-lowlevel-openmoko"
RDEPENDS_${PN}_om-gta02 += "fsogsmd-module-modem-ti-calypso"

## om-gta04
#  modem_option_gtm601.so
RDEPENDS_${PN}_om-gta04 += "fsogsmd-module-modem-option-gtm601"

## motorola-ezx
#  modem_freescale_neptune.so
#  lowlevel_motorola_ezx.so
RDEPENDS_${PN}_motorola-ezx += "fsogsmd-module-modem-freescale-neptune"
RDEPENDS_${PN}_motorola-ezx += "fsogsmd-module-lowlevel-motorola-ezx"

## crespo
#  modem_samsung.so
#  lowlevel_samsung_crespo.so
RDEPENDS_${PN}_crespo += "fsogsmd-module-lowlevel-samsung-crespo"
RDEPENDS_${PN}_crespo += "fsogsmd-module-modem-samsung"

