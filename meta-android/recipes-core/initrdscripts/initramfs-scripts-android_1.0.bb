DESCRIPTION = "Simple init script to boot on an Android device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"

RPROVIDES_${PN} += "virtual/android-initramfs-scripts"

SRC_URI = " \
  file://init.sh \
  file://machine.conf \
  file://distro.conf"

PACKAGES = "${PN}"
FILES_${PN} = "/init /machine.conf /distro.conf"

RDEPENDS_${PN} = "e2fsprogs-e2fsck e2fsprogs-resize2fs"

do_install() {
    install -m 0755 ${WORKDIR}/init.sh ${D}/init
    install -m 0644 ${WORKDIR}/machine.conf ${D}/machine.conf
    install -m 0644 ${WORKDIR}/distro.conf ${D}/distro.conf
}
