require cornucopia.inc
inherit fso-plugin

LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

DEPENDS += "alsa-lib libcmtspeechdata libsamplerate0"

# We need to uncomment the line below after the migration of all scenario files from
# fsodeviced to fsoaudiod is done. Otherwise we will get build errors as both fsodeviced
# and fsoaudiod are dependencies of the FSO framework.
# PROVIDES_${PN} = "openmoko-alsa-scenarios virtual/alsa-scenarios"

SRCREV = "${FSO_CORNUCOPIA_SRCREV}"
PV = "0.11.0+gitr${SRCPV}"
PE = "2"
PR = "${INC_PR}.0"

EXTRA_OECONF = "\
  --enable-cmtspeechdata \
  --enable-samplerate \
"

inherit update-rc.d

INITSCRIPT_NAME = "${PN}"
INITSCRIPT_PARAMS = "defaults 30"

inherit systemd
SYSTEMD_PACKAGES = "${PN}-systemd"
SYSTEMD_SERVICE = "${PN}.service"

RDEPENDS_${PN} += "${PN}-config"

SRC_URI += "file://${PN}"

FILES_${PN}-config = " \
  ${sysconfdir}/asound.conf \
  ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf \
"
CONFFILES_${PN}-config = " \
  ${sysconfdir}/asound.conf \
  ${sysconfdir}/freesmartphone/conf/palm_pre/${PN}.conf \
"
RCONFLICTS_${PN} = "alsa-state"

do_install_append() {
  install -d ${D}${sysconfdir}/init.d/
  install -m 0755 ${WORKDIR}/${PN} ${D}${sysconfdir}/init.d/
  install -d ${D}${systemd_unitdir}/system/
  install -m 0644 ${S}/data/${PN}.service ${D}${systemd_unitdir}/system/${PN}.service
}

PACKAGES =+ "${PN}-alsa-plugins ${PN}-alsa-plugins-dbg ${PN}-alsa-plugins-dev ${PN}-config"
FILES_${PN}-alsa-plugins = "${libdir}/alsa-lib/fsoaudio_session.so"
FILES_${PN}-alsa-plugins-dev = "${libdir}/alsa-lib/fsoaudio_session.la"
FILES_${PN}-alsa-plugins-dbg = "${libdir}/alsa-lib/.debug/fsoaudio_session.so"
