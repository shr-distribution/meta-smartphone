DESCRIPTION = "Finger friendly alarms"
AUTHOR = "Lukasz Pankowski <lukpank@o2.pl>"
HOMEPAGE = "http://ffalarms.projects.openmoko.org/"
SECTION = "x11/applications"
PRIORITY = "optional"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
DEPENDS = "elementary libeflvala libical"
RDEPENDS_${PN} = "atd alsa-utils-amixer ttf-dejavu-sans libical"
RSUGGESTS_${PN} = "mplayer alsa-utils-aplay frameworkd"
PV = "0.4+gitr${SRCPV}"

#disable as-needed for now, fails on many undefined symbols otherwise
ASNEEDED = ""

SRC_URI = "git://git.shr-project.org/repo/ffalarms.git;protocol=http;branch=master \
"

SRCREV = "482023c0b0357de56a1f66f4b438cc2846888720"
S = "${WORKDIR}/git"

inherit vala

do_compile() {
        oe_runmake VAPIDIR=${STAGING_DATADIR}/vala/vapi
}
do_install() {
        oe_runmake install DESTDIR=${D} SYSCONFDIR=${sysconfdir}
}

pkg_postinst_${PN}() {
#!/bin/sh
/etc/init.d/dbus-1 reload
}

pkg_postrm_${PN}() {
#!/bin/sh
/etc/init.d/dbus-1 reload
}

FILES_${PN} += "${datadir}/${PN} ${datadir}/applications ${datadir}/pixmaps"

MAINTAINER = "Lukasz Pankowski <lukpank@o2.pl>"
