DESCRIPTION = "Android Bluetooth firmware loader"
SECTION = "devel"
LICENSE = "Android"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"

SRCREV = "69371f6ecc82ed44e1292eae7d92313a5bfa68a8"
PV = "4.1.1+gitr${SRCPV}"

SRC_URI = "${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master"
S = "${WORKDIR}/git/android/brcm_patchram_plus"

inherit autotools
