DESCRIPTION = "Simple init script to boot on an Android device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RDEPENDS:${PN} = " iproute2 busybox-mdev "

RPROVIDES:${PN} += "virtual/android-initramfs-scripts"

SRC_URI = " \
  file://init.sh \
  file://init_functions.sh \
  file://machine.conf"

FILES:${PN} = "/init /init_functions.sh /machine.conf /distro.conf"

RDEPENDS:${PN} = "e2fsprogs-e2fsck e2fsprogs-resize2fs"

do_install() {
    install -m 0755 ${WORKDIR}/init.sh ${D}/init
    install -m 0644 ${WORKDIR}/init_functions.sh ${D}/init_functions.sh
    install -m 0644 ${WORKDIR}/machine.conf ${D}/machine.conf
    echo "# Name of the distribution" >  ${WORKDIR}/distro.conf
    echo "distro_name=${DISTRO}"      >> ${WORKDIR}/distro.conf
    install -m 0644 ${WORKDIR}/distro.conf ${D}/distro.conf
}
