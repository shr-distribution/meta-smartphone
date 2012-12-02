DESCRIPTION = "Multiple cat utility"
AUTHOR = "Michael 'Mickey' Lauer <mlauer@vanille-media.de>"
SECTION = "console"

require python-helpers.inc

LICENSE = "GPL-2.0+"
LIC_FILES_CHKSUM = "file://multicat;endline=8;md5=edbb42725a55aa6f91dfc070c43907c9"

SRCREV = "${FSO_PYTHONHELPERS_SRCREV}"
PV = "0.0.0+gitr${SRCPV}"
PR = "r2"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 multicat/multicat ${D}${bindir}
}

RDEPENDS_${PN} = "\
  python-core \
"
