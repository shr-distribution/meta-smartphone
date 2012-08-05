DESCRIPTION = "Android Image Creation and Booting Utilities"
SECTION = "console/utils"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"
SRCREV = "b649ca092266b1975a5f693a70191980a2f26276"
PV = "1.0+gitr${SRCREV}"
PR = "r3"
SRC_URI = "\
  ${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master \
"
S = "${WORKDIR}/git/android/image-utils"

inherit autotools native

do_deploy () {
    install -d ${DEPLOY_DIR_TOOLS}
    install -m 0755 fastboot/fastboot ${DEPLOY_DIR_TOOLS}
}
do_deploy[sstate-outputdirs] = "${DEPLOY_DIR_TOOLS}"
addtask deploy before do_package after do_install
