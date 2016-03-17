DESCRIPTION = "Advanced Geocaching Tool for Linux - Towards paperless geocaching!"
SECTION = "devel/python"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://advancedcaching/core.py;beginline=4;endline=16;md5=be89f9b328f6de84d438ceccd89e54e0"
AUTHOR = "Daniel Fett"
HOMEPAGE = "http://www.danielfett.de/internet-und-opensource,software,agtl"
RDEPENDS_${PN} = "python-pygtk python-html python-image python-netclient python-misc python-sqlite3 python-mime python-json python-lxml"
SRCREV = "8de517067329f9663139293289f811c0bf3712af"
PV = "0.9.1.0+gitr${SRCPV}"

SRC_URI = "git://github.com/webhamster/advancedcaching.git;protocol=git;branch=master \
  file://0001-build-add-default-opkg-build.patch \
"

inherit distutils

S = "${WORKDIR}/git"

do_compile_append() {
  sed -i "s#./core.py#${PYTHON_SITEPACKAGES_DIR}/${PN}/core.py#g" ${S}/agtl
}

FILES_${PN} += "${datadir}/applications/* ${datadir}/icons/* ${datadir}/agtl"

PNBLACKLIST[advancedcaching] ?= "RDEPENDS on python-pygtk was removed from oe-core"
