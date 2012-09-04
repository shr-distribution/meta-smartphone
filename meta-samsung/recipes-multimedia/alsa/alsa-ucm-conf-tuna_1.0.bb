DESCRIPTION = "ALSA Use Case Manager (UCM) configuration files for the Samsung Tuna device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r0"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = "git://git@github.com/openwebos-ports/alsa-ucm-conf.git;branch=master;protocol=ssh"
S = "${WORKDIR}/git"
SRCREV = "9c188f0f423f95f8dbba7f662b659ef1b7bf65ff"

do_install() {
    mkdir -p ${D}${datadir}/alsa/ucm/Tuna
    cp ${S}/Tuna/* ${D}${datadir}/alsa/ucm/Tuna/
}

PACKAGES = "${PN}"
FILES_${PN} = "${datadir}/alsa/ucm/Tuna"
