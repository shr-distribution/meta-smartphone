require recipes-bsp/u-boot/u-boot.inc

SRCREV = "279bbbca12e71ae68278b756048194003a6d6e21"

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=1707d6db1d42237583f50183a5651ecb \
                    file://README;beginline=1;endline=22;md5=5ba4218ac89af7846802d0348df3fb90"

S = "${WORKDIR}/git"

PV = "2011.06+2011.09-rc2+gitr${SRCPV}"

SRC_URI = " \
  git://www.denx.de/git/u-boot.git;protocol=git \
  file://0001-config-Always-use-GNU-ld.patch \
  file://0001-Copy-u-boot.bin-to-CONFIG_SYS_TEXT_BASE-if-chainload.patch \
  file://0002-Store-existing-atags-address-at-startup-if-chainload.patch \
  file://0003-Make-bootm-optionally-use-pre-existing-atags-for-Lin.patch \
  file://0004-Nokia-RX-51-aka-N900-support.patch \
  file://0005-Only-delay-boot-if-keyboard-open.patch \
  file://0006-Change-Wireless-LAN-mode-from-M4-to-M0.patch \
  file://0007-RX-51-Compilation-fixes.patch \
  file://0008-RX-51-Add-support-for-resetting-twl4030-watchdog.patch \
  file://0009-RX-51-Fix-keymap.patch \
  file://0010-include-common.h-Add-some-macros-for-ANSI-escape-cod.patch \
  file://0011-drivers-video-cfb_console.c-Added-support-for-ANSI-e.patch \
  file://0012-New-command-bootmenu-ANSI-terminal-Boot-Menu-support.patch \
  file://0013-New-config-variable-CONFIG_MENU.patch \
  file://0014-New-config-variable-CONFIG_PREMONITOR.patch \
  file://0015-configs-nokia_rx51.h-update-saved-env.patch \
  file://0016-RX-51-Add-support-for-bootmenu.patch \
  file://0017-RX-51-use-gpio-Use-generic-API-n-see-81bdc155c72ef9e.patch \
  file://0018-RX-51-Use-generic-MMC-driver.patch \
"

UBOOT_MACHINE_nokia900 = "nokia_rx51_config"

COMPATIBLE_MACHINE = "nokia900"
