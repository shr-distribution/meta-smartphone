DESCRIPTION = "Simple init script to chroot into an exisiting rootfs somewhere we can't \
boot into directly"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PR = "r8"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = " \
  file://init.sh \
  file://machine.conf"

PACKAGES = "${PN}"
FILES:${PN} = "/init /machine.conf"

do_install() {
    cp ${WORKDIR}/init.sh ${D}/init
    install -m 0755 ${WORKDIR}/init.sh ${D}/init
    install -m 0644 ${WORKDIR}/machine.conf ${D}/machine.conf
}
