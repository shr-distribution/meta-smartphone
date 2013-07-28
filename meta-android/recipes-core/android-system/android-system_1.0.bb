DESCRIPTION = "System configuration and startup scripts for the Android compatibility layer"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

PR = "r3"

PACKAGE_ARCH = "${MACHINE_ARCH}"

# We're the former android-initscripts package
RPROVIDES_${PN} = "android-initscripts"
RREPLACES_${PN} = "android-initscripts"
RCONFLICTS_${PN} = "android-initscripts"

inherit systemd

SRC_URI = " \
    file://init.rc \
    file://android_system.service \
    file://android_system_setup \
"

do_install() {
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/android_system.service ${D}${systemd_unitdir}/system

    install -m 0644 ${WORKDIR}/init.rc ${D}/init.rc

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/android_system_setup ${D}${bindir}
}

SYSTEMD_SERVICE_${PN} = "android_system.service"

FILES_${PN} += "/init.rc"
