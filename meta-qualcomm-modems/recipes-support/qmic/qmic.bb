SUMMARY = "QMI IDL compiler"
HOMEPAGE = "https://github.com/andersson/qmic"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ca25dbf5ebfc1a058bfc657c895aac2f"

DEPENDS = "util-linux"

PV = "1.0"

SRC_URI = "git://github.com/andersson/qmic.git;protocol=https;branch=master"

EXTRA_OEMAKE = "prefix=${prefix} bindir=${bindir} libdir=${libdir} includedir=${includedir} servicedir=${systemd_system_unitdir} LDFLAGS='${LDFLAGS}'"

SRCREV = "815dd495eb087b3b3ea02a8ed43716efac43db1c"
S = "${WORKDIR}/git"

BBCLASSEXTEND = "native"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}
