DESCRIPTION = "Finger friendly phonelog"
AUTHOR = "Lukasz Pankowski <lukpank@o2.pl>"
HOMEPAGE = "http://www.opkg.org/package_343.html"
SECTION = "x11/applications"
PRIORITY = "optional"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://ffphonelog.vala;endline=16;md5=37b136bcd8f47e0d65fcaf815b61db75"
DEPENDS = "libeflvala"
RDEPENDS_${PN} = "phoneuid"
PV = "0.1+gitr${SRCPV}"
PR = "r1"

SRC_URI = "\
	git://git.shr-project.org/repo/ffphonelog.git;protocol=http;branch=master \
"

export XDG_DATA_DIRS = "${STAGING_DATADIR}"

SRCREV = "316fc86cbd9ef7a547117541f816ce10adae37d4"
S = "${WORKDIR}/git"

FILES_${PN} += "${datadir}/applications ${datadir}/pixmaps"

EXTRA_OEMAKE = " \
	CC='${CC}' \
	CFLAGS_APPEND='${CFLAGS}' \
	LDFLAGS_APPEND='${LDFLAGS}' \
	DESTDIR='${D}' \
	PREFIX=/usr"

do_install() {
       oe_runmake install
}

MAINTAINER = "Lukasz Pankowski <lukpank@o2.pl>"
