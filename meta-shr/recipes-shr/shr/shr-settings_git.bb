DESCRIPTION = "Modular settings application for SHR based on python-elementary"
HOMEPAGE = "http://shr-project.org"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://shr-settings;endline=4;md5=ec1482bfb96c1ba7a2d5c69812980bf2"
RDEPENDS_${PN} = "python-elementary python-dbus python-codecs python-shell python-pyrtc python python-core python-edbus dbus-x11 frameworkd python-phoneutils python-pexpect"
SECTION = "x11/application"
SRCREV = "cc5de00964f7ec8008f06b9ac84b72dd6755575f"
PE = "1"
PV = "0.1.1+gitr${SRCPV}"
PR = "r1"

inherit setuptools

PACKAGES =+ "\
  ${PN}-addons-illume \
  ${PN}-backup-configuration \
"

RRECOMMENDS_${PN} = "\
  ${PN}-addons-illume \
  ${PN}-backup-configuration \
"

do_install_append() {
  install -d ${D}/${sysconfdir}/profile.d/
  install -m 0755 "${WORKDIR}/elementary.sh" "${D}/${sysconfdir}/profile.d/elementary.sh"
}

SRC_URI = "git://git.shr-project.org/repo/shr-settings.git;protocol=http;branch=master \
           file://elementary.sh"
S = "${WORKDIR}/git"
FILES_${PN} += "${sysconfdir}/profile.d/elementary.sh"
FILES_${PN} += "${prefix}/share/pixmaps"
FILES_${PN} += "${prefix}/share/applications"
FILES_${PN}-addons-illume = "${prefix}/share/applications/shr-settings-addons-illume"
FILES_${PN}-backup-configuration = "${sysconfdir}/shr-settings/"
CONFFILES_${PN} = "${sysconfdir}/profile.d/elementary.sh"
CONFFILES_${PN}-backup-configuration = "\
  ${sysconfdir}/shr-settings/backup.conf \
  ${sysconfdir}/shr-settings/backup.blacklist \
  ${sysconfdir}/shr-settings/backup.whitelist \
"

PNBLACKLIST[shr-settings] ?= "Runtime depends on blacklisted python-edbus - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[shr-settings] ?= "Runtime depends on blacklisted python-elementary - the recipe will be removed on 2017-09-01 unless the issue is fixed"
