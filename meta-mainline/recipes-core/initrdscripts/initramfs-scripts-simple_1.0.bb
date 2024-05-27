DESCRIPTION = "Simple init script to boot on an Android device"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RDEPENDS:${PN} = " iproute2 busybox-mdev e2fsprogs e2fsprogs-resize2fs parted"

SRC_URI = " \
  file://init.sh \
  file://init_functions.sh \
"

FILES:${PN} = "/init /init_functions.sh"

do_install() {
    install -m 0755 ${UNPACKDIR}/init.sh ${D}/init
    install -m 0644 ${UNPACKDIR}/init_functions.sh ${D}/init_functions.sh
}
