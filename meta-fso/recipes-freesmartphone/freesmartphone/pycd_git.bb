DESCRIPTION = "Python Clone Factory"
AUTHOR = "Jan Luebbe <jluebbe@debian.org>"
SECTION = "console/network"

require python-helpers.inc

LICENSE = "GPL-2.0+"
LIC_FILES_CHKSUM = "file://pyc.c;endline=19;md5=483dcff64d8ab106070a0498e4d0c446"

SRCREV = "${FSO_PYTHONHELPERS_SRCREV}"
PV = "0.1+gitr${SRCPV}"

S = "${WORKDIR}/git/${PN}"

RDEPENDS_${PN} = "\
  python-dbus \
  python-misc \
  python-pkgutil \
  python-resource \
"

do_compile() {
    ${CC} pyc.c -o pyc
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 pyc ${D}${bindir}/pyc
    install -d ${D}${sbindir}
    install -m 755 pycd.py ${D}${sbindir}/pycd
    install -d ${D}${sysconfdir}/dbus-1
    install -m 644 pycd.conf ${D}${sysconfdir}/dbus-1
}

