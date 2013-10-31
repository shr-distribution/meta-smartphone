DESCRIPTION = "Receive a forwarded serial from serial-forward and provide a PTY"
AUTHOR = "Holger 'Zecke' Freyther"
LICENSE = "GPL"
SECTION = "console/network"
SRCREV = "2725c6c18cace2acaf4f20c0748300dcea4dd90c"
PV = "1.1+gitr${SRCPV}"
PE = "1"

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
