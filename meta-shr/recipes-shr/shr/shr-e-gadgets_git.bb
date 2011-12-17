DESCRIPTION = "An e17 module for a lot of needed shr-gadgets"
HOMEPAGE = "http://shr-project.org"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=d32239bcb673463ab874e80d47fae504"
DEPENDS = "e-wm elementary"
RDEPENDS_${PN} = "e-wm"
SECTION = "x11/application"

SRCREV = "32f3685d3b3f0484dfd19038e7597e8d830b5a65"
PV = "0.0.0+gitr${SRCPV}"
PR = "r13"

inherit autotools

SRC_URI = "\
	git://git.shr-project.org/repo/shr-e-gadgets.git;protocol=http;branch=master \
	file://label-in-text-get-api-change.patch \
"
S = "${WORKDIR}/git"

EXTRA_OECONF = "--with-edje-cc=${STAGING_BINDIR_NATIVE}/edje_cc"

FILES_${PN} += "\
	${datadir}/shr_elm_softkey \
	${libdir}/enlightenment/modules/*/*.desktop \
	${libdir}/enlightenment/modules/*/*.edj \
	${libdir}/enlightenment/modules/*/*/*.so \
"
FILES_${PN}-static += "${libdir}/enlightenment/modules/*/*/*.a"
FILES_${PN}-dev += "${libdir}/enlightenment/modules/*/*/*.la"
FILES_${PN}-dbg += "${libdir}/enlightenment/modules/*/*/.debug"

