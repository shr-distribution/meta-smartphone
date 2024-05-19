SUMMARY = "QMI IDL compiler"
HOMEPAGE = "https://github.com/andersson/qmic"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ca25dbf5ebfc1a058bfc657c895aac2f"

DEPENDS = "util-linux"

PV = "1.0"

SRC_URI = "git://github.com/linux-msm/qmic.git;protocol=https;branch=master"

EXTRA_OEMAKE = "prefix=${prefix} bindir=${bindir} libdir=${libdir} includedir=${includedir} servicedir=${systemd_system_unitdir} LDFLAGS='${LDFLAGS}'"

SRCREV = "4574736afce75aa5eec1e1069a19563410167c9f"
S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}
