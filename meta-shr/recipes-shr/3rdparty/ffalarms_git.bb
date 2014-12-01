DESCRIPTION = "Finger friendly alarms"
AUTHOR = "Lukasz Pankowski <lukpank@o2.pl>"
MAINTAINER = "Lukasz Pankowski <lukpank@o2.pl>"
HOMEPAGE = "http://ffalarms.projects.openmoko.org/"
SECTION = "x11/applications"
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://COPYING;md5=f27defe1e96c2e1ecd4e0c9be8967949"
DEPENDS = "elementary libeflvala libical"
RDEPENDS_${PN} = "atd alsa-utils-amixer ttf-dejavu-sans libical"
RSUGGESTS_${PN} = "mplayer alsa-utils-aplay frameworkd"
PV = "0.4+gitr${SRCPV}"
PR = "r2"

#disable as-needed for now, fails on many undefined symbols otherwise
ASNEEDED = ""

PNBLACKLIST[ffalarms] = "Doesn't build after vala upgrade from 0.24.0 to 0.26.1"

SRC_URI = "git://git.shr-project.org/repo/ffalarms.git;protocol=http;branch=master \
"

SRCREV = "679a1e54f4993d16b4f048b80b0c970d92a54052"
S = "${WORKDIR}/git"

inherit vala

do_compile() {
        oe_runmake VAPIDIR=${STAGING_DATADIR}/vala/vapi
}
do_install() {
        oe_runmake install DESTDIR=${D} SYSCONFDIR=${sysconfdir}
}

FILES_${PN} += "${datadir}/${PN} ${datadir}/applications ${datadir}/pixmaps"

pkg_postinst_${PN}() {
    if [ -z "$D" ]; then
        if [ -f /etc/init.d/dbus-1 ]; then
             /etc/init.d/dbus-1 reload || true
        fi
    fi
}

pkg_postrm_${PN}() {
    if [ -z "$D" ]; then
        if [ -f /etc/init.d/dbus-1 ]; then
             /etc/init.d/dbus-1 reload || true
        fi
    fi
}
