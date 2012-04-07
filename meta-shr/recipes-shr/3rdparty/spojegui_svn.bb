DESCRIPTION = "GUI for CLI program spoje.py (http://code.google.com/p/spoje/) for searching public transport connections in Czech Republic."
HOMEPAGE = "http://multi.xeres.cz/programovani/spojegui"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://src/spoje-gui.py;beginline=6;endline=17;md5=b9c4fb15d796c1241daf1c28217bf8a1"
AUTHOR = "Josef Jebavy <josef.jebavy@gmail.com>"
SECTION = "x11/applications"
RDEPENDS_${PN} = "python-core python-elementary python-sqlite3 python-netclient"

SRCREV = "68"
PV = "0.4.2+svnr${SRCPV}"
PR = "r1"

S = "${WORKDIR}/build"

SRC_URI = "svn://xeres.cz/spoje/trunk;module=build"

# override base_do_compile with own as included Makefile calls ipkg-build and it's not usefull to us at all
do_compile() {
}

do_install() {
  install -d "${D}/${bindir}"
  install -m 0755 "${S}/src/spoje.py" "${D}/${bindir}"
  install -m 0755 "${S}/src/spoje-gui.py" "${D}/${bindir}"
  install -d "${D}/${datadir}/pixmaps"
  install -m 0644 "${S}/img/train.png" "${D}/${datadir}/pixmaps"
  install -d "${D}/${datadir}/applications"
  install -m 0644 "${S}/data/spoje.desktop" "${D}/${datadir}/applications"
}

FILES_${PN} += " ${datadir}/applications/spoje.desktop ${datadir}/pixmaps"
