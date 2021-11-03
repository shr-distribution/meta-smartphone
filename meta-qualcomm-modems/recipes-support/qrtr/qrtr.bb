SUMMARY = "QIPCRTR Name Service"
HOMEPAGE = "https://github.com/andersson/rpmsgexport"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=15329706fbfcb5fc5edcc1bc7c139da5"

DEPENDS = "util-linux"

PV = "1.0"

SRC_URI = "git://github.com/andersson/qrtr.git;protocol=https;branch=master"

SRCREV = "cd6bedd5d00f211e6c1e3803ff2f9f53c246435e"
S = "${WORKDIR}/git"

EXTRA_OEMAKE = "prefix=${prefix} bindir=${bindir} libdir=${libdir} includedir=${includedir} servicedir=${systemd_system_unitdir} LDFLAGS='${LDFLAGS}'"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "${PN}-ns.service"

inherit systemd
FILES:${PN} += "${systemd_system_unitdir}"
