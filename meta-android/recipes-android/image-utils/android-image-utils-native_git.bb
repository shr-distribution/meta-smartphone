DESCRIPTION = "Android Image Creation and Booting Utilities"
SECTION = "console/utils"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"

DEPENDS = "zlib-native"

SRCREV = "40a6c924a2f5031dfabfbf0a70d1e1435c9bc832"
PV = "1.0+gitr${SRCPV}"
PR = "r4"
SRC_URI = "\
  ${FREESMARTPHONE_GIT}/utilities.git;protocol=git;branch=master \
"
S = "${WORKDIR}/git/android/image-utils"

inherit autotools deploy native

PARALLEL_MAKE = ""

do_deploy () {
    install -d ${DEPLOY_DIR_TOOLS}
    install -m 0755 fastboot/fastboot ${DEPLOY_DIR_TOOLS}
}
do_deploy[sstate-outputdirs] = "${DEPLOY_DIR_TOOLS}"
