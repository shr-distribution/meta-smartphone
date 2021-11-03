SUMMARY = "Qualcomm Remote Filesystem Service Implementation"
HOMEPAGE = "https://github.com/andersson/rpmsgexport"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ff273e1fd41fa52668171e0817c89724"

DEPENDS = "util-linux udev"

PV = "1.0"

SRC_URI = "git://github.com/andersson/rpmsgexport.git;protocol=https;branch=master"

EXTRA_OEMAKE = "prefix=${prefix} bindir=${bindir} libdir=${libdir} includedir=${includedir} servicedir=${systemd_system_unitdir} LDFLAGS='${LDFLAGS}'"

SRCREV = "324d88d668f36c6a5e6a9c2003a050b8a5a3cd60"
S = "${WORKDIR}/git"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}
