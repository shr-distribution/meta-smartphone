DESCRIPTION = "Several initscripts to startup daemons from Android compatibility layer"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PR = "r3"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
    file://android_finish.upstart \
    file://init.rc"

do_install() {
    mkdir -p ${D}${webos_upstartconfdir}
    install -m 0644 ${WORKDIR}/android_finish.upstart \
        ${D}${sysconfdir}/event.d/android_finish

    install -m 0644 ${WORKDIR}/init.rc ${D}/init.rc
}

PACKAGES = "${PN}-sysvinit ${PN}-upstart ${PN}"

ALLOW_EMPTY_${PN}-sysvinit = "1"
ALLOW_EMPTY_${PN}-upstart = "1"

RDEPENDS_${PN}-upstart = "${PN}"
RDEPENDS_${PN}-sysvinit = "${PN}"


FILES_${PN} = "/init.rc /init.usb.rc"
FILES_${PN}-sysvinit = "${sysconfdir}/init.d/ ${sysconfdir}/rc*/"
FILES_${PN}-upstart = "${sysconfdir}/event.d/"
