DESCRIPTION = "Touchscreen management daemon - manages the touchscreen of the palm pre machine"
HOMEPAGE = "http://www.freesmartphone.org"
AUTHOR = "Valéry Febvre <vfebvre@easter-eggs.com>"
SECTION = "base"

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"

DEPENDS = "tslib"

PR = "r1"
PV = "1.0.0+gitr${SRCPV}"

SRCREV = "25729581e06241a5fb41b2b4f6c80e7067ae4a28"
SRC_URI = " \
 git://github.com/freesmartphone/utilities;protocol=https;branch=master \
 file://tsmd \
 file://tsmd_control \
"

S = "${WORKDIR}/git/palmpre/tsmd"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit autotools update-rc.d

INITSCRIPT_NAME = "tsmd"
INITSCRIPT_PARAMS = "defaults 10"

do_install_append() {
    install -d 0644 ${D}${sysconfdir}/init.d/
    install -m 0755 ${WORKDIR}/${INITSCRIPT_NAME} ${D}${sysconfdir}/init.d/
    install -d 0644 ${D}${bindir}/
    install -m 0755 ${WORKDIR}/tsmd_control ${D}${bindir}/tsmd_control
}
