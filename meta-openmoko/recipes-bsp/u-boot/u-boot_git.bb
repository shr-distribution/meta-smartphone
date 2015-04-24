require recipes-bsp/u-boot/u-boot.inc

SRC_URI = "git://git.goldelico.com/gta04-uboot.git;branch=gta04 \
  file://0001-config-Always-use-GNU-ld.patch \
  file://gcc5.patch \
"
SRCREV = "9e2ea9f36fc1d914d68a112643ba90fc037c3b27"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb \
                    file://README;beginline=1;endline=22;md5=3a00ef51d3fc96e9d6c1bc4708ccd3b5"

S = "${WORKDIR}/git/u-boot"

PV = "2011.03+gitr${SRCPV}"

UBOOT_MACHINE_om-gta04 = "omap3_gta04_config"

COMPATIBLE_MACHINE = "om-gta04"

PACKAGE_ARCH = "${MACHINE_ARCH}"

EXTRA_OEMAKE_append = " KCFLAGS=-fgnu89-inline"
