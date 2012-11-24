DESCRIPTION = "Simple init script to chroot into an exisiting rootfs somewhere we can't \
boot into directly"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r6"

COMPATIBLE_MACHINE = "(crespo|tuna)"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = "file://init.sh"

PACKAGES = "${PN}"
FILES_${PN} = "/init"

do_install() {
  cp ${WORKDIR}/init.sh ${D}/init
  chmod +x ${D}/init
}
