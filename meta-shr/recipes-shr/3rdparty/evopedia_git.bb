DESCRIPTION = "evopedia is an offline Wikipedia viewer "
AUTHOR = "Christian Reitwießner"
HOMEPAGE = "http://www.reitwiessner.de/openmoko/evopedia.html"
LICENSE = "GPLv3+"
LIC_FILES_CHKSUM = "file://debian/copyright;md5=4f00322f2567b19a412b9c77e6bad425"

DEPENDS = "bzip2"

PV = "0.4.2+gitr${SRCPV}"

SRC_URI = "git://gitorious.org/evopedia/evopedia.git;protocol=git;branch=master"

SRCREV = "36ca70acb457762d623e8e6c83039256dbe5c7f4"
S = "${WORKDIR}/git"

inherit qt4x11

EXTRA_QMAKEVARS_PRE += " PREFIX=/usr"

FILES_${PN}-dbg += "${bindir}/.debug"

do_install() {
       oe_runmake install INSTALL_ROOT=${D}
}
