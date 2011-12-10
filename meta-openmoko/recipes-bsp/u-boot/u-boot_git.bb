require recipes-bsp/u-boot/u-boot.inc

SRC_URI = "git://git.goldelico.com/gta04-uboot.git"
SRCREV = "e17fde7c50b8c54967450c7aea465e7e6bcb29dd"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb \
                    file://README;beginline=1;endline=22;md5=3a00ef51d3fc96e9d6c1bc4708ccd3b5"

S = "${WORKDIR}/git"

PV = "2011.03+gitr${SRCPV}"


UBOOT_MACHINE_om-gta04 = "omap3_gta04_config"

COMPATIBLE_MACHINE = "om-gta04"

PACKAGE_ARCH = "${MACHINE_ARCH}"
