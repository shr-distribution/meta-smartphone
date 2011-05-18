require u-boot.inc

PR = "r73"

SRCREV_pn-u-boot = "bd2313078114c4b44c4a5ce149af43bcb7fc8854"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=4c6cde5df68eff615d36789dc18edd3b \
                    file://README;beginline=1;endline=22;md5=3a00ef51d3fc96e9d6c1bc4708ccd3b5"

S = "${WORKDIR}/git"

PV = "2010.06+gitr${SRCPV}"

SRC_URI = " \
  git://www.denx.de/git/u-boot.git;protocol=git \
  file://0001-ARM-Avoid-compiler-optimization-for-usages-of-readb-.patch \
  file://0001-Reduce-delays-in-omap-i2c-driver.patch \
  file://0002-Make-bootm-optionally-use-pre-existing-atags-for-Lin.patch \
  file://0003-Store-existing-atags-at-startup-if-chainloading.patch \
  file://0004-Nokia-RX-51-aka-N900-support.patch \
  file://0001-nokia-rx51-fix-declaration-fails-when-building-with-.patch \
  file://0005-fix-loading-file-from-ext2-partition-on-OMAP3-evm.patch \
  file://0006-omap3_mmc.c-fix-formating.patch \
  file://0007-Only-delay-boot-if-keyboard-open.patch \
  file://gcc-4.5.O0.patch \
  file://0001-configs-nokia_rx51.h-start-shr-as-default-and-change.patch \
"

UBOOT_MACHINE_nokia900 = "nokia_rx51_config"

COMPATIBLE_MACHINE = "nokia900"
