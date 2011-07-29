DESCRIPTION = "Legacy GNU-tar to unpack hd images on install for Zaurus spitz"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://${WORKDIR}/gnu-tar;md5=34b647d6444a9a38e269c2793c7d0bf2"
PR = "r1"

SRC_URI = "http://pocketworkstation.org/files/gnu-tar.gz"
SRC_URI[md5sum] = "0c1494335e4f33ac5712d1f72de2eff2"
SRC_URI[sha256sum] = "68e12755957a27644ced166ef9093a44f81be6e04727f2759beef5968de41f61"

do_compile() {
        :
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = ""

COMPATIBLE_MACHINE = "spitz"

inherit deploy

addtask deploy before do_populate_sysroot after do_compile

do_deploy() {
        install -d ${DEPLOY_DIR_IMAGE}
        install -m 0755 ${WORKDIR}/gnu-tar ${DEPLOY_DIR_IMAGE}/gnu-tar
}
