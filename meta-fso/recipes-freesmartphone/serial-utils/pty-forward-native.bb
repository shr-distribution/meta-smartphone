DESCRIPTION = "Receive a forwarded serial from serial-forward and provide a PTY"
AUTHOR = "Holger 'Zecke' Freyther"
LICENSE = "GPL"
SECTION = "console/network"
SRCREV = "00dbec2636ae0385ad028587e20e446272ff97ec"
PV = "1.1+gitr${SRCPV}"
PE = "1"
PR = "r0"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=ebb5c50ab7cab4baeffba14977030c07"

SRC_URI = "${FREESMARTPHONE_GIT}/cornucopia.git;protocol=git"
S = "${WORKDIR}/git/tools/serial_forward"

inherit autotools native

do_deploy() {
    install -d ${DEPLOY_DIR_IMAGE}
    install -m 0755 ${S}/src/pty_forward ${DEPLOY_DIR_IMAGE}/pty-forward
}

addtask deploy before do_package after do_install
