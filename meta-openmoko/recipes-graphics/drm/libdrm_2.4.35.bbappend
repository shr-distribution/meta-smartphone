FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
SRC_URI_append_armv4 = " file://glamo.patch"
PRINC := "${@int(PRINC) + 2}"

EXTRA_OECONF_append_armv4 = " --enable-glamo-experimental-api"
