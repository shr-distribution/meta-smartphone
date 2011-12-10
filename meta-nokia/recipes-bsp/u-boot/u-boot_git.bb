require recipes-bsp/u-boot/u-boot.inc

SRCREV = "b1af6f532e0d348b153d5c148369229d24af361a"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb \
                    file://README;beginline=1;endline=22;md5=5ba4218ac89af7846802d0348df3fb90"

S = "${WORKDIR}/git"

PR = "r2"
PV = "2011.06+gitr${SRCPV}"

SRC_URI = " \
  git://www.denx.de/git/u-boot.git;protocol=git \
  file://0001-Copy-u-boot.bin-to-CONFIG_SYS_TEXT_BASE-if-chainload.patch \
  file://0002-Store-existing-atags-at-startup-if-chainloading.patch \
  file://0003-Make-bootm-optionally-use-pre-existing-atags-for-Lin.patch \
  file://0004-Nokia-RX-51-aka-N900-support.patch \
  file://0005-Tidy-up-the-patches.patch \
  file://0006-We-don-t-need-that-init_loadaddr-in-env.patch \
  file://0007-Only-delay-boot-if-keyboard-open.patch \
  file://0008-configs-nokia_rx51.h-update-saved-env.patch \
"

UBOOT_MACHINE_nokia900 = "nokia_rx51_config"

COMPATIBLE_MACHINE = "nokia900"
