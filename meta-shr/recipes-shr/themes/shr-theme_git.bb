DESCRIPTION = "Standard icon theme for the SHR distribution"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=297cd7a08a1ae4d946a1164f25bacd88"
SECTION = "x11/data"

SRCREV = "7f1c1b2fc81ce99815fe7946acf2c8d06502ad73"
PV = "0.0.2+gitr${SRCPV}"

inherit autotools gettext allarch

SRC_URI = "git://git.shr-project.org/repo/shr.git;protocol=http;branch=master"
S = "${WORKDIR}/git/${PN}"

pkg_postinst_shr-theme () {
}

do_configure_prepend() {
# insane.bbclass does not like shared repos and check whole WORKDIR instead just S
rm -rf ${WORKDIR}/git/libframeworkd-phonegui*
rm -rf ${WORKDIR}/git/neod/
rm -rf ${WORKDIR}/git/openmoko-*
rm -rf ${WORKDIR}/git/ophonekitd/
rm -rf ${WORKDIR}/git/shr-dialer/
rm -rf ${WORKDIR}/git/shr-contacts/
rm -rf ${WORKDIR}/git/shr-messages/
rm -rf ${WORKDIR}/git/shr-settings/
rm -rf ${WORKDIR}/git/shr-splash/
rm -rf ${WORKDIR}/git/libhito/
}

FILES_${PN} += "${datadir}/icons"
