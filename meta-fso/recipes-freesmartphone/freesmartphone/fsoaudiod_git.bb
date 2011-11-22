require cornucopia.inc
inherit fso-plugin

LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=78aab3f7875ffe21aebed9932fa3f993"

DEPENDS += "alsa-lib libcmtspeechdata"

# We need to uncomment the line below after the migration of all scenario files from
# fsodeviced to fsoaudiod is done. Otherwise we will get build errors as both fsodeviced
# and fsoaudiod are dependencies of the FSO framework.
# PROVIDES_${PN} = "openmoko-alsa-scenarios virtual/alsa-scenarios"

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.1.0+gitr${SRCPV}"
PE = "2"
PR = "${INC_PR}.5"

EXTRA_OECONF = "\
  --enable-cmtspeechdata \
"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 30"

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

PACKAGES =+ "${PN}-systemd"
FILES_${PN}-systemd += "${base_libdir}/systemd"
RDEPENDS_${PN}-systemd += "${PN}"

SRC_URI += "file://${PN}"

CONFFILES_${PN} = " \
  ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf \
  ${sysconfdir}/asound.conf \
"
RCONFLICTS_${PN} = "alsa-state"

do_install_append() {
  install -d ${D}${sysconfdir}/init.d/
  install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
  install -d ${D}${base_libdir}/systemd/system/
  install -m 0644 ${WORKDIR}/git/${PN}/data/${PN}.service ${D}${base_libdir}/systemd/system/${PN}.service
}

PACKAGES =+ "${PN}-alsa-plugins ${PN}-alsa-plugins-dbg ${PN}-alsa-plugins-dev"
FILES_${PN}-alsa-plugins = "${libdir}/alsa-lib/fsoaudio_session.so"
FILES_${PN}-alsa-plugins-dev = "${libdir}/alsa-lib/fsoaudio_session.la"
FILES_${PN}-alsa-plugins-dbg = "${libdir}/alsa-lib/.debug/fsoaudio_session.so"
