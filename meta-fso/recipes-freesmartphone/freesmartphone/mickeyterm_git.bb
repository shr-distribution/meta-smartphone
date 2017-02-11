DESCRIPTION = "Mickey's Terminal Program"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
SECTION = "console/network"

require python-helpers.inc
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://mickeyterm/mickeyterm;beginline=2;endline=14;md5=28d1e2f4654d1f2e546ebf429210faed"

SRCREV = "${FSO_PYTHONHELPERS_SRCREV}"
PV = "2.9.1+gitr${SRCPV}"
PR = "r3"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 mickeyterm/mickeyterm ${D}${bindir}
}

RDEPENDS_${PN} = "\
  python-readline \
  python-pyserial \
  python-textutils \
  python-threading \
"
RRECOMMENDS_${PN} += "\
  fso-gsm0710muxd \
  python-dbus \
"

PNBLACKLIST[mickeyterm] ?= "Runtime depends on blacklisted fso-gsm0710muxd"
