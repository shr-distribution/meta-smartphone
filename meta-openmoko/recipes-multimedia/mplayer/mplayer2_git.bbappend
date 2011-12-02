FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_om-gta02 = " \
  file://glamo.patch \
  file://tremor.build.fix.patch \
"

DEPENDS += "tremor"

EXTRA_OECONF_append_om-gta02 = " --enable-glamo --disable-libvorbis --enable-tremor"
PRINC := "${@int(PRINC) + 2}"
