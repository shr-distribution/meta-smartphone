SUMMARY = "Qualcomm Remote Filesystem Service Implementation"
HOMEPAGE = "https://github.com/andersson/rmtfs"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ca25dbf5ebfc1a058bfc657c895aac2f"

DEPENDS = "util-linux qrtr qmic-native udev"

PV = "1.0"

SRC_URI = "git://github.com/andersson/rmtfs.git;protocol=git;branch=master"

EXTRA_OEMAKE = "prefix=${prefix} bindir=${bindir} libdir=${libdir} includedir=${includedir} servicedir=${systemd_system_unitdir}"

SRCREV = "df6c19d0330d251af0a7c812bf5ddb847962ce2c"
S = "${WORKDIR}/git"

inherit systemd

SYSTEMD_PACKAGES = "${PN}"
SYSTEMD_SERVICE:${PN} = "${PN}.service"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}

FILES:${PN} += "${systemd_system_unitdir}"
