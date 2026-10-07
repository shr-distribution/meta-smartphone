DESCRIPTION = "Raise the HP TouchPad's CPU frequency on touch input through the interactive governor's boostpulse"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

COMPATIBLE_MACHINE = "tenderloin-halium"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
    file://tenderloin-touch-boost.c \
    file://tenderloin-touch-boost.service \
"
S = "${UNPACKDIR}"

inherit systemd

do_compile() {
    ${CC} ${CFLAGS} -Wall ${LDFLAGS} -o tenderloin-touch-boost ${S}/tenderloin-touch-boost.c
}

do_install() {
    install -d ${D}${sbindir}
    install -m 0755 tenderloin-touch-boost ${D}${sbindir}
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/tenderloin-touch-boost.service ${D}${systemd_system_unitdir}
}

SYSTEMD_SERVICE:${PN} = "tenderloin-touch-boost.service"
