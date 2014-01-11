FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_armv4 = " \
  file://glamo.patch \
  file://tremor.build.fix.patch \
"

DEPENDS_append_armv4 = " tremor"

PACKAGECONFIG_append_armv4 = " mad"
EXTRA_OECONF_append_armv4 = " --enable-glamo --disable-libvorbis --enable-tremor"
PRINC := "${@int(PRINC) + 3}"
