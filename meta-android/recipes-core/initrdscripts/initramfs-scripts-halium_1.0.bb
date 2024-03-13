DESCRIPTION = "Halium init script to boot on an Android device with an Halium image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RDEPENDS:${PN} = "busybox-mdev e2fsprogs-e2fsck e2fsprogs-resize2fs"

SRC_URI += " \
  file://init.sh \
  file://machine.conf \
  file://distro.conf \
  git://github.com/Tofee/initramfs-tools-halium.git;branch=tofe/halium-9.0;protocol=https \
  file://functions \
"

S = "${WORKDIR}/git"

SRCREV = "0a2275aafe651d19e9eb3aa7a801c4d28550298f"

do_install:append() {
    install -m 0755 ${WORKDIR}/init.sh ${D}/init
    install -m 0644 ${WORKDIR}/machine.conf ${D}/machine.conf
    install -m 0644 ${WORKDIR}/distro.conf ${D}/distro.conf

    install -m 0644 ${WORKDIR}/git/scripts/halium ${D}/halium-boot.sh
    install -m 0644 ${WORKDIR}/functions ${D}/functions
}

FILES:${PN} += " \
    /init /machine.conf /distro.conf \
    /halium-boot.sh /functions \
"
