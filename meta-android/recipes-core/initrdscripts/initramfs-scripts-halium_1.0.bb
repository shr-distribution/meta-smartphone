DESCRIPTION = "Halium init script to boot on an Android device with an Halium image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}"

RPROVIDES_${PN} += "virtual/android-initramfs-scripts"

SRC_URI += " \
  file://init.sh \
  file://machine.conf \
  file://distro.conf \
  git://github.com/Tofee/initramfs-tools-halium.git;branch=tofe/ab-scheme \
  file://functions \
"

SRCREV="37e2c80265e646169c8aee07acb1a9a3785ed699"

do_install_append() {
    install -m 0755 ${WORKDIR}/init.sh ${D}/init
    install -m 0644 ${WORKDIR}/machine.conf ${D}/machine.conf
    install -m 0644 ${WORKDIR}/distro.conf ${D}/distro.conf

    install -m 0644 ${WORKDIR}/git/scripts/halium ${D}/halium-boot.sh
    install -m 0644 ${WORKDIR}/functions ${D}/functions
}

FILES_${PN} += " \
    /init /machine.conf /distro.conf \
    /halium-boot.sh /functions \
"
