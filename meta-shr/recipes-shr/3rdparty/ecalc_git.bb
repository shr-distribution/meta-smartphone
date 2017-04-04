DESCRIPTION = "A simple calculator which is elementary-themed"
HOMEPAGE = "http://github.com/spaetz/calc"
AUTHOR = "Sebastian Spaeth <Sebastian@SSpaeth.de>"
LICENSE  = "MIT"
LIC_FILES_CHKSUM = "file://ecalc;endline=19;md5=4db4fc5e62ce4982122df4db99e80b85"
RDEPENDS_${PN} = "python-elementary python python-edbus"
SECTION = "x11/application"
RPROVIDES_${PN} = "calc"

PV = "0.0.4+gitr${SRCPV}"
PE = "1"

SRCREV = "7ac9b7638b5e55171f270d5bd4d826db77a7720f"
SRC_URI = "git://github.com/spaetz/calc.git;protocol=http;branch=master \
"
S = "${WORKDIR}/git"

do_install(){
        install -d ${D}${datadir}/applications
        install -m 0644 ${S}/data/ecalc.desktop ${D}${datadir}/applications/
        install -d ${D}${datadir}/pixmaps
        install -m 0644 ${S}/data/calculator.png ${D}${datadir}/pixmaps/
        install -d ${D}${bindir}
        install -m 0744 ${S}/ecalc ${D}${bindir}/
}

FILES_${PN} += "${prefix}/share/pixmaps"
FILES_${PN} += "${prefix}/share/applications"

PNBLACKLIST[ecalc] ?= "Runtime depends on blacklisted python-edbus - the recipe will be removed on 2017-09-01 unless the issue is fixed"

PNBLACKLIST[ecalc] ?= "Runtime depends on blacklisted python-elementary - the recipe will be removed on 2017-09-01 unless the issue is fixed"
