DESCRIPTION = "A simple calculator which is elementary-themed"
HOMEPAGE = "http://github.com/spaetz/calc"
AUTHOR = "Sebastian Spaeth <Sebastian@SSpaeth.de>"
LICENSE  = "MIT"
LIC_FILES_CHKSUM = "file://ecalc;endline=19;md5=4db4fc5e62ce4982122df4db99e80b85"
RDEPENDS_${PN} = "python-elementary python python-edbus"
SECTION = "x11/application"
PR = "r1"
RPROVIDES_${PN} = "calc"

SRCREV = "a226c689d801330eabab08dea0b66dbc8d18b851"
SRC_URI = "git://github.com/spaetz/calc.git;protocol=http;branch=master"
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
