SUMMARY = "Qualcomm Remote Filesystem Service Implementation"
HOMEPAGE = "https://github.com/andersson/rmtfs"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ca25dbf5ebfc1a058bfc657c895aac2f"

DEPENDS = "util-linux qrtr qmic-native udev"

PV = "1.0"

SRC_URI = "git://github.com/andersson/rmtfs.git;protocol=git;branch=master \
           file://0001-Fix-rmtfs-Makefile-prefix.patch \
"

SRCREV = "cfb76ff6eecec15636f182009cda50ea0c654c60"
S = "${WORKDIR}/git"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}
