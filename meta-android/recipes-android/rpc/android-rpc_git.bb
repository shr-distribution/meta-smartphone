DESCRIPTION = "Android RPC library"
SECTION = "devel"
LICENSE = "Android"

LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"

SRCREV = "9da90ad4ebc0efe978c61060d8fdf7362cc9115c"
DEPENDS = "libgee"
PV = "1.1+gitr${SRCPV}"
PR = "r1"

SRC_URI = "\
  ${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master \
"
S = "${WORKDIR}/git/android/rpc"

inherit autotools-brokensep
