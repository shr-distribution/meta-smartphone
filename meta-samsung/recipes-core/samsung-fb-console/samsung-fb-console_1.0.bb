DESCRIPTION = "Simple init script to enable framebuffer console via omapdss"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

COMPATIBLE_MACHINE = "tuna"
PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI = "file://fb-console.sh"

PACKAGES = "${PN}"
FILES_${PN} = " \
  /etc/init.d/fb-console.sh \
  /etc/rcS.d/S05fb-console.sh"

do_install() {
    mkdir -p ${D}${sysconfdir}/init.d
    mkdir -p ${D}${sysconfdir}/rcS.d
    cp ${WORKDIR}/fb-console.sh ${D}${sysconfdir}/init.d/
    chmod +x ${D}${sysconfdir}/init.d/fb-console.sh
    ln -sf ../init.d/fb-console.sh ${D}${sysconfdir}/rcS.d/S05fb-console.sh
}
