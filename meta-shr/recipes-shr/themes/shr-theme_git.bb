DESCRIPTION = "Standard icon theme for the SHR distribution"
HOMEPAGE = "http://shr-project.org/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=297cd7a08a1ae4d946a1164f25bacd88"
SECTION = "x11/data"

SRCREV = "730da87ad75c814c16c856ca138605d985b6efdb"
PV = "0.0.2+gitr${SRCPV}"
PR = "r2"

inherit autotools gettext allarch

SRC_URI = "git://github.com/shr-distribution/shr;protocol=https;branch=master"
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
