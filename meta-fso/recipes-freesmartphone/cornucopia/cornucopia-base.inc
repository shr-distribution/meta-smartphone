DESCRIPTION = "The freesmartphone.org API reference implementation (FSO 2.0)"
AUTHOR = "Team FSO"
HOMEPAGE = "http://www.freesmartphone.org"
LICENSE = "LGPLv2.1"
LIC_FILES_CHKSUM = "file://COPYING;md5=fbc093901857fcd118f065f900982c24"
SECTION = "devel"
DEPENDS = "vala-native glib-2.0 libfso-glib libgee"
DEPENDS += "${@['libfsoframework', ''][(bb.data.getVar('PN', d, 1) in 'libfsoframework'.split())]}"

PE = "2"
INC_PR = "r4"

# ugly but works
SRC_VER = "${@bb.data.getVar('PV',d,1).replace('0.11.999+0.12.0-rc', '0.12.0-rc')}"

def get_shrt_ver(bb, d):
    SRC_VER = bb.data.getVar('SRC_VER',d,1);
    SHRT_VER = "%s.%s" % (SRC_VER.split('.')[0], SRC_VER.split('.')[1])
    if SRC_VER.find('-rc') != -1:
        SHRT_VER += SRC_VER[SRC_VER.find('-rc'):]
    return SHRT_VER

SHRT_VER = "${@get_shrt_ver(bb, d)}"

SRC_URI = "http://downloads.freesmartphone.org/sources/cornucopia/${SHRT_VER}/${PN}-${PV}.tar.bz2"

S = "${WORKDIR}/${PN}-${SRC_VER}"

inherit autotools pkgconfig vala

FILES_${PN} += "\
  ${sysconfdir} \
  ${datadir}\${PN} \
  ${datadir}/dbus-1 \
  ${libdir}/cornucopia/modules/*/lib*.so \
"
FILES_${PN}-dev += "\
  ${libdir}/cornucopia/modules/*/lib*.la \
"
FILES_${PN}-dbg += "\
  ${libdir}/cornucopia/modules/*/.debug \
"
