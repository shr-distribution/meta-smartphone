SUMMARY = "A tool to read/write/update android boot images. Forked from AOSP."
HOMEPAGE = "https://github.com/osm0sis/mkbootimg"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=15329706fbfcb5fc5edcc1bc7c139da5"

DEPENDS = "util-linux"

PV = "1.0"

SRC_URI = "git://github.com/andersson/qrtr.git;protocol=git;branch=master \
           file://0001-Fix-qrtr-Makefile-prefix.patch \
"

SRCREV = "c1bdfb37dbb231abbd8b540f147ac064452f5411"
S = "${WORKDIR}/git"

INSANE_SKIP_${PN} = "ldflags"

do_install() {
    oe_runmake install 'DESTDIR=${D}'
}
