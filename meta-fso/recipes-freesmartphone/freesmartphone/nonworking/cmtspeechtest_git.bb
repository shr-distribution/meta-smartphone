DESCRIPTION = "cmtspeechdata library test"
SECTION = "devel"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
SRCREV = "e4cf49712b2c9c0d4170357b3a98b3f686f0799f"
DEPENDS = "libcmtspeechdata libfso-glib"
PV = "0.0+gitr${SRCPV}"
PR = "r0"

SRC_URI = "\
  ${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master \
"
S = "${WORKDIR}/git/nokian900/cmtspeechtest"

inherit autotools

FILES_${PN} += "${datadir}"
