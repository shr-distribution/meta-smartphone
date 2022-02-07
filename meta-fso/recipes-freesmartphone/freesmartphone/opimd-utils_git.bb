DESCRIPTION = "Test scripts for freesmartphone.org opimd interface"
HOMEPAGE = "http://freesmartphone.org"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://setup.py;beginline=21;endline=21;md5=7fdad70bdcd4f6b50d634caae99e60d7 \
                    file://opimd-dates;beginline=4;endline=9;md5=d866a506fcc240d5af028dd8a0bbb6ae \
"
RDEPENDS_${PN} = "python-elementary python-dbus python-codecs python-shell python python-core python-edbus frameworkd"
SECTION = "x11/application"
SRCREV = "b9693e1dac778929d345cbb2be8fae6d2fa79888"
PV = "0.0.3+gitr${SRCPV}"
PE = "1"

inherit setuptools

PACKAGES =+ "\
  ${PN}-cli \
  ${PN}-notes \
  ${PN}-data \
"

RDEPENDS_${PN} = "\
  ${PN}-data \
  shr-settings \
  ffphonelog \
  shr-theme \
"

RDEPENDS_${PN}-notes = "\
  ${PN}-data \
"

RRECOMMENDS_${PN} = "\
  ${PN}-notes \
  ${PN}-cli \
"

SRC_URI = "git://github.com/shr-distribution/opimd-utils;protocol=https"
S = "${WORKDIR}/git"

FILES_${PN} += "${sysconfdir}/X11/Xsession.d/89opimd-notifier"
FILES_${PN} += "${prefix}/share/applications/"
FILES_${PN} += "${prefix}/share/pixmaps/"
FILES_${PN}-data += "${prefix}/share/pixmaps/opimd-utils/"
FILES_${PN}-cli += "${prefix}/bin/opimd-cli"
FILES_${PN}-notes += "${prefix}/bin/opimd-notes"
FILES_${PN}-notes += "${prefix}/share/pixmaps/opimd-notes.png"
FILES_${PN}-notes += "${prefix}/share/applications/opimd-notes.desktop"
