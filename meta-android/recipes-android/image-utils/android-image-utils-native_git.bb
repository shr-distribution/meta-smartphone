DESCRIPTION = "Android Image Creation and Booting Utilities"
SECTION = "console/utils"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYING;md5=c19179f3430fd533888100ab6616e114"
SRCREV = "40a6c924a2f5031dfabfbf0a70d1e1435c9bc832"
PV = "1.0+gitr${SRCPV}"
PR = "r4"
SRC_URI = "\
  git://github.com/freesmartphone/utilities;protocol=https;branch=master \
"
S = "${WORKDIR}/git/android/image-utils"

inherit autotools native deploy

PARALLEL_MAKE = ""

do_deploy () {
    install -d ${DEPLOY_DIR_TOOLS}
    install -m 0755 fastboot/fastboot ${DEPLOY_DIR_TOOLS}
}
do_deploy[sstate-outputdirs] = "${DEPLOY_DIR_TOOLS}"
