DESCRIPTION = "ConfClerk is an application written in Qt, which makes conference schedules available offline."
AUTHOR = "Ixonos Plc., Philipp Spitzer, Gregor Herrmann, Stefan Strahl"
HOMEPAGE = "http://www.toastfreeware.priv.at/confclerk"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
RDEPENDS_${PN} = "qt4-plugin-sqldriver-sqlite"

SRC_URI = "http://www.toastfreeware.priv.at/tarballs/confclerk/${PN}-${PV}.tar.gz"
SRC_URI[md5sum] = "503fdcbcf7eb9be31caa85ce20903833"
SRC_URI[sha256sum] = "3da892836bdcbb4d6ecd379ef66eb9c78236a96d8fa6cf7c35eba54b4659e0f7"

inherit qt4x11

EXTRA_QMAKEVARS_PRE += " PREFIX=/usr"

FILES_${PN}-dbg += "${bindir}/.debug"

do_install() {
       oe_runmake install INSTALL_ROOT=${D}
}
