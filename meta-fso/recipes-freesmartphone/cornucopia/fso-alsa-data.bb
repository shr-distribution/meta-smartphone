DESCRIPTION = "freesmartphone.org Alsa data files"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
PV = "0.1.0"
PR = "r0"

SRC_URI = "\
  file://default \
  file://default.conf \
"

do_install() {
    install -d ${D}${sysconfdir}/freesmartphone/alsa/default/
    # master conf
    install -m 0644 ${WORKDIR}/default.conf ${D}${sysconfdir}/freesmartphone/alsa/
    # scenarii
    install -m 0644 ${WORKDIR}/default/* ${D}${sysconfdir}/freesmartphone/alsa/default/
}

FILES_${PN} = "\
    ${sysconfdir} \
"
