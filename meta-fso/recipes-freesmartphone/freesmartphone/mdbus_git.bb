DESCRIPTION = "Mickey's DBus introspection and calling Program"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
SECTION = "console/network"

require python-helpers.inc
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://mickeydbus/mdbus;beginline=3;endline=9;md5=f306ba812e2de9cafe2e0b57f73c415b"

SRCREV = "${FSO_PYTHONHELPERS_SRCREV}"
PV = "0.9.2+gitr${SRCPV}"
PR = "r3"

do_install() {
	install -d ${D}${bindir}
	install -m 0755 mickeydbus/mdbus ${D}${bindir}
}

RDEPENDS_${PN} = "\
  python-dbus \
  python-pygobject \
  python-pprint \
  python-xml \
"
