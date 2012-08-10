DESCRIPTION = "Android ADB utility"
SECTION = "devel"
LICENSE = "Android"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"

SRCREV = "e57f7bafb0e19bb3780132883cef910daa5efaeb"
PV = "2.3.7+gitr${SRCPV}"
PR = "r0"

SRC_URI = "${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master"
S = "${WORKDIR}/git/android/adb"

inherit autotools
